package io.github.kkusylabs.useradmin.client.core.auth;

/**
 * Supplies bearer tokens for authenticated API requests.
 */
public interface AuthTokenProvider {
	
	/**
	 * Returns the current authentication token.
	 *
	 * @return bearer token value, or {@code null} when authentication is unavailable
	 */
	String getToken();
}
