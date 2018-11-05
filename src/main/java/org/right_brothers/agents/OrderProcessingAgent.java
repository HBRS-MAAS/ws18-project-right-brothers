package org.right_brothers.agents;

// imports for shutdown of platform behaviour
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPANames;

import jade.core.Agent;
//import jade.core.AID;
import jade.core.behaviours.*;
import java.util.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;

import org.right_brothers.objects.Order;
//import org.json.simple.JSONObject;

public class OrderProcessingAgent extends Agent {

    protected void setup() {
        // Printout a welcome message
        System.out.println("\tOrder-processing-agent "+getAID().getLocalName()+" is born.");

        this.publishSellerAID();
        
        // Add the behaviour serving requests for offer from buyer agents
        addBehaviour(new OfferRequestsServer());
        // Add the behaviour that will terminate the seller if no buyers are online
        addBehaviour(new SellerTerminator());
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("\t"+getAID().getLocalName()+" terminating.");
    }
    protected void publishSellerAID(){
        // Register the Bread-selling service in the yellow pages
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
    

    private class SellerTerminator extends Behaviour {
        /*
         * Inner class SellerTerminator
         * This behaviour is used by Bread-seller agents to check if there are any 
         * buyer agents alive. If there are none, then the seller agent will terminate
         * itself. The checking is done in action method and the suicide is commited in
         * done method.
         * */
        private int numberOfBuyersAlive;
        public void action() {
            // Update the list of buyer agents
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Bakery-customer-agent");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                // System.out.println(result.length + " buyers online.");
                this.numberOfBuyersAlive = (int) result.length;
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        public boolean done () {
            if (this.numberOfBuyersAlive == 0) {
                // myAgent.doDelete();
                myAgent.addBehaviour(new shutdown());
                return true;
            }
            else {
                return false;
            }
        }
    }

    private class OfferRequestsServer extends CyclicBehaviour {
        /*
         * Inner class OfferRequestsServer.
         * This is the behaviour used by Bread-seller agents to serve incoming requests
         * for offer from buyer agents.
         * If the requested Bread is in the local catalogue the seller agent replies
         * with a PROPOSE message specifying the price. Otherwise a REFUSE message is
         * sent back.
         * */
        public void action() {
            // System.out.println("\tinside OfferRequestsServer action");
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Message received. Process it
                try {
                    Order order = (Order) msg.getContentObject();
                    System.out.println(order.guid);
                } catch(Exception e){
//                    System.out.println("something");
                }
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
            catch (Exception e) {
                //LOGGER.error(e);
            }
        }
    }
}
