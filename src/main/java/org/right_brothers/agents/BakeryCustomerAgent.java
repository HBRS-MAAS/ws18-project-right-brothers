package org.right_brothers.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.right_brothers.objects.Order;
//import org.json.simple.JSONObject;

@SuppressWarnings("serial")
public class BakeryCustomerAgent extends Agent {
    // The list of known seller agents
    private AID[] sellerAgents;
//    private Order order;

    protected void setup() {
		// Printout a welcome message
        System.out.println("\tCustomer-agent "+getAID().getLocalName()+" is born.");

        this.publishOrderProcessingAID();
        // wait for order processing agents to be born
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Order order = new Order("order-001");
        // get the agent id of all the available order processing agents
        this.getCustomerAID();
        // Get the title of the Bread to buy as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String Bread_title = (String) args[i];
                // System.out.println("\tTrying to buy " + Bread_title);
                addBehaviour(new  RequestPerformer(order));
            }
        }
        else {
            // Make the agent terminate immediately
            System.out.println("\tNo Bread title specified");
            doDelete();
        }
        // addBehaviour(new shutdown());
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    protected void publishOrderProcessingAID(){
        // Register the Bakery-customer-agent service in the yellow pages
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
    protected void getCustomerAID() {
        // Update the list of customers agents
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

	private class RequestPerformer extends Behaviour {
        /*
        Inner class RequestPerformer.
        This is the behaviour used by Bread-buyer agents to request seller
        agents the target Bread.
        */
//		private AID bestSeller; // The agent who provides the best offer
//		private int bestPrice; // The best offered price
//		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
        private Order order;

        RequestPerformer (Order order) {
            this.order = order;
            // System.out.println("inside constructor of RequestPerformer " + this.Bread_title);
        }

		public void action() {
			switch (step) {
			case 0:
				// Send the request to all sellers
                // System.out.println("\t" + myAgent.getLocalName() + " inside step 0 " + this.Bread_title);
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
				// Prepare the template to get confirmation
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("Bread-trade"),
				MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
                // System.out.println("\t" + myAgent.getLocalName() + " inside step 1 " + this.Bread_title);
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
                    // System.out.println(reply.getContent());
					if (reply.getPerformative() == ACLMessage.CONFIRM) {
						// received confirmation
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
            // if got confirmation, kill the agent
            if (step == 2) {
                myAgent.doDelete();
                return true;
            }
            else {
                return false;
            }
		}
	} // End of inner class RequestPerformer


}
