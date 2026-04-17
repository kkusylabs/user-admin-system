package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.utils.StringNormalizer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a user.
 *
 * <p>All fields are optional. {@code null} values are treated as unchanged.</p>
 *
 * @param fullName     updated display name
 * @param email        updated email address (must be valid if provided)
 * @param phone        updated phone number
 * @param jobTitle     updated job title
 * @param active       updated active state
 * @param departmentId updated department identifier
 * @param role         updated role
 * @author kkusy
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
) {
        public UpdateUserRequest {
                fullName = StringNormalizer.trim(fullName);
                phone = StringNormalizer.trim(phone);
                jobTitle = StringNormalizer.trim(jobTitle);
                email = StringNormalizer.trim(email);
        }
}