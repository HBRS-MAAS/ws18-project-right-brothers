package org.right_brothers.data.messages;

import java.util.Vector;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProofingRequestMessage implements java.io.Serializable {
    private float proofingTime;
    private Vector<Integer> productQuantities;
    private Vector<String> guids;
    private String productType;

    public ProofingRequestMessage() {
        this.productQuantities = new Vector<Integer> ();
        this.guids = new Vector<String> ();
    }

    public float getProofingTime() {
        return proofingTime;
    }

    public void setProofingTime(float proofingTime) {
        this.proofingTime = proofingTime;
    }

    public Vector<Integer> getProductQuantities() {
        return productQuantities;
    }

    public void setProductQuantities(Vector<Integer> productQuantities) {
        this.productQuantities = productQuantities;
    }

    public void setGuids(Vector<String> guids) {
        this.guids = guids;
    }

    public Vector<String> getGuids() {
        return guids;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductType() {
        return productType;
    }

}
