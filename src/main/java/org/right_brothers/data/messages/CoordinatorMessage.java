package org.right_brothers.data.messages;

import jade.util.leap.Serializable;

@SuppressWarnings("serial")
public abstract class CoordinatorMessage implements Serializable{
    private String id;

    public CoordinatorMessage(String id) {
    	this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
