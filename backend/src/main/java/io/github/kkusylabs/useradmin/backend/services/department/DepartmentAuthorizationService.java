package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentCapabilities;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotEmptyException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Handles authorization and capability checks for department-related operations.
 *
 * <p>Provides two types of functionality:</p>
 * <ul>
 *     <li>Validation methods that enforce permissions and throw exceptions on failure</li>
 *     <li>Capability methods that describe what actions the current user is allowed to perform</li>
 * </ul>
 *
 * <p>Current rules:</p>
 * <ul>
 *     <li>Only administrators can create, rename, or delete departments</li>
 *     <li>A department can only be deleted if it has no associated users</li>
 * </ul>
 *
 * @author kkusy
 */
@Component
public class DepartmentAuthorizationService {

    private final UserRepository userRepository;

    public DepartmentAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates whether the actor can create a department.
     *
     * @throws InsufficientPermissionsException if the actor is not an administrator
     */
    public void validateCreate(User actor) {
        requireAdmin(actor, "Only administrators can create departments.");
    }

    /**
     * Validates whether the actor can rename a department.
     *
     * @throws InsufficientPermissionsException if the actor is not an administrator
     */
    public void validateRename(User actor) {
        requireAdmin(actor, "Only administrators can rename departments.");
    }

    /**
     * Validates whether the actor can delete a department.
     *
     * @throws InsufficientPermissionsException if the actor is not an administrator
     * @throws DepartmentNotEmptyException      if the department still has associated users
     */
    public void validateDelete(User actor, Department department) {
        requireAdmin(actor, "Only administrators can delete departments.");

        if (!isEmpty(department)) {
            throw new DepartmentNotEmptyException(department.getId());
        }
    }

    /**
     * Returns whether the actor can create departments.
     */
    public CreateDepartmentCapabilities getCreateCapabilities(User actor) {
        return new CreateDepartmentCapabilities(actor != null && actor.isAdmin());
    }

    /**
     * Returns the capabilities of the actor for a given department.
     *
     * <p>Includes both allowed actions and reasons when an action is not permitted.</p>
     */
    public DepartmentCapabilities getCapabilities(User actor, Department department) {
        boolean isAdmin = actor != null && actor.isAdmin();

        if (!isAdmin) {
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
            throw new InsufficientPermissionsException();
        }
    }
}