package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.models.Role;

/**
 * Response payload representing a user.
 *
 * @param id             the unique identifier of the user
 * @param username       the username used for login
 * @param fullName       the user's full display name
 * @param email          the user's email address (if provided)
 * @param phone          the user's phone number (if provided)
 * @param jobTitle       the user's job title (if provided)
 * @param active         whether the user is active
 * @param role           the role assigned to the user
 * @param departmentId   the identifier of the user's department
 * @param departmentName the name of the user's department
 * @author kkusy
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
        Long departmentId,
        String departmentName
) {
}