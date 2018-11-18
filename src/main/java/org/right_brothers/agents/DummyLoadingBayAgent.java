package org.right_brothers.agents;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class DummyLoadingBayAgent extends Agent{
	protected void setup() {
		System.out.println("Hello! dummy-loading-bay "+getAID().getLocalName()+" is ready.");
		
		addBehaviour(new TimeStepConfirmationBehaviour());
	}
	
	private class TimeStepConfirmationBehaviour extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				System.out.println(String.format("\t dummy-loading-bay::Received message from cooling-rack %s", 
						msg.getSender().getName()));
				System.out.println(String.format("\t message:: %s", msg.getContent()));
			}
			else {
				block();
			}
		}
	}
}
