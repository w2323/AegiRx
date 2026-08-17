package com.aegisrx.service;

import com.aegisrx.dao.UserDAO;
import com.aegisrx.dao.BatchDAO;
import com.aegisrx.domain.User;
import com.aegisrx.domain.enums.UserRole;

import java.sql.SQLException;
import java.util.List;

// processing logic
public class UserService {

	private final UserDAO userDAO;
	private final BatchDAO batchDAO;

	public UserService() {
		this.userDAO = new UserDAO();
		this.batchDAO = new BatchDAO();
	}

	public User createUser(String username, String fullName, String email, 
						   UserRole role, String password) throws SQLException {
		// Validate
		if (userDAO.usernameExists(username)) {
			throw new IllegalArgumentException("Username '" + username + "' is already taken.");
		}

		User user = new User(username, fullName, email, role);
		user.setPasswordHash(hashPassword(password));
		userDAO.insert(user);
		return user;
	}

	public boolean updateUser(User user) throws SQLException {
		return userDAO.update(user);
	}

	public boolean deleteUser(int userId) throws SQLException {
		User user = userDAO.findById(userId);
		if (user == null) throw new IllegalArgumentException("User not found.");

		// Extension: Can't delete manufacturer with active batches
		if (user.getRole() == UserRole.MANUFACTURER && batchDAO.hasActiveBatches(userId)) {
			throw new IllegalStateException(
				"Cannot delete manufacturer with active batches. Suspend them instead.");
		}

		return userDAO.delete(userId);
	}

	public User findById(int userId) throws SQLException {
		return userDAO.findById(userId);
	}

	public User findByUsername(String username) throws SQLException {
		return userDAO.findByUsername(username);
	}

	public List<User> getAllUsers() throws SQLException {
		return userDAO.findAll();
	}

	public List<User> getUsersByRole(UserRole role) throws SQLException {
		return userDAO.findByRole(role);
	}

	public boolean suspendUser(int userId) throws SQLException {
		User user = userDAO.findById(userId);
		if (user == null) throw new IllegalArgumentException("User not found.");
		user.setStatus("SUSPENDED");
		return userDAO.update(user);
	}

	private String hashPassword(String password) {
		// Simple hash for demo - in production use BCrypt
		return Integer.toHexString(password.hashCode());
	}
}
