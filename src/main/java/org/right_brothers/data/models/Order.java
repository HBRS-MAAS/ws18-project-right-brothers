package org.right_brothers.data.models;

import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements java.io.Serializable {
    private String customer_id;
    private String guid;
    private Date delivery_date;
    private Date order_date;
    private Hashtable<String,Integer> products;

    public Order() {
        this.delivery_date = new Date();
        this.order_date = new Date();
        this.setProducts(new Hashtable<>());
    }

    public String getCustomerId() {
        return customer_id;
    }

	@JsonProperty("customer_id")
    public void setCustomerId(String customerId) {
        this.customer_id = customerId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Date getDeliveryDate() {
        return delivery_date;
    }

	@JsonProperty("delivery_date")
    public void setDeliveryDate(Date delivery_date) {
        this.delivery_date = delivery_date;
    }

    public Hashtable<String,Integer> getProducts() {
        return products;
    }

    public void setProducts(Hashtable<String,Integer> products) {
        this.products = products;
    }

	@JsonProperty("order_date")
    public void setOrderDate(Date order_date) {
        this.order_date = order_date;
    }

    public Date getOrderDate() {
        return order_date;
    }
}
