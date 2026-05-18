package io.github.kkusylabs.useradmin.client.core.api.department;

/**
 * Detailed department information returned by the API.
 *
 * @param id unique department identifier
 * @param name department name
 * @param description department description
 * @param active indicates whether the department is currently active
 */
public record DepartmentDetailsResponse(
		Long id, String name, 
		String description, 
		boolean active) {
}