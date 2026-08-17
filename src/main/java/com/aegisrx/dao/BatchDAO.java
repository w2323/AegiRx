package com.aegisrx.dao;

import com.aegisrx.domain.MedicineBatch;
import com.aegisrx.domain.enums.BatchStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// dao implementation
public class BatchDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public MedicineBatch findById(int batchId) throws SQLException {
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id WHERE b.batch_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, batchId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public MedicineBatch findByGbi(String gbi) throws SQLException {
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id WHERE b.gbi = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, gbi);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public List<MedicineBatch> findByManufacturer(int manufacturerId) throws SQLException {
		List<MedicineBatch> batches = new ArrayList<>();
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id WHERE b.manufacturer_id = ? ORDER BY b.created_at DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, manufacturerId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) batches.add(mapRow(rs));
		}
		return batches;
	}

	public List<MedicineBatch> findByManufacturerAndStatus(int manufacturerId, BatchStatus status) throws SQLException {
		List<MedicineBatch> batches = new ArrayList<>();
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id " +
					 "WHERE b.manufacturer_id = ? AND b.status = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, manufacturerId);
			ps.setString(2, status.name());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) batches.add(mapRow(rs));
		}
		return batches;
	}

	public List<MedicineBatch> findByStatus(BatchStatus status) throws SQLException {
		List<MedicineBatch> batches = new ArrayList<>();
		String sql = "SELECT b.*, d.drug_name FROM medicine_batches b " +
					 "LEFT JOIN drug_profiles d ON b.drug_id = d.drug_id WHERE b.status = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, status.name());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) batches.add(mapRow(rs));
		}
		return batches;
	}

	public int insert(MedicineBatch batch) throws SQLException {
		String sql = "INSERT INTO medicine_batches (gbi, drug_id, manufacturer_id, production_date, expiry_date, quantity, status, current_holder) " +
					 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, batch.getGbi());
			ps.setInt(2, batch.getDrugId());
			ps.setInt(3, batch.getManufacturerId());
			ps.setDate(4, Date.valueOf(batch.getProductionDate()));
			ps.setDate(5, Date.valueOf(batch.getExpiryDate()));
			ps.setInt(6, batch.getQuantity());
			ps.setString(7, batch.getStatus().name());
			ps.setInt(8, batch.getCurrentHolderId());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				batch.setBatchId(id);
				return id;
			}
		}
		return -1;
	}

	public boolean update(MedicineBatch batch) throws SQLException {
		String sql = "UPDATE medicine_batches SET gbi = ?, quantity = ?, status = ?, current_holder = ? WHERE batch_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, batch.getGbi());
			ps.setInt(2, batch.getQuantity());
			ps.setString(3, batch.getStatus().name());
			ps.setInt(4, batch.getCurrentHolderId());
			ps.setInt(5, batch.getBatchId());
			return ps.executeUpdate() > 0;
		}
	}

	public boolean updateStatus(int batchId, BatchStatus status, int currentHolder) throws SQLException {
		String sql = "UPDATE medicine_batches SET status = ?, current_holder = ? WHERE batch_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setInt(2, currentHolder);
			ps.setInt(3, batchId);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean updateStatus(int batchId, BatchStatus status) throws SQLException {
		String sql = "UPDATE medicine_batches SET status = ? WHERE batch_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, status.name());
			ps.setInt(2, batchId);
			return ps.executeUpdate() > 0;
		}
	}

public List<String> getChainOfCustody(int batchId) throws SQLException {
		List<String> chain = new ArrayList<>();
		String sql = "SELECT bt.transfer_date, u.full_name AS from_name, p.pharmacy_name AS to_name, bt.status " +
					 "FROM batch_transfers bt " +
					 "JOIN users u ON bt.from_user_id = u.user_id " +
					 "JOIN pharmacies p ON bt.to_pharmacy_id = p.pharmacy_id " +
					 "WHERE bt.batch_id = ? ORDER BY bt.transfer_date";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, batchId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				chain.add(String.format("[%s] %s → %s (%s)",
					rs.getTimestamp("transfer_date"),
					rs.getString("from_name"),
					rs.getString("to_name"),
					rs.getString("status")));
			}
		}
		return chain;
	}

	public boolean hasActiveBatches(int manufacturerId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM medicine_batches WHERE manufacturer_id = ? AND status IN ('ACTIVE', 'IN_TRANSIT', 'IN_STOCK')";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, manufacturerId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return rs.getInt(1) > 0;
		}
		return false;
	}

	public void insertTransfer(int batchId, int fromUserId, int toPharmacyId) throws SQLException {
		String sql = "INSERT INTO batch_transfers (batch_id, from_user_id, to_pharmacy_id, status) VALUES (?, ?, ?, 'INITIATED')";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, batchId);
			ps.setInt(2, fromUserId);
			ps.setInt(3, toPharmacyId);
			ps.executeUpdate();
		}
	}

	private MedicineBatch mapRow(ResultSet rs) throws SQLException {
		MedicineBatch b = new MedicineBatch();
		b.setBatchId(rs.getInt("batch_id"));
		b.setGbi(rs.getString("gbi"));
		b.setDrugId(rs.getInt("drug_id"));
		try { b.setDrugName(rs.getString("drug_name")); } catch (SQLException ignored) {}
		b.setManufacturerId(rs.getInt("manufacturer_id"));
		Date pd = rs.getDate("production_date");
		if (pd != null) b.setProductionDate(pd.toLocalDate());
		Date ed = rs.getDate("expiry_date");
		if (ed != null) b.setExpiryDate(ed.toLocalDate());
		b.setQuantity(rs.getInt("quantity"));
		b.setStatus(BatchStatus.valueOf(rs.getString("status")));
		b.setCurrentHolderId(rs.getInt("current_holder"));
		Timestamp ts = rs.getTimestamp("created_at");
		if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
		return b;
	}
}
