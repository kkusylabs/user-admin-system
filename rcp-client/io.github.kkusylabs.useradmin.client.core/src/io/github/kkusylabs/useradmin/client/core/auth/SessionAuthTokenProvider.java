package io.github.kkusylabs.useradmin.client.core.auth;

import java.util.Objects;

/**
 * {@link AuthTokenProvider} implementation backed by a
 * {@link SessionTokenStore}.
 *
 * <p>Returns the currently stored session token for authenticated API requests.
 */
public class SessionAuthTokenProvider implements AuthTokenProvider {
	private final SessionTokenStore tokenStore;

	/**
	 * Creates a new token provider backed by the supplied token store.
	 *
	 * @param tokenStore session token store used to retrieve authentication tokens
	 */
	public SessionAuthTokenProvider(SessionTokenStore tokenStore) {
		this.tokenStore = Objects.requireNonNull(tokenStore, "tokenStore must not be null");
	}

	@Override
	public String getToken() {
		return tokenStore.getToken();
	}
}