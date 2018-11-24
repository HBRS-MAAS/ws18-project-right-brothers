package org.right_brothers.agents;

// for shutdown behaviour
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPANames;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.right_brothers.utils.JsonConverter;
import org.right_brothers.data.messages.UnbakedProductMessage;

import java.util.*;


@SuppressWarnings("serial")
public class BakingStageTester extends BaseAgent {

    private AID ovenManager = new AID("ovenManager", AID.ISLOCALNAME);
    private AID coolingRackAgent = new AID("cooling-rack", AID.ISLOCALNAME);
    private int counter = 0;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");
        this.register("Baking-tester", "JADE-bakery");

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
                System.out.println("\tReceived msg : " + messageContent + " at " + baseAgent.getCurrentHour());
                if (this.sender == coolingRackAgent){
                    //this.sendUnbakedProduct();
                }
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
    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
    private class shutdown extends OneShotBehaviour{
        public void action() {
            ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
            Codec codec = new SLCodec();
            myAgent.getContentManager().registerLanguage(codec);
            myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
            shutdownMessage.addReceiver(myAgent.getAMS());
            shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
            shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
            try {
                myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
                myAgent.send(shutdownMessage);
            }
            catch (Exception e) {}
        }
    }
}
