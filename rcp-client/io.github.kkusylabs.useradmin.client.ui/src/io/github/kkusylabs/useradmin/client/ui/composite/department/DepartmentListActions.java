package io.github.kkusylabs.useradmin.client.ui.composite.department;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;

public interface DepartmentListActions {

	void addDepartmentRequested();

	void deleteDepartmentRequested(DepartmentListItemResponse department);

	void departmentSelected(DepartmentListItemResponse department);
}
