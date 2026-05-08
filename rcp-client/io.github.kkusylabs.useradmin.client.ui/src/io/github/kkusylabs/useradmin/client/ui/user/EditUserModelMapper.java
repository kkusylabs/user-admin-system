package io.github.kkusylabs.useradmin.client.ui.user;

import io.github.kkusylabs.useradmin.client.core.api.user.EditUserResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserDetailResponse;

public final class EditUserModelMapper {

	private EditUserModelMapper() {
	}

	public static EditUserModel fromResponse(EditUserResponse response) {
		return fromUserDetail(response.user());
	}

	public static EditUserModel fromUserDetail(UserDetailResponse user) {
		EditUserModel model = new EditUserModel();
		model.setId(user.id());
		model.setUsername(user.username());
		model.setFullName(user.fullName());
		model.setEmail(user.email());
		model.setPhone(user.phone());
		model.setJobTitle(user.jobTitle());
		model.setActive(user.active());
		model.setRole(user.role());
		model.setDepartmentId(user.department() == null ? null : user.department().id());
		return model;
	}
}