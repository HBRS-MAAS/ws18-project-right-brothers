package org.right_brothers.objects;

import org.right_brothers.objects.Date;

import java.util.*;


public class Order implements java.io.Serializable {

    private Date order_date;
    private Date delivery_date;
    private String products;
    private String customer_id;
    private String guid;

    public Order(String orderGuid){
//        System.out.println("inside init of Order");
//      TODO : initialise orders based on actual orders rather than using dummy strings
        this.guid = "some guid";
        this.customer_id = "some customer id";
        this.order_date = null;
        this.delivery_date= null;
        this.products = null;

    }
    public void setOrderDate (Date od){
        this.order_date = od;
    }
    public void setDeliveryDate (Date dd) {
        this.delivery_date = dd;
    }
    public void setCustomerId (String cid) {
        this.customer_id = cid;
    }
    public void setProducts (String prdts) {
        this.products = prdts;
    }
    public Date getOrderDate () {
        return this.order_date;
    }
    public Date getDeliveryDate () {
        return this.delivery_date;
    }
    public String getOrderGuid () {
        return this.guid;
    }
    public String getCustomerId () {
        return this.customer_id;
    }
    public String getProducts () {
        return this.products;
    }
    
}
