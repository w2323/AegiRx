package com.aegisrx.ai;

import com.aegisrx.util.AppConfig;
import com.google.gson.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// integration with ai
public class GeminiClient {

	private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
	private final HttpClient httpClient;
	private final Gson gson;

	public GeminiClient() {
		this.httpClient = HttpClient.newHttpClient();
		this.gson = new Gson();
	}

public String generateContent(String prompt) {
		String apiKey = AppConfig.getGeminiApiKey();
		String model = AppConfig.getGeminiModel();

		if (apiKey == null || apiKey.isEmpty()) {
			System.out.println("[AI] Gemini API key not configured. Using fallback.");
			return null;
		}

		try {
			String url = BASE_URL + model + ":generateContent?key=" + apiKey;

			// Build request body
			JsonObject textPart = new JsonObject();
			textPart.addProperty("text", prompt);

			JsonArray parts = new JsonArray();
			parts.add(textPart);

			JsonObject content = new JsonObject();
			content.add("parts", parts);

			JsonArray contents = new JsonArray();
			contents.add(content);

			JsonObject requestBody = new JsonObject();
			requestBody.add("contents", contents);

			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
				.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return extractText(response.body());
			} else {
				System.err.println("[AI] Gemini API error " + response.statusCode() + ": " + response.body());
				return null;
			}

		} catch (IOException | InterruptedException e) {
			System.err.println("[AI] Gemini request failed: " + e.getMessage());
			return null;
		}
	}

private String extractText(String responseJson) {
		try {
			JsonObject root = JsonParser.parseString(responseJson).getAsJsonObject();
			JsonArray candidates = root.getAsJsonArray("candidates");
			if (candidates != null && candidates.size() > 0) {
				JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
				JsonObject contentObj = firstCandidate.getAsJsonObject("content");
				JsonArray partsArr = contentObj.getAsJsonArray("parts");
				if (partsArr != null && partsArr.size() > 0) {
					return partsArr.get(0).getAsJsonObject().get("text").getAsString();
				}
			}
		} catch (Exception e) {
			System.err.println("[AI] Error parsing Gemini response: " + e.getMessage());
		}
		return null;
	}
}
