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
import org.right_brothers.utils.InputParser; 
import org.right_brothers.data.models.Order;
import org.right_brothers.data.models.Product;
import org.right_brothers.data.models.Oven;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Step;
import org.right_brothers.data.messages.BakedProductMessage;
import org.right_brothers.data.messages.UnbakedProductMessage;
import org.right_brothers.objects.UnbakedProduct;
import org.right_brothers.utils.JsonConverter;

@SuppressWarnings("serial")
public class OvenManager extends BaseAgent {
    private AID coolingRacksAgent = new AID("cooling-rack", AID.ISLOCALNAME);
    private AID proofer = new AID("dummy", AID.ISLOCALNAME);
    private AID orderProcessor = new AID("dummy", AID.ISLOCALNAME);
    private List<Product> availableProducts;
    private String bakeryGuid = "bakery-001";
    private List<Oven> ovens;
    private List<UnbakedProduct> unbakedProduct;

    private List<Order> orders;

    protected void setup() {
        super.setup();
        System.out.println("\tOven-manager "+getAID().getLocalName()+" is born.");

        this.register("Oven-manager-agent", "JADE-bakery");

        this.orders = new ArrayList();
        this.unbakedProduct = new ArrayList<UnbakedProduct> ();
        this.availableProducts = new ArrayList<Product> ();

        // TODO: get bakery guid as argument
        //         Object[] args = getArguments();

        this.getAllInformation();

        this.addBehaviour(new OrderServer(orderProcessor));
        this.addBehaviour(new UnbakedProductsServer(proofer));
        this.addBehaviour(new BakeProducts());
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    private void getAllInformation(){
		InputParser<Vector<Bakery>> parser2 = new InputParser<>
			("/config/sample/bakeries.json", new TypeReference<Vector<Bakery>>(){});
		List<Bakery> bakeries = parser2.parse();
        for (Bakery b : bakeries) {
            if (b.getGuid().equalsIgnoreCase(this.bakeryGuid)){
                this.availableProducts = b.getProducts();
                this.ovens = b.getEquipment().getOvens();
            }
        }
        System.out.println("Number of oven " + this.ovens.size());
    }

    private class BakeProducts extends CyclicBehaviour{
        public void action(){
            if (!baseAgent.getAllowAction()) {
                return;
            }
            ArrayList<BakedProductMessage> message = new ArrayList<BakedProductMessage> ();
            ArrayList<UnbakedProduct> temp = new ArrayList<UnbakedProduct> ();
            for (UnbakedProduct pm : unbakedProduct) {
                if (pm.getIsBaking())
                    continue;
                //TODO: check if baking is done
                //if (this.getCurrentHour() == pm.getProcessStartTime() + pm.getBakingDuration()){
                System.out.println("\tBaked " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
                BakedProductMessage bpm = new BakedProductMessage();
                bpm.setGuid(pm.getGuid());
                bpm.setQuantity(pm.getQuantity());
                bpm.setCoolingDuration(pm.getCoolingDuration());
                message.add(bpm);
                temp.add(pm);
                //}
                //}
                //else {
                //TODO: check if the ovens are free or not
                //TODO: check of the ovens are at correct temp or not
                //TODO: start baking
                    //pm.setIsBaking(true);
                    //pm.setProcessStartTime(this.getCurrentHour());
                //}
            }
            if (message.size() > 0) {
                String messageContent = JsonConverter.getJsonString(message);
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(coolingRacksAgent);
                inform.setContent(messageContent);
                inform.setConversationId("Baked-products-001");
                baseAgent.sendMessage(inform);
            }
            for (UnbakedProduct pm : temp)
                unbakedProduct.remove(pm);
            baseAgent.finished();
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
                for (UnbakedProduct up : unbakedProduct) {
                    if (up.getGuid().equals(upm.getProductType()) && !up.getIsBaking()){
                        up.setQuantity(this.getTotalQuantity(upm.getProductQuantities()));
                        alreadyAdded = true;
                    }
                }
                if (!alreadyAdded){
                    UnbakedProduct up = this.getUnbakedProductFromUnbakedProductMessage(upm);
                    unbakedProduct.add(up);
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
        private UnbakedProduct getUnbakedProductFromUnbakedProductMessage(UnbakedProductMessage upm){
            UnbakedProduct up = new UnbakedProduct();
            Product p = this.getProductWithSameGuid(upm.getProductType());
            up.setGuid(p.getGuid());
            up.setBakingTemp(p.getRecipe().getBakingTemp());
            up.setBreadsPerOven(p.getBatch().getBreadsPerOven());
            up.setQuantity(this.getTotalQuantity(upm.getProductQuantities()));
            for (Step s : p.getRecipe().getSteps()) {
                if (s.getAction().equals("cooling"))
                    up.setCoolingDuration(s.getDuration());
                if (s.getAction().equals("baking"))
                    up.setBakingDuration(s.getDuration());
            }
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
