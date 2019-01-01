package org.right_brothers.agents;

import org.right_brothers.visualizer.ui.Visualizer;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;


@SuppressWarnings("serial")
public class VisualizationAgent extends Agent {

    private Visualizer guiWindow;

	protected void setup() {
	// Printout a welcome message
		System.out.println("Hello! Visualization-agent "+getAID().getName()+" is ready.");

        // launch the gui window in another thread
        new Thread() {
            @Override
            public void run() {
                Visualizer.run(new String[] {});
            }
        }.start();
        guiWindow = Visualizer.waitForInstance();
		addBehaviour(new MessageServer());
	}
	protected void takeDown() {
		System.out.println(getAID().getLocalName() + ": Terminating.");
	}

    private class MessageServer extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                guiWindow.updateBoard(msg.getConversationId().toLowerCase(), msg.getContent());
            }
            else {
                block();
            }
        }
    }
}
