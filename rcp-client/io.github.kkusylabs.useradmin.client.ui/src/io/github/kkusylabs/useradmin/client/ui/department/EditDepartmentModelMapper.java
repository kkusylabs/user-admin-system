package io.github.kkusylabs.useradmin.client.ui.department;

import java.util.Objects;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentDetailsResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;

public final class EditDepartmentModelMapper {

	private EditDepartmentModelMapper() {
	}

	public static EditDepartmentModel fromResponse(DepartmentListItemResponse response) {
		Objects.requireNonNull(response, "response must not be null");
		return fromDetails(response.department());
	}

	public static EditDepartmentModel fromDetails(DepartmentDetailsResponse details) {
		Objects.requireNonNull(details, "details must not be null");

		EditDepartmentModel model = new EditDepartmentModel();
		model.setId(details.id());
		model.setName(details.name());
		model.setDescription(details.description());
		model.setActive(details.active());

		return model;
	}
}