package com.aegisrx.dao;

import com.aegisrx.util.AppConfig;

// db operations
public class PersistenceHandlerFactory {

	private static PersistenceHandler handler;

	public static PersistenceHandler createHandler() {
		if (handler != null) return handler;

		String type = AppConfig.getPersistenceType();
		switch (type.toLowerCase()) {
			case "file":
				handler = new FileHandler();
				break;
			case "database":
			default:
				handler = new DatabaseHandler();
				break;
		}

		handler.initialize();
		System.out.println("[Persistence] Using handler: " + handler.getType());
		return handler;
	}

	public static PersistenceHandler getHandler() {
		if (handler == null) return createHandler();
		return handler;
	}
}
