package org.right_brothers.agents;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

// import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.maas.agents.BaseAgent;
import org.right_brothers.data.models.Order;
import org.maas.utils.JsonConverter;
import org.right_brothers.data.messages.ProofingRequestMessage;

import java.util.*;


@SuppressWarnings("serial")
public class DummyDoughManager extends BaseAgent {

    private AID proofer;
    private String bakeryGuid = "bakery-001";
    private List<Order> orderList;
    private int productCount = 0;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-doughManager "+getAID().getName()+" is ready.");
        orderList = new Vector<Order>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            bakeryGuid = (String) args[0];
        }
        this.proofer = new AID("Proofer_" + bakeryGuid, AID.ISLOCALNAME);
        AID orderProcessor = new AID(bakeryGuid, AID.ISLOCALNAME);

        this.register("DoughManager", this.bakeryGuid + "-doughManager");
        this.addBehaviour(new OrderServer(orderProcessor));
    }

    protected void takeDown() {
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }


    private class ProofingRequestSender extends Behaviour {
        private String message;
        private AID receiver;
        private String conversationId;
        private MessageTemplate mt;
        private int step = 0;
        public ProofingRequestSender(String message, AID receiver, String conversationId) {
            this.message = message;
            this.receiver = receiver;
            this.conversationId = conversationId;
        }
        public void action() {
            switch (step) {
            case 0:
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                cfp.addReceiver(this.receiver);
                cfp.setConversationId(this.conversationId);
                cfp.setContent(this.message);
                baseAgent.sendMessage(cfp);
                System.out.println(" sent cfp to proofer" + this.message);
                this.mt = MessageTemplate.and(MessageTemplate.MatchSender(this.receiver),
                        MessageTemplate.MatchConversationId(this.conversationId));
                step = 1;
                productCount ++;
                break;
            case 1:
                ACLMessage reply = myAgent.receive(mt);
                if (reply != null) {
                    // System.out.println(reply.getContent());
                    if (reply.getPerformative() == ACLMessage.PROPOSE) {
                        System.out.println("proofer is available for " + this.message);
                        ACLMessage msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        msg.addReceiver(this.receiver);
                        msg.setConversationId(this.conversationId);
                        msg.setContent(this.message);
                        baseAgent.sendMessage(msg);
                        this.mt = MessageTemplate.MatchSender(this.receiver);
                        step = 2;
                    }
                    else if (reply.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("proofer is not available for " + this.message);
                        myAgent.addBehaviour(new ProofingRequestSender(this.message, this.receiver, this.conversationId));
                        productCount --;
                        step = 3;
                    }
                }
                else {
                    block();
                }
                break;
            case 2:
                ACLMessage proposal_reply = myAgent.receive(mt);
                if (proposal_reply != null) {
                    if (proposal_reply.getPerformative() == ACLMessage.INFORM) {
                        System.out.println("proofer accepted proofing request for " + this.message);
                    }
                    else if (proposal_reply.getPerformative() == ACLMessage.FAILURE) {
                    // else {
                        System.out.println("proofer refused proofing request for " + this.message);
                        myAgent.addBehaviour(new ProofingRequestSender(this.message, this.receiver, this.conversationId));
                    }
                    productCount --;
                    step = 3;
                }
                else {
                    block();
                }
                break;
            default:
                break;
            }
        }
        public boolean done() {
            if (step == 3) {
                System.out.println(productCount);
                if (productCount == 0) {
                    baseAgent.finished();
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public void stepAction(){
        if (this.orderList.size() > 0){
            List<ProofingRequestMessage> proofingRequests = this.convertOrdersToProofingRequest(this.orderList);
            this.orderList.clear();
            System.out.println(proofingRequests.size());
            // for (ProofingRequestMessage proofingRequest : proofingRequests) {
            for (int i = 0; i < 3; i++) {
                ProofingRequestMessage proofingRequest = proofingRequests.get(i);
                String messageContent = JsonConverter.getJsonString(proofingRequest);
                this.addBehaviour(new ProofingRequestSender(messageContent, this.proofer, "proofing-Request"));
            }
        }
        if (productCount == 0){
            baseAgent.finished();
        }
    }

    private List<ProofingRequestMessage> convertOrdersToProofingRequest(List<Order> orderList){
        Vector<ProofingRequestMessage> proofingRequests = new Vector<ProofingRequestMessage> ();
        for (Order order : orderList) {
            Hashtable<String, Integer> products = order.getProducts();
            Set<String> keys = products.keySet();
            for(String key: keys){
                boolean added = false;
                for (ProofingRequestMessage prm : proofingRequests) {
                    if (prm.getProductType().equals(key)){
                        this.appendGuidAndQuantity(prm, order.getGuid(), products.get(key));
                        added = true;
                        break;
                    }
                }
                if (added){
                    continue;
                }
                ProofingRequestMessage prm = new ProofingRequestMessage();
                prm.setProductType(key);
                prm.setProofingTime(1);
                this.appendGuidAndQuantity(prm, order.getGuid(), products.get(key));
                proofingRequests.add(prm);
            }
        }
        return proofingRequests;
    }

    private void appendGuidAndQuantity(ProofingRequestMessage prm, String guid, int quantity){
        Vector<String> guids = prm.getGuids();
        guids.add(guid);
        prm.setGuids(guids);
        Vector<Integer> productQuantities = prm.getProductQuantities();
        productQuantities.add(quantity);
        prm.setProductQuantities(productQuantities);
    }

    /*
     * Server for the order from order processing agent's message
     * */
    private class OrderServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public OrderServer(AID orderProcessor){
            this.sender = orderProcessor;
        }
        public void action() {
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            // MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("order"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String order = msg.getContent();
                Order o = this.parseOrder(order);
                System.out.println("\tDummy DoughManager Received Order with guid: " + o.getGuid());
                orderList.add(o);
            }
            else {
                block();
            }
        }
        private Order parseOrder(String orderString){
            ObjectMapper mapper = new ObjectMapper();
            try {
                Order data = mapper.readValue(orderString, Order.class);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
