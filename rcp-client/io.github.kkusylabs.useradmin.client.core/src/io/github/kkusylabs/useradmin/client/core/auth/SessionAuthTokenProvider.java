package io.github.kkusylabs.useradmin.client.core.auth;

import java.util.Objects;

public class SessionAuthTokenProvider implements AuthTokenProvider {
	private final SessionTokenStore tokenStore;

	public SessionAuthTokenProvider(SessionTokenStore tokenStore) {
		this.tokenStore = Objects.requireNonNull(tokenStore, "tokenStore must not be null");
	}

	@Override
	public String getToken() {
		return tokenStore.getToken();
	}
}