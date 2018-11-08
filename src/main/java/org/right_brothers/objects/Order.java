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
//        System.out.println("inside init of Order");
//      TODO : initialise orders based on actual orders rather than using dummy strings
        this.guid = "some guid";
        this.customer_id = "some customer id";
        this.order_date = null;
        this.delivery_date= null;
        this.products = null;

    }
    
}
