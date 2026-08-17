package com.aegisrx.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

// dao implementation
public class FileHandler implements PersistenceHandler {
	private static final String DATA_DIR = "data";
	private final Gson gson;

	public FileHandler() {
		this.gson = new GsonBuilder().setPrettyPrinting().create();
	}

	@Override
	public boolean initialize() {
		try {
			Files.createDirectories(Paths.get(DATA_DIR));
			return true;
		} catch (IOException e) {
			System.err.println("[FileHandler] Cannot create data directory: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean isAvailable() {
		return Files.isDirectory(Paths.get(DATA_DIR));
	}

	@Override
	public String getType() {
		return "file";
	}

	public <T> void saveToFile(String filename, T data) throws IOException {
		String json = gson.toJson(data);
		Files.writeString(Paths.get(DATA_DIR, filename), json);
	}

	public <T> T loadFromFile(String filename, Class<T> clazz) throws IOException {
		String json = Files.readString(Paths.get(DATA_DIR, filename));
		return gson.fromJson(json, clazz);
	}
}
