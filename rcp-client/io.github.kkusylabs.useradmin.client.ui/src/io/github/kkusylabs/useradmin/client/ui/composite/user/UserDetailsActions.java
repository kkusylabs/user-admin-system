package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserPatch;

/**
 * Callback interface for actions initiated from the user details UI.
 */
public interface UserDetailsActions {
	/**
	 * Requests that the specified user be loaded into edit mode.
	 *
	 * @param user selected user item
	 */
	void editUserRequested(UserListItemResponse user);

	/**
	 * Requests creation of a new user.
	 *
	 * @param request create request containing user details
	 */
	void createUserRequested(CreateUserRequest request);
	
	/**
	 * Requests update of an existing user.
	 *
	 * @param userId identifier of the user to update
	 * @param patch patch containing modified user fields
	 */
	void updateUserRequested(long userId, UserPatch patch);

	/**
	 * Requests cancellation of the current create or edit workflow.
	 */
	void cancelRequested();
}
