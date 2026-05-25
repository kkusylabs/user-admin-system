package io.github.kkusylabs.useradmin.client.ui.part;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import io.github.kkusylabs.useradmin.client.core.api.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentApiClient;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
import io.github.kkusylabs.useradmin.client.ui.composite.department.DepartmentDetailsActions;
import io.github.kkusylabs.useradmin.client.ui.composite.department.DepartmentDetailsComposite;
import io.github.kkusylabs.useradmin.client.ui.composite.department.DepartmentListActions;
import io.github.kkusylabs.useradmin.client.ui.composite.department.DepartmentListComposite;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import io.github.kkusylabs.useradmin.client.ui.runtime.UiApiRunner;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 * Eclipse part responsible for department administration workflows.
 *
 * <p>
 * This part coordinates interactions between:
 * </p>
 *
 * <ul>
 *   <li>{@link DepartmentListComposite}</li>
 *   <li>{@link DepartmentDetailsComposite}</li>
 *   <li>{@link DepartmentApiClient}</li>
 * </ul>
 *
 * <p>
 * Supported workflows include:
 * </p>
 *
 * <ul>
 *   <li>loading departments</li>
 *   <li>department selection synchronization</li>
 *   <li>department creation</li>
 *   <li>department editing</li>
 *   <li>department deletion</li>
 *   <li>unsaved change handling</li>
 * </ul>
 *
 * <p>
 * REST operations are executed asynchronously through
 * {@link UiApiRunner}.
 * </p>
 */
public class DepartmentPart {

	private DepartmentListComposite departmentListComposite;
	private DepartmentDetailsComposite departmentDetailsComposite;

	@Inject
	private UiApiRunner apiRunner;

	@Inject
	private DepartmentApiClient departmentApiClient;
	
	@Inject
	private SessionTokenStore tokenStore;

	private DepartmentListItemResponse selectedDepartment;
	
	private boolean sessionEnabled;
	
	private boolean detailsEditing;

	private int apiBusyCount;
	
	private boolean suppressSelectionEvents;

	/**
	 * Creates the department administration UI.
	 *
	 * @param parent parent composite
	 */
	@PostConstruct
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		departmentListComposite = new DepartmentListComposite(sash, SWT.BORDER);
		departmentDetailsComposite = new DepartmentDetailsComposite(sash, SWT.BORDER);

		sash.setWeights(new int[] { 55, 45 });

		wireEvents();

		sessionEnabled = tokenStore.hasToken();

		updateUiEnabledState();

