package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentSummary;
import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.DeleteUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Service responsible for enforcing authorization rules for {@link User} operations.
 *
 * <p>Centralizes permission checks and capability calculations for creating,
 * updating, and deleting users. Rules are evaluated based on the acting user
 * ("actor"), the target user, and business constraints such as role and department.</p>
 *
 * <p>This service separates authorization logic from application flow, allowing
 * controllers and services to delegate permission decisions to a single component.</p>
 *
 * <p><strong>Note:</strong> Update-related authorization is currently a stub and
 * will be implemented in a future iteration.</p>
 */
@Component
public class UserAuthorizationService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    /**
     * Creates a new authorization service.
     *
     * @param userRepository repository used for queries required by authorization rules
     */
    public UserAuthorizationService(UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Validates whether the actor is allowed to create a user with the given role
     * and department.
     *
     * @param actor               the acting user
     * @param requestedRole       the role for the new user
     * @param requestedDepartment the department for the new user
     * @throws InsufficientPermissionsException if the actor is not allowed to create the user
     */
    public void validateCreation(User actor, Role requestedRole, Department requestedDepartment) {
        require(actor != null, "Actor is required.");

        if (actor.isAdmin()) {
            return;
        }

        if (actor.isManager()) {
            require(requestedRole == Role.USER,
                    "Managers may only create users with role USER.");
            require(sameDepartment(actor.getDepartment(), requestedDepartment),
                    "Managers may only create users in their own department.");
            return;
        }

        deny("You do not have permission to create users.");
    }

    public boolean canCreate(User actor) {
        require(actor != null, "Actor is required.");

        return actor.isAdmin() || actor.isManager();
    }

    /**
     * Computes creation capabilities for the actor.
     *
     * <p>Returns the roles, departments, and flags the actor is allowed to use when
     * creating a user. Intended for UI consumption (e.g. form configuration).</p>
     *
     * @param actor           the acting user
     * @return the actor's creation capabilities
     */
    public CreateUserCapabilities getCreateCapabilities(
            User actor
    ) {
        require(actor != null, "Actor is required.");

        if (actor.isAdmin()) {
            return new CreateUserCapabilities(
                    true,
                    Set.of(Role.ADMIN, Role.MANAGER, Role.USER),
                    getAllDepartmentOptions(),
                    Role.USER
            );
        }

        if (actor.isManager()) {
            Department department = actor.getDepartment();

            List<DepartmentOption> assignableDepartments =
                    department == null
                            ? List.of()
                            : List.of(new DepartmentOption(department.getId(), department.getName()));

            return new CreateUserCapabilities(
                    true,
                    Set.of(Role.USER),
                    assignableDepartments,
                    Role.USER
            );
        }

        return new CreateUserCapabilities(
                false,
                Set.of(),
                List.of(),
                Role.USER
        );
    }

    /**
     * Validates whether the actor is allowed to delete the target user.
     *
     * @param actor  the acting user
     * @param target the user to be deleted
     * @throws InsufficientPermissionsException if the actor is not allowed to delete the user
     */
    public void validateDeletion(User actor, User target) {
        require(actor != null, "Actor is required.");
        require(target != null, "Target user is required.");

        require(!sameUser(actor, target),
                "You may not delete your own account.");

        if (actor.isAdmin()) {
            require(!wouldLeaveSystemWithoutActiveAdmin(target),
                    "You may not delete the last active administrator.");
            return;
        }

        if (actor.isManager()) {
            require(canManageUser(actor, target),
                    "Managers can only delete basic users in their own department.");
            return;
        }

        deny("You do not have permission to delete users.");
    }

    public boolean canDelete(User actor, User target) {
        return getDeleteCapabilities(actor, target).canDelete();
    }

    /**
     * Computes deletion capabilities for the actor relative to the target user.
     *
     * <p>Wraps {@link #validateDeletion(User, User)} and converts the result into
     * a capability object suitable for API responses.</p>
     *
     * @param actor  the acting user
     * @param target the user being evaluated
     * @return the deletion capabilities, including denial reason if applicable
     */
    public DeleteUserCapabilities getDeleteCapabilities(User actor, User target) {
        try {
            validateDeletion(actor, target);
            return new DeleteUserCapabilities(true, null);
        } catch (InsufficientPermissionsException e) {
            return new DeleteUserCapabilities(false, e.getMessage());
        }
    }

    public void validateUpdate(User actor,
                               User target,
                               UpdateUserRequest request,
                               Department requestedDepartment) {


    }

    public boolean canEdit(User actor, User target) {
        require(actor != null, "Actor is required.");
        require(target != null, "Target user is required.");

        if (sameUser(actor, target)) {
            return true;
        }

        if (actor.isAdmin()) {
            return true;
        }

        if (actor.isManager()) {
            return canManageUser(actor, target);
        }

        return false;
    }

    public UpdateUserCapabilities getUpdateCapabilities(User actor, User target) {

        return new UpdateUserCapabilities(
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new InsufficientPermissionsException(message);
        }
    }

    private void deny(String message) {
        throw new InsufficientPermissionsException(message);
    }

    public static boolean sameUser(User a, User b) {
        return a != null
                && b != null
                && Objects.equals(a.getId(), b.getId());
    }

    public static boolean sameDepartment(Department a, Department b) {
        return a != null
                && b != null
                && Objects.equals(a.getId(), b.getId());
    }

    public static boolean canManageUser(User actor, User target) {
        return actor != null
                && target != null
                && actor.isManager()
                && target.isBasicUser()
                && sameDepartment(actor.getDepartment(), target.getDepartment());
    }

    /**
     * Returns all departments as UI-friendly options, sorted by name.
     */
    private List<DepartmentOption> getAllDepartmentOptions() {
        return departmentRepository.findAllBy().stream()
                .sorted(Comparator.comparing(DepartmentSummary::getName, String.CASE_INSENSITIVE_ORDER))
                .map(d -> new DepartmentOption(d.getId(), d.getName()))
                .toList();
    }

    /**
     * Determines whether deleting the given user would leave the system without
     * any active administrators.
     *
     * @param user the user being evaluated
     * @return {@code true} if this is the last active administrator
     */
    public boolean wouldLeaveSystemWithoutActiveAdmin(User user) {
        return user.isAdmin()
                && user.isActive()
                && userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1;
    }

}