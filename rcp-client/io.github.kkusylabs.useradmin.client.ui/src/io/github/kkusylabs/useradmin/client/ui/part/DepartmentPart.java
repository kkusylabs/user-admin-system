package io.github.kkusylabs.useradmin.client.ui.part;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
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
	
	@Inject
	private UISynchronize uiSync;

	private DepartmentListItemResponse selectedDepartment;

	private boolean suppressSelectionEvents;
	
	private boolean sessionEnabled;

	private boolean apiBusy;

	private boolean detailsEditing;

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
				if (!canUseDepartmentListActions()) {
					return;
				}
				
				beginCreateDepartment();
			}

			@Override
			public void deleteDepartmentRequested(DepartmentListItemResponse department) {
				if (!canUseDepartmentListActions()) {
					return;
				}
				
				deleteDepartment(department);
			}

			@Override
			public void departmentSelected(DepartmentListItemResponse department) {
				if (!canUseDepartmentListActions()) {
					return;
				}
				
				selectDepartment(department);
			}
		});
	}

	private void wireDepartmentDetailsActions() {
		departmentDetailsComposite.setActions(new DepartmentDetailsActions() {

			@Override
			public void editDepartmentRequested(DepartmentListItemResponse department) {
				beginEditDepartment(department);
			}

			@Override
			public void createDepartmentRequested(CreateDepartmentRequest request) {
				createDepartment(request);
			}

			@Override
			public void updateDepartmentRequested(long departmentId, UpdateDepartmentRequest request) {
				updateDepartment(departmentId, request);
			}

			@Override
			public void cancelRequested() {
				cancelEditing();
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
		uiSync.asyncExec(() -> {
			sessionEnabled = true;
			apiBusy = false;
			detailsEditing = false;
			selectedDepartment = null;
			updateUiEnabledState();
			loadDepartments();
		});
	}
	
	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object event) {
		uiSync.asyncExec(() -> {
			sessionEnabled = false;
			apiBusy = false;
			detailsEditing = false;
			clearDepartmentUi();
			updateUiEnabledState();
		});
	}

	private void loadDepartments() {
		apiRunner.task(departmentApiClient::getDepartments)
				.onControl(departmentListComposite)
				.onBefore(this::beginApi)
				.onSuccess(this::showDepartments)
				.onAfter(this::endApi)
				.onError(
						"Load Failed",
						"Could not fetch departments.")
				.execute();
	}

	private void showDepartments(DepartmentListResponse response) {
		departmentListComposite.setDepartments(response);
		reconcileSelectedDepartment(response);
	}

	private void reconcileSelectedDepartment(DepartmentListResponse response) {
		if (selectedDepartment == null) {
			departmentDetailsComposite.clear();
			return;
		}

		DepartmentListItemResponse refreshed =
				findDepartment(response, selectedDepartment.department().id());

		if (refreshed == null) {
			selectedDepartment = null;
			departmentDetailsComposite.clear();
			return;
		}

		selectedDepartment = refreshed;

		suppressSelectionEvents = true;
		try {
			departmentListComposite.selectDepartment(refreshed.department().id());
		} finally {
			suppressSelectionEvents = false;
		}

		departmentDetailsComposite.showViewMode(refreshed);
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

	private void deleteDepartment(DepartmentListItemResponse department) {
		boolean confirmed =
				MessageDialog.openConfirm(
						departmentListComposite.getShell(),
						"Delete Department",
						"Delete department '" +
								department.department().name() +
								"'?");

		if (!confirmed) {
			return;
		}

		apiRunner.task(() ->
				departmentApiClient.deleteDepartment(department.department().id()))
				.onControl(departmentListComposite)
				.onBefore(this::beginApi)
				.onSuccess(v -> {
					if (selectedDepartment != null &&
							selectedDepartment.department().id().equals(department.department().id())) {

						selectedDepartment = null;
						departmentDetailsComposite.clear();
					}

					loadDepartments();

					MessageDialog.openInformation(
							departmentListComposite.getShell(),
							"Department Deleted",
							"Department deleted successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Delete Failed",
						"Could not delete department.")
				.execute();
	}

	private void selectDepartment(
			DepartmentListItemResponse department) {

		if (suppressSelectionEvents) {
			return;
		}

		selectedDepartment = department;

		showSelectedDepartment();
	}

	private void beginEditDepartment(DepartmentListItemResponse department) {
		departmentDetailsComposite.showEditMode(department);
		setDetailsEditing(true);
	}

	private void createDepartment(CreateDepartmentRequest request) {
		apiRunner.task(() -> departmentApiClient.createDepartment(request))
				.onControl(departmentDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(created -> {
					selectedDepartment = created;
					departmentDetailsComposite.showViewMode(created);
					setDetailsEditing(false);
					loadDepartments();

					MessageDialog.openInformation(
							departmentDetailsComposite.getShell(),
							"Department Created",
							"Department created successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Create Failed",
						"Could not create department.")
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
					selectedDepartment = updated;
					departmentDetailsComposite.showViewMode(updated);
					setDetailsEditing(false);
					suppressSelectionEvents = true;
					try {
						departmentListComposite.replaceDepartment(updated);
						departmentListComposite.selectDepartment(updated.department().id());
					} finally {
						suppressSelectionEvents = false;
					}

					MessageDialog.openInformation(
							departmentDetailsComposite.getShell(),
							"Department Updated",
							"Department updated successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Update Failed",
						"Could not update department.")
				.execute();
	}

	private void cancelEditing() {
		setDetailsEditing(false);
		showSelectedDepartment();
	}
	
	private void showSelectedDepartment() {
		if (selectedDepartment != null) {
			departmentDetailsComposite.showViewMode(
					selectedDepartment);
			return;
		}

		departmentDetailsComposite.clear();
	}
	
	private void clearDepartmentUi() {
		selectedDepartment = null;
		departmentListComposite.clear();
		departmentDetailsComposite.clear();
	}

	private void setDetailsEditing(boolean editing) {
		this.detailsEditing = editing;
		updateUiEnabledState();
	}

	private void beginApi() {
		apiBusy = true;
		updateUiEnabledState();
	}

	private void endApi() {
		apiBusy = false;
		updateUiEnabledState();
	}

	private void updateUiEnabledState() {
		boolean baseEnabled =
				sessionEnabled && !apiBusy;

		departmentListComposite.getShell()
				.setCursor(apiBusy ? departmentListComposite.getDisplay().getSystemCursor(SWT.CURSOR_WAIT) : null);
		
		departmentDetailsComposite.setEnabled(baseEnabled);

		departmentListComposite.setEnabled(
				baseEnabled && !detailsEditing);
	}

	private boolean canUseDepartmentListActions() {
		return sessionEnabled &&
				!apiBusy &&
				!detailsEditing;
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
