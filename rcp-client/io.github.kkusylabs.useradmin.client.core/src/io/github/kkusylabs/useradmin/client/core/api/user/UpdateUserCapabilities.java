package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.List;
import java.util.Set;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

public record UpdateUserCapabilities(
		boolean canUpdate, 
		boolean canEditProfile,
		boolean canEditJobTitle,
		boolean canEditRole,
		boolean canEditDepartment,
		boolean canEditActive, 
		Set<Role> roleOptions, 
		List<DepartmentOption> departmentOptions, 
		String reason) {
}
