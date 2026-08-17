package com.aegisrx.ui;

import com.aegisrx.domain.User;
import com.aegisrx.domain.enums.UserRole;
import com.aegisrx.ui.controllers.*;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

// frontend stuff
public class NavigationManager {

	private static Stage stage;
	private static User currentUser;
	private static Scene scene;

	public static void init(Stage primaryStage) {
		stage = primaryStage;
	}

	public static void showRoleSelector() {
		Pane root = RoleSelectorController.createView();
		setScene(root);
	}

	public static void showAdminDashboard() {
		Pane root = AdminDashboardController.createView();
		setScene(root);
	}

	public static void showManufacturerDashboard() {
		Pane root = ManufacturerDashboardController.createView();
		setScene(root);
	}

	public static void showPharmacyDashboard() {
		Pane root = PharmacyDashboardController.createView();
		setScene(root);
	}

	public static void showPatientDashboard() {
		Pane root = PatientDashboardController.createView();
		setScene(root);
	}

	private static void setScene(Pane root) {
		root.getStyleClass().add("main-pane");
		scene = new Scene(root, stage.getWidth(), stage.getHeight());
		String css = NavigationManager.class.getResource("/css/aegisrx.css").toExternalForm();
		scene.getStylesheets().add(css);
		stage.setScene(scene);
	}

	public static void setCurrentUser(User user) {
		currentUser = user;
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	public static Stage getStage() {
		return stage;
	}

public static void navigateToDashboard(UserRole role) {
		switch (role) {
			case ADMIN:
				showAdminDashboard();
				break;
			case MANUFACTURER:
				showManufacturerDashboard();
				break;
			case PHARMACY:
				showPharmacyDashboard();
				break;
			case PATIENT:
				showPatientDashboard();
				break;
		}
	}
}
