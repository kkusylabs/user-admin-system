package io.github.kkusylabs.useradmin.client.core.api.department;

/**
 * Request used to update an existing department.
 *
 * @param name updated department name
 * @param description updated department description
 * @param active updated active status
 */
public record UpdateDepartmentRequest(
		String name, 
		String description, 
		Boolean active) {
}