		if (sessionEnabled) {
			loadDepartments();
		}
	}

	private void wireEvents() {
		wireDepartmentListActions();
		wireDepartmentDetailsActions();
	}

	private void wireDepartmentListActions() {
		departmentListComposite.setActions(new DepartmentListActions() {

			@Override
			public void addDepartmentRequested() {
				if (canUseDepartmentListActions()) {
					beginCreateDepartment();
				}
			}

			@Override
			public void deleteDepartmentRequested(DepartmentListItemResponse department) {
				if (canUseDepartmentListActions()) {
					deleteDepartment(department);
				}
			}

			@Override
			public void departmentSelected(DepartmentListItemResponse department) {
				if (suppressSelectionEvents) {
					return;
				}
				
				if (canAcceptDepartmentSelection()) {
					selectDepartment(department);
				}
			}
		});
	}

	private void wireDepartmentDetailsActions() {
		departmentDetailsComposite.setActions(new DepartmentDetailsActions() {

			@Override
			public void editDepartmentRequested(DepartmentListItemResponse department) {
				if (canUseDepartmentDetailsActions()) {
					beginEditDepartment(department);
				}
			}

			@Override
			public void createDepartmentRequested(CreateDepartmentRequest request) {
				if (canUseDepartmentDetailsActions()) {
					createDepartment(request);
				}
			}

			@Override
			public void updateDepartmentRequested(long departmentId, UpdateDepartmentRequest request) {
				if (canUseDepartmentDetailsActions()) {
					updateDepartment(departmentId, request);
				}
			}

			@Override
			public void cancelRequested() {
				if (canUseDepartmentDetailsActions()) {
					cancelEditing();
				}
			}
		});
	}

	/**
	 * Handles successful authentication events by loading department data.
	 *
	 * @param username authenticated username
	 */
	@Inject
	@Optional
	public void onLoginSuccess(@UIEventTopic(AppTopics.LOGIN_SUCCESS) String username) {
		sessionEnabled = true;
		apiBusyCount = 0;
		detailsEditing = false;
		selectedDepartment = null;
		updateUiEnabledState();
		loadDepartments();
	}
	
	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object event) {
		sessionEnabled = false;
		apiBusyCount = 0;
		detailsEditing = false;
		clearDepartmentUi();
		updateUiEnabledState();
	}

	private void loadDepartments() {
		apiRunner.task(departmentApiClient::getDepartments)
				.onControl(departmentListComposite)
				.onBefore(this::beginApi)
				.onSuccess(this::showDepartments)
				.onAfter(this::endApi)
				.onError(
						"Load Failed", "Could not fetch departments.")
				.execute();
	}

	private void showDepartments(DepartmentListResponse response) {
		departmentListComposite.setDepartments(response);
		reconcileSelectedDepartment(response);
	}

	private void reconcileSelectedDepartment(DepartmentListResponse response) {
		Long selectedId = selectedDepartmentId();

		if (selectedId == null) {
			selectDepartmentEverywhere(null);
			return;
		}

		DepartmentListItemResponse refreshed = findDepartment(response, selectedId);
		selectDepartmentEverywhere(refreshed);
	}
	
	private Long selectedDepartmentId() {
		return selectedDepartment == null
				? null
				: selectedDepartment.department().id();
	}

	private DepartmentListItemResponse findDepartment(
			DepartmentListResponse response,
			Long departmentId) {

		if (response == null || response.departments() == null || departmentId == null) {
			return null;
		}

		return response.departments()
				.stream()
				.filter(item -> departmentId.equals(item.department().id()))
				.findFirst()
				.orElse(null);
	}

	private void beginCreateDepartment() {
		selectedDepartment = null;
		departmentDetailsComposite.showCreateMode();
		setDetailsEditing(true);
	}
	
	private void beginEditDepartment(DepartmentListItemResponse department) {
		selectedDepartment = department;
		departmentDetailsComposite.showEditMode(department);
		setDetailsEditing(true);
	}
	
	private void createDepartment(CreateDepartmentRequest request) {
		apiRunner.task(() -> departmentApiClient.createDepartment(request))
				.onControl(departmentDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(created -> {
					setDetailsEditing(false);
					selectDepartment(created);
					loadDepartments();

					showSuccess("Department Created", "Department created successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Create Failed", "Could not create department.")
				.execute();
	}
	
	private void updateDepartment(
			long departmentId,
			UpdateDepartmentRequest request) {

		apiRunner.task(() ->
				departmentApiClient.updateDepartment(departmentId, request))
				.onControl(departmentDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(updated -> {
					setDetailsEditing(false);
					selectDepartment(updated);
					replaceAndSelectDepartment(updated);
					
					showSuccess("Department Updated", "Department updated successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Update Failed", "Could not update department.")
				.execute();
	}

	private void deleteDepartment(DepartmentListItemResponse department) {
		if (!confirmDeleteDepartment(department)) {
			return;
		}

		apiRunner.task(() ->
				departmentApiClient.deleteDepartment(department.department().id()))
				.onControl(departmentListComposite)
				.onBefore(this::beginApi)
				.onSuccess(v -> {
					clearSelectionIfDeleted(department);
					loadDepartments();

					showSuccess("Department Deleted", "Department deleted successfully.");
				})
				.onAfter(this::endApi)
				.onError("Delete Failed", "Could not delete department.")
				.execute();
	}
	
	private boolean confirmDeleteDepartment(DepartmentListItemResponse department) {
		return MessageDialog.openConfirm(
				departmentListComposite.getShell(),
				"Delete Department",
				"Delete department '" + department.department().name() + "'?");
	}

	private void selectDepartment(DepartmentListItemResponse department) {
		selectedDepartment = department;

		if (department == null) {
			departmentDetailsComposite.clear();
			return;
		}

		departmentDetailsComposite.showViewMode(department);
	}
	
	private void selectDepartmentEverywhere(DepartmentListItemResponse department) {
		selectDepartment(department);
		selectDepartmentInList(department == null ? null : department.department().id());
	}
	
	private void selectDepartmentInList(Long departmentId) {
		suppressSelectionEvents = true;
		try {
			departmentListComposite.selectDepartment(departmentId);
		} finally {
			suppressSelectionEvents = false;
		}
	}
	
	private void replaceAndSelectDepartment(DepartmentListItemResponse updated) {
		suppressSelectionEvents = true;
		try {
			departmentListComposite.replaceDepartment(updated);
			departmentListComposite.selectDepartment(updated.department().id());
		} finally {
			suppressSelectionEvents = false;
		}
	}

	private void clearSelectionIfDeleted(DepartmentListItemResponse deleted) {
		if (selectedDepartment != null &&
				selectedDepartment.department().id().equals(deleted.department().id())) {
			selectDepartmentEverywhere(null);
		}
	}

	private void cancelEditing() {
		setDetailsEditing(false);
		selectDepartment(selectedDepartment);
	}

	private void clearDepartmentUi() {
		selectedDepartment = null;
		departmentListComposite.clear();
		departmentDetailsComposite.clear();
	}
	
	private void showSuccess(String title, String message) {
		MessageDialog.openInformation(
				departmentDetailsComposite.getShell(),
				title,
				message);
	}

	private void setDetailsEditing(boolean editing) {
		this.detailsEditing = editing;
		updateUiEnabledState();
	}

	private void beginApi() {
		apiBusyCount++;
		updateUiEnabledState();
	}

	private void endApi() {
		apiBusyCount = Math.max(0, apiBusyCount - 1);
		updateUiEnabledState();
	}
	
	private boolean isApiBusy() {
		return apiBusyCount > 0;
	}

	private void updateUiEnabledState() {
		boolean baseEnabled = sessionEnabled && !isApiBusy();

		if (departmentListComposite == null || departmentDetailsComposite == null) {
			return;
		}

		if (!departmentListComposite.isDisposed()) {
			departmentListComposite.getShell()
					.setCursor(isApiBusy()
							? departmentListComposite.getDisplay().getSystemCursor(SWT.CURSOR_WAIT)
							: null);

			departmentListComposite.setEnabled(baseEnabled && !detailsEditing);
		}

		if (!departmentDetailsComposite.isDisposed()) {
			departmentDetailsComposite.setEnabled(baseEnabled);
		}
	}
	
	private boolean canAcceptDepartmentSelection() {
		return sessionEnabled && !detailsEditing;
	}

	private boolean canUseDepartmentListActions() {
		return sessionEnabled && !isApiBusy() && !detailsEditing;
	}
	
	private boolean canUseDepartmentDetailsActions() {
		return sessionEnabled && !isApiBusy();
	}

	/**
	 * Sets focus to the department list composite.
	 */
	@Focus
	public void setFocus() {
		departmentListComposite.setFocus();
	}

	/**
	 * Performs part cleanup during disposal.
	 */
	@PreDestroy
	public void dispose() {
	}
}
