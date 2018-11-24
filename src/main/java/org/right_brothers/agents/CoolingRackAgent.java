package org.right_brothers.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.right_brothers.agents.BaseAgent;
import org.right_brothers.objects.ProcessedProduct;
import org.right_brothers.data.messages.ProductMessage;
import org.right_brothers.utils.JsonConverter;

@SuppressWarnings("serial")
public class CoolingRackAgent extends BaseAgent{
    private AID LOADING_BAY_AGENT = new AID("dummy", AID.ISLOCALNAME);
    private AID ovenManager = new AID("OvenManager", AID.ISLOCALNAME);
    private List<ProcessedProduct> processedProducts;
    
    protected void setup() {
        super.setup();
        System.out.println("\tHello! cooling-rack "+getAID().getLocalName()+" is ready.");
        
        this.register("cooling-rack-agent", "JADE-bakery");
        this.processedProducts = new ArrayList<ProcessedProduct> ();

        addBehaviour(new ProcessedProductsServer(this.ovenManager));
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
            ArrayList<ProcessedProduct> temp = new ArrayList<ProcessedProduct> ();
            for (ProcessedProduct pm : processedProducts) {
                if (pm.getProcessStartTime() < 0){
                    pm.setProcessStartTime(baseAgent.getCurrentHour());
                    System.out.println("\tStarted cooling " + pm.getQuantity() + " " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
                }
                if (baseAgent.getCurrentHour() >= pm.getProcessStartTime() + pm.getCoolingDuration() + 1){
                    System.out.println("\tCooled " + pm.getGuid() + " at time " + baseAgent.getCurrentHour());
                    temp.add(pm);
                }
            }
            for (ProcessedProduct pm : temp)
                processedProducts.remove(pm);
            if (temp.size() > 0) {
                this.sendProducts(temp);
            }
            baseAgent.finished();
        }
        private void sendProducts(ArrayList<ProcessedProduct> temp){
            Hashtable<String,Integer> outMsg = new Hashtable<String,Integer> ();
            for (ProcessedProduct pm : temp) {
                outMsg.put(pm.getGuid(), pm.getQuantity());
            }
            ProductMessage p = new ProductMessage();
            p.setProducts(outMsg);
            String messageContent = JsonConverter.getJsonString(p);
            ACLMessage loadingBayMessage = new ACLMessage(ACLMessage.INFORM);
            loadingBayMessage.addReceiver(LOADING_BAY_AGENT);
            loadingBayMessage.setConversationId("baked-products-152");
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
