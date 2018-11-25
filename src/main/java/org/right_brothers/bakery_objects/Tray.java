package org.right_brothers.bakery_objects;

import org.right_brothers.bakery_objects.UnbakedProduct;
import org.right_brothers.data.models.Oven;

public class Tray {
    private int temp;
    private String guid;
    private int coolingRate;
    private int heatingRate;
    private UnbakedProduct usedFor;

    public Tray(){
        this.usedFor = null;
    }
    public Tray(Oven o, String name){
        this.coolingRate = o.getCoolingRate();
        this.heatingRate = o.getHeatingRate();
        this.guid = o.getGuid() + "-" + name;
        this.temp = 200;
        this.usedFor = null;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public int getTemp() {
        return temp;
    }

    public void setUsedFor(UnbakedProduct usedFor) {
        this.usedFor = usedFor;
    }

    public UnbakedProduct getUsedFor() {
        return usedFor;
    }

    public void setCoolingRate(int coolingRate) {
        this.coolingRate = coolingRate;
    }

    public int getCoolingRate() {
        return coolingRate;
    }

    public void setHeatingRate(int heatingRate) {
        this.heatingRate = heatingRate;
    }

    public int getHeatingRate() {
        return heatingRate;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }

    public boolean isFree() {
        return this.usedFor == null;
    }

    public void setNextTimeStepTemp(){
        if (this.isFree())
            return;
        UnbakedProduct pm = this.usedFor;
        int diff = pm.getBakingTemp() - this.temp;
        int rate;
        if (diff > 0)
            rate = this.heatingRate;
        else if (diff < 0)
            rate = -this.coolingRate;
        else 
            return;
        if (Math.abs(rate) > Math.abs(diff)) {
            this.temp += diff;
        }
        else {
            this.temp += rate;
        }
    }
}
