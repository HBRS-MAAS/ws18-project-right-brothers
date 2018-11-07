package org.right_brothers.agents;

import jade.core.Agent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.right_brothers.objects.Order;
//import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class BakeryCustomerAgent extends Agent {
    private AID[] sellerAgents;

    private static int totalAgents;

    protected void setup() {
        System.out.println("\tCustomer-agent "+getAID().getLocalName()+" is born.");
        totalAgents++;
        
        this.publishCustomerAID();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Order order = new Order("order-001");
        
        this.getOrderProcessorAID();
        Object[] args = getArguments();
        
        addBehaviour(new  RequestPerformer(order));
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    protected void publishCustomerAID(){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Bakery-customer-agent");
        sd.setName("JADE-bakery");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    protected void getOrderProcessorAID() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Order-processing-agent");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            sellerAgents = new AID[result.length];
            for (int i = 0; i < result.length; ++i) {
                sellerAgents[i] = result[i].getName();
            }
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /*
     * Inner class RequestPerformer.
     * This is the behavior used by Bread-buyer agents to request seller
     * agents the target Bread.
     * */
	private class RequestPerformer extends Behaviour {
		private MessageTemplate mt;
		private int step = 0;
    private Order order;

    RequestPerformer (Order order) {
        this.order = order;
    }

		public void action() {
			switch (step) {
			case 0:
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				}
        try {
            cfp.setContentObject(this.order);

        } catch(Exception e){
            e.printStackTrace();
        }
				cfp.setConversationId("Bread-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Bread-trade"),
				MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.CONFIRM) {
                        System.out.println("\t" + myAgent.getLocalName() + " received confirmation from " + reply.getSender().getLocalName());
                        totalAgents--;
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
                System.out.println(totalAgents);
            	if(totalAgents == 0) {
                    myAgent.addBehaviour(new shutdown());
            	}
                else {
                    myAgent.doDelete();
                }
                return true;
            }
            return false;
		}
		
	} // End of inner class RequestPerformer

    // Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
    private class shutdown extends OneShotBehaviour{
		public void action() {
            System.out.println("inside shutdown behaviour");
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
