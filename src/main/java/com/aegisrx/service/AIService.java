package com.aegisrx.service;

import com.aegisrx.ai.GeminiClient;
import com.aegisrx.ai.PromptTemplates;
import com.aegisrx.dao.MedicationDAO;
import com.aegisrx.domain.Medication;
import com.aegisrx.util.AppConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

// core business logic
public class AIService {

	private final GeminiClient geminiClient;
	private final MedicationDAO medicationDAO;

	public AIService() {
		this.geminiClient = new GeminiClient();
		this.medicationDAO = new MedicationDAO();
	}

public String askQuestion(int patientUserId, String question) throws SQLException {
		if (!AppConfig.isGeminiEnabled()) {
			return "AI assistant is not available. Please configure a Gemini API key in application.properties.";
		}

		// Build patient context
		List<Medication> meds = medicationDAO.findActiveByPatient(patientUserId);
		String context = buildPatientContext(meds);

		String prompt = PromptTemplates.smartSuggestionPrompt(context, question);
		String response = geminiClient.generateContent(prompt);

		if (response == null) {
			return "The AI assistant is currently receiving too many requests. Please wait about 30 seconds and try again!";
		}

		return response;
	}

public String getDietaryRecommendation(int patientUserId) throws SQLException {
		if (!AppConfig.isGeminiEnabled()) {
			return getLocalDietaryRecommendation(patientUserId);
		}

		List<Medication> meds = medicationDAO.findActiveByPatient(patientUserId);
		String context = buildPatientContext(meds);

		String prompt = "Based on the following patient profile:\n" + context + 
			"\n\nProvide 5 specific dietary recommendations and 5 foods to avoid. " +
			"Format as numbered lists. Be concise and actionable.";

		String response = geminiClient.generateContent(prompt);
		return response != null ? response : getLocalDietaryRecommendation(patientUserId);
	}

	private String buildPatientContext(List<Medication> meds) {
		if (meds.isEmpty()) return "No active medications.";

		return "Active Medications:\n" + meds.stream()
			.map(m -> "- " + m.getDrugName() + " (" + m.getDosage() + ", " + m.getFrequency() + ")")
			.collect(Collectors.joining("\n"));
	}

	private String getLocalDietaryRecommendation(int patientUserId) {
		return "General Dietary Recommendations:\n" +
			   "1. Maintain a balanced diet rich in fruits and vegetables\n" +
			   "2. Stay hydrated - drink at least 8 glasses of water daily\n" +
			   "3. Avoid grapefruit if taking statins\n" +
			   "4. Limit sodium intake for blood pressure medications\n" +
			   "5. Maintain consistent Vitamin K intake if on anticoagulants\n\n" +
			   "For personalized AI recommendations, configure the Gemini API key.";
	}

	public boolean isAIAvailable() {
		return AppConfig.isGeminiEnabled();
	}
}
