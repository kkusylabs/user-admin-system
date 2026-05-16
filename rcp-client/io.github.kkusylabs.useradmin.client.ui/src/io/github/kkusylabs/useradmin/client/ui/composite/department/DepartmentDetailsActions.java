package io.github.kkusylabs.useradmin.client.ui.composite.department;

import io.github.kkusylabs.useradmin.client.core.api.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.UpdateDepartmentRequest;

public interface DepartmentDetailsActions {

	void editDepartmentRequested(DepartmentListItemResponse department);

	void createDepartmentRequested(CreateDepartmentRequest request);

	void updateDepartmentRequested(long departmentId, UpdateDepartmentRequest request);

	void cancelRequested();
}
