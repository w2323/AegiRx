package com.aegisrx.ui.controllers;

import com.aegisrx.domain.MedicineBatch;
import com.aegisrx.domain.enums.BatchStatus;
import com.aegisrx.domain.enums.DisposalReason;
import com.aegisrx.service.BatchService;
import com.aegisrx.service.InventoryService;
import com.aegisrx.service.SaleService;
import com.aegisrx.dao.PharmacyDAO;
import com.aegisrx.domain.Pharmacy;
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
import java.util.List;

// handles the ui logic
public class PharmacyDashboardController {

	private static final BatchService batchService = new BatchService();
	private static final InventoryService inventoryService = new InventoryService();
	private static final SaleService saleService = new SaleService();
	
	// Tracks which pharmacy is currently being viewed.
	private static int currentPharmacyId = -1;
	
	// Tracks which user last loaded this portal.
	// I use this to detect if the user actually changed between loads.
	// If the same user reloads the portal (e.g. after switching pharmacies via the dropdown),
	// I keep the manually selected pharmacy instead of resetting to their default.
	private static int lastLoadedUserId = -1;

	public static Pane createView() {
		// Only re-query the database for the linked pharmacy if a DIFFERENT user just opened this portal.
		// If it's the same user (just reloading after a dropdown switch), we keep their current selection.
		try {
			User user = NavigationManager.getCurrentUser();
			if (user != null && user.getUserId() != lastLoadedUserId) {
				// This is a new user — look up which pharmacy they work at.
				Pharmacy p = new PharmacyDAO().findByStaffUserId(user.getUserId());
				if (p != null) {
					currentPharmacyId = p.getPharmacyId();
					System.out.println("[PharmacyPortal] Logged in as: " + user.getFullName() + " → " + p.getPharmacyName());
				} else {
					// No pharmacy linked to this user. We'll use -1 to signal "not set".
					currentPharmacyId = -1;
					System.err.println("[PharmacyPortal] No pharmacy linked to user ID " + user.getUserId());
				}
				// Remember this user so we don't reset on the next reload.
				lastLoadedUserId = user.getUserId();
			}
			// If same user is reloading, currentPharmacyId keeps the value set by the dropdown.
		} catch (SQLException ex) {
			System.err.println("[PharmacyPortal] Database Error: " + ex.getMessage());
		}

		BorderPane root = new BorderPane();
		root.getStyleClass().add("main-pane");

		BorderPane[] ref = { root };
		VBox sidebar = UIHelper.sidebar("Pharmacy Portal",
			new String[]{ "🔍  Verify Medicine", "📥  Add to Inventory", "🗑  Destroy Medicine", "💰  Sell Medicine", "📋  Current Stock" },
			new Runnable[]{
				() -> ref[0].setCenter(UIHelper.scrollableContent(createVerifyContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createInventoryContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createDestroyContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createSaleContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createStockContent()))
			},
			() -> NavigationManager.showRoleSelector()
		);
		root.setLeft(sidebar);
		root.setCenter(UIHelper.scrollableContent(createVerifyContent()));
		
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
			logoView.setImage(new Image(PharmacyDashboardController.class.getResource("/images/logo.jpg").toExternalForm()));
			logoView.setFitHeight(32);
			logoView.setPreserveRatio(true);
		} catch (Exception ignored) {}
		
		Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
		Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
		
		// The dropdown in the header lets staff switch between pharmacy locations.
	// I'm using Pharmacy objects directly so ALL pharmacies show up (even ones with no staff linked).
	ComboBox<Pharmacy> cbPharmacy = new ComboBox<>();
	try {
		List<Pharmacy> allPharmacies = new PharmacyDAO().findAll();
		cbPharmacy.setItems(FXCollections.observableArrayList(allPharmacies));
		
		// Pre-select the pharmacy that matches the current user's linked pharmacy.
		for (Pharmacy ph : allPharmacies) {
			if (ph.getPharmacyId() == currentPharmacyId) {
				cbPharmacy.setValue(ph);
				break;
			}
		}
	} catch (Exception ignored) {}
	
