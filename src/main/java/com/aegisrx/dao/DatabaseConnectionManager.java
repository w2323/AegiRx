package com.aegisrx.dao;

import com.aegisrx.util.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// db operations
public class DatabaseConnectionManager {
	private static DatabaseConnectionManager instance;
	private Connection connection;

	private DatabaseConnectionManager() {
		try {
			Class.forName(AppConfig.getDbDriver());
		} catch (ClassNotFoundException e) {
			System.err.println("[DB] JDBC Driver not found: " + e.getMessage());
		}
	}

	public static synchronized DatabaseConnectionManager getInstance() {
		if (instance == null) {
			instance = new DatabaseConnectionManager();
		}
		return instance;
	}

	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(
				AppConfig.getDbUrl(),
				AppConfig.getDbUser(),
				AppConfig.getDbPassword()
			);
			System.out.println("[DB] Connected to " + AppConfig.getDbUrl());
		}
		return connection;
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				System.out.println("[DB] Connection closed.");
			}
		} catch (SQLException e) {
			System.err.println("[DB] Error closing connection: " + e.getMessage());
		}
	}

public boolean testConnection() {
		try {
			Connection conn = getConnection();
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			System.err.println("[DB] Connection test failed: " + e.getMessage());
			return false;
		}
	}

	public static String getUserFriendlyError(SQLException e) {
		String msg = e.getMessage().toLowerCase();
		if (msg.contains("violation of unique key") || msg.contains("duplicate key")) {
			return "This record already exists. Please use a unique value.";
		}
		if (msg.contains("foreign key constraint") || msg.contains("conflicted with the foreign key")) {
			return "Invalid selection. Please ensure the selected item exists.";
		}
		if (msg.contains("connection refused") || msg.contains("network-related")) {
			return "Database connection failed. Please ensure SQL Server is running.";
		}
		if (msg.contains("string or binary data would be truncated")) {
			return "Input is too long. Please shorten your text.";
		}
		if (msg.contains("login failed")) {
			return "Database login failed. Please check your credentials.";
		}
		return "An unexpected database error occurred.";
	}
}
