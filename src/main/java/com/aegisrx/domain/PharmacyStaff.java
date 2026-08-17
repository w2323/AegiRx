package com.aegisrx.domain;

import com.aegisrx.domain.enums.UserRole;

// data model for this
public class PharmacyStaff extends User {
	private int pharmacyId;

	public PharmacyStaff() {
		super();
		setRole(UserRole.PHARMACY);
	}

	public PharmacyStaff(String username, String fullName, String email, int pharmacyId) {
		super(username, fullName, email, UserRole.PHARMACY);
		this.pharmacyId = pharmacyId;
	}

	public int getPharmacyId() { return pharmacyId; }
	public void setPharmacyId(int pharmacyId) { this.pharmacyId = pharmacyId; }
}
