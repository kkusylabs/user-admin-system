package io.github.kkusylabs.useradmin.backend.dtos.user;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

/**
 * Represents the capabilities available to a user when updating another user.
 *
 * <p>This object is intended for UI/query use and describes what actions
 * and options should be presented when rendering an "edit user" form.</p>
 *
 * <p>It is NOT used for validation or authorization enforcement.</p>
 */
public record UpdateUserCapabilities(

        /**
         * Whether the actor is allowed to update the target user at all.
         */
        boolean canUpdate,

        /**
         * Whether the actor can edit basic profile fields (e.g., fullName, email, phone).
         */
        boolean canEditProfile,

        /**
         * Whether the actor can edit the job title.
         */
        boolean canEditJobTitle,

        /**
         * Whether the actor can change the user's role.
         */
        boolean canEditRole,

        /**
         * Whether the actor can change the user's department.
         */
        boolean canEditDepartment,

        /**
         * Whether the actor can activate/deactivate the user.
         */
        boolean canEditActive,

        /**
         * The set of roles the actor is allowed to assign (if role editing is allowed).
         */
        Set<Role> roleOptions,

        /**
         * The list of department options to display in the UI.
         *
         * <p>If department editing is allowed, this will contain all selectable departments.
         * If not allowed, this should contain the target user's current department (if any)
         * so the UI can display it in a disabled control.</p>
         */
        List<DepartmentOption> departmentOptions,

        /**
         * Reason explaining why the update is not allowed (if canUpdate is false).
         */
        String reason

) {

    public UpdateUserCapabilities {
    }

    /**
     * Returns a capabilities object indicating no update is allowed.
     *
     * @param reason explanation for denial
     * @return a denial capabilities object
     */
    public static UpdateUserCapabilities none(String reason) {
        return new UpdateUserCapabilities(
                false,
                false,
                false,
                false,
                false,
                false,
                Set.of(),
                List.of(),
                reason
        );
    }
}