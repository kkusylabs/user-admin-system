package io.github.kkusylabs.useradmin.client.core.auth;

public class SessionTokenStore {
	private volatile String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void clear() {
		this.token = null;
	}

	public boolean hasToken() {
		return token != null && !token.isBlank();
	}
}
