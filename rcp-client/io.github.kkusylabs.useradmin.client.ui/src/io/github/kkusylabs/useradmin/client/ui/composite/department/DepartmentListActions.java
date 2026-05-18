package io.github.kkusylabs.useradmin.client.ui.composite.department;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;

/**
 * Callback interface for actions initiated from the department list UI.
 */
public interface DepartmentListActions {

	/**
	 * Requests creation of a new department.
	 */
	void addDepartmentRequested();

	/**
	 * Requests deletion of the specified department.
	 *
	 * @param department selected department item
	 */
	void deleteDepartmentRequested(DepartmentListItemResponse department);

	/**
	 * Notifies that a department was selected from the list.
	 *
	 * @param department selected department item
	 */
	void departmentSelected(DepartmentListItemResponse department);
}
