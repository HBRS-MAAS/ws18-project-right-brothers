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
import org.right_brothers.utils.JsonConverter;

@SuppressWarnings("serial")
public class OvenManager extends BaseAgent {
    private AID cooling_racks_agent = new AID("dummy", AID.ISLOCALNAME);
    private AID proofer = new AID("dummy", AID.ISLOCALNAME);
    private AID orderProcessor = new AID("dummy", AID.ISLOCALNAME);
    private List<Product> available_products;
    private List<String> available_product_names;
    private String bakery_guid = "bakery-001";
    private List<Oven> ovens;
    private List<ProductMessage> unbakedProduct;

    private List<Order> orders;

    protected void setup() {
        super.setup();
        System.out.println("\tOven-manager "+getAID().getLocalName()+" is born.");

        this.register("Oven-manager-agent", "JADE-bakery");

        this.orders = new ArrayList();
        this.unbakedProduct = new ArrayList<ProductMessage> ();
        this.available_products = new ArrayList<Product> ();
        this.available_product_names = new ArrayList<String> ();

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
            if (b.getGuid().equalsIgnoreCase(this.bakery_guid)){
                this.available_products = b.getProducts();
                this.ovens = b.getEquipment().getOvens();
            }
        }
//         System.out.println(this.available_products);
        System.out.println(this.ovens);
        for (Product p : this.available_products) {
            this.available_product_names.add(p.getGuid());
        }
//         System.out.println(this.available_product_names);
    }

    private class BakeProducts extends CyclicBehaviour{
        public void action(){
            ArrayList<ProductMessage> temp = new ArrayList<ProductMessage> ();
            for (ProductMessage pm : unbakedProduct) {
                if (pm.getIsBaking())
                    continue;
                //TODO: check if the ovens are free or not
                //TODO: check of the ovens are at correct temp or not
                //TODO: start baking
                    //pm.setIsBaking(true);
                    //pm.setProcessStartTime(this.getCurrentHour());
                //TODO: check if baking is done
                //if (this.getCurrentHour() == pm.getProcessStartTime() + pm.getBakingDuration()){
                String messageContent = JsonConverter.getJsonString(pm);
                System.out.println("\tBaked " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
//                 System.out.println(messageContent);
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(cooling_racks_agent);
                inform.setContent(messageContent);
                inform.setConversationId("Baked-products-001");
                myAgent.send(inform);
                temp.add(pm);
                //}
            }
            for (ProductMessage pm : temp)
                unbakedProduct.remove(pm);
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
                String order_guid = msg.getContent();
                System.out.println("\tUnbaked Order guid: " + order_guid);
                for (Order o : orders) {
                    if (o.getGuid().equalsIgnoreCase(order_guid)){
                        this.addUnbakedProducts(o.getProducts());
                    }
                }
            }
            else {
                block();
            }
        }
        private void addUnbakedProducts(Hashtable<String, Integer> products){
            Enumeration e = products.keys();
            while (e.hasMoreElements()){
                String productName = (String) e.nextElement();
                boolean alreadyAdded = false;
                if (available_product_names.contains(productName)){
                    for (ProductMessage p : unbakedProduct) {
                        if (p.getGuid().equals(productName) && !p.getIsBaking()){
                            p.setQuantity(p.getQuantity() + products.get(productName));
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded) {
                        for (Product p : available_products) {
                            if (p.getGuid().equals(productName)){
                                ProductMessage pm = this.getProductMessageFromProduct(p);
                                pm.setQuantity(products.get(productName));
                                unbakedProduct.add(pm);
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("Product with name " + productName + " is not offered by " + bakery_guid);
//                     throw new Error("Product with name " + productName + " is not offered by " + bakery_guid);
                }
            }
        }
        private ProductMessage getProductMessageFromProduct(Product p) {
            ProductMessage pm = new ProductMessage(p.getGuid());
            pm.setCoolingRate(p.getRecipe().getCoolingRate());
            pm.setBakingTemp(p.getRecipe().getBakingTemp());
            pm.setBreadsPerOven(p.getBatch().getBreadsPerOven());
            for (Step s : p.getRecipe().getSteps()) {
                if (s.getAction().equals("cooling"))
                    pm.setCoolingDuration(s.getDuration());
                if (s.getAction().equals("baking"))
                    pm.setBakingDuration(s.getDuration());
            }
            return pm;
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
