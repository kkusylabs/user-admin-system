package io.github.kkusylabs.useradmin.backend.dtos;

import io.github.kkusylabs.useradmin.backend.models.Role;
import jakarta.validation.constraints.*;

/**
 * Request payload for creating a new user.
 * <p>
 * This request contains the necessary identity, authentication, and profile
 * information required to create a {@code User}.
 *
 * <h3>Fields</h3>
 * <ul>
 *     <li><b>username</b> – unique username used for login</li>
 *     <li><b>password</b> – plain-text password (will be hashed before storage)</li>
 *     <li><b>fullName</b> – user's full display name</li>
 *     <li><b>email</b> – unique email address</li>
 *     <li><b>phone</b> – phone number</li>
 *     <li><b>jobTitle</b> – user's job title</li>
 *     <li><b>active</b> – whether the user is active upon creation</li>
 *     <li><b>departmentId</b> – identifier of the associated department</li>
 *     <li><b>role</b> – role assigned to the user</li>
 * </ul>
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