package org.right_brothers.agents;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import org.right_brothers.data.messages.CoordinatorMessage;

import jade.core.behaviours.*;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

@SuppressWarnings("serial")
public class CoordinatorAgent extends Agent {
    private List<CoordinatorMessage> messages;

    protected void setup() {
        System.out.println("\tCoordinator-agent "+getAID().getLocalName()+" is born.");
        messages = new Vector<CoordinatorMessage>();

        addBehaviour(new RequestsServer());
        addBehaviour(new InformServer());
    }

    protected void takeDown() {
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
                Optional<CoordinatorMessage> result = messages.stream()
                 .filter(m -> id.equals(m.getId()))
                 .findFirst();
                try {
                    if(result.isPresent()) {
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContentObject(result.get());
                    }else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContentObject(null);
                    }
                }catch(IOException e) {
                    e.printStackTrace();
                }
                myAgent.send(reply);
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
                try {
                    CoordinatorMessage data = (CoordinatorMessage)informMessage.getContentObject();
                    messages.add(data);

                    System.out.println(String.format("\tReceived INFORM: %s", data.getClass()));
                    System.out.println("\tMessage: " + data.getId());
                    System.out.println("\tCurrent messages list size: " + messages.size());
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                ACLMessage reply = informMessage.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Got your Object.");
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }
}