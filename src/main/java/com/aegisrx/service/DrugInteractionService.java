package com.aegisrx.service;

import com.aegisrx.ai.*;
import com.aegisrx.dao.MedicationDAO;
import com.aegisrx.domain.InteractionReport;
import com.aegisrx.domain.Medication;
import com.aegisrx.util.AppConfig;

import java.sql.SQLException;
import java.util.List;

// service layer stuff
public class DrugInteractionService {

	private final MedicationDAO medicationDAO;
	private InteractionCheckStrategy strategy;

	public DrugInteractionService() {
		this.medicationDAO = new MedicationDAO();

		// Select strategy based on config
		if (AppConfig.isGeminiEnabled()) {
			this.strategy = new AIInteractionChecker();
		} else {
			this.strategy = new LocalInteractionChecker();
		}
	}

public List<InteractionReport> checkPatientInteractions(int patientUserId) throws SQLException {
		List<Medication> meds = medicationDAO.findActiveByPatient(patientUserId);

		if (meds.size() < 2) {
			System.out.println("[InteractionService] Less than 2 active medications. No check needed.");
			return List.of();
		}

		System.out.println("[InteractionService] Checking interactions using: " + strategy.getStrategyName());
		return strategy.checkInteractions(meds);
	}

public List<InteractionReport> checkInteractions(List<Medication> medications) {
		return strategy.checkInteractions(medications);
	}

public void setStrategy(InteractionCheckStrategy strategy) {
		this.strategy = strategy;
	}

	public String getCurrentStrategyName() {
		return strategy.getStrategyName();
	}
}
