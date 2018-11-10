package org.right_brothers.data.messages;

import jade.util.leap.Serializable;

public class CoordinatorMessage implements Serializable{
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
