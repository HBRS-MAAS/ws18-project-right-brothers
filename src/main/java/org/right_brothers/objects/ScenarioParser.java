package org.right_brothers;

import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//import java.io.InputStream;
import java.io.File;

/*
 * Parses the json files for different objects and returns a more readable objects
 */
public class ScenarioParser {

    private String client_file = "clients.json";
    private String meta_file = "meta.json";
    private String bakery_file = "bakeries.json";
    private String delivery_file = "delivery.json";
    private String network_file = "street-network.json";
    private String config_path = "/config/sample/";
    private ArrayList customer_list;

    /*
     * constructor: initialises few arraylist objects for further use
     */
    public ScenarioParser () {
        System.out.println("inside ScenarioParser constructor");
        this.customer_list = new ArrayList();
    }

    /*
     * returns the name of all the customers in a particular scenario
     */
    public ArrayList read_customer_file(){

        ArrayList customer_ids = new ArrayList();
        JSONParser parser = new JSONParser();
        JSONObject jsonOrder = null;
        File file = new File(this.getClass().getResource(this.config_path + this.client_file).getPath());
        try {
            Object obj = parser.parse(new FileReader(file));

            JSONArray customers = (JSONArray) obj;
            System.out.println(customers);

            Iterator<JSONObject> i = customers.iterator();
            while (i.hasNext()) {
                JSONObject customer = (JSONObject) i.next();
                this.customer_list.add(customer);
                String guid = (String) customer.get("guid");
                System.out.println(guid);
                customer_ids.add(guid);
            }

            System.out.println(this.customer_list.size());

////            System.out.println(jsonOrder);
//            JSONObject jsonDate = (JSONObject) jsonOrder.get("order_date");
////            System.out.println(jsonDate);
//            this.order_date = new Date((int)(long) jsonDate.get("day"), (int)(long) jsonDate.get("hour"));
//
//            jsonDate = (JSONObject) jsonOrder.get("delivery_date");
////            System.out.println(jsonDate);
//            this.delivery_date = new Date((int)(long) jsonDate.get("day"), (int)(long) jsonDate.get("hour"));
//
//            this.products = (String) jsonOrder.get("products").toString();
////            System.out.println(this.products);
//            this.customer_id = (String) jsonOrder.get("customer_id");
//            this.guid = (String) jsonOrder.get("guid");
////            System.out.println(this.customer_id + " " + this.guid);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return customer_ids;
    }

    // TODO: make functions for reading other files
}

