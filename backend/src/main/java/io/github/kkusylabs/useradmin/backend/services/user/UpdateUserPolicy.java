package io.github.kkusylabs.useradmin.backend.services.user;


import io.github.kkusylabs.useradmin.backend.models.Role;

import java.util.Objects;
import java.util.Set;

/**
 * Represents the authorization policy for updating a user.
 *
 * <p>This object is used for command-side validation and answers:
 * <ul>
 *     <li>whether the actor may update the target at all</li>
 *     <li>which categories of changes are allowed</li>
 *     <li>which roles may be assigned</li>
 *     <li>why the update is not allowed, if applicable</li>
 * </ul>
 *
 * <p>This policy does not include UI concerns such as department option lists.
 */
public record UpdateUserPolicy(
        boolean canUpdate,
        boolean canEditProfile,
        boolean canEditJobTitle,
        boolean canEditRole,
        boolean canEditDepartment,
        boolean canEditActive,
        String reason
) {
    public UpdateUserPolicy {
    }

    /**
     * Creates a denial policy with no editable fields.
     *
     * @param reason explanation for denial
     * @return a denial policy
     */
    public static UpdateUserPolicy denied(String reason) {
        return new UpdateUserPolicy(
                false,
                false,
                false,
                false,
                false,
                false,
                reason
        );
    }

    /**
     * Creates an admin policy.
     *
     * @param assignableRoles the roles the admin may assign
     * @return an admin update policy
     */
    public static UpdateUserPolicy admin() {
        return new UpdateUserPolicy(
                true,
                true,
                true,
                true,
                true,
                true,
                null
        );
    }

    public static UpdateUserPolicy adminSelf() {
        return new UpdateUserPolicy(
                true,
                true,
                true,
                false,
                true,
                false,
                null
        );
    }

    /**
     * Creates a self-service profile-only policy.
     *
     * @return a self-service update policy
     */
    public static UpdateUserPolicy selfProfileOnly() {
        return new UpdateUserPolicy(
                true,
                true,
                false,
                false,
                false,
                false,
                null
        );
    }

    /**
     * Creates a manager policy for updating a managed basic user.
     *
     * @return a manager update policy
     */
    public static UpdateUserPolicy managerManagedUser() {
        return new UpdateUserPolicy(
                true,
                true,
                true,
                false,
                false,
                true,
                null
        );
    }
}
