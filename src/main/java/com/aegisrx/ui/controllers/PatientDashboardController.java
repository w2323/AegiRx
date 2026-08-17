package com.aegisrx.ui.controllers;

import com.aegisrx.domain.*;
import com.aegisrx.observer.CaretakerNotificationObserver;
import com.aegisrx.observer.UIAlertObserver;
import com.aegisrx.service.*;
import com.aegisrx.ui.NavigationManager;
import com.aegisrx.ui.UIHelper;
import javafx.application.Platform;
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

// controller for the view
public class PatientDashboardController {

	private static final MedicationProfileService medService = new MedicationProfileService();
	private static final DrugInteractionService interactionService = new DrugInteractionService();
	private static final DietaryConflictService dietaryService = new DietaryConflictService();
	private static final SmartLocatorService locatorService = new SmartLocatorService();
	private static final AIService aiService = new AIService();

	static {
		dietaryService.addObserver(new UIAlertObserver());
		dietaryService.addObserver(new CaretakerNotificationObserver());
	}

	public static Pane createView() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("main-pane");

		BorderPane[] ref = { root };
		VBox sidebar = UIHelper.sidebar("Patient Portal",
			new String[]{
				"🏠  Dashboard",
				"💊  My Medications",
				"⚗  Drug Interactions",
				"🍽  Food Tracker",
				"📍  Smart Locator",
				"🤖  AI Assistant"
			},
			new Runnable[]{
				() -> ref[0].setCenter(UIHelper.scrollableContent(createDashboardContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createMedicationsContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createInteractionsContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createFoodContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createLocatorContent())),
				() -> ref[0].setCenter(UIHelper.scrollableContent(createAIChatContent()))
			},
			() -> NavigationManager.showRoleSelector()
		);
		root.setLeft(sidebar);
		root.setCenter(UIHelper.scrollableContent(createDashboardContent()));
		
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
			logoView.setImage(new Image(PatientDashboardController.class.getResource("/images/logo.jpg").toExternalForm()));
			logoView.setFitHeight(32);
			logoView.setPreserveRatio(true);
		} catch (Exception ignored) {}
		
		Region spacer1 = new Region(); HBox.setHgrow(spacer1, Priority.ALWAYS);
		Region spacer2 = new Region(); HBox.setHgrow(spacer2, Priority.ALWAYS);
		
		ComboBox<User> cbActiveUser = new ComboBox<>();
		try {
			java.util.List<User> users = new com.aegisrx.service.UserService().getUsersByRole(com.aegisrx.domain.enums.UserRole.PATIENT);
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
					setText(item.getFullName() + " (Patient)");
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
				javafx.application.Platform.runLater(NavigationManager::showPatientDashboard);
			}
		});

		Label lblActive = new Label("Active Patient Profile (ID: " + NavigationManager.getCurrentUser().getUserId() + "):");
		lblActive.setStyle("-fx-text-fill: #8a9aaa; -fx-font-weight: bold;");
		header.getChildren().addAll(backBtn, logoView, spacer1, lblActive, cbActiveUser, spacer2);
		
		root.setTop(header);
		
		return root;
	}

	// ──── Dashboard Home ────
	private static VBox createDashboardContent() {
		VBox content = new VBox(22);
		int patientId = NavigationManager.getCurrentUser().getUserId();

		content.getChildren().add(UIHelper.pageHeader(
			"Welcome, " + NavigationManager.getCurrentUser().getFullName(),
			"Patient Safety Dashboard — Monitor your medications, diet, and health", "PATIENT"));

		// Stats
		int medCount = 0, alertCount = 0;
		try { medCount = medService.getActiveMedications(patientId).size(); } catch (SQLException ignored) {}
		try { alertCount = dietaryService.getUnacknowledgedAlerts(patientId).size(); } catch (SQLException ignored) {}

		HBox stats = new HBox(16);
		stats.getChildren().addAll(
			UIHelper.statCard("💊", String.valueOf(medCount), "Active Medications", "#00d4aa"),
			UIHelper.statCard("⚠", String.valueOf(alertCount), "Pending Alerts", alertCount > 0 ? "#e74c5e" : "#2ed573"),
			UIHelper.statCard("🧠", interactionService.getCurrentStrategyName().contains("AI") ? "ON" : "OFF", "AI Engine", "#3090f0"),
			UIHelper.statCard("🛡", "Active", "Protection Status", "#2ed573")
		);

		// Alerts
		VBox alertsCard = new VBox(12);
		alertsCard.getStyleClass().add("card");
		Label alertTitle = new Label("⚠  Pending Dietary Alerts");
		alertTitle.getStyleClass().add("card-title");
		Label alertSub = new Label("Conflicts detected between your food log and active medications");
		alertSub.getStyleClass().add("card-subtitle");
		alertsCard.getChildren().addAll(alertTitle, alertSub);

		try {
			List<DietaryAlert> alerts = dietaryService.getUnacknowledgedAlerts(patientId);
			if (alerts.isEmpty()) {
				alertsCard.getChildren().add(UIHelper.successBanner("No pending alerts — Your diet is safe!"));
			} else {
				for (DietaryAlert alert : alerts) {
					HBox alertRow = new HBox(12);
					alertRow.setAlignment(Pos.CENTER_LEFT);
					alertRow.setStyle("-fx-background-color: #0f1822; -fx-padding: 12 16; -fx-background-radius: 10;");

					Label badge = UIHelper.severityBadge(alert.getSeverity().getDisplayName(),
						alert.getSeverity().name());
					Label reason = new Label(alert.getReason());
					reason.setWrapText(true);
					reason.setStyle("-fx-text-fill: #a0b0c0;");
					HBox.setHgrow(reason, Priority.ALWAYS);

					Button ack = new Button("Acknowledge");
					ack.getStyleClass().add("btn-ghost");
					ack.setOnAction(e -> {
						try { dietaryService.acknowledgeAlert(alert.getAlertId()); ack.setText("✅ Done"); ack.setDisable(true); }
						catch (SQLException ignored) {}
					});

					alertRow.getChildren().addAll(badge, reason, ack);
					alertsCard.getChildren().add(alertRow);
				}
			}
		} catch (SQLException ignored) {
			alertsCard.getChildren().add(new Label("⚠ Could not load alerts"));
		}

		content.getChildren().addAll(stats, alertsCard);
		return content;
	}

	// ──── I built this section so patients can keep track of the medicines they take. ────
	@SuppressWarnings("unchecked")
	private static VBox createMedicationsContent() {
		VBox content = new VBox(22);
		int patientId = NavigationManager.getCurrentUser().getUserId();

		content.getChildren().add(UIHelper.pageHeader(
			"My Medications", "UC7 — Register and manage your active medication profile", "HEALTH"));

		// I'm using a dropdown here so users can pick a drug from our verified list.
		ComboBox<DrugProfile> cbDrug = new ComboBox<>();
		cbDrug.setPromptText("Search and select");
		cbDrug.setPrefWidth(340);
		try { cbDrug.setItems(FXCollections.observableArrayList(medService.getAllDrugs())); }
		catch (SQLException ignored) {}

		TextField tfDosage = UIHelper.textField("Enter dosage amount");
		TextField tfFrequency = UIHelper.textField("Enter frequency");

		// Here is where I arrange the input fields in a nice grid.
		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formField("MEDICATION NAME", "e.g. Metformin, Warfarin, Lisinopril — select from verified database", cbDrug),
			UIHelper.formRow(
				UIHelper.formField("DOSAGE", "e.g. 500mg, 10mg, 850mg — include units", tfDosage),
				UIHelper.formField("FREQUENCY", "e.g. Twice daily, Once at night, Every 8 hours", tfFrequency)
			)
		);

		VBox resultArea = new VBox(8);

		// I added this table so the user can actually see their current list of meds.
		TableView<Medication> table = new TableView<>();
		TableColumn<Medication, String> colDrug = new TableColumn<>("Medication");
		colDrug.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDrugName()));
		colDrug.setPrefWidth(180);
		TableColumn<Medication, String> colDos = new TableColumn<>("Dosage");
		colDos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDosage()));
		colDos.setPrefWidth(100);
		TableColumn<Medication, String> colFreq = new TableColumn<>("Frequency");
		colFreq.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFrequency()));
		colFreq.setPrefWidth(150);
		TableColumn<Medication, String> colDate = new TableColumn<>("Since");
		colDate.setCellValueFactory(d -> new SimpleStringProperty(
			d.getValue().getAddedAt() != null ? d.getValue().getAddedAt().toLocalDate().toString() : "—"));
		colDate.setPrefWidth(110);
		table.getColumns().addAll(colDrug, colDos, colFreq, colDate);
		table.setPrefHeight(220);
		table.setPlaceholder(new Label("No active medications — Add one above"));

		try { table.setItems(FXCollections.observableArrayList(medService.getActiveMedications(patientId))); }
		catch (SQLException ignored) {}

		// This button saves the new medication into the database.
		Button btnAdd = new Button("➕  Add Medication");
		btnAdd.getStyleClass().add("btn-primary");
		btnAdd.setOnAction(e -> {
			resultArea.getChildren().clear();
			try {
				DrugProfile drug = cbDrug.getValue();
				if (drug == null) { resultArea.getChildren().add(UIHelper.errorBanner("Select a medication from the dropdown")); return; }
				if (tfDosage.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Enter dosage (e.g. 500mg)")); return; }

				Medication med = medService.addMedication(patientId, drug.getDrugId(), tfDosage.getText(), tfFrequency.getText());
				resultArea.getChildren().add(UIHelper.successBanner(drug.getDrugName() + " added to your profile!"));
				table.getItems().add(med);
				tfDosage.clear(); tfFrequency.clear();
			} catch (Exception ex) { resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage())); }
		});

		Button btnRemove = new Button("🗑  Remove Selected");
		btnRemove.getStyleClass().add("btn-danger");
		btnRemove.setOnAction(e -> {
			Medication sel = table.getSelectionModel().getSelectedItem();
			if (sel != null) { try { medService.removeMedication(sel.getMedId()); table.getItems().remove(sel); } catch (SQLException ignored) {} }
		});

		VBox addCard = UIHelper.card("➕  Add New Medication", "Register a prescribed medication to your active profile for safety monitoring",
			formGrid,
			UIHelper.infoStrip("Added medications are used for drug interaction checks and dietary conflict analysis."),
			new HBox(12, btnAdd), resultArea
		);

		VBox tableCard = UIHelper.card("📋  Active Medication Profile", "Your current medications monitored by AegisRx",
			table, new HBox(12, btnRemove));

		content.getChildren().addAll(addCard, tableCard);
		return content;
	}

	// ──── UC8: Drug Interactions ────
	private static VBox createInteractionsContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Check Drug Interactions", "UC8 — AI-powered analysis of potential medication conflicts", "AI SAFETY"));

		VBox resultCard = new VBox(12);
		resultCard.getStyleClass().add("card");
		Label resultTitle = new Label("📊  Analysis Results");
		resultTitle.getStyleClass().add("card-title");
		Label resultSub = new Label("Click the button above to run interaction analysis");
		resultSub.getStyleClass().add("card-subtitle");
		resultCard.getChildren().addAll(resultTitle, resultSub);

		Button btnCheck = new Button("🔬  Analyze My Medications");
		btnCheck.getStyleClass().add("btn-primary");
		btnCheck.setOnAction(e -> {
			resultCard.getChildren().clear();
			resultCard.getChildren().addAll(resultTitle);
			Label loading = new Label("⏳  Scanning your medication profile...");
			loading.setStyle("-fx-text-fill: #3090f0; -fx-font-style: italic;");
			resultCard.getChildren().add(loading);

			new Thread(() -> {
				try {
					int patientId = NavigationManager.getCurrentUser().getUserId();
					List<InteractionReport> reports = interactionService.checkPatientInteractions(patientId);

					Platform.runLater(() -> {
						resultCard.getChildren().clear();
						resultCard.getChildren().add(resultTitle);

						if (reports.isEmpty()) {
							VBox safeBox = new VBox(6);
							safeBox.getStyleClass().addAll("result-banner", "result-success");
							safeBox.getChildren().addAll(
								new Label("✅  No Known Interactions Detected") {{ setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2ed573;"); }},
								new Label("Your current medication profile appears safe. Continue as prescribed.") {{ setStyle("-fx-text-fill: #4a8a6a;"); }}
							);
							resultCard.getChildren().add(safeBox);
						} else {
							resultCard.getChildren().add(UIHelper.warnStrip(reports.size() + " interaction(s) found — Review each below"));

							for (InteractionReport r : reports) {
								VBox rBox = new VBox(8);
								rBox.setStyle("-fx-background-color: #0f1822; -fx-padding: 16; -fx-background-radius: 10; -fx-border-color: #1a2838; -fx-border-radius: 10; -fx-border-width: 1;");

								HBox header = new HBox(10);
								header.setAlignment(Pos.CENTER_LEFT);
								header.getChildren().addAll(
									UIHelper.severityBadge(r.getSeverity().getDisplayName(), r.getSeverity().name()),
									new Label(r.getDrugName1() + "  ↔  " + r.getDrugName2()) {{ setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e0e8f0;"); }}
								);

								Label desc = new Label(r.getDescription());
								desc.setWrapText(true);
								desc.setStyle("-fx-text-fill: #8a9aaa;");

								rBox.getChildren().addAll(header, desc);

								if (r.getRecommendation() != null && !r.getRecommendation().isEmpty()) {
									Label rec = new Label("💡  " + r.getRecommendation());
									rec.setWrapText(true);
									rec.setStyle("-fx-text-fill: #00d4aa; -fx-font-size: 12px;");
									rBox.getChildren().add(rec);
								}

								resultCard.getChildren().add(rBox);
							}
						}
					});
				} catch (Exception ex) {
					Platform.runLater(() -> {
						resultCard.getChildren().clear();
						resultCard.getChildren().addAll(resultTitle, UIHelper.errorBanner(ex.getMessage()));
					});
				}
			}).start();
		});

		VBox infoCard = UIHelper.card("🧠  Interaction Check Engine",
			"Strategy: " + interactionService.getCurrentStrategyName(),
			UIHelper.infoStrip("This analysis checks all pairwise combinations of your active medications for known conflicts."),
			new HBox(12, btnCheck)
		);

		content.getChildren().addAll(infoCard, resultCard);
		return content;
	}

	// ──── UC9 & UC10: Food Tracker ────
	private static VBox createFoodContent() {
		VBox content = new VBox(22);
		int patientId = NavigationManager.getCurrentUser().getUserId();

		content.getChildren().add(UIHelper.pageHeader(
			"Food Tracker", "UC9/UC10 — Log meals and receive AI-powered dietary conflict alerts", "DIET SAFETY"));

		TextField tfFood = UIHelper.textField("What did you eat?");
		tfFood.setPrefWidth(360);

		ComboBox<String> cbCategory = new ComboBox<>(FXCollections.observableArrayList(
			"Fruits", "Vegetables", "Dairy", "Meat", "Grains",
			"Sweets", "Beverages", "Snacks", "Salty Foods",
			"Seafood", "Fast Food", "Other"
		));
		cbCategory.setPromptText("Pick category");
		cbCategory.setPrefWidth(340);

		VBox resultArea = new VBox(8);

		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formField("FOOD ITEM", "e.g. Grapefruit, Spinach Salad, Chocolate Cake, Grilled Chicken", tfFood),
			UIHelper.formField("FOOD CATEGORY", "e.g. Fruits, Dairy, Sweets — helps AI analyze nutritional interactions", cbCategory)
		);

		Button btnLog = new Button("🍽  Log Food");
		btnLog.getStyleClass().add("btn-primary");
		btnLog.setOnAction(e -> {
			resultArea.getChildren().clear();
			if (tfFood.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Enter what you ate (e.g. Grapefruit)")); return; }
			try {
				String cat = cbCategory.getValue() != null ? cbCategory.getValue() : "Other";
				FoodEntry entry = dietaryService.logFood(patientId, tfFood.getText(), cat, "{}");
				resultArea.getChildren().add(UIHelper.successBanner(
					tfFood.getText() + " logged at " + entry.getLoggedAt().toLocalTime().toString().substring(0, 5)));
				tfFood.clear();
			} catch (Exception ex) { resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage())); }
		});

		VBox logCard = UIHelper.card("🍽  Log a Meal", "Record what you eat — AegisRx will check for conflicts with your medications",
			formGrid,
			UIHelper.infoStrip("If a conflict is detected, you'll receive an alert immediately. Common conflicts: Grapefruit + Statins, Spinach + Warfarin."),
			new HBox(12, btnLog), resultArea
		);

		// History
		VBox histCard = new VBox(12);
		histCard.getStyleClass().add("card");
		Label histTitle = new Label("📋  Today's Food Log");
		histTitle.getStyleClass().add("card-title");
		histCard.getChildren().add(histTitle);

		try {
			List<FoodEntry> entries = dietaryService.getTodaysFoodLog(patientId);
			if (entries.isEmpty()) {
				histCard.getChildren().add(new Label("No food logged today — Start by logging a meal above") {{ setStyle("-fx-text-fill: #4a5a6a;"); }});
			} else {
				for (FoodEntry entry : entries) {
					HBox row = new HBox(10);
					row.setStyle("-fx-background-color: #0f1822; -fx-padding: 10 14; -fx-background-radius: 8;");
					row.setAlignment(Pos.CENTER_LEFT);
					row.getChildren().addAll(
						new Label("🍽") {{ setStyle("-fx-font-size: 16px;"); }},
						new Label(entry.getFoodName()) {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #c0d0e0;"); }},
						new Label("(" + entry.getFoodCategory() + ")") {{ setStyle("-fx-text-fill: #5a6a7a;"); }},
						new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }},
						new Label(entry.getLoggedAt().toLocalTime().toString().substring(0, 5)) {{ setStyle("-fx-text-fill: #3a4a5a;"); }}
					);
					histCard.getChildren().add(row);
				}
			}
		} catch (SQLException ignored) {
			histCard.getChildren().add(new Label("⚠ Could not load food history"));
		}

		content.getChildren().addAll(logCard, histCard);
		return content;
	}

	// ──── UC11: Smart Locator ────
	private static VBox createLocatorContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"Smart Locator", "UC11 — Find pharmacies near you with verified stock of your medicine", "LOCATOR"));

		ComboBox<DrugProfile> cbDrug = new ComboBox<>();
		cbDrug.setPromptText("Select medicine");
		cbDrug.setPrefWidth(340);
		try { cbDrug.setItems(FXCollections.observableArrayList(locatorService.getAllDrugs())); }
		catch (SQLException ignored) {}

		TextField tfRegion = UIHelper.textField("Enter city or region");
		tfRegion.setPrefWidth(340);

		VBox resultArea = new VBox(12);

		VBox formGrid = new VBox(14);
		formGrid.getChildren().addAll(
			UIHelper.formField("MEDICINE", "e.g. Metformin, Lisinopril — the medicine you're looking for", cbDrug),
			UIHelper.formField("CITY / REGION", "e.g. Islamabad, Lahore, Punjab, Sindh — narrowing down search area", tfRegion)
		);

		Button btnSearch = new Button("📍  Search Pharmacies");
		btnSearch.getStyleClass().add("btn-primary");
		btnSearch.setOnAction(e -> {
			resultArea.getChildren().clear();
			try {
				DrugProfile drug = cbDrug.getValue();
				if (drug == null) { resultArea.getChildren().add(UIHelper.errorBanner("Select a medicine to search")); return; }
				if (tfRegion.getText().isEmpty()) { resultArea.getChildren().add(UIHelper.errorBanner("Enter a city or region (e.g. Islamabad)")); return; }

				List<Pharmacy> results = locatorService.locateMedicine(drug.getDrugId(), tfRegion.getText());
				if (results.isEmpty()) {
					resultArea.getChildren().add(UIHelper.warnStrip(
						"No pharmacies in '" + tfRegion.getText() + "' have verified stock of " + drug.getDrugName() +
						". Try a different region."));
				} else {
					resultArea.getChildren().add(UIHelper.successBanner(
						results.size() + " pharmacy(ies) found with verified " + drug.getDrugName() + " stock"));

					for (Pharmacy p : results) {
						VBox pCard = new VBox(6);
						pCard.setStyle("-fx-background-color: #0f1822; -fx-padding: 16; -fx-background-radius: 10; -fx-border-color: #1a2838; -fx-border-radius: 10; -fx-border-width: 1;");
						pCard.getChildren().addAll(
							new Label("🏥  " + p.getPharmacyName()) {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #c0d0e0; -fx-font-size: 14px;"); }},
							new Label("📍  " + p.getCity() + ", " + p.getRegion()) {{ setStyle("-fx-text-fill: #8a9aaa;"); }},
							new Label("📞  " + p.getContactInfo()) {{ setStyle("-fx-text-fill: #5a7a6a;"); }}
						);
						resultArea.getChildren().add(pCard);
					}
				}
			} catch (Exception ex) { resultArea.getChildren().add(UIHelper.errorBanner(ex.getMessage())); }
		});

		VBox card = UIHelper.card("📍  Find Medicine Near You", "Search for pharmacies with verified stock in the AegisRx network",
			formGrid,
			UIHelper.infoStrip("Only pharmacies with AegisRx-verified stock are shown. Results are guaranteed authentic."),
			new HBox(12, btnSearch)
		);

		content.getChildren().addAll(card, resultArea);
		return content;
	}

	// ──── AI Smart Suggestion Panel ────
	private static VBox createAIChatContent() {
		VBox content = new VBox(22);

		content.getChildren().add(UIHelper.pageHeader(
			"🤖  AI Health Assistant",
			"Ask questions about your medications, diet, and health — powered by Gemini AI",
			aiService.isAIAvailable() ? "AI ONLINE" : "AI OFFLINE"));

		// Chat area
		VBox chatArea = new VBox(12);
		chatArea.setStyle("-fx-padding: 8;");

		ScrollPane chatScroll = new ScrollPane(chatArea);
		chatScroll.setFitToWidth(true);
		chatScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
		chatScroll.setPrefHeight(380);

		// Welcome
		VBox welcomeMsg = new VBox(6);
		welcomeMsg.getStyleClass().add("chat-bubble-ai");
		welcomeMsg.getChildren().addAll(
			new Label("👋  Hello! I'm your AegisRx AI assistant.") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #c0d0e0;"); }},
			new Label("I can help you with:") {{ setStyle("-fx-text-fill: #8a9aaa;"); }},
			new Label("  • \"What are the side effects of Metformin?\"") {{ setStyle("-fx-text-fill: #5a7a6a; -fx-font-size: 12px;"); }},
			new Label("  • \"Can I eat grapefruit with my statin?\"") {{ setStyle("-fx-text-fill: #5a7a6a; -fx-font-size: 12px;"); }},
			new Label("  • \"What foods should I avoid?\"") {{ setStyle("-fx-text-fill: #5a7a6a; -fx-font-size: 12px;"); }},
			new Label("  • \"Is it safe to take aspirin with warfarin?\"") {{ setStyle("-fx-text-fill: #5a7a6a; -fx-font-size: 12px;"); }}
		);
		chatArea.getChildren().add(welcomeMsg);

		// Input
		TextField tfInput = UIHelper.textField("Type your question here...");
		HBox.setHgrow(tfInput, Priority.ALWAYS);

		Button btnSend = new Button("Send");
		btnSend.getStyleClass().add("btn-primary");

		Button btnDiet = new Button("📋  Get Diet Plan");
		btnDiet.getStyleClass().add("btn-secondary");

		HBox inputRow = new HBox(10, tfInput, btnSend, btnDiet);
		inputRow.setAlignment(Pos.CENTER_LEFT);

		Runnable sendMessage = () -> {
			String q = tfInput.getText().trim();
			if (q.isEmpty()) return;

			VBox userBubble = new VBox(4);
			userBubble.getStyleClass().add("chat-bubble-user");
			userBubble.setMaxWidth(500);
			userBubble.getChildren().add(new Label("You:  " + q) {{ setStyle("-fx-text-fill: #c0d0e0;"); }});
			chatArea.getChildren().add(userBubble);
			tfInput.clear();

			Label thinking = new Label("🤖  Thinking...") {{ setStyle("-fx-text-fill: #3090f0; -fx-font-style: italic;"); }};
			chatArea.getChildren().add(thinking);

			new Thread(() -> {
				try {
					String resp = aiService.askQuestion(NavigationManager.getCurrentUser().getUserId(), q);
					Platform.runLater(() -> {
						chatArea.getChildren().remove(thinking);
						VBox aiBubble = new VBox(4);
						aiBubble.getStyleClass().add("chat-bubble-ai");
						aiBubble.setMaxWidth(550);
						Label respLabel = new Label("🤖  " + resp);
						respLabel.setWrapText(true);
						respLabel.setStyle("-fx-text-fill: #b0c0d0;");
						aiBubble.getChildren().add(respLabel);
						chatArea.getChildren().add(aiBubble);
					});
				} catch (Exception ex) {
					Platform.runLater(() -> { chatArea.getChildren().remove(thinking); chatArea.getChildren().add(UIHelper.errorBanner(ex.getMessage())); });
				}
			}).start();
		};

		btnSend.setOnAction(e -> sendMessage.run());
		tfInput.setOnAction(e -> sendMessage.run());

		btnDiet.setOnAction(e -> {
			Label thinking = new Label("🤖  Generating personalized dietary plan...") {{ setStyle("-fx-text-fill: #3090f0; -fx-font-style: italic;"); }};
			chatArea.getChildren().add(thinking);

			new Thread(() -> {
				try {
					String resp = aiService.getDietaryRecommendation(NavigationManager.getCurrentUser().getUserId());
					Platform.runLater(() -> {
						chatArea.getChildren().remove(thinking);
						VBox aiBubble = new VBox(4);
						aiBubble.getStyleClass().add("chat-bubble-ai");
						Label respLabel = new Label("🤖  " + resp);
						respLabel.setWrapText(true);
						respLabel.setStyle("-fx-text-fill: #b0c0d0;");
						aiBubble.getChildren().add(respLabel);
						chatArea.getChildren().add(aiBubble);
					});
				} catch (Exception ex) {
					Platform.runLater(() -> { chatArea.getChildren().remove(thinking); });
				}
			}).start();
		});

		VBox chatCard = UIHelper.card("💬  Chat", "Have a conversation with your AI health assistant",
			chatScroll, inputRow);

		content.getChildren().add(chatCard);
		return content;
	}
}
