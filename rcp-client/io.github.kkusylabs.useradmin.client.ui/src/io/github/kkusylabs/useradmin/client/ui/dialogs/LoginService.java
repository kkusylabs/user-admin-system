package io.github.kkusylabs.useradmin.client.ui.dialogs;

import java.util.concurrent.CompletableFuture;

import org.eclipse.e4.core.services.events.IEventBroker;

import io.github.kkusylabs.useradmin.client.core.api.auth.AuthApiClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.LoginResponse;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import io.github.kkusylabs.useradmin.client.ui.runtime.ApiExecutor;

public class LoginService {

	private final AuthApiClient authApiClient;
	private final SessionTokenStore tokenStore;
	private final IEventBroker eventBroker;
	private final ApiExecutor apiExecutor;

	public LoginService(AuthApiClient authApiClient, 
			SessionTokenStore tokenStore, 
			IEventBroker eventBroker,
			ApiExecutor apiExecutor) {

		this.authApiClient = authApiClient;
		this.tokenStore = tokenStore;
		this.eventBroker = eventBroker;
		this.apiExecutor = apiExecutor;
	}

	public CompletableFuture<LoginResponse> loginAsync(String username, String password) {

		return CompletableFuture.supplyAsync(() -> {
			LoginResponse response = authApiClient.login(username, password);

			tokenStore.setToken(response.accessToken());
			eventBroker.post(AppTopics.LOGIN_SUCCESS, username);

			return response;
		}, apiExecutor.executorService());
	}
}
