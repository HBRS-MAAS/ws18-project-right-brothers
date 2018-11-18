package org.right_brothers.agents;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import org.right_brothers.data.messages.CoordinatorMessage;
import org.right_brothers.agents.BaseAgent;

import jade.core.behaviours.*;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

@SuppressWarnings("serial")
public class CoordinatorAgent extends BaseAgent {
    private List<String> messages;

    protected void setup() {
        System.out.println("\tCoordinator-agent "+getAID().getLocalName()+" is born.");
        this.register("Coordinator-Agent", "JADE-Bakery-Testing");
        messages = new Vector<String>();

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
            MessageTemplate requestTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);

            ACLMessage requestMessage = myAgent.receive(requestTemplate);
            if (requestMessage != null) {
                String id = requestMessage.getContent();
                System.out.println(String.format("\tReceived REQUEST: %s", id));

                ACLMessage reply = requestMessage.createReply();
                Optional<String> result = messages.stream()
                 .filter(m -> id.equals(m))
                 .findFirst();
                if(result.isPresent()) {
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent(result.get());
                }else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("");
                }
                baseAgent.sendMessage(reply);
            }
            else {
                block();
            }
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

                System.out.println(String.format("\tReceived INFORM: %s", data));
                System.out.println("\tCurrent messages list size: " + messages.size());
                ACLMessage reply = informMessage.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Got your Object.");
                baseAgent.sendMessage(reply);
            }
            else {
                block();
            }
        }
    }
}
