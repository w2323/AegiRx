package com.aegisrx.domain.enums;

// data model for this
public enum AlertSeverity {
	LOW("Low"),
	MEDIUM("Medium"),
	HIGH("High"),
	CRITICAL("Critical");

	private final String displayName;

	AlertSeverity(String displayName) {
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
