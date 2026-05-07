package io.github.kkusylabs.useradmin.client.ui.lifecycle;

import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.LoginApiClient;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentApiClient;
import io.github.kkusylabs.useradmin.client.core.api.user.UserApiClient;
import io.github.kkusylabs.useradmin.client.core.auth.SessionAuthTokenProvider;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.config.AppConfig;

public class E4LifeCycle {
	public E4LifeCycle() {
		System.out.println("E4LifeCycle constructor");
	}

	@PostContextCreate
	public void postContextCreate(IEclipseContext context) {
		System.out.println("postContextCreate");
		AppConfig appConfig = new AppConfig();

		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		ObjectMapper objectMapper = new ObjectMapper();

		SessionTokenStore tokenStore = new SessionTokenStore();
		SessionAuthTokenProvider tokenProvider = new SessionAuthTokenProvider(tokenStore);

		RestClient restClient = new RestClient(httpClient, objectMapper, appConfig.getBaseUrl(), Duration.ofSeconds(20),
				tokenProvider);

		LoginApiClient loginApiClient = new LoginApiClient(restClient);
		UserApiClient userApiClient = new UserApiClient(restClient);
		DepartmentApiClient departmentApiClient = new DepartmentApiClient(restClient);

		context.set(AppConfig.class, appConfig);
		context.set(SessionTokenStore.class, tokenStore);
		context.set(LoginApiClient.class, loginApiClient);
		context.set(UserApiClient.class, userApiClient);
		context.set(DepartmentApiClient.class, departmentApiClient);
	}
}
