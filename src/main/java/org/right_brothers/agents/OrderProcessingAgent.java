package org.right_brothers.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;

public class OrderProcessingAgent extends Agent {

    protected void setup() {
        System.out.println("\tOrder-processing-agent "+getAID().getLocalName()+" is born.");

        this.publishSellerAID();
        
        addBehaviour(new OfferRequestsServer());
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("\t"+getAID().getLocalName()+" terminating.");
    }
    protected void publishSellerAID(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Order-processing-agent");
        sd.setName("JADE-bakery");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    
    /*
     * Inner class OfferRequestsServer.
     * This is the behaviour used by Bread-seller agents to serve incoming requests
     * for offer from buyer agents.
     * If the requested Bread is in the local catalogue the seller agent replies
     * with a PROPOSE message specifying the price. Otherwise a REFUSE message is
     * sent back.
     * */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Got your order.");
                System.out.println(msg.getContent());
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
}
