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
import org.right_brothers.data.messages.ProductMessage;
import org.right_brothers.objects.CooledProduct;
import org.right_brothers.utils.JsonConverter;

@SuppressWarnings("serial")
public class PostBakingProcessor extends BaseAgent {
    private AID intermediater = new AID("intermediater", AID.ISLOCALNAME);
    private AID coolingRackAgent = new AID("dummy", AID.ISLOCALNAME);
    private AID orderProcessor = new AID("dummy", AID.ISLOCALNAME);
    private List<Product> availableProducts;
    private String bakeryGuid = "bakery-001";
    private List<CooledProduct> cooledProducts;
    private List<Order> orders;

    protected void setup() {
        super.setup();
        System.out.println("\tPostBakingProcessor "+getAID().getLocalName()+" is born.");

        this.register("PostBakingProcessor", "JADE-bakery");

        this.orders = new ArrayList();
        this.cooledProducts = new ArrayList<CooledProduct> ();
        this.availableProducts = new ArrayList<Product> ();

        // TODO: get bakery guid as argument
        //         Object[] args = getArguments();

        this.getAllInformation();

        this.addBehaviour(new OrderServer(orderProcessor));
        this.addBehaviour(new CooledProductsServer(coolingRackAgent));
//         this.addBehaviour(new Bake());
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
                break;
            }
        }
    }

    /*
     * Server for the order guid for the dough preparation stage agent's(proofer) message
     * */
    private class CooledProductsServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public CooledProductsServer(AID proofer){
            this.sender = proofer;
        }
        public void action() {
            baseAgent.finished();
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("order_guid"));
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                String messageContent = msg.getContent();
                System.out.println("\tReceived cooled product " + messageContent);
                ProductMessage pm = this.parseProductMessage(messageContent);
                Set<String> keys = pm.getProducts().keySet();
                for(String productName: keys){
                    CooledProduct cp = this.getCooledProductFromProductName(productName);
                    System.out.println(cp);
                }
            }
            else {
                block();
            }
        }
        private ProductMessage parseProductMessage(String orderString){
            ObjectMapper mapper = new ObjectMapper();
            try {
                ProductMessage data = mapper.readValue(orderString, ProductMessage.class);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        private CooledProduct getCooledProductFromProductName(String productName){
            CooledProduct up = new CooledProduct();
            Product p = this.getProductWithSameGuid(productName);
            up.setGuid(p.getGuid());
            Vector<Step> steps = new Vector<Step> ();
            boolean addStep = false;
            // ASSUMPTION: The steps are in order of recipe.
            for (Step s : p.getRecipe().getSteps()) {
                if (s.getAction().equals("cooling")){
                    addStep = true;
                    continue;
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
//         private int getTotalQuantity(Vector<Integer> vec){
//             int sum = 0;
//             for (int i : vec)
//                 sum += i;
//             return sum;
//         }
//         private void iterativelyAddUnbakedProducts(int quantity, UnbakedProduct up) {
//             int newQuantity = quantity;
//             while (newQuantity > 0) {
//                 UnbakedProduct newUp = up.clone();
//                 int quantityToBeAdded = ((newQuantity <= up.getBreadsPerOven()) ? newQuantity : newQuantity - up.getBreadsPerOven());
//                 newQuantity -= quantityToBeAdded;
//                 newUp.setQuantity(quantityToBeAdded);
//                 unbakedProducts.add(newUp);
//             }
//         }

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
