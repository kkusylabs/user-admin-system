package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.models.Role;

import java.util.List;
import java.util.Set;

/**
 * Describes the current actor's permissions and assignable options for creating a user.
 *
 * <p>Includes whether user creation is allowed and, if so, which roles and
 * departments may be assigned. If creation is not allowed, {@code reason}
 * explains why.</p>
 *
 * <p>This DTO is intended for query/UI use and does not enforce authorization.
 * Command-side validation is handled by the service layer.</p>
 *
 * @param canCreate whether the actor is allowed to create a user
 * @param roleOptions roles the actor may assign to the new user
 * @param departmentOptions departments the actor may assign to the new user
 * @param reason explanation for denial when {@code canCreate} is {@code false}, otherwise {@code null}
 */
public record CreateUserCapabilities(

        boolean canCreate,
        Set<Role> roleOptions,
        List<DepartmentOption> departmentOptions,
        String reason

) {
    /**
     * Creates a capability response that denies user creation.
     *
     * @param reason explanation shown to the caller
     * @return a response with no assignable roles or departments
     */
    public static CreateUserCapabilities none(String reason) {
        return new CreateUserCapabilities(
                false,
                Set.of(),
                List.of(),
                reason
        );
    }
}
