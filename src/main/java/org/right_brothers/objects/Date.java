package org.right_brothers.objects;

public class Date implements java.io.Serializable {

    public int day;
    public int hour;
    public Date(int day, int hour) {
        this.day = day;
        this.hour = hour;
    }
}

