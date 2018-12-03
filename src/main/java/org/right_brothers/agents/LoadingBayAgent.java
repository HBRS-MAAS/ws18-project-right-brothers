package org.right_brothers.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import org.right_brothers.data.messages.CompletedProductMessage;
import org.right_brothers.data.messages.LoadingBayBox;
import org.right_brothers.data.messages.LoadingBayMessage;
import org.right_brothers.data.models.Order;
import org.right_brothers.utils.InputParser; 
import org.right_brothers.utils.JsonConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Product;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class LoadingBayAgent extends BaseAgent {
	private AID orderProcessor = new AID("dummy", AID.ISLOCALNAME);
	private AID postBakingProcessor = new AID("postBakingProcessor", AID.ISLOCALNAME);
    private Hashtable<String,Integer> productPackingLookup;
    private static List<Product> availableProducts;
    private String bakeryGuid = "bakery-001";
    private int boxCounter = 0;
	
	private List<Order> orderList = new ArrayList<>();
	private Hashtable<String, Integer> productBuffer = new Hashtable<>();
	
	protected void setup() {
        super.setup();
        System.out.println("\tLoading-Bay-Agent "+getAID().getLocalName()+" is born.");
        this.register("Loading-bay-agent", "JADE-bakery");
        this.getAllInformation();
        this.setupPackagingKnowledgeBase();
        this.addBehaviour(new OrderReceiver(orderProcessor));
        this.addBehaviour(new CompletedProductReceiver(postBakingProcessor));
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    private void setupPackagingKnowledgeBase() {
        this.productPackingLookup = new Hashtable<String, Integer>();
        for (Product p : this.availableProducts) {
            this.productPackingLookup.put(p.getGuid(), p.getPackaging().getBreadsPerBox());
        }
    }

    private void getAllInformation() {
        InputParser<Vector<Bakery>> parser = new InputParser<>
            ("/config/sample/bakeries.json", new TypeReference<Vector<Bakery>>(){});
        List<Bakery> bakeries = parser.parse();
        for (Bakery b : bakeries) {
            if (b.getGuid().equalsIgnoreCase(this.bakeryGuid)){
                this.availableProducts = b.getProducts();
                break;
            }
        }
    }

    class sortByDeliveryTime implements Comparator<Order> 
    { 
        // Sorting in Ascending order of delivery time 
        public int compare(Order a, Order b) 
        {
            int timeParam1;
            int timeParam2;
            // Assuming orders do not arrive months in advance. Order has Date in only day and hours
            timeParam1 = (a.getDeliveryDate().getDay()*24)+(a.getDeliveryDate().getHour());
            timeParam2 = (b.getDeliveryDate().getDay()*24)+(b.getDeliveryDate().getHour());
            return timeParam1 - timeParam2; 
        } 
    } 

    private void prioritizeOrderList(boolean verbose) {
        int day = 0;
        int hour = 0;
        if (verbose) {
            System.out.println("\tOrder-list before prioritizing"); 
            for (int i=0; i<this.orderList.size(); i++) {
                day = this.orderList.get(i).getDeliveryDate().getDay();
                hour = this.orderList.get(i).getDeliveryDate().getHour();
                System.out.println("guid: "+this.orderList.get(i).getGuid()+" Day: "+day+" Hour: "+hour); 
            }
        }

        Collections.sort(this.orderList, new sortByDeliveryTime());

        if (verbose) {
            System.out.println("\tOrder-list after prioritizing"); 
            for (int i=0; i<this.orderList.size(); i++) {
                day = this.orderList.get(i).getDeliveryDate().getDay();
                hour = this.orderList.get(i).getDeliveryDate().getHour();
                System.out.println("guid: "+this.orderList.get(i).getGuid()+" Day: "+day+" Hour: "+hour); 
            }
        }
    }
	/**
     * Receives order details from order processor
     **/
	private class OrderReceiver extends CyclicBehaviour {
        private AID sender;

        public OrderReceiver(AID orderProcessor){
            this.sender = orderProcessor;
        }
        public void action() {
            if (!baseAgent.getAllowAction()) {
                return;
            }
        	MessageTemplate mt = MessageTemplate.and(
        			MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchSender(sender)),
        			MessageTemplate.MatchConversationId("order"));
        			
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String order = msg.getContent();
                Order o = this.parseOrder(order);
                System.out.println("\tReceived Order with guid: " + o.getGuid());
                orderList.add(o);
                prioritizeOrderList(true);
            }
            else {
                block();
            }
            baseAgent.finished();
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
	/**
     * Receives completed products from post baking processor
     **/
    private class CompletedProductReceiver extends CyclicBehaviour {
        private AID sender;

        public CompletedProductReceiver(AID postBakingProcessor){
            this.sender = postBakingProcessor;
        }
        public void action() {
            if (!baseAgent.getAllowAction()) {
                return;
            }
        	MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
        			MessageTemplate.MatchSender(sender));
        			
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String completedProductString = msg.getContent();
                List<CompletedProductMessage> completedProductList = JsonConverter.getInstance(completedProductString,
                		new TypeReference<List<CompletedProductMessage>>() {});
                
                for(CompletedProductMessage messageItem: completedProductList) {
                	System.out.println(String.format("\tReceived completed product: %s, quantity: %S",
                			messageItem.getGuid(), messageItem.getQuantity()));
                	
                	String productName = messageItem.getGuid();
                	if(productBuffer.containsKey(productName)) {
                		productBuffer.put(productName, productBuffer.get(productName) + messageItem.getQuantity());
                	} else {
                		productBuffer.put(productName, messageItem.getQuantity());
                	}
                }
                checkProductOrderReady();
            }
            else {
                block();
            }
            baseAgent.finished();
        }

        private void checkProductOrderReady() {
            Hashtable<String,Integer> products;
            LoadingBayMessage ldm = new LoadingBayMessage();
            List<LoadingBayBox> boxes = new ArrayList<LoadingBayBox>();
            int zero_counter = 0; 
            if (orderList.size() > 0) {
                System.out.println("\n_____________________________Inventory Check (Before Update)__________________________");
                displayOrderStatus();
                System.out.println("__________________________________Inventory Check Complete_____________________________\n");
            }
            products = orderList.get(0).getProducts();
            Set<String> keys = products.keySet();
            for(String key: keys) {
                Integer availableCount = productBuffer.get(key);
                Integer orderProductCount = products.get(key);
                if (orderProductCount > 0) {
                    if ((availableCount != null) && (orderProductCount <= availableCount)) {
                        //Create message contents
                        this.packTheProducts(key, products.get(key), boxes);
                        productBuffer.put(key, productBuffer.get(key) - orderProductCount);
                        products.put(key, 0);
                        orderList.get(0).setProducts(products);
                        zero_counter++;
                    }
                } else {
                    zero_counter++;
                }
            }
            if (!boxes.isEmpty()) {
                ldm.setOrderId(orderList.get(0).getGuid());
                ldm.setBoxes(boxes);
                this.sendPackedProducts(ldm);
            }
            if (zero_counter == products.size()) {
                System.out.println("Complete "+orderList.get(0).getGuid()+" has been passed to the next stage");
                orderList.remove(0);
                if (orderList.size() > 0) {
                    checkProductOrderReady();
                }
            }
            if (orderList.size() > 0) {
                System.out.println("\n_____________________________Inventory Check (After Update)__________________________");
                displayOrderStatus();
                System.out.println("__________________________________Inventory Check Complete_____________________________\n");
            }
        }

        private void displayOrderStatus() {
            Hashtable<String,Integer> products;
            for (int i=0; i<orderList.size(); i++) {
                products = orderList.get(i).getProducts();
                System.out.println("Order Id: "+orderList.get(i).getGuid());
                Set<String> keys = products.keySet();
                for(String key: keys) {
                    Integer availableCount = productBuffer.get(key);
                    Integer orderProductCount = products.get(key);
                    System.out.println("Priority "+(i+1)+" ,Inventory "+key+" available "+availableCount+" ,and Order needs "+orderProductCount);
                }
            }
        }

        private void packTheProducts(String productType, int quantity, List<LoadingBayBox> boxes) {
            int boxCapacity = productPackingLookup.get(productType);
            int numBoxes = (int) Math.ceil((float) quantity/ boxCapacity);
            int residual = quantity;
            for (int i=1; i<=numBoxes; i++) {
                boxCounter++;
                LoadingBayBox box = new LoadingBayBox();
                box.setBoxId(""+boxCounter);
                box.setProductType(productType);
                if (residual < boxCapacity)
                    box.setQuantity(residual);
                else
                    box.setQuantity(boxCapacity);
                    residual = residual - boxCapacity;
                boxes.add(box);
            }
        }

        private void sendPackedProducts(LoadingBayMessage message) {
                String messageContent = JsonConverter.getJsonString(message);
                System.out.println("\nLoading-Bay-Message being sent "+messageContent+"\n");
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(orderProcessor);
                inform.setContent(messageContent);
                inform.setConversationId("Loading-Bay-"+message.getOrderId());
                baseAgent.sendMessage(inform);
        }
    }
}
