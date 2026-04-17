package io.github.kkusylabs.useradmin.backend.mappers;

import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.user.UserResponse;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link User} entities and user-related DTOs.
 *
 * <p>
 * This component is responsible for constructing and updating {@link User}
 * entities from request DTOs and for converting entities into
 * {@link UserResponse} objects.
 * </p>
 *
 * <p>
 * Normalization and field-level transformations, such as trimming text and
 * hashing passwords, are handled here to keep service-layer code focused on
 * application flow.
 * </p>
 */
@Component
public final class UserMapper {
    /**
     * Creates a new {@link User} entity from a creation request.
     *
     * <p>
     * String fields are normalized as needed before being assigned. The password
     * is hashed before storage. If {@code active} is not provided, it defaults
     * to {@code true}.
     * </p>
     *
     * @param request the user creation request
     * @param department the resolved department for the new user, or {@code null}
     * @return a new user entity populated from the request
     */
    public User fromCreateRequest(CreateUserRequest request, Department department) {
        User user = new User();
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setJobTitle(request.jobTitle());
        user.setActive(request.active());
        user.setRole(request.role());
        user.setDepartment(department);
        return user;
    }

    /**
     * Applies an update request to an existing {@link User} entity.
     *
     * <p>
     * Only non-{@code null} fields in the request are applied. Fields omitted
     * from the request are left unchanged.
     * </p>
     *
     * @param user the user entity to update
     * @param request the update request
     * @param requestedDepartment the resolved department to assign, or {@code null}
     *                            if no department change was requested
     */
    public void updateEntity(User user, UpdateUserRequest request, Department requestedDepartment) {
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }

        if (request.email() != null) {
            user.setEmail(request.email());
        }

        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        if (request.jobTitle() != null) {
            user.setJobTitle(request.jobTitle());
        }

        if (request.active() != null) {
            user.setActive(request.active());
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (requestedDepartment != null) {
            user.setDepartment(requestedDepartment);
        }
    }

    /**
     * Converts a {@link User} entity into a {@link UserResponse}.
     *
     * @param user the user entity
     * @param department the user's department, or {@code null}
     * @return the corresponding response DTO
     */
    public UserResponse toResponse(User user, Department department) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getJobTitle(),
                user.isActive(),
                user.getRole(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null
        );
    }
}