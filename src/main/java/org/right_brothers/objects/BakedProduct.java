package org.right_brothers.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BakedProduct implements java.io.Serializable {
    private String guid;
    private int coolingDuration;
    private boolean isCooling;
    private int quantity;

    public BakedProduct(){
        this.isCooling = false;
    }
    
    public void setGuid(String id) {
        this.guid = id;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setIsCooling(boolean isCooling) {
        this.isCooling = isCooling;
    }

    public boolean getIsCooling() {
        return isCooling;
    }

    public void setCoolingDuration(int coolingDuration) {
        this.coolingDuration = coolingDuration;
    }

    public int getCoolingDuration() {
        return coolingDuration;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

}
