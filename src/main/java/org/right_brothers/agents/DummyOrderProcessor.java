package org.right_brothers.agents;

import java.util.*;
// import java.util.stream.Collectors;
import java.io.IOException;
import java.io.File;
import java.util.Scanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
// import jade.lang.acl.UnreadableException;

import org.maas.agents.BaseAgent;
import org.right_brothers.data.models.Order;
import org.right_brothers.data.models.Date;
import org.maas.utils.JsonConverter;
import org.right_brothers.data.messages.UnbakedProductMessage;
import org.right_brothers.data.models.Client;

import java.util.*;


@SuppressWarnings("serial")
public class DummyOrderProcessor extends BaseAgent {

    private List<AID> agentList;
    private int counter = 0;
    private String bakeryGuid = "bakery-001";
    private List<Order> orderList;

    protected void setup() {
        super.setup();
        System.out.println("\tHello! Dummy-orderProcessor "+getAID().getName()+" is ready.");
        this.register("OrderProcessor", "JADE-bakery");

        Object[] args = getArguments();
        String scenarioDirectory = "small";
        if (args != null && args.length > 0) {
            bakeryGuid = (String) args[0];
            scenarioDirectory = (String) args[1];
        }

        this.orderList = this.readOrdersFromJsonFile(scenarioDirectory);
        System.out.println("Total orders: " + this.orderList.size());
        this.agentList = this.getAgentsOfMyBakery();
        System.out.println("Agents in my bakery: " + this.agentList.size());

    }

    protected void takeDown() {
        System.out.println("\t" + getAID().getLocalName() + ": Terminating.");
    }

    private Vector<Order> readOrdersFromJsonFile(String scenarioDirectory) {
        String filePath = "config/" + scenarioDirectory + "/clients.json";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(filePath).getFile());
        String fileString = "";
        try (Scanner sc = new Scanner(file)) {
            sc.useDelimiter("\\Z"); 
            fileString = sc.next();
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TypeReference<?> type = new TypeReference<Vector<Client>>(){};
        Vector<Client> clientList = JsonConverter.getInstance(fileString, type);
        System.out.println("Total clients: " + clientList.size());
        Vector<Order> orderList = new Vector<Order>();
        for (Client client : clientList) {
        // for (int i = 1; i < 2; i++) {
        //     Client client = clientList.get(0);
            orderList.addAll(client.getOrders());
        }
        return orderList;
    }

    private List<AID> getAgentsOfMyBakery(){
        Vector<AID> agentList = new Vector<AID>();
        List<DFAgentDescription> agents;

        // get all alive agents
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            agents = Arrays.asList(result);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
            agents = new Vector<DFAgentDescription>();
        }
        // get only those agents which belong to same bakery as orderProcessor
        for (DFAgentDescription agent : agents) {
            if (agent.getName().getLocalName().substring(0, 10).equals(this.bakeryGuid)){
                agentList.add(agent.getName());
            }
        }
        return agentList;
    }

	private class SendOrder extends OneShotBehaviour {
        private Order order;
        public SendOrder(Order order){
            this.order = order;
        }
		public void action() {
            String messageContent = JsonConverter.getJsonString(this.order);
            ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
            for (AID agent : agentList) {
                inform.addReceiver(agent);
            }
            inform.setContent(messageContent);
            inform.setConversationId("order");
            baseAgent.sendMessage(inform);
		}
	}

    @Override
    public void stepAction(){
        if (baseAgent.getCurrentMinute() == 0){
            Vector<Order> sendOrderList = new Vector<Order>();
            for (Order order : orderList) {
                Date date = order.getOrderDate();
                if (date.getDay() == this.getCurrentDay() && date.getHour() == this.getCurrentHour() ){
                    sendOrderList.add(order);
                }
            }
            for (Order order : sendOrderList) {
                this.addBehaviour(new SendOrder(order));
                this.orderList.remove(order);
            }
        }
        baseAgent.finished();
    }

}
