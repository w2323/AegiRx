package com.aegisrx.dao;

// dao implementation
public interface PersistenceHandler {
	boolean initialize();
	boolean isAvailable();
	String getType();
}
