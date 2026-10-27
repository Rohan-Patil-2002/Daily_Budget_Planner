package com.jsp.Models;

public class SpendedByDay {
	private int dayId;
    private int spendId;
    private int day;
    private double spendAmount;
    private String note;


	public SpendedByDay(int dayId, int spendId, int day, double spendAmount, String note) {
		this.dayId = dayId;
		this.spendId = spendId;
		this.day = day;
		this.spendAmount = spendAmount;
		this.note = note;
	}
	public SpendedByDay() {
		// TODO Auto-generated constructor stub
	}
	public int getDayId() {
		return dayId;
	}
	public void setDayId(int dayId) {
		this.dayId = dayId;
	}
	public int getSpendId() {
		return spendId;
	}
	public void setSpendId(int spendId) {
		this.spendId = spendId;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public double getSpendAmount() {
		return spendAmount;
	}
	public void setSpendAmount(double spendAmount) {
		this.spendAmount = spendAmount;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}

}
