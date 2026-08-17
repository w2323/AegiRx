package com.aegisrx.util;

import java.time.LocalDate;

// some common utils
public class ValidationUtils {

	public static boolean isNullOrEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static boolean isValidExpiryDate(LocalDate production, LocalDate expiry) {
		if (production == null || expiry == null) return false;
		return expiry.isAfter(production) && expiry.isAfter(LocalDate.now());
	}

	public static boolean isPositiveQuantity(int qty) {
		return qty > 0;
	}

	public static boolean isValidEmail(String email) {
		if (isNullOrEmpty(email)) return false;
		return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
	}
}
