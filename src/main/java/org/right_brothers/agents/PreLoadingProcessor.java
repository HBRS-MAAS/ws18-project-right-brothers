package org.right_brothers.agents;

import java.util.*;
// import java.util.stream.Collectors;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

// import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
// import jade.domain.FIPAException;
// import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.maas.agents.BaseAgent;
import org.right_brothers.bakery_objects.CooledProduct;
import org.right_brothers.utils.InputParser; 
import org.right_brothers.data.models.Product;
import org.right_brothers.data.models.Bakery;
import org.right_brothers.data.models.Step;
import org.right_brothers.data.messages.ProductMessage;
import org.right_brothers.data.messages.CompletedProductMessage;
import org.maas.utils.JsonConverter;
import org.maas.utils.Time;

@SuppressWarnings("serial")
public class PreLoadingProcessor extends BaseAgent {
    private AID coolingRackAgent;
    private AID packagingAgent;
    private List<Product> availableProductList;
    private String bakeryGuid = "bakery-001";
    private List<CooledProduct> cooledProductsList;
    private int processedProductConversationNumber = 0;

    protected void setup() {
        super.setup();
        System.out.println("\tPreLoadingProcessor "+getAID().getLocalName()+" is born.");
        Object[] args = getArguments();
        String scenarioDirectory = "small";
        String whichTest = "single-stage";
        if (args != null && args.length > 0) {
            this.bakeryGuid = (String) args[0];
            scenarioDirectory = (String) args [1];
            whichTest = (String) args [2];
        }
        if (whichTest.equals("single-stage")) {
            System.out.println("\t\t Packaging Single Stage Testing");
            coolingRackAgent = new AID(this.bakeryGuid + "-dummy-cooling-racks", AID.ISLOCALNAME);
        } else {
            /*Normal operation use actual cooling racks agent*/
            coolingRackAgent = new AID(this.bakeryGuid + "-cooling-rack", AID.ISLOCALNAME);
        }
        AID orderProcessor = new AID(this.bakeryGuid + "-dummy-order-processor", AID.ISLOCALNAME);
        packagingAgent = new AID(this.bakeryGuid + "-packaging-agent", AID.ISLOCALNAME);
        this.register("PreLoadingProcessor", this.bakeryGuid+"-PreLoadingProcessor");

        this.cooledProductsList = new ArrayList<CooledProduct> ();
        this.availableProductList = new ArrayList<Product> ();

        // TODO: get bakery guid as argument
        //         Object[] args = getArguments();

        this.getAllInformation(scenarioDirectory);

        this.addBehaviour(new CooledProductsServer(coolingRackAgent));
    }

    protected void takeDown() {
        this.deRegister();
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }
    private void getAllInformation(String scenarioDirectory){
		InputParser<Vector<Bakery>> parser2 = new InputParser<>
			("/config/"+scenarioDirectory+"/bakeries.json", new TypeReference<Vector<Bakery>>(){});
		List<Bakery> bakeries = parser2.parse();
        for (Bakery b : bakeries) {
            if (b.getGuid().equalsIgnoreCase(this.bakeryGuid)){
                this.availableProductList = b.getProducts();
                break;
            }
        }
    }

