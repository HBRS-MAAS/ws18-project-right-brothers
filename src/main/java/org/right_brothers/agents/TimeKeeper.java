package org.right_brothers.agents;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class TimeKeeper extends Agent{
	private int currentTimeStep;
	private int countAgentsReplied;
	
	protected void setup() {
		System.out.println("Hallo! time-teller-agent "+getAID().getName()+" is ready.");
		
		addBehaviour(new TimeTellerTickBehaviour(this, 100));
		addBehaviour(new TimeStepConfirmationBehaviour());
	}
	
	protected void takeDown() {
	}
	
	private List<DFAgentDescription> getAllAgents(){
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			return Arrays.asList(result);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
			return new Vector<DFAgentDescription>();
		}
	}
	
	private class TimeTellerTickBehaviour extends TickerBehaviour {

		public TimeTellerTickBehaviour(Agent a, long period) {
			super(a, period);
		}
		
		@Override
		protected void onTick() {
			if(countAgentsReplied == 0) {
				List<DFAgentDescription> agents = getAllAgents();
				currentTimeStep++;
				countAgentsReplied = agents.size();
				
				System.out.println("### " + agents.size() + " ###");
				
				for (DFAgentDescription agent : agents) {
					ACLMessage timeMessage = new ACLMessage(ACLMessage.INFORM);
					timeMessage.addReceiver(agent.getName());
					timeMessage.setContent(Integer.toString(currentTimeStep));
					myAgent.send(timeMessage);
				} 
			}
		}
		
	}
	
	private class TimeStepConfirmationBehaviour extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				countAgentsReplied--;
			}
			else {
				block();
			}
		}
	}
}
