package com.aegisrx.domain;

import com.aegisrx.domain.enums.AlertSeverity;
import java.time.LocalDateTime;

// just a plain pojo
public class DietaryAlert {
	private int alertId;
	private int patientUserId;
	private int foodLogId;
	private int conflictingDrugId;
	private String conflictingDrugName;
	private AlertSeverity severity;
	private String reason;
	private boolean acknowledged;
	private LocalDateTime acknowledgedAt;
	private LocalDateTime createdAt;

	public DietaryAlert() {
		this.acknowledged = false;
		this.createdAt = LocalDateTime.now();
	}

	public void acknowledge() {
		this.acknowledged = true;
		this.acknowledgedAt = LocalDateTime.now();
	}

	// Getters and Setters
	public int getAlertId() { return alertId; }
	public void setAlertId(int alertId) { this.alertId = alertId; }

	public int getPatientUserId() { return patientUserId; }
	public void setPatientUserId(int patientUserId) { this.patientUserId = patientUserId; }

	public int getFoodLogId() { return foodLogId; }
	public void setFoodLogId(int foodLogId) { this.foodLogId = foodLogId; }

	public int getConflictingDrugId() { return conflictingDrugId; }
	public void setConflictingDrugId(int conflictingDrugId) { this.conflictingDrugId = conflictingDrugId; }

	public String getConflictingDrugName() { return conflictingDrugName; }
	public void setConflictingDrugName(String conflictingDrugName) { this.conflictingDrugName = conflictingDrugName; }

	public AlertSeverity getSeverity() { return severity; }
	public void setSeverity(AlertSeverity severity) { this.severity = severity; }

	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }

	public boolean isAcknowledged() { return acknowledged; }
	public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }

	public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
	public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
