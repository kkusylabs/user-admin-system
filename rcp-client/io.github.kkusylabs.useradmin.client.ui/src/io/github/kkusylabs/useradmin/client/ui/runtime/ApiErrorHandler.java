package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.api.ForbiddenException;
import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;

public final class ApiErrorHandler {

	private final IEventBroker eventBroker;

	public ApiErrorHandler(IEventBroker eventBroker) {
		this.eventBroker = eventBroker;
	}

	public void handleDefault(Shell shell, Throwable error, String errorTitle, String fallbackErrorMessage) {

		if (error instanceof UnauthorizedException) {
			eventBroker.post(AppTopics.AUTH_EXPIRED, new Object());
			return;
		}

		if (error instanceof ForbiddenException) {
			MessageDialog.openError(shell, "Permission Denied", "You do not have permission to perform this action.");
			return;
		}

		MessageDialog.openError(shell, errorTitle, getUserFriendlyMessage(error, fallbackErrorMessage));
	}

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