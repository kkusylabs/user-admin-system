package io.github.kkusylabs.useradmin.client.ui.addon;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.dialogs.LoginDialog;
import io.github.kkusylabs.useradmin.client.ui.dialogs.LoginService;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/**
 * Application startup addon responsible for coordinating the login workflow.
 *
 * <p>
 * This addon displays the login dialog during application startup and when
 * authentication expires. The login dialog is deferred until the primary
 * application shell becomes available.
 * </p>
 *
 * <p>
 * Authentication expiration events are received through the Eclipse event
 * broker using {@link AppTopics#AUTH_EXPIRED}.
 * </p>
 */
public class AppStartupAddon {

	@Inject
	private UISynchronize uiSync;

	@Inject
	private MApplication application;
	
	@Inject SessionTokenStore tokenStore;

	@Inject
	private LoginService loginService;

	private boolean loginShowing = false;

	/**
	 * Initializes the startup workflow by asynchronously waiting for the
	 * application shell and displaying the login dialog.
	 */
	@PostConstruct
	public void startup() {
		uiSync.asyncExec(this::showLoginWhenShellExists);
	}

	/**
	 * Handles authentication expiration events by clearing the current session
	 * token and reopening the login dialog.
	 *
	 * @param ignored unused event payload
	 */
	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object ignored) {

		uiSync.asyncExec(() -> {
			tokenStore.clear();
			showLoginWhenShellExists();
		});
	}

	/**
	 * Waits for the primary application shell to become available before
	 * displaying the login dialog.
	 */
	private void showLoginWhenShellExists() {
		Shell shell = getShell();

		if (shell == null || shell.isDisposed()) {
			uiSync.asyncExec(this::showLoginWhenShellExists);
			return;
		}

		showLoginIfNeeded(shell);
	}

	/**
	 * Displays the login dialog if one is not already active.
	 *
	 * <p>
	 * If the login dialog is canceled or closed without successful
	 * authentication, the main application shell is closed.
	 * </p>
	 *
	 * @param shell application shell used as the dialog parent
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
	 * Returns the primary SWT shell associated with the Eclipse application
	 * window.
	 *
	 * @return application shell, or {@code null} if not yet available
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
