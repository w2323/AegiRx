package com.aegisrx.domain;

import com.aegisrx.domain.enums.UserRole;

// simple entity class
public class Manufacturer extends User {
	private String companyName;
	private String licenseNumber;

	public Manufacturer() {
		super();
		setRole(UserRole.MANUFACTURER);
	}

	public Manufacturer(String username, String fullName, String email, String companyName) {
		super(username, fullName, email, UserRole.MANUFACTURER);
		this.companyName = companyName;
	}

	public String getCompanyName() { return companyName; }
	public void setCompanyName(String companyName) { this.companyName = companyName; }

	public String getLicenseNumber() { return licenseNumber; }
	public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}
