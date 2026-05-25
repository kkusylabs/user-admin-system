package io.github.kkusylabs.useradmin.client.ui.addon;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents; // Make sure to import this
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

	private boolean loginShowing = false;

	/**
	 * Handles the initial application startup event. The shell is guaranteed to
	 * exist at this point.
	 */
	@Inject
	@Optional
	public void onAppStartupComplete(@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Object event) {
		triggerDeferredLogin();
	}

	/**
	 * Reopens the login workflow after authentication expires.
	 */
	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object ignored) {
		triggerDeferredLogin();
	}

	/**
	 * Reopens the login workflow after an explicit logout.
	 */
	@Inject
	@Optional
	public void onLogout(@UIEventTopic(AppTopics.LOGOUT) Object ignored) {
		triggerDeferredLogin();
	}

	/**
	 * Safely queues the login dialog to the end of the SWT event loop. * For
	 * startup: Gives the window a split second to finish rendering. For
	 * logout/expired events: Allows concurrent UI listeners to wipe their widgets
	 * first.
	 */
	private void triggerDeferredLogin() {
		Shell shell = getShell();
		if (shell != null && !shell.isDisposed()) {
			uiSync.asyncExec(() -> showLoginIfNeeded(shell));
		}
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
	 * Returns the primary SWT shell associated with the Eclipse application window.
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