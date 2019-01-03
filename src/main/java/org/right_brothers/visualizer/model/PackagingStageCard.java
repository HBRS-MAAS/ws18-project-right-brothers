package org.right_brothers.visualizer.model;

import java.util.ArrayList;
import java.util.List;

public class PackagingStageCard extends StageCard {
	private List<CardItem> products;
	
	public PackagingStageCard() {
		super("");
		products  = new ArrayList<>();
	}
	
	public PackagingStageCard(String bakeryId, List<CardItem> products) {
		super(bakeryId);
		this.products  = products != null? products: new ArrayList<>();
	}

	public List<CardItem> getProducts() {
		return products;
	}

	public void setProducts(List<CardItem> products) {
		this.products = products;
	}
	
	public boolean isComplete() {
		for(CardItem item: products) {
			if(item.getQuantity() > 0) {
				return false;
			}
		}
		return true;
	}
}
