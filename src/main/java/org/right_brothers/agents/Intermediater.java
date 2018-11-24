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
import org.right_brothers.data.messages.ProcessedProductMessage;
import org.right_brothers.objects.BakedProduct;
import org.right_brothers.utils.JsonConverter;

@SuppressWarnings("serial")
public class Intermediater extends BaseAgent{
    private AID ovenManager = new AID("OvenManager", AID.ISLOCALNAME);
    private AID coolingRacksAgent = new AID("cooling-rack", AID.ISLOCALNAME);
    private List<BakedProduct> bakedProducts;
    
    protected void setup() {
        super.setup();
        System.out.println("\tIntermediater "+getAID().getLocalName()+" is born.");
        
        this.register("intermediater-agent", "JADE-bakery");
        this.bakedProducts = new ArrayList<BakedProduct> ();

        addBehaviour(new BakedProductsServer(ovenManager));
        addBehaviour(new Process());
    }
    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    
    private class Process extends CyclicBehaviour{
        public void action(){
            if (!baseAgent.getAllowAction()) {
                return;
            }
            ArrayList<ProcessedProductMessage> message = this.processProducts();
            if (message.size() > 0) {
                this.sendBakedProducts(message);
            }
            baseAgent.finished();
        }
        private ArrayList<ProcessedProductMessage> processProducts (){
            ArrayList<BakedProduct> temp = new ArrayList<BakedProduct> ();
            ArrayList<ProcessedProductMessage> message = new ArrayList<ProcessedProductMessage> ();
            for (BakedProduct bp : bakedProducts) {
                if (bp.getProcessStartTime() < 0){
                    if (baseAgent.getCurrentHour() + bp.getIntermediateSteps().get(0).getDuration() + 1 > 12)
                        continue;
                    bp.setProcessStartTime(baseAgent.getCurrentHour());
                    System.out.println("\tStarted " + bp.getIntermediateSteps().get(0).getAction() + " " + bp.getQuantity() + " " + bp.getGuid() + " at time " + baseAgent.getCurrentHour());
                }
                if (baseAgent.getCurrentHour() >= bp.getProcessStartTime() + bp.getIntermediateSteps().get(0).getDuration() + 1){
                    System.out.println("\tFinished " + bp.getIntermediateSteps().get(0).getAction() + " " + bp.getQuantity() + " " + bp.getGuid() + " at time " + baseAgent.getCurrentHour());
                    bp.finishedStep();
                    bp.setProcessStartTime(-1);
                    if (bp.getIntermediateSteps().size() == 0) {
                        ProcessedProductMessage ppm = new ProcessedProductMessage();
                        ppm.setGuid(bp.getGuid());
                        ppm.setQuantity(bp.getQuantity());
                        ppm.setCoolingDuration(bp.getCoolingDuration());
                        message.add(ppm);
                        temp.add(bp);
                    }
                }
            }
            for (BakedProduct bp : temp)
                bakedProducts.remove(bp);
            return message;
        }
        private void sendBakedProducts(ArrayList<ProcessedProductMessage> message){
            String messageContent = JsonConverter.getJsonString(message);
            ACLMessage loadingBayMessage = new ACLMessage(ACLMessage.INFORM);
            loadingBayMessage.addReceiver(coolingRacksAgent);
            loadingBayMessage.setConversationId("baked-products-152");
            loadingBayMessage.setContent(messageContent);
            baseAgent.sendMessage(loadingBayMessage);
        }
    }

    /*
     * Server for the intermediate products from oven manager agent's message
     * */
    private class BakedProductsServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public BakedProductsServer(AID sender){
            this.sender = sender;
        }
        public void action() {
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String messageContent = msg.getContent();
                System.out.println("\tReceived intermediate product: " + messageContent);
                ArrayList<BakedProduct> receivedBakedProducts = this.parseBakedProducts(messageContent);
                bakedProducts.addAll(receivedBakedProducts);
            }
            else {
                block();
            }
        }
        private ArrayList<BakedProduct> parseBakedProducts(String orderString){
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<?> type = new TypeReference<ArrayList<BakedProduct>>(){};
            try {
                ArrayList<BakedProduct> data = mapper.readValue(orderString, type);
                return data;
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
