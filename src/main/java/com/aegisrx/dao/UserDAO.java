package com.aegisrx.dao;

import com.aegisrx.domain.User;
import com.aegisrx.domain.enums.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// dao implementation
public class UserDAO {

	private Connection getConnection() throws SQLException {
		return DatabaseConnectionManager.getInstance().getConnection();
	}

	public User findById(int userId) throws SQLException {
		String sql = "SELECT * FROM users WHERE user_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public User findByUsername(String username) throws SQLException {
		String sql = "SELECT * FROM users WHERE username = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return mapRow(rs);
		}
		return null;
	}

	public List<User> findAll() throws SQLException {
		List<User> users = new ArrayList<>();
		String sql = "SELECT * FROM users ORDER BY created_at DESC";
		try (Statement st = getConnection().createStatement();
			 ResultSet rs = st.executeQuery(sql)) {
			while (rs.next()) users.add(mapRow(rs));
		}
		return users;
	}

	public List<User> findByRole(UserRole role) throws SQLException {
		List<User> users = new ArrayList<>();
		String sql = "SELECT * FROM users WHERE role = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, role.name());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) users.add(mapRow(rs));
		}
		return users;
	}

	public int insert(User user) throws SQLException {
		String sql = "INSERT INTO users (username, password_hash, full_name, email, role, status) VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getPasswordHash());
			ps.setString(3, user.getFullName());
			ps.setString(4, user.getEmail());
			ps.setString(5, user.getRole().name());
			ps.setString(6, user.getStatus());
			ps.executeUpdate();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys.next()) {
				int id = keys.getInt(1);
				user.setUserId(id);
				return id;
			}
		}
		return -1;
	}

	public boolean update(User user) throws SQLException {
		String sql = "UPDATE users SET username=?, full_name=?, email=?, role=?, status=? WHERE user_id=?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, user.getUsername());
			ps.setString(2, user.getFullName());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getRole().name());
			ps.setString(5, user.getStatus());
			ps.setInt(6, user.getUserId());
			return ps.executeUpdate() > 0;
		}
	}

	public boolean delete(int userId) throws SQLException {
		String sql = "DELETE FROM users WHERE user_id = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setInt(1, userId);
			return ps.executeUpdate() > 0;
		}
	}

	public boolean usernameExists(String username) throws SQLException {
		String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
			ps.setString(1, username);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) return rs.getInt(1) > 0;
		}
		return false;
	}

	private User mapRow(ResultSet rs) throws SQLException {
		User user = new User();
		user.setUserId(rs.getInt("user_id"));
		user.setUsername(rs.getString("username"));
		user.setPasswordHash(rs.getString("password_hash"));
		user.setFullName(rs.getString("full_name"));
		user.setEmail(rs.getString("email"));
		user.setRole(UserRole.valueOf(rs.getString("role")));
		user.setStatus(rs.getString("status"));
		Timestamp ts = rs.getTimestamp("created_at");
		if (ts != null) user.setCreatedAt(ts.toLocalDateTime());
		return user;
	}
}
