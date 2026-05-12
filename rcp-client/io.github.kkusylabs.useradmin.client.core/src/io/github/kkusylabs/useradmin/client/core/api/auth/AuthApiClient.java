package io.github.kkusylabs.useradmin.client.core.api.auth;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

public class AuthApiClient {
	private final RestClient restClient;

	public AuthApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient);
	}

	public LoginResponse login(String username, String password) {
		return restClient.post("/auth/login", new LoginRequest(username, password), LoginResponse.class);
	}
	
	public MeResponse me() {
		return restClient.get("/auth/me", MeResponse.class);
	}
}