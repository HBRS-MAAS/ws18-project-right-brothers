package org.right_brothers.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class CoolingRackAgent extends BaseAgent{
	private AID LOADING_BAY_AGENT = new AID("dummy", AID.ISLOCALNAME);
	
	protected void setup() {
		System.out.println("\tHello! cooling-rack "+getAID().getLocalName()+" is ready.");
		
        this.register("cooling-rack-agent", "JADE-bakery");
		addBehaviour(new BakedProdutsServer());
	}
    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
	
	private class BakedProdutsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(String.format("\t cooling-rack::Received message from oven-manager %s", 
						msg.getSender().getName()));
				System.out.println(String.format("\t message:: %s", msg.getContent()));
				
				// Magically finishes cooling and notifies loading bay
				
				ACLMessage loadingBayMessage = new ACLMessage(ACLMessage.INFORM);
	            loadingBayMessage.addReceiver(LOADING_BAY_AGENT);
	            loadingBayMessage.setConversationId("baked-products-152");
	            loadingBayMessage.setContent(msg.getContent());
	            try {
	                baseAgent.sendMessage(loadingBayMessage);
	            }
	            catch (Exception e) {}
			}
			else {
				block();
			}
		}
	}
}
