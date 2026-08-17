package com.aegisrx.service;

import com.aegisrx.dao.*;
import com.aegisrx.domain.*;
import com.aegisrx.domain.enums.BatchStatus;

import java.sql.SQLException;
import java.util.List;

// core business logic
public class SaleService {

	private final BatchDAO batchDAO;
	private final SaleDAO saleDAO;
	private final MedicationDAO medicationDAO;
	private final DrugInteractionService interactionService;

	public SaleService() {
		this.batchDAO = new BatchDAO();
		this.saleDAO = new SaleDAO();
		this.medicationDAO = new MedicationDAO();
		this.interactionService = new DrugInteractionService();
	}

public SaleRecord processSale(int batchId, int pharmacyId, int patientUserId, int quantitySold) throws SQLException {

		// First, I need to find the batch in the system to make sure it exists.
		MedicineBatch batch = batchDAO.findById(batchId);
		if (batch == null) throw new IllegalArgumentException("Batch not found.");

		// I'm blocking the sale if the medicine is expired. Safety first!
		if (batch.isExpired()) throw new IllegalStateException("⚠ Medicine is EXPIRED. Sale blocked.");
		
		// I also check if the status is okay for selling.
		if (!batch.canSell()) {
			throw new IllegalStateException("Medicine cannot be sold. Status: " + batch.getStatus());
		}
		
		// We have to check if the pharmacy actually has enough units for the patient.
		if (batch.getQuantity() < quantitySold) {
			throw new IllegalStateException("Insufficient stock in batch. Available: " + batch.getQuantity());
		}

		// Safety Check: I'm pulling the patient's current medications from the database.
		List<Medication> patientMeds = medicationDAO.findActiveByPatient(patientUserId);

		// Then I add the one they want to buy to see if it causes any bad reactions.
		Medication newMed = new Medication();
		newMed.setDrugId(batch.getDrugId());
		newMed.setDrugName(batch.getDrugName());
		patientMeds.add(newMed);

		// I run the interaction check here.
		List<InteractionReport> interactions = interactionService.checkInteractions(patientMeds);

		// If there's a serious interaction, I block the sale. 
		boolean hasSevere = interactions.stream()
			.anyMatch(r -> r.getSeverity().ordinal() >= 2);  // High or Critical severity.

		if (hasSevere) {
			throw new IllegalStateException("⚠ SEVERE DRUG INTERACTION DETECTED. Review required before sale.");
		}

		// Updating the stock:
		// I subtract the sold quantity from the current batch quantity.
		int remainingQty = batch.getQuantity() - quantitySold;
		batch.setQuantity(remainingQty);
		
		// If the batch is completely gone, I mark it as 'SOLD'.
		if (remainingQty <= 0) {
			batch.markSold();
			batchDAO.updateStatus(batchId, BatchStatus.SOLD);
		}

		// Save the update back to the database.
		batchDAO.update(batch);

		// Inventory management:
		java.sql.Connection conn = DatabaseConnectionManager.getInstance().getConnection();

		if (remainingQty <= 0) {
			// If the batch is empty, I just delete the inventory record.
			String sql = "DELETE FROM pharmacy_inventory WHERE batch_id = ? AND pharmacy_id = ?";
			try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, batchId);
				ps.setInt(2, pharmacyId);
				ps.executeUpdate();
			}
		} 
		else {
			// Otherwise, I just update the number of tablets left in the pharmacy.
			String sql = "UPDATE pharmacy_inventory SET quantity = ? WHERE batch_id = ? AND pharmacy_id = ?";
			try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setInt(1, remainingQty);
				ps.setInt(2, batchId);
				ps.setInt(3, pharmacyId);
				ps.executeUpdate();
			}
		}

		// I add the medicine to the patient's profile so they can track it in their portal.
		Medication med = new Medication(patientUserId, batch.getDrugId(), batch.getDrugName(), String.valueOf(quantitySold), "As prescribed");
		medicationDAO.insert(med);

		// Finally, I create a record for the sale history.
		SaleRecord sale = new SaleRecord();
		sale.setPharmacyId(pharmacyId);
		sale.setPatientUserId(patientUserId);
		sale.setBatchId(batchId);
		saleDAO.insert(sale);

		return sale;
	}

public List<InteractionReport> preCheckInteractions(int batchId, int patientUserId) throws SQLException {
		MedicineBatch batch = batchDAO.findById(batchId);
		List<Medication> patientMeds = medicationDAO.findActiveByPatient(patientUserId);
		Medication newMed = new Medication();
		newMed.setDrugId(batch.getDrugId());
		newMed.setDrugName(batch.getDrugName());
		patientMeds.add(newMed);
		return interactionService.checkInteractions(patientMeds);
	}

	public List<SaleRecord> getSalesByPharmacy(int pharmacyId) throws SQLException {
		return saleDAO.findByPharmacy(pharmacyId);
	}

	public List<SaleRecord> getSalesByPatient(int patientUserId) throws SQLException {
		return saleDAO.findByPatient(patientUserId);
	}
}
