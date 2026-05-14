package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.UserListFilter;

public interface UserFilterActions {
	void searchRequested(UserListFilter filter);

	void clearFilterRequested();
}
