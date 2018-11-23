package org.right_brothers;

import java.util.*;
import org.right_brothers.agents.BakeryCustomerAgent;
import org.right_brothers.agents.OrderProcessingAgent;
import org.right_brothers.utils.InputParser; 
import org.right_brothers.data.models.Client;
import org.right_brothers.data.models.Bakery;

import com.fasterxml.jackson.core.type.TypeReference;

public class Start {
    public static void main(String[] args) {
        List<String> agents = new Vector<>();
        List<String> cmd = new Vector<>();

        List<String> arguments = Arrays.asList(args);

		InputParser<Vector<Client>> parser = new InputParser<>
			("/config/sample/clients.json", new TypeReference<Vector<Client>>(){});
		
		List<Client> clients = parser.parse();

		InputParser<Vector<Bakery>> parser2 = new InputParser<>
			("/config/sample/bakeries.json", new TypeReference<Vector<Bakery>>(){});
		List<Bakery> bakeries = parser2.parse();

        BakeryCustomerAgent.setClients(clients);
        OrderProcessingAgent.setBakeries(bakeries);
        System.out.println(clients.size());

        if(arguments.size() > 0) {
            String customArgument = String.join(" ", arguments).trim();
            if(arguments.get(0).equalsIgnoreCase("OvenManagerTest")){
                cmd.add("-agents");
                agents.add("dummy:org.right_brothers.agents.OvenManagerTester");
                agents.add("ovenManager:org.right_brothers.agents.OvenManager");
                agents.add("cooling-rack:org.right_brothers.agents.CoolingRackAgent");
                agents.add("dummy-loading-bay:org.right_brothers.agents.DummyLoadingBayAgent");
            }
            if(arguments.get(0).equalsIgnoreCase("coordinatorTest")){
                cmd.add("-agents");
                agents.add("TimeKeeper:org.right_brothers.agents.TimeKeeper");
                agents.add("dummy:org.right_brothers.agents.DummyAgent");
                agents.add("coordinator:org.right_brothers.agents.CoordinatorAgent");
            }
            if(arguments.get(0).equalsIgnoreCase("coordinatorTestVisualization")){
                cmd.add("-agents");
                agents.add("TimeKeeper:org.right_brothers.agents.TimeKeeper");
                agents.add("dummy:org.right_brothers.agents.DummyAgent");
                agents.add("coordinator:org.right_brothers.agents.CoordinatorAgent");
                agents.add("visualization:org.right_brothers.agents.VisualizationAgent");
            }
            if(arguments.get(0).equalsIgnoreCase("server")) {
                for (int i = 1; i < arguments.size(); i++) {
                   cmd.add(arguments.get(i)); 
                }
                cmd.add("-agents");
                for (Bakery b : bakeries) {
                    agents.add(b.getGuid() + ":org.right_brothers.agents.OrderProcessingAgent");
                }
            } else if(arguments.get(0).equalsIgnoreCase("-host")) {
                cmd.add("-container");
                cmd.addAll(arguments);
                cmd.add("-agents");
                for (Client c : clients) {
                    agents.add(c.getGuid() + ":org.right_brothers.agents.BakeryCustomerAgent");
                }
            }
        } else {
            cmd.add("-agents");
            agents.add("TimeKeeper:org.right_brothers.agents.TimeKeeper");
            for (Bakery b : bakeries) {
                agents.add(b.getGuid() + ":org.right_brothers.agents.OrderProcessingAgent");
            }
            for (Client c : clients) {
                agents.add(c.getGuid() + ":org.right_brothers.agents.BakeryCustomerAgent");
            }
        }

        cmd.add(String.join(";", agents));
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
}
