package org.right_brothers.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.maas.agents.BaseAgent;

@SuppressWarnings("serial")
public class DummyPackaging extends BaseAgent{
    private AID coolingRackAgent;
    private String bakeryGuid = "bakery-001";
    
    protected void setup() {
        super.setup();
        System.out.println("\tdummy-packaging-agent "+getAID().getLocalName()+" is ready.");
        
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bakeryGuid = (String) args[0];
        }
        
        this.coolingRackAgent = new AID(this.bakeryGuid + "-cooling-rack", AID.ISLOCALNAME);
       
        addBehaviour(new ProductMessageServer(this.coolingRackAgent));
    }
    
    protected void takeDown() {
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    
    private class ProductMessageServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public ProductMessageServer (AID sender){
            this.sender = sender;
        }
        public void action() {
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(this.sender));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println(String.format("\tdummy-packaging-agent::%s Received message from cooling-rack %s", 
                        this.myAgent.getLocalName(), msg.getSender().getLocalName()));
                String messageContent = msg.getContent();
                System.out.println(String.format("\tmessage:: %s", messageContent));
            }
            else {
                block();
            }
        }
    }
}
