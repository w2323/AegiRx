package com.aegisrx.dao;

import com.aegisrx.domain.Medication;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// handling data access here
public class MedicationDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public List<Medication> findActiveByPatient(int patientUserId) throws SQLException {
		List<Medication> meds = new ArrayList<>();
		String sql = "SELECT m.*, d.drug_name FROM patient_medications m " +
					 "JOIN drug_profiles d ON m.drug_id = d.drug_id " +
					 "WHERE m.patient_user_id = ? AND m.is_active = 1 ORDER BY m.added_at DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) meds.add(mapRow(rs));
		}
		return meds;
	}

	public int insert(Medication med) throws SQLException {
		String sql = "INSERT INTO patient_medications (patient_user_id, drug_id, dosage, frequency, is_active) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, med.getPatientUserId());
			ps.setInt(2, med.getDrugId());
			ps.setString(3, med.getDosage());
			ps.setString(4, med.getFrequency());
			ps.setBoolean(5, med.isActive());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				med.setMedId(id);
				return id;
			}
		}
		return -1;
	}

	public boolean updateDosage(int medId, String dosage, String frequency) throws SQLException {
		String sql = "UPDATE patient_medications SET dosage = ?, frequency = ? WHERE med_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, dosage);
			ps.setString(2, frequency);
			ps.setInt(3, medId);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean deactivate(int medId) throws SQLException {
		String sql = "UPDATE patient_medications SET is_active = 0 WHERE med_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, medId);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean isDuplicate(int patientUserId, int drugId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM patient_medications WHERE patient_user_id = ? AND drug_id = ? AND is_active = 1";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ps.setInt(2, drugId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return rs.getInt(1) > 0;
		}
		return false;
	}

	private Medication mapRow(ResultSet rs) throws SQLException {
		Medication m = new Medication();
		m.setMedId(rs.getInt("med_id"));
		m.setPatientUserId(rs.getInt("patient_user_id"));
		m.setDrugId(rs.getInt("drug_id"));
		try { m.setDrugName(rs.getString("drug_name")); } catch (SQLException ignored) {}
		m.setDosage(rs.getString("dosage"));
		m.setFrequency(rs.getString("frequency"));
		m.setActive(rs.getBoolean("is_active"));
		Timestamp ts = rs.getTimestamp("added_at");
		if (ts != null) m.setAddedAt(ts.toLocalDateTime());
		return m;
	}
}
