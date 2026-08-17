package com.aegisrx.ui;

import com.aegisrx.dao.DatabaseConnectionManager;
import com.aegisrx.dao.PersistenceHandlerFactory;
import javafx.application.Application;
import javafx.stage.Stage;

// controller for the view
public class MainApp extends Application {

	private static Stage primaryStage;

	@Override
	public void start(Stage stage) {
		primaryStage = stage;
		stage.setTitle("AegisRx — Pharmaceutical Supply Chain & Patient Safety");
		stage.setWidth(1280);
		stage.setHeight(800);
		stage.setMinWidth(1000);
		stage.setMinHeight(700);

		// Initialize persistence
		PersistenceHandlerFactory.createHandler();

		// Navigate to role selector
		NavigationManager.init(stage);
		NavigationManager.showRoleSelector();

		stage.show();
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	@Override
	public void stop() {
		DatabaseConnectionManager.getInstance().closeConnection();
		System.out.println("[AegisRx] Application shutdown complete.");
	}

	public static void main(String[] args) {
		launch(args);
	}
}
