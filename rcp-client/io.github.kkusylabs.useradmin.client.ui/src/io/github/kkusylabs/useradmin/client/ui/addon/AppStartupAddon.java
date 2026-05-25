package io.github.kkusylabs.useradmin.client.ui.addon;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.ui.dialogs.LoginDialog;
import io.github.kkusylabs.useradmin.client.ui.dialogs.LoginService;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import jakarta.inject.Inject;

/**
 * Application startup addon responsible for coordinating the login workflow.
 */
public class AppStartupAddon {

	@Inject
	private UISynchronize uiSync;

	@Inject
	private MApplication application;

	@Inject
	private LoginService loginService;

	private boolean loginRequested;
	private boolean loginShowing;

	/**
	 * Handles the initial application startup event.
	 */
	@Inject
	@Optional
	public void onAppStartupComplete(
			@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Object event) {
		scheduleLogin();
	}

	/**
	 * Reopens the login workflow after authentication expires.
	 */
	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object ignored) {
		scheduleLogin();
	}

	/**
	 * Reopens the login workflow after an explicit logout.
	 */
	@Inject
	@Optional
	public void onLogout(@UIEventTopic(AppTopics.LOGOUT) Object ignored) {
		scheduleLogin();
	}

	/**
	 * Queues the login dialog to run after the current SWT event finishes.
	 *
	 * <p>
	 * This lets startup, logout, and authentication-expired listeners finish
	 * updating the UI before the modal login dialog opens.
	 * </p>
	 */
	private void scheduleLogin() {
		if (loginRequested || loginShowing) {
			return;
		}

		Shell shell = getShell();

		if (shell == null || shell.isDisposed()) {
			return;
		}

		loginRequested = true;

		uiSync.asyncExec(() -> {
			loginRequested = false;

			if (!shell.isDisposed()) {
				showLoginIfNeeded(shell);
			}
		});
	}

	/**
	 * Displays the login dialog if one is not already active.
	 */
	private void showLoginIfNeeded(Shell shell) {
		if (loginShowing) {
			return;
		}

		loginShowing = true;

		try {
			LoginDialog dialog = new LoginDialog(shell, loginService);

			int result = dialog.open();

			if (result != Window.OK) {
				shell.close();
			}
		} finally {
			loginShowing = false;
		}
	}

	/**
	 * Returns the primary SWT shell associated with the application window.
	 *
	 * @return application shell, or {@code null} if unavailable
	 */
	private Shell getShell() {
		if (application.getChildren().isEmpty()) {
			return null;
		}

		MWindow window = application.getChildren().get(0);

		if (window == null) {
			return null;
		}

		Object widget = window.getWidget();

		if (widget instanceof Shell shell) {
			return shell;
		}

		return null;
	}
}