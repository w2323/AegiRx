package com.aegisrx.domain;

import com.aegisrx.domain.enums.UserRole;

// simple entity class
public class Admin extends User {

	public Admin() {
		super();
		setRole(UserRole.ADMIN);
	}

	public Admin(String username, String fullName, String email) {
		super(username, fullName, email, UserRole.ADMIN);
	}
}
