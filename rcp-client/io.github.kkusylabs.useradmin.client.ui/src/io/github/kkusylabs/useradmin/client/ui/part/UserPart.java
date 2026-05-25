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
import io.github.kkusylabs.useradmin.client.core.auth.SessionTokenStore;
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

/**
 * Eclipse part responsible for user administration workflows.
 *
 * <p>
 * This part coordinates interactions between:
 * </p>
 *
 * <ul>
 *   <li>{@link UserFilterComposite}</li>
 *   <li>{@link UserListComposite}</li>
 *   <li>{@link UserDetailsComposite}</li>
 *   <li>{@link UserApiClient}</li>
 *   <li>{@link DepartmentApiClient}</li>
 * </ul>
 *
 * <p>
 * Supported workflows include:
 * </p>
 *
 * <ul>
 *   <li>user filtering and search</li>
 *   <li>paged user navigation</li>
 *   <li>user selection synchronization</li>
 *   <li>user creation</li>
 *   <li>user editing</li>
 *   <li>user deletion</li>
 *   <li>department option loading</li>
 *   <li>unsaved change handling</li>
 * </ul>
 *
 * <p>
 * REST operations are executed asynchronously through
 * {@link UiApiRunner}.
 * </p>
 */
public class UserPart {

	private UserListComposite userListComposite;
	private UserFilterComposite userFilterComposite;
	private UserDetailsComposite userDetailsComposite;

	@Inject
	private UiApiRunner apiRunner;

	@Inject
	private UserApiClient userApiClient;

	@Inject
	private DepartmentApiClient departmentApiClient;

	@Inject
	private SessionTokenStore tokenStore;

	private int currentPage = 0;
	private int pageSize = 25;

	private UserListResponse currentResponse;
	private UserListItemResponse selectedUser;

	private boolean suppressSelectionEvents;
	private boolean sessionEnabled;
	private boolean detailsEditing;

	private int apiBusyCount;
	
	public static final String ID = "io.github.kkusylabs.useradmin.client.ui.part.users";

	/**
	 * Creates the user administration UI.
	 *
	 * @param parent parent composite
	 */
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

		sessionEnabled = tokenStore.hasToken();
		updateUiEnabledState();

