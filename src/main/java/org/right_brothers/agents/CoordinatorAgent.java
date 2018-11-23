package org.right_brothers.agents;

import java.util.List;
import java.util.Vector;
import jade.core.behaviours.*;
// import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.right_brothers.agents.BaseAgent;

@SuppressWarnings("serial")
public class CoordinatorAgent extends BaseAgent {
    private List<String> messages;

    protected void setup() {
        super.setup();
        System.out.println("\tCoordinator-agent "+getAID().getLocalName()+" is born.");

        messages = new Vector<String>();

        this.register("Coordinator-agent", "JADE-bakery");

        addBehaviour(new RequestsServer());
        addBehaviour(new InformServer());
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t"+getAID().getLocalName()+" terminating.");
    }

    /*
     * Serves the request from various types of agent. Receives a REQUEST from an agent and reply 
     * with whatever was asked by the agent
     */
    private class RequestsServer extends CyclicBehaviour {
        public void action() {
            baseAgent.finished();
            MessageTemplate requestTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

            ACLMessage requestMessage = myAgent.receive(requestTemplate);
            if (requestMessage != null) {
                String id = requestMessage.getContent();
                System.out.println(String.format("\t" + myAgent.getLocalName() + " received REQUEST: %s", id));

                ACLMessage reply = requestMessage.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent(null);
                String mess = this.isMessageConfirmable(id, messages); // Please add your logic here to decide when message can be termed as confirmed.
                if (mess != null) {
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent(mess);
                }
                baseAgent.sendMessage(reply);
            }
            else {
                block();
            }
        }

        public String isMessageConfirmable(String id, List<String> msg) {
            for (String m : msg) {
                try {
                    if (m.contains(id)) {
                        return m;
                    }
                }
                catch(NullPointerException e)
                {
                    System.out.println("m string in Null");
                    return null;
                }
            }
            return null;
        }
    }

    /*
     * Serves the inform messages from various types of agent. Receives a INFORM from an agent 
     * and adds the content object to a queue reply with CONFIRM 
     */
    private class InformServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate informTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

            ACLMessage informMessage = myAgent.receive(informTemplate);
            if (informMessage != null) {
                String data = informMessage.getContent();
                messages.add(data);
                System.out.println("\t" + myAgent.getLocalName() + " received INFORM:" + data);
                System.out.println("\t" + myAgent.getLocalName() + " current messages list size: " + messages.size());
                ACLMessage reply = informMessage.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Got your Message.");
                baseAgent.sendMessage(reply);
            }
            else {
                block();
            }
        }
    }
}
