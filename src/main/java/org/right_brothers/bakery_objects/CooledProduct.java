package org.right_brothers.bakery_objects;

import org.right_brothers.data.messages.CompletedProductMessage;
import org.right_brothers.data.models.Step;
import java.util.Vector;

public class CooledProduct implements java.io.Serializable {
    private String guid;
    private Vector<Step> intermediateSteps;
    private int quantity;
    private int remainingTimeDuration;
    
	public CooledProduct() {
        this.remainingTimeDuration = -1;
        this.intermediateSteps = new Vector<Step> ();
	}

    public CompletedProductMessage getCompletedProductMessage() {
        CompletedProductMessage completedProductMessage = new CompletedProductMessage();
        completedProductMessage.setGuid(this.guid);
        completedProductMessage.setQuantity(this.quantity);
        return completedProductMessage;
    }

    public void setGuid(String id) {
        this.guid = id;
    }

    public String getGuid() {
        return this.guid;
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
