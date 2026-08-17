package com.aegisrx.ai;

import com.aegisrx.domain.InteractionReport;
import com.aegisrx.domain.Medication;
import com.aegisrx.domain.enums.AlertSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// integration with ai
public class AIInteractionChecker implements InteractionCheckStrategy {

	private final GeminiClient geminiClient;

	public AIInteractionChecker() {
		this.geminiClient = new GeminiClient();
	}

	@Override
	public List<InteractionReport> checkInteractions(List<Medication> medications) {
		if (medications == null || medications.size() < 2) {
			return new ArrayList<>();
		}

		String medList = medications.stream()
			.map(m -> m.getDrugName() + " (" + m.getDosage() + ", " + m.getFrequency() + ")")
			.collect(Collectors.joining("\n- ", "- ", ""));

		String prompt = PromptTemplates.drugInteractionPrompt(medList);
		String aiResponse = geminiClient.generateContent(prompt);

		if (aiResponse == null) {
			System.out.println("[AI] Falling back to local interaction check.");
			return new LocalInteractionChecker().checkInteractions(medications);
		}

		return parseAIResponse(aiResponse);
	}

	@Override
	public String getStrategyName() {
		return "AI-Powered (Gemini)";
	}

	private List<InteractionReport> parseAIResponse(String response) {
		List<InteractionReport> reports = new ArrayList<>();

		if (response.toLowerCase().contains("no known interactions")) {
			return reports;
		}

		// Parse structured response - look for drug pairs
		String[] sections = response.split("(?=Drug Pair:|\\d+\\.)");
		for (String section : sections) {
			if (section.trim().isEmpty()) continue;

			InteractionReport report = new InteractionReport();
			
			// Extract severity
			if (section.contains("CRITICAL")) report.setSeverity(AlertSeverity.CRITICAL);
			else if (section.contains("HIGH")) report.setSeverity(AlertSeverity.HIGH);
			else if (section.contains("MEDIUM")) report.setSeverity(AlertSeverity.MEDIUM);
			else if (section.contains("LOW")) report.setSeverity(AlertSeverity.LOW);
			else continue;  // Skip if no severity found

			// Extract drug names from "Drug Pair:" line or "↔" separator
			if (section.contains("↔")) {
				String pairLine = section.substring(section.indexOf("↔") - 30, 
					Math.min(section.indexOf("↔") + 30, section.length()));
				String[] parts = pairLine.split("↔");
				if (parts.length == 2) {
					report.setDrugName1(parts[0].replaceAll("[^a-zA-Z\\s]", "").trim());
					report.setDrugName2(parts[1].replaceAll("[^a-zA-Z\\s]", "").trim());
				}
			}

			report.setDescription(section.trim());
			report.setRecommendation("");

			// Extract recommendation if present
			if (section.contains("Recommendation:")) {
				report.setRecommendation(
					section.substring(section.indexOf("Recommendation:") + 15).trim()
				);
			}

			if (report.getDrugName1() != null) {
				reports.add(report);
			}
		}

		// If parsing failed but response exists, create a generic report
		if (reports.isEmpty() && !response.trim().isEmpty()) {
			InteractionReport generic = new InteractionReport();
			generic.setDrugName1("Analysis");
			generic.setDrugName2("Result");
			generic.setSeverity(AlertSeverity.MEDIUM);
			generic.setDescription(response);
			generic.setRecommendation("Please review the AI analysis above.");
			reports.add(generic);
		}

		return reports;
	}
}
