package org.right_brothers.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.Vector;
import org.right_brothers.data.messages.CompletedProductMessage;
import org.right_brothers.data.messages.LoadingBayBox;
import org.right_brothers.data.messages.LoadingBayMessage;
import org.right_brothers.data.models.Order;
import org.right_brothers.data.models.OrderItem;
import org.right_brothers.data.models.SortByDeliveryTime;
import org.right_brothers.utils.InputParser; 
import org.maas.utils.JsonConverter;
import org.maas.agents.BaseAgent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Product;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")

public class ProductBoxerAgent extends BaseAgent {
    private AID loadingBayAgent;
    private Hashtable<String,Integer> productPackingLookup;
    private static List<Product> availableProductList;
    private String bakeryGuid = "bakery-001";
    private int boxCounter = 0;
    private boolean useHardPriority = false;
    private boolean verbose = false;
	private List<OrderItem> orderList = new ArrayList<>();
	private Hashtable<String, Integer> productBuffer = new Hashtable<>();
	
	protected void setup() {
        super.setup();
        System.out.println("\tProduct-Boxer-Agent "+getAID().getLocalName()+" is born.");
        Object[] args = getArguments();
        String scenarioDirectory = "small";
        if (args != null && args.length > 0) {
            this.bakeryGuid = (String) args[0];
            scenarioDirectory = (String) args [1];
        }
        AID orderProcessor =  new AID(bakeryGuid, AID.ISLOCALNAME);
        AID preLoadingProcessor = new AID(bakeryGuid + "-preLoadingProcessor", AID.ISLOCALNAME);
        loadingBayAgent = new AID(bakeryGuid + "-loader-agent", AID.ISLOCALNAME);
        this.register("Product-Boxer-agent", this.bakeryGuid+"-ProductBoxerAgent");
        this.getAllInformation(scenarioDirectory);
        this.setupPackagingKnowledgeBase();
        this.addBehaviour(new OrderReceiver(orderProcessor));
        this.addBehaviour(new CompletedProductReceiver(preLoadingProcessor));
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    /**
     * Setup knowledge about the boxing capacity of the products.
     **/
    private void setupPackagingKnowledgeBase() {
        this.productPackingLookup = new Hashtable<String, Integer>();
        for (Product p : this.availableProductList) {
            this.productPackingLookup.put(p.getGuid(), p.getPackaging().getBreadsPerBox());
        }
    }

    /**
     * Get information about the products the bakery makes.
     **/
    private void getAllInformation(String scenarioDirectory) {
        InputParser<Vector<Bakery>> parser = new InputParser<>
            ("/config/"+scenarioDirectory+"/bakeries.json", new TypeReference<Vector<Bakery>>(){});
        List<Bakery> bakeries = parser.parse();
        for (Bakery b : bakeries) {
            if (b.getGuid().equalsIgnoreCase(this.bakeryGuid)){
                this.availableProductList = b.getProducts();
                break;
            }
        }
    }

    /**
     * Print information depending on verbose
     **/
    private void print(String str){
        if (this.verbose){
            System.out.println(str);
        }
    }
    /**
     * Prioritizes the order list according to the delivery times
     **/
    private void prioritizeOrderList(boolean verbose) {
        int day = 0;
        int hour = 0;
        if (verbose) {
            System.out.println("\tOrder-list before prioritizing"); 
            for (int i=0; i<this.orderList.size(); i++) {
                day = this.orderList.get(i).getOrder().getDeliveryDate().getDay();
                hour = this.orderList.get(i).getOrder().getDeliveryDate().getHour();
                System.out.println("guid: "+this.orderList.get(i).getOrder().getGuid()+" Day: "+day+" Hour: "+hour); 
            }
        }

        Collections.sort(this.orderList, new SortByDeliveryTime());

        if (verbose) {
            System.out.println("\tOrder-list after prioritizing"); 
            for (int i=0; i<this.orderList.size(); i++) {
                day = this.orderList.get(i).getOrder().getDeliveryDate().getDay();
                hour = this.orderList.get(i).getOrder().getDeliveryDate().getHour();
                System.out.println("guid: "+this.orderList.get(i).getOrder().getGuid()+" Day: "+day+" Hour: "+hour); 
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
                print("\tProduct-Boxer-Agent received Order with guid: " + o.getGuid());
                OrderItem newOrder = new OrderItem(o, false);
                orderList.add(newOrder);
                // New order received so re-prioritize the order list
                prioritizeOrderList(verbose);
                // Maybe some products in new order can be satisfied with the already available products.
                if (useHardPriority)
                    /*
                     * If hard priority then waits till the high priority order is completed and then moves to
                     * process the next order.
                     */
                    checkProductOrderReadyHardPriority();
                else
                    /* 
                     * If soft priority then checks if products in the high priority order can be satisfied, if yes then proceeses them
                     * if not then checks if the products can be used to satisfy the other orders.
                     */
                    checkProductOrderReadySoftPriority();

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

        public CompletedProductReceiver(AID preLoadingProcessor){
            this.sender = preLoadingProcessor;
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
                	print(String.format("\t"+getAID().getLocalName()+" :Received completed product: %s, quantity: %S",
                			messageItem.getGuid(), messageItem.getQuantity()));
                	
                	String productName = messageItem.getGuid();
                	if(productBuffer.containsKey(productName)) {
                		productBuffer.put(productName, productBuffer.get(productName) + messageItem.getQuantity());
                	} else {
                		productBuffer.put(productName, messageItem.getQuantity());
                	}
                }
                // New products received so check if any of the orders in the list can be satisfied
                if (useHardPriority)
                    /*
                     * If hard priority then waits till the high priority order is completed and then moves to
                     * process the next order.
                     */
                    checkProductOrderReadyHardPriority();
                else
                    /* 
                     * If soft priority then checks if products in the high priority order can be satisfied, if yes then proceeses them
                     * if not then checks if the products can be used to satisfy the other orders.
                     */
                    checkProductOrderReadySoftPriority();
            }
            else {
                block();
            }
            baseAgent.finished();
        }
    }
    private void displayInventory(String when, boolean verbose) {
        if (verbose) {
            if (orderList.size() > 0) {
                System.out.println("\n_____________________________"+((ProductBoxerAgent) baseAgent).bakeryGuid+" Inventory Check ("+when+")__________________________");
                displayOrderStatus();
                System.out.println("__________________________________Inventory Check Complete_____________________________\n");
            } else {
                System.out.println("\nAll orders have been serviced !");
            }
        }
    }

    /**
     * Hard priority handler.
     **/
    private void checkProductOrderReadyHardPriority() {
        Hashtable<String,Integer> products;
        LoadingBayMessage loadingBayMessage = new LoadingBayMessage();
        List<LoadingBayBox> boxes = new ArrayList<LoadingBayBox>();
        int zeroCounter = 0;

        displayInventory("Before Update", verbose);

        products = orderList.get(0).getOrder().getProducts();
        Set<String> keys = products.keySet();
        for(String key: keys) {
            Integer availableCount = productBuffer.get(key);
            Integer orderProductCount = products.get(key);
            if (orderProductCount > 0) {
                if ((availableCount != null) && (orderProductCount <= availableCount)) {
                    //Create message contents
                    this.packTheProducts(key, orderProductCount, boxes);
                    productBuffer.put(key, availableCount - orderProductCount);
                    products.put(key, 0);
                    orderList.get(0).getOrder().setProducts(products);
                    zeroCounter++;
                }
            } else {
                zeroCounter++;
            }
        }
        if (!boxes.isEmpty()) {
            loadingBayMessage.setOrderId(orderList.get(0).getOrder().getGuid());
            loadingBayMessage.setBoxes(boxes);
            this.sendPackedProducts(loadingBayMessage, verbose);
        }
        if (zeroCounter == products.size()) {
            print("Complete "+orderList.get(0).getOrder().getGuid()+" has been passed to the next stage");
            orderList.remove(0);
            if (orderList.size() > 0) {
                checkProductOrderReadyHardPriority();
            }
        }

        displayInventory("After Update", verbose);

    }

    /**
     * Soft priority handler.
     **/
    private void checkProductOrderReadySoftPriority() {
        Hashtable<String,Integer> products;
        LoadingBayMessage loadingBayMessage;
        List<LoadingBayBox> boxes;
        int zeroCounter;
        displayInventory("Before Update", verbose);
        for (int i=0; i<orderList.size(); i++) {
            zeroCounter = 0;
            loadingBayMessage = new LoadingBayMessage();
            boxes = new ArrayList<LoadingBayBox>();
            products = orderList.get(i).getOrder().getProducts();
            Set<String> keys = products.keySet();
            for(String key: keys) {
                Integer availableCount = productBuffer.get(key);
                Integer orderProductCount = products.get(key);
                int boxCapacity = productPackingLookup.get(key);
                if (orderProductCount > 0) {
                    if (availableCount != null) {
                        //Create message contents
                        //Check if all products are ready then send one message with all of them
                        if(orderProductCount <= availableCount) {
                            this.packTheProducts(key, orderProductCount, boxes);
                            productBuffer.put(key, availableCount - orderProductCount);
                            products.put(key, 0);
                            orderList.get(i).getOrder().setProducts(products);
                            zeroCounter++;
                        } else if (availableCount >= boxCapacity) {
                            // If only a subset of products are ready, just send the complete boxes
                            int num_products = ((int) Math.floor((float) availableCount/ boxCapacity))*boxCapacity;
                            this.packTheProducts(key, num_products, boxes);
                            productBuffer.put(key, availableCount - num_products);
                            products.put(key, orderProductCount - num_products);
                            orderList.get(i).getOrder().setProducts(products);
                        }

                    }
                } else {
                    zeroCounter++;
                }
            }
            if (!boxes.isEmpty()) {
                // System.out.println("\n Boxes not empty");
                loadingBayMessage.setOrderId(orderList.get(i).getOrder().getGuid());
                loadingBayMessage.setBoxes(boxes);
                this.sendPackedProducts(loadingBayMessage, verbose);
            }
            if (zeroCounter == products.size()) {
                print("Order "+orderList.get(i).getOrder().getGuid()+" Complete");
                orderList.get(i).setIsOrderComplete(true);
            }
        }
        // Remove the products which were completed from the todo list.
        int l = 0;
        while(l < orderList.size()) {
            if (orderList.get(l).getIsOrderComplete()) {
                print("Complete "+orderList.get(l).getOrder().getGuid()+" has been passed to the next stage");
                orderList.remove(l);
                // Reset counter if an order is removed from list
                l = 0;
                continue;
            }
            l++;
        }
        displayInventory("After Update", verbose);
    }

    /**
     * Display the status of the order.
     **/
    private void displayOrderStatus() {
        Hashtable<String,Integer> products;
        for (int i=0; i<orderList.size(); i++) {
            products = orderList.get(i).getOrder().getProducts();
            System.out.println("Order Id: "+orderList.get(i).getOrder().getGuid());
            Set<String> keys = products.keySet();
            for(String key: keys) {
                Integer availableCount = productBuffer.get(key);
                Integer orderProductCount = products.get(key);
                System.out.println("Priority "+(i+1)+" ,Inventory "+key+" available "+availableCount+" ,and Order needs "+orderProductCount);
            }
        }
    }

    /**
     * Pack the products into boxes.
     **/
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

    /**
     * Send the packed boxed products to the loading bay.
     **/
    private void sendPackedProducts(LoadingBayMessage message, boolean verbose) {
            String messageContent = JsonConverter.getJsonString(message);
            if (verbose)
                System.out.println("\nProduct-Boxer-Message being sent "+messageContent+"\n");
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(loadingBayAgent);
            inform.setContent(messageContent);
            inform.setConversationId("boxes-ready");
            baseAgent.sendMessage(inform);
    }
}
