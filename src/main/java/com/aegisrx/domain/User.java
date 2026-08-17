package com.aegisrx.domain;

import com.aegisrx.domain.enums.UserRole;
import java.time.LocalDateTime;

// data model for this
public class User {
	private int userId;
	private String username;
	private String passwordHash;
	private String fullName;
	private String email;
	private UserRole role;
	private String status;
	private LocalDateTime createdAt;

	public User() {
		this.status = "ACTIVE";
		this.createdAt = LocalDateTime.now();
	}

	public User(String username, String fullName, String email, UserRole role) {
		this();
		this.username = username;
		this.fullName = fullName;
		this.email = email;
		this.role = role;
	}

	// Getters and Setters
	public int getUserId() { return userId; }
	public void setUserId(int userId) { this.userId = userId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPasswordHash() { return passwordHash; }
	public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

	public String getFullName() { return fullName; }
	public void setFullName(String fullName) { this.fullName = fullName; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public UserRole getRole() { return role; }
	public void setRole(UserRole role) { this.role = role; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	@Override
	public String toString() {
		return fullName + " (" + role.getDisplayName() + ")";
	}
}
