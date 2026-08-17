module com.aegisrx {
	// JavaFX modules - transitive so controllers can access types
	requires transitive javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.graphics;

	// JDBC + MySQL
	requires java.sql;

	// Gson
	requires com.google.gson;

	// HTTP Client (for Gemini API)
	requires java.net.http;

	// Open packages to JavaFX for reflection
	opens com.aegisrx to javafx.graphics;
	opens com.aegisrx.ui to javafx.graphics;
	opens com.aegisrx.ui.controllers to javafx.fxml;
	opens com.aegisrx.domain to com.google.gson, javafx.base;
	opens com.aegisrx.domain.enums to javafx.base;

	// Export all packages
	exports com.aegisrx;
	exports com.aegisrx.ui;
	exports com.aegisrx.ui.controllers;
	exports com.aegisrx.domain;
	exports com.aegisrx.domain.enums;
	exports com.aegisrx.dao;
	exports com.aegisrx.service;
	exports com.aegisrx.ai;
	exports com.aegisrx.observer;
	exports com.aegisrx.util;
}
