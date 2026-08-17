package com.aegisrx.dao;

import com.aegisrx.domain.DrugProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// dao implementation
public class DrugProfileDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public DrugProfile findById(int drugId) throws SQLException {
		String sql = "SELECT * FROM drug_profiles WHERE drug_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, drugId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public List<DrugProfile> findAll() throws SQLException {
		List<DrugProfile> profiles = new ArrayList<>();
		String sql = "SELECT * FROM drug_profiles ORDER BY drug_name";
		try (Statement st = getConnection().createStatement();
			 ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) profiles.add(mapRow(rs));
		}
		return profiles;
	}

	public List<DrugProfile> findByManufacturer(int manufacturerId) throws SQLException {
		List<DrugProfile> profiles = new ArrayList<>();
		String sql = "SELECT * FROM drug_profiles WHERE manufacturer_id = ? ORDER BY drug_name";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, manufacturerId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) profiles.add(mapRow(rs));
		}
		return profiles;
	}

	public List<DrugProfile> searchByName(String name) throws SQLException {
		List<DrugProfile> profiles = new ArrayList<>();
		String sql = "SELECT * FROM drug_profiles WHERE drug_name LIKE ? ORDER BY drug_name";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, "%" + name + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) profiles.add(mapRow(rs));
		}
		return profiles;
	}

	public int insert(DrugProfile profile) throws SQLException {
		String sql = "INSERT INTO drug_profiles (drug_name, category, description, manufacturer_id) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, profile.getDrugName());
			ps.setString(2, profile.getCategory());
			ps.setString(3, profile.getDescription());
			ps.setInt(4, profile.getManufacturerId());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				profile.setDrugId(id);
				return id;
			}
		}
		return -1;
	}

	private DrugProfile mapRow(ResultSet rs) throws SQLException {
		DrugProfile dp = new DrugProfile();
		dp.setDrugId(rs.getInt("drug_id"));
		dp.setDrugName(rs.getString("drug_name"));
		dp.setCategory(rs.getString("category"));
		dp.setDescription(rs.getString("description"));
		dp.setManufacturerId(rs.getInt("manufacturer_id"));
		return dp;
	}
}
