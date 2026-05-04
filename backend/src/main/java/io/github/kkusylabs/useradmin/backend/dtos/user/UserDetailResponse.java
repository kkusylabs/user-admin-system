package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

/**
 * Detailed representation of a user.
 *
 * @param id database identifier
 * @param username login username
 * @param fullName display name
 * @param email email address
 * @param phone phone number, or {@code null} if not set
 * @param jobTitle job title, or {@code null} if not set
 * @param active whether the account is active
 * @param role assigned security role
 * @param department assigned department, or {@code null} if unavailable
 */
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
