package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.List;
import java.util.Set;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

/**
 * Capability and reference data required for user creation workflows.
 *
 * @param canCreate indicates whether the current user can create users
 * @param roleOptions available role assignments for new users
 * @param departmentOptions available department selections for new users
 * @param reason optional explanation when user creation is unavailable
 */
public record CreateUserCapabilities(
		boolean canCreate, 
		Set<Role> roleOptions,
		List<DepartmentOption> departmentOptions, 
		String reason) {
}