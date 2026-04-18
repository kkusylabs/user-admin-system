package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

/**
 * Response payload representing a user.
 *
 * <p>Includes basic user information, associated department details,
 * and authorization capabilities relative to the acting user.</p>
 *
 * @param id                     the unique identifier of the user
 * @param username               the username used for login
 * @param fullName               the user's full display name
 * @param email                  the user's email address (if provided)
 * @param phone                  the user's phone number (if provided)
 * @param jobTitle               the user's job title (if provided)
 * @param active                 whether the user is active
 * @param role                   the role assigned to the user
 * @param departmentOption       the user's department, or {@code null} if none
 * @param updateUserCapabilities the caller's update permissions for the user
 * @param deleteUserCapabilities the caller's delete permissions for the user
 */
public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String jobTitle,
        boolean active,
        Role role,
        DepartmentOption departmentOption,
        UpdateUserCapabilities updateUserCapabilities,
        DeleteUserCapabilities deleteUserCapabilities
) {
}