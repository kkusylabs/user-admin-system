package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;

/**
 * Callback interface for actions initiated from the user list UI.
 */
public interface UserListActions {
	/**
	 * Requests creation of a new user.
	 */
	void addUserRequested();

	/**
	 * Requests deletion of the specified user.
	 *
	 * @param user selected user item
	 */
	void deleteUserRequested(UserListItemResponse user);

	/**
	 * Notifies that a user was selected from the list.
	 *
	 * @param user selected user item
	 */
	void userSelected(UserListItemResponse user);
	
	/**
	 * Requests navigation to the first page of results.
	 */
	void firstPageRequested();
	
	/**
	 * Requests navigation to the previous page of results.
	 */
	void previousPageRequested();
	
	/**
	 * Requests navigation to the next page of results.
	 */
	void nextPageRequested();
	
	/**
	 * Requests navigation to the last page of results.
	 */
	void lastPageRequested();
	
	/**
	 * Notifies that the selected page size changed.
	 *
	 * @param pageSize selected page size
	 */
	void pageSizeChanged(int pageSize);
}