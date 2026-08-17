package com.aegisrx.domain;

import java.time.LocalDateTime;

// simple entity class
public class FoodEntry {
	private int logId;
	private int patientUserId;
	private String foodName;
	private String foodCategory;
	private String nutritionalProps;  // JSON: {"sugar": "high", "sodium": "low", ...}
	private LocalDateTime loggedAt;

	public FoodEntry() {
		this.loggedAt = LocalDateTime.now();
	}

	public FoodEntry(int patientUserId, String foodName, String foodCategory, String nutritionalProps) {
		this();
		this.patientUserId = patientUserId;
		this.foodName = foodName;
		this.foodCategory = foodCategory;
		this.nutritionalProps = nutritionalProps;
	}

	public int getLogId() { return logId; }
	public void setLogId(int logId) { this.logId = logId; }

	public int getPatientUserId() { return patientUserId; }
	public void setPatientUserId(int patientUserId) { this.patientUserId = patientUserId; }

	public String getFoodName() { return foodName; }
	public void setFoodName(String foodName) { this.foodName = foodName; }

	public String getFoodCategory() { return foodCategory; }
	public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }

	public String getNutritionalProps() { return nutritionalProps; }
	public void setNutritionalProps(String nutritionalProps) { this.nutritionalProps = nutritionalProps; }

	public LocalDateTime getLoggedAt() { return loggedAt; }
	public void setLoggedAt(LocalDateTime loggedAt) { this.loggedAt = loggedAt; }

	@Override
	public String toString() {
		return foodName + " (" + foodCategory + ") at " + loggedAt;
	}
}
