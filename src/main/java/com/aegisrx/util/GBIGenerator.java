package com.aegisrx.util;

import java.util.UUID;

// utility stuff
public class GBIGenerator {

	public static String generate() {
		String datePart = java.time.LocalDate.now().toString().replace("-", "");
		String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		return "GBI-" + datePart + "-" + randomPart;
	}
}
