package io.github.kkusylabs.useradmin.client.core.api.department;

/**
 * Request used to create a new department.
 *
 * @param name department name
 * @param description optional department description
 */
public record CreateDepartmentRequest(
		String name, 
		String description) {
}