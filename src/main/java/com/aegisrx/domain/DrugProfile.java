package com.aegisrx.domain;

// simple entity class
public class DrugProfile {
	private int drugId;
	private String drugName;
	private String category;
	private String description;
	private int manufacturerId;

	public DrugProfile() {}

	public DrugProfile(String drugName, String category, String description, int manufacturerId) {
		this.drugName = drugName;
		this.category = category;
		this.description = description;
		this.manufacturerId = manufacturerId;
	}

	public int getDrugId() { return drugId; }
	public void setDrugId(int drugId) { this.drugId = drugId; }

	public String getDrugName() { return drugName; }
	public void setDrugName(String drugName) { this.drugName = drugName; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public int getManufacturerId() { return manufacturerId; }
	public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }

	@Override
	public String toString() {
		return drugName + " (" + category + ")";
	}
}
