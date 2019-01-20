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

import org.maas.agents.BaseAgent;
import org.right_brothers.bakery_objects.Tray;
import org.right_brothers.bakery_objects.UnbakedProduct;
import org.right_brothers.utils.InputParser; 
import org.right_brothers.data.models.Product;
import org.right_brothers.data.models.Oven;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Step;
import org.right_brothers.data.messages.UnbakedProductMessage;
import org.right_brothers.data.messages.BakedProductMessage;
import org.maas.utils.JsonConverter;
import org.maas.utils.Time;

@SuppressWarnings("serial")
public class OvenManager extends BaseAgent {
    private AID postBakingProcessor;
    private List<Product> availableProductList;
    private String bakeryGuid = "bakery-001";
    private List<Tray> trayList;
    private List<UnbakedProduct> unbakedProductList;
    private int bakedProductConversationNumber = 0;
    private boolean verbose = false;

    protected void setup() {
        super.setup();
        System.out.println("\tOven-manager "+getAID().getLocalName()+" is born.");

        String scenarioDirectory = "small";
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bakeryGuid = (String) args[0];
            scenarioDirectory = (String) args [1];
        }
        postBakingProcessor = new AID(this.bakeryGuid + "-postBakingProcessor", AID.ISLOCALNAME);
        // AID proofer = new AID(this.bakeryGuid + "-dummy-proofer", AID.ISLOCALNAME);
        AID proofer = new AID("Proofer_" + this.bakeryGuid, AID.ISLOCALNAME);

        this.register("Oven-manager-agent", this.bakeryGuid+"-OverManager");

        this.unbakedProductList = new ArrayList<UnbakedProduct> ();
        this.availableProductList = new ArrayList<Product> ();

        this.getAllInformation(scenarioDirectory);

        this.addBehaviour(new UnbakedProductsServer(proofer));
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    private void getAllInformation(String scenarioDirectory){
        String filePath = "/config/" + scenarioDirectory + "/bakeries.json";
		InputParser<Vector<Bakery>> parser2 = new InputParser<>
			(filePath, new TypeReference<Vector<Bakery>>(){});
		List<Bakery> bakeries = parser2.parse();
        List<Oven> ovens = new ArrayList<Oven> ();
        for (Bakery b : bakeries) {
            if (b.getGuid().equalsIgnoreCase(this.bakeryGuid)){
                this.availableProductList = b.getProducts();
                ovens = b.getEquipment().getOvens();
                break;
            }
        }
        this.print("Number of oven " + ovens.size());
        this.trayList = new ArrayList<Tray> (ovens.size() * 4);
        for (Oven o : ovens) {
            for (int i = 0; i < 4; i++) {
                Tray t = new Tray(o, Integer.toString(i));
                this.trayList.add(t);
            }
        }
        this.print("Number of trayList " + this.trayList.size());
    }

