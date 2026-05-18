package io.github.kkusylabs.useradmin.client.ui.dialogs;

import java.util.concurrent.CompletableFuture;

import org.eclipse.e4.core.services.events.IEventBroker;

import io.github.kkusylabs.useradmin.client.core.api.auth.AuthApiClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.LoginResponse;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import io.github.kkusylabs.useradmin.client.ui.runtime.ApiExecutor;

/**
 * Service responsible for coordinating user authentication workflows.
 *
 * <p>
 * This service authenticates users through {@link AuthApiClient},
 * stores the resulting session token, and publishes login success
 * events through the Eclipse event broker.
 * </p>
 */
public class LoginService {

	private final AuthApiClient authApiClient;
	private final SessionTokenStore tokenStore;
	private final IEventBroker eventBroker;
	private final ApiExecutor apiExecutor;

	/**
	 * Creates the login service.
	 *
	 * @param authApiClient authentication API client
	 * @param tokenStore session token storage
	 * @param eventBroker Eclipse event broker
	 * @param apiExecutor executor used for asynchronous API operations
	 */
	public LoginService(AuthApiClient authApiClient, 
			SessionTokenStore tokenStore, 
			IEventBroker eventBroker,
			ApiExecutor apiExecutor) {

		this.authApiClient = authApiClient;
		this.tokenStore = tokenStore;
		this.eventBroker = eventBroker;
		this.apiExecutor = apiExecutor;
	}

	/**
	 * Performs asynchronous user authentication.
	 *
	 * <p>
	 * On successful authentication the session token is stored and a
	 * {@link AppTopics#LOGIN_SUCCESS} event is published.
	 * </p>
	 *
	 * @param username login username
	 * @param password login password
	 * @return future containing the login response
	 */
	public CompletableFuture<LoginResponse> loginAsync(String username, String password) {

		return CompletableFuture.supplyAsync(() -> {
			LoginResponse response = authApiClient.login(username, password);

			tokenStore.setToken(response.accessToken());
			eventBroker.post(AppTopics.LOGIN_SUCCESS, username);

			return response;
		}, apiExecutor.executorService());
	}
}
