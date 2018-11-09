package org.right_brothers;

import java.util.*;
import org.right_brothers.objects.ScenarioParser;
import org.right_brothers.agents.BakeryCustomerAgent;
import org.right_brothers.agents.OrderProcessingAgent;

public class Start {
    public static void main(String[] args) {
        List<String> agents = new Vector<>();
        List<String> cmd = new Vector<>();

        List<String> arguments = Arrays.asList(args);

        ScenarioParser sp = new ScenarioParser();

        ArrayList<String> customer_ids = sp.read_customer_file();
        ArrayList<String> bakery_ids = sp.read_bakery_file();
        BakeryCustomerAgent.setScenarioParser(sp);
        OrderProcessingAgent.setScenarioParser(sp);

        if(arguments.size() > 0) {
            String customArgument = String.join(" ", arguments).trim();
            if(customArgument.equalsIgnoreCase("server")) {
                cmd.add("-agents");
                for (String bakery_id : bakery_ids) {
                    agents.add(bakery_id + ":org.right_brothers.agents.OrderProcessingAgent");
                }
            } else {
                cmd.add("-container");
                cmd.addAll(arguments);
                cmd.add("-agents");
                for (String cus_id : customer_ids) {
                    agents.add(getBuyerAgentInitializationString(cus_id));
                }
            }
        } else {
            cmd.add("-agents");
            agents.add("test:org.right_brothers.agents.DummyAgent");
            for (String bakery_id : bakery_ids) {
                agents.add(bakery_id + ":org.right_brothers.agents.OrderProcessingAgent");
            }
            for (String cus_id : customer_ids) {
                agents.add(getBuyerAgentInitializationString(cus_id));
            }
        }

        cmd.add(String.join(";", agents));
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
    private static String getBuyerAgentInitializationString(String customer_id){
        // String order = "{\"order_date\": { \"day\": 1, \"hour\": 0 }, \"guid\": \"order-001\", \"products\": { \"Bagel\": 10, \"Donut\": 1, \"Berliner\": 8, \"Muffin\": 5, \"Bread\": 0 }, \"customer_id\": \"customer-001\", \"delivery_date\": { \"day\": 2, \"hour\": 20 } }";
        String order = "";
        String buyerString = customer_id + ":org.right_brothers.agents.BakeryCustomerAgent(" + order + ")";
        return buyerString;
    }
}
