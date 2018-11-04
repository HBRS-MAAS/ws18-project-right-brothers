package org.right_brothers;

import java.util.*;

public class Start {
    public static void main(String[] args) {
    	List<String> agents = new Vector<>();
    	List<String> cmd = new Vector<>();
    	
    	List<String> arguments = Arrays.asList(args);
    	
    	if(arguments.size() > 0) {
    		String customArgument = String.join(" ", arguments).trim();
    		if(customArgument.toLowerCase().equals("server")) {
    			cmd.add("-agents");
    			agents.add("remote_seller_1:org.right_brothers.agents.OrderProcessingAgent");
    		} else {
    			cmd.addAll(Arrays.asList("-container",customArgument, "-agents"));
    			agents.add(getBuyerAgentInitializationString(1));
    		}
    	} else {
    		cmd.add("-agents");
    		
            agents.add("test:org.right_brothers.agents.DummyAgent");
            agents.add("seller1:org.right_brothers.agents.OrderProcessingAgent");
            
            agents.add(getBuyerAgentInitializationString(1));
    	}       
        
        cmd.add(String.join(";", agents));
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
    private static String getBuyerAgentInitializationString(int buyerID){
        String order = "{\"order_date\": { \"day\": 1, \"hour\": 0 }, \"guid\": \"order-001\", \"products\": { \"Bagel\": 10, \"Donut\": 1, \"Berliner\": 8, \"Muffin\": 5, \"Bread\": 0 }, \"customer_id\": \"customer-001\", \"delivery_date\": { \"day\": 2, \"hour\": 20 } }";
        String buyerString = "buyer" + buyerID + ":org.right_brothers.agents.BakeryCustomerAgent(" + order + ")";
        return buyerString;
    }
}
