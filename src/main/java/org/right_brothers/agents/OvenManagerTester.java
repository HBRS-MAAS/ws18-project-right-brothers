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
import jade.lang.acl.UnreadableException;

import java.util.Arrays;
import java.util.List;

import org.right_brothers.data.messages.BakedProduct;
import org.right_brothers.data.messages.CoordinatorMessage;
import org.right_brothers.data.messages.Dough;
import org.right_brothers.data.messages.UnbakedProduct;


@SuppressWarnings("serial")
public class OvenManagerTester extends Agent {

    private AID ovenManager = new AID("ovenManager", AID.ISLOCALNAME);
    private int counter = 0;

    protected void setup() {
        System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");
        String orderString = " { \"customerId\": \"customer-001\", \"guid\": \"order-331\", \"orderDate\": { \"day\": 7, \"hour\": 0 }, \"deliveryDate\": { \"day\": 11, \"hour\": 11 }, \"products\": { \"Multigrain Bread\": 7} }"; 
        String orderGuid = "order-331";
 
        // TODO: always add counter after adding behaviour
        // This dummy agent acts like test agent
        this.addBehaviour(new StringInformSender(orderString, ovenManager, "order"));
        this.counter++;
        this.addBehaviour(new StringInformSender(orderGuid, ovenManager, "order_guid"));
        this.counter++;
    }
    protected void takeDown() {
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }


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
                myAgent.addBehaviour(new shutdown());
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
