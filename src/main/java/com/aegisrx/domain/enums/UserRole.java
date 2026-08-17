package com.aegisrx.domain.enums;

// simple entity class
public enum UserRole {
	ADMIN("Admin"),
	MANUFACTURER("Manufacturer"),
	PHARMACY("Pharmacy Staff"),
	PATIENT("Patient");

	private final String displayName;

	UserRole(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
