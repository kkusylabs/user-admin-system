package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.api.ForbiddenException;
import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;


//private void loadUsers() {
//    apiRunner.run(
//            () -> userApiClient.getUsers(),
//            response -> {
//                userListComposite.setUserListResponse(response);
//                userDetailsComposite.clear();
//            },
//            "Load Users Failed",
//            "Could not load users.");
//}

//private void loadUserDetails(Long userId) {
//    apiRunner.run(
//            () -> userApiClient.getUser(userId),
//            user -> userDetailsComposite.showUser(user),
//            error -> {
//                if (error instanceof NotFoundException) {
//                    MessageDialog.openInformation(
//                            shell,
//                            "User Not Found",
//                            "This user no longer exists. The list will be refreshed.");
//
//                    loadUsers();
//                    userDetailsComposite.clear();
//                    return true;
//                }
//
//                return false;
//            },
//            "Load User Failed",
//            "Could not load user.");
//}


public final class UiApiRunner {

	private final ApiExecutor apiExecutor;
	private final UISynchronize uiSync;
	private final IEventBroker eventBroker;
	private final Shell shell;

	public UiApiRunner(ApiExecutor apiExecutor, UISynchronize uiSync, IEventBroker eventBroker, Shell shell) {

		this.apiExecutor = apiExecutor;
		this.uiSync = uiSync;
		this.eventBroker = eventBroker;
		this.shell = shell;
	}

	public <T> void run(Supplier<T> apiCall, Consumer<T> onSuccess, String errorTitle, String fallbackErrorMessage) {

		run(apiCall, onSuccess, null, errorTitle, fallbackErrorMessage);
	}

	public <T> void run(Supplier<T> apiCall, Consumer<T> onSuccess, ApiErrorHandler customErrorHandler,
			String errorTitle, String fallbackErrorMessage) {

		CompletableFuture.supplyAsync(apiCall, apiExecutor.executorService())
				.whenComplete((result, error) -> uiSync.asyncExec(() -> {
					if (isShellDisposed()) {
						return;
					}

					if (error != null) {
						handleError(error, customErrorHandler, errorTitle, fallbackErrorMessage);
						return;
					}

					onSuccess.accept(result);
				}));
	}

	public void runVoid(Runnable apiCall, Runnable onSuccess, String errorTitle, String fallbackErrorMessage) {

		runVoid(apiCall, onSuccess, null, errorTitle, fallbackErrorMessage);
	}

	public void runVoid(Runnable apiCall, Runnable onSuccess, ApiErrorHandler customErrorHandler, String errorTitle,
			String fallbackErrorMessage) {

		CompletableFuture.runAsync(apiCall, apiExecutor.executorService())
				.whenComplete((ignored, error) -> uiSync.asyncExec(() -> {
					if (isShellDisposed()) {
						return;
					}

					if (error != null) {
						handleError(error, customErrorHandler, errorTitle, fallbackErrorMessage);
						return;
					}

					onSuccess.run();
				}));
	}

	private void handleError(Throwable error, ApiErrorHandler customErrorHandler, String errorTitle,
			String fallbackErrorMessage) {

		Throwable cause = unwrap(error);

		if (customErrorHandler != null && customErrorHandler.handle(cause)) {
			return;
		}

		handleDefaultError(cause, errorTitle, fallbackErrorMessage);
	}

	private void handleDefaultError(Throwable cause, String errorTitle, String fallbackErrorMessage) {

		if (cause instanceof UnauthorizedException) {
			eventBroker.post(AppTopics.AUTH_EXPIRED, new Object());
			return;
		}

		if (cause instanceof ForbiddenException) {
			MessageDialog.openError(shell, "Permission Denied", "You do not have permission to perform this action.");
			return;
		}

		MessageDialog.openError(shell, errorTitle, getUserFriendlyMessage(cause, fallbackErrorMessage));
	}

	private String getUserFriendlyMessage(Throwable cause, String fallbackErrorMessage) {

		String message = cause.getMessage();

		if (message != null && !message.isBlank()) {
			return message;
		}

		return fallbackErrorMessage;
	}

	private Throwable unwrap(Throwable error) {
		if (error instanceof CompletionException && error.getCause() != null) {
			return error.getCause();
		}

		if (error instanceof ExecutionException && error.getCause() != null) {
			return error.getCause();
		}

		return error;
	}

	private boolean isShellDisposed() {
		return shell == null || shell.isDisposed();
	}

	@FunctionalInterface
	public interface ApiErrorHandler {
		boolean handle(Throwable error);
	}
}