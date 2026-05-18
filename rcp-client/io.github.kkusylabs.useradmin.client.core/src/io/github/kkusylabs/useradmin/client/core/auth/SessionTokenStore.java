package io.github.kkusylabs.useradmin.client.core.auth;

/**
 * In-memory store for the current session authentication token.
 *
 * <p>Intended for lightweight client-side token management during authenticated
 * API interactions.
 */
public class SessionTokenStore {
	private volatile String token;

	/**
	 * Returns the currently stored authentication token.
	 *
	 * @return current token value, or {@code null} when no token is stored
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Stores the authentication token used for subsequent API requests.
	 *
	 * @param token bearer token value
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Removes the currently stored authentication token.
	 */
	public void clear() {
		this.token = null;
	}

	/**
	 * Returns whether a non-blank authentication token is currently stored.
	 *
	 * @return {@code true} when a token is available
	 */
	public boolean hasToken() {
		return token != null && !token.isBlank();
	}
}
