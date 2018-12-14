package org.maas.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.maas.agents.BaseAgent;
import org.maas.objects.ProcessedProduct;
import org.maas.data.messages.ProductMessage;
import org.maas.utils.JsonConverter;
import org.maas.utils.Time;

@SuppressWarnings("serial")
public class CoolingRackAgent extends BaseAgent{
    private AID LOADING_BAY_AGENT = new AID("dummy", AID.ISLOCALNAME);
    private AID intermediater = new AID("intermediater", AID.ISLOCALNAME);
    private List<ProcessedProduct> processedProducts;
    private int cooledProductConvesationNumber = 0;
    
    protected void setup() {
        super.setup();
        System.out.println("\tHello! cooling-rack "+getAID().getLocalName()+" is ready.");
        
        this.register("cooling-rack-agent", "JADE-bakery");
        this.processedProducts = new ArrayList<ProcessedProduct> ();

        addBehaviour(new ProcessedProductsServer(this.intermediater));
        addBehaviour(new CoolProducts());
    }
    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    
    private class CoolProducts extends CyclicBehaviour{
        public void action(){
            if (!baseAgent.getAllowAction()) {
                return;
            }
            if (baseAgent.getCurrentTime().lessThan(new Time(baseAgent.getCurrentDay(), 12, 0))){
                ArrayList<ProcessedProduct> message = this.getCooledProducts();
                if (message.size() > 0) {
                    this.sendProducts(message);
                }
            }
            baseAgent.finished();
        }
        private ArrayList<ProcessedProduct> getCooledProducts() {
            ArrayList<ProcessedProduct> temp = new ArrayList<ProcessedProduct> ();
            for (ProcessedProduct processedProduct : processedProducts) {
                if (processedProduct.getRemainingTimeDuration() < 0){
                    processedProduct.setRemainingTimeDuration(processedProduct.getCoolingDuration());
                    System.out.println("\tStarted cooling " + processedProduct.getQuantity() + " " + processedProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
                }
                if (processedProduct.getRemainingTimeDuration() == 0){
                    System.out.println("\tCooled " + processedProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
                    temp.add(processedProduct);
                }
                processedProduct.setRemainingTimeDuration(processedProduct.getRemainingTimeDuration() - 1);
            }
            for (ProcessedProduct pm : temp)
                processedProducts.remove(pm);
            return temp;
        }
        private void sendProducts(ArrayList<ProcessedProduct> temp){
            Hashtable<String,Integer> outMsg = new Hashtable<String,Integer> ();
            for (ProcessedProduct pm : temp) {
                if (outMsg.containsKey(pm.getGuid())){
                    outMsg.put(pm.getGuid(), pm.getQuantity() + outMsg.get(pm.getGuid()));
                }
                else {
                    outMsg.put(pm.getGuid(), pm.getQuantity());
                }
            }
            ProductMessage p = new ProductMessage();
            p.setProducts(outMsg);
            String messageContent = JsonConverter.getJsonString(p);
            ACLMessage loadingBayMessage = new ACLMessage(ACLMessage.INFORM);
            loadingBayMessage.addReceiver(LOADING_BAY_AGENT);
            cooledProductConvesationNumber ++;
            loadingBayMessage.setConversationId("cooled-product-" + Integer.toString(cooledProductConvesationNumber));
            loadingBayMessage.setContent(messageContent);
            baseAgent.sendMessage(loadingBayMessage);
        }
    }

    private class ProcessedProductsServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public ProcessedProductsServer (AID sender){
            this.sender = sender;
        }
        public void action() {
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(this.sender));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println(String.format("\tcooling-rack::Received message from oven-manager %s", 
                        msg.getSender().getName()));
                String messageContent = msg.getContent();
                System.out.println(String.format("\tmessage:: %s", messageContent));
                ArrayList<ProcessedProduct> receivedProcessedProducts = this.parseProcessedProducts(messageContent);
                processedProducts.addAll(receivedProcessedProducts);
            }
            else {
                block();
            }
        }
        private ArrayList<ProcessedProduct> parseProcessedProducts(String orderString){
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<?> type = new TypeReference<ArrayList<ProcessedProduct>>(){};
            try {
                ArrayList<ProcessedProduct> data = mapper.readValue(orderString, type);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
