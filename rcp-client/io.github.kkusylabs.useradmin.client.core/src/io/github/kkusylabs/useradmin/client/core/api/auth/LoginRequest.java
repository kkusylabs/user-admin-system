package io.github.kkusylabs.useradmin.client.core.api.auth;

public record LoginRequest(
		String username, 
		String password) {
}
