package com.aegisrx.domain;

// simple entity class
public class Pharmacy {
	private int pharmacyId;
	private String pharmacyName;
	private String city;
	private String region;
	private String contactInfo;
	private int staffUserId;

	public Pharmacy() {}

	public Pharmacy(String pharmacyName, String city, String region, String contactInfo) {
		this.pharmacyName = pharmacyName;
		this.city = city;
		this.region = region;
		this.contactInfo = contactInfo;
	}

	public int getPharmacyId() { return pharmacyId; }
	public void setPharmacyId(int pharmacyId) { this.pharmacyId = pharmacyId; }

	public String getPharmacyName() { return pharmacyName; }
	public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

	public String getCity() { return city; }
	public void setCity(String city) { this.city = city; }

	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }

	public String getContactInfo() { return contactInfo; }
	public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

	public int getStaffUserId() { return staffUserId; }
	public void setStaffUserId(int staffUserId) { this.staffUserId = staffUserId; }

	@Override
	public String toString() {
		return pharmacyName + " - " + city;
	}
}
