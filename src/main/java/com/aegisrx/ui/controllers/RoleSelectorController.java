package com.aegisrx.ui.controllers;

import com.aegisrx.domain.User;
import com.aegisrx.domain.enums.UserRole;
import com.aegisrx.service.UserService;
import com.aegisrx.ui.NavigationManager;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;

import java.sql.SQLException;
import java.util.List;

// controller for the view
public class RoleSelectorController {

	public static Pane createView() {
		VBox root = new VBox(36);
		root.setAlignment(Pos.CENTER);
		root.setPadding(new Insets(50));
		root.getStyleClass().add("main-pane");

		// Brand
		Label brand = new Label("💊 AegisRx");
		brand.setStyle("-fx-font-size: 52px; -fx-font-weight: bold; -fx-text-fill: #00d4aa;");

		Label tagline = new Label("Pharmaceutical Supply Chain & Patient Safety Ecosystem");
		tagline.setStyle("-fx-font-size: 15px; -fx-text-fill: #5a6a80;");

		Label byLine = new Label("by THE SHIELD  •  Turab Qasim  •  Abdullah Tanzeem  •  Wasif Mughal");
		byLine.setStyle("-fx-font-size: 12px; -fx-text-fill: #3a4a5a; -fx-font-style: italic;");

		// Divider
		HBox divLine = new HBox();
		divLine.setMaxWidth(80);
		divLine.setMinHeight(3);
		divLine.setStyle("-fx-background-color: #00d4aa; -fx-background-radius: 2;");

		Label selectLabel = new Label("Select Your Portal");
		selectLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #c0d0e0;");

		// Role cards
		HBox cardsRow = new HBox(22);
		cardsRow.setAlignment(Pos.CENTER);
		cardsRow.getChildren().addAll(
			createRoleCard("🔧", "Admin", "Manage all user accounts\nand system configuration", UserRole.ADMIN),
			createRoleCard("🏭", "Manufacturer", "Register medicine batches\nand initiate transfers", UserRole.MANUFACTURER),
			createRoleCard("🏥", "Pharmacy", "Verify, stock, destroy\nand sell medicines", UserRole.PHARMACY),
			createRoleCard("👤", "Patient", "Medications, diet tracker\nAI assistant & locator", UserRole.PATIENT)
		);

		// DB status
		HBox statusRow = new HBox(16);
		statusRow.setAlignment(Pos.CENTER);

		Label dbStatus;
		try {
			boolean connected = com.aegisrx.dao.DatabaseConnectionManager.getInstance().testConnection();
			dbStatus = new Label(connected ? "●  Database Connected" : "●  Demo Mode (No DB)");
			dbStatus.setStyle("-fx-text-fill: " + (connected ? "#2ed573" : "#f0a030") + "; -fx-font-size: 11px;");
		} catch (Exception e) {
			dbStatus = new Label("●  Demo Mode (No DB)");
			dbStatus.setStyle("-fx-text-fill: #f0a030; -fx-font-size: 11px;");
		}

		Label aiStatus = new Label(com.aegisrx.util.AppConfig.isGeminiEnabled()
			? "●  AI Engine Active" : "●  AI Engine Offline");
		aiStatus.setStyle("-fx-text-fill: " + (com.aegisrx.util.AppConfig.isGeminiEnabled()
			? "#2ed573" : "#5a6a7a") + "; -fx-font-size: 11px;");

		statusRow.getChildren().addAll(dbStatus, new Label("  |  ") {{ setStyle("-fx-text-fill: #2a3a4a;"); }}, aiStatus);

		root.getChildren().addAll(brand, tagline, byLine, divLine, selectLabel, cardsRow, statusRow);
		return root;
	}

	private static VBox createRoleCard(String icon, String title, String description, UserRole role) {
		VBox card = new VBox(14);
		card.setAlignment(Pos.CENTER);
		card.getStyleClass().add("role-card");
		card.setPrefWidth(240);
		card.setPrefHeight(230);

		Label iconLabel = new Label(icon);
		iconLabel.getStyleClass().add("role-icon");

		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("role-title");

		Label descLabel = new Label(description);
		descLabel.getStyleClass().add("role-desc");
		descLabel.setTextAlignment(TextAlignment.CENTER);

		Label hint = new Label("Click to enter →");
		hint.setStyle("-fx-text-fill: #2a3a4a; -fx-font-size: 10px;");

		card.getChildren().addAll(iconLabel, titleLabel, descLabel, hint);

		card.setOnMouseClicked(e -> selectUserForRole(role));
		card.setOnMouseEntered(e -> hint.setStyle("-fx-text-fill: #00d4aa; -fx-font-size: 10px;"));
		card.setOnMouseExited(e -> hint.setStyle("-fx-text-fill: #2a3a4a; -fx-font-size: 10px;"));

		return card;
	}

	private static void selectUserForRole(UserRole role) {
		try {
			UserService userService = new UserService();
			List<User> users = userService.getUsersByRole(role);

			// If there are users for this role, just pick the first one automatically.
			if (!users.isEmpty()) {
				NavigationManager.setCurrentUser(users.get(0));
			} else {
				// No users found — create a temporary demo user so the app doesn't crash.
				User demo = new User("demo_" + role.name().toLowerCase(),
					"Demo " + role.getDisplayName(), "demo@aegisrx.com", role);
				demo.setUserId(role.ordinal() + 100);
				NavigationManager.setCurrentUser(demo);
			}
		} catch (SQLException e) {
			// Database is offline — fall back to demo mode.
			User demo = new User("demo_" + role.name().toLowerCase(),
				"Demo " + role.getDisplayName(), "demo@aegisrx.com", role);
			demo.setUserId(role.ordinal() + 100);
			NavigationManager.setCurrentUser(demo);
		}
		NavigationManager.navigateToDashboard(role);
	}
}
