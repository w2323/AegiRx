package com.aegisrx.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// some common utils
public class AppConfig {
	private static final Properties props = new Properties();
	private static boolean loaded = false;

	static {
		loadConfig();
	}

	private static void loadConfig() {
		try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
			if (is != null) {
				props.load(is);
				loaded = true;
			} else {
				System.err.println("[AppConfig] application.properties not found, using defaults.");
				setDefaults();
			}
		} catch (IOException e) {
			System.err.println("[AppConfig] Error loading config: " + e.getMessage());
			setDefaults();
		}
	}

	private static void setDefaults() {
		props.setProperty("db.url",
				"jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=aegisrx;encrypt=true;trustServerCertificate=true;loginTimeout=2;");
		props.setProperty("db.user", "sa");
		props.setProperty("db.password", "YourStrong!Passw0rd");
		props.setProperty("db.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
		props.setProperty("persistence.type", "database");
		props.setProperty("gemini.api.key", "");
		props.setProperty("gemini.model", "gemini-1.5-flash");
		loaded = true;
	}

	public static String get(String key) {
		return props.getProperty(key, "");
	}

	public static String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public static String getDbUrl() {
		return get("db.url");
	}

	public static String getDbUser() {
		return get("db.user");
	}

	public static String getDbPassword() {
		return get("db.password");
	}

	public static String getDbDriver() {
		return get("db.driver");
	}

	public static String getPersistenceType() {
		return get("persistence.type", "database");
	}

	public static String getGeminiApiKey() {
		return get("gemini.api.key");
	}

	public static String getGeminiModel() {
		return get("gemini.model", "gemini-2.0-flash");
	}

	public static boolean isGeminiEnabled() {
		String key = getGeminiApiKey();
		return key != null && !key.isEmpty();
	}
}
