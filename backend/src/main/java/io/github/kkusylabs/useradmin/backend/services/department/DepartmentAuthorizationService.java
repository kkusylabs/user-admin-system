package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotEmptyException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Centralizes authorization and validation rules for department operations.
 *
 * <p>This service provides two complementary APIs:</p>
 * <ul>
 *   <li><b>Capability checks</b> (e.g. {@code canCreate}) for UI hints and conditional rendering</li>
 *   <li><b>Validation methods</b> (e.g. {@code validateCreateRequest}) that enforce rules and throw on failure</li>
 * </ul>
 *
 * <p>Rules enforced:</p>
 * <ul>
 *   <li>Only administrators can create, update, or delete departments</li>
 *   <li>A department must have no users before it can be deleted</li>
 * </ul>
 *
 * <p>Callers should use {@code can*} methods for lightweight checks and always rely on
 * {@code validate*} methods before executing state-changing operations.</p>
 */
@Component
public class DepartmentAuthorizationService {

    private final UserRepository userRepository;

    public DepartmentAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns whether the given user is allowed to create departments.
     *
     * <p>Intended for UI-level checks. Does not throw.</p>
     *
     * @param actor the current user (may be {@code null})
     * @return {@code true} if the user is an administrator
     */
    public boolean canCreate(User actor) {
        return isAdmin(actor);
    }

    /**
     * Returns whether the given user is allowed to update departments.
     *
     * @param actor the current user (may be {@code null})
     * @return {@code true} if the user is an administrator
     */
    public boolean canUpdate(User actor, Department department) {
        return isAdmin(actor);
    }

    /**
     * Returns whether the given user is allowed to delete departments in principle.
     *
     * <p>This does not check whether a specific department is deletable
     * (e.g. whether it is empty).</p>
     *
     * @param actor the current user (may be {@code null})
     * @return {@code true} if the user is an administrator
     */
    public boolean canDelete(User actor, Department department) {
        return isAdmin(actor);
    }

    /**
     * Validates that the given user can create a department.
     *
     * @param actor the current user
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    public void validateCreateRequest(User actor) {
        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException("Only administrators can create departments.");
        }
    }

    /**
     * Validates that the given user can update the specified department.
     *
     * @param actor the current user
     * @param department the department to update
     * @throws IllegalArgumentException if {@code department} is {@code null}
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    public void validateUpdateRequest(User actor, Department department) {
        Objects.requireNonNull(department, "Department is required.");

        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException("Only administrators can update departments.");
        }
    }

    public void validateDeleteRequest(User actor, Department department) {
        Objects.requireNonNull(department, "Department is required.");

        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException("Only administrators can delete departments.");
        }

        if (hasUsers(department)) {
            throw new DepartmentNotEmptyException(department.getId());
        }
    }

    private boolean isAdmin(User actor) {
        return actor != null && actor.isAdmin();
    }

    private boolean hasUsers(Department department) {
        return userRepository.existsByDepartmentId(department.getId());
    }
}