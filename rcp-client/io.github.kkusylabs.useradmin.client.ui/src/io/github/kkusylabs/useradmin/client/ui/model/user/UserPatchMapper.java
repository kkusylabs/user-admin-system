package io.github.kkusylabs.useradmin.client.ui.model.user;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.user.UserDetailResponse;

public final class UserPatchMapper {

	private UserPatchMapper() {
	}

	public static Map<String, Object> toPatch(UserDetailResponse original, EditUserModel edited) {
		Objects.requireNonNull(original, "original must not be null");
		Objects.requireNonNull(edited, "edited must not be null");

		Map<String, Object> patch = new LinkedHashMap<>();

		putIfChanged(patch, "fullName", original.fullName(), edited.getFullName());
		putIfChanged(patch, "email", original.email(), edited.getEmail());
		putIfChanged(patch, "phone", original.phone(), edited.getPhone());
		putIfChanged(patch, "jobTitle", original.jobTitle(), edited.getJobTitle());

		if (original.active() != edited.isActive()) {
			patch.put("active", edited.isActive());
		}

		Long originalDepartmentId = original.department() == null ? null : original.department().id();

		putIfChanged(patch, "departmentId", originalDepartmentId, edited.getDepartmentId());
		putIfChanged(patch, "role", original.role(), edited.getRole());

		return patch;
	}

	private static void putIfChanged(Map<String, Object> patch, String fieldName, Object originalValue,
			Object editedValue) {

		if (!Objects.equals(originalValue, editedValue)) {
			patch.put(fieldName, editedValue);
		}
	}
}
