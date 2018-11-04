package org.right_brothers.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


@SuppressWarnings("serial")
public class BakeryCustomerAgent extends Agent {
    private AID[] sellerAgents;

    protected void setup() {
        System.out.println("\tCustomer-agent "+getAID().getLocalName()+" is born.");

        this.publishOrderProcessingAID();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.getOrderProcessorAID();
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String Bread_title = (String) args[i];
                addBehaviour(new  RequestPerformer(Bread_title));
            }
        }
        else {
            System.out.println("\tNo Bread title specified");
            doDelete();
        }
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

    protected void publishOrderProcessingAID(){
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
        private String Bread_title;

        RequestPerformer (String Bread_title) {
            this.Bread_title = Bread_title;
        }

		public void action() {
			switch (step) {
			case 0:
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				}
				cfp.setContent(this.Bread_title);
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
						step = 2;
					}
				}
				else {
					block();
				}
				break;
            default:
                step = 0;
                break;
			}
		}
		public boolean done() {
            if (step == 2) {
                myAgent.doDelete();
                return true;
            }
            return false;
		}
	} // End of inner class RequestPerformer


}
