package io.github.kkusylabs.useradmin.client.ui.dialogs;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.core.api.auth.AuthApiClient;
import io.github.kkusylabs.useradmin.client.core.api.auth.LoginResponse;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;

public class LoginDialog extends TitleAreaDialog {

	private final AuthApiClient apiClient;
	private final SessionTokenStore tokenStore;
	private final IEventBroker eventBroker;

	private Text usernameText;
	private Text passwordText;

	public LoginDialog(Shell parentShell, 
			AuthApiClient apiClient, 
			SessionTokenStore tokenStore,
			IEventBroker eventBroker) {
		super(parentShell);
		this.apiClient = apiClient;
		this.tokenStore = tokenStore;
		this.eventBroker = eventBroker;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		usernameText = new Text(container, SWT.BORDER);
		passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);

		return container;
	}

	@Override
	protected void okPressed() {
		try {
			String username = usernameText.getText();
			LoginResponse response = apiClient.login(username, passwordText.getText());
			tokenStore.setToken(response.accessToken());
			eventBroker.post(AppTopics.LOGIN_SUCCESS, username);
			super.okPressed();

		} catch (Exception e) {
			setErrorMessage("Login failed");
		}
	}
}
