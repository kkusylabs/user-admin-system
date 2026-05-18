package io.github.kkusylabs.useradmin.client.ui.composite.department;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.core.api.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentDetailsResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;
import io.github.kkusylabs.useradmin.client.ui.util.TextUtil;


/**
 * Composite displaying department details and edit workflows.
 *
 * <p>
 * This composite supports:
 * </p>
 *
 * <ul>
 *   <li>viewing department details</li>
 *   <li>editing existing departments</li>
 *   <li>creating new departments</li>
 *   <li>capability-aware UI behavior</li>
 * </ul>
 *
 * <p>
 * User interactions are delegated through
 * {@link DepartmentDetailsActions}.
 * </p>
 */
public class DepartmentDetailsComposite extends Composite {

	private Text nameText;
	private Text descriptionText;
	private Button activeCheckbox;

	private Button editButton;
	private Button saveButton;
	private Button cancelButton;

	private DepartmentDetailsActions actions;

	private DepartmentListItemResponse currentListItem;
	private DepartmentDetailsResponse currentDepartment;

	private boolean createMode;
	private boolean editMode;

	/**
	 * Creates the department details composite.
	 *
	 * @param parent parent composite
	 * @param style SWT style flags
	 */
	public DepartmentDetailsComposite(Composite parent, int style) {
		super(parent, style);

		createControls();
		hookListeners();
		clear();
	}

	private void createControls() {
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 8;
		layout.marginHeight = 8;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 8;
		setLayout(layout);

		Label title = new Label(this, SWT.NONE);
		title.setText("Department Details:");

		GridData titleData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		titleData.horizontalSpan = 2;
		title.setLayoutData(titleData);

		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData separatorData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		separatorData.horizontalSpan = 2;
		separatorData.verticalIndent = 2;
		separatorData.heightHint = 8;
		separator.setLayoutData(separatorData);

		new Label(this, SWT.NONE).setText("Name:");
		nameText = new Text(this, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label descriptionLabel = new Label(this, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, false, false));
		descriptionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridData descriptionData = new GridData(SWT.FILL, SWT.TOP, true, false);
		descriptionData.heightHint = descriptionText.getLineHeight() * 5;
		descriptionText.setLayoutData(descriptionData);
		
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
	}

	/**
	 * Registers callbacks for department detail actions.
	 *
	 * @param actions action handler
	 */
	public void setActions(DepartmentDetailsActions actions) {
		this.actions = actions;
	}

	private void hookListeners() {
		editButton.addListener(SWT.Selection, e -> {
			if (actions != null && currentListItem != null) {
				actions.editDepartmentRequested(currentListItem);
			}
		});

		saveButton.addListener(SWT.Selection, e -> {
			if (actions == null) {
				return;
			}

			if (createMode) {
				actions.createDepartmentRequested(buildCreateRequest());
				return;
			}

			if (currentDepartment != null) {
				actions.updateDepartmentRequested(
						currentDepartment.id(),
						buildUpdateRequest());
			}
		});

		cancelButton.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.cancelRequested();
			}
		});
	}

	/**
	 * Displays the specified department in read-only view mode.
	 *
	 * @param item department list item to display
	 */
	public void showViewMode(DepartmentListItemResponse item) {
		this.currentListItem = item;
		this.currentDepartment = item == null ? null : item.department();
		this.createMode = false;
		this.editMode = false;

		if (currentDepartment == null) {
			clear();
			return;
		}

		setDepartmentFields(currentDepartment);
		setEditable(false);

		showActionButtons(item.canUpdate(), false);
	}

	/**
	 * Displays the specified department in edit mode.
	 *
	 * @param item department list item to edit
	 */
	public void showEditMode(DepartmentListItemResponse item) {
		this.currentListItem = item;
		this.currentDepartment = item == null ? null : item.department();
		this.createMode = false;
		this.editMode = true;

		if (currentDepartment == null) {
			clear();
			return;
		}

		setDepartmentFields(currentDepartment);
		setEditable(item.canUpdate());

		showActionButtons(false, item.canUpdate());
	}

	/**
	 * Displays the create department workflow.
	 */
	public void showCreateMode() {
		this.currentListItem = null;
		this.currentDepartment = null;
		this.createMode = true;
		this.editMode = true;

		clearFields();

		activeCheckbox.setSelection(true);
		activeCheckbox.setEnabled(false);

		nameText.setEnabled(true);
		descriptionText.setEnabled(true);

		showActionButtons(false, true);
	}

	/**
	 * Clears the current department details and resets the composite state.
	 */
	public void clear() {
		this.currentListItem = null;
		this.currentDepartment = null;
		this.createMode = false;
		this.editMode = false;

		clearFields();
		setEditable(false);

		showActionButtons(false, false);
	}

	/**
	 * Returns whether the composite is currently in a create or edit workflow.
	 *
	 * @return {@code true} if editing or creating a department
	 */
	public boolean hasPendingChanges() {
		return createMode || editMode;
	}

	private void setDepartmentFields(DepartmentDetailsResponse department) {
		nameText.setText(TextUtil.nullToEmpty(department.name()));
		descriptionText.setText(TextUtil.nullToEmpty(department.description()));
		activeCheckbox.setSelection(department.active());
	}

	private void clearFields() {
		nameText.setText("");
		descriptionText.setText("");
		activeCheckbox.setSelection(false);
	}

	private void setEditable(boolean editable) {
		nameText.setEnabled(editable);
		descriptionText.setEnabled(editable);
		activeCheckbox.setEnabled(editable);
	}

	private CreateDepartmentRequest buildCreateRequest() {
		return new CreateDepartmentRequest(
				TextUtil.trimToNull(nameText.getText()),
				TextUtil.trimToNull(descriptionText.getText()));
	}

	private UpdateDepartmentRequest buildUpdateRequest() {
		return new UpdateDepartmentRequest(
				TextUtil.trimToNull(nameText.getText()),
				TextUtil.trimToNull(descriptionText.getText()),
				activeCheckbox.getSelection());
	}

	private void showActionButtons(boolean showEdit, boolean showSaveCancel) {
		setButtonVisible(editButton, showEdit);
		setButtonVisible(saveButton, showSaveCancel);
		setButtonVisible(cancelButton, showSaveCancel);

		layout(true, true);
	}

	private void setButtonVisible(Button button, boolean visible) {
		button.setVisible(visible);
		((GridData) button.getLayoutData()).exclude = !visible;
	}
}
