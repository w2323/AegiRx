package com.aegisrx.ai;

import com.aegisrx.domain.InteractionReport;
import com.aegisrx.domain.Medication;
import com.aegisrx.domain.enums.AlertSeverity;

import java.util.*;

// ai related stuff
public class LocalInteractionChecker implements InteractionCheckStrategy {

	// Known interaction rules (drug name -> conflicting drug name -> severity + description)
	private static final Map<String, Map<String, InteractionRule>> RULES = new HashMap<>();

	static {
		addRule("Warfarin", "Aspirin", AlertSeverity.HIGH,
			"Concurrent use increases bleeding risk significantly.",
			"Avoid combining. Consult doctor for alternative anticoagulant.");

		addRule("Warfarin", "Ibuprofen", AlertSeverity.HIGH,
			"NSAIDs increase anticoagulant effect and GI bleeding risk.",
			"Use acetaminophen instead of ibuprofen when possible.");

		addRule("Metformin", "Insulin", AlertSeverity.MEDIUM,
			"Combined use may cause hypoglycemia.",
			"Monitor blood sugar closely. Adjust dosage under medical supervision.");

		addRule("Lisinopril", "Potassium Supplements", AlertSeverity.HIGH,
			"ACE inhibitors + potassium can cause hyperkalemia.",
			"Monitor potassium levels regularly.");

		addRule("Simvastatin", "Amlodipine", AlertSeverity.MEDIUM,
			"Amlodipine increases simvastatin levels. Risk of myopathy.",
			"Limit simvastatin dose to 20mg/day when combined.");

		addRule("Methotrexate", "Ibuprofen", AlertSeverity.CRITICAL,
			"NSAIDs reduce methotrexate clearance. Risk of toxicity.",
			"Avoid combination. Life-threatening toxicity possible.");

		addRule("Ciprofloxacin", "Antacids", AlertSeverity.MEDIUM,
			"Antacids reduce ciprofloxacin absorption.",
			"Take ciprofloxacin 2 hours before or 6 hours after antacids.");

		addRule("Fluoxetine", "Tramadol", AlertSeverity.CRITICAL,
			"Risk of serotonin syndrome - potentially fatal.",
			"Do NOT combine. Seek immediate alternative.");

		addRule("Atorvastatin", "Clarithromycin", AlertSeverity.HIGH,
			"Clarithromycin increases statin levels. Risk of rhabdomyolysis.",
			"Use azithromycin as alternative antibiotic.");

		addRule("Metformin", "Alcohol", AlertSeverity.HIGH,
			"Alcohol increases risk of lactic acidosis with metformin.",
			"Limit alcohol intake significantly.");
	}

	private static void addRule(String drug1, String drug2, AlertSeverity severity,
								String description, String recommendation) {
		RULES.computeIfAbsent(drug1.toLowerCase(), k -> new HashMap<>())
			 .put(drug2.toLowerCase(), new InteractionRule(severity, description, recommendation));
		RULES.computeIfAbsent(drug2.toLowerCase(), k -> new HashMap<>())
			 .put(drug1.toLowerCase(), new InteractionRule(severity, description, recommendation));
	}

	@Override
	public List<InteractionReport> checkInteractions(List<Medication> medications) {
		List<InteractionReport> reports = new ArrayList<>();

		for (int i = 0; i < medications.size(); i++) {
			for (int j = i + 1; j < medications.size(); j++) {
				String drug1 = medications.get(i).getDrugName().toLowerCase();
				String drug2 = medications.get(j).getDrugName().toLowerCase();

				Map<String, InteractionRule> drug1Rules = RULES.get(drug1);
				if (drug1Rules != null && drug1Rules.containsKey(drug2)) {
					InteractionRule rule = drug1Rules.get(drug2);
					reports.add(new InteractionReport(
						medications.get(i).getDrugName(),
						medications.get(j).getDrugName(),
						rule.severity,
						rule.description,
						rule.recommendation
					));
				}
			}
		}
		return reports;
	}

	@Override
	public String getStrategyName() {
		return "Local Database Lookup";
	}

	private static class InteractionRule {
		AlertSeverity severity;
		String description;
		String recommendation;

		InteractionRule(AlertSeverity severity, String description, String recommendation) {
			this.severity = severity;
			this.description = description;
			this.recommendation = recommendation;
		}
	}
}
