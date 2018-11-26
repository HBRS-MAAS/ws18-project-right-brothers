package org.right_brothers.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.right_brothers.bakery_objects.CooledProduct;
import org.right_brothers.data.messages.CompletedProductMessage;
import org.right_brothers.data.messages.ProductMessage;
import org.right_brothers.data.models.Order;
import org.right_brothers.data.models.Product;
import org.right_brothers.data.models.Step;
import org.right_brothers.utils.JsonConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class PackagingAgent extends BaseAgent {
	private AID orderProcessor = new AID("dummy", AID.ISLOCALNAME);
	private AID postBakingProcessor = new AID("postBakingProcessor", AID.ISLOCALNAME);
	
	private List<Order> orderList = new ArrayList<>();
	
	protected void setup() {
        super.setup();
        System.out.println("\tPostBakingProcessor "+getAID().getLocalName()+" is born.");

        // TODO - Not registering for now so that timer does not wait for it
        // this.register("PackagingAgent", "JADE-bakery");
        
        this.addBehaviour(new OrderReceiver(orderProcessor));
        this.addBehaviour(new CompletedProductReceiver(postBakingProcessor));
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
        	MessageTemplate mt = MessageTemplate.and(
        			MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.MatchSender(sender)),
        			MessageTemplate.MatchConversationId("order"));
        			
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String order = msg.getContent();
                Order o = this.parseOrder(order);
                System.out.println("\tReceived Order with guid: " + o.getGuid());
                orderList.add(o);
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
	
	/**
     * Receives completed products from post baking processor
     **/
	private class CompletedProductReceiver extends CyclicBehaviour {
        private AID sender;

        public CompletedProductReceiver(AID postBakingProcessor){
            this.sender = postBakingProcessor;
        }
        public void action() {
        	MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
        			MessageTemplate.MatchSender(sender));
        			
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String completedProductString = msg.getContent();
                List<CompletedProductMessage> completedProductList = JsonConverter.getInstance(completedProductString,
                		new TypeReference<List<CompletedProductMessage>>() {});
                
                for(CompletedProductMessage messageItem: completedProductList) {
                	System.out.println(String.format("\tReceived completed product with guid: %s, quantity: %S",
                			messageItem.getGuid(), messageItem.getQuantity()));
                }
            }
            else {
                block();
            }
        }
    }
}
