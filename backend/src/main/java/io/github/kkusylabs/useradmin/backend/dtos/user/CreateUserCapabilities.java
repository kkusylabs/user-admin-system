package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

import java.util.List;
import java.util.Set;

public record CreateUserCapabilities(

        boolean canCreate,
        Set<Role> assignableRoles,
        List<DepartmentOption> assignableDepartments,
        String reason

) {
    public static CreateUserCapabilities none(String reason) {
        return new CreateUserCapabilities(
                false,
                Set.of(),
                List.of(),
                reason
        );
    }
}
