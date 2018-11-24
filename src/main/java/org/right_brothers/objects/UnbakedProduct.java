package org.right_brothers.objects;

import org.right_brothers.objects.Tray;

public class UnbakedProduct implements java.io.Serializable {
    private String guid;
    private int coolingDuration;
    private int bakingDuration;
    private int bakingTemp;
    private int quantity;
    private int breadsPerOven;
    private Tray scheduled;
    private int processStartTime;
    
	public UnbakedProduct() {
        this.scheduled = null;
	}

    public UnbakedProduct clone() {
        UnbakedProduct up = new UnbakedProduct();
        up.setGuid(this.getGuid());
        up.setCoolingDuration(this.getCoolingDuration());
        up.setBakingDuration(this.getBakingDuration());
        up.setBakingTemp(this.getBakingTemp());
        up.setQuantity(this.getQuantity());
        up.setBreadsPerOven(this.getBreadsPerOven());
        up.setScheduled(this.getScheduled());
        up.setProcessStartTime(this.getProcessStartTime());
        return up;
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

    public void setScheduled(Tray scheduled) {
        this.scheduled = scheduled;
    }

    public Tray getScheduled() {
        return scheduled;
    }

    public boolean isScheduled(){
        return this.scheduled!=null;
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

    public String toString() {
        String s = "<";
        s += this.guid ;
        s += ", quantity: " + Integer.toString(this.quantity);
        s += ", scheduled: " + Boolean.toString(this.isScheduled());
        s += ">";
        return s;
    }
}
