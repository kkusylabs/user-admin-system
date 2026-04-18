package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.*;
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
     * <p>Copies fields from the request into a new entity and assigns the
     * resolved {@link Department}. This method does not perform validation,
     * apply defaults, or handle password encoding.</p>
     *
     * @param request     the user creation request
     * @param department  the resolved department, or {@code null}
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
     * <p>Includes department information and authorization capabilities
     * relative to the acting user.</p>
     *
     * @param user               the user entity
     * @param updateCapabilities the caller's update permissions for the user
     * @param deleteCapabilities the caller's delete permissions for the user
     * @return the response DTO
     */
    public UserResponse toResponse(
            User user,
            UpdateUserCapabilities updateCapabilities,
            DeleteUserCapabilities deleteCapabilities
    ) {
        Department department = user.getDepartment();

        DepartmentOption departmentOption = department == null
                ? null
                : new DepartmentOption(department.getId(), department.getName());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getJobTitle(),
                user.isActive(),
                user.getRole(),
                departmentOption,
                updateCapabilities,
                deleteCapabilities
        );
    }

}