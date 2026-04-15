package io.github.kkusylabs.useradmin.backend.dtos;

import io.github.kkusylabs.useradmin.backend.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating an existing user.
 * <p>
 * All fields are optional. Any field left {@code null} is treated as unchanged.
 *
 * <h3>Fields</h3>
 * <ul>
 *     <li><b>fullName</b> – updated display name</li>
 *     <li><b>email</b> – updated email address, if provided</li>
 *     <li><b>phone</b> – updated phone number</li>
 *     <li><b>jobTitle</b> – updated job title</li>
 *     <li><b>active</b> – updated active state</li>
 *     <li><b>departmentId</b> – updated department identifier</li>
 *     <li><b>role</b> – updated role</li>
 * </ul>
 */
public record UpdateUserRequest(
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

        Long departmentId,

        Role role
) {}
