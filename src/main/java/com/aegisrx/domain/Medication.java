package com.aegisrx.domain;

import java.time.LocalDateTime;

// just a plain pojo
public class Medication {
	private int medId;
	private int patientUserId;
	private int drugId;
	private String drugName;  // denormalized for display
	private String dosage;
	private String frequency;
	private boolean active;
	private LocalDateTime addedAt;

	public Medication() {
		this.active = true;
		this.addedAt = LocalDateTime.now();
	}

	public Medication(int patientUserId, int drugId, String drugName, String dosage, String frequency) {
		this();
		this.patientUserId = patientUserId;
		this.drugId = drugId;
		this.drugName = drugName;
		this.dosage = dosage;
		this.frequency = frequency;
	}

	public int getMedId() { return medId; }
	public void setMedId(int medId) { this.medId = medId; }

	public int getPatientUserId() { return patientUserId; }
	public void setPatientUserId(int patientUserId) { this.patientUserId = patientUserId; }

	public int getDrugId() { return drugId; }
	public void setDrugId(int drugId) { this.drugId = drugId; }

	public String getDrugName() { return drugName; }
	public void setDrugName(String drugName) { this.drugName = drugName; }

	public String getDosage() { return dosage; }
	public void setDosage(String dosage) { this.dosage = dosage; }

	public String getFrequency() { return frequency; }
	public void setFrequency(String frequency) { this.frequency = frequency; }

	public boolean isActive() { return active; }
	public void setActive(boolean active) { this.active = active; }

	public LocalDateTime getAddedAt() { return addedAt; }
	public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

	@Override
	public String toString() {
		return drugName + " " + dosage + " (" + frequency + ")";
	}
}
