package io.github.kkusylabs.useradmin.client.core.api.auth;

/**
 * Authentication request containing user login credentials.
 *
 * @param username account username
 * @param password account password
 */
public record LoginRequest(
		String username, 
		String password) {
}