		if (sessionEnabled) {
			loadInitialData();
		}
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
				if (canUseListAndFilterActions()) {
					currentPage = 0;
					loadUsers();
				}
			}

			@Override
			public void clearFilterRequested() {
				if (canUseListAndFilterActions()) {
					userFilterComposite.clear();
					currentPage = 0;
					loadUsers();
				}
			}
		});
	}

	private void wireUserListActions() {
		userListComposite.setActions(new UserListActions() {

			@Override
			public void addUserRequested() {
				if (canUseListAndFilterActions()) {
					beginCreateUser();
				}
			}

			@Override
			public void deleteUserRequested(UserListItemResponse user) {
				if (canUseListAndFilterActions()) {
					deleteUser(user);
				}
			}

			@Override
			public void userSelected(UserListItemResponse user) {
				if (suppressSelectionEvents) {
					return;
				}

				if (canAcceptUserSelection()) {
					selectUser(user);
				}
			}

			@Override
			public void firstPageRequested() {
				if (canUseListAndFilterActions()) {
					goToFirstPage();
				}
			}

			@Override
			public void previousPageRequested() {
				if (canUseListAndFilterActions()) {
					goToPreviousPage();
				}
			}

			@Override
			public void nextPageRequested() {
				if (canUseListAndFilterActions()) {
					goToNextPage();
				}
			}

			@Override
			public void lastPageRequested() {
				if (canUseListAndFilterActions()) {
					goToLastPage();
				}
			}

			@Override
			public void pageSizeChanged(int pageSize) {
				if (canUseListAndFilterActions()) {
					changePageSize(pageSize);
				}
			}
		});
	}

	private void wireUserDetailsActions() {
		userDetailsComposite.setActions(new UserDetailsActions() {

			@Override
			public void editUserRequested(UserListItemResponse user) {
				if (canUseUserDetailsActions()) {
					beginEditUser(user);
				}
			}

			@Override
			public void createUserRequested(CreateUserRequest request) {
				if (canUseUserDetailsActions()) {
					createUser(request);
				}
			}

			@Override
			public void updateUserRequested(long userId, UserPatch patch) {
				if (canUseUserDetailsActions()) {
					updateUser(userId, patch);
				}
			}

			@Override
			public void cancelRequested() {
				if (canUseUserDetailsActions()) {
					cancelEditing();
				}
			}
		});
	}

	/**
	 * Handles successful authentication events by loading initial user and
	 * department data.
	 *
	 * @param username authenticated username
	 */
	@Inject
	@Optional
	public void onLoginSuccess(@UIEventTopic(AppTopics.LOGIN_SUCCESS) String username) {
		sessionEnabled = true;
		apiBusyCount = 0;
		detailsEditing = false;
		suppressSelectionEvents = false;

		currentPage = 0;
		currentResponse = null;
		selectedUser = null;

		updateUiEnabledState();
		loadInitialData();
	}
	
	/**
	 * Clears authenticated UI state after authentication expires.
	 *
	 * @param ignored unused event payload
	 */
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object ignored) {
		clearSessionState();
	}

	/**
	 * Clears authenticated UI state after an explicit logout.
	 *
	 * @param ignored unused event payload
	 */
	@Inject
	@Optional
	public void onLogout(@UIEventTopic(AppTopics.LOGOUT) Object ignored) {
		clearSessionState();
	}

	/**
	 * Clears authenticated UI state after the current session ends.
	 *
	 * <p>
	 * Resets workflow state, clears loaded data, and disables authenticated
	 * actions until the user logs in again.
	 * </p>
	 */
	private void clearSessionState() {
		sessionEnabled = false;
		apiBusyCount = 0;
		detailsEditing = false;
		suppressSelectionEvents = false;

		clearUserUi();
		updateUiEnabledState();
	}

	private void loadInitialData() {
		clearUserUi();
		loadDepartments();
		loadUsers();
	}

	private void loadDepartments() {
		apiRunner.task(departmentApiClient::getDepartments)
				.onControl(userFilterComposite)
				.onBefore(this::beginApi)
				.onSuccess(this::initializeDepartments)
				.onAfter(this::endApi)
				.onError("Load Failed", "Could not load department options.")
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

	private String formatDepartmentName(DepartmentDetailsResponse department) {
		return department.active()
				? department.name()
				: department.name() + " (inactive)";
	}

	private void loadUsers() {
		UserListFilter filter = userFilterComposite.getFilter();

		apiRunner.task(() -> userApiClient.getUsers(currentPage, pageSize, filter))
				.onControl(userListComposite)
				.onBefore(this::beginApi)
				.onSuccess(this::showUsers)
				.onAfter(this::endApi)
				.onError("Load Failed", "Could not fetch user details.")
				.execute();
	}

	private void showUsers(UserListResponse response) {
		currentResponse = response;
		userListComposite.setUsers(response);
		reconcileSelectedUser(response);
	}

	private void reconcileSelectedUser(UserListResponse response) {
		Long selectedId = selectedUserId();

		if (selectedId == null) {
			selectUserEverywhere(null);
			return;
		}

		UserListItemResponse refreshed = findUser(response, selectedId);
		selectUserEverywhere(refreshed);
	}

	private Long selectedUserId() {
		return selectedUser == null
				? null
				: selectedUser.user().id();
	}

	private UserListItemResponse findUser(UserListResponse response, Long userId) {
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

	private void beginCreateUser() {
		selectedUser = null;

		apiRunner.task(userApiClient::getCreateUserCapabilities)
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(capabilities -> {
					userDetailsComposite.showCreateMode(capabilities);
					setDetailsEditing(true);
				})
				.onAfter(this::endApi)
				.onError("Create Failed", "Could not prepare create-user form.")
				.execute();
	}

	private void beginEditUser(UserListItemResponse item) {
		selectedUser = item;

		apiRunner.task(() -> userApiClient.getUserEditData(item.user().id()))
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(response -> {
					userDetailsComposite.showEditMode(response);
					setDetailsEditing(true);
				})
				.onAfter(this::endApi)
				.onError("Load Failed", "Could not load user edit details.")
				.execute();
	}

	private void createUser(CreateUserRequest request) {
		apiRunner.task(() -> userApiClient.createUser(request))
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(created -> {
					setDetailsEditing(false);
					selectUser(created);
					loadUsers();
					
					showSuccess("User Created", "User created successfully.");
				})
				.onAfter(this::endApi)
				.onError("Create Failed", "Could not create user.")
				.execute();
	}

	private void updateUser(long userId, UserPatch patch) {
		apiRunner.task(() -> userApiClient.updateUser(userId, patch.asMap()))
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(updated -> {
					setDetailsEditing(false);
					selectUser(updated);
					replaceAndSelectUser(updated);
					
					showSuccess("User Updated", "User updated successfully.");
				})
				.onAfter(this::endApi)
				.onError("Update Failed", "Could not update user.")
				.execute();
	}

	private void deleteUser(UserListItemResponse user) {
		if (!confirmDeleteUser(user)) {
			return;
		}

		apiRunner.task(() -> userApiClient.deleteUser(user.user().id()))
				.onControl(userListComposite)
				.onBefore(this::beginApi)
				.onSuccess(v -> {
					clearSelectionIfDeleted(user);
					loadUsers();

					showSuccess("User Deleted", "User deleted successfully.");
				})
				.onAfter(this::endApi)
				.onError("Delete Failed", "Could not delete user.")
				.execute();
	}

	private boolean confirmDeleteUser(UserListItemResponse user) {
		return MessageDialog.openConfirm(
				userListComposite.getShell(),
				"Delete User",
				"Delete user '" + user.user().username() + "'?");
	}

	private void selectUser(UserListItemResponse user) {
		selectedUser = user;

		if (user == null) {
			userDetailsComposite.clear();
			return;
		}

		userDetailsComposite.showViewMode(user);
	}

	private void selectUserEverywhere(UserListItemResponse user) {
		selectUser(user);
		selectUserInList(user == null ? null : user.user().id());
	}

	private void selectUserInList(Long userId) {
		suppressSelectionEvents = true;
		try {
			userListComposite.selectUser(userId);
		} finally {
			suppressSelectionEvents = false;
		}
	}
	
	private void replaceAndSelectUser(UserListItemResponse updated) {
		suppressSelectionEvents = true;
		try {
			userListComposite.replaceUser(updated);
			userListComposite.selectUser(updated.user().id());
		} finally {
			suppressSelectionEvents = false;
		}
	}

	private void clearSelectionIfDeleted(UserListItemResponse deleted) {
		if (selectedUser != null &&
				selectedUser.user().id().equals(deleted.user().id())) {
			selectUserEverywhere(null);
		}
	}

	private void cancelEditing() {
		setDetailsEditing(false);
		selectUser(selectedUser);
	}

	private void goToFirstPage() {
		if (currentPage > 0) {
			currentPage = 0;
			loadUsers();
		}
	}

	private void goToPreviousPage() {
		if (currentPage > 0) {
			currentPage--;
			loadUsers();
		}
	}

	private void goToNextPage() {
		if (currentResponse != null && !currentResponse.users().last()) {
			currentPage++;
			loadUsers();
		}
	}

	private void goToLastPage() {
		if (currentResponse == null) {
			return;
		}

		currentPage = Math.max(currentResponse.users().totalPages() - 1, 0);
		loadUsers();
	}

	private void changePageSize(int pageSize) {
		this.pageSize = pageSize;
		this.currentPage = 0;
		loadUsers();
	}

	private void clearUserUi() {
		currentPage = 0;
		currentResponse = null;
		selectedUser = null;

		userFilterComposite.clear();
		userListComposite.clear();
		userDetailsComposite.clear();
	}

	private void showSuccess(String title, String message) {
		MessageDialog.openInformation(
				userDetailsComposite.getShell(),
				title,
				message);
	}

	private void setDetailsEditing(boolean editing) {
		detailsEditing = editing;
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

		if (userListComposite == null ||
				userFilterComposite == null ||
				userDetailsComposite == null) {
			return;
		}

		if (!userListComposite.isDisposed()) {
			userListComposite.getShell()
					.setCursor(isApiBusy()
							? userListComposite.getDisplay().getSystemCursor(SWT.CURSOR_WAIT)
							: null);

			userListComposite.setEnabled(baseEnabled && !detailsEditing);
		}

		if (!userFilterComposite.isDisposed()) {
			userFilterComposite.setEnabled(baseEnabled && !detailsEditing);
		}

		if (!userDetailsComposite.isDisposed()) {
			userDetailsComposite.setEnabled(baseEnabled);
		}
	}
	
	private boolean canAcceptUserSelection() {
		return sessionEnabled && !detailsEditing;
	}

	private boolean canUseListAndFilterActions() {
		return sessionEnabled && !isApiBusy() && !detailsEditing;
	}

	private boolean canUseUserDetailsActions() {
		return sessionEnabled && !isApiBusy();
	}
	
	public boolean hasPendingChanges() {
		return detailsEditing;
	}

	/**
	 * Sets focus to the user list composite.
	 */
	@Focus
	public void setFocus() {
		userListComposite.setFocus();
	}

	/**
	 * Performs part cleanup during disposal.
	 */
	@PreDestroy
	public void dispose() {
	}
}