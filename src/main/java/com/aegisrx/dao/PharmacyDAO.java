package com.aegisrx.dao;

import com.aegisrx.domain.Pharmacy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// dao implementation
public class PharmacyDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public Pharmacy findById(int pharmacyId) throws SQLException {
		String sql = "SELECT * FROM pharmacies WHERE pharmacy_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, pharmacyId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public Pharmacy findByStaffUserId(int staffUserId) throws SQLException {
		String sql = "SELECT * FROM pharmacies WHERE staff_user_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, staffUserId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public List<Pharmacy> findAll() throws SQLException {
		List<Pharmacy> pharmacies = new ArrayList<>();
		String sql = "SELECT * FROM pharmacies ORDER BY pharmacy_name";
		try (Statement st = getConnection().createStatement();
			 ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) pharmacies.add(mapRow(rs));
		}
		return pharmacies;
	}

	public List<Pharmacy> findByRegion(String region) throws SQLException {
		List<Pharmacy> pharmacies = new ArrayList<>();
		String sql = "SELECT * FROM pharmacies WHERE region LIKE ? OR city LIKE ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, "%" + region + "%");
			ps.setString(2, "%" + region + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) pharmacies.add(mapRow(rs));
		}
		return pharmacies;
	}

public List<Pharmacy> findWithDrugInStock(int drugId, String region) throws SQLException {
		List<Pharmacy> pharmacies = new ArrayList<>();
		String sql = "SELECT DISTINCT p.*, pi.quantity AS stock_qty FROM pharmacies p " +
					 "JOIN pharmacy_inventory pi ON p.pharmacy_id = pi.pharmacy_id " +
					 "JOIN medicine_batches b ON pi.batch_id = b.batch_id " +
					 "WHERE pi.drug_id = ? AND b.status = 'IN_STOCK' " +
					 "AND b.expiry_date > GETDATE() " +
					 "AND (p.region LIKE ? OR p.city LIKE ?) " +
					 "ORDER BY pi.quantity DESC";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, drugId);
			ps.setString(2, "%" + region + "%");
			ps.setString(3, "%" + region + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) pharmacies.add(mapRow(rs));
		}
		return pharmacies;
	}

	public int insert(Pharmacy pharmacy) throws SQLException {
		String sql = "INSERT INTO pharmacies (pharmacy_name, city, region, contact_info, staff_user_id) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, pharmacy.getPharmacyName());
			ps.setString(2, pharmacy.getCity());
			ps.setString(3, pharmacy.getRegion());
			ps.setString(4, pharmacy.getContactInfo());
			
			// If no staff user was chosen (staffUserId == 0), store NULL in the DB.
			// This avoids a Foreign Key error when a pharmacy has no staff assigned yet.
			if (pharmacy.getStaffUserId() > 0) {
				ps.setInt(5, pharmacy.getStaffUserId());
			} else {
				ps.setNull(5, Types.INTEGER);
			}
			
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				pharmacy.setPharmacyId(id);
				System.out.println("[PharmacyDAO] Inserted pharmacy: '" + pharmacy.getPharmacyName() + "' with ID=" + id);
				return id;
			}
		}
		return -1;
	}

	public boolean exists(int pharmacyId) throws SQLException {
		String sql = "SELECT COUNT(*) FROM pharmacies WHERE pharmacy_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, pharmacyId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return rs.getInt(1) > 0;
		}
		return false;
	}

	private Pharmacy mapRow(ResultSet rs) throws SQLException {
		Pharmacy p = new Pharmacy();
		p.setPharmacyId(rs.getInt("pharmacy_id"));
		p.setPharmacyName(rs.getString("pharmacy_name"));
		p.setCity(rs.getString("city"));
		p.setRegion(rs.getString("region"));
		p.setContactInfo(rs.getString("contact_info"));
		
		// staff_user_id can be NULL in the database, so I check before setting it.
		int staffId = rs.getInt("staff_user_id");
		if (!rs.wasNull()) {
			p.setStaffUserId(staffId);
		}
		// If it was NULL, staffUserId stays 0 (the default in the Pharmacy class).
		return p;
	}
}
