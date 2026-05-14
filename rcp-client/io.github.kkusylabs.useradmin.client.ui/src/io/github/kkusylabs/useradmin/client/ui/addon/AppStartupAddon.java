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

public class AppStartupAddon {

	@Inject
	private UISynchronize uiSync;

	@Inject
	private MApplication application;
	
	@Inject SessionTokenStore tokenStore;

	@Inject
	private LoginService loginService;

	private boolean loginShowing = false;

	@PostConstruct
	public void startup() {
		uiSync.asyncExec(this::showLoginWhenShellExists);
	}

	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object ignored) {

		uiSync.asyncExec(() -> {
			tokenStore.setToken(null);
			showLoginWhenShellExists();
		});
	}

	private void showLoginWhenShellExists() {
		Shell shell = getShell();

		if (shell == null || shell.isDisposed()) {
			uiSync.asyncExec(this::showLoginWhenShellExists);
			return;
		}

		showLoginIfNeeded(shell);
	}

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
