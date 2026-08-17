package com.aegisrx.ui.controllers;

import com.aegisrx.domain.DrugProfile;
import com.aegisrx.domain.MedicineBatch;
import com.aegisrx.domain.Pharmacy;
import com.aegisrx.service.BatchService;
import com.aegisrx.dao.PharmacyDAO;
import com.aegisrx.ui.NavigationManager;
import com.aegisrx.ui.UIHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.aegisrx.domain.User;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.time.LocalDate;

// handles the ui logic
public class ManufacturerDashboardController {

	private static final BatchService batchService = new BatchService();

	public static Pane createView() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("main-pane");

		BorderPane[] ref = { root };
		VBox sidebar = UIHelper.sidebar("Manufacturer Portal",
			new String[]{ "📦  Register Batch", "🚚  Transfer Batch", "📋  My Batches" },
			new Runnable[]{
				() -> ref[0].setCenter(UIHelper.scrollableContent(createRegisterContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createTransferContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createHistoryContent()))
			},
			() -> NavigationManager.showRoleSelector()
		);
		root.setLeft(sidebar);
		root.setCenter(UIHelper.scrollableContent(createRegisterContent()));
		
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
			logoView.setImage(new Image(ManufacturerDashboardController.class.getResource("/images/logo.jpg").toExternalForm()));
			logoView.setFitHeight(32);
			logoView.setPreserveRatio(true);
		} catch (Exception ignored) {}
		
		Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
		Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
		
		ComboBox<User> cbActiveUser = new ComboBox<>();
		try {
			java.util.List<User> users = new com.aegisrx.service.UserService().getUsersByRole(com.aegisrx.domain.enums.UserRole.MANUFACTURER);
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
					if (item.getRole() == com.aegisrx.domain.enums.UserRole.PHARMACY) {
						try {
							Pharmacy p = new PharmacyDAO().findByStaffUserId(item.getUserId());
							setText(p != null ? p.getPharmacyName() + " - " + p.getCity() + " (" + item.getFullName() + ")" : item.getFullName());
						} catch (SQLException e) { setText(item.getFullName()); }
					} else {
						// Manufacturers already have company name in full_name in seed data
						setText(item.getFullName());
					}
				}
			}
		});
		cbActiveUser.setButtonCell(new ListCell<User>() {
			@Override
			protected void updateItem(User item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) { setText(null); }
				else {
					if (item.getRole() == com.aegisrx.domain.enums.UserRole.PHARMACY) {
						try {
							Pharmacy p = new PharmacyDAO().findByStaffUserId(item.getUserId());
							setText(p != null ? p.getPharmacyName() + " - " + p.getCity() : item.getFullName());
						} catch (SQLException e) { setText(item.getFullName()); }
					} else {
						// Show the full name which includes (Company) for Manufacturers
						setText(item.getFullName());
					}
				}
			}
		});

		cbActiveUser.setOnAction(e -> {
			User selected = cbActiveUser.getValue();
			if (selected != null && selected.getUserId() != NavigationManager.getCurrentUser().getUserId()) {
				NavigationManager.setCurrentUser(selected);
				javafx.application.Platform.runLater(NavigationManager::showManufacturerDashboard);
			}
		});

		Label lblActive = new Label("Active Manufacturer Profile:");
		lblActive.setStyle("-fx-text-fill: #8a9aaa; -fx-font-weight: bold;");
		header.getChildren().addAll(backBtn, logoView, spacer1, lblActive, cbActiveUser, spacer2);
		
		root.setTop(header);
		
		return root;
	}

	// ---- UC1: Register Medicine Batch ----
	private static VBox createRegisterContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Register Medicine Batch", "UC1 — Create a new batch and receive a unique Global Batch Identifier (GBI)", "MANUFACTURER"));

		// Form
		ComboBox<DrugProfile> cbDrug = new ComboBox<>();
		cbDrug.setPromptText("Select from dropdown");
		cbDrug.setPrefWidth(340);
		try {
			cbDrug.setItems(FXCollections.observableArrayList(batchService.getDrugProfilesForManufacturer(
				NavigationManager.getCurrentUser().getUserId())));
			if (cbDrug.getItems().isEmpty()) {
				cbDrug.setItems(FXCollections.observableArrayList(batchService.getAllDrugProfiles()));
			}
		} catch (SQLException ignored) {}

		DatePicker dpProd = new DatePicker(LocalDate.now());
		dpProd.setPrefWidth(340);
		DatePicker dpExp = new DatePicker(LocalDate.now().plusYears(2));
		dpExp.setPrefWidth(340);

		TextField tfQty = UIHelper.textField("Enter quantity");

		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formField("MEDICATION", "e.g. Warfarin, Metformin, Aspirin — select from verified database", cbDrug),
			UIHelper.formRow(
				UIHelper.formField("PRODUCTION DATE", "e.g. 2026-04-19 — when the batch was manufactured", dpProd),
				UIHelper.formField("EXPIRY DATE", "e.g. 2028-04-19 — must be after production date", dpExp)
			),
			UIHelper.formField("QUANTITY PRODUCED", "e.g. 5000 — total units in this batch", tfQty)
		);

		VBox resultArea = new VBox(8);

		Button btnRegister = new Button("📦  Register Batch");
		btnRegister.getStyleClass().add("btn-primary");
		btnRegister.setOnAction(e -> {
			resultArea.getChildren().clear();
			try {
				DrugProfile drug = cbDrug.getValue();
				if (drug == null) { resultArea.getChildren().add(UIHelper.errorBanner("Please select a medication from the dropdown")); return; }
				if (tfQty.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Please enter quantity produced")); return; }

				int qty = Integer.parseInt(tfQty.getText().trim());
				MedicineBatch batch = batchService.registerBatch(
					drug.getDrugId(), NavigationManager.getCurrentUser().getUserId(),
					dpProd.getValue(), dpExp.getValue(), qty
				);

				VBox successBox = new VBox(8);
				successBox.getStyleClass().addAll("result-banner", "result-success");
				
				Button btnCopy = new Button("📋  Copy GBI Code");
				btnCopy.getStyleClass().add("btn-ghost");
				btnCopy.setOnAction(ev -> {
					UIHelper.copyToClipboard(batch.getGbi());
					btnCopy.setText("✅  GBI Copied!");
				});

				successBox.getChildren().addAll(
					new Label("✅  Batch Registered Successfully!") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #2ed573; -fx-font-size: 15px;"); }},
					new HBox(15, new Label("GBI:  " + batch.getGbi()) {{ setStyle("-fx-text-fill: #2ed573; -fx-font-size: 13px;"); }}, btnCopy),
					new Label("Drug: " + drug.getDrugName() + "  |  Qty: " + qty + "  |  Status: Active") {{ setStyle("-fx-text-fill: #4a8a6a;"); }}
				);
				resultArea.getChildren().add(successBox);
				tfQty.clear();
			} catch (NumberFormatException ex) {
				resultArea.getChildren().add(UIHelper.errorBanner("Quantity must be a number (e.g. 5000)"));
			} catch (Exception ex) {
				resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage()));
			}
		});

		VBox card = UIHelper.card("📦  New Batch Registration", "Enter the production details for the new medicine batch",
			formGrid,
			UIHelper.infoStrip("A unique Global Batch Identifier (GBI) will be auto-generated upon registration."),
			new HBox(12, btnRegister),
			resultArea
		);

		content.getChildren().add(card);
		return content;
	}

	// ---- UC2: Transfer Batch ----
	// This part of the code handles moving medicine from the factory to a pharmacy
	private static VBox createTransferContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Transfer Medicine Batch", "UC2 — Initiate transfer of a batch to a verified pharmacy", "LOGISTICS"));

		// Dropdown to choose which batch to ship
		ComboBox<MedicineBatch> cbBatch = new ComboBox<>();
		cbBatch.setPromptText("Select batch to transfer");
		cbBatch.setPrefWidth(380);
		// We only show "Active" batches that the current manufacturer owns
		try { cbBatch.setItems(FXCollections.observableArrayList(
			batchService.getActiveBatches(NavigationManager.getCurrentUser().getUserId()))); } catch (SQLException ignored) {}

		// Dropdown to choose which pharmacy will receive the medicine
		ComboBox<Pharmacy> cbPharmacy = new ComboBox<>();
		cbPharmacy.setPromptText("Select receiving pharmacy");
		cbPharmacy.setPrefWidth(380);
		try { cbPharmacy.setItems(FXCollections.observableArrayList(new PharmacyDAO().findAll())); } catch (SQLException ignored) {}

		// Text field for entering the number of units to ship
		TextField tfTransferQty = UIHelper.textField("Quantity to transfer");

		VBox resultArea = new VBox(8);

		// Organizing the input fields in a vertical layout
		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formField("ACTIVE BATCH", "e.g. GBI-20260419-A1B2C3D4 — only Active batches shown", cbBatch),
			UIHelper.formField("RECEIVING PHARMACY", "e.g. CityCare Pharmacy - Islamabad — must be registered in AegisRx", cbPharmacy),
			UIHelper.formField("QUANTITY", "Number of units to ship", tfTransferQty)
		);

		// The button that triggers the transfer logic
		Button btnTransfer = new Button("🚚  Initiate Transfer");
		btnTransfer.getStyleClass().add("btn-primary");

		btnTransfer.setOnAction(e -> {
			resultArea.getChildren().clear();

			try {
				// Get the selected batch, pharmacy, and typed quantity
				MedicineBatch batch = cbBatch.getValue();
				Pharmacy pharmacy = cbPharmacy.getValue();
				
				// Make sure the user didn't leave anything empty
				if (batch == null) { 
					resultArea.getChildren().add(UIHelper.errorBanner("Please select a batch")); 
					return; 
				}
				if (pharmacy == null) { 
					resultArea.getChildren().add(UIHelper.errorBanner("Please select a pharmacy")); 
					return; 
				}
				if (tfTransferQty.getText().isEmpty()) { 
					resultArea.getChildren().add(UIHelper.errorBanner("Enter quantity")); 
					return; 
				}

				// Parse the quantity number and call the service to do the work
				int qty = Integer.parseInt(tfTransferQty.getText().trim());

				batchService.transferBatch(batch.getBatchId(),
					NavigationManager.getCurrentUser().getUserId(), pharmacy.getPharmacyId(), qty);
				
				// Show a success message to the user
				resultArea.getChildren().add(UIHelper.successBanner(
					"Transfer initiated!  " + qty + " units of " + batch.getGbi() + "  →  " + pharmacy.getPharmacyName()));
				
				// If the whole batch was sent, remove it from the dropdown list
				if (qty == batch.getQuantity()) {
					cbBatch.getItems().remove(batch);
				}

				tfTransferQty.clear(); // Clear the text field for the next transfer
			} 
			catch (Exception ex) {
				// If there's an error (like not enough stock), show it here
				resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage()));
			}
		});

		VBox card = UIHelper.card("🚚  Initiate Transfer", "Transfer a batch from your facility to a verified pharmacy",
			formGrid,
			UIHelper.warnStrip("Once transferred, the batch status changes to 'In Transit' and cannot be reversed."),
			new HBox(12, btnTransfer),
			resultArea
		);

		content.getChildren().add(card);
		return content;
	}

	// ---- Batch History ----
	@SuppressWarnings("unchecked")
	private static VBox createHistoryContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader("My Batches", "View all medicine batches registered by your account"));

		TableView<MedicineBatch> table = new TableView<>();

		TableColumn<MedicineBatch, String> colGbi = new TableColumn<>("Batch GBI");
		colGbi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGbi()));
		colGbi.setPrefWidth(210);

		TableColumn<MedicineBatch, String> colDrug = new TableColumn<>("Drug Name");
		colDrug.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDrugName() != null ? d.getValue().getDrugName() : "—"));
		colDrug.setPrefWidth(150);

		TableColumn<MedicineBatch, String> colQty = new TableColumn<>("Quantity");
		colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
		colQty.setPrefWidth(90);

		TableColumn<MedicineBatch, String> colStatus = new TableColumn<>("Status");
		colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().getDisplayName()));
		colStatus.setPrefWidth(110);

		TableColumn<MedicineBatch, String> colProd = new TableColumn<>("Production");
		colProd.setCellValueFactory(d -> new SimpleStringProperty(
			d.getValue().getProductionDate() != null ? d.getValue().getProductionDate().toString() : "—"));
		colProd.setPrefWidth(110);

		TableColumn<MedicineBatch, String> colExp = new TableColumn<>("Expiry");
		colExp.setCellValueFactory(d -> new SimpleStringProperty(
			d.getValue().getExpiryDate() != null ? d.getValue().getExpiryDate().toString() : "—"));
		colExp.setPrefWidth(110);

		TableColumn<MedicineBatch, Void> colAction = new TableColumn<>("Action");
		colAction.setPrefWidth(100);
		colAction.setCellFactory(param -> new TableCell<>() {
			private final Button btn = new Button("📋 Copy");
			{
				btn.getStyleClass().add("btn-ghost");
				btn.setOnAction(event -> {
					MedicineBatch batch = getTableView().getItems().get(getIndex());
					UIHelper.copyToClipboard(batch.getGbi());
				});
			}
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) { setGraphic(null); }
				else { setGraphic(btn); }
			}
		});

		table.getColumns().addAll(colGbi, colDrug, colQty, colStatus, colProd, colExp, colAction);
		table.setPlaceholder(new Label("No batches found. Register a new batch to get started."));
		table.setMinHeight(350);

		try {
			table.setItems(FXCollections.observableArrayList(
				batchService.getBatchesByManufacturer(NavigationManager.getCurrentUser().getUserId())));
		} catch (SQLException e) {
			table.setPlaceholder(new Label("⚠ Database not connected"));
		}

		VBox tableCard = UIHelper.card("📋  Batch Registry", "All batches with their current lifecycle status", table);
		content.getChildren().add(tableCard);
		return content;
	}
}
