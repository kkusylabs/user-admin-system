package io.github.kkusylabs.useradmin.client.core.api.department;

public record UpdateDepartmentRequest(
		String name, 
		String description, 
		Boolean active) {
}