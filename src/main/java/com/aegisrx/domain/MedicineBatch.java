package com.aegisrx.domain;

import com.aegisrx.domain.enums.BatchStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

// simple entity class
public class MedicineBatch {
	private int batchId;
	private String gbi;  // Global Batch Identifier
	private int drugId;
	private String drugName;  // denormalized for display
	private int manufacturerId;
	private LocalDate productionDate;
	private LocalDate expiryDate;
	private int quantity;
	private BatchStatus status;
	private int currentHolderId;
	private LocalDateTime createdAt;

	public MedicineBatch() {
		this.status = BatchStatus.ACTIVE;
		this.createdAt = LocalDateTime.now();
	}

	// ---- State Transition Methods (Information Expert) ----

	public boolean canTransfer() {
		return status == BatchStatus.ACTIVE;
	}

	public boolean canAddToInventory() {
		return status == BatchStatus.IN_TRANSIT;
	}

	public boolean canSell() {
		return status == BatchStatus.IN_STOCK && !isExpired();
	}

	public boolean canDestroy() {
		return status == BatchStatus.IN_STOCK;
	}

	public boolean isExpired() {
		return expiryDate != null && LocalDate.now().isAfter(expiryDate);
	}

	public void markInTransit(int toPharmacyHolderId) {
		if (!canTransfer()) throw new IllegalStateException("Batch cannot be transferred from status: " + status);
		this.status = BatchStatus.IN_TRANSIT;
		this.currentHolderId = toPharmacyHolderId;
	}

	public void markInStock() {
		if (!canAddToInventory()) throw new IllegalStateException("Batch cannot be stocked from status: " + status);
		this.status = BatchStatus.IN_STOCK;
	}

	public void markSold() {
		if (!canSell()) throw new IllegalStateException("Batch cannot be sold from status: " + status);
		this.status = BatchStatus.SOLD;
	}

	public void markDestroyed() {
		if (!canDestroy()) throw new IllegalStateException("Batch cannot be destroyed from status: " + status);
		this.status = BatchStatus.DESTROYED;
	}

	// ---- Getters and Setters ----
	public int getBatchId() { return batchId; }
	public void setBatchId(int batchId) { this.batchId = batchId; }

	public String getGbi() { return gbi; }
	public void setGbi(String gbi) { this.gbi = gbi; }

	public int getDrugId() { return drugId; }
	public void setDrugId(int drugId) { this.drugId = drugId; }

	public String getDrugName() { return drugName; }
	public void setDrugName(String drugName) { this.drugName = drugName; }

	public int getManufacturerId() { return manufacturerId; }
	public void setManufacturerId(int manufacturerId) { this.manufacturerId = manufacturerId; }

	public LocalDate getProductionDate() { return productionDate; }
	public void setProductionDate(LocalDate productionDate) { this.productionDate = productionDate; }

	public LocalDate getExpiryDate() { return expiryDate; }
	public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }

	public BatchStatus getStatus() { return status; }
	public void setStatus(BatchStatus status) { this.status = status; }

	public int getCurrentHolderId() { return currentHolderId; }
	public void setCurrentHolderId(int currentHolderId) { this.currentHolderId = currentHolderId; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	@Override
	public String toString() {
		return "Batch[" + gbi + "] " + drugName + " - " + status;
	}
}
