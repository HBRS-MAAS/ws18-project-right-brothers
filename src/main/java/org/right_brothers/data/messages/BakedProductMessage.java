package org.right_brothers.data.messages;

import java.util.Vector;
import org.right_brothers.data.models.Step;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BakedProductMessage implements java.io.Serializable {
    private String guid;
    private int coolingDuration;
    private Vector<Step> intermediateSteps;
    private int quantity;
    
	public BakedProductMessage() {
        this.intermediateSteps = new Vector<Step> ();
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

    public void setIntermediateSteps(Vector<Step> intermediateSteps) {
        this.intermediateSteps = intermediateSteps;
    }

    public Vector<Step> getIntermediateSteps() {
        return intermediateSteps;
    }
}
