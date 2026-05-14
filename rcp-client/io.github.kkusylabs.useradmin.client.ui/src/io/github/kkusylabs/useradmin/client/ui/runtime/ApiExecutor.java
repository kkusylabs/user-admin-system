package io.github.kkusylabs.useradmin.client.ui.runtime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApiExecutor {

	private final ExecutorService executorService;

	public ApiExecutor() {
		this.executorService = Executors.newFixedThreadPool(4, r -> {
			Thread thread = new Thread(r, "api-client");
			thread.setDaemon(true);
			return thread;
		});
	}

	public ExecutorService executorService() {
		return executorService;
	}

	public void shutdown() {
		executorService.shutdown();
	}
}