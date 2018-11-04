package org.right_brothers.objects;

public class Date implements java.io.Serializable {

    int day;
    int hour;
    public Date(int day, int hour) {
        this.day = day;
        this.hour = hour;
    }

    public int getDay(){
        return this.day;
    }
    public int getHour(){
        return this.hour;
    }

}

