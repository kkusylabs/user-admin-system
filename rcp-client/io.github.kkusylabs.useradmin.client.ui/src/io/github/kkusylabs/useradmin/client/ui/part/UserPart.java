package io.github.kkusylabs.useradmin.client.ui.part;

import java.util.List;

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
	
	@Inject DepartmentApiClient departmentApiClient;
	
	@Inject
	private UISynchronize uiSync;
	
	private int currentPage = 0;
	
	private int pageSize = 25;
	
	private UserListResponse currentResponse;
	
	private UserListItemResponse selectedUser;
	
	private boolean suppressSelectionEvents;
	
	@Inject
	private SessionTokenStore tokenStore;
	
	private boolean sessionEnabled;
	
	private boolean apiBusy;
	
	private boolean detailsEditing;
	
	private int pendingInitialLoads;


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
		updateUiEnabledState();;
		
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
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				currentPage = 0;
				reloadUsers();
			}

			@Override
			public void clearFilterRequested() {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				userFilterComposite.clear();
				currentPage = 0;
				reloadUsers();
			}
		});		
	}
	
	private void wireUserListActions() {
		userListComposite.setActions(new UserListActions() {
			@Override
			public void addUserRequested() {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				beginCreateUser();
			}

			@Override
			public void deleteUserRequested(UserListItemResponse user) {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				deleteUser(user);
			}

			@Override
			public void userSelected(UserListItemResponse user) {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				selectUser(user);
			}
			

			@Override
			public void firstPageRequested() {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				goToFirstPage();
			}
			
			@Override
			public void previousPageRequested() {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				goToPreviousPage();
			}

			@Override
			public void nextPageRequested() {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				goToNextPage();
			}

			@Override
			public void lastPageRequested() {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				goToLastPage();
			}
			
			public void pageSizeChanged(int pageSize) {
				if (!canUseListAndFilterActions()) {
					return;
				}
				
				changePageSize(pageSize);
			}
		});		
	}
	
	private void wireUserDetailsActions() {
		userDetailsComposite.setActions(new UserDetailsActions() {
			@Override
			public void editUserRequested(UserListItemResponse user) {
				beginEditUser(user);
			}

			@Override
			public void createUserRequested(CreateUserRequest request) {
				createUser(request);
			}
			
			@Override 
			public void updateUserRequested(long userId, UserPatch patch) {
				updateUser(userId, patch);
			}

			@Override
			public void cancelRequested() {
				cancelEditing();
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
		uiSync.asyncExec(() -> {
			sessionEnabled = true;
			detailsEditing = false;

			currentPage = 0;
			currentResponse = null;
			selectedUser = null;

			updateUiEnabledState();
			loadInitialData();
		});
	}
	
	@Inject
	@Optional
	public void onAuthExpired(@UIEventTopic(AppTopics.AUTH_EXPIRED) Object event) {
		uiSync.asyncExec(() -> {
			sessionEnabled = false;
			apiBusy = false;
			detailsEditing = false;
			clearUserUi();
			updateUiEnabledState();
		});
	}
	
	private void loadInitialData() {
		clearUserUi();
		apiBusy = true;
		updateUiEnabledState();
		pendingInitialLoads = 2;
		
		loadDepartments();
		loadInitialUsers();
	}
	
	private void initialLoadCompleted() {
		pendingInitialLoads--;

		if (pendingInitialLoads <= 0) {
			apiBusy = false;
			updateUiEnabledState();
		}
	}
	
	private void loadDepartments() {
		apiRunner.task(() -> departmentApiClient.getDepartments())
				.onControl(userFilterComposite)
				.onSuccess(this::initializeDepartments)
				.onAfter(this::initialLoadCompleted)
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
	
	private String formatDepartmentName(
			DepartmentDetailsResponse department) {

		if (department.active()) {
			return department.name();
		}

		return department.name() + " (inactive)";
	}
	
	private void loadInitialUsers() {
		loadUsers(null, this::initialLoadCompleted);
	}
	
	private void reloadUsers() {
		loadUsers(this::beginApi, this::endApi);
	}
	
	private void loadUsers(Runnable before, Runnable after) {

		UserListFilter filter = userFilterComposite.getFilter();

		var request = apiRunner.task(() -> userApiClient.getUsers(currentPage, pageSize, filter))
				.onControl(userListComposite)
				.onSuccess(this::showUsers)
				.onError("Load Failed", "Could not fetch user details.");

		if (before != null) {
			request.onBefore(before);
		}
		
		if (after != null) {
			request.onAfter(after);	
		}

		request.execute();
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
	
	private void beginCreateUser() {
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
	
	private void deleteUser(UserListItemResponse user) {

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
				.onBefore(this::beginApi)
				.onSuccess(v -> {
					userDetailsComposite.clear();
					reloadUsers();

					MessageDialog.openInformation(
							userListComposite.getShell(),
							"User Deleted",
							"User deleted successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Delete Failed",
						"Could not delete user.")
				.execute();
	}
	
	private void selectUser(UserListItemResponse user) {
		if (suppressSelectionEvents) {
			return;
		}

		selectedUser = user;
		showSelectedUser();
	}
	
	private void goToFirstPage() {
		if (currentPage > 0) {
			currentPage = 0;
			reloadUsers();
		}		
	}
	
	private void goToPreviousPage() {
		if (currentPage > 0) {
			currentPage--;
			reloadUsers();
		}
	}
	
	private void goToNextPage() {
		if (currentResponse == null) {
			return;
		}

		if (!currentResponse.users().last()) {
			currentPage++;
			reloadUsers();
		}
	}
	
	private void goToLastPage() {
		if (currentResponse == null) {
			return;
		}

		currentPage = Math.max(currentResponse.users().totalPages() - 1, 0);

		reloadUsers();
	}
	
	private void changePageSize(int pageSize) {
		this.pageSize = pageSize;
		this.currentPage = 0;
		reloadUsers();
	}
	
	private void beginEditUser(UserListItemResponse item) {

		apiRunner.task(() ->
				userApiClient.getUserEditData(item.user().id()))
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(response -> {
					userDetailsComposite.showEditMode(response);
					setDetailsEditing(true);
				})
				.onAfter(this::endApi)
				.onError(
						"Load Failed",
						"Could not load user edit details.")
				.execute();
	}
	
	private void createUser(CreateUserRequest request) {

		apiRunner.task(() -> userApiClient.createUser(request))
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(created -> {
					selectedUser = created;
					userDetailsComposite.showViewMode(created);
					setDetailsEditing(false);
					reloadUsers();
					MessageDialog.openInformation(
							userDetailsComposite.getShell(),
							"User Created",
							"User created successfully.");
				})
				.onAfter(this::endApi)
				.onError(
						"Create Failed",
						"Could not create user.")
				.execute();
	}
	
	private void updateUser(long userId, UserPatch patch) {

		apiRunner.task(() ->
				userApiClient.updateUser(userId, patch.asMap()))
				.onControl(userDetailsComposite)
				.onBefore(this::beginApi)
				.onSuccess(updated -> {
					selectedUser = updated;
					userDetailsComposite.showViewMode(updated);
					setDetailsEditing(false);
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
				.onAfter(this::endApi)
				.onError(
						"Update Failed",
						"Could not update user.")
				.execute();
	}
		
	private void cancelEditing() {
		setDetailsEditing(false);
		showSelectedUser();		
	}
	
	private void showSelectedUser() {
		if (selectedUser != null) {
			userDetailsComposite.showViewMode(
					selectedUser);
			return;
		}

		userDetailsComposite.clear();
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

		userDetailsComposite.setEnabled(baseEnabled);

		userFilterComposite.setEnabled(
				baseEnabled && !detailsEditing);

		userListComposite.setEnabled(
				baseEnabled && !detailsEditing);
	}
	
	private boolean canUseListAndFilterActions() {
		return sessionEnabled && !apiBusy && !detailsEditing;
	}

	private void clearUserUi() {
		currentPage = 0;
		currentResponse = null;
		selectedUser = null;
		userFilterComposite.clear();
		userListComposite.clear();
		userDetailsComposite.clear();
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
