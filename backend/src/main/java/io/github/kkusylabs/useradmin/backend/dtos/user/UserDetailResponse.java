package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

public record UserDetailResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String jobTitle,
        boolean active,
        Role role,
        DepartmentOption department
) {
}
