package com.aegisrx.ui;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

// handles the ui logic
public class UIHelper {

	// ---- Form Field with Label + Example ----
	public static VBox formField(String label, String example, Node inputField) {
		VBox group = new VBox(5);
		group.getStyleClass().add("form-group");

		Label lbl = new Label(label);
		lbl.getStyleClass().add("field-label");

		Label eg = new Label(example);
		eg.getStyleClass().add("field-example");

		group.getChildren().addAll(lbl, eg, inputField);
		return group;
	}

	public static VBox formField(String label, String example, Node inputField, double prefWidth) {
		VBox group = formField(label, example, inputField);
		if (inputField instanceof Region) {
			((Region) inputField).setPrefWidth(prefWidth);
		}
		return group;
	}

	// ---- Text Field with prompt ----
	public static TextField textField(String prompt) {
		TextField tf = new TextField();
		tf.setPromptText(prompt);
		tf.setPrefWidth(320);
		return tf;
	}

	public static PasswordField passwordField(String prompt) {
		PasswordField pf = new PasswordField();
		pf.setPromptText(prompt);
		pf.setPrefWidth(320);
		return pf;
	}

	// ---- Page Header ----
	public static VBox pageHeader(String title, String subtitle) {
		VBox header = new VBox(4);
		header.getStyleClass().add("page-header");

		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("header-title");

		ImageView logoView = new ImageView();
		try {
			logoView.setImage(new Image(UIHelper.class.getResource("/images/logo.jpg").toExternalForm()));
			logoView.setFitHeight(24);
			logoView.setPreserveRatio(true);
		} catch (Exception ignored) {}

		HBox titleRow = new HBox(10);
		titleRow.setAlignment(Pos.CENTER_LEFT);
		titleRow.getChildren().addAll(logoView, titleLabel);

		Label subLabel = new Label(subtitle);
		subLabel.getStyleClass().add("header-subtitle");

		header.getChildren().addAll(titleRow, subLabel);
		return header;
	}

	public static VBox pageHeader(String title, String subtitle, String badgeText) {
		VBox header = pageHeader(title, subtitle);

		Label badge = new Label(badgeText);
		badge.getStyleClass().add("header-badge");
		
		HBox row = new HBox(12);
		row.setAlignment(Pos.CENTER_LEFT);
		row.getChildren().addAll(header.getChildren().get(0), badge);
		
		header.getChildren().set(0, row);
		return header;
	}

	// ---- Card Container ----
	public static VBox card(String title, String subtitle, Node... children) {
		VBox card = new VBox(14);
		card.getStyleClass().add("card");

		if (title != null) {
			Label titleLabel = new Label(title);
			titleLabel.getStyleClass().add("card-title");
			card.getChildren().add(titleLabel);
		}

		if (subtitle != null) {
			Label subLabel = new Label(subtitle);
			subLabel.getStyleClass().add("card-subtitle");
			card.getChildren().add(subLabel);
		}

		card.getChildren().addAll(children);
		return card;
	}

	// ---- Stat Card ----
	public static VBox statCard(String icon, String value, String label, String color) {
		VBox card = new VBox(6);
		card.getStyleClass().add("stat-card");
		card.setAlignment(Pos.CENTER_LEFT);

		HBox top = new HBox(10);
		top.setAlignment(Pos.CENTER_LEFT);

		Label iconLabel = new Label(icon);
		iconLabel.getStyleClass().add("stat-icon");

		Label lblLabel = new Label(label.toUpperCase());
		lblLabel.getStyleClass().add("stat-label");

		top.getChildren().addAll(iconLabel, lblLabel);

		Label valLabel = new Label(value);
		valLabel.getStyleClass().add("stat-value");
		valLabel.setStyle("-fx-text-fill: " + color + ";");

		card.getChildren().addAll(top, valLabel);
		HBox.setHgrow(card, Priority.ALWAYS);
		return card;
	}

	// ---- Result Banner ----
	public static Label successBanner(String message) {
		Label lbl = new Label("✅  " + message);
		lbl.getStyleClass().addAll("result-banner", "result-success");
		lbl.setWrapText(true);
		lbl.setMaxWidth(Double.MAX_VALUE);
		return lbl;
	}

	public static Label errorBanner(String message) {
		Label lbl = new Label("❌  " + message);
		lbl.getStyleClass().addAll("result-banner", "result-error");
		lbl.setWrapText(true);
		lbl.setMaxWidth(Double.MAX_VALUE);
		return lbl;
	}

	// ---- Severity Badge ----
	public static Label severityBadge(String text, String level) {
		Label badge = new Label(text);
		badge.getStyleClass().add("badge-" + level.toLowerCase());
		return badge;
	}

