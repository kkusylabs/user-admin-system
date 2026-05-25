package io.github.kkusylabs.useradmin.client.ui.lifecycle;

import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
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
import io.github.kkusylabs.useradmin.client.ui.runtime.ApiErrorHandler;
import io.github.kkusylabs.useradmin.client.ui.runtime.ApiExecutor;
import io.github.kkusylabs.useradmin.client.ui.runtime.UiApiRunner;
import jakarta.annotation.PreDestroy;

/**
 * Eclipse E4 application lifecycle configuration.
 *
 * <p>
 * This lifecycle class initializes shared application services and registers
 * them in the Eclipse dependency injection context during application startup.
 * </p>
 *
 * <p>
 * Configured services include:
 * </p>
 *
 * <ul>
 *   <li>HTTP and REST infrastructure</li>
 *   <li>authentication and session management</li>
 *   <li>REST API clients</li>
 *   <li>asynchronous API execution support</li>
 *   <li>UI error handling and API coordination</li>
 * </ul>
 */
public class E4LifeCycle {
	
	/**
	 * Creates the application lifecycle configuration.
	 */
	public E4LifeCycle() {
	}

	/**
	 * Initializes application services after the Eclipse context is created.
	 *
	 * <p>
	 * Core infrastructure services are created and registered in the
	 * dependency injection context for use throughout the application.
	 * </p>
	 *
	 * @param context Eclipse dependency injection context
	 * @param eventBroker Eclipse event broker
	 * @param uiSync SWT UI synchronization service
	 */
	@PostContextCreate
	public void postContextCreate(
			IEclipseContext context, 
			IEventBroker eventBroker,
			UISynchronize uiSync) {
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
		
		ApiErrorHandler apiErrorHandler = new ApiErrorHandler(eventBroker, tokenStore);
		ApiExecutor apiExecutor = new ApiExecutor();
		UiApiRunner apiRunner = new UiApiRunner(apiExecutor, uiSync, apiErrorHandler);
		
		LoginService loginService = new LoginService(loginApiClient, tokenStore, eventBroker, apiExecutor);
		
		context.set(AppConfig.class, appConfig);
		context.set(SessionTokenStore.class, tokenStore);
		context.set(AuthApiClient.class, loginApiClient);
		context.set(UserApiClient.class, userApiClient);
		context.set(DepartmentApiClient.class, departmentApiClient);
		context.set(ApiExecutor.class, apiExecutor);
		context.set(ApiErrorHandler.class, apiErrorHandler);
		context.set(UiApiRunner.class, apiRunner);
		context.set(LoginService.class, loginService);
	}
	
	/**
	 * Performs application shutdown cleanup.
	 *
	 * <p>
	 * This currently shuts down asynchronous API execution resources.
	 * </p>
	 *
	 * @param context Eclipse dependency injection context
	 */
	@PreDestroy
	public void preDestroy(IEclipseContext context) {
		ApiExecutor apiExecutor = context.get(ApiExecutor.class);

		if (apiExecutor != null) {
			apiExecutor.shutdown();
		}
	}
}
