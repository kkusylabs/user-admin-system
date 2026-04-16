package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentCapabilities;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class DepartmentAuthorizationService {

    private final UserRepository userRepository;

    public DepartmentAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateCreate(User actor) {
        requireAdmin(actor, "Only administrators can create departments.");
    }

    public void validateRename(User actor) {
        requireAdmin(actor, "Only administrators can rename departments.");
    }

    public void validateDelete(User actor, Department department) {
        requireAdmin(actor, "Only administrators can delete departments.");

        if (!isEmpty(department)) {
            throw new IllegalStateException("Cannot delete a department that still has users.");
        }
    }

    public CreateDepartmentCapabilities getCreateCapabilities(User actor) {
        return new CreateDepartmentCapabilities(actor != null && actor.isAdmin());
    }

    public DepartmentCapabilities getCapabilities(User actor, Department department) {
        boolean canRename = actor != null && actor.isAdmin();

        if (actor == null || !actor.isAdmin()) {
            return new DepartmentCapabilities(
                    false,
                    false,
                    "Only administrators can delete departments."
            );
        }

        if (!isEmpty(department)) {
            return new DepartmentCapabilities(
                    true,
                    false,
                    "Cannot delete a department that still has users."
            );
        }

        return new DepartmentCapabilities(
                true,
                true,
                null
        );
    }

    private boolean isEmpty(Department department) {
        return !userRepository.existsByDepartmentId(department.getId());
    }

    private void requireAdmin(User actor, String message) {
        if (actor == null || !actor.isAdmin()) {
            throw new InsufficientPermissionsException(message);
        }
    }
}
