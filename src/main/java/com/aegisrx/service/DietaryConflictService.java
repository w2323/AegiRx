package com.aegisrx.service;

import com.aegisrx.ai.GeminiClient;
import com.aegisrx.ai.PromptTemplates;
import com.aegisrx.dao.*;
import com.aegisrx.domain.*;
import com.aegisrx.domain.enums.AlertSeverity;
import com.aegisrx.observer.AlertObserver;
import com.aegisrx.util.AppConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// processing logic
public class DietaryConflictService {

	private final FoodEntryDAO foodEntryDAO;
	private final MedicationDAO medicationDAO;
	private final DietaryAlertDAO alertDAO;
	private final GeminiClient geminiClient;
	private final List<AlertObserver> observers;

	public DietaryConflictService() {
		this.foodEntryDAO = new FoodEntryDAO();
		this.medicationDAO = new MedicationDAO();
		this.alertDAO = new DietaryAlertDAO();
		this.geminiClient = new GeminiClient();
		this.observers = new ArrayList<>();
	}

	// ---- Observer pattern methods ----
	public void addObserver(AlertObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(AlertObserver observer) {
		observers.remove(observer);
	}

	private void notifyObservers(DietaryAlert alert) {
		for (AlertObserver observer : observers) {
			observer.onAlert(alert);
		}
	}

	// ---- UC9: Log Food Intake ----
	public FoodEntry logFood(int patientUserId, String foodName, String foodCategory, 
							  String nutritionalProps) throws SQLException {
		FoodEntry entry = new FoodEntry(patientUserId, foodName, foodCategory, nutritionalProps);
		foodEntryDAO.insert(entry);

		System.out.println("[DietaryService] Food logged: " + foodName);

		// Trigger dietary conflict check
		checkDietaryConflict(patientUserId, entry);

		return entry;
	}

	// ---- UC10: Receive Dietary Conflict Alert ----
	private void checkDietaryConflict(int patientUserId, FoodEntry entry) throws SQLException {
		List<Medication> meds = medicationDAO.findActiveByPatient(patientUserId);
		if (meds.isEmpty()) return;

		String medsStr = formatMedications(meds);

		if (AppConfig.isGeminiEnabled()) {
			// Use AI for dietary conflict analysis
			String prompt = PromptTemplates.dietaryConflictPrompt(
				"Patient conditions", medsStr, entry.getFoodName(), entry.getFoodCategory());

			String aiResponse = geminiClient.generateContent(prompt);
			if (aiResponse != null && aiResponse.toLowerCase().contains("yes")) {
				DietaryAlert alert = createAlertFromAI(patientUserId, entry, aiResponse);
				alertDAO.insert(alert);
				notifyObservers(alert);
				checkRepeatedIgnoredAlerts(patientUserId);
			}
		} else {
			// Use local rules
			DietaryAlert alert = checkLocalRules(patientUserId, entry, meds);
			if (alert != null) {
				alertDAO.insert(alert);
				notifyObservers(alert);
				checkRepeatedIgnoredAlerts(patientUserId);
			}
		}
	}

	private DietaryAlert checkLocalRules(int patientUserId, FoodEntry entry, List<Medication> meds) {
		String food = entry.getFoodName().toLowerCase();
		String category = entry.getFoodCategory() != null ? entry.getFoodCategory().toLowerCase() : "";

		for (Medication med : meds) {
			String drug = med.getDrugName().toLowerCase();

			// Warfarin + Vitamin K foods (spinach, kale, broccoli)
			if (drug.contains("warfarin") && 
				(food.contains("spinach") || food.contains("kale") || food.contains("broccoli"))) {
				return createAlert(patientUserId, entry, med, AlertSeverity.HIGH,
					"Vitamin K in " + entry.getFoodName() + " can reduce Warfarin effectiveness. " +
					"Avoid high Vitamin K foods while on anticoagulant therapy.");
			}

			// Statins + Grapefruit
			if ((drug.contains("statin") || drug.contains("simvastatin") || drug.contains("atorvastatin")) 
				&& food.contains("grapefruit")) {
				return createAlert(patientUserId, entry, med, AlertSeverity.HIGH,
					"Grapefruit increases statin blood levels, raising risk of muscle damage (rhabdomyolysis). " +
					"Avoid grapefruit and grapefruit juice.");
			}

			// MAO Inhibitors + Tyramine-rich foods (cheese, wine)
			if (drug.contains("maoi") && (food.contains("cheese") || food.contains("wine"))) {
				return createAlert(patientUserId, entry, med, AlertSeverity.CRITICAL,
					"Tyramine in " + entry.getFoodName() + " with MAO inhibitors can cause " +
					"dangerous blood pressure spike. AVOID immediately.");
			}

			// Metformin / Insulin + High sugar
			if ((drug.contains("metformin") || drug.contains("insulin")) && 
				(category.contains("sweet") || food.contains("candy") || food.contains("cake") || 
				 food.contains("sugar") || food.contains("chocolate"))) {
				return createAlert(patientUserId, entry, med, AlertSeverity.MEDIUM,
					"High-sugar food " + entry.getFoodName() + " may counteract " + med.getDrugName() + 
					" effects. Diabetic patients should limit sugar intake.");
			}

			// ACE inhibitors + high sodium
			if ((drug.contains("lisinopril") || drug.contains("enalapril")) && 
				(food.contains("chips") || food.contains("fries") || category.contains("salty"))) {
				return createAlert(patientUserId, entry, med, AlertSeverity.MEDIUM,
					"High-sodium food counteracts " + med.getDrugName() + " blood pressure control. " +
					"Hypertension patients should limit sodium intake.");
			}

			// Ciprofloxacin + Dairy
			if (drug.contains("ciprofloxacin") && 
				(food.contains("milk") || food.contains("yogurt") || food.contains("cheese"))) {
				return createAlert(patientUserId, entry, med, AlertSeverity.MEDIUM,
					"Dairy products reduce Ciprofloxacin absorption. Take medication 2 hours before " +
					"or 6 hours after consuming dairy.");
			}
		}

		return null;  // No conflict
	}

	private DietaryAlert createAlert(int patientUserId, FoodEntry entry, Medication med,
									  AlertSeverity severity, String reason) {
		DietaryAlert alert = new DietaryAlert();
		alert.setPatientUserId(patientUserId);
		alert.setFoodLogId(entry.getLogId());
		alert.setConflictingDrugId(med.getDrugId());
		alert.setConflictingDrugName(med.getDrugName());
		alert.setSeverity(severity);
		alert.setReason(reason);
		return alert;
	}

	private DietaryAlert createAlertFromAI(int patientUserId, FoodEntry entry, String aiResponse) {
		DietaryAlert alert = new DietaryAlert();
		alert.setPatientUserId(patientUserId);
		alert.setFoodLogId(entry.getLogId());
		alert.setReason(aiResponse);

		// Parse severity from AI response
		if (aiResponse.contains("CRITICAL")) alert.setSeverity(AlertSeverity.CRITICAL);
		else if (aiResponse.contains("HIGH")) alert.setSeverity(AlertSeverity.HIGH);
		else if (aiResponse.contains("MEDIUM")) alert.setSeverity(AlertSeverity.MEDIUM);
		else alert.setSeverity(AlertSeverity.LOW);

		return alert;
	}

	private void checkRepeatedIgnoredAlerts(int patientUserId) throws SQLException {
		int count = alertDAO.countRecentSevereIgnored(patientUserId);
		if (count >= 3) {
			System.out.println("[DietaryService] ⚠ Patient " + patientUserId + 
				" has " + count + " ignored severe alerts. WARNING FLAG added.");
		}
	}

	public boolean acknowledgeAlert(int alertId) throws SQLException {
		return alertDAO.acknowledge(alertId);
	}

	public List<DietaryAlert> getUnacknowledgedAlerts(int patientUserId) throws SQLException {
		return alertDAO.findUnacknowledged(patientUserId);
	}

	public List<FoodEntry> getTodaysFoodLog(int patientUserId) throws SQLException {
		return foodEntryDAO.findByPatientToday(patientUserId);
	}

	public List<FoodEntry> getFoodHistory(int patientUserId) throws SQLException {
		return foodEntryDAO.findByPatient(patientUserId);
	}

	private String formatMedications(List<Medication> meds) {
		StringBuilder sb = new StringBuilder();
		for (Medication m : meds) {
			sb.append(m.getDrugName()).append(" (").append(m.getDosage()).append("), ");
		}
		return sb.toString();
	}
}
