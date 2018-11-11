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

        if(arguments.size() > 0) {
            String customArgument = String.join(" ", arguments).trim();
            if(customArgument.equalsIgnoreCase("server")) {
                cmd.add("-agents");
                for (Bakery b : bakeries) {
                    agents.add(b.getGuid() + ":org.right_brothers.agents.OrderProcessingAgent");
                }
            } else {
                cmd.add("-container");
                cmd.addAll(arguments);
                cmd.add("-agents");
                for (Client c : clients) {
                    agents.add(c.getGuid() + ":org.right_brothers.agents.BakeryCustomerAgent");
                }
            }
        } else {
            cmd.add("-agents");
            agents.add("test:org.right_brothers.agents.DummyAgent");
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
