package org.right_brothers.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.maas.utils.JsonConverter;
import org.maas.agents.BaseAgent;
import org.right_brothers.data.messages.ProductMessage;

import java.util.*;


@SuppressWarnings("serial")
public class PackagingStageTester extends BaseAgent {

    private AID preLoadingProcessor = new AID("preLoadingProcessor", AID.ISLOCALNAME);
    private AID packagingAgent = new AID("packaging-agent", AID.ISLOCALNAME);
    private AID loadingBayAgent = new AID("loader-agent", AID.ISLOCALNAME);
    private int counter = 0;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");
        this.register("OrderProcessing", "OrderProcessing");

        String orderString = " { \"customerId\": \"customer-001\", \"guid\": \"order-331\", \"orderDate\": { \"day\": 7, \"hour\": 0 }, \"deliveryDate\": { \"day\": 11, \"hour\": 11 }, \"products\": { \"Multigrain Bread\": 7, \"Donut\":5} }";
        String orderString1 = " { \"customerId\": \"customer-015\", \"guid\": \"order-354\", \"orderDate\": { \"day\": 8, \"hour\": 0 }, \"deliveryDate\": { \"day\": 10, \"hour\": 0 }, \"products\": { \"Multigrain Bread\": 5, \"Donut\":4} }"; 
        String orderString2 = " { \"customerId\": \"customer-014\", \"guid\": \"order-389\", \"orderDate\": { \"day\": 9, \"hour\": 0 }, \"deliveryDate\": { \"day\": 9, \"hour\": 11 }, \"products\": { \"Multigrain Bread\": 8, \"Donut\":11, \"Bun\":10} }"; 

        ProductMessage pm = new ProductMessage();
        Hashtable products = new Hashtable<String, Integer> ();
        products.put("Multigrain Bread", 20);
        products.put("Donut", 20);
        products.put("Bun", 20);
        pm.setProducts(products);
        String messageContent = JsonConverter.getJsonString(pm);
 
        // TODO: always add counter after adding behaviour
        // This dummy agent acts like test agent
        this.addBehaviour(new StringInformSender(orderString, preLoadingProcessor, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString1, preLoadingProcessor, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(messageContent, preLoadingProcessor, "order_guid"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString, packagingAgent, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString1, packagingAgent, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString2, packagingAgent, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString, loadingBayAgent, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString1, loadingBayAgent, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString2, loadingBayAgent, "order"));
        this.counter++;
        
        this.addBehaviour(new InformServer(preLoadingProcessor));
        this.addBehaviour(new InformServer(packagingAgent));
        this.addBehaviour(new InformServer(loadingBayAgent));
//         this.addBehaviour(new InformServer(coolingRackAgent));
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
                System.out.println("\tReceived msg : " + messageContent + " at " + baseAgent.getCurrentHour());
            }
            else {
                block();
            }
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
}
