package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;

public interface UserListActions {
	void addUserRequested();

	void deleteUserRequested(UserListItemResponse user);

	void refreshUsersRequested();

	void userSelected(UserListItemResponse user);
}