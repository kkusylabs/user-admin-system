package io.github.kkusylabs.useradmin.client.ui.lifecycle;

import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.kkusylabs.useradmin.client.core.api.RestClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.AuthApiClient;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentApiClient;
import io.github.kkusylabs.useradmin.client.core.api.user.UserApiClient;
import io.github.kkusylabs.useradmin.client.core.auth.SessionAuthTokenProvider;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.config.AppConfig;
import io.github.kkusylabs.useradmin.client.ui.dialogs.LoginService;
import io.github.kkusylabs.useradmin.client.ui.runtime.ApiExecutor;
import jakarta.annotation.PreDestroy;

public class E4LifeCycle {
	public E4LifeCycle() {
	}

	@PostContextCreate
	public void postContextCreate(IEclipseContext context, IEventBroker eventBroker) {
		AppConfig appConfig = new AppConfig();

		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

		ObjectMapper objectMapper = new ObjectMapper();

		SessionTokenStore tokenStore = new SessionTokenStore();
		SessionAuthTokenProvider tokenProvider = new SessionAuthTokenProvider(tokenStore);

		RestClient restClient = new RestClient(httpClient, objectMapper, appConfig.getBaseUrl(), Duration.ofSeconds(20),
				tokenProvider);

		AuthApiClient loginApiClient = new AuthApiClient(restClient);
		UserApiClient userApiClient = new UserApiClient(restClient);
		DepartmentApiClient departmentApiClient = new DepartmentApiClient(restClient);

		ApiExecutor apiExecutor = new ApiExecutor();
		
		LoginService loginService = new LoginService(loginApiClient, tokenStore, eventBroker, apiExecutor);
		
		context.set(AppConfig.class, appConfig);
		context.set(SessionTokenStore.class, tokenStore);
		context.set(AuthApiClient.class, loginApiClient);
		context.set(UserApiClient.class, userApiClient);
		context.set(DepartmentApiClient.class, departmentApiClient);
		context.set(ApiExecutor.class, apiExecutor);
		context.set(LoginService.class, loginService);
	}
	
	@PreDestroy
	public void preDestroy(IEclipseContext context) {
		ApiExecutor apiExecutor = context.get(ApiExecutor.class);

		if (apiExecutor != null) {
			apiExecutor.shutdown();
		}
	}
}
