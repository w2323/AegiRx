package com.aegisrx.dao;

// handling data access here
public class DatabaseHandler implements PersistenceHandler {

	@Override
	public boolean initialize() {
		return DatabaseConnectionManager.getInstance().testConnection();
	}

	@Override
	public boolean isAvailable() {
		return DatabaseConnectionManager.getInstance().testConnection();
	}

	@Override
	public String getType() {
		return "database";
	}
}
