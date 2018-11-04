package org.right_brothers.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPANames;

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
        addBehaviour(new SellerTerminator());
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
     * Inner class SellerTerminator
     * This behaviour is used by Bread-seller agents to check if there are any 
     * buyer agents alive. If there are none, then the seller agent will terminate
     * itself. The checking is done in action method and the suicide is commited in
     * done method.
     * */
    private class SellerTerminator extends Behaviour {
        private int numberOfBuyersAlive;
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Bakery-customer-agent");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                this.numberOfBuyersAlive = (int) result.length;
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        public boolean done () {
            if (this.numberOfBuyersAlive == 0) {
                shutdown();
                return true;
            }
            else {
                return false;
            }
        }
        
        public void shutdown() {
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
            catch (Exception e) {
            }
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
