package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;

public interface UserDetailsActions {
	void editUserRequested(UserListItemResponse user);

	void createUserRequested(CreateUserRequest request);

//	void patchUserRequested(UserPatch patch);

	void cancelRequested();
}
