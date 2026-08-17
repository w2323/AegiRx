package com.aegisrx.domain;

import com.aegisrx.domain.enums.AlertSeverity;

// just a plain pojo
public class InteractionReport {
	private int interactionId;
	private int drugId1;
	private int drugId2;
	private String drugName1;
	private String drugName2;
	private AlertSeverity severity;
	private String description;
	private String recommendation;

	public InteractionReport() {}

	public InteractionReport(String drugName1, String drugName2, AlertSeverity severity,
							 String description, String recommendation) {
		this.drugName1 = drugName1;
		this.drugName2 = drugName2;
		this.severity = severity;
		this.description = description;
		this.recommendation = recommendation;
	}

	public int getInteractionId() { return interactionId; }
	public void setInteractionId(int interactionId) { this.interactionId = interactionId; }

	public int getDrugId1() { return drugId1; }
	public void setDrugId1(int drugId1) { this.drugId1 = drugId1; }

	public int getDrugId2() { return drugId2; }
	public void setDrugId2(int drugId2) { this.drugId2 = drugId2; }

	public String getDrugName1() { return drugName1; }
	public void setDrugName1(String drugName1) { this.drugName1 = drugName1; }

	public String getDrugName2() { return drugName2; }
	public void setDrugName2(String drugName2) { this.drugName2 = drugName2; }

	public AlertSeverity getSeverity() { return severity; }
	public void setSeverity(AlertSeverity severity) { this.severity = severity; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getRecommendation() { return recommendation; }
	public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

	@Override
	public String toString() {
		return "[" + severity + "] " + drugName1 + " ↔ " + drugName2 + ": " + description;
	}
}
