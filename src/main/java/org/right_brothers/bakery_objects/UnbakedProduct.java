package org.right_brothers.bakery_objects;

import org.right_brothers.bakery_objects.Tray;
import org.right_brothers.data.models.Step;
import java.util.Vector;

public class UnbakedProduct implements java.io.Serializable {
    private String guid;
    private int coolingDuration;
    private Vector<Step> intermediateSteps;
    private int bakingDuration;
    private int bakingTemp;
    private int quantity;
    private int breadsPerOven;
    private Tray scheduled;
    private int remainingTimeDuration;
    
	public UnbakedProduct() {
        this.scheduled = null;
        this.intermediateSteps = new Vector<Step> ();
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
        up.setRemainingTimeDuration(this.getRemainingTimeDuration());
        up.setIntermediateSteps(this.getIntermediateSteps());
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

    public void setRemainingTimeDuration(int remainingTimeDuration) {
        this.remainingTimeDuration = remainingTimeDuration;
    }

    public int getRemainingTimeDuration() {
        return remainingTimeDuration;
    }

    public void setIntermediateSteps(Vector<Step> intermediateSteps) {
        this.intermediateSteps = intermediateSteps;
    }

    public Vector<Step> getIntermediateSteps() {
        return intermediateSteps;
    }

    public String toString() {
        String s = "<";
        s += this.guid ;
        s += ", quantity: " + Integer.toString(this.quantity);
        s += ", scheduled: " + Boolean.toString(this.isScheduled());
        s += ", intermediateSteps: " + Integer.toString(this.intermediateSteps.size());
        s += ">";
        return s;
    }
}
