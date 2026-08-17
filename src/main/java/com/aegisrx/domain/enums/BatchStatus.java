package com.aegisrx.domain.enums;

// just a plain pojo
public enum BatchStatus {
	ACTIVE("Active"),
	IN_TRANSIT("In Transit"),
	IN_STOCK("In Stock"),
	SOLD("Sold"),
	DESTROYED("Destroyed");

	private final String displayName;

	BatchStatus(String displayName) {
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
