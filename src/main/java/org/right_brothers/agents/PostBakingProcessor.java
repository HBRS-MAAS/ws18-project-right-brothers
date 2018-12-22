package org.right_brothers.agents;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import org.maas.agents.BaseAgent;
import org.right_brothers.bakery_objects.BakedProduct;
import org.right_brothers.data.messages.ProcessedProductMessage;
import org.maas.utils.JsonConverter;
import org.maas.utils.Time;

@SuppressWarnings("serial")
public class PostBakingProcessor extends BaseAgent{
    private AID coolingRacksAgent;
    private List<BakedProduct> bakedProductList;
    private String bakeryGuid;
    
    protected void setup() {
        super.setup();
        System.out.println("\t"+getAID().getLocalName()+" is born.");
        
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.bakeryGuid = (String) args[0];
        } else {
            this.bakeryGuid = "bakery-001";
        }
        AID ovenManager = new AID(this.bakeryGuid + "-ovenManager", AID.ISLOCALNAME);
        this.coolingRacksAgent = new AID(this.bakeryGuid + "-cooling-rack", AID.ISLOCALNAME);

        this.register("postBakingProcessor-agent", "JADE-bakery");
        this.bakedProductList = new ArrayList<BakedProduct> ();

        addBehaviour(new BakedProductsServer(ovenManager));
    }
    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    
    /*
     * Process products
     */
    @Override
    protected void stepAction(){
        if (baseAgent.getCurrentTime().lessThan(new Time(baseAgent.getCurrentDay(), 12, 0))){
            ArrayList<ProcessedProductMessage> message = this.processProducts();
            if (message.size() > 0) {
                this.sendBakedProducts(message);
            }
        }
        baseAgent.finished();
    }
    private ArrayList<ProcessedProductMessage> processProducts (){
        ArrayList<BakedProduct> temp = new ArrayList<BakedProduct> ();
        ArrayList<ProcessedProductMessage> message = new ArrayList<ProcessedProductMessage> ();
        for (BakedProduct bakedProduct : bakedProductList) {
            if (bakedProduct.getRemainingTimeDuration() < 0){
                bakedProduct.setRemainingTimeDuration(bakedProduct.getIntermediateSteps().get(0).getDuration());
                System.out.println("\tStarted " + bakedProduct.getIntermediateSteps().get(0).getAction() + " " + bakedProduct.getQuantity() + " " + bakedProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
            }
            if (bakedProduct.getRemainingTimeDuration() == 0){
                System.out.println("\tFinished " + bakedProduct.getIntermediateSteps().get(0).getAction() + " " + bakedProduct.getQuantity() + " " + bakedProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
                bakedProduct.finishedStep();
                bakedProduct.setRemainingTimeDuration(-1);
                if (bakedProduct.getIntermediateSteps().size() == 0) {
                    ProcessedProductMessage processedProductMessage = new ProcessedProductMessage();
                    processedProductMessage.setGuid(bakedProduct.getGuid());
                    processedProductMessage.setQuantity(bakedProduct.getQuantity());
                    processedProductMessage.setCoolingDuration(bakedProduct.getCoolingDuration());
                    message.add(processedProductMessage);
                    temp.add(bakedProduct);
                    continue;
                }
            }
            bakedProduct.setRemainingTimeDuration(bakedProduct.getRemainingTimeDuration() - 1);
        }
        for (BakedProduct bp : temp)
            bakedProductList.remove(bp);
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
                System.out.println("\tReceived intermediate product: " + messageContent + " at " + baseAgent.getCurrentHour());
                TypeReference<?> type = new TypeReference<ArrayList<BakedProduct>>(){};
                ArrayList<BakedProduct> receivedBakedProducts = JsonConverter.getInstance(messageContent, type);
                bakedProductList.addAll(receivedBakedProducts);
            }
            else {
                block();
            }
        }
    }
}
