package io.github.kkusylabs.useradmin.client.core.api.auth;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

public class LoginApiClient {
	private final RestClient restClient;

	public LoginApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient);
	}

	public LoginResponse login(String username, String password) {
		return restClient.post("/auth/login", new LoginRequest(username, password), LoginResponse.class);
	}
}