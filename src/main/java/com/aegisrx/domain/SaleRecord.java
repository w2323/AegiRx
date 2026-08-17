package com.aegisrx.domain;

import java.time.LocalDateTime;

// data model for this
public class SaleRecord {
	private int saleId;
	private int pharmacyId;
	private int patientUserId;
	private int batchId;
	private String batchGbi;
	private String drugName;
	private LocalDateTime saleDate;
	private String status;

	public SaleRecord() {
		this.saleDate = LocalDateTime.now();
		this.status = "COMPLETED";
	}

	public int getSaleId() { return saleId; }
	public void setSaleId(int saleId) { this.saleId = saleId; }

	public int getPharmacyId() { return pharmacyId; }
	public void setPharmacyId(int pharmacyId) { this.pharmacyId = pharmacyId; }

	public int getPatientUserId() { return patientUserId; }
	public void setPatientUserId(int patientUserId) { this.patientUserId = patientUserId; }

	public int getBatchId() { return batchId; }
	public void setBatchId(int batchId) { this.batchId = batchId; }

	public String getBatchGbi() { return batchGbi; }
	public void setBatchGbi(String batchGbi) { this.batchGbi = batchGbi; }

	public String getDrugName() { return drugName; }
	public void setDrugName(String drugName) { this.drugName = drugName; }

	public LocalDateTime getSaleDate() { return saleDate; }
	public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
