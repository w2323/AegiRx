package com.aegisrx.domain;

import com.aegisrx.domain.enums.UserRole;
import java.util.ArrayList;
import java.util.List;

// data model for this
public class Patient extends User {
	private String conditions;  // comma-separated: DIABETES, HYPERTENSION, etc.
	private String allergies;
	private List<Medication> activeMedications;
	private int warningFlags;

	public Patient() {
		super();
		setRole(UserRole.PATIENT);
		this.activeMedications = new ArrayList<>();
		this.warningFlags = 0;
	}

	public Patient(String username, String fullName, String email) {
		super(username, fullName, email, UserRole.PATIENT);
		this.activeMedications = new ArrayList<>();
		this.warningFlags = 0;
	}

	public String getConditions() { return conditions; }
	public void setConditions(String conditions) { this.conditions = conditions; }

	public String getAllergies() { return allergies; }
	public void setAllergies(String allergies) { this.allergies = allergies; }

	public List<Medication> getActiveMedications() { return activeMedications; }
	public void setActiveMedications(List<Medication> activeMedications) {
		this.activeMedications = activeMedications;
	}

	public void addMedication(Medication med) {
		this.activeMedications.add(med);
	}

	public int getWarningFlags() { return warningFlags; }
	public void setWarningFlags(int warningFlags) { this.warningFlags = warningFlags; }
	public void incrementWarningFlags() { this.warningFlags++; }
}
