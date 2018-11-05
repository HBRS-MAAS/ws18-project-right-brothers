package org.right_brothers.agents;

import jade.core.Agent;
import jade.core.behaviours.*;
//import jade.lang.acl.ACLMessage;


@SuppressWarnings("serial")
public class DummyAgent extends Agent {
	protected void setup() {
	// Printout a welcome message
		System.out.println("\tHello! Dummy-agent "+getAID().getName()+" is ready.");

        this.doDelete();

	}
	protected void takeDown() {
		System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
	}

}
