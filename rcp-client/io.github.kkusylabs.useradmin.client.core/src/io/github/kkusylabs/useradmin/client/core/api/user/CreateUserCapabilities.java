package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.List;
import java.util.Set;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

public record CreateUserCapabilities(
		boolean canCreate, 
		Set<Role> assignableRoles,
		List<DepartmentOption> assignableDepartments, 
		String reason) {
}