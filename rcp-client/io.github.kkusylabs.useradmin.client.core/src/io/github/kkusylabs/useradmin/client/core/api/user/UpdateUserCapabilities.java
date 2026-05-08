package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.List;
import java.util.Set;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

public record UpdateUserCapabilities(
		boolean canUpdate, 
		boolean canEditFullName, 
		boolean canEditEmail,
		boolean canEditPhone, 
		boolean canEditJobTitle, 
		boolean canEditActive, 
		boolean canEditDepartment,
		boolean canEditRole, 
		Set<Role> assignableRoles, 
		List<DepartmentOption> assignableDepartments, 
		String reason) {
}
