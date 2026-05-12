package io.github.kkusylabs.useradmin.client.core.test.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.http.HttpClient;
import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.AuthApiClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.LoginResponse;
import io.github.kkusylabs.useradmin.client.core.auth.SessionAuthTokenProvider;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;

public abstract class AbstractRealBackendApiTest {

	protected RestClient restClient;
	protected AuthApiClient authApiClient;
	protected SessionTokenStore tokenStore;

	@BeforeEach
	void baseSetUp() {
		String baseUrl = firstNonBlank(System.getProperty("useradmin.api.baseUrl"),
				System.getenv("USERADMIN_API_BASEURL"),
				"http://localhost:8081/api");
		
		System.out.println("baseUrl=" + baseUrl);

		assumeTrue(baseUrl != null && !baseUrl.isBlank(), "Real backend base URL not configured");

		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		ObjectMapper objectMapper = new ObjectMapper();

		tokenStore = new SessionTokenStore();

		restClient = new RestClient(httpClient, objectMapper, baseUrl, Duration.ofSeconds(20),
				new SessionAuthTokenProvider(tokenStore));

		authApiClient = new AuthApiClient(restClient);
	}

	protected void loginAs(String username, String password) {
		LoginResponse login = authApiClient.login(username, password);

		assertNotNull(login);
		assertNotNull(login.accessToken());
		assertFalse(login.accessToken().isBlank());

		tokenStore.setToken(login.accessToken());
	}

	protected void loginAsAdmin() {
		loginAs("admin1", "admin12345");
	}

	protected void loginAsManager() {
		loginAs("manager1", "manager12345");
	}

	protected void loginAsUser() {
		loginAs("user1", "user12345");
	}

	protected static String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}
}