package io.github.kkusylabs.useradmin.client.core.api.department;

public record DepartmentListItemResponse(
		DepartmentDetailsResponse department, 
		boolean canUpdate, 
		boolean canDelete) {
}