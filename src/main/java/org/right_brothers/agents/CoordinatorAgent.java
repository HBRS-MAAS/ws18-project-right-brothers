package org.right_brothers.agents;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import org.right_brothers.data.messages.CoordinatorMessage;
import org.right_brothers.data.messages.TimeStep;

import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CoordinatorAgent extends BaseAgent {
	private List<CoordinatorMessage> messages;
	
    protected void setup() {
        this.register("order-processing", "coordinator");
        messages = new Vector<CoordinatorMessage>();
        
        addBehaviour(new RequestsServer());
    }

    protected void takeDown() {
        deRegister();
    }

    private class RequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate requestTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            MessageTemplate responseTemplate = MessageTemplate.MatchPerformative(ACLMessage.REQUEST_WHEN);
            MessageTemplate informTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            
            ACLMessage requestMessage = myAgent.receive(requestTemplate);
            ACLMessage responseMessage = myAgent.receive(responseTemplate);
            ACLMessage informMessage = myAgent.receive(informTemplate);
            if (requestMessage != null) {
                try {
                	CoordinatorMessage data = (CoordinatorMessage)requestMessage.getContentObject();
                	messages.add(data);
                	
					System.out.println(String.format("Received message of type: %s", data.getClass()));
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
            }
            else if(responseMessage != null) {
                String id = responseMessage.getContent();
                System.out.println(String.format("Received query for: %s", id));
                
                ACLMessage reply = responseMessage.createReply();
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
            if (informMessage != null) {
                try {
                	TimeStep step = (TimeStep)informMessage.getContentObject();
                	currentTime = step;
                	
					System.out.println(String.format("Updated time step day: %d, hour: %d", step.getDay(), step.getHour()));
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
            }
            else {
                block();
            }
        }
    }
}
