package org.right_brothers.agents;

import java.util.*;
// import java.util.stream.Collectors;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

// import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
// import jade.domain.FIPAException;
// import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.right_brothers.agents.BaseAgent;
import org.right_brothers.bakery_objects.Tray;
import org.right_brothers.bakery_objects.UnbakedProduct;
import org.right_brothers.utils.InputParser; 
import org.right_brothers.data.models.Order;
import org.right_brothers.data.models.Product;
import org.right_brothers.data.models.Oven;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Step;
import org.right_brothers.data.messages.UnbakedProductMessage;
import org.right_brothers.data.messages.BakedProductMessage;
import org.right_brothers.utils.JsonConverter;

@SuppressWarnings("serial")
public class OvenManager extends BaseAgent {
    private AID intermediater = new AID("intermediater", AID.ISLOCALNAME);
    private AID proofer = new AID("dummy", AID.ISLOCALNAME);
    private AID orderProcessor = new AID("dummy", AID.ISLOCALNAME);
    private List<Product> availableProducts;
    private String bakeryGuid = "bakery-001";
    private List<Tray> trays;
    private List<UnbakedProduct> unbakedProducts;
    private List<Order> orders;

    protected void setup() {
        super.setup();
        System.out.println("\tOven-manager "+getAID().getLocalName()+" is born.");

        this.register("Oven-manager-agent", "JADE-bakery");

        this.orders = new ArrayList();
        this.unbakedProducts = new ArrayList<UnbakedProduct> ();
        this.availableProducts = new ArrayList<Product> ();

        // TODO: get bakery guid as argument
        //         Object[] args = getArguments();

        this.getAllInformation();

        this.addBehaviour(new OrderServer(orderProcessor));
        this.addBehaviour(new UnbakedProductsServer(proofer));
        this.addBehaviour(new Bake());
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    private void getAllInformation(){
		InputParser<Vector<Bakery>> parser2 = new InputParser<>
			("/config/sample/bakeries.json", new TypeReference<Vector<Bakery>>(){});
		List<Bakery> bakeries = parser2.parse();
        List<Oven> ovens = new ArrayList<Oven> ();
        for (Bakery b : bakeries) {
            if (b.getGuid().equalsIgnoreCase(this.bakeryGuid)){
                this.availableProducts = b.getProducts();
                ovens = b.getEquipment().getOvens();
                break;
            }
        }
        System.out.println("Number of oven " + ovens.size());
        this.trays = new ArrayList<Tray> (ovens.size() * 4);
        for (Oven o : ovens) {
            for (int i = 0; i < 4; i++) {
                Tray t = new Tray(o, Integer.toString(i));
                this.trays.add(t);
            }
        }
        System.out.println("Number of trays " + this.trays.size());
    }

    private class Bake extends CyclicBehaviour{
        public void action(){
            if (!baseAgent.getAllowAction()) {
                return;
            }
            if (baseAgent.getCurrentHour() == 0) {
                this.resumeBaking();
            }
            if (baseAgent.getCurrentHour() <= 12) {
                ArrayList<BakedProductMessage> message = this.getBakedProducts();
                if (message.size() > 0) {
                    this.sendBakedProducts(message);
                }
                this.scheduleProducts();
                this.bakeProducts();
            }
            if (baseAgent.getCurrentHour() == 12) {
                this.haltBaking();
            }
            baseAgent.finished();
        }
        private void resumeBaking(){
            for (Tray t : trays) {
                if (t.isFree())
                    continue;
                UnbakedProduct pm = t.getUsedFor();
                if (pm.isScheduled())
                    continue;
                pm.setProcessStartTime(baseAgent.getCurrentHour());
                System.out.println("\tResumed Baking " + pm.getQuantity() + " " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
            }
        }
        private void haltBaking(){
            for (Tray t : trays) {
                if (t.isFree())
                    continue;
                UnbakedProduct pm = t.getUsedFor();
                if (pm.isScheduled())
                    continue;
                int alreadyBakedTime = baseAgent.getCurrentHour() - pm.getProcessStartTime();
                pm.setBakingDuration(pm.getBakingDuration() - alreadyBakedTime);
                pm.setProcessStartTime(-1);
                System.out.println("\tHalted Baking " + pm.getQuantity() + " " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
            }
        }
        private ArrayList<BakedProductMessage> getBakedProducts() {
            ArrayList<BakedProductMessage> message = new ArrayList<BakedProductMessage> ();
            for (Tray t : trays) {
                if (t.isFree())
                    continue;
                UnbakedProduct pm = t.getUsedFor();
                if (pm.isScheduled())
                    continue;
                if (baseAgent.getCurrentHour() >= pm.getProcessStartTime() + pm.getBakingDuration() + 1){
                    System.out.println("\tBaked " + pm.getQuantity() + " " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
                    BakedProductMessage bpm = new BakedProductMessage();
                    bpm.setGuid(pm.getGuid());
                    bpm.setQuantity(pm.getQuantity());
                    bpm.setCoolingDuration(pm.getCoolingDuration());
                    bpm.setIntermediateSteps(pm.getIntermediateSteps());
                    message.add(bpm);
                    t.setUsedFor(null);
                }
            }
            return message;
        }
        private void sendBakedProducts(ArrayList<BakedProductMessage> message) {
            String messageContent = JsonConverter.getJsonString(message);
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(intermediater);
            inform.setContent(messageContent);
            inform.setConversationId("Baked-products-001");
            baseAgent.sendMessage(inform);
        }
        private void bakeProducts(){
            ArrayList<UnbakedProduct> temp = new ArrayList<UnbakedProduct> ();
            for (UnbakedProduct pm : unbakedProducts) {
                if (pm.isScheduled()) {
                    Tray t = pm.getScheduled();
                    if (t.getTemp() == pm.getBakingTemp()){
                        temp.add(pm);
                        pm.setScheduled(null);
                        pm.setProcessStartTime(baseAgent.getCurrentHour());
                        System.out.println("\tStarted Baking " + pm.getQuantity() + " " + pm.getGuid() + " at " + baseAgent.getCurrentHour());
                    }
                    else {
                        t.setNextTimeStepTemp();
                    }
                }
            }
            for (UnbakedProduct p : temp) {
                unbakedProducts.remove(p);
            }
        }
        private void scheduleProducts(){
            for (UnbakedProduct pm : unbakedProducts) {
                if (pm.isScheduled())
                    continue;
                Tray t = this.getFreeTray();
                if (t == null) 
                    break;
                t.setUsedFor(pm);
                pm.setScheduled(t);
                System.out.println("\tScheduled " + pm.getQuantity() + " " + pm.getGuid() + " at " + baseAgent.getCurrentHour());
            }
        }
        private Tray getFreeTray(){
            for (Tray t : trays) {
                if (t.isFree()){
                    return t;
                }
            }
            return null;
        }
    }

    /*
     * Server for the order guid for the dough preparation stage agent's(proofer) message
     * */
    private class UnbakedProductsServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public UnbakedProductsServer(AID proofer){
            this.sender = proofer;
        }
        public void action() {
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("order_guid"));
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                String messageContent = msg.getContent();
                System.out.println("\tReceived Unbaked product " + messageContent);
                UnbakedProductMessage upm = this.parseUnbakedProductMessage(messageContent);
                /*
                 * Just add quantity to already existing product if possible, otherwise
                 * add the whole product to the queue
                 */
                boolean alreadyAdded = false;
                for (UnbakedProduct up : unbakedProducts) {
                    if (up.getGuid().equals(upm.getProductType())){
                        int newQuantity = this.getTotalQuantity(upm.getProductQuantities());
                        if (up.getQuantity() + newQuantity <= up.getBreadsPerOven()){
                            up.setQuantity(up.getQuantity() + newQuantity);
                            alreadyAdded = true;
                            break;
                        }
                        else {
                            int remainingQuantity = up.getBreadsPerOven() - up.getQuantity();
                            up.setQuantity(up.getBreadsPerOven());
                            newQuantity -= remainingQuantity;
                            this.iterativelyAddUnbakedProducts(newQuantity, up);
                            alreadyAdded = true;
                            break;
                        }
                    }
                }
                if (!alreadyAdded){
                    UnbakedProduct up = this.getUnbakedProductFromProductName(upm.getProductType());
                    int newQuantity = this.getTotalQuantity(upm.getProductQuantities());
                    this.iterativelyAddUnbakedProducts(newQuantity, up);
                }
            }
            else {
                block();
            }
        }
        private UnbakedProductMessage parseUnbakedProductMessage(String orderString){
            ObjectMapper mapper = new ObjectMapper();
            try {
                UnbakedProductMessage data = mapper.readValue(orderString, UnbakedProductMessage.class);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        private UnbakedProduct getUnbakedProductFromProductName(String productName){
            UnbakedProduct up = new UnbakedProduct();
            Product p = this.getProductWithSameGuid(productName);
            up.setGuid(p.getGuid());
            up.setBakingTemp(p.getRecipe().getBakingTemp());
            up.setBreadsPerOven(p.getBatch().getBreadsPerOven());
            Vector<Step> steps = new Vector<Step> ();
            boolean addStep = false;
            // ASSUMPTION: The steps are in order of recipe.
            for (Step s : p.getRecipe().getSteps()) {
                if (s.getAction().equals("baking")){
                    up.setBakingDuration(s.getDuration());
                    addStep = true;
                    continue;
                }
                if (s.getAction().equals("cooling")){
                    up.setCoolingDuration(s.getDuration());
                    addStep = false;
                    break;
                }
                if (addStep) {
                    steps.add(s);
                }
            }
            up.setIntermediateSteps(steps);
            return up;
        }
        private Product getProductWithSameGuid(String productName){
            for (Product p : availableProducts) {
                if (p.getGuid().equals(productName)){
                    return p;
                }
            }
            System.out.println("Product with name " + productName + " is not offered by " + bakeryGuid);
            // TODO: make codacy approved Error
            // throw new Error("Product with name " + productName + " is not offered by " + bakeryGuid);
            return null;
        }
        private int getTotalQuantity(Vector<Integer> vec){
            int sum = 0;
            for (int i : vec)
                sum += i;
            return sum;
        }
        private void iterativelyAddUnbakedProducts(int quantity, UnbakedProduct up) {
            int newQuantity = quantity;
            while (newQuantity > 0) {
                UnbakedProduct newUp = up.clone();
                int quantityToBeAdded = ((newQuantity <= up.getBreadsPerOven()) ? newQuantity : newQuantity - up.getBreadsPerOven());
                newQuantity -= quantityToBeAdded;
                newUp.setQuantity(quantityToBeAdded);
                unbakedProducts.add(newUp);
            }
        }

    }
    /*
     * Server for the order from order processing agent's message
     * */
    private class OrderServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public OrderServer(AID orderProcessor){
            this.sender = orderProcessor;
        }
        public void action() {
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("order"));
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                String order = msg.getContent();
                Order o = this.parseOrder(order);
                System.out.println("\tReceived Order with guid: " + o.getGuid());
                orders.add(o);
            }
            else {
                block();
            }
        }
        private Order parseOrder(String orderString){
            ObjectMapper mapper = new ObjectMapper();
            try {
                Order data = mapper.readValue(orderString, Order.class);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
