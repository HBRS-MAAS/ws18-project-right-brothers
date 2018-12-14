package org.right_brothers.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.maas.agents.BaseAgent;
import org.right_brothers.utils.JsonConverter;
import org.right_brothers.data.messages.ProductMessage;

import java.util.*;


@SuppressWarnings("serial")
public class PackagingStageTester extends BaseAgent {

    private AID postBakingProcessor = new AID("postBakingProcessor", AID.ISLOCALNAME);
    private AID packagingAgent = new AID("packaging-agent", AID.ISLOCALNAME);
    private int counter = 0;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");
        this.register("Packaging-test", "JADE-bakery");

        String orderString = " { \"customerId\": \"customer-001\", \"guid\": \"order-331\", \"orderDate\": { \"day\": 7, \"hour\": 0 }, \"deliveryDate\": { \"day\": 11, \"hour\": 11 }, \"products\": { \"Multigrain Bread\": 7, \"Donut\":5} }";
        String orderString1 = " { \"customerId\": \"customer-015\", \"guid\": \"order-354\", \"orderDate\": { \"day\": 8, \"hour\": 0 }, \"deliveryDate\": { \"day\": 10, \"hour\": 0 }, \"products\": { \"Multigrain Bread\": 3, \"Donut\":4} }"; 

        ProductMessage pm = new ProductMessage();
        Hashtable products = new Hashtable<String, Integer> ();
        products.put("Multigrain Bread", 10);
        products.put("Donut", 10);
        pm.setProducts(products);
        String messageContent = JsonConverter.getJsonString(pm);
 
        // TODO: always add counter after adding behaviour
        // This dummy agent acts like test agent
        this.addBehaviour(new StringInformSender(orderString, postBakingProcessor, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString1, postBakingProcessor, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(messageContent, postBakingProcessor, "order_guid"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString, packagingAgent, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderString1, packagingAgent, "order"));
        this.counter++;
        
        this.addBehaviour(new InformServer(postBakingProcessor));
        this.addBehaviour(new InformServer(packagingAgent));
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
