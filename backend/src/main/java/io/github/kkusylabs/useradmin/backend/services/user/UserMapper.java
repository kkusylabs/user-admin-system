package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link User} entities and user-related DTOs.
 */
@Component
public final class UserMapper {
    /**
     * Creates a new {@link User} entity from a creation request.
     *
     * <p>The password is intentionally not copied here because it must be encoded
     * before it is stored.</p>
     *
     * @param request user creation request
     * @param department resolved department to assign
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
     * Applies a partial update request to an existing {@link User}.
     *
     * <p>Only fields present in the request are applied. Present nullable fields
     * such as phone and job title may clear the existing value.</p>
     *
     * @param user user entity to update
     * @param request update request
     * @param requestedDepartment resolved department to assign when a department
     *                            change was requested; otherwise {@code null}
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

    /**
     * Converts a user entity to a user detail response DTO.
     *
     * @param user user entity to convert
     * @return detail response for the user
     */
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

    /**
     * Converts a user entity to a list item response.
     *
     * @param user user entity to convert
     * @param canUpdate whether the current actor may update the user
     * @param canDelete whether the current actor may delete the user
     * @return list item response for the user
     */
  public UserListItemResponse toListItemResponse(
            User user,
            boolean canUpdate,
            boolean canDelete
    ) {
        return new UserListItemResponse(
                toDetailResponse(user),
                canUpdate,
                canDelete
        );
  }

    /**
     * Converts a user entity and update capabilities to an edit response.
     *
     * @param user user being edited
     * @param updateCapabilities actor-relative update capabilities
     * @return edit response for the user
     */
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