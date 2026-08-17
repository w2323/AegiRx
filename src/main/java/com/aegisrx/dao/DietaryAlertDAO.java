package com.aegisrx.dao;

import com.aegisrx.domain.DietaryAlert;
import com.aegisrx.domain.enums.AlertSeverity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// db operations
public class DietaryAlertDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public int insert(DietaryAlert alert) throws SQLException {
		String sql = "INSERT INTO dietary_alerts (patient_user_id, food_log_id, conflicting_med, severity, reason) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, alert.getPatientUserId());
			ps.setInt(2, alert.getFoodLogId());
			ps.setInt(3, alert.getConflictingDrugId());
			ps.setString(4, alert.getSeverity().name());
			ps.setString(5, alert.getReason());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				alert.setAlertId(id);
				return id;
			}
		}
		return -1;
	}

	public boolean acknowledge(int alertId) throws SQLException {
		String sql = "UPDATE dietary_alerts SET acknowledged = 1, acknowledged_at = NOW() WHERE alert_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, alertId);
			return ps.executeUpdate() > 0;
		}
	}

	public List<DietaryAlert> findUnacknowledged(int patientUserId) throws SQLException {
		List<DietaryAlert> alerts = new ArrayList<>();
		String sql = "SELECT da.*, d.drug_name FROM dietary_alerts da " +
					 "LEFT JOIN drug_profiles d ON da.conflicting_med = d.drug_id " +
					 "WHERE da.patient_user_id = ? AND da.acknowledged = 0 ORDER BY da.created_at DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) alerts.add(mapRow(rs));
		}
		return alerts;
	}

	public int countRecentSevereIgnored(int patientUserId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM dietary_alerts WHERE patient_user_id = ? " +
					 "AND severity IN ('HIGH', 'CRITICAL') AND acknowledged = 0 " +
					 "AND created_at > DATE_SUB(NOW(), INTERVAL 1 DAY)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return rs.getInt(1);
		}
		return 0;
	}

	public void insertDisposalLog(int batchId, int pharmacyId, String reason) throws SQLException {
		String sql = "INSERT INTO disposal_log (batch_id, pharmacy_id, reason) VALUES (?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, batchId);
			ps.setInt(2, pharmacyId);
			ps.setString(3, reason);
			ps.executeUpdate();
		}
	}

	private DietaryAlert mapRow(ResultSet rs) throws SQLException {
		DietaryAlert a = new DietaryAlert();
		a.setAlertId(rs.getInt("alert_id"));
		a.setPatientUserId(rs.getInt("patient_user_id"));
		a.setFoodLogId(rs.getInt("food_log_id"));
		a.setConflictingDrugId(rs.getInt("conflicting_med"));
		try { a.setConflictingDrugName(rs.getString("drug_name")); } catch (SQLException ignored) {}
		a.setSeverity(AlertSeverity.valueOf(rs.getString("severity")));
		a.setReason(rs.getString("reason"));
		a.setAcknowledged(rs.getBoolean("acknowledged"));
		Timestamp ack = rs.getTimestamp("acknowledged_at");
		if (ack != null) a.setAcknowledgedAt(ack.toLocalDateTime());
		Timestamp ts = rs.getTimestamp("created_at");
		if (ts != null) a.setCreatedAt(ts.toLocalDateTime());
		return a;
	}
}
