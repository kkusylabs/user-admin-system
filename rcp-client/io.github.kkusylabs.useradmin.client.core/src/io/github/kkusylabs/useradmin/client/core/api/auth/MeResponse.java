package io.github.kkusylabs.useradmin.client.core.api.auth;

/**
 * Response containing information about the currently authenticated user.
 *
 * @param userId unique user identifier
 * @param username authenticated user's username
 */
public record MeResponse(
		Long userId, 
		String username) {
}
