package io.github.kkusylabs.useradmin.backend.dtos.user;

import java.util.List;
import java.util.Set;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

/**
 * Describes the current actor's permissions and available options when updating a user.
 *
 * <p>This object is intended for query/UI use. It indicates which fields may be
 * modified and which values may be assigned. It does not enforce authorization;
 * command-side validation is handled by the service layer.</p>
 *
 * @param canUpdate whether the actor is allowed to update the target user at all
 * @param canEditProfile whether basic profile fields (e.g., fullName, email, phone) may be edited
 * @param canEditJobTitle whether the job title may be edited
 * @param canEditRole whether the user's role may be changed
 * @param canEditDepartment whether the user's department may be changed
 * @param canEditActive whether the user's active status may be changed
 * @param roleOptions roles the actor may assign (if role editing is allowed)
 * @param departmentOptions departments available for selection; if editing is not allowed,
 *                          contains the current department for display purposes
 * @param reason explanation for denial when {@code canUpdate} is {@code false}, otherwise {@code null}
 */
public record UpdateUserCapabilities(
        boolean canUpdate,
        boolean canEditProfile,
        boolean canEditJobTitle,
        boolean canEditRole,
        boolean canEditDepartment,
        boolean canEditActive,
        Set<Role> roleOptions,
        List<DepartmentOption> departmentOptions,
        String reason

) {

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