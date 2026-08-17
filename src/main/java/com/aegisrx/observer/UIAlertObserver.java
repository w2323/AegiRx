package com.aegisrx.observer;

import com.aegisrx.domain.DietaryAlert;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

// handles events
public class UIAlertObserver implements AlertObserver {

	@Override
	public void onAlert(DietaryAlert alert) {
		Platform.runLater(() -> {
			AlertType type;
			switch (alert.getSeverity()) {
				case CRITICAL:
				case HIGH:
					type = AlertType.ERROR;
					break;
				case MEDIUM:
					type = AlertType.WARNING;
					break;
				default:
					type = AlertType.INFORMATION;
			}

			Alert dialog = new Alert(type);
			dialog.setTitle("⚠ Dietary Conflict Alert");
			dialog.setHeaderText(alert.getSeverity().getDisplayName() + " Severity Alert");
			dialog.setContentText(alert.getReason() +
				(alert.getConflictingDrugName() != null ?
					"\n\nConflicting Medication: " + alert.getConflictingDrugName() : ""));

			dialog.getButtonTypes().setAll(
				new ButtonType("Acknowledge Warning"),
				ButtonType.CLOSE
			);

			dialog.showAndWait().ifPresent(btn -> {
				if (btn.getText().equals("Acknowledge Warning")) {
					alert.acknowledge();
				}
			});
		});
	}
}