	// A custom cell factory makes the dropdown show the pharmacy name and city.
	cbPharmacy.setCellFactory(lv -> new ListCell<Pharmacy>() {
		@Override
		protected void updateItem(Pharmacy item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) { setText(null); }
			else { setText(item.getPharmacyName() + " - " + item.getCity()); }
		}
	});
	// Same for the button that shows the currently selected item.
	cbPharmacy.setButtonCell(new ListCell<Pharmacy>() {
		@Override
		protected void updateItem(Pharmacy item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) { setText(null); }
			else { setText(item.getPharmacyName() + " - " + item.getCity()); }
		}
	});
	
	// When the user picks a different pharmacy, I update the ID and reload the dashboard.
	cbPharmacy.setOnAction(e -> {
		Pharmacy selected = cbPharmacy.getValue();
		if (selected != null && selected.getPharmacyId() != currentPharmacyId) {
			currentPharmacyId = selected.getPharmacyId();
			System.out.println("[PharmacyPortal] Switched to pharmacy: " + selected.getPharmacyName());
			javafx.application.Platform.runLater(NavigationManager::showPharmacyDashboard);
		}
	});
	
	Label lblActive = new Label("Active Pharmacy Profile:");
	lblActive.setStyle("-fx-text-fill: #8a9aaa; -fx-font-weight: bold;");
	header.getChildren().addAll(backBtn, logoView, spacer1, lblActive, cbPharmacy, spacer2);
		
		root.setTop(header);
		
		return root;
	}

	// ---- UC3: Verify Medicine Authenticity ----
	private static VBox createVerifyContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Verify Medicine Authenticity", "UC3 — Check a medicine's authenticity using its Global Batch Identifier", "SECURITY"));

		TextField tfGbi = UIHelper.textField("Enter the batch GBI code");
		tfGbi.setPrefWidth(400);

		VBox resultArea = new VBox(12);

		Button btnVerify = new Button("🔍  Verify Batch");
		btnVerify.getStyleClass().add("btn-primary");
		btnVerify.setOnAction(e -> {
			resultArea.getChildren().clear();
			String gbi = tfGbi.getText().trim();
			if (gbi.isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Please enter a GBI code")); return; }

			try {
				MedicineBatch batch = batchService.verifyAuthenticity(gbi);
				if (batch == null) {
					VBox warning = new VBox(10);
					warning.getStyleClass().add("counterfeit-box");
					warning.getChildren().addAll(
						new Label("⚠  SUSPECTED COUNTERFEIT") {{ setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e74c5e;"); }},
						new Label("Batch GBI '" + gbi + "' was NOT FOUND in the AegisRx database.") {{ setStyle("-fx-text-fill: #c06070;"); }},
						new Label("Action: Do NOT accept this medicine. Report to regulatory authorities immediately.") {{ setStyle("-fx-text-fill: #c06070; -fx-font-weight: bold;"); }}
					);
					resultArea.getChildren().add(warning);
				} else {
					boolean isValid = batch.getStatus() == BatchStatus.ACTIVE ||
									  batch.getStatus() == BatchStatus.IN_TRANSIT || batch.getStatus() == BatchStatus.IN_STOCK;

					VBox verifyBox = new VBox(10);
					verifyBox.getStyleClass().add(isValid ? "verified-box" : "counterfeit-box");
					verifyBox.getChildren().addAll(
						new Label(isValid ? "✅  VERIFIED — AUTHENTIC MEDICINE" : "⚠  " + batch.getStatus().getDisplayName())
							{{ setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + (isValid ? "#2ed573" : "#e74c5e") + ";"); }}
					);

					// Batch details as formatted grid
					GridPane details = new GridPane();
					details.setHgap(20);
					details.setVgap(8);
					String lblStyle = "-fx-font-weight: bold; -fx-text-fill: #5a7a6a; -fx-font-size: 11px;";
					String valStyle = "-fx-text-fill: #c0d0e0;";

					int row = 0;
					details.add(new Label("GBI") {{ setStyle(lblStyle); }}, 0, row);
					details.add(new Label(batch.getGbi()) {{ setStyle(valStyle); }}, 1, row++);
					details.add(new Label("DRUG") {{ setStyle(lblStyle); }}, 0, row);
					details.add(new Label(batch.getDrugName() != null ? batch.getDrugName() : "—") {{ setStyle(valStyle); }}, 1, row++);
					details.add(new Label("STATUS") {{ setStyle(lblStyle); }}, 0, row);
					details.add(new Label(batch.getStatus().getDisplayName()) {{ setStyle(valStyle); }}, 1, row++);
					details.add(new Label("QUANTITY") {{ setStyle(lblStyle); }}, 0, row);
					details.add(new Label(String.valueOf(batch.getQuantity())) {{ setStyle(valStyle); }}, 1, row++);
					details.add(new Label("EXPIRY") {{ setStyle(lblStyle); }}, 0, row);
					String expStr = batch.getExpiryDate() != null ? batch.getExpiryDate().toString() : "N/A";
					details.add(new Label(expStr + (batch.isExpired() ? "  ⚠ EXPIRED" : "")) {{ setStyle(valStyle); }}, 1, row++);

					verifyBox.getChildren().add(details);

					// Chain of custody
					try {
						List<String> chain = batchService.getChainOfCustody(batch.getBatchId());
						if (!chain.isEmpty()) {
							verifyBox.getChildren().add(new Label("CHAIN OF CUSTODY") {{ setStyle(lblStyle + " -fx-padding: 10 0 0 0;"); }});
							for (String entry : chain) {
								verifyBox.getChildren().add(new Label("  → " + entry) {{ setStyle("-fx-text-fill: #8a9aaa;"); }});
							}
						}
					} catch (SQLException ignored) {}

					resultArea.getChildren().add(verifyBox);
				}
			} catch (Exception ex) {
				resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage()));
			}
		});

		Button btnPaste = new Button("📋  Paste");
		btnPaste.getStyleClass().add("btn-ghost");
		btnPaste.setOnAction(ev -> {
			javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
			if (cb.hasString()) tfGbi.setText(cb.getString());
		});

		VBox card = UIHelper.card("🔍  Batch Verification", "Enter the GBI code printed on the medicine packaging",
			UIHelper.formField("GLOBAL BATCH IDENTIFIER (GBI)", "e.g. GBI-20260101-A1B2C3D4 — printed as barcode on packaging", 
				new HBox(10, tfGbi, btnPaste)),
			new HBox(12, btnVerify)
		);

		content.getChildren().addAll(card, resultArea);
		return content;
	}

	// ---- UC4: Update Pharmacy Inventory ----
	@SuppressWarnings("unchecked")
	private static VBox createInventoryContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Add to Inventory", "UC4 — Receive incoming shipments and add them to your stock", "INVENTORY"));

		TableView<MedicineBatch> table = new TableView<>();
		TableColumn<MedicineBatch, String> colGbi = new TableColumn<>("Batch GBI");
		colGbi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGbi()));
		colGbi.setPrefWidth(210);
		TableColumn<MedicineBatch, String> colDrug = new TableColumn<>("Drug");
		colDrug.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDrugName() != null ? d.getValue().getDrugName() : "—"));
		colDrug.setPrefWidth(150);
		TableColumn<MedicineBatch, String> colQty = new TableColumn<>("Quantity");
		colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
		colQty.setPrefWidth(90);
		table.getColumns().addAll(colGbi, colDrug, colQty);
		table.setPrefHeight(220);
		table.setPlaceholder(new Label("No incoming shipments at this time"));

		try { table.setItems(FXCollections.observableArrayList(inventoryService.getInTransitBatches(currentPharmacyId))); }
		catch (SQLException ignored) {}

		VBox resultArea = new VBox(8);

		Button btnAdd = new Button("📥  Add Selected to Inventory");
		btnAdd.getStyleClass().add("btn-primary");
		btnAdd.setOnAction(e -> {
			resultArea.getChildren().clear();
			MedicineBatch sel = table.getSelectionModel().getSelectedItem();
			if (sel == null) { resultArea.getChildren().add(UIHelper.errorBanner("Select a batch from the table above")); return; }
			try {
				inventoryService.addToInventory(sel.getBatchId(), currentPharmacyId);
				resultArea.getChildren().add(UIHelper.successBanner(
					"Batch " + sel.getGbi() + " added to inventory!  Status: In Stock"));
				table.getItems().remove(sel);
			} catch (Exception ex) { resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage())); }
		});

		VBox card = UIHelper.card("📥  Incoming Shipments", "Batches currently 'In Transit' destined for your pharmacy — select one to add to stock",
			UIHelper.infoStrip("Only batches shipped to your pharmacy appear here. Click a row to select it."),
			table, new HBox(12, btnAdd), resultArea
		);

		content.getChildren().add(card);
		return content;
	}

	// ---- UC5: Mark Medicine as Destroyed ----
	@SuppressWarnings("unchecked")
	private static VBox createDestroyContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Destroy Medicine", "UC5 — Mark expired, damaged, or recalled stock for disposal", "DISPOSAL"));

		TableView<MedicineBatch> table = new TableView<>();
		TableColumn<MedicineBatch, String> colGbi = new TableColumn<>("Batch GBI");
		colGbi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGbi()));
		colGbi.setPrefWidth(210);
		TableColumn<MedicineBatch, String> colDrug = new TableColumn<>("Drug");
		colDrug.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDrugName() != null ? d.getValue().getDrugName() : "—"));
		colDrug.setPrefWidth(150);
		TableColumn<MedicineBatch, String> colExp = new TableColumn<>("Expiry Date");
		colExp.setCellValueFactory(d -> new SimpleStringProperty(
			d.getValue().getExpiryDate() != null ? d.getValue().getExpiryDate().toString() : "—"));
		colExp.setPrefWidth(120);
		TableColumn<MedicineBatch, String> colExpired = new TableColumn<>("Expired?");
		colExpired.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isExpired() ? "⚠ YES" : "No"));
		colExpired.setPrefWidth(90);
		table.getColumns().addAll(colGbi, colDrug, colExp, colExpired);
		table.setPrefHeight(200);
		table.setPlaceholder(new Label("No in-stock batches available"));

		try { table.setItems(FXCollections.observableArrayList(inventoryService.getInStockBatches(currentPharmacyId))); }
		catch (SQLException ignored) {}

		ComboBox<DisposalReason> cbReason = new ComboBox<>(FXCollections.observableArrayList(DisposalReason.values()));
		cbReason.setPromptText("Select reason");
		cbReason.setPrefWidth(340);

		VBox resultArea = new VBox(8);

		Button btnDestroy = new Button("🗑  Confirm Destruction");
		btnDestroy.getStyleClass().add("btn-danger");
		btnDestroy.setOnAction(e -> {
			resultArea.getChildren().clear();
			MedicineBatch sel = table.getSelectionModel().getSelectedItem();
			if (sel == null) { resultArea.getChildren().add(UIHelper.errorBanner("Select a batch from the table")); return; }
			if (cbReason.getValue() == null) { resultArea.getChildren().add(UIHelper.errorBanner("Select a disposal reason")); return; }
			try {
				inventoryService.destroyMedicine(sel.getBatchId(), currentPharmacyId, cbReason.getValue());
				resultArea.getChildren().add(UIHelper.successBanner(
					"Batch " + sel.getGbi() + " destroyed.  Reason: " + cbReason.getValue().getDisplayName()));
				table.getItems().remove(sel);
			} catch (Exception ex) { resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage())); }
		});

		VBox card = UIHelper.card("🗑  Medicine Disposal", "Select an in-stock batch and reason for destruction",
			UIHelper.warnStrip("Destruction is irreversible. The batch will be permanently removed from inventory."),
			table,
			UIHelper.formField("DISPOSAL REASON", "e.g. Expired, Damaged, Recalled by manufacturer", cbReason),
			new HBox(12, btnDestroy),
			resultArea
		);

		content.getChildren().add(card);
		return content;
	}

	// ---- UC6: Purchase Medicine ----
	private static VBox createSaleContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Sell Medicine", "UC6 — Process a sale with automated AI safety verification", "SALES"));

		TextField tfPatientId = UIHelper.textField("Enter patient's user ID");
		TextField tfQty = UIHelper.textField("Enter quantity to sell");

		ComboBox<MedicineBatch> cbBatch = new ComboBox<>();
		cbBatch.setPromptText("Select in-stock batch");
		cbBatch.setPrefWidth(380);
		try { cbBatch.setItems(FXCollections.observableArrayList(inventoryService.getInStockBatches(currentPharmacyId))); }
		catch (SQLException ignored) {}

		VBox resultArea = new VBox(10);

		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formField("PATIENT USER ID", "e.g. 6 — the registered patient buying medicine (ask for their AegisRx ID)", tfPatientId),
			UIHelper.formField("MEDICINE BATCH", "e.g. GBI-20260201-I9J0K1L2 (Simvastatin) — select from in-stock batches", cbBatch),
			UIHelper.formField("QUANTITY", "Number of units to dispense", tfQty)
		);

		Button btnSell = new Button("💰  Process Sale");
		btnSell.getStyleClass().add("btn-primary");
		btnSell.setOnAction(e -> {
			resultArea.getChildren().clear();
			try {
				if (tfPatientId.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Enter patient user ID")); return; }
				if (tfQty.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Enter quantity")); return; }
				MedicineBatch batch = cbBatch.getValue();
				if (batch == null) { resultArea.getChildren().add(UIHelper.errorBanner("Select a medicine batch")); return; }

				int patientId = Integer.parseInt(tfPatientId.getText().trim());
				int qty = Integer.parseInt(tfQty.getText().trim());

				// I'm adding a check here to make sure the Patient ID actually exists in our system.
				// This prevents the "Foreign Key" database error you saw.
				com.aegisrx.service.UserService userService = new com.aegisrx.service.UserService();
				com.aegisrx.domain.User patient = userService.findById(patientId);

				if (patient == null) {
					resultArea.getChildren().add(UIHelper.errorBanner("Error: Patient ID " + patientId + " does not exist."));
					return;
				}

				// I also check if the user is actually a Patient, not an Admin or Manufacturer.
				if (patient.getRole() != com.aegisrx.domain.enums.UserRole.PATIENT) {
					resultArea.getChildren().add(UIHelper.errorBanner("Error: ID " + patientId + " belongs to a " + patient.getRole().getDisplayName() + ", not a Patient."));
					return;
				}

				saleService.processSale(batch.getBatchId(), currentPharmacyId, patientId, qty);

				VBox successBox = new VBox(8);
				successBox.getStyleClass().addAll("result-banner", "result-success");
				successBox.getChildren().addAll(
					new Label("✅  Sale Completed Successfully!") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #2ed573; -fx-font-size: 15px;"); }},
					new Label("Batch: " + batch.getGbi() + "  |  Patient ID: " + patientId) {{ setStyle("-fx-text-fill: #4a8a6a;"); }},
					new Label("🟢 Safety Check: PASSED — No severe interactions detected") {{ setStyle("-fx-text-fill: #2ed573;"); }}
				);
				resultArea.getChildren().add(successBox);
				cbBatch.getItems().remove(batch);
			} catch (IllegalStateException ex) {
				resultArea.getChildren().add(UIHelper.errorBanner("SALE BLOCKED — " + ex.getMessage()));
			} catch (NumberFormatException ex) {
				resultArea.getChildren().add(UIHelper.errorBanner("Patient ID must be a number (e.g. 6)"));
			} catch (Exception ex) {
				resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage()));
			}
		});

		VBox card = UIHelper.card("💰  Process Medicine Sale", "Verify patient identity and run AI safety check before dispensing",
			formGrid,
			UIHelper.infoStrip("An automated drug interaction check runs before every sale. Severe interactions will BLOCK the sale."),
			new HBox(12, btnSell),
			resultArea
		);

		content.getChildren().add(card);
		return content;
	}

	// ---- Stock Overview ----
	@SuppressWarnings("unchecked")
	private static VBox createStockContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader("Current Stock", "View all medicine batches currently in your pharmacy inventory"));

		int stockCount = 0;
		try { stockCount = inventoryService.getInStockBatches(currentPharmacyId).size(); } catch (SQLException ignored) {}

		HBox stats = new HBox(16);
		stats.getChildren().addAll(
			UIHelper.statCard("📦", String.valueOf(stockCount), "In-Stock Batches", "#00d4aa"),
			UIHelper.statCard("🏥", "Pharmacy #" + currentPharmacyId, "Current Facility", "#3090f0")
		);

		TableView<MedicineBatch> table = new TableView<>();
		TableColumn<MedicineBatch, String> colGbi = new TableColumn<>("Batch GBI");
		colGbi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGbi()));
		colGbi.setPrefWidth(210);
		TableColumn<MedicineBatch, String> colDrug = new TableColumn<>("Drug");
		colDrug.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDrugName() != null ? d.getValue().getDrugName() : "—"));
		colDrug.setPrefWidth(150);
		TableColumn<MedicineBatch, String> colQty = new TableColumn<>("Quantity");
		colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
		colQty.setPrefWidth(90);
		TableColumn<MedicineBatch, String> colExp = new TableColumn<>("Expiry");
		colExp.setCellValueFactory(d -> new SimpleStringProperty(
			d.getValue().getExpiryDate() != null ? d.getValue().getExpiryDate().toString() : "—"));
		colExp.setPrefWidth(120);
		TableColumn<MedicineBatch, String> colExpired = new TableColumn<>("Expired?");
		colExpired.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isExpired() ? "⚠ YES" : "No"));
		colExpired.setPrefWidth(90);
		table.getColumns().addAll(colGbi, colDrug, colQty, colExp, colExpired);
		table.setMinHeight(300);
		table.setPlaceholder(new Label("Inventory empty — add incoming shipments first"));

		try { table.setItems(FXCollections.observableArrayList(inventoryService.getInStockBatches(currentPharmacyId))); }
		catch (SQLException ignored) {}

		VBox tableCard = UIHelper.card("📋  Inventory Register", "All batches with verified stock in your pharmacy", table);
		content.getChildren().addAll(stats, tableCard);
		return content;
	}
}
