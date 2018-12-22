package org.right_brothers.agents;

import java.util.*;
// import java.util.stream.Collectors;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.Agent;
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
public class BakingStageTester extends BaseAgent {

    private AID ovenManager;
    private int counter = 0;
    private String bakeryName;
    private List<Order> orders;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");
        this.register("Baking-tester", "JADE-bakery");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            bakeryName = (String) args[0];
        } else {
            bakeryName = "bakery-001";
        }
        this.ovenManager = new AID(bakeryName + "-ovenManager", AID.ISLOCALNAME);
        AID coolingRackAgent = new AID(bakeryName + "-cooling-rack", AID.ISLOCALNAME);

        this.orders = new ArrayList();
        String orderString = " { \"customerId\": \"customer-001\", \"guid\": \"order-331\", \"orderDate\": { \"day\": 7, \"hour\": 0 }, \"deliveryDate\": { \"day\": 11, \"hour\": 11 }, \"products\": { \"Multigrain Bread\": 7, \"Donut\":5} }"; 

        UnbakedProductMessage upm = new UnbakedProductMessage();
        Vector<String> guids = new Vector<String> ();
        guids.add("Order-123"); guids.add("Order-456");
        upm.setGuids(guids);
        upm.setProductType("Multigrain Bread");
        Vector<Integer> vec = new Vector<Integer> ();
        vec.add(8); vec.add(7);
        upm.setProductQuantities(vec);
        String unbakedProduct = JsonConverter.getJsonString(upm);
//         String orderGuid = "order-331";
 
        // TODO: always add counter after adding behaviour
        // This dummy agent acts like test agent
        this.addBehaviour(new StringInformSender(orderString, ovenManager, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(unbakedProduct, ovenManager, "order_guid"));
        this.counter++;
        this.addBehaviour(new InformServer(ovenManager));
        this.addBehaviour(new InformServer(coolingRackAgent));
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
                System.out.println(String.format("\tdummy agent::%s Received message from cooling-rack %s", 
                        this.myAgent.getLocalName(), msg.getSender().getLocalName()));
                System.out.println("\tReceived msg : " + messageContent + " at " + baseAgent.getCurrentHour());
//                 if (this.sender == coolingRackAgent){
                    //this.sendUnbakedProduct();
//                 }
            }
            else {
                block();
            }
        }
        private void sendUnbakedProduct(){
            UnbakedProductMessage upm = new UnbakedProductMessage();
            Vector<String> guids = new Vector<String> ();
            guids.add("Order-123"); guids.add("Order-456");
            upm.setGuids(guids);
            upm.setProductType("Multigrain Bread");
            Vector<Integer> vec = new Vector<Integer> ();
            vec.add(8);
            upm.setProductQuantities(vec);
            String unbakedProduct = JsonConverter.getJsonString(upm);
            myAgent.addBehaviour(new StringInformSender(unbakedProduct, ovenManager, "order_guid"));
        }
    }

    /* 
     * Note: Even though the behaviour below is generic, it is not being blocked with allowAction
     */
    private class StringInformSender extends Behaviour {
        private MessageTemplate mt;
        private String message;
        private AID receiver;
        private String conversationId;
        
        public StringInformSender(String message, AID receiver, String conversationId) {
            this.message = message;
            this.receiver = receiver;
            this.conversationId = conversationId;
        }

        public void action() {
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(receiver);
            inform.setContent(message);
            inform.setConversationId(this.conversationId);
            myAgent.send(inform);
        }
        public boolean done(){
            counter --;
            if (counter == 0){
                System.out.println("No more inform messages left");
            }
            return true;
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
