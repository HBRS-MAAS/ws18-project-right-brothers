package org.right_brothers;

import java.util.*;
//import org.right_brothers.objects.Order;
//import org.right_brothers.agents.DummyAgent;
//import org.right_brothers.agents.BakeryCustomerAgent;
//import org.right_brothers.agents.OrderProcessingAgent;

public class Start {
    public static void main(String[] args) {
        List<String> agents = new Vector<>();
        agents.add("test:org.right_brothers.agents.DummyAgent");
        agents.add("seller1:org.right_brothers.agents.OrderProcessingAgent");


        Start someObjectName = new Start();
        for (int i = 1; i < 2 ; i++) {
            agents.add(someObjectName.getBuyerAgentInitializationString(i));
        }

        List<String> cmd = new Vector<>();
        cmd.add("-agents");
        StringBuilder sb = new StringBuilder();
        for (String a : agents) {
            sb.append(a);
            sb.append(";");
        }
        cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
    private String getBuyerAgentInitializationString(int buyerID){
//        String order = "{\"order_date\": { \"day\": 1, \"hour\": 0 }, \"guid\": \"order-001\", \"products\": { \"Bagel\": 10, \"Donut\": 1, \"Berliner\": 8, \"Muffin\": 5, \"Bread\": 0 }, \"customer_id\": \"customer-001\", \"delivery_date\": { \"day\": 2, \"hour\": 20 } }";
        String order = "";
        String buyerString = "buyer" + buyerID + ":org.right_brothers.agents.BakeryCustomerAgent(" + order + ")";
        return buyerString;
    }
}
