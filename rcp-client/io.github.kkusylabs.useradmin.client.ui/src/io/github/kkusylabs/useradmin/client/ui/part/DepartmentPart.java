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

	private boolean suppressSelectionEvents;

	@PostConstruct
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		departmentListComposite = new DepartmentListComposite(sash, SWT.BORDER);
		departmentDetailsComposite = new DepartmentDetailsComposite(sash, SWT.BORDER);

		sash.setWeights(new int[] { 55, 45 });

		wireEvents();
		
		if (tokenStore.hasToken()) {
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
				handleAddDepartmentRequested();
			}

			@Override
			public void deleteDepartmentRequested(DepartmentListItemResponse department) {
				handleDeleteDepartmentRequested(department);
			}

			@Override
			public void departmentSelected(DepartmentListItemResponse department) {
				selectDepartment(department);
			}
		});
	}

	private void wireDepartmentDetailsActions() {
		departmentDetailsComposite.setActions(new DepartmentDetailsActions() {

			@Override
			public void editDepartmentRequested(DepartmentListItemResponse department) {
				handleEditDepartmentRequested(department);
			}

			@Override
			public void createDepartmentRequested(CreateDepartmentRequest request) {
				handleCreateDepartmentRequested(request);
			}

			@Override
			public void updateDepartmentRequested(long departmentId, UpdateDepartmentRequest request) {
				handleUpdateDepartmentRequested(departmentId, request);
			}

			@Override
			public void cancelRequested() {
				restoreSelectedDepartmentView();
			}
		});
	}

	@Inject
	@Optional
	public void onLoginSuccess(@UIEventTopic(AppTopics.LOGIN_SUCCESS) String username) {
		selectedDepartment = null;

		loadDepartments();
	}

	private void loadDepartments() {
		apiRunner.task(departmentApiClient::getDepartments)
				.onControl(departmentListComposite)
				.onSuccess(this::showDepartments)
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

	private void handleAddDepartmentRequested() {
		if (departmentDetailsComposite.hasPendingChanges()) {
			boolean discard = confirmDiscardChanges();

			if (!discard) {
				return;
			}
		}

		selectedDepartment = null;
		departmentDetailsComposite.showCreateMode();
	}

	private void handleDeleteDepartmentRequested(DepartmentListItemResponse department) {
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
				.onError(
						"Delete Failed",
						"Could not delete department.")
				.execute();
	}

	private void selectDepartment(DepartmentListItemResponse department) {
		if (suppressSelectionEvents) {
			return;
		}

		if (departmentDetailsComposite.hasPendingChanges()) {
			boolean discard = confirmDiscardChanges();

			if (!discard) {
				reselectCurrentDepartment();
				return;
			}
		}

		selectedDepartment = department;
		departmentDetailsComposite.showViewMode(department);
	}

	private void handleEditDepartmentRequested(DepartmentListItemResponse department) {
		departmentDetailsComposite.showEditMode(department);
	}

	private void handleCreateDepartmentRequested(CreateDepartmentRequest request) {
		apiRunner.task(() -> departmentApiClient.createDepartment(request))
				.onControl(departmentDetailsComposite)
				.onSuccess(created -> {
					selectedDepartment = created;
					departmentDetailsComposite.showViewMode(created);
					loadDepartments();

					MessageDialog.openInformation(
							departmentDetailsComposite.getShell(),
							"Department Created",
							"Department created successfully.");
				})
				.onError(
						"Create Failed",
						"Could not create department.")
				.execute();
	}

	private void handleUpdateDepartmentRequested(
			long departmentId,
			UpdateDepartmentRequest request) {

		apiRunner.task(() ->
				departmentApiClient.updateDepartment(departmentId, request))
				.onControl(departmentDetailsComposite)
				.onSuccess(updated -> {
					selectedDepartment = updated;
					departmentDetailsComposite.showViewMode(updated);

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
				.onError(
						"Update Failed",
						"Could not update department.")
				.execute();
	}

	private void restoreSelectedDepartmentView() {
		if (selectedDepartment != null) {
			departmentDetailsComposite.showViewMode(selectedDepartment);
			return;
		}

		departmentDetailsComposite.clear();
	}

	private void reselectCurrentDepartment() {
		if (selectedDepartment == null) {
			return;
		}

		suppressSelectionEvents = true;

		try {
			departmentListComposite.selectDepartment(selectedDepartment.department().id());
		} finally {
			suppressSelectionEvents = false;
		}
	}

	private boolean confirmDiscardChanges() {
		return MessageDialog.openQuestion(
				departmentDetailsComposite.getShell(),
				"Discard Changes?",
				"Discard unsaved changes?");
	}

	@Focus
	public void setFocus() {
		departmentListComposite.setFocus();
	}

	@PreDestroy
	public void dispose() {
	}
}
