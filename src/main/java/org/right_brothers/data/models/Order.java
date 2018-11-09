package org.right_brothers.data.models;

import java.util.Hashtable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
	private String customerId;
	private String guid;
	private Date deliveryDate;
	private Hashtable<String,Integer> products;
	
	public Order() {
		this.deliveryDate = new Date();
		this.setProducts(new Hashtable<>());
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public Hashtable<String,Integer> getProducts() {
		return products;
	}

	public void setProducts(Hashtable<String,Integer> products) {
		this.products = products;
	}
}
