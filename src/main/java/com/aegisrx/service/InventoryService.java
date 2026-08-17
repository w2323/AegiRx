package com.aegisrx.service;

import com.aegisrx.dao.BatchDAO;
import com.aegisrx.dao.DietaryAlertDAO;
import com.aegisrx.dao.PharmacyDAO;
import com.aegisrx.domain.MedicineBatch;
import com.aegisrx.domain.enums.BatchStatus;
import com.aegisrx.domain.enums.DisposalReason;

import java.sql.*;
import java.util.List;

// service layer stuff
public class InventoryService {

	private final BatchDAO batchDAO;
	private final PharmacyDAO pharmacyDAO;
	private final DietaryAlertDAO alertDAO;

	public InventoryService() {
		this.batchDAO = new BatchDAO();
		this.pharmacyDAO = new PharmacyDAO();
		this.alertDAO = new DietaryAlertDAO();
	}

	// ---- UC4: Update Pharmacy Inventory ----
	public void addToInventory(int batchId, int pharmacyId) throws SQLException {
		MedicineBatch batch = batchDAO.findById(batchId);

		if (batch == null) throw new IllegalArgumentException("Batch not found.");
		if (!batch.canAddToInventory()) {
			throw new IllegalStateException("Batch is not In Transit. Current status: " + batch.getStatus());
		}
		if (batch.getCurrentHolderId() != pharmacyId) {
			throw new IllegalStateException("Batch was not destined for this pharmacy. Logistics error logged.");
		}

		// Update batch status using Information Expert
		batch.markInStock();
		batchDAO.updateStatus(batchId, BatchStatus.IN_STOCK);

		// Add to pharmacy_inventory
		Connection conn = com.aegisrx.dao.DatabaseConnectionManager.getInstance().getConnection();
		String sql = "INSERT INTO pharmacy_inventory (pharmacy_id, batch_id, drug_id, quantity) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, pharmacyId);
			ps.setInt(2, batchId);
			ps.setInt(3, batch.getDrugId());
			ps.setInt(4, batch.getQuantity());
			ps.executeUpdate();
		}

		System.out.println("[InventoryService] Batch " + batch.getGbi() + " added to pharmacy " + pharmacyId + " inventory.");
	}

	// ---- UC5: Mark Medicine as Destroyed ----
	public void destroyMedicine(int batchId, int pharmacyId, DisposalReason reason) throws SQLException {
		MedicineBatch batch = batchDAO.findById(batchId);

		if (batch == null) throw new IllegalArgumentException("Batch not found.");
		if (!batch.canDestroy()) {
			throw new IllegalStateException("Batch cannot be destroyed from status: " + batch.getStatus());
		}
		if (batch.getCurrentHolderId() != pharmacyId) {
			throw new IllegalStateException("Batch belongs to a different facility. Destruction blocked.");
		}

		// Update using Information Expert
		batch.markDestroyed();
		batchDAO.updateStatus(batchId, BatchStatus.DESTROYED);

		// Remove from inventory
		Connection conn = com.aegisrx.dao.DatabaseConnectionManager.getInstance().getConnection();
		String sql = "DELETE FROM pharmacy_inventory WHERE batch_id = ? AND pharmacy_id = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, batchId);
			ps.setInt(2, pharmacyId);
			ps.executeUpdate();
		}

		// Log disposal
		alertDAO.insertDisposalLog(batchId, pharmacyId, reason.name());

		System.out.println("[InventoryService] Batch " + batch.getGbi() + " destroyed. Reason: " + reason);
	}

	public List<MedicineBatch> getInTransitBatches(int pharmacyId) throws SQLException {
		// Find batches IN_TRANSIT destined for this pharmacy
		Connection conn = com.aegisrx.dao.DatabaseConnectionManager.getInstance().getConnection();
		java.util.List<MedicineBatch> batches = new java.util.ArrayList<>();
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id " +
					 "WHERE b.status = 'IN_TRANSIT' AND b.current_holder = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, pharmacyId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MedicineBatch b = new MedicineBatch();
				b.setBatchId(rs.getInt("batch_id"));
				b.setGbi(rs.getString("gbi"));
				b.setDrugId(rs.getInt("drug_id"));
				try { b.setDrugName(rs.getString("drug_name")); } catch (SQLException ignored) {}
				b.setQuantity(rs.getInt("quantity"));
				b.setStatus(BatchStatus.valueOf(rs.getString("status")));
				b.setCurrentHolderId(rs.getInt("current_holder"));
				Date ed = rs.getDate("expiry_date");
				if (ed != null) b.setExpiryDate(ed.toLocalDate());
				batches.add(b);
			}
		}
		return batches;
	}

	public List<MedicineBatch> getInStockBatches(int pharmacyId) throws SQLException {
		Connection conn = com.aegisrx.dao.DatabaseConnectionManager.getInstance().getConnection();
		java.util.List<MedicineBatch> batches = new java.util.ArrayList<>();
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id " +
					 "WHERE b.status = 'IN_STOCK' AND b.current_holder = ?";
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, pharmacyId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				MedicineBatch b = new MedicineBatch();
				b.setBatchId(rs.getInt("batch_id"));
				b.setGbi(rs.getString("gbi"));
				b.setDrugId(rs.getInt("drug_id"));
				try { b.setDrugName(rs.getString("drug_name")); } catch (SQLException ignored) {}
				b.setQuantity(rs.getInt("quantity"));
				b.setStatus(BatchStatus.valueOf(rs.getString("status")));
				b.setCurrentHolderId(rs.getInt("current_holder"));
				Date ed = rs.getDate("expiry_date");
				if (ed != null) b.setExpiryDate(ed.toLocalDate());
				batches.add(b);
			}
		}
		return batches;
	}
}
