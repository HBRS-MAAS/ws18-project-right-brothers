package org.right_brothers;

import java.util.*;
import org.right_brothers.agents.DummyAgent;
import org.right_brothers.agents.BakeryCustomerAgent;
import org.right_brothers.agents.OrderProcessingAgent;

public class Start {
    // list of breads in this world
    private String[] breads_list = {"breadA", "breadB", "breadC", "breadD", "breadW", "breadX", "breadY", "breadZ" };
    // random number generator
    private Random rand = new Random();
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
        // System.out.println(this.rand.nextInt(this.breads_list.length));
        String breads = "";
        for (int i = 0; i < 2; i++) {
            String bread = this.breads_list[this.rand.nextInt(this.breads_list.length)];
            breads += "," + bread;
        }
        breads = breads.substring(1);
        String buyerString = "buyer" + buyerID + ":org.right_brothers.agents.BakeryCustomerAgent(" + breads + ")";
        return buyerString;
    }
}
