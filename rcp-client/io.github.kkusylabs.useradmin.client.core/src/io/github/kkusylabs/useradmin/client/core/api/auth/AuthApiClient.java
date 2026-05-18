package io.github.kkusylabs.useradmin.client.core.api.auth;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;

/**
 * Client for authentication-related API operations.
 *
 * <p>Provides methods for user authentication and retrieving information about
 * the currently authenticated user.
 */
public class AuthApiClient {
	private final RestClient restClient;

	/**
	 * Creates a new authentication API client.
	 *
	 * @param restClient underlying REST client used to execute HTTP requests
	 */
	public AuthApiClient(RestClient restClient) {
		this.restClient = Objects.requireNonNull(restClient);
	}

	/**
	 * Authenticates a user using username and password credentials.
	 *
	 * @param username account username
	 * @param password account password
	 * @return authentication response containing login result details
	 */
	public LoginResponse login(String username, String password) {
		return restClient.post("/auth/login", new LoginRequest(username, password), LoginResponse.class);
	}
	
	/**
	 * Returns information about the currently authenticated user.
	 *
	 * @return authenticated user details
	 */
	public MeResponse me() {
		return restClient.get("/auth/me", MeResponse.class);
	}
}