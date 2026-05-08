package io.github.kkusylabs.useradmin.client.core.api.department;

public record DepartmentDetailsResponse(
		Long id, String name, 
		String description, 
		boolean active) {
}