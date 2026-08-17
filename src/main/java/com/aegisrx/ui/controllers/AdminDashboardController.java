package com.aegisrx.ui.controllers;

import com.aegisrx.domain.User;
import com.aegisrx.domain.enums.UserRole;
import com.aegisrx.service.UserService;
import com.aegisrx.ui.NavigationManager;
import com.aegisrx.ui.UIHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;

// frontend stuff
public class AdminDashboardController {

	private static final UserService userService = new UserService();

	public static Pane createView() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("main-pane");

		BorderPane[] rootRef = { root };
		VBox sidebar = UIHelper.sidebar("Admin Portal",
			new String[]{ "👥  User Management", "🏥  Pharmacy Management", "📊  System Overview" },
			new Runnable[]{ 
				() -> rootRef[0].setCenter(createUserMgmtView()), 
				() -> rootRef[0].setCenter(createPharmacyMgmtView()),
				() -> rootRef[0].setCenter(createOverviewView()) 
			},
			() -> NavigationManager.showRoleSelector()
		);
		root.setLeft(sidebar);
		root.setCenter(UIHelper.scrollableContent(createUserMgmtContent()));
		
		// --- GLOBAL STICKY HEADER ---
		HBox header = new HBox(15);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(10, 20, 10, 20));
		header.setStyle("-fx-background-color: #0f1822; -fx-border-color: #1a2838; -fx-border-width: 0 0 1 0;");
		
		Button backBtn = new Button("❮");
		backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #c0d0e0; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
		backBtn.setOnAction(e -> NavigationManager.showRoleSelector());
		
		ImageView logoView = new ImageView();
		try {
			logoView.setImage(new Image(AdminDashboardController.class.getResource("/images/logo.jpg").toExternalForm()));
			logoView.setFitHeight(32);
			logoView.setPreserveRatio(true);
		} catch (Exception ignored) {}
		
		Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
		Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
		
		ComboBox<User> cbActiveUser = new ComboBox<>();
		try {
			java.util.List<User> users = new com.aegisrx.service.UserService().getUsersByRole(com.aegisrx.domain.enums.UserRole.ADMIN);
			cbActiveUser.setItems(FXCollections.observableArrayList(users));
			for (User u : users) {
				if (u.getUserId() == NavigationManager.getCurrentUser().getUserId()) {
					cbActiveUser.setValue(u);
					break;
				}
			}
		} catch(Exception ignored) {}
		
		cbActiveUser.setCellFactory(lv -> new ListCell<User>() {
			@Override
			protected void updateItem(User item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) { setText(null); }
				else {
					// Admin dashboard only has Admin users, but keep it robust
					setText(item.getFullName() + " (" + item.getRole().getDisplayName() + ")");
				}
			}
		});
		cbActiveUser.setButtonCell(new ListCell<User>() {
			@Override
			protected void updateItem(User item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) { setText(null); }
				else { setText(item.getFullName()); }
			}
		});

		cbActiveUser.setOnAction(e -> {
			User selected = cbActiveUser.getValue();
			if (selected != null && selected.getUserId() != NavigationManager.getCurrentUser().getUserId()) {
				NavigationManager.setCurrentUser(selected);
				javafx.application.Platform.runLater(NavigationManager::showAdminDashboard);
			}
		});

		Label lblActive = new Label("Active Admin Profile:");
		lblActive.setStyle("-fx-text-fill: #8a9aaa; -fx-font-weight: bold;");
		header.getChildren().addAll(backBtn, logoView, spacer1, lblActive, cbActiveUser, spacer2);
		
		root.setTop(header);

		return root;
	}

	private static ScrollPane createUserMgmtView() {
		return UIHelper.scrollableContent(createUserMgmtContent());
	}

	private static VBox createUserMgmtContent() {
		VBox content = new VBox(22);

		// Header
		content.getChildren().add(UIHelper.pageHeader(
			"User Management", "UC12 — Create, view, and manage all system users", "ADMIN"));

		// ---- Create User Card ----
		TextField tfUsername = UIHelper.textField("Enter username");
		TextField tfFullName = UIHelper.textField("Enter full name");
		TextField tfEmail = UIHelper.textField("Enter email address");
		PasswordField pfPassword = UIHelper.passwordField("Set password");

		ComboBox<UserRole> cbRole = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
		cbRole.setPromptText("Choose a role");
		cbRole.setPrefWidth(320);

		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formRow(
				UIHelper.formField("USERNAME", "e.g. john_doe, admin_01", tfUsername),
				UIHelper.formField("FULL NAME", "e.g. Dr. Ahmed Khan", tfFullName)
			),
			UIHelper.formRow(
				UIHelper.formField("EMAIL ADDRESS", "e.g. ahmed.khan@pharmax.com", tfEmail),
				UIHelper.formField("ROLE", "e.g. Manufacturer, Pharmacy Staff, Patient", cbRole)
			),
			UIHelper.formField("INITIAL PASSWORD", "e.g. P@ssw0rd123 (min 6 characters)", pfPassword)
		);

		// Result area
		VBox resultArea = new VBox();

		// ---- Users Table ----
		TableView<User> table = buildUsersTable();

		Button btnCreate = new Button("✚  Create User Account");
		btnCreate.getStyleClass().add("btn-primary");
		btnCreate.setOnAction(e -> {
			resultArea.getChildren().clear();
			try {
				if (tfUsername.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Username is required")); return; }
				if (tfFullName.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Full name is required")); return; }
				if (cbRole.getValue() == null) { resultArea.getChildren().add(UIHelper.errorBanner("Please select a role")); return; }
				if (pfPassword.getText().length() < 3) { resultArea.getChildren().add(UIHelper.errorBanner("Password must be at least 3 characters")); return; }

				User user = userService.createUser(
					tfUsername.getText().trim(), tfFullName.getText().trim(),
					tfEmail.getText().trim(), cbRole.getValue(), pfPassword.getText()
				);
				
				// Refresh the table visually to reflect the DB change instantly
				try {
					table.setItems(FXCollections.observableArrayList(userService.getAllUsers()));
				} catch (SQLException ex) { }

				resultArea.getChildren().add(UIHelper.successBanner(
					"User created successfully!  Name: " + user.getFullName() + "  |  ID: " + user.getUserId() + "  |  Role: " + user.getRole().getDisplayName()
				));
				tfUsername.clear(); tfFullName.clear(); tfEmail.clear(); pfPassword.clear(); cbRole.setValue(null);
			} catch (Exception ex) {
				resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage()));
			}
		});

		VBox createCard = UIHelper.card("➕  Create New User", "Fill in the details below to register a new system user",
			formGrid,
			UIHelper.infoStrip("All fields marked above are required. Duplicate usernames will be rejected."),
			new HBox(12, btnCreate),
			resultArea
		);

		// ---- Users Table is now declared above ----
		VBox.setVgrow(table, Priority.ALWAYS);
		table.setMinHeight(250);

		VBox tableCard = UIHelper.card("📋  All Registered Users", "View and manage existing user accounts", table);

		content.getChildren().addAll(createCard, tableCard);
		return content;
	}

	// ---- NEW: Pharmacy Management View ----
	private static ScrollPane createPharmacyMgmtView() {
		VBox content = new VBox(22);
		content.getChildren().add(UIHelper.pageHeader("Pharmacy Management", "UC13 — Register facilities and link them to staff", "ADMIN"));

		TextField tfName = UIHelper.textField("Pharmacy Name");
		TextField tfCity = UIHelper.textField("City");
		TextField tfRegion = UIHelper.textField("Region");
		TextField tfContact = UIHelper.textField("Contact Info");
		
		ComboBox<User> cbStaff = new ComboBox<>();
		cbStaff.setPromptText("Select Staff User");
		cbStaff.setPrefWidth(320);
		try {
			cbStaff.setItems(FXCollections.observableArrayList(userService.getUsersByRole(UserRole.PHARMACY)));
		} catch (SQLException ignored) {}

		TableView<com.aegisrx.domain.Pharmacy> table = new TableView<>();
		TableColumn<com.aegisrx.domain.Pharmacy, String> colId = new TableColumn<>("ID");
		colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getPharmacyId())));
		TableColumn<com.aegisrx.domain.Pharmacy, String> colName = new TableColumn<>("Pharmacy Name");
		colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPharmacyName()));
		TableColumn<com.aegisrx.domain.Pharmacy, String> colLoc = new TableColumn<>("Location");
		colLoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCity() + ", " + d.getValue().getRegion()));
		table.getColumns().addAll(colId, colName, colLoc);
		table.setPrefHeight(250);

		try { table.setItems(FXCollections.observableArrayList(new com.aegisrx.dao.PharmacyDAO().findAll())); } catch (SQLException ignored) {}

		VBox resultArea = new VBox();
		Button btnCreate = new Button("✚  Register Pharmacy");
		btnCreate.getStyleClass().add("btn-primary");
		// This button triggers the logic to save a new pharmacy record to the database
		btnCreate.setOnAction(e -> {
			resultArea.getChildren().clear();
			try {
				// Only the pharmacy name and city are mandatory - staff can be linked later.
				if (tfName.getText().isEmpty() || tfCity.getText().isEmpty()) {
					resultArea.getChildren().add(UIHelper.errorBanner("Pharmacy Name and City are required."));
					return;
				}

				// I'll create a new Pharmacy object with the data from the form.
				com.aegisrx.domain.Pharmacy p = new com.aegisrx.domain.Pharmacy(
					tfName.getText().trim(), 
					tfCity.getText().trim(), 
					tfRegion.getText().trim(), 
					tfContact.getText().trim()
				);
				
				// Link to a staff user only if one was actually selected.
				// If left blank, the DAO will store NULL in the database (which is fine).
				if (cbStaff.getValue() != null) {
					p.setStaffUserId(cbStaff.getValue().getUserId());
				}

				// Now I call the DAO to save it in SQL Server.
				new com.aegisrx.dao.PharmacyDAO().insert(p);
				
				// If we reach here, it worked! I refresh the table to show the new pharmacy.
				table.setItems(FXCollections.observableArrayList(new com.aegisrx.dao.PharmacyDAO().findAll()));
				
				String staffNote = (cbStaff.getValue() != null)
					? " linked to " + cbStaff.getValue().getFullName()
					: " (no staff linked yet — assign later)";
				resultArea.getChildren().add(UIHelper.successBanner("Pharmacy '" + p.getPharmacyName() + "' registered" + staffNote + "!"));
				
				// Clear the form fields so the admin can add another one easily.
				tfName.clear(); tfCity.clear(); tfRegion.clear(); tfContact.clear(); cbStaff.setValue(null);
			} catch (SQLException ex) {
				// I use our custom error helper to show a message that actually makes sense.
				resultArea.getChildren().add(UIHelper.errorBanner(com.aegisrx.dao.DatabaseConnectionManager.getUserFriendlyError(ex)));
			} catch (Exception ex) {
				resultArea.getChildren().add(UIHelper.errorBanner("Error: " + ex.getMessage()));
			}
		});

		VBox form = UIHelper.card("➕  Register New Pharmacy", "Link a physical facility to a registered pharmacy staff account",
			new VBox(14, 
				UIHelper.formRow(UIHelper.formField("PHARMACY NAME", "e.g. CityCare Pharmacy", tfName), UIHelper.formField("CITY", "e.g. Islamabad", tfCity)),
				UIHelper.formRow(UIHelper.formField("REGION", "e.g. Punjab", tfRegion), UIHelper.formField("CONTACT INFO", "e.g. +92-51-...", tfContact)),
				UIHelper.formField("LINKED STAFF USER", "Only users with 'Pharmacy Staff' role shown here", cbStaff)
			),
			btnCreate, resultArea
		);

		content.getChildren().addAll(form, UIHelper.card("📋  Registered Pharmacies", "Current facilities in the AegisRx network", table));
		return UIHelper.scrollableContent(content);
	}

	private static ScrollPane createOverviewView() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader("System Overview", "Dashboard statistics and system health"));

		int totalUsers = 0, mfgCount = 0, pharmCount = 0, patientCount = 0;
		try {
			totalUsers = userService.getAllUsers().size();
			mfgCount = userService.getUsersByRole(UserRole.MANUFACTURER).size();
			pharmCount = userService.getUsersByRole(UserRole.PHARMACY).size();
			patientCount = userService.getUsersByRole(UserRole.PATIENT).size();
		} catch (SQLException ignored) {}

		HBox stats = new HBox(16);
		stats.getChildren().addAll(
			UIHelper.statCard("👥", String.valueOf(totalUsers), "Total Users", "#00d4aa"),
			UIHelper.statCard("🏭", String.valueOf(mfgCount), "Manufacturers", "#3090f0"),
			UIHelper.statCard("🏥", String.valueOf(pharmCount), "Pharmacies", "#f0a030"),
			UIHelper.statCard("👤", String.valueOf(patientCount), "Patients", "#a060e0")
		);

		content.getChildren().add(stats);
		return UIHelper.scrollableContent(content);
	}

	@SuppressWarnings("unchecked")
	private static TableView<User> buildUsersTable() {
		TableView<User> table = new TableView<>();

		TableColumn<User, String> colId = new TableColumn<>("ID");
		colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getUserId())));
		colId.setPrefWidth(55);

		TableColumn<User, String> colUsername = new TableColumn<>("Username");
		colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
		colUsername.setPrefWidth(130);

		TableColumn<User, String> colName = new TableColumn<>("Full Name");
		colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
		colName.setPrefWidth(180);

		TableColumn<User, String> colEmail = new TableColumn<>("Email");
		colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail() != null ? d.getValue().getEmail() : "—"));
		colEmail.setPrefWidth(200);

		TableColumn<User, String> colRole = new TableColumn<>("Role");
		colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole().getDisplayName()));
		colRole.setPrefWidth(120);

		TableColumn<User, String> colStatus = new TableColumn<>("Status");
		colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
		colStatus.setPrefWidth(90);

		table.getColumns().addAll(colId, colUsername, colName, colEmail, colRole, colStatus);
		table.setPlaceholder(new Label("No users found. Create one above to get started."));

		try {
			table.setItems(FXCollections.observableArrayList(userService.getAllUsers()));
		} catch (SQLException e) {
			table.setPlaceholder(new Label("⚠ Database not connected — Users will appear here when DB is available"));
		}

		return table;
	}
}
