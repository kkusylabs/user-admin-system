package io.github.kkusylabs.useradmin.client.ui.dialogs;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.core.api.UnauthorizedException;

public class LoginDialog extends TitleAreaDialog {

	private final LoginService loginService;
	private boolean loginInProgress;

	private Text usernameText;
	private Text passwordText;
	
	public LoginDialog(
			Shell parentShell, 
			LoginService loginService) {
		super(parentShell);
		this.loginService = loginService;
	}

	@Override
	public void create() {
		super.create();

		setTitle("Login");
		setMessage("Enter your user name and password", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(2, false));

		Label usernameLabel = new Label(container, SWT.NONE);
		usernameLabel.setText("User name:");

		usernameText = new Text(container, SWT.BORDER);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setText("Password:");

		passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		return area;
	}

	@Override
	protected void okPressed() {
		if (loginInProgress) {
			return;
		}
		
		String username = usernameText.getText().trim();
		String password = passwordText.getText();
		
		if (username.isEmpty()) {
			setErrorMessage("Enter a username.");
			usernameText.setFocus();
			return;
		}
		
		if (password.isEmpty()) {
			setErrorMessage("Enter a password.");
			passwordText.setFocus();
			return;
		}
		
		loginInProgress = true;
		setErrorMessage(null);
		setMessage("Signing in...");
		
		getButton(OK).setEnabled(false);
		getButton(CANCEL).setEnabled(false);
		
		loginService.loginAsync(username, password)
			.whenComplete((response, error) -> 
				getShell().getDisplay().asyncExec(() -> {
					if (getShell() == null || getShell().isDisposed()) {
						return;
					}
					
					if (error != null) {
						loginInProgress = false;
						getButton(OK).setEnabled(true);
						getButton(CANCEL).setEnabled(true);
						setErrorMessage(toLoginErrorMessage(error));

						if (shouldClearPassword(error)) {
							passwordText.setText("");
							passwordText.setFocus();
						}
						
						return;
					}
					
					superOkPressed();
				}));
	}
	
	private String toLoginErrorMessage(Throwable error) {
		Throwable cause = unwrap(error);

		if (cause instanceof UnauthorizedException) {
			return "Invalid username or password.";
		}

		return "Login failed.";
	}

	private Throwable unwrap(Throwable error) {
		if (error instanceof CompletionException && error.getCause() != null) {
			return error.getCause();
		}

		if (error instanceof ExecutionException && error.getCause() != null) {
			return error.getCause();
		}

		return error;
	}
	
	private boolean shouldClearPassword(Throwable error) {
		Throwable cause = unwrap(error);
		return cause instanceof UnauthorizedException;
	}
	
	private void superOkPressed() {
	    super.okPressed();
	}
}
