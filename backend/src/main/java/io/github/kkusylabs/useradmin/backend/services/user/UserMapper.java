package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link User} entities and user-related DTOs.
 *
 *
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
    public void updateUser(User user, UpdateUserRequest request, Department requestedDepartment) {
        if (request.fullName().isPresent())  {
            user.setFullName(request.fullName().get());
        }

        if (request.email().isPresent()) {
            user.setEmail(request.email().get());
        }

        if (request.phone().isPresent()) {
            user.setPhone(request.phone().orElse(null));
        }

        if (request.jobTitle().isPresent()) {
            user.setJobTitle(request.jobTitle().orElse(null));
        }

        if (request.active().isPresent()) {
            user.setActive(request.active().get());
        }

        if (request.role().isPresent()) {
            user.setRole(request.role().get());
        }

        if (request.departmentId().isPresent()) {
            user.setDepartment(requestedDepartment);
        }
    }

    public UserDetailResponse toDetailResponse(User user) {
        Department department = user.getDepartment();

        DepartmentOption departmentOption = department == null
                ? null
                : new DepartmentOption(department.getId(), department.getName());

        return new UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getJobTitle(),
                user.isActive(),
                user.getRole(),
                departmentOption
        );
    }

  public UserListItemResponse toListItemResponse(
            User user,
            boolean canEdit,
            boolean canDelete
    ) {
        return new UserListItemResponse(
                toDetailResponse(user),
                canEdit,
                canDelete
        );
  }

    public EditUserResponse toEditResponse(
            User user,
            UpdateUserCapabilities updateCapabilities
    ) {
        return new EditUserResponse(
                toDetailResponse(user),
                updateCapabilities
        );
    }
}