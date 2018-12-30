package org.right_brothers.agents;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

// import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.maas.agents.BaseAgent;
import org.right_brothers.data.models.Order;
import org.maas.utils.JsonConverter;
import org.right_brothers.data.messages.UnbakedProductMessage;

import java.util.*;


@SuppressWarnings("serial")
public class DummyProofer extends BaseAgent {

    private AID ovenManager;
    private String bakeryGuid = "bakery-001";
    private List<Order> orderList;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-proofer "+getAID().getName()+" is ready.");
        this.register("Baking-tester", "JADE-bakery");
        orderList = new Vector<Order>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            bakeryGuid = (String) args[0];
        }
        this.ovenManager = new AID(bakeryGuid + "-ovenManager", AID.ISLOCALNAME);
        AID coolingRackAgent = new AID(bakeryGuid + "-cooling-rack", AID.ISLOCALNAME);
        AID orderProcessor = new AID(bakeryGuid + "-dummy-order-processor", AID.ISLOCALNAME);

        this.addBehaviour(new InformServer(coolingRackAgent));
        this.addBehaviour(new OrderServer(orderProcessor));
    }

    protected void takeDown() {
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    private class InformServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public InformServer (AID orderProcessor){
            this.sender = orderProcessor;
        }
        public void action() {
            baseAgent.finished();
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String messageContent = msg.getContent();
                System.out.println(String.format("\tReceived msg: %s at %s from %s", 
                            messageContent, baseAgent.getCurrentTime(), msg.getSender().getLocalName()));
            }
            else {
                block();
            }
        }
    }

    private class InformSender extends OneShotBehaviour {
        private MessageTemplate mt;
        private String message;
        private AID receiver;
        private String conversationId;
        
        public InformSender(String message, AID receiver, String conversationId) {
            this.message = message;
            this.receiver = receiver;
            this.conversationId = conversationId;
        }
        public void action() {
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(this.receiver);
            inform.setContent(this.message);
            inform.setConversationId(this.conversationId);
            baseAgent.sendMessage(inform);
        }
    }

    @Override
    public void stepAction(){
        if (this.orderList.size() > 0){
            List<UnbakedProductMessage> unbakedProducts = this.convertOrdersToUnbakedProducts(this.orderList);
            this.orderList.clear();
            for (UnbakedProductMessage unbakedProduct : unbakedProducts) {
                String messageContent = JsonConverter.getJsonString(unbakedProduct);
                this.addBehaviour(new InformSender(messageContent, this.ovenManager, "unbakedProduct"));
            }
        }
        baseAgent.finished();
    }

    private List<UnbakedProductMessage> convertOrdersToUnbakedProducts(List<Order> orderList){
        Vector<UnbakedProductMessage> unbakedProducts = new Vector<UnbakedProductMessage> ();
        for (Order order : orderList) {
            Hashtable<String, Integer> products = order.getProducts();
            Set<String> keys = products.keySet();
            for(String key: keys){
                boolean added = false;
                for (UnbakedProductMessage upm : unbakedProducts) {
                    if (upm.getProductType().equals(key)){
                        this.appendGuidAndQuantity(upm, order.getGuid(), products.get(key));
                        added = true;
                        break;
                    }
                }
                if (added){
                    continue;
                }
                UnbakedProductMessage upm = new UnbakedProductMessage();
                upm.setProductType(key);
                this.appendGuidAndQuantity(upm, order.getGuid(), products.get(key));
                unbakedProducts.add(upm);
            }
        }
        return unbakedProducts;
    }

    private void appendGuidAndQuantity(UnbakedProductMessage upm, String guid, int quantity){
        Vector<String> guids = upm.getGuids();
        guids.add(guid);
        upm.setGuids(guids);
        Vector<Integer> productQuantities = upm.getProductQuantities();
        productQuantities.add(quantity);
        upm.setProductQuantities(productQuantities);
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
                // System.out.println("\tReceived Order with guid: " + o.getGuid());
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
}
