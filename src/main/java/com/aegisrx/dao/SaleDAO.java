package com.aegisrx.dao;

import com.aegisrx.domain.SaleRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handling data access here
public class SaleDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public int insert(SaleRecord sale) throws SQLException {
		String sql = "INSERT INTO sales (pharmacy_id, patient_user_id, batch_id, status) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, sale.getPharmacyId());
			ps.setInt(2, sale.getPatientUserId());
			ps.setInt(3, sale.getBatchId());
			ps.setString(4, sale.getStatus());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				sale.setSaleId(id);
				return id;
			}
		}
		return -1;
	}

	public List<SaleRecord> findByPharmacy(int pharmacyId) throws SQLException {
		List<SaleRecord> sales = new ArrayList<>();
		String sql = "SELECT s.*, b.gbi, d.drug_name FROM sales s " +
					 "JOIN medicine_batches b ON s.batch_id = b.batch_id " +
					 "JOIN drug_profiles d ON b.drug_id = d.drug_id " +
					 "WHERE s.pharmacy_id = ? ORDER BY s.sale_date DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, pharmacyId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) sales.add(mapRow(rs));
		}
		return sales;
	}

	public List<SaleRecord> findByPatient(int patientUserId) throws SQLException {
		List<SaleRecord> sales = new ArrayList<>();
		String sql = "SELECT s.*, b.gbi, d.drug_name FROM sales s " +
					 "JOIN medicine_batches b ON s.batch_id = b.batch_id " +
					 "JOIN drug_profiles d ON b.drug_id = d.drug_id " +
					 "WHERE s.patient_user_id = ? ORDER BY s.sale_date DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) sales.add(mapRow(rs));
		}
		return sales;
	}

	private SaleRecord mapRow(ResultSet rs) throws SQLException {
		SaleRecord s = new SaleRecord();
		s.setSaleId(rs.getInt("sale_id"));
		s.setPharmacyId(rs.getInt("pharmacy_id"));
		s.setPatientUserId(rs.getInt("patient_user_id"));
		s.setBatchId(rs.getInt("batch_id"));
		try { s.setBatchGbi(rs.getString("gbi")); } catch (SQLException ignored) {}
		try { s.setDrugName(rs.getString("drug_name")); } catch (SQLException ignored) {}
		Timestamp ts = rs.getTimestamp("sale_date");
		if (ts != null) s.setSaleDate(ts.toLocalDateTime());
		s.setStatus(rs.getString("status"));
		return s;
	}
}
