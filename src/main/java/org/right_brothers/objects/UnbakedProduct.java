package org.right_brothers.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnbakedProduct implements java.io.Serializable {
    private String guid;
    private int coolingDuration;
    private int bakingDuration;
    private int bakingTemp;
    private int quantity;
    private int breadsPerOven;
    private boolean isBaking;
    private int processStartTime;
    
	public UnbakedProduct() {
        this.isBaking = false;
	}
    public void setGuid(String id) {
        this.guid = id;
    }

    public String getGuid() {
        return this.guid;
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

    public void setIsBaking(boolean isBaking) {
        this.isBaking = isBaking;
    }

    public boolean getIsBaking() {
        return this.isBaking;
    }

    public void setBakingDuration(int bakingDuration) {
        this.bakingDuration = bakingDuration;
    }

    public int getBakingDuration() {
        return bakingDuration;
    }

    public void setBakingTemp(int bakingTemp) {
        this.bakingTemp = bakingTemp;
    }

    public int getBakingTemp() {
        return bakingTemp;
    }

    public void setBreadsPerOven(int breadsPerOven) {
        this.breadsPerOven = breadsPerOven;
    }

    public int getBreadsPerOven() {
        return breadsPerOven;
    }

    public void setProcessStartTime(int processStartTime) {
        this.processStartTime = processStartTime;
    }

    public int getProcessStartTime() {
        return processStartTime;
    }
}
