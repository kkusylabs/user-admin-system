package io.github.kkusylabs.useradmin.client.core.api.department;

import java.util.List;

/**
 * Response containing available departments and related permissions.
 *
 * @param departments department entries returned by the API
 * @param canCreate   indicates whether the current user can create departments
 */
public record DepartmentListResponse(
		List<DepartmentListItemResponse> departments, 
		boolean canCreate) {
}