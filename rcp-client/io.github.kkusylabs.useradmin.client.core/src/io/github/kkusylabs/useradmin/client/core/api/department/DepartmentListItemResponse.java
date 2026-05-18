package io.github.kkusylabs.useradmin.client.core.api.department;

/**
 * Department entry returned in department API responses.
 *
 * @param department department details
 * @param canUpdate indicates whether the current user can update the department
 * @param canDelete indicates whether the current user can delete the department
 */
public record DepartmentListItemResponse(
		DepartmentDetailsResponse department, 
		boolean canUpdate, 
		boolean canDelete) {
}