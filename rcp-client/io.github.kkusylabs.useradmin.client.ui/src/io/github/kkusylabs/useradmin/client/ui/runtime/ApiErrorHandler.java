package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.api.ForbiddenException;
import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;

/**
 * Centralized UI error handling utility for asynchronous API operations.
 *
 * <p>
 * This handler coordinates:
 * </p>
 *
 * <ul>
 *   <li>authentication expiration handling</li>
 *   <li>authorization error dialogs</li>
 *   <li>default API error dialogs</li>
 *   <li>exception unwrapping for async operations</li>
 * </ul>
 */
public final class ApiErrorHandler {

	private final IEventBroker eventBroker;
	
	private final SessionTokenStore tokenStore;

	/**
	 * Creates the API error handler.
	 *
	 * @param eventBroker Eclipse event broker
	 */
	public ApiErrorHandler(IEventBroker eventBroker, SessionTokenStore tokenStore) {
		this.eventBroker = eventBroker;
		this.tokenStore = tokenStore;
	}

	/**
	 * Handles an API error using standard application error behavior.
	 *
	 * <p>
	 * Unauthorized errors trigger authentication expiration handling while
	 * forbidden errors display a permission dialog.
	 * </p>
	 *
	 * @param shell parent shell
	 * @param error API error
	 * @param errorTitle error dialog title
	 * @param fallbackErrorMessage fallback error message
	 */
	public void handleDefault(Shell shell, Throwable error, String errorTitle, String fallbackErrorMessage) {

		if (error instanceof UnauthorizedException) {
			tokenStore.clear();
			eventBroker.post(AppTopics.AUTH_EXPIRED, null);
			return;
		}

		if (error instanceof ForbiddenException) {
			MessageDialog.openError(shell, "Permission Denied", "You do not have permission to perform this action.");
			return;
		}

		MessageDialog.openError(shell, errorTitle, getUserFriendlyMessage(error, fallbackErrorMessage));
	}

	/**
	 * Unwraps nested asynchronous execution exceptions to expose the root cause.
	 *
	 * @param error wrapped error
	 * @return root cause error
	 */
	public static Throwable unwrap(Throwable error) {
		Throwable current = error;

		while ((current instanceof CompletionException || current instanceof ExecutionException
				|| current instanceof InvocationTargetException) && current.getCause() != null) {

			current = current.getCause();
		}

		return current;
	}

	private String getUserFriendlyMessage(Throwable error, String fallbackErrorMessage) {

		String message = error.getMessage();

		if (message != null && !message.isBlank()) {
			return message;
		}

		return fallbackErrorMessage;
	}
	
}