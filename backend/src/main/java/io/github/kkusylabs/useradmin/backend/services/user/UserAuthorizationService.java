package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.exceptions.ValidationException;
import io.github.kkusylabs.useradmin.backend.exceptions.department.InactiveDepartmentException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.LastActiveAdminDeletionException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
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
    public UserAuthorizationService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    public boolean canCreate(User actor) {
        requirePresent(actor, "actor", "Actor is required.");
        return getCreatePolicy(actor).canCreate();
    }

    private CreateUserPolicy getCreatePolicy(User actor) {
        if (actor.isAdmin()) {
            return CreateUserPolicy.allowed();
        }

        if (actor.isManager()) {
            Department department = actor.getDepartment();

            if (department == null || !department.isActive()) {
                return CreateUserPolicy.denied(
                        "Managers must belong to an active department to create users."
                );
            }

            return CreateUserPolicy.allowed();
        }

        return CreateUserPolicy.denied(
                "You do not have permission to create users."
        );
    }

    public void validateCreateRequest(User actor, Role role, Department department) {
        requirePresent(actor, "actor", "Actor is required.");

        CreateUserPolicy policy = getCreatePolicy(actor);
        requirePermission(policy.canCreate(), policy.reason());

        validateCreateRole(actor, role);
        validateCreateDepartment(actor, department);
    }

    private void validateCreateRole(User actor, Role role) {
        requirePresent(role, "role", "Role is required.");

        requirePermission(
                canAssignRoleForCreate(actor, role),
                "You do not have permission to assign this role."
        );
    }

    private void validateCreateDepartment(User actor, Department department) {
        requirePresent(department, "department", "Department is required.");

        if (!department.isActive()) {
            throw new InactiveDepartmentException(department.getId());
        }

        requirePermission(canAssignDepartmentForCreate(actor, department),
                "You do not have permission to assign this department.");
    }

    private boolean canAssignRoleForCreate(User actor, Role role) {
        Set<Role> assignableRoles = getAssignableRolesForCreate(actor);
        return assignableRoles.contains(role);
    }

    private boolean canAssignDepartmentForCreate(User actor, Department department) {
        // Admins can assign to any department (active check handled elsewhere)
        if (actor.isAdmin()) {
            return true;
        }

        // Managers can only assign to their own active department
        if (actor.isManager()) {
            Department actorDepartment = actor.getDepartment();
            return sameDepartment(actorDepartment, department);
        }

        return false;
    }

    public CreateUserCapabilities getCreateCapabilities(User actor) {
        requirePresent(actor, "actor", "Actor is required.");

        CreateUserPolicy policy = getCreatePolicy(actor);
        if (!policy.canCreate()) {
            return CreateUserCapabilities.none(policy.reason());
        }

        List<DepartmentOption> departments = getAssignableDepartmentsForCreate(actor);
        if (departments.isEmpty()) {
            return CreateUserCapabilities.none("You cannot create users because there are no departments available to assign.");
        }

        Set<Role> roles = getAssignableRolesForCreate(actor);
        if (roles.isEmpty()) {
            return CreateUserCapabilities.none("You cannot create users because there are no roles available to assign.");
        }

        return new CreateUserCapabilities(
                true, // hasAssignableDepartments
                roles,
                departments,
                null // reason
        );
    }

    private Set<Role> getAssignableRolesForCreate(User actor) {
        if (actor.isAdmin()) {
            return EnumSet.allOf(Role.class);
        }

        if (actor.isManager()) {
            return EnumSet.of(Role.USER);
        }

        return EnumSet.noneOf(Role.class);
    }

    private List<DepartmentOption> getAssignableDepartmentsForCreate(User actor) {
        if (actor.isAdmin()) {
            return getSelectableDepartmentOptionsForCreate();
        }

        if (actor.isManager()) {
            Department department = actor.getDepartment();

            if (department != null && department.isActive()) {
                return List.of(new DepartmentOption(department.getId(), department.getName()));
            }
        }

        return List.of();
    }

    private List<DepartmentOption> getSelectableDepartmentOptionsForCreate() {
        return departmentRepository.findActiveOrderByNameIgnoreCase().stream()
                .map(d -> new DepartmentOption(d.getId(), d.getName()))
                .toList();
    }

    public boolean canDelete(User actor, User target) {
        return getDeletePolicy(actor, target).canDelete();
    }

    private DeleteUserPolicy getDeletePolicy(User actor, User target) {
        requirePresent(actor, "actor", "Actor is required.");
        requirePresent(actor, "target", "Target is required.");

        if (sameUser(actor, target)) {
            return DeleteUserPolicy.denied("You may not delete your own account.");
        }

        if (actor.isAdmin()) {
            return DeleteUserPolicy.allowed();
        }

        if (actor.isManager()) {
            if (!canManageUser(actor, target)) {
                return DeleteUserPolicy.denied(
                        "Managers can only delete basic users in their own department."
                );
            }
            return DeleteUserPolicy.allowed();
        }

        return DeleteUserPolicy.denied(
                "You do not have permission to delete users."
        );
    }

    public void validateDeletionRequest(User actor, User target) {
        DeleteUserPolicy policy = getDeletePolicy(actor, target);

        requirePermission(
                policy.canDelete(),
                policy.reason()
        );

        if (wouldLeaveSystemWithoutActiveAdmin(target)) {
            throw new LastActiveAdminDeletionException();
        }
    }

    /**
     * Computes deletion capabilities for the actor relative to the target user.
     *
     * @param actor  the acting user
     * @param target the user being evaluated
     * @return the deletion capabilities, including denial reason if applicable
     */
    public DeleteUserCapabilities getDeleteCapabilities(User actor, User target) {
        DeleteUserPolicy policy = getDeletePolicy(actor, target);

        if (!policy.canDelete()) {
            return DeleteUserCapabilities.none(policy.reason());
        }

        if (wouldLeaveSystemWithoutActiveAdmin(target)) {
            return DeleteUserCapabilities.none(
                    "You may not delete the last active administrator."
            );
        }

        return new DeleteUserCapabilities(
                true,
                null // reason
        );
    }


    public boolean canUpdate(User actor, User target) {
        return getUpdatePolicy(actor, target).canUpdate();
    }
    public void validateUpdateRequest(
            User actor,
            User target,
            UpdateUserRequest request,
            Department requestedDepartment
    ) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");
        Objects.requireNonNull(request, "Request is required.");

        UpdateUserPolicy policy = getUpdatePolicy(actor, target);

        requirePermission(
                policy.canUpdate(),
                policy.reason()
        );

        validateProfileChanges(policy, request, target);
        validateJobTitleChange(policy, request, target);
        validateRoleChange(policy, request, target);
        validateDepartmentChange(actor, policy, request, target, requestedDepartment);
        validateActiveChange(policy, request, target);
    }

    private void validateProfileChanges(
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            User target
    ) {
        boolean profileChangeRequested =
                request.fullName().isPresent()
                        || request.email().isPresent()
                        || request.phone().isPresent();

        if (!profileChangeRequested) {
            return;
        }

        requirePermission(
                policy.canEditProfile(),
                "You do not have permission to update this user's profile."
        );
    }

    private void validateJobTitleChange(
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            User target
    ) {
        if (!request.jobTitle().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditJobTitle(),
                "You do not have permission to update this user's job title."
        );
    }

    private void validateRoleChange(
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            User target
    ) {
        if (!request.role().isPresent()) {
            return;
        }

        Role requestedRole = request.role().orElse(null);

        requirePermission(
                policy.canEditRole(),
                "You do not have permission to change this user's role."
        );

        requirePermission(
                requestedRole != null,
                "role cannot be null."
        );

        requirePermission(
                policy.assignableRoles().contains(requestedRole),
                "You do not have permission to assign this role."
        );
    }

    private void validateDepartmentChange(
            User actor,
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            User target,
            Department requestedDepartment
    ) {
        if (!request.departmentId().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditDepartment(),
                "You do not have permission to change this user's department."
        );

        requirePermission(
                requestedDepartment != null,
                "departmentId is invalid."
        );

        requirePermission(
                requestedDepartment.isActive(),
                "You may only assign users to an active department."
        );

        requirePermission(
                canAssignDepartmentForUpdate(actor, target, requestedDepartment),
                "You do not have permission to assign this department."
        );
    }

    private boolean canAssignDepartmentForUpdate(
            User actor,
            User target,
            Department requestedDepartment
    ) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");
        Objects.requireNonNull(requestedDepartment, "Requested department is required.");

        // Admins can assign to any department (active check handled elsewhere)
        if (actor.isAdmin()) {
            return true;
        }

        // Managers can only assign basic users within their own department
        if (actor.isManager() && canManageBasicUserInOwnDepartment(actor, target)) {
            Department actorDepartment = actor.getDepartment();

            return actorDepartment != null
                    && actorDepartment.isActive()
                    && actorDepartment.getId().equals(requestedDepartment.getId());
        }

        return false;
    }

    private void validateActiveChange(
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            User target
    ) {
        if (!request.active().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditActive(),
                "You do not have permission to change this user's active status."
        );

        requirePermission(
                request.active().orElse(null) != null,
                "active cannot be null."
        );
    }

    public UpdateUserCapabilities getUpdateCapabilities(User actor, User target) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");

        UpdateUserPolicy policy = getUpdatePolicy(actor, target);

        if (!policy.canUpdate()) {
            return UpdateUserCapabilities.none(policy.reason());
        }

        List<DepartmentOption> departmentOptions = policy.canEditDepartment()
                ? getSelectableDepartmentOptionsForUpdate(actor, target)
                : getCurrentDepartmentOption(target.getDepartment());

        return new UpdateUserCapabilities(
                true,
                policy.canEditProfile(),
                policy.canEditJobTitle(),
                policy.canEditRole(),
                policy.canEditDepartment(),
                policy.canEditActive(),
                policy.assignableRoles(),
                departmentOptions,
                null
        );
    }

    public UpdateUserPolicy getUpdatePolicy(User actor, User target) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");

        // Cannot update yourself beyond allowed scope (handled below, but keep this first for clarity if needed)
        if (sameUser(actor, target)) {
            return UpdateUserPolicy.selfProfileOnly();
        }

        // Admins can update everything (with role restrictions handled via assignableRoles)
        if (actor.isAdmin()) {
            return UpdateUserPolicy.admin(
                    getAssignableRolesForAdmin(actor, target)
            );
        }

        // Managers can update basic users in their own department
        if (actor.isManager() && canManageBasicUserInOwnDepartment(actor, target)) {
            return UpdateUserPolicy.managerManagedUser();
        }

        // Everything else is denied
        return UpdateUserPolicy.denied(
                "You do not have permission to update this user."
        );
    }

    private Set<Role> getAssignableRolesForAdmin(User actor, User target) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");

        Set<Role> roles = EnumSet.of(Role.ADMIN, Role.MANAGER, Role.USER);

        // Prevent removing admin role from the last active admin
        if (target.isAdmin() && wouldLeaveSystemWithoutActiveAdmin(target)) {
            roles.remove(Role.ADMIN);
        }

        // Prevent admin from removing their own admin role (optional rule)
        if (sameUser(actor, target)) {
            roles.remove(Role.ADMIN);
        }

        return roles;
    }

    private List<DepartmentOption> getSelectableDepartmentOptionsForUpdate(User actor, User target) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");

        // Admins can assign to all active departments
        if (actor.isAdmin()) {
            return departmentRepository.findAll().stream()
                    .filter(Department::isActive)
                    .map(d -> new DepartmentOption(d.getId(), d.getName()))
                    .toList();
        }

        // Managers can only assign within their own active department (if allowed)
        if (actor.isManager() && canManageBasicUserInOwnDepartment(actor, target)) {
            Department dept = actor.getDepartment();

            if (dept != null && dept.isActive()) {
                return List.of(new DepartmentOption(dept.getId(), dept.getName()));
            }
        }

        return List.of();
    }

    private List<DepartmentOption> getCurrentDepartmentOption(Department department) {
        if (department == null) {
            return List.of();
        }

        return List.of(new DepartmentOption(department.getId(), department.getName()));
    }

    private boolean canManageBasicUserInOwnDepartment(User actor, User target) {
        Objects.requireNonNull(actor, "Actor is required.");
        Objects.requireNonNull(target, "Target user is required.");

        // Actor must be a manager
        if (!actor.isManager()) {
            return false;
        }

        // Target must be a basic user (not manager/admin)
        if (!target.isBasicUser()) {
            return false;
        }

        Department actorDepartment = actor.getDepartment();
        Department targetDepartment = target.getDepartment();

        // Both must have departments
        if (actorDepartment == null || targetDepartment == null) {
            return false;
        }

        // Actor’s department must be active
        if (!actorDepartment.isActive()) {
            return false;
        }

        // Must be same department
        return actorDepartment.getId().equals(targetDepartment.getId());
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

    private static void requirePresent(Object value, String field, String message) {
        if (value == null) {
            throw ValidationException.field(field, message);
        }
    }

    private void requirePermission(boolean condition, String message) {
        if (!condition) {
            throw new InsufficientPermissionsException(message);
        }
    }
}