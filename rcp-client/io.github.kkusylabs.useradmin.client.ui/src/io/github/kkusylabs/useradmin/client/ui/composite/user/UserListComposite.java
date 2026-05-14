package io.github.kkusylabs.useradmin.client.ui.composite.user;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;

public class UserListComposite extends Composite {

	private TableViewer viewer;

	private Button addButton;

	private Button deleteButton;

	private Button refreshButton;

	private Button firstButton;

	private Button previousButton;

	private Label pageLabel;

	private Button nextButton;

	private Button lastButton;

	public UserListComposite(Composite parent, int style) {
		super(parent, style);
		createControls();
	}

	private void createControls() {
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 8;
		layout.marginHeight = 8;
		layout.verticalSpacing = 8;
		setLayout(layout);

		Composite buttonBar = new Composite(this, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		GridLayout buttonLayout = new GridLayout(3, false);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonLayout.horizontalSpacing = 6;
		buttonBar.setLayout(buttonLayout);

		addButton = SwtUtil.createPushButton(buttonBar, "Add...");
		deleteButton = SwtUtil.createPushButton(buttonBar, "Delete");
		refreshButton = SwtUtil.createPushButton(buttonBar, "Refresh");

		viewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite pagingBar = new Composite(this, SWT.NONE);
		pagingBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		pagingBar.setLayout(new GridLayout(5, false));

		firstButton = new Button(pagingBar, SWT.PUSH);
		firstButton.setText("<<");

		previousButton = new Button(pagingBar, SWT.PUSH);
		previousButton.setText("<");

		pageLabel = new Label(pagingBar, SWT.CENTER);
		pageLabel.setText("Page 1 of 1");
		pageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		nextButton = new Button(pagingBar, SWT.PUSH);
		nextButton.setText(">");

		lastButton = new Button(pagingBar, SWT.PUSH);
		lastButton.setText(">>");

		int buttonWidth = Math.max(
				Math.max(firstButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
						previousButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x),
				Math.max(nextButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
						lastButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x));

		buttonWidth += 10;

		firstButton.setLayoutData(new GridData(buttonWidth, SWT.DEFAULT));
		previousButton.setLayoutData(new GridData(buttonWidth, SWT.DEFAULT));
		nextButton.setLayoutData(new GridData(buttonWidth, SWT.DEFAULT));
		lastButton.setLayoutData(new GridData(buttonWidth, SWT.DEFAULT));
	}

}
