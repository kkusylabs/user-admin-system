package io.github.kkusylabs.useradmin.client.ui.part;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentApiClient;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentDetailsResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;
import io.github.kkusylabs.useradmin.client.core.api.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.client.core.api.user.UserApiClient;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListFilter;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserPatch;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserDetailsActions;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserDetailsComposite;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserFilterActions;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserFilterComposite;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserListActions;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserListComposite;
import io.github.kkusylabs.useradmin.client.ui.events.AppTopics;
import io.github.kkusylabs.useradmin.client.ui.runtime.UiApiRunner;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

public class UserPart {

	private UserListComposite userListComposite;
	
	private UserFilterComposite userFilterComposite;
	
	private UserDetailsComposite userDetailsComposite;
	
	@Inject
	private UiApiRunner apiRunner;
	
	@Inject
	private UserApiClient userApiClient;
	
	@Inject DepartmentApiClient departmentApiClient;
	
	private int currentPage = 0;
	
	private int pageSize = 25;
	
	private UserListResponse currentResponse;
	
	private UserListItemResponse selectedUser;
	
	private boolean suppressSelectionEvents;

	@PostConstruct
	public void createControls(Composite parent) {	
		parent.setLayout(new GridLayout(1, false));

		userFilterComposite = new UserFilterComposite(parent, SWT.NONE);
		userFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		userListComposite = new UserListComposite(sash, SWT.BORDER);
		userDetailsComposite = new UserDetailsComposite(sash, SWT.BORDER);

		sash.setWeights(new int[] { 55, 45 });

		wireEvents();
	}
	
	private void wireEvents() {
		wireUserFilterActions();
		wireUserListActions();
		wireUserDetailsActions();
	}
	
	private void wireUserFilterActions() {
		userFilterComposite.setActions(new UserFilterActions() {

			@Override
			public void searchRequested(UserListFilter filter) {
				currentPage = 0;
				loadUsers();
			}

			@Override
			public void clearFilterRequested() {
				currentPage = 0;
				loadUsers();
			}
		});		
	}
	
	private void wireUserListActions() {
		userListComposite.setActions(new UserListActions() {
			@Override
			public void addUserRequested() {
				handleAddUserRequested();
			}

			@Override
			public void deleteUserRequested(UserListItemResponse user) {
				handleDeleteUserRequested(user);
			}

			@Override
			public void userSelected(UserListItemResponse user) {
				handleUserSelection(user);
			}
			

			@Override
			public void firstPageRequested() {
				handleFirstPageRequested();
			}
			
			@Override
			public void previousPageRequested() {
				handlePreviousPageRequested();
			}

			@Override
			public void nextPageRequested() {
				handleNextPageRequested();
			}

			@Override
			public void lastPageRequested() {
				handleLastPageRequested();
			}
			
			public void pageSizeChanged(int pageSize) {
				handlePageSizeChanged(pageSize);
			}
		});		
	}
	
	private void wireUserDetailsActions() {
		userDetailsComposite.setActions(new UserDetailsActions() {
			@Override
			public void editUserRequested(UserListItemResponse user) {
				handleEditUserRequested(user);
			}

			@Override
			public void createUserRequested(CreateUserRequest request) {
				handleCreateUserRequested(request);
			}
			
			@Override 
			public void updateUserRequested(long userId, UserPatch patch) {
				handleUpdateUserRequested(userId, patch);
			}

			@Override
			public void cancelRequested() {
				handleCancelUserRequested();
			}
		});		
	}
	
	@Inject
	@Optional
	public void onLoginSuccess(@UIEventTopic(AppTopics.LOGIN_SUCCESS) String username) {
		currentPage = 0;
		currentResponse = null;
		
		loadInitialData();
	}
	
	
	private void loadInitialData() {
		loadDepartments();
		loadUsers();
	}
	
	private void loadDepartments() {
		apiRunner.task(() -> departmentApiClient.getDepartments())
				.onControl(userFilterComposite)
				.onSuccess(this::initializeDepartments)
				.onError(
						"Load Failed",
						"Could not load department options.")
				.execute();
	}
	
	private void initializeDepartments(DepartmentListResponse response) {
		List<DepartmentOption> options =
				response == null || response.departments() == null
						? List.of()
						: response.departments()
								.stream()
								.map(DepartmentListItemResponse::department)
								.filter(department -> department != null)
								.map(department ->
										new DepartmentOption(
												department.id(),
												formatDepartmentName(department)))
								.toList();

		userFilterComposite.setDepartmentOptions(options);
	}
	
	private String formatDepartmentName(
			DepartmentDetailsResponse department) {

		if (department.active()) {
			return department.name();
		}

		return department.name() + " (inactive)";
	}
	
	private void loadUsers() {
		UserListFilter filter = userFilterComposite.getFilter();

		apiRunner.task(() -> userApiClient.getUsers(currentPage, pageSize, filter))
				.onControl(userListComposite)
				.onSuccess(this::showUsers)
				.onError("Load Failed", "Could not fetch user details.")
				.execute();
	}
	
	private void showUsers(UserListResponse response) {
		this.currentResponse = response;

		userListComposite.setUsers(response);
		
		reconcileSelectedUser(response);
	}
	
