package org.right_brothers.bakery_objects;

import org.right_brothers.data.models.Step;
import java.util.Vector;

public class BakedProduct implements java.io.Serializable {
    private String guid;
    private int coolingDuration;
    private Vector<Step> intermediateSteps;
    private int quantity;
    private int remainingTimeDuration;
    
	public BakedProduct() {
        this.remainingTimeDuration = -1;
        this.intermediateSteps = new Vector<Step> ();
	}

    public BakedProduct clone() {
        BakedProduct bp = new BakedProduct();
        bp.setGuid(this.getGuid());
        bp.setCoolingDuration(this.getCoolingDuration());
        bp.setQuantity(this.getQuantity());
        bp.setRemainingTimeDuration(this.getRemainingTimeDuration());
        bp.setIntermediateSteps(this.getIntermediateSteps());
        return bp;
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

    public void finishedStep(){
        this.intermediateSteps.remove(0);
    }

    public String toString() {
        String s = "<";
        s += this.guid ;
        s += ", quantity: " + Integer.toString(this.quantity);
        s += ", intermediateSteps: " + Integer.toString(this.intermediateSteps.size());
        s += ", remainingTimeDuration: " + Integer.toString(this.remainingTimeDuration);
        s += ">";
        return s;
    }
}
