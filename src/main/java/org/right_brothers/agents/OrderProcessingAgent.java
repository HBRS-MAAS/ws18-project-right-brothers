package org.right_brothers.agents;

import java.util.*;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;

import org.right_brothers.data.models.Order;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Location;
import org.right_brothers.data.models.Equipment;
import org.right_brothers.data.models.Product;

public class OrderProcessingAgent extends Agent {

    private static List<Bakery> bakeries;
    private String guid;
    private String name;
    private Location location;
    private Equipment equipment;
    private List<Product> products;

    protected void setup() {
        System.out.println("\tOrder-processing-agent "+getAID().getLocalName()+" is born.");

        this.publishSellerAID();
        this.getBakeryInformation();
        
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
    public static void setBakeries(List<Bakery> list_of_bakeries){
        bakeries = list_of_bakeries;
    }
    private void getBakeryInformation() {
        for (Bakery b : this.bakeries) {
            if (b.getGuid().equals(getAID().getLocalName())) {
                this.guid = b.getGuid();
                this.location = b.getLocation();
                this.name = b.getName();
                this.equipment = b.getEquipment();
                this.products = b.getProducts();
            }
        }
    }

    /*
     * Inner class OfferRequestsServer.
     * This is the behaviour used by Bread-seller agents to serve incoming requests
     * for offer from buyer agents.
     * The customer agents send PROPOSE messages and this behavior responds with a CONFIRM message
     * */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Message received. Process it
                try {
                    Order order = (Order) msg.getContentObject();
                    System.out.println("\tOrder guid " + order.getGuid());
                    System.out.println("\tOrder customer id " + order.getCustomerId());
                } catch(Exception e){
                    System.out.println("Could not read order");
                }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Got your order.");
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
}
