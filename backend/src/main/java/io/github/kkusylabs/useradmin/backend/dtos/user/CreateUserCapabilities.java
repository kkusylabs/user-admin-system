package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

import java.util.List;
import java.util.Set;

public record CreateUserCapabilities(
        boolean canCreateUser,
        Set<Role> assignableRoles,
        List<DepartmentOption> assignableDepartments,
        Role defaultRole
) {
}
