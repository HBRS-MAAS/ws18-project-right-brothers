package org.right_brothers.data.messages;

public class LoadingBayBox {
	private String boxId;
	private String productType;
	private int quantity;
	
	public LoadingBayBox() {}
	
	public LoadingBayBox(String boxId, String productType, int quantity) {
		this.boxId = boxId;
		this.productType = productType;
		this.quantity = quantity;
	}
	
	public String getBoxId() {
		return boxId;
	}
	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}