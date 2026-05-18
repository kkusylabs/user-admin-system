package io.github.kkusylabs.useradmin.client.ui.composite.department;

import io.github.kkusylabs.useradmin.client.core.api.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.UpdateDepartmentRequest;

/**
 * Callback interface for department detail actions initiated from the UI.
 *
 * <p>
 * Implementations handle department create, update, edit, and cancel
 * workflows triggered from the department details composite.
 * </p>
 */
public interface DepartmentDetailsActions {

	/**
	 * Requests that the specified department be loaded into edit mode.
	 *
	 * @param department selected department item
	 */
	void editDepartmentRequested(DepartmentListItemResponse department);

	/**
	 * Requests creation of a new department.
	 *
	 * @param request create request containing department details
	 */
	void createDepartmentRequested(CreateDepartmentRequest request);

	/**
	 * Requests update of an existing department.
	 *
	 * @param departmentId identifier of the department to update
	 * @param request update request containing modified department values
	 */
	void updateDepartmentRequested(long departmentId, UpdateDepartmentRequest request);

	/**
	 * Requests cancellation of the current create or edit workflow.
	 */
	void cancelRequested();
}
