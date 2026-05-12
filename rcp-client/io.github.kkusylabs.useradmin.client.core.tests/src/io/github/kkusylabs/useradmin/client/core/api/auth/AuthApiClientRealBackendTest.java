package io.github.kkusylabs.useradmin.client.core.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.core.test.support.AbstractRealBackendApiTest;

class AuthApiClientRealBackendTest extends AbstractRealBackendApiTest {

	@Test
	void login_admin1_succeeds() {
		LoginResponse response = authApiClient.login("admin1", "admin12345");

		assertNotNull(response);
		assertNotNull(response.accessToken());
		assertFalse(response.accessToken().isBlank());
	}
	
	@Test
	void login_manager1_succeeds() {
		LoginResponse response = authApiClient.login("manager1", "manager12345");

		assertNotNull(response);
		assertNotNull(response.accessToken());
		assertFalse(response.accessToken().isBlank());
	}
	
	@Test
	void login_user1_succeeds() {
		LoginResponse response = authApiClient.login("user1", "user12345");

		assertNotNull(response);
		assertNotNull(response.accessToken());
		assertFalse(response.accessToken().isBlank());
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
	void me_returns_manager_identity_after_login() {
		LoginResponse login = authApiClient.login("manager1", "manager12345");
		tokenStore.setToken(login.accessToken());

		MeResponse me = authApiClient.me();

		assertNotNull(me);
		assertNotNull(me.userId());
		assertEquals("manager1", me.username());
	}
	
	@Test
	void me_returns_user_identity_after_login() {
		LoginResponse login = authApiClient.login("user1", "user12345");
		tokenStore.setToken(login.accessToken());

		MeResponse me = authApiClient.me();

		assertNotNull(me);
		assertNotNull(me.userId());
		assertEquals("user1", me.username());
	}


	@Test
	void me_without_token_fails() {
		assertThrows(UnauthorizedException.class, () -> authApiClient.me());
	}
	
	@Test
	void me_with_invalid_token_fails() {
		tokenStore.setToken("not-a-real-token");
		assertThrows(UnauthorizedException.class, () -> authApiClient.me());
	}
}