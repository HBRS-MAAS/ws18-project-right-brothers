package org.right_brothers.data.messages;

import jade.util.leap.Serializable;

@SuppressWarnings("serial")
public class ProductMessage implements Serializable {
    private String guid;
    private int coolingRate;
    private int coolingDuration;
    private int bakingDuration;
    private int bakingTemp;
    private int quantity;
    private int breadsPerOven;
    private boolean isBaking;
    private boolean cooled;
    private int processStartTime;
    
	public ProductMessage(String guid) {
        this.guid = guid;
        this.isBaking = false;
        this.cooled = false;
	}
    public void setGuid(String id) {
        this.guid = id;
    }

    public String getGuid() {
        return this.guid;
    }

    public void setCoolingRate(int coolingRate) {
        this.coolingRate = coolingRate;
    }

    public int getCoolingRate() {
        return coolingRate;
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

    public void setCooled(boolean cooled) {
        this.cooled = cooled;
    }

    public boolean getCooled() {
        return cooled;
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
