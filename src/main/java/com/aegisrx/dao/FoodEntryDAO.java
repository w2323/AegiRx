package com.aegisrx.dao;

import com.aegisrx.domain.FoodEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// db operations
public class FoodEntryDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public int insert(FoodEntry entry) throws SQLException {
		String sql = "INSERT INTO food_log (patient_user_id, food_name, food_category, nutritional_props) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setInt(1, entry.getPatientUserId());
			ps.setString(2, entry.getFoodName());
			ps.setString(3, entry.getFoodCategory());
			ps.setString(4, entry.getNutritionalProps());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				entry.setLogId(id);
				return id;
			}
		}
		return -1;
	}

	public List<FoodEntry> findByPatientToday(int patientUserId) throws SQLException {
		List<FoodEntry> entries = new ArrayList<>();
		String sql = "SELECT * FROM food_log WHERE patient_user_id = ? AND CAST(logged_at AS DATE) = CAST(GETDATE() AS DATE) ORDER BY logged_at DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) entries.add(mapRow(rs));
		}
		return entries;
	}

	public List<FoodEntry> findByPatient(int patientUserId) throws SQLException {
		List<FoodEntry> entries = new ArrayList<>();
		String sql = "SELECT TOP 50 * FROM food_log WHERE patient_user_id = ? ORDER BY logged_at DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, patientUserId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) entries.add(mapRow(rs));
		}
		return entries;
	}

	private FoodEntry mapRow(ResultSet rs) throws SQLException {
		FoodEntry e = new FoodEntry();
		e.setLogId(rs.getInt("log_id"));
		e.setPatientUserId(rs.getInt("patient_user_id"));
		e.setFoodName(rs.getString("food_name"));
		e.setFoodCategory(rs.getString("food_category"));
		e.setNutritionalProps(rs.getString("nutritional_props"));
		Timestamp ts = rs.getTimestamp("logged_at");
		if (ts != null) e.setLoggedAt(ts.toLocalDateTime());
		return e;
	}
}