	// ---- Info / Warning Strips ----
	public static HBox infoStrip(String message) {
		HBox strip = new HBox(8);
		strip.getStyleClass().add("info-strip");
		strip.setAlignment(Pos.CENTER_LEFT);
		Label icon = new Label("ℹ");
		icon.setStyle("-fx-text-fill: #3090f0; -fx-font-size: 16px;");
		Label text = new Label(message);
		text.setStyle("-fx-text-fill: #6090c0; -fx-font-size: 12px;");
		text.setWrapText(true);
		strip.getChildren().addAll(icon, text);
		return strip;
	}

	public static HBox warnStrip(String message) {
		HBox strip = new HBox(8);
		strip.getStyleClass().add("warn-strip");
		strip.setAlignment(Pos.CENTER_LEFT);
		Label icon = new Label("⚠");
		icon.setStyle("-fx-text-fill: #f0a030; -fx-font-size: 16px;");
		Label text = new Label(message);
		text.setStyle("-fx-text-fill: #c09040; -fx-font-size: 12px;");
		text.setWrapText(true);
		strip.getChildren().addAll(icon, text);
		return strip;
	}

	// ---- Sidebar Builder ----
	public static VBox sidebar(String portalName, String[] btnLabels, Runnable[] actions, Runnable onLogout) {
		VBox sidebar = new VBox(4);
		sidebar.getStyleClass().add("sidebar");

		Label brand = new Label("💊 AegisRx");
		brand.getStyleClass().add("sidebar-brand");

		Label role = new Label(portalName.toUpperCase());
		role.getStyleClass().add("sidebar-role");

		sidebar.getChildren().addAll(brand, role, new Separator());

		// Section label
		Label navLabel = new Label("NAVIGATION");
		navLabel.getStyleClass().add("sidebar-section-label");
		sidebar.getChildren().add(navLabel);

		Button[] buttons = new Button[btnLabels.length];
		for (int i = 0; i < btnLabels.length; i++) {
			final int idx = i;
			buttons[i] = new Button(btnLabels[i]);
			buttons[i].getStyleClass().add("sidebar-btn");
			if (i == 0) buttons[i].getStyleClass().add("sidebar-btn-active");
			buttons[i].setMaxWidth(Double.MAX_VALUE);
			buttons[i].setOnAction(e -> {
				for (Button b : buttons) b.getStyleClass().remove("sidebar-btn-active");
				buttons[idx].getStyleClass().add("sidebar-btn-active");
				actions[idx].run();
			});
			sidebar.getChildren().add(buttons[i]);
		}

		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		Label sectionLabel2 = new Label("ACCOUNT");
		sectionLabel2.getStyleClass().add("sidebar-section-label");

		Button btnLogout = new Button("🚪  Switch Role");
		btnLogout.getStyleClass().add("sidebar-btn");
		btnLogout.setMaxWidth(Double.MAX_VALUE);
		btnLogout.setOnAction(e -> onLogout.run());

		String displayUser = NavigationManager.getCurrentUser() != null ?
			NavigationManager.getCurrentUser().getFullName() : "User";
		
		// Resolve Pharmacy name for staff to match Manufacturer style (seed data style)
		if (NavigationManager.getCurrentUser() != null && NavigationManager.getCurrentUser().getRole() == com.aegisrx.domain.enums.UserRole.PHARMACY) {
			try {
				com.aegisrx.domain.Pharmacy p = new com.aegisrx.dao.PharmacyDAO().findByStaffUserId(NavigationManager.getCurrentUser().getUserId());
				if (p != null) displayUser = displayUser + " (" + p.getPharmacyName() + ")";
			} catch (Exception ignored) {}
		}
			
		Label userLabel = new Label("👤 " + displayUser);
		userLabel.getStyleClass().add("sidebar-footer");

		sidebar.getChildren().addAll(spacer, new Separator(), sectionLabel2, btnLogout, new Separator(), userLabel);
		return sidebar;
	}

	// ---- Two-column form row ----
	public static HBox formRow(VBox left, VBox right) {
		HBox row = new HBox(20);
		HBox.setHgrow(left, Priority.ALWAYS);
		HBox.setHgrow(right, Priority.ALWAYS);
		row.getChildren().addAll(left, right);
		return row;
	}

	// ---- Scrollable dashboard content ----
	public static ScrollPane scrollableContent(VBox content) {
		content.getStyleClass().add("dashboard-container");
		ScrollPane scroll = new ScrollPane(content);
		scroll.setFitToWidth(true);
		scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
		return scroll;
	}

	public static void copyToClipboard(String text) {
		Clipboard clipboard = Clipboard.getSystemClipboard();
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		clipboard.setContent(content);
	}
}
