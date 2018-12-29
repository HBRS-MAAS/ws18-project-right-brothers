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
import org.maas.data.messages.ProductMessage;

import java.util.*;


@SuppressWarnings("serial")
public class DummyCoolingRackAgent extends BaseAgent {

    private AID preLoadingProcessor;
    private String bakeryGuid = "bakery-001";
    private List<Order> orderList;
    private int cooledProductConvesationNumber = 0;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Cooling-Racks "+getAID().getName()+" is ready.");
        orderList = new Vector<Order>();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            bakeryGuid = (String) args[0];
        }
        /*Registered as order aggregator because loading bay uses yellow pages*/
        this.register("order-aggregator", this.bakeryGuid + "-order-aggregator");
        this.preLoadingProcessor = new AID(bakeryGuid + "-preLoadingProcessor", AID.ISLOCALNAME);
        AID orderProcessor = new AID(bakeryGuid + "-dummy-order-processor", AID.ISLOCALNAME);
        AID loadingbay = new AID(bakeryGuid + "-loader-agent", AID.ISLOCALNAME);

        this.addBehaviour(new InformServer(loadingbay));
        this.addBehaviour(new OrderServer(orderProcessor));
    }

    protected void takeDown() {
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    private class InformServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public InformServer (AID orderProcessor){
            this.sender = orderProcessor;
        }
        public void action() {
            baseAgent.finished();
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String messageContent = msg.getContent();
                System.out.println(String.format("\tOrder-aggregator received msg: %s at %s from %s", 
                            messageContent, baseAgent.getCurrentTime(), msg.getSender().getLocalName()));
            }
            else {
                block();
            }
        }
    }

    private class InformSender extends OneShotBehaviour {
        private MessageTemplate mt;
        private String message;
        private AID receiver;
        private String conversationId;
        
        public InformSender(String message, AID receiver, String conversationId) {
            this.message = message;
            this.receiver = receiver;
            this.conversationId = conversationId;
        }
        public void action() {
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            inform.addReceiver(this.receiver);
            inform.setContent(this.message);
            inform.setConversationId(this.conversationId);
            baseAgent.sendMessage(inform);
        }
    }

    @Override
    public void stepAction(){
        if (this.orderList.size() > 0){
            for (Order order : orderList) {
                ProductMessage cooledProducts = this.convertOrdersToCooledProducts(order);
                String messageContent = JsonConverter.getJsonString(cooledProducts);
                cooledProductConvesationNumber ++;
                this.addBehaviour(new InformSender(messageContent, this.preLoadingProcessor, "cooled-product"));
            }
            this.orderList.clear();
        }
        baseAgent.finished();
    }

    private ProductMessage convertOrdersToCooledProducts(Order order){
        ProductMessage cooledProducts = new ProductMessage();
        Hashtable<String, Integer> products = order.getProducts();
        cooledProducts.setProducts(products);
        System.out.println("\tCooling-Racks sending products for " + order.getGuid());
        return cooledProducts;
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
            MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("order"));
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                String order = msg.getContent();
                Order o = this.parseOrder(order);
                System.out.println("\tCooling-Racks received Order with guid: " + o.getGuid());
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
