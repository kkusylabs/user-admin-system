package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Describes whether the current user can create a department.
 *
 * @param canCreateDepartment whether department creation is allowed
 * @author kkusy
 */
public record CreateDepartmentCapabilities(
        boolean canCreateDepartment
) {
}