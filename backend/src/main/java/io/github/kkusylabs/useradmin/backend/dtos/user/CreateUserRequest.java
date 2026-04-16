package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.models.Role;
import jakarta.validation.constraints.*;

/**
 * Request payload for creating a user.
 *
 * @param username     unique username used for login (alphanumeric, '.', '_', '-')
 * @param password     plain-text password (will be hashed before storage; 8–100 characters)
 * @param fullName     user's full display name
 * @param email        user's email address (must be unique if provided)
 * @param phone        user's phone number
 * @param jobTitle     user's job title
 * @param active       whether the user is active upon creation
 * @param departmentId identifier of the associated department
 * @param role         role assigned to the user
 * @author kkusy
 */
public record CreateUserRequest(
        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
        String username,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @NotBlank
        @Size(max = 100)
        String fullName,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone,

        @Size(max = 100)
        String jobTitle,

        Boolean active,

        @NotNull
        Long departmentId,

        Role role
) {
}