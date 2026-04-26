package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.exceptions.ValidationException;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotEmptyException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

/**
 * Centralizes authorization and validation rules for department operations.
 *
 * <p>Provides two complementary APIs:</p>
 * <ul>
 *   <li><b>Capability checks</b> ({@code can*}) for UI-level decisions (no side effects)</li>
 *   <li><b>Validation methods</b> ({@code validate*}) that enforce rules and throw exceptions on failure</li>
 * </ul>
 *
 * <p>Rules enforced:</p>
 * <ul>
 *   <li>Only administrators can create, update, or delete departments</li>
 *   <li>A department must be empty before it can be deleted</li>
 * </ul>
 *
 * <p>All validation methods are fail-fast and throw domain-specific exceptions.</p>
 */
@Component
public class DepartmentAuthorizationService {

    private final UserRepository userRepository;

    public DepartmentAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns whether the user can create departments.
     *
     * @param actor current user (may be {@code null})
     * @return {@code true} if the user is an administrator
     */
    public boolean canCreate(User actor) {
        return isAdmin(actor);
    }

    /**
     * Returns whether the user can update the given department.
     *
     * @param actor current user (may be {@code null})
     * @param department target department
     * @return {@code true} if the user is an administrator
     */
    public boolean canUpdate(User actor, Department department) {
        return isAdmin(actor);
    }

    /**
     * Returns whether the user can delete the given department.
     *
     * <p>Requires the user to be an administrator and the department to be empty.</p>
     *
     * @param actor current user (may be {@code null})
     * @param department target department (may be {@code null})
     * @return {@code true} if the user is an administrator and the department is deletable
     */
    public boolean canDelete(User actor, Department department) {
        return isAdmin(actor) && isDeletable(department);
    }

    /**
     * Validates that the user can create a department.
     *
     * @param actor current user (may be {@code null})
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    public void validateCreateRequest(User actor) {
        requireAdmin(actor, "Only administrators can create departments.");
    }

    /**
     * Validates that the user can update the given department.
     *
     * @param actor current user (may be {@code null})
     * @param department target department (must not be {@code null})
     * @throws ValidationException if {@code department} is {@code null}
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    public void validateUpdateRequest(User actor, Department department) {
        requireDepartment(department);
        requireAdmin(actor, "Only administrators can update departments.");
    }


    /**
     * Validates that the user can delete the given department.
     *
     * <p>Also ensures the department has no users assigned.</p>
     *
     * @param actor current user (may be {@code null})
     * @param department target department (must not be {@code null})
     * @throws ValidationException if {@code department} is {@code null}
     * @throws InsufficientPermissionsException if the user is not an administrator
     * @throws DepartmentNotEmptyException if the department still has users
     */
    public void validateDeleteRequest(User actor, Department department) {
        requireDepartment(department);
        requireAdmin(actor,"Only administrators can delete departments.");

        if (!isDeletable(department)) {
            throw new DepartmentNotEmptyException(department.getId());
        }
    }

    /**
     * Returns whether the given user is an administrator.
     *
     * @param actor user (may be {@code null})
     * @return {@code true} if the user is admin
     */
    private boolean isAdmin(User actor) {
        return actor != null && actor.isAdmin();
    }

    /**
     * Returns whether the given department can be safely deleted.
     *
     * <p>A department is deletable if it is not {@code null} and has no users assigned.</p>
     *
     * @param department department to check (may be {@code null})
     * @return {@code true} if the department has no associated users
     */
    public boolean isDeletable(Department department) {
        return department != null && !userRepository.existsByDepartmentId(department.getId());
    }

    /**
     * Ensures that the given user has administrator privileges.
     *
     * <p>This is an internal guard used by validation methods to enforce
     * authorization rules. It follows a fail-fast approach and throws
     * immediately if the requirement is not met.</p>
     *
     * @param actor current user (may be {@code null})
     * @param message error message used for the exception
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    private void requireAdmin(User actor, String message) {
        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException(message);
        }
    }

    /**
     * Ensures that a department is provided.
     *
     * <p>This is an internal validation guard used by request validation methods.
     * It follows a fail-fast approach and throws immediately if the department
     * is {@code null}.</p>
     *
     * @param department department to validate (may be {@code null})
     * @throws ValidationException if {@code department} is {@code null}
     */
    private void requireDepartment(Department department) {
        if (department == null) {
            throw new ValidationException("Department is required.");
        }
    }
}