	private void reconcileSelectedUser(UserListResponse response) {
		if (selectedUser == null) {
			userDetailsComposite.clear();
			return;
		}

		UserListItemResponse refreshed =
				findUser(response, selectedUser.user().id());

		if (refreshed == null) {
			selectedUser = null;
			userDetailsComposite.clear();
			return;
		}

		selectedUser = refreshed;
		userListComposite.selectUser(refreshed.user().id());
		userDetailsComposite.showViewMode(refreshed);
	}
	
	private UserListItemResponse findUser(
			UserListResponse response,
			Long userId) {

		if (response == null || response.users() == null || userId == null) {
			return null;
		}

		return response.users()
				.content()
				.stream()
				.filter(item -> userId.equals(item.user().id()))
				.findFirst()
				.orElse(null);
	}
	
	private void handleAddUserRequested() {
		apiRunner.task(userApiClient::getCreateUserCapabilities)
			.onControl(userDetailsComposite)
			.onSuccess(userDetailsComposite::showCreateMode)
			.onError("Create Failed", "Could not prepare create-user form.")
			.execute();
	}
	
	private void handleDeleteUserRequested(UserListItemResponse user) {

		boolean confirmed =
				MessageDialog.openConfirm(
						userListComposite.getShell(),
						"Delete User",
						"Delete user '" +
								user.user().username() +
								"'?");

		if (!confirmed) {
			return;
		}

		apiRunner.task(() ->
				userApiClient.deleteUser(user.user().id()))
				.onControl(userListComposite)
				.onSuccess(v -> {
					userDetailsComposite.clear();
					loadUsers();

					MessageDialog.openInformation(
							userListComposite.getShell(),
							"User Deleted",
							"User deleted successfully.");
				})
				.onError(
						"Delete Failed",
						"Could not delete user.")
				.execute();
	}
	
	private void handleUserSelection(UserListItemResponse user) {
		if (suppressSelectionEvents) {
			return;
		}

		if (userDetailsComposite.hasPendingChanges()) {
			boolean discard = MessageDialog.openQuestion(
					userDetailsComposite.getShell(),
					"Discard Changes?",
					"Discard unsaved changes?");

			if (!discard) {
				reselectCurrentUser();
				return;
			}
		}

		selectedUser = user;
		userDetailsComposite.showViewMode(user);
	}
	
	private void reselectCurrentUser() {
		if (selectedUser == null) {
			return;
		}

		suppressSelectionEvents = true;

		try {
			userListComposite.selectUser(selectedUser.user().id());
		} finally {
			suppressSelectionEvents = false;
		}
	}
	
	private void handleFirstPageRequested() {
		if (currentPage > 0) {
			currentPage = 0;
			loadUsers();
		}		
	}
	
	private void handlePreviousPageRequested() {
		if (currentPage > 0) {
			currentPage--;
			loadUsers();
		}
	}
	
	private void handleNextPageRequested() {
		if (currentResponse == null) {
			return;
		}

		if (!currentResponse.users().last()) {
			currentPage++;
			loadUsers();
		}
	}
	
	private void handleLastPageRequested() {
		if (currentResponse == null) {
			return;
		}

		currentPage = Math.max(currentResponse.users().totalPages() - 1, 0);

		loadUsers();
	}
	
	private void handlePageSizeChanged(int pageSize) {
		this.pageSize = pageSize;
		this.currentPage = 0;
		loadUsers();
	}
	
	private void handleEditUserRequested(UserListItemResponse item) {

		apiRunner.task(() ->
				userApiClient.getUserEditData(item.user().id()))
				.onControl(userDetailsComposite)
				.onSuccess(response -> {
					userDetailsComposite.showEditMode(response);
				})
				.onError(
						"Load Failed",
						"Could not load user edit details.")
				.execute();
	}
	
	private void handleCreateUserRequested(CreateUserRequest request) {

		apiRunner.task(() -> userApiClient.createUser(request))
				.onControl(userDetailsComposite)
				.onSuccess(created -> {
					loadUsers();
					MessageDialog.openInformation(
							userDetailsComposite.getShell(),
							"User Created",
							"User created successfully.");
				})
				.onError(
						"Create Failed",
						"Could not create user.")
				.execute();
	}
	
	private void handleUpdateUserRequested(long userId, UserPatch patch) {

		apiRunner.task(() ->
				userApiClient.updateUser(userId, patch.asMap()))
				.onControl(userDetailsComposite)
				.onSuccess(updated -> {
					selectedUser = updated;
					userDetailsComposite.showViewMode(updated);

					suppressSelectionEvents = true;
					try {
						userListComposite.replaceUser(updated);
						userListComposite.selectUser(updated.user().id());
					} finally {
						suppressSelectionEvents = false;
					}

					MessageDialog.openInformation(
							userDetailsComposite.getShell(),
							"User Updated",
							"User updated successfully.");
				})
				.onError(
						"Update Failed",
						"Could not update user.")
				.execute();
	}
	
	private void handleCancelUserRequested() {

		if (selectedUser != null) {
			userDetailsComposite.showViewMode(selectedUser);
			return;
		}

		userDetailsComposite.clear();
	}
	
	@Focus
	public void setFocus() {
		userListComposite.setFocus();
	}
	
	@PreDestroy
	public void dispose() {
	}
}
