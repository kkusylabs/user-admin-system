package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;

public final class ApiErrorHandler {

	private final IEventBroker eventBroker;

	private final SessionTokenStore tokenStore;

	private final ExceptionDialogMessageMapper dialogMessageMapper;

	public ApiErrorHandler(
			IEventBroker eventBroker,
			SessionTokenStore tokenStore,
			ExceptionDialogMessageMapper dialogMessageMapper) {

		this.eventBroker = Objects.requireNonNull(eventBroker);
		this.tokenStore = Objects.requireNonNull(tokenStore);
		this.dialogMessageMapper = Objects.requireNonNull(dialogMessageMapper);
	}

	public void handleDefault(
			Shell shell,
			Throwable error,
			String errorTitle,
			String fallbackErrorMessage) {

		Throwable unwrapped = unwrap(error);

		if (unwrapped instanceof UnauthorizedException) {
			tokenStore.clear();
			eventBroker.post(AppTopics.AUTH_EXPIRED, null);
			return;
		}

		String message = dialogMessageMapper.toDialogMessage(
				unwrapped,
				fallbackErrorMessage);

		MessageDialog.openError(shell, errorTitle, message);
	}

	public static Throwable unwrap(Throwable error) {
		Throwable current = error;

		while ((current instanceof CompletionException
				|| current instanceof ExecutionException
				|| current instanceof InvocationTargetException)
				&& current.getCause() != null) {

			current = current.getCause();
		}

		return current;
	}
}