package org.right_brothers.objects;

import org.right_brothers.objects.Date;

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

public class Order implements java.io.Serializable {

    public Date order_date;
    public Date delivery_date;
    public String products;
    public String customer_id;
    public String guid;

    public Order(String orderGuid){
        JSONParser parser = new JSONParser();
        JSONObject jsonOrder = null;
        File file = new File(this.getClass().getResource("/config/simple/random-scenario.json").getPath());
        try {
            Object obj = parser.parse(new FileReader(file));

            JSONObject jsonObject = (JSONObject) obj;

            JSONArray orders = (JSONArray) jsonObject.get("orders");
            Iterator<JSONObject> i= orders.iterator();
            while (i.hasNext()) {
                JSONObject something = (JSONObject) i.next();
                String guid = (String) something.get("guid");
                if (guid.equals(orderGuid)) {
                    jsonOrder = something;
                    break;
                }
            }
//            System.out.println(jsonOrder);
            JSONObject jsonDate = (JSONObject) jsonOrder.get("order_date");
//            System.out.println(jsonDate);
            this.order_date = new Date((int)(long) jsonDate.get("day"), (int)(long) jsonDate.get("hour"));

            jsonDate = (JSONObject) jsonOrder.get("delivery_date");
//            System.out.println(jsonDate);
            this.delivery_date = new Date((int)(long) jsonDate.get("day"), (int)(long) jsonDate.get("hour"));

            this.products = (String) jsonOrder.get("products").toString();
//            System.out.println(this.products);
            this.customer_id = (String) jsonOrder.get("customer_id");
            this.guid = (String) jsonOrder.get("guid");
//            System.out.println(this.customer_id + " " + this.guid);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    
}
