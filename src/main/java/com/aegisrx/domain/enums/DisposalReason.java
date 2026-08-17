package com.aegisrx.domain.enums;

// just a plain pojo
public enum DisposalReason {
	EXPIRED("Expired"),
	DAMAGED("Damaged"),
	RECALLED("Recalled");

	private final String displayName;

	DisposalReason(String displayName) {
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
