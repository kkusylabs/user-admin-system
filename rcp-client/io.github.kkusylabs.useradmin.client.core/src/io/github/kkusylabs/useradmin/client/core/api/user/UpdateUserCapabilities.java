package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.List;
import java.util.Set;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

/**
 * Capability and reference data used for user update workflows.
 *
 * @param canUpdate indicates whether the current user can update the target user
 * @param canEditProfile indicates whether profile details can be modified
 * @param canEditJobTitle indicates whether the job title can be modified
 * @param canEditRole indicates whether the assigned role can be modified
 * @param canEditDepartment indicates whether the assigned department can be modified
 * @param canEditActive indicates whether the active status can be modified
 * @param roleOptions available role assignments for the target user
 * @param departmentOptions available department selections for the target user
 * @param reason optional explanation when updates are restricted
 */
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
