package io.github.kkusylabs.useradmin.backend.models;

/**
 * Represents the security role assigned to a user.
 * <p>
 * Roles determine what actions a user is allowed to perform in the system.
 */
public enum Role {

    /**
     * A system administrator with full access to manage users,
     * departments, roles, and cross-department operations.
     */
    ADMIN,

    /**
     * A department manager with elevated privileges, typically limited
     * to managing users and operations within their own department.
     */
    MANAGER,

    /**
     * A basic user with standard access, such as viewing data that is
     * available to authenticated users.
     */
    USER
}