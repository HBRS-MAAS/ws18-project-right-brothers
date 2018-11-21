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

import java.util.Arrays;
import java.util.List;


@SuppressWarnings("serial")
public class DummyAgent extends Agent {

    private AID coordinator = new AID("coordinator", AID.ISLOCALNAME);
    private int counter = 0;

	protected void setup() {
		System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");

        // TODO: always add counter after adding behaviour
        // This dummy agent acts like test agent
        List<String> informMessages = Arrays.asList("dough-1", "baked-1", "unbaked-1");
        for(String msg: informMessages) {
        	this.addBehaviour(new InformPerformer(msg));
        	this.counter++;
        }
        
        this.addBehaviour(new RequestPerformer("dough-unavailable"));
    	this.counter++;
	}
	protected void takeDown() {
		System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
	}

    private class RequestPerformer extends Behaviour {
        private MessageTemplate mt;
        private int step = 0;
        private String itemId;
        
        public RequestPerformer(String itemId) {
        	this.itemId = itemId;
        }

        public void action() {
            switch (step) {
            case 0:
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(coordinator);
                request.setContent(itemId);
                request.setConversationId("testing");
                request.setReplyWith("request"+System.currentTimeMillis()); // Unique value
                myAgent.send(request);
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("testing"),
                MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                step = 1;
                break;
            case 1:
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.CONFIRM) {
                        System.out.println("\t" + myAgent.getLocalName() + " received CONFIRM from " + reply.getSender().getLocalName());
                        String data = (String)reply.getContent();
                        System.out.println("\t" + myAgent.getLocalName() + " Reply Message: " + data);
                        step = 2;
                    }
                    if (reply.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("\t" + myAgent.getLocalName() + " received REFUSE from " + reply.getSender().getLocalName() + " for " + itemId);
                        step = 2;
                    }
                }
                else {
                    block();
                }
                break;
            default:
                break;
            }
        }
        public boolean done() {
            if (step == 2) {
                counter --;
                if (counter == 0){
                    myAgent.addBehaviour(new shutdown());
                }
                return true;
            }
            return false;
        }
    }

    private class InformPerformer extends Behaviour {
        private MessageTemplate mt;
        private int step = 0;
        private String message;
        
        public InformPerformer(String message) {
        	this.message = message;
        }

        public void action() {
            switch (step) {
            case 0:
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(coordinator);
                inform.setContent(message);
                inform.setConversationId("testing");
                inform.setReplyWith("request"+ message +System.currentTimeMillis()); // Unique value
                myAgent.send(inform);
                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("testing"),
                MessageTemplate.MatchInReplyTo(inform.getReplyWith()));
                step = 1;
                break;
            case 1:
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    if (reply.getPerformative() == ACLMessage.CONFIRM) {
                        System.out.println("\t" + myAgent.getLocalName() + " received confirmation from " + reply.getSender().getLocalName());
                        System.out.println("\t" + myAgent.getLocalName() + " Reply Message: " + reply.getContent());
                        step = 2;
                    }
                    if (reply.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("\t" + myAgent.getLocalName() + " received refusal from " + reply.getSender().getLocalName());
                        step = 2;
                    }
                }
                else {
                    block();
                }
                break;
            default:
                break;
            }
        }
        public boolean done() {
            if (step == 2) {
                counter --;
                if (counter == 0){
                    myAgent.addBehaviour(new shutdown());
                }
                return true;
            }
            return false;
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
