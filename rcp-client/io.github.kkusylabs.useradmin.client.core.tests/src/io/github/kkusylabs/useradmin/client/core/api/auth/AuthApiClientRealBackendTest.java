package io.github.kkusylabs.useradmin.client.core.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.core.test.support.AbstractRealBackendApiTest;

class AuthApiClientRealBackendTest extends AbstractRealBackendApiTest {

	@Test
	void login_admin1_succeeds() {
		LoginResponse response = authApiClient.login("admin1", "admin12345");

		assertNotNull(response);
		assertNotNull(response.accessToken());
	}

	@Test
	void login_with_bad_password_fails() {
		assertThrows(UnauthorizedException.class, () -> authApiClient.login("admin1", "wrong-password"));
	}

	@Test
	void me_returns_admin_identity_after_login() {
		loginAsAdmin();

		MeResponse me = authApiClient.me();

		assertNotNull(me);
		assertNotNull(me.userId());
		assertEquals("admin1", me.username());
	}

	@Test
	void me_without_token_fails() {
		assertThrows(UnauthorizedException.class, () -> authApiClient.me());
	}
}