package org.right_brothers.data.messages;

import jade.util.leap.Serializable;

public class TimeStep implements Serializable {
	private int day;
	private int hour;
	
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public int getHour() {
		return hour;
	}
	public void setHour(int hour) {
		this.hour = hour;
	}
}
