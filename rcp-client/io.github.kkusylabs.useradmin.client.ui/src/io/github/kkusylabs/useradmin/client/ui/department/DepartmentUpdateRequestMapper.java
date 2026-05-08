package io.github.kkusylabs.useradmin.client.ui.department;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.department.UpdateDepartmentRequest;

public final class DepartmentUpdateRequestMapper {

	private DepartmentUpdateRequestMapper() {
	}

	public static UpdateDepartmentRequest toRequest(EditDepartmentModel model) {
		Objects.requireNonNull(model, "model must not be null");

		return new UpdateDepartmentRequest(normalize(model.getName()), normalizeToNull(model.getDescription()),
				model.isActive());
	}

	private static String normalize(String value) {
		return value == null ? null : value.trim();
	}

	private static String normalizeToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}