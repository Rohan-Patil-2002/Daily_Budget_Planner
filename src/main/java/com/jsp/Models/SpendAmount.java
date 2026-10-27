package com.jsp.Models;

public class SpendAmount {
    private int spendId;
    private int planId;
    private int day;
    private double dayAmount;
    private double currentAmount;
    private double dailyBudget;

    // Constructors
    public SpendAmount() {}

    public SpendAmount(int spendId, int planId, int day, double dayAmount, double currentAmount, double dailyBudget) {
        this.spendId = spendId;
        this.planId = planId;
        this.day = day;
        this.dayAmount = dayAmount;
        this.currentAmount = currentAmount;
        this.dailyBudget = dailyBudget;
    }

    // Getters and Setters
    public int getSpendId() {
        return spendId;
    }

    public void setSpendId(int spendId) {
        this.spendId = spendId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getDayAmount() {
		return dayAmount;
	}

	public void setDayAmount(double dayAmount) {
		this.dayAmount = dayAmount;
	}

	public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public double getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(double dailyBudget) {
        this.dailyBudget = dailyBudget;
    }
}

