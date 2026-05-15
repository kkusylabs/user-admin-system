package io.github.kkusylabs.useradmin.client.ui.composite.user;

import io.github.kkusylabs.useradmin.client.core.api.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserPatch;

public interface UserDetailsActions {
	void editUserRequested(UserListItemResponse user);

	void createUserRequested(CreateUserRequest request);

	void updateUserRequested(long userId, UserPatch patch);

	void cancelRequested();
}
