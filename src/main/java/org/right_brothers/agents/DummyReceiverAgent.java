package org.right_brothers.agents;

import java.util.*;

// import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.maas.agents.BaseAgent;

@SuppressWarnings("serial")
public class DummyReceiverAgent extends BaseAgent {

    private String bakeryGuid = "bakery-001";

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-proofer "+getAID().getName()+" is ready.");

        Object[] args = getArguments();
        String name = "preLoadingProcessor";
        String senderName = "cooling-rack";
        if (args != null && args.length > 0) {
            this.bakeryGuid = (String) args[0];
            name = (String) args[1];
            senderName = (String) args[2];
        }

        this.register(this.bakeryGuid + "-" + name, this.bakeryGuid + "-" + name);
        AID sender = new AID(bakeryGuid + "-" + senderName, AID.ISLOCALNAME);
        this.addBehaviour(new InformServer(sender));
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
                System.out.println(String.format("\tFinal received msg: %s at %s from %s", 
                            messageContent, baseAgent.getCurrentTime(), msg.getSender().getLocalName()));
            }
            else {
                block();
            }
        }
    }

}
