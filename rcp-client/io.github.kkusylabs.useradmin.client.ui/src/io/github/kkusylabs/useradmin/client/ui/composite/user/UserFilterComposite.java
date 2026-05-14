package io.github.kkusylabs.useradmin.client.ui.composite.user;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;

public class UserFilterComposite extends Composite {

	private Text nameText;
	
	private ComboViewer roleViewer;
	
	private ComboViewer departmentViewer;
	
	private ComboViewer activeViewer;
	
	private Button searchButton;
	
	private Button clearButton;
	
	public UserFilterComposite(Composite parent, int style) {
		super(parent, style);
		createControls();

	}

	private void createControls() {
		GridLayout layout = new GridLayout(9, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;
		setLayout(layout);

		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name:");

		nameText = new Text(this, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		GridData nameData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		nameData.widthHint = 160;
		nameText.setLayoutData(nameData);

		Label roleLabel = new Label(this, SWT.NONE);
		roleLabel.setText("Role:");

		roleViewer = new ComboViewer(this, SWT.READ_ONLY);
		roleViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		roleViewer.getCombo().setItems("Any", "Admin", "Manager", "User");

		Label departmentLabel = new Label(this, SWT.NONE);
		departmentLabel.setText("Department:");

		departmentViewer = new ComboViewer(this, SWT.READ_ONLY);
		GridData departmentData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		departmentData.widthHint = 130;
		departmentViewer.getCombo().setLayoutData(departmentData);
		departmentViewer.getCombo().setItems("Any", "Sales", "Engineering", "Support");

		Label activeLabel = new Label(this, SWT.NONE);
		activeLabel.setText("Active:");

		activeViewer = new ComboViewer(this, SWT.READ_ONLY);
		GridData activeData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		activeData.widthHint = 80;
		activeViewer.getCombo().setLayoutData(activeData);
		activeViewer.getCombo().setItems("Any", "Active", "Inactive");

		Composite buttons = new Composite(this, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		searchButton = SwtUtil.createPushButton(buttons, "Search");

		clearButton = SwtUtil.createPushButton(buttons, "Clear");
	}

}
