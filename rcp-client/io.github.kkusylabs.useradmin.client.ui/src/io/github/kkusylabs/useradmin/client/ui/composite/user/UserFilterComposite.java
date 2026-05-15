package io.github.kkusylabs.useradmin.client.ui.composite.user;

import java.util.List;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import io.github.kkusylabs.useradmin.client.core.api.common.SortSpec;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;
import io.github.kkusylabs.useradmin.client.core.api.user.Role;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListFilter;
import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;
import io.github.kkusylabs.useradmin.client.ui.util.TextUtil;

public class UserFilterComposite extends Composite {

	private Text searchText;
	
	private ComboViewer roleViewer;
	
	private ComboViewer departmentViewer;
	
	private ComboViewer activeViewer;
	
	private Button searchButton;
	
	private Button clearButton;
	
	private UserFilterActions actions;
	
	private List<DepartmentOption> departmentOptions = List.of();
	
	public UserFilterComposite(Composite parent, int style) {
		super(parent, style);
		createControls();
		hookListeners();
		clear();
	}

	private void createControls() {
		GridLayout layout = new GridLayout(9, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;
		setLayout(layout);

		Label searchLabel = new Label(this, SWT.NONE);
		searchLabel.setText("Search:");

		searchText = new Text(this, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		GridData searchData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		searchData.widthHint = 180;
		searchText.setLayoutData(searchData);

		Label roleLabel = new Label(this, SWT.NONE);
		roleLabel.setText("Role:");

		roleViewer = new ComboViewer(this, SWT.READ_ONLY);
		roleViewer.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false));

		Label departmentLabel = new Label(this, SWT.NONE);
		departmentLabel.setText("Department:");

		departmentViewer = new ComboViewer(this, SWT.READ_ONLY);

		GridData departmentData =
				new GridData(SWT.FILL, SWT.CENTER, false, false);

		departmentData.widthHint = 140;

		departmentViewer.getCombo().setLayoutData(departmentData);

		Label activeLabel = new Label(this, SWT.NONE);
		activeLabel.setText("Active:");

		activeViewer = new ComboViewer(this, SWT.READ_ONLY);

		GridData activeData =
				new GridData(SWT.FILL, SWT.CENTER, false, false);

		activeData.widthHint = 90;

		activeViewer.getCombo().setLayoutData(activeData);

		Composite buttons = new Composite(this, SWT.NONE);

		buttons.setLayoutData(
				new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		GridLayout buttonLayout = new GridLayout(2, false);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;

		buttons.setLayout(buttonLayout);

		searchButton = SwtUtil.createPushButton(buttons, "Search");

		clearButton = SwtUtil.createPushButton(buttons, "Clear");
	}
	
	public void setActions(UserFilterActions actions) {
		this.actions = actions;
	}
	
	private void hookListeners() {
		searchButton.addListener(SWT.Selection, e -> {
			if (this.actions != null) {
				this.actions.searchRequested(getFilter());
			}
		});

		clearButton.addListener(SWT.Selection, e -> {
			clear();

			if (this.actions != null) {
				this.actions.clearFilterRequested();
			}
		});

		searchText.addListener(SWT.DefaultSelection, e -> {
			if (this.actions != null) {
				this.actions.searchRequested(getFilter());
			}
		});
	}
	
	public void setDepartmentOptions(List<DepartmentOption> departments) {
		this.departmentOptions =
				departments == null ? List.of() : departments;

		departmentViewer.getCombo().removeAll();

		departmentViewer.getCombo().add("Any");

		for (DepartmentOption department : this.departmentOptions) {
			departmentViewer.getCombo().add(department.name());
		}

		departmentViewer.getCombo().select(0);
	}
	
	public UserListFilter getFilter() {
		String search = TextUtil.trimToNull(searchText.getText());

		Boolean active = switch (activeViewer.getCombo().getText()) {
		case "Active" -> Boolean.TRUE;
		case "Inactive" -> Boolean.FALSE;
		default -> null;
		};

		Role role = switch (roleViewer.getCombo().getText()) {
		case "Admin" -> Role.ADMIN;
		case "Manager" -> Role.MANAGER;
		case "User" -> Role.USER;
		default -> null;
		};

		Long departmentId = getSelectedDepartmentId();

		return new UserListFilter(
				search,
				active,
				departmentId,
				role,
				defaultSort());
	}

	public void clear() {
		searchText.setText("");

		initializeRoleOptions();
		initializeDepartmentOptions();
		initializeActiveOptions();
	}

	private void initializeRoleOptions() {
		roleViewer.getCombo().removeAll();

		roleViewer.getCombo().add("Any");
		roleViewer.getCombo().add("Admin");
		roleViewer.getCombo().add("Manager");
		roleViewer.getCombo().add("User");

		roleViewer.getCombo().select(0);
	}

	private void initializeDepartmentOptions() {
		departmentViewer.getCombo().removeAll();

		departmentViewer.getCombo().add("Any");

		for (DepartmentOption department : departmentOptions) {
			departmentViewer.getCombo().add(department.name());
		}

		departmentViewer.getCombo().select(0);
	}

	private void initializeActiveOptions() {
		activeViewer.getCombo().removeAll();

		activeViewer.getCombo().add("Any");
		activeViewer.getCombo().add("Active");
		activeViewer.getCombo().add("Inactive");

		activeViewer.getCombo().select(0);
	}
	
	private Long getSelectedDepartmentId() {
		int index = departmentViewer.getCombo().getSelectionIndex();

		if (index <= 0) {
			return null;
		}

		int optionIndex = index - 1;

		if (optionIndex >= departmentOptions.size()) {
			return null;
		}

		return departmentOptions.get(optionIndex).id();
	}
	
	private SortSpec defaultSort() {
		return SortSpec.asc("username");
	}

}
