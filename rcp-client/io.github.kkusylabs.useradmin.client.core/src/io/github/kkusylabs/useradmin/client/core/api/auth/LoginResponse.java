package io.github.kkusylabs.useradmin.client.core.api.auth;

/**
 * Authentication response returned after a successful login.
 *
 * @param accessToken bearer token used for authenticated API requests
 * @param tokenType token type returned by the authentication service, typically
 *                  {@code Bearer}
 */
public record LoginResponse(
		String accessToken,
		String tokenType) {
}
