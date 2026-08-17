package com.aegisrx.ai;

// prompt handling
public class PromptTemplates {

	public static String drugInteractionPrompt(String medicationList) {
		return "You are a pharmacology safety assistant for the AegisRx healthcare system. " +
			   "Given the following list of active medications a patient is taking:\n\n" +
			   medicationList + "\n\n" +
			   "Analyze ALL possible pairwise drug-drug interactions. For each interaction found, " +
			   "provide the following in a structured format:\n" +
			   "- Drug Pair: [Drug A] ↔ [Drug B]\n" +
			   "- Severity: [LOW / MEDIUM / HIGH / CRITICAL]\n" +
			   "- Description: [Brief explanation of the interaction mechanism]\n" +
			   "- Recommendation: [What the patient should do]\n\n" +
			   "If no interactions are found, state 'No Known Interactions Detected.' " +
			   "Be thorough but concise. This is for patient safety - accuracy is critical.";
	}

	public static String dietaryConflictPrompt(String conditions, String medications, String foodItem, String foodCategory) {
		return "You are a dietary safety assistant for the AegisRx healthcare system. " +
			   "A patient has the following health conditions: " + conditions + "\n" +
			   "They are currently taking these medications: " + medications + "\n\n" +
			   "The patient just logged eating: " + foodItem + " (Category: " + foodCategory + ")\n\n" +
			   "Analyze if this food item conflicts with ANY of their medications or health conditions. " +
			   "Consider:\n" +
			   "1. Drug-food interactions (e.g., grapefruit with statins, vitamin K with warfarin)\n" +
			   "2. Condition-based dietary restrictions (e.g., high-sugar foods for diabetics, " +
			   "high-sodium for hypertension patients)\n\n" +
			   "Respond with:\n" +
			   "- Conflict Found: [YES / NO]\n" +
			   "- Severity: [LOW / MEDIUM / HIGH / CRITICAL]\n" +
			   "- Conflicting With: [medication name or condition]\n" +
			   "- Reason: [Brief explanation]\n" +
			   "- Dietary Recommendation: [What to eat instead or precautions]\n\n" +
			   "Be specific and actionable. Patient safety depends on this analysis.";
	}

	public static String smartSuggestionPrompt(String patientContext, String userQuestion) {
		return "You are a helpful AI health assistant integrated into the AegisRx patient safety system. " +
			   "The patient's current profile:\n" + patientContext + "\n\n" +
			   "The patient asks: \"" + userQuestion + "\"\n\n" +
			   "Provide a helpful, concise answer focused on medication safety and dietary guidance. " +
			   "Always recommend consulting a healthcare professional for serious concerns. " +
			   "Keep your response under 200 words.";
	}
}
