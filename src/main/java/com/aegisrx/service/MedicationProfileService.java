package com.aegisrx.service;

import com.aegisrx.dao.MedicationDAO;
import com.aegisrx.dao.DrugProfileDAO;
import com.aegisrx.domain.Medication;
import com.aegisrx.domain.DrugProfile;

import java.sql.SQLException;
import java.util.List;

// core business logic
public class MedicationProfileService {

	private final MedicationDAO medicationDAO;
	private final DrugProfileDAO drugProfileDAO;

	public MedicationProfileService() {
		this.medicationDAO = new MedicationDAO();
		this.drugProfileDAO = new DrugProfileDAO();
	}

public Medication addMedication(int patientUserId, int drugId, String dosage, String frequency) throws SQLException {
		// Validate drug exists in system
		DrugProfile drug = drugProfileDAO.findById(drugId);
		if (drug == null) {
			throw new IllegalArgumentException("Medication not found in verified database.");
		}

		// Check for duplicates
		if (medicationDAO.isDuplicate(patientUserId, drugId)) {
			throw new IllegalStateException("This medication is already in your active profile. Update dosage instead.");
		}

		Medication med = new Medication(patientUserId, drugId, drug.getDrugName(), dosage, frequency);
		medicationDAO.insert(med);

		System.out.println("[MedProfileService] Added " + drug.getDrugName() + " to patient " + patientUserId);
		return med;
	}

	public boolean updateDosage(int medId, String dosage, String frequency) throws SQLException {
		return medicationDAO.updateDosage(medId, dosage, frequency);
	}

	public boolean removeMedication(int medId) throws SQLException {
		return medicationDAO.deactivate(medId);
	}

	public List<Medication> getActiveMedications(int patientUserId) throws SQLException {
		return medicationDAO.findActiveByPatient(patientUserId);
	}

	public List<DrugProfile> searchDrugs(String name) throws SQLException {
		return drugProfileDAO.searchByName(name);
	}

	public List<DrugProfile> getAllDrugs() throws SQLException {
		return drugProfileDAO.findAll();
	}
}
