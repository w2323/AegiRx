package com.aegisrx.service;

import com.aegisrx.dao.BatchDAO;
import com.aegisrx.dao.PharmacyDAO;
import com.aegisrx.dao.DrugProfileDAO;
import com.aegisrx.domain.MedicineBatch;
import com.aegisrx.domain.DrugProfile;
import com.aegisrx.domain.enums.BatchStatus;
import com.aegisrx.util.GBIGenerator;
import com.aegisrx.util.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// core business logic
public class BatchService {

	private final BatchDAO batchDAO;
	private final PharmacyDAO pharmacyDAO;
	private final DrugProfileDAO drugProfileDAO;

	public BatchService() {
		this.batchDAO = new BatchDAO();
		this.pharmacyDAO = new PharmacyDAO();
		this.drugProfileDAO = new DrugProfileDAO();
	}

	// ---- UC1: Register Medicine Batch ----
	// I wrote this part to handle when a manufacturer makes a new batch of medicine.
	public MedicineBatch registerBatch(int drugId, int manufacturerId,
										LocalDate productionDate, LocalDate expiryDate,
										int quantity) throws SQLException {

		// I have to check the dates here. Medicine shouldn't expire before it's even made!
		if (!ValidationUtils.isValidExpiryDate(productionDate, expiryDate)) {
			throw new IllegalArgumentException("Expiry date must be after production date and in the future.");
		}

		// Also, it's impossible to make zero or negative medicine, so I'm blocking that.
		if (!ValidationUtils.isPositiveQuantity(quantity)) {
			throw new IllegalArgumentException("Quantity must be positive.");
		}

		// Here I'm looking up the drug in the database to make sure it's a real one we know.
		DrugProfile drug = drugProfileDAO.findById(drugId);
		if (drug == null) {
			throw new IllegalArgumentException("Drug profile not found in database.");
		}

		// Now I create a new Batch object and fill it with all the info from the manufacturer.
		MedicineBatch batch = new MedicineBatch();
		batch.setGbi(GBIGenerator.generate()); // This generates a unique ID so we can track it.
		batch.setDrugId(drugId);
		batch.setDrugName(drug.getDrugName());
		batch.setManufacturerId(manufacturerId);
		batch.setProductionDate(productionDate);
		batch.setExpiryDate(expiryDate);
		batch.setQuantity(quantity);
		batch.setStatus(BatchStatus.ACTIVE);             // It starts as 'Active' since it's fresh.
		batch.setCurrentHolderId(manufacturerId);        // The manufacturer is the one holding it for now.

		// Finally, I save it to the database so it stays there.
		batchDAO.insert(batch);

		System.out.println("[BatchService] Registered batch: " + batch.getGbi());
		return batch;
	}

	// ---- UC2: Transfer Medicine Batch ----
	// This function is for moving medicine from the factory to a pharmacy.
	public void transferBatch(int batchId, int manufacturerId, int pharmacyId, int quantityToTransfer) throws SQLException {

		// First, I fetch the batch from the database so I can check its details.
		MedicineBatch batch = batchDAO.findById(batchId);

		// I added some checks here to make sure everything is okay before moving.
		if (batch == null) throw new IllegalArgumentException("Batch not found.");

		// I need to make sure the person trying to ship it is actually the owner.
		if (batch.getManufacturerId() != manufacturerId) {
			throw new IllegalStateException("Only the owning manufacturer can transfer this batch.");
		}

		// We only allow moving 'Active' medicine. If it's already sold or thrown away, we can't ship it.
		if (!batch.canTransfer()) {
			throw new IllegalStateException("Batch cannot be transferred from status: " + batch.getStatus());
		}

		// I check if the manufacturer actually has enough quantity to ship what they promised.
		if (batch.getQuantity() < quantityToTransfer) {
			throw new IllegalStateException("Insufficient quantity in batch. Available: " + batch.getQuantity());
		}

		// Just making sure the pharmacy is actually registered in our system.
		if (!pharmacyDAO.exists(pharmacyId)) {
			throw new IllegalArgumentException("Pharmacy ID " + pharmacyId + " not found in AegisRx database.");
		}

		// If they are sending everything, I just change the status to 'In Transit'.
		if (quantityToTransfer == batch.getQuantity()) {
			batch.markInTransit(pharmacyId); // This updates the internal state.
			batchDAO.updateStatus(batchId, BatchStatus.IN_TRANSIT, pharmacyId);
			batchDAO.insertTransfer(batchId, manufacturerId, pharmacyId); // Keep a record of the move.
		} 
		else {
			// This is the cool part: if they only send SOME, I "split" the batch.
			int remainingQty = batch.getQuantity() - quantityToTransfer;
			batch.setQuantity(remainingQty); // The original batch gets smaller.
			batchDAO.update(batch);          // Save the new smaller quantity.

			// Then I create a brand new "sub-batch" for the units that are actually shipping.
			MedicineBatch subBatch = new MedicineBatch();

			// I add "-S" and a timestamp to the ID so it's unique but linked to the original.
			subBatch.setGbi(batch.getGbi() + "-S" + (System.currentTimeMillis() % 1000)); 
			subBatch.setDrugId(batch.getDrugId());
			subBatch.setDrugName(batch.getDrugName());
			subBatch.setManufacturerId(manufacturerId);
			subBatch.setProductionDate(batch.getProductionDate());
			subBatch.setExpiryDate(batch.getExpiryDate());
			subBatch.setQuantity(quantityToTransfer); // This is just the portion being moved.
			subBatch.setStatus(BatchStatus.IN_TRANSIT);
			subBatch.setCurrentHolderId(pharmacyId);   // Now the pharmacy is the holder.

			// Save the new sub-batch and record the transfer history.
			int newId = batchDAO.insert(subBatch);
			batchDAO.insertTransfer(newId, manufacturerId, pharmacyId);
		}
	}

	// ---- UC3: Verify Medicine Authenticity ----
	// I made this to help people check if their medicine is fake or real.
	public MedicineBatch verifyAuthenticity(String gbi) throws SQLException {
		// I look for the unique GBI code in the database.
		MedicineBatch batch = batchDAO.findByGbi(gbi);

		// If it's not there, I warn that it might be a counterfeit!
		if (batch == null) {
			System.out.println("[BatchService] ⚠ SUSPECTED COUNTERFEIT - Batch not found: " + gbi);
			return null; 
		}

		// If I found it, I return the batch info to show it's authentic.
		System.out.println("[BatchService] Batch verified: " + batch.getGbi() + " Status: " + batch.getStatus());
		return batch;
	}

	public List<String> getChainOfCustody(int batchId) throws SQLException {
		return batchDAO.getChainOfCustody(batchId);
	}

	public List<MedicineBatch> getBatchesByManufacturer(int manufacturerId) throws SQLException {
		return batchDAO.findByManufacturer(manufacturerId);
	}

	public List<MedicineBatch> getActiveBatches(int manufacturerId) throws SQLException {
		return batchDAO.findByManufacturerAndStatus(manufacturerId, BatchStatus.ACTIVE);
	}

	public List<DrugProfile> getDrugProfilesForManufacturer(int manufacturerId) throws SQLException {
		return drugProfileDAO.findByManufacturer(manufacturerId);
	}

	public List<DrugProfile> getAllDrugProfiles() throws SQLException {
		return drugProfileDAO.findAll();
	}
}
