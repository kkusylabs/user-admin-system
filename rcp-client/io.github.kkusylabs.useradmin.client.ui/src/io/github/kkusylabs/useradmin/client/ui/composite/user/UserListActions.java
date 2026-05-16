package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;

public interface UserListActions {
	void addUserRequested();

	void deleteUserRequested(UserListItemResponse user);

	void userSelected(UserListItemResponse user);
	
	void firstPageRequested();
	
	void previousPageRequested();
	
	void nextPageRequested();
	
	void lastPageRequested();
	
	void pageSizeChanged(int pageSize);
}