package io.github.kkusylabs.useradmin.client.ui.composite.user;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;
import io.github.kkusylabs.useradmin.client.core.api.user.CreateUserCapabilities;
import io.github.kkusylabs.useradmin.client.core.api.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.client.core.api.user.EditUserResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.Role;
import io.github.kkusylabs.useradmin.client.core.api.user.UpdateUserCapabilities;
import io.github.kkusylabs.useradmin.client.core.api.user.UserDetailResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserPatch;
import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;
import io.github.kkusylabs.useradmin.client.ui.util.TextUtil;

public class UserDetailsComposite extends Composite {
	private Label passwordLabel;
	
	private Text usernameText;
	private Text passwordText;
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
	
	private UserDetailsActions actions;
	
	private UserListItemResponse currentListItem;
	private UserDetailResponse currentUser;
	private UpdateUserCapabilities updateCapabilities;
	private CreateUserCapabilities createCapabilities;
	
	private boolean createMode;
	private boolean editMode;
	
	private List<DepartmentOption> departmentOptions = List.of();

	public UserDetailsComposite(Composite parent, int style) {
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
		
		passwordLabel = new Label(this, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		passwordText = new Text(this, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
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
	}
	
	public void setActions(UserDetailsActions actions) {
		this.actions = actions;
	}
	
	private void hookListeners() {
		editButton.addListener(SWT.Selection, e -> {
			if (this.actions != null && currentListItem != null) {
				this.actions.editUserRequested(currentListItem);
			}
		});

		saveButton.addListener(SWT.Selection, e -> {
			if (this.actions == null) {
				return;
			}

			if (createMode) {
				this.actions.createUserRequested(buildCreateRequest());
				return;
			}

			if (currentUser != null) {
				this.actions.updateUserRequested(currentUser.id(), buildPatch());
			}
		});

		cancelButton.addListener(SWT.Selection, e -> {
			if (this.actions != null) {
				this.actions.cancelRequested();
			}
		});		
	}
	
	public void showViewMode(UserListItemResponse item) {
		this.currentListItem = item;
		this.currentUser = item == null ? null : item.user();
		this.updateCapabilities = null;
		this.createCapabilities = null;
		this.createMode = false;
		this.editMode = false;

		if (currentUser == null) {
			clear();
			return;
		}

		setPasswordVisible(false);
		
		setUserFields(currentUser);
		setRoleOptions(List.of(Role.values()));
		setDepartmentOptions(currentUser.department() == null ? List.of() : List.of(currentUser.department()));
		selectRole(currentUser.role());
		selectDepartment(currentUser.department());

		setEditable(false);

		showActionButtons(item.canUpdate(), false);
	}
	
	public void showEditMode(EditUserResponse response) {
		this.currentUser = response.user();
		this.updateCapabilities = response.updateCapabilities();

		this.createCapabilities = null;
		this.createMode = false;
		this.editMode = true;

		setPasswordVisible(false);
		
		setUserFields(currentUser);

		setRoleOptions(updateCapabilities.roleOptions().stream().toList());
		setDepartmentOptions(updateCapabilities.departmentOptions());

		selectRole(currentUser.role());
		selectDepartment(currentUser.department());

		applyUpdateCapabilities(updateCapabilities);
		showActionButtons(false, true);
	}
	
	public void showCreateMode(CreateUserCapabilities capabilities) {
		this.currentListItem = null;
		this.currentUser = null;
		this.updateCapabilities = null;
		this.createCapabilities = capabilities;
		this.createMode = true;
		this.editMode = true;

		clearFields();

		setPasswordVisible(true);
		
		setRoleOptions(capabilities.roleOptions().stream().toList());
		setDepartmentOptions(capabilities.departmentOptions());

		if (roleCombo.getItemCount() > 0) {
			roleCombo.select(0);
		}

		if (departmentCombo.getItemCount() > 0) {
			departmentCombo.select(0);
		}

		activeCheckbox.setSelection(true);

		setEditable(capabilities.canCreate());

		showActionButtons(false, capabilities.canCreate());
	}
	

	public void clear() {
		this.currentListItem = null;
		this.currentUser = null;
		this.updateCapabilities = null;
		this.createCapabilities = null;
		this.createMode = false;
		this.editMode = false;

		clearFields();
		setPasswordVisible(false);
		setRoleOptions(List.of());
		setDepartmentOptions(List.of());
		setEditable(false);

		showActionButtons(false, false);
	}
	
	private void setUserFields(UserDetailResponse user) {
		usernameText.setText(TextUtil.nullToEmpty(user.username()));
		passwordText.setText("");
		fullNameText.setText(TextUtil.nullToEmpty(user.fullName()));
		emailText.setText(TextUtil.nullToEmpty(user.email()));
		phoneText.setText(TextUtil.nullToEmpty(user.phone()));
		jobTitleText.setText(TextUtil.nullToEmpty(user.jobTitle()));
		activeCheckbox.setSelection(user.active());
	}
	
	private void clearFields() {
		usernameText.setText("");
		passwordText.setText("");
		fullNameText.setText("");
		emailText.setText("");
		phoneText.setText("");
		jobTitleText.setText("");
		activeCheckbox.setSelection(false);
	}
	
	private void setEditable(boolean editable) {
		usernameText.setEnabled(createMode && editable);
		passwordText.setEnabled(createMode && editable);

		fullNameText.setEnabled(editable);
		emailText.setEnabled(editable);
		phoneText.setEnabled(editable);
		jobTitleText.setEnabled(editable);
		roleCombo.setEnabled(editable);
		departmentCombo.setEnabled(editable);
		activeCheckbox.setEnabled(editable);
	}
	
	private void applyUpdateCapabilities(UpdateUserCapabilities capabilities) {
		usernameText.setEnabled(false);
		passwordText.setEnabled(false);

		fullNameText.setEnabled(capabilities.canEditProfile());
		emailText.setEnabled(capabilities.canEditProfile());
		phoneText.setEnabled(capabilities.canEditProfile());

		jobTitleText.setEnabled(capabilities.canEditJobTitle());
		roleCombo.setEnabled(capabilities.canEditRole());
		departmentCombo.setEnabled(capabilities.canEditDepartment());
		activeCheckbox.setEnabled(capabilities.canEditActive());
	}
	
	private CreateUserRequest buildCreateRequest() {
		return new CreateUserRequest(
				TextUtil.trimToNull(usernameText.getText()),
				passwordText.getText(),
				TextUtil.trimToNull(fullNameText.getText()),
				TextUtil.trimToNull(emailText.getText()),
				TextUtil.trimToNull(phoneText.getText()),
				TextUtil.trimToNull(jobTitleText.getText()),
				getSelectedDepartmentId(),
				getSelectedRole());
	}
	
	private UserPatch buildPatch() {
		UserPatch patch = new UserPatch();

		if (updateCapabilities == null) {
			return patch;
		}

		if (updateCapabilities.canEditProfile()) {
			patch.fullName(TextUtil.trimToNull(fullNameText.getText()));
			patch.email(TextUtil.trimToNull(emailText.getText()));
			patch.phone(TextUtil.trimToNull(phoneText.getText()));
		}

		if (updateCapabilities.canEditJobTitle()) {
			patch.jobTitle(TextUtil.trimToNull(jobTitleText.getText()));
		}

		if (updateCapabilities.canEditRole()) {
			patch.role(getSelectedRole());
		}

		if (updateCapabilities.canEditDepartment()) {
			patch.departmentId(getSelectedDepartmentId());
		}

		if (updateCapabilities.canEditActive()) {
			patch.active(activeCheckbox.getSelection());
		}

		return patch;
	}
	
	private void setRoleOptions(List<Role> roles) {
		roleCombo.removeAll();

		for (Role role : roles) {
			roleCombo.add(role.name());
		}
	}
	
	private void setDepartmentOptions(List<DepartmentOption> departments) {
		this.departmentOptions = departments == null ? List.of() : departments;

		departmentCombo.removeAll();

		for (DepartmentOption department : this.departmentOptions) {
			departmentCombo.add(department.name());
		}
	}
	
	private void selectRole(Role role) {
		if (role == null) {
			roleCombo.deselectAll();
			return;
		}

		String roleName = role.name();

		for (int i = 0; i < roleCombo.getItemCount(); i++) {
			if (roleName.equals(roleCombo.getItem(i))) {
				roleCombo.select(i);
				return;
			}
		}

		roleCombo.deselectAll();
	}

	private void selectDepartment(DepartmentOption department) {
		if (department == null) {
			departmentCombo.deselectAll();
			return;
		}

		for (int i = 0; i < departmentOptions.size(); i++) {
			if (department.id().equals(departmentOptions.get(i).id())) {
				departmentCombo.select(i);
				return;
			}
		}

		departmentCombo.deselectAll();
	}
	
	private Role getSelectedRole() {
		int index = roleCombo.getSelectionIndex();

		if (index < 0) {
			return null;
		}

		return Role.valueOf(roleCombo.getItem(index));
	}
	
	private Long getSelectedDepartmentId() {
		int index = departmentCombo.getSelectionIndex();

		if (index < 0 || index >= departmentOptions.size()) {
			return null;
		}

		return departmentOptions.get(index).id();
	}
	
	private void setPasswordVisible(boolean visible) {
		passwordLabel.setVisible(visible);
		passwordText.setVisible(visible);

		((GridData) passwordLabel.getLayoutData()).exclude = !visible;
		((GridData) passwordText.getLayoutData()).exclude = !visible;

		layout(true, true);
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
	
	public boolean hasPendingChanges() {
		return createMode || editMode;
	}
}
