package io.github.kkusylabs.useradmin.client.core.api.auth;

public record LoginResponse(
		String accessToken,
		String tokenType) {
}
