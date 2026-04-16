package io.github.kkusylabs.useradmin.backend.dtos.user;
import io.github.kkusylabs.useradmin.backend.models.Role;

/**
 * Response payload representing a user.
 * <p>
 * Contains user identity, profile, and organizational information.
 *
 * <h3>Fields</h3>
 * <ul>
 *     <li><b>id</b> – unique identifier of the user</li>
 *     <li><b>username</b> – username used for login</li>
 *     <li><b>fullName</b> – user's full display name</li>
 *     <li><b>email</b> – email address, if provided</li>
 *     <li><b>phone</b> – phone number, if provided</li>
 *     <li><b>jobTitle</b> – user's job title, if provided</li>
 *     <li><b>active</b> – whether the user is active</li>
 *     <li><b>role</b> – role assigned to the user</li>
 *     <li><b>departmentId</b> – identifier of the user's department</li>
 *     <li><b>departmentName</b> – name of the user's department</li>
 * </ul>
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
) {}
