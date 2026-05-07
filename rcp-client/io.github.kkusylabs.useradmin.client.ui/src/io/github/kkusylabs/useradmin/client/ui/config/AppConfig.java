package io.github.kkusylabs.useradmin.client.ui.config;

public class AppConfig {
	private final String baseUrl;

	public AppConfig() {
		this.baseUrl = firstNonBlank(
				System.getProperty("useradmin.api.baseUrl"),
				System.getenv("USERADMIN_API_BASEURL"), 
				"http://localhost:8080/api");
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}
}