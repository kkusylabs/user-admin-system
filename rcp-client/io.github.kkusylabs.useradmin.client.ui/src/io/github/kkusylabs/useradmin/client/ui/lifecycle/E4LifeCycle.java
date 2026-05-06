package io.github.kkusylabs.useradmin.client.ui.lifecycle;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;

import io.github.kkusylabs.useradmin.client.ui.config.AppConfig;

public class E4LifeCycle {
	public E4LifeCycle() {
		System.out.println("E4LifeCycle constructor");
	}

	@PostContextCreate
	public void postContextCreate(IEclipseContext context) {
		System.out.println("postContextCreate");
		AppConfig config = new AppConfig();
//		SessionManager sessionManager = new SessionManager();
//		ApiClient apiClient = new ApiClient(sessionManager, config);
//		
		context.set(AppConfig.class, config);
//		context.set(SessionManager.class, sessionManager);
//		context.set(ApiClient.class, apiClient);
	}
}