    /*
     * Baking products
     */
    @Override
    protected void stepAction(){
        if (baseAgent.getCurrentTime().lessThan(new Time(baseAgent.getCurrentDay(), 12, 0))){
            ArrayList<BakedProductMessage> message = this.getBakedProducts();
            if (message.size() > 0) {
                this.sendBakedProducts(message);
            }
            this.scheduleProducts();
            this.startBakingProducts();
        }
        baseAgent.finished();
    }
    private ArrayList<BakedProductMessage> getBakedProducts() {
        ArrayList<BakedProductMessage> message = new ArrayList<BakedProductMessage> ();
        for (Tray t : trayList) {
            if (t.isFree())
                continue;
            UnbakedProduct unbakedProduct = t.getUsedFor();
            if (unbakedProduct.isScheduled())
                continue;
            if (unbakedProduct.getRemainingTimeDuration() == 0){
                this.print("\tBaked " + unbakedProduct.getQuantity() + " " + unbakedProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
                BakedProductMessage bakedProductMessage = new BakedProductMessage();
                bakedProductMessage.setGuid(unbakedProduct.getGuid());
                bakedProductMessage.setQuantity(unbakedProduct.getQuantity());
                bakedProductMessage.setCoolingDuration(unbakedProduct.getCoolingDuration());
                bakedProductMessage.setIntermediateSteps(unbakedProduct.getIntermediateSteps());
                message.add(bakedProductMessage);
                t.setUsedFor(null);
            }
            else {
                unbakedProduct.setRemainingTimeDuration(unbakedProduct.getRemainingTimeDuration() - 1);
            }
        }
        return message;
    }
    private void sendBakedProducts(ArrayList<BakedProductMessage> message) {
        String messageContent = JsonConverter.getJsonString(message);
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.addReceiver(postBakingProcessor);
        inform.setContent(messageContent);
        bakedProductConversationNumber ++;
        inform.setConversationId("Baked-products-" + Integer.toString(bakedProductConversationNumber));
        baseAgent.sendMessage(inform);
    }
    private void startBakingProducts(){
        ArrayList<UnbakedProduct> temp = new ArrayList<UnbakedProduct> ();
        for (UnbakedProduct unbakedProduct : unbakedProductList) {
            if (unbakedProduct.isScheduled()) {
                Tray tray = unbakedProduct.getScheduled();
                if (tray.getTemp() == unbakedProduct.getBakingTemp()){
                    temp.add(unbakedProduct);
                    unbakedProduct.setScheduled(null);
                    unbakedProduct.setRemainingTimeDuration(unbakedProduct.getBakingDuration());
                    this.print("\tStarted Baking " + unbakedProduct.getQuantity() + " " + unbakedProduct.getGuid() + " at " + baseAgent.getCurrentHour());
                }
                else {
                    tray.setNextTimeStepTemp();
                }
            }
        }
        for (UnbakedProduct p : temp) {
            unbakedProductList.remove(p);
        }
    }
    private void scheduleProducts(){
        for (UnbakedProduct unbakedProduct : unbakedProductList) {
            if (unbakedProduct.isScheduled())
                continue;
            Tray tray = this.getFreeTray();
            if (tray == null) 
                break;
            tray.setUsedFor(unbakedProduct);
            unbakedProduct.setScheduled(tray);
            this.print("\tScheduled " + unbakedProduct.getQuantity() + " " + unbakedProduct.getGuid() + " at " + baseAgent.getCurrentHour());
        }
    }
    private Tray getFreeTray(){
        for (Tray t : trayList) {
            if (t.isFree()){
                return t;
            }
        }
        return null;
    }
    private void print(String str){
        if (this.verbose){
            System.out.println(str);
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
            MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("dough-Notification"));
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                String messageContent = msg.getContent();
                print("\tReceived Unbaked product " + messageContent);
                ACLMessage reply = msg.createReply();
                reply.setConversationId("dough-Notification-reply");
                reply.setPerformative(ACLMessage.CONFIRM);
                baseAgent.sendMessage(reply);
                TypeReference<?> type = new TypeReference<UnbakedProductMessage>(){};
                UnbakedProductMessage unbakedProductMessage = JsonConverter.getInstance(messageContent, type);
                /*
                 * Just add quantity to already existing product if possible, otherwise
                 * add the whole product to the queue
                 */
                boolean alreadyAdded = false;
                for (UnbakedProduct up : unbakedProductList) {
                    if (up.getGuid().equals(unbakedProductMessage.getProductType())){
                        int newQuantity = this.getTotalQuantity(unbakedProductMessage.getProductQuantities());
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
                    UnbakedProduct up = this.getUnbakedProductFromProductName(unbakedProductMessage.getProductType());
                    if(up != null) {
	                    int newQuantity = this.getTotalQuantity(unbakedProductMessage.getProductQuantities());
	                    this.iterativelyAddUnbakedProducts(newQuantity, up);
                    }
                }
            }
            else {
                block();
            }
        }
        private UnbakedProduct getUnbakedProductFromProductName(String productName){
            UnbakedProduct up = new UnbakedProduct();
            Product p = this.getProductWithSameGuid(productName);
            if(p != null) {
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
            return null;
        }
        private Product getProductWithSameGuid(String productName){
            for (Product p : availableProductList) {
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
                unbakedProductList.add(newUp);
            }
        }
    }
}
