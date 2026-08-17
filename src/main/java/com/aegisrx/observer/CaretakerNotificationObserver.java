package com.aegisrx.observer;

import com.aegisrx.domain.DietaryAlert;

// handles events
public class CaretakerNotificationObserver implements AlertObserver {

	@Override
	public void onAlert(DietaryAlert alert) {
		// In a real system, this would send an SMS/email to caretaker
		System.out.println("[CARETAKER ALERT] Patient " + alert.getPatientUserId() +
			" received " + alert.getSeverity() + " dietary alert: " + alert.getReason());

		if (alert.getSeverity().ordinal() >= 2) {  // HIGH or CRITICAL
			System.out.println("[CARETAKER ALERT] ⚠ HIGH PRIORITY - Notifying caretaker immediately.");
		}
	}
}
