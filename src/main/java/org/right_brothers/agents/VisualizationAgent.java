package org.right_brothers.agents;

import javafx.application.Application;

import org.right_brothers.utils.Animation;

import jade.core.Agent;
// import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
// import jade.lang.acl.MessageTemplate;

// import java.util.Arrays;
// import java.util.List;


@SuppressWarnings("serial")
public class VisualizationAgent extends Agent {

    private Animation guiWindow;
    public int counter;

	protected void setup() {
	// Printout a welcome message
		System.out.println("Hello! Visualization-agent "+getAID().getName()+" is ready.");

        this.counter = 0;
        // launch the gui window in another thread
        new Thread() {
            @Override
            public void run() {
                Application.launch(Animation.class);
            }
        }.start();
        guiWindow = Animation.waitForStartUpTest();
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
                counter ++;
                guiWindow.editTextObject(counter);
            }
            else {
                block();
            }
        }
    }
}
