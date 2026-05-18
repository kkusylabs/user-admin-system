package io.github.kkusylabs.useradmin.client.ui.config;

/**
 * Application configuration used by the RCP client.
 *
 * <p>
 * Configuration values are resolved in the following order:
 * </p>
 *
 * <ol>
 *   <li>Java system properties</li>
 *   <li>environment variables</li>
 *   <li>default values</li>
 * </ol>
 *
 * <p>
 * The backend API base URL can be configured using:
 * </p>
 *
 * <ul>
 *   <li>System property: {@code useradmin.api.baseUrl}</li>
 *   <li>Environment variable: {@code USERADMIN_API_BASEURL}</li>
 * </ul>
 */
public class AppConfig {
	private final String baseUrl;

	/**
	 * Creates the application configuration and resolves configuration values.
	 */
	public AppConfig() {
		this.baseUrl = firstNonBlank(
				System.getProperty("useradmin.api.baseUrl"),
				System.getenv("USERADMIN_API_BASEURL"), 
				"http://localhost:8080/api");
	}

	/**
	 * Returns the configured backend API base URL.
	 *
	 * @return backend API base URL
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Returns the first non-blank value from the provided list.
	 *
	 * @param values candidate values
	 * @return first non-blank value, or {@code null} if none exist
	 */
	private static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}
}