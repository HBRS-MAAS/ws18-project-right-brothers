package org.right_brothers.agents;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;

import javafx.application.Application;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.domain.FIPANames;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.util.Arrays;
import java.util.List;

import org.right_brothers.data.messages.BakedProduct;
import org.right_brothers.data.messages.CoordinatorMessage;
import org.right_brothers.data.messages.Dough;
import org.right_brothers.data.messages.UnbakedProduct;

import org.right_brothers.agents.Animation;


@SuppressWarnings("serial")
public class VisualizationAgent extends Agent {

	protected void setup() {
	// Printout a welcome message
		System.out.println("Hello! Visualization-agent "+getAID().getName()+" is ready.");
// 		Application.launch(Animation.class); // Currently the platform shutdown is called when the application is closed.
// 		addBehaviour(new shutdown());
		addBehaviour(new MessageServer());

	}
	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}

    private class MessageServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                String msgString =  msg.getContent();
                System.out.println("\tMessage inside VisualizationAgent " + msgString);
            }
            else {
                block();
            }
        }
    }
}
