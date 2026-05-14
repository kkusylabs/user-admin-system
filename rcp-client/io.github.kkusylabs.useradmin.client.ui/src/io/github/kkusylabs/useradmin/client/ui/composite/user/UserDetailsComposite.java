package io.github.kkusylabs.useradmin.client.ui.composite.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;

public class UserDetailsComposite extends Composite {
	private Text usernameText;
	private Text fullNameText;
	private Text emailText;
	private Text phoneText;
	private Text jobTitleText;
	private Combo roleCombo;
	private Combo departmentCombo;
	private Button activeCheckbox;
	private Button editButton;
	private Button saveButton;
	private Button cancelButton;

	public UserDetailsComposite(Composite parent, int style) {
		super(parent, style);
		createControls();
	}

	private void createControls() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 8;
		layout.marginHeight = 8;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 8;
		setLayout(layout);

		Label title = new Label(this, SWT.NONE);
		title.setText("User Details:");

		GridData titleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		titleData.horizontalSpan = 2;
		title.setLayoutData(titleData);

		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData separatorData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		separatorData.horizontalSpan = 2;
		separatorData.verticalIndent = 2;
		separatorData.heightHint = 8;
		separator.setLayoutData(separatorData);

		new Label(this, SWT.NONE).setText("Username:");
		usernameText = new Text(this, SWT.BORDER);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(this, SWT.NONE).setText("Full Name:");
		fullNameText = new Text(this, SWT.BORDER);
		fullNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		new Label(this, SWT.NONE).setText("Email:");
		emailText = new Text(this, SWT.BORDER);
		emailText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(this, SWT.NONE).setText("Phone:");
		phoneText = new Text(this, SWT.BORDER);
		phoneText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(this, SWT.NONE).setText("Job Title:");
		jobTitleText = new Text(this, SWT.BORDER);
		jobTitleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		new Label(this, SWT.NONE).setText("Role:");
		roleCombo = new Combo(this, SWT.READ_ONLY);
		roleCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		new Label(this, SWT.NONE).setText("Department:");
		departmentCombo = new Combo(this, SWT.READ_ONLY);
		departmentCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		new Label(this, SWT.NONE).setText("Active:");
		activeCheckbox = new Button(this, SWT.CHECK);
		activeCheckbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		Composite buttons = new Composite(this, SWT.NONE);

		GridData buttonData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1);
		buttonData.verticalIndent = 12;
		buttons.setLayoutData(buttonData);

		GridLayout buttonLayout = new GridLayout(3, false);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonLayout.horizontalSpacing = 6;
		buttons.setLayout(buttonLayout);

		editButton = SwtUtil.createPushButton(buttons, "Edit");

		saveButton = SwtUtil.createPushButton(buttons, "Save");

		cancelButton = SwtUtil.createPushButton(buttons, "Cancel");

		// Wire these to your UserDetailsActions/action handler later.
		// editButton.addListener(SWT.Selection, e -> ...);
		// saveButton.addListener(SWT.Selection, e -> ...);
		// cancelButton.addListener(SWT.Selection, e -> ...);
	}

}
