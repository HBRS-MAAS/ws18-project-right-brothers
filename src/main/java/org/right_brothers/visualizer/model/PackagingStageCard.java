package org.right_brothers.visualizer.model;

import java.util.ArrayList;
import java.util.List;

public class PackagingStageCard extends StageCard {
	private List<CardItem> products;
	
	public PackagingStageCard() {
		bakeryId = "";
		products  = new ArrayList<>();
	}

	public List<CardItem> getProducts() {
		return products;
	}

	public void setProducts(List<CardItem> products) {
		this.products = products;
	}
}