    /*
     * Processing products after cooling
     */
    @Override
    protected void stepAction(){
        if (baseAgent.getCurrentTime().lessThan(new Time(baseAgent.getCurrentDay(), 12, 0))){
            ArrayList<CompletedProductMessage> message = this.processProducts();
            if (message.size() > 0) {
                this.sendCompletedProducts(message);
            }
        }
        baseAgent.finished();
    }
    private ArrayList<CompletedProductMessage> processProducts (){
        ArrayList<CooledProduct> temp = new ArrayList<CooledProduct> ();
        ArrayList<CompletedProductMessage> message = new ArrayList<CompletedProductMessage> ();
        for (CooledProduct cooledProduct : cooledProductsList) {
            if (cooledProduct.getRemainingTimeDuration() < 0){
                cooledProduct.setRemainingTimeDuration(cooledProduct.getIntermediateSteps().get(0).getDuration());
                // System.out.println("\tStarted " + cooledProduct.getIntermediateSteps().get(0).getAction() + " " + cooledProduct.getQuantity() + " " + cooledProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
            }
            if (cooledProduct.getRemainingTimeDuration() == 0){
                // System.out.println("\tFinished " + cooledProduct.getIntermediateSteps().get(0).getAction() + " " + cooledProduct.getQuantity() + " " + cooledProduct.getGuid() + " at time " + baseAgent.getCurrentHour());
                cooledProduct.finishedStep();
                cooledProduct.setRemainingTimeDuration(-1);
                if (cooledProduct.getIntermediateSteps().size() == 0) {
                    message.add(cooledProduct.getCompletedProductMessage());
                    temp.add(cooledProduct);
                    continue;
                }
            }
            cooledProduct.setRemainingTimeDuration(cooledProduct.getRemainingTimeDuration() - 1);
        }
        for (CooledProduct cooledProduct : temp)
            cooledProductsList.remove(cooledProduct);
        return message;
    }
    private void sendCompletedProducts(ArrayList<CompletedProductMessage> message){
        String messageContent = JsonConverter.getJsonString(message);
        ACLMessage loadingBayMessage = new ACLMessage(ACLMessage.INFORM);
        loadingBayMessage.addReceiver(packagingAgent);
        processedProductConversationNumber ++;
        loadingBayMessage.setConversationId("baked-products-" + Integer.toString(processedProductConversationNumber));
        loadingBayMessage.setContent(messageContent);
        baseAgent.sendMessage(loadingBayMessage);
    }

    /*
     * Server for the order guid for the dough preparation stage agent's(proofer) message
     * */
    private class CooledProductsServer extends CyclicBehaviour {
        private MessageTemplate mt;
        private AID sender;

        public CooledProductsServer(AID proofer){
            this.sender = proofer;
        }
        public void action() {
            baseAgent.finished();
            this.mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(sender));
            MessageTemplate mt2 = MessageTemplate.and(this.mt, MessageTemplate.MatchConversationId("cooled-product"));
            ACLMessage msg = myAgent.receive(mt2);
            if (msg != null) {
                String messageContent = msg.getContent();
                // System.out.println("\tPreLoadingProcessor received cooled product " + messageContent);
                TypeReference<?> type = new TypeReference<ProductMessage>(){};
                ProductMessage productMessage = JsonConverter.getInstance(messageContent, type);
                Set<String> keys = productMessage.getProducts().keySet();
                for(String productName: keys){
                    CooledProduct cooledProduct = this.getCooledProductFromProductName(productName);
                    cooledProduct.setQuantity(productMessage.getProducts().get(productName));
                    cooledProductsList.add(cooledProduct);
                }
            }
            else {
                block();
            }
        }
        private CooledProduct getCooledProductFromProductName(String productName){
            CooledProduct up = new CooledProduct();
            Product p = this.getProductWithSameGuid(productName);
            up.setGuid(p.getGuid());
            Vector<Step> steps = new Vector<Step> ();
            boolean addStep = false;
            // ASSUMPTION: The steps are in order of recipe.
            for (Step s : p.getRecipe().getSteps()) {
                if (s.getAction().equals("cooling")){
                    addStep = true;
                    continue;
                }
                if (addStep) {
                    steps.add(s);
                }
            }
            up.setIntermediateSteps(steps);
            return up;
        }
        private Product getProductWithSameGuid(String productName){
            for (Product p : availableProductList) {
                if (p.getGuid().equals(productName)){
                    return p;
                }
            }
            System.out.println("Product with name " + productName + " is not offered by " + bakeryGuid);
            // TODO: make codacy approved Error
            // throw new Error("Product with name " + productName + " is not offered by " + bakeryGuid);
            return null;
        }
    }
}
