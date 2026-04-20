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
 * <p>Provides two complementary APIs:</p>
 * <ul>
 *   <li><b>Capability checks</b> ({@code can*}) for UI hints</li>
 *   <li><b>Validation methods</b> ({@code validate*}) that enforce rules and throw on failure</li>
 * </ul>
 *
 * <p>Rules enforced:</p>
 * <ul>
 *   <li>Only administrators can create, update, or delete departments</li>
 *   <li>A department must be empty before it can be deleted</li>
 * </ul>
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
     * Returns whether the user can delete the given department in principle.
     *
     * <p>Does not check whether the department is empty.</p>
     *
     * @param actor current user (may be {@code null})
     * @param department target department
     * @return {@code true} if the user is an administrator
     */
    public boolean canDelete(User actor, Department department) {
        return isAdmin(actor);
    }

    /**
     * Validates that the user can create a department.
     *
     * @param actor current user
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    public void validateCreateRequest(User actor) {
        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException("Only administrators can create departments.");
        }
    }


    /**
     * Validates that the user can update the given department.
     *
     * @param actor current user
     * @param department target department (must not be {@code null})
     * @throws IllegalArgumentException if {@code department} is {@code null}
     * @throws InsufficientPermissionsException if the user is not an administrator
     */
    public void validateUpdateRequest(User actor, Department department) {
        Objects.requireNonNull(department, "Department is required.");

        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException("Only administrators can update departments.");
        }
    }


    /**
     * Validates that the user can delete the given department.
     *
     * <p>Also ensures the department has no users assigned.</p>
     *
     * @param actor current user
     * @param department target department (must not be {@code null})
     * @throws IllegalArgumentException if {@code department} is {@code null}
     * @throws InsufficientPermissionsException if the user is not an administrator
     * @throws DepartmentNotEmptyException if the department still has users
     */
    public void validateDeleteRequest(User actor, Department department) {
        Objects.requireNonNull(department, "Department is required.");

        if (!isAdmin(actor)) {
            throw new InsufficientPermissionsException("Only administrators can delete departments.");
        }

        if (hasUsers(department)) {
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
     * Returns whether the department has any users assigned.
     *
     * @param department department
     * @return {@code true} if at least one user belongs to the department
     */
    private boolean hasUsers(Department department) {
        return userRepository.existsByDepartmentId(department.getId());
    }
}