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
        String scenarioDir = "small";

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
            if(arguments.get(0).equalsIgnoreCase("packagingStageTest")){
                cmd.add("-agents");
                String endTime = "000.13.00";
                agents.add("dummy:org.right_brothers.agents.PackagingStageTester");
                agents.add("postBakingProcessor:org.right_brothers.agents.PreLoadingProcessor");
                agents.add("packaging-agent:org.right_brothers.agents.LoadingBayAgent");
                agents.add("TimeKeeper:org.maas.agents.TimeKeeper(" + scenarioDir + ", " + endTime + ")");
            }
            if(arguments.get(0).equalsIgnoreCase("bakingStageTest")){
                cmd.add("-agents");
                String endTime = "000.06.00";
                agents.add("dummy:org.right_brothers.agents.BakingStageTester");
                agents.add("ovenManager:org.right_brothers.agents.OvenManager");
                agents.add("intermediater:org.right_brothers.agents.PostBakingProcessor");
                agents.add("cooling-rack:org.maas.agents.CoolingRackAgent");
                agents.add("TimeKeeper:org.maas.agents.TimeKeeper(" + scenarioDir + ", " + endTime + ")");
            }
            if(arguments.get(0).equalsIgnoreCase("coordinatorTest")){
                cmd.add("-agents");
                String endTime = "000.06.00";
                agents.add("dummy:org.right_brothers.agents.DummyAgent");
                agents.add("coordinator:org.right_brothers.agents.CoordinatorAgent");
                agents.add("TimeKeeper:org.maas.agents.TimeKeeper(" + scenarioDir + ", " + endTime + ")");
            }
            if(arguments.get(0).equalsIgnoreCase("coordinatorTestVisualization")){
                cmd.add("-agents");
                String endTime = "000.06.00";
                agents.add("dummy:org.right_brothers.agents.DummyAgent");
                agents.add("coordinator:org.right_brothers.agents.CoordinatorAgent");
                agents.add("visualization:org.right_brothers.agents.VisualizationAgent");
                agents.add("TimeKeeper:org.maas.agents.TimeKeeper(" + scenarioDir + ", " + endTime + ")");
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
            agents.add("TimeKeeper:org.maas.agents.TimeKeeper");
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
