package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.widgets.Control;

import jakarta.inject.Inject;

/**
 * Fluent utility for executing asynchronous REST API operations within the
 * Eclipse E4 UI environment.
 *
 * <p>
 * This runner coordinates:
 * </p>
 *
 * <ul>
 *   <li>background API execution</li>
 *   <li>SWT UI thread synchronization</li>
 *   <li>control lifecycle safety checks</li>
 *   <li>default and custom error handling</li>
 *   <li>success callback execution</li>
 * </ul>
 *
 * <p>
 * Requests are configured using a fluent builder API through
 * {@link ApiRequestBuilder}.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * apiRunner.task(() -> userApiClient.getUsers(page, size, filter))
 *     .onControl(userListComposite)
 *     .onSuccess(this::showUsers)
 *     .onError("Load Failed", "Could not load users.")
 *     .execute();
 * }</pre>
 */
public final class UiApiRunner {

	private final ApiExecutor apiExecutor;
	private final UISynchronize uiSync;
	private final ApiErrorHandler apiErrorHandler;

	/**
	 * Creates the UI API runner.
	 *
	 * @param apiExecutor executor used for background API operations
	 * @param uiSync SWT UI synchronization service
	 * @param apiErrorHandler default API error handler
	 */
	@Inject
	public UiApiRunner(ApiExecutor apiExecutor, UISynchronize uiSync, ApiErrorHandler apiErrorHandler) {
		this.apiExecutor = apiExecutor;
		this.uiSync = uiSync;
		this.apiErrorHandler = apiErrorHandler;
	}

	/**
	 * Starts a new asynchronous API request for a task that returns a result.
	 *
	 * @param apiCall API operation supplier
	 * @param <T> result type
	 * @return request builder
	 */
	public <T> ApiRequestBuilder<T> task(Supplier<T> apiCall) {
		return new ApiRequestBuilder<>(apiCall);
	}

	/**
	 * Starts a new asynchronous API request for a task that does not return a
	 * result.
	 *
	 * @param runnable API operation
	 * @return request builder
	 */
	public ApiRequestBuilder<Void> task(Runnable runnable) {
		return new ApiRequestBuilder<>(() -> {
			runnable.run();
			return null;
		});
	}

	/**
	 * Fluent builder used to configure and execute asynchronous API requests.
	 *
	 * @param <T> API response type
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
							Throwable cause = ApiErrorHandler.unwrap(error);

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