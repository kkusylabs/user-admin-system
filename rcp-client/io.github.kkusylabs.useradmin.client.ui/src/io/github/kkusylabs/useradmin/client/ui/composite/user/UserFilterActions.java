package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.UserListFilter;

/**
 * Callback interface for actions initiated from the user filter UI.
 */
public interface UserFilterActions {
	/**
	 * Requests execution of a user search using the specified filter criteria.
	 *
	 * @param filter user filter values
	 */
	void searchRequested(UserListFilter filter);

	/**
	 * Requests clearing of the current filter values.
	 */
	void clearFilterRequested();
}
