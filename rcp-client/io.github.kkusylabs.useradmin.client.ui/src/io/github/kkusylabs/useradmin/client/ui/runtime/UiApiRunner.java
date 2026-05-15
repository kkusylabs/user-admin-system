package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.widgets.Control;

import jakarta.inject.Inject;

//private void loadUserDetails(Long userId) {
//    apiRunner.task(() -> userApiClient.getUser(userId))
//        .onControl(userDetails)
//        .onBefore(() -> userDetails.setLoading(true))
//        .onSuccess(user -> userDetails.showUser(user))
//        .onCustomError(error -> {
//            if (error instanceof NotFoundException) {
//                MessageDialog.openInformation(shell, "Gone", "User no longer exists.");
//                loadUsers(); // Trigger a refresh
//                return true; // We handled it!
//            }
//            return false; // Not a 404? Let the default error handler take it.
//        })
//        .onError("Load Failed", "Could not fetch user details.")
//        .execute();
//}


/**
 * A fluent, thread-safe runner for executing API calls within an Eclipse e4 environment.
 * Handles background execution, UI synchronization, and lifecycle safety checks.
 */
public final class UiApiRunner {

	private final ApiExecutor apiExecutor;
	private final UISynchronize uiSync;
	private final ApiErrorHandler apiErrorHandler;

	@Inject
	public UiApiRunner(ApiExecutor apiExecutor, UISynchronize uiSync, ApiErrorHandler apiErrorHandler) {
		this.apiExecutor = apiExecutor;
		this.uiSync = uiSync;
		this.apiErrorHandler = apiErrorHandler;
	}

	/**
	 * Starts a new API request for a task that returns a result.
	 */
	public <T> ApiRequestBuilder<T> task(Supplier<T> apiCall) {
		return new ApiRequestBuilder<>(apiCall);
	}

	/**
	 * Starts a new API request for a task that returns nothing (void).
	 */
	public ApiRequestBuilder<Void> task(Runnable runnable) {
		return new ApiRequestBuilder<>(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * Inner Builder class to configure and execute the API request.
	 */
	public final class ApiRequestBuilder<T> {
		private final Supplier<T> apiCall;
		private Control control;
		private Runnable onBefore;
		private Consumer<T> onSuccess;
		private CustomApiErrorHandler customErrorHandler;
		private String errorTitle;
		private String fallbackMessage;

		private ApiRequestBuilder(Supplier<T> apiCall) {
			this.apiCall = Objects.requireNonNull(apiCall);
		}

		/**
		 * Mandatory: The SWT Control (Part/Composite) that owns this request. 
		 * Used to check if the UI is still alive before updating it.
		 */
		public ApiRequestBuilder<T> onControl(Control control) {
			this.control = control;
			return this;
		}

		/**
		 * Optional: Logic to run on the UI thread before the background task starts.
		 */
		public ApiRequestBuilder<T> onBefore(Runnable onBefore) {
			this.onBefore = onBefore;
			return this;
		}

		/**
		 * Optional: Logic to run on the UI thread after a successful API response.
		 */
		public ApiRequestBuilder<T> onSuccess(Consumer<T> onSuccess) {
			this.onSuccess = onSuccess;
			return this;
		}

		/**
		 * Optional: Specific error handling logic (e.g., handling a 404). 
		 * Should return true if the error was handled, false to fallback to default.
		 */
		public ApiRequestBuilder<T> onCustomError(CustomApiErrorHandler handler) {
			this.customErrorHandler = handler;
			return this;
		}

		/**
		 * Optional: Titles and messages for the default error dialog.
		 */
		public ApiRequestBuilder<T> onError(String title, String message) {
			this.errorTitle = title;
			this.fallbackMessage = message;
			return this;
		}

		/**
		 * Executes the request.
		 * @throws NullPointerException if onControl() was not called.
		 */
		public void execute() {
			Objects.requireNonNull(control, "UiApiRunner: .onControl() is mandatory for lifecycle safety.");

			// 1. Initial UI feedback (Immediate)
			if (onBefore != null) {
				uiSync.asyncExec(onBefore);
			}

			// 2. Background Execution
			CompletableFuture.supplyAsync(apiCall, apiExecutor.executorService())
					.whenComplete((result, error) -> uiSync.asyncExec(() -> {
						
						// 3. Lifecycle Safety Check
						if (control.isDisposed()) {
							return;
						}

						// 4. Handle Failure
						if (error != null) {
							Throwable cause = apiErrorHandler.unwrap(error);

							if (customErrorHandler != null && customErrorHandler.handle(cause)) {
								return; // Handled by caller
							}

							apiErrorHandler.handleDefault(control.getShell(), cause, errorTitle, fallbackMessage);
							return;
						}

						// 5. Handle Success
						if (onSuccess != null) {
							onSuccess.accept(result);
						}
					}));
		}
	}

	@FunctionalInterface
	public interface CustomApiErrorHandler {
		boolean handle(Throwable error);
	}
}