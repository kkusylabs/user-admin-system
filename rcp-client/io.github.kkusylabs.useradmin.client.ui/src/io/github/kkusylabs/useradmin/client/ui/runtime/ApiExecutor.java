package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared executor service used for asynchronous REST API operations.
 *
 * <p>
 * API requests are executed using a fixed-size daemon thread pool.
 * </p>
 */
public final class ApiExecutor {

	private final ExecutorService executorService;

	/**
	 * Creates the API executor.
	 */
	public ApiExecutor() {
		this.executorService = Executors.newFixedThreadPool(4, r -> {
			Thread thread = new Thread(r, "api-client");
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Returns the executor service used for asynchronous API execution.
	 *
	 * @return executor service
	 */
	public ExecutorService executorService() {
		return executorService;
	}

	/**
	 * Shuts down the executor service.
	 */
	public void shutdown() {
		executorService.shutdown();
	}
}