package org.right_brothers.objects;

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
    private ArrayList bakery_list;
    private ArrayList delivery_list;

    /*
     * constructor: initialises few arraylist objects for further use
     */
    public ScenarioParser () {
        System.out.println("inside ScenarioParser constructor");
        this.customer_list = new ArrayList();
        this.bakery_list = new ArrayList();
        this.delivery_list = new ArrayList();
    }

    /*
     * returns the name of all the customers in a particular scenario
     */
    public ArrayList read_customer_file(){
        ArrayList customer_ids = new ArrayList();
        JSONParser parser = new JSONParser();
        File file = new File(this.getClass().getResource(this.config_path + this.client_file).getPath());
        try {
            Object obj = parser.parse(new FileReader(file));
            JSONArray customers = (JSONArray) obj;

            Iterator<JSONObject> i = customers.iterator();
            while (i.hasNext()) {
                JSONObject customer = (JSONObject) i.next();
                this.customer_list.add(customer);
                String guid = (String) customer.get("guid");
                customer_ids.add(guid);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return customer_ids;
    }

    /*
     * returns the name of all the bakeries in a particular scenario
     */
    public ArrayList read_bakery_file(){
        ArrayList bakery_ids = new ArrayList();
        JSONParser parser = new JSONParser();
        File file = new File(this.getClass().getResource(this.config_path + this.bakery_file).getPath());
        try {
            Object obj = parser.parse(new FileReader(file));
            JSONArray bakeries = (JSONArray) obj;

            Iterator<JSONObject> i = bakeries.iterator();
            while (i.hasNext()) {
                JSONObject bakery = (JSONObject) i.next();
                this.bakery_list.add(bakery);
                String guid = (String) bakery.get("guid");
                bakery_ids.add(guid);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bakery_ids;
    }

    /*
     * returns the name of all the delivery company in a particular scenario
     */
    public ArrayList read_delivery_file(){
        ArrayList delivery_ids = new ArrayList();
        JSONParser parser = new JSONParser();
        File file = new File(this.getClass().getResource(this.config_path + this.delivery_file).getPath());
        try {
            Object obj = parser.parse(new FileReader(file));
            JSONArray deliveries = (JSONArray) obj;

            Iterator<JSONObject> i = deliveries.iterator();
            while (i.hasNext()) {
                JSONObject delivery = (JSONObject) i.next();
                this.delivery_list.add(delivery);
                String guid = (String) delivery.get("guid");
                delivery_ids.add(guid);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return delivery_ids;
    }

    /*
     * returns a JSONObject containing the information of a customer
     * with given guid
     */
    public JSONObject get_customer_from_guid(String guid){
        for (Object o : this.customer_list) {
            JSONObject customer = (JSONObject) o;
            if (guid.equals(customer.get("guid"))){
                return customer;
            }
        }
        return null;
    }


    // TODO: make function for reading network file
}

