package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.department.InactiveDepartmentException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.LastActiveAdminDeletionException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.LastActiveAdminUpdateException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Service responsible for enforcing authorization rules for {@link User} operations.
 *
 * <p>Centralizes permission checks and capability calculations for creating,
 * updating, and deleting users. Rules are evaluated based on the authenticated
 * actor, the target user, and business constraints such as role and department.</p>
 */
@Component
public class UserAuthorizationService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public UserAuthorizationService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository
    ) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * Checks whether the actor may create users.
     *
     * @param actor authenticated actor
     * @return {@code true} if creation is allowed
     */
    public boolean canCreate(User actor) {
        return getCreatePolicy(actor).canCreate();
    }

    /**
     * Resolves the create policy for the actor.
     *
     * <p>Admins may create users. Managers may create users only when they belong
     * to an active department. Basic users may not create users.</p>
     *
     * @param actor authenticated actor
     * @return the resolved create policy
     */
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

    /**
     * Validates a create-user command against actor, role, and department rules.
     *
     * @param actor authenticated actor
     * @param role requested role for the new user
     * @param department requested department for the new user
     * @throws InsufficientPermissionsException if creation or assignment is not allowed
     * @throws InactiveDepartmentException if the requested department is inactive
     */
    public void validateCreateRequest(User actor, Role role, Department department) {
        CreateUserPolicy policy = getCreatePolicy(actor);
        requirePermission(policy.canCreate(), policy.reason());

        requirePermission(
                canAssignRoleForCreate(actor, role),
                "You do not have permission to assign this role."
        );

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

    /**
     * Determines whether the actor may assign the requested department during creation.
     *
     * <p>Admins may assign any active department. Managers may assign only their own
     * department.</p>
     *
     * @param actor authenticated actor
     * @param department requested department
     * @return {@code true} if the department may be assigned
     */
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

    /**
     * Builds the actor's create-user capabilities.
     *
     * <p>The result is intended for query/UI use and describes whether creation is
     * available, which roles and departments may be assigned, and why creation is
     * unavailable if denied.</p>
     *
     * @param actor authenticated actor
     * @return create capabilities for the actor
     */
    public CreateUserCapabilities getCreateCapabilities(User actor) {
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

    /**
     * Returns the roles the actor may assign during user creation.
     *
     * @param actor authenticated actor
     * @return assignable roles
     */
    private Set<Role> getAssignableRolesForCreate(User actor) {
        if (actor.isAdmin()) {
            return EnumSet.allOf(Role.class);
        }

        if (actor.isManager()) {
            return EnumSet.of(Role.USER);
        }

        return EnumSet.noneOf(Role.class);
    }

    /**
     * Returns the departments the actor may assign during user creation.
     *
     * @param actor authenticated actor
     * @return assignable departments
     */
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

    /**
     * Checks whether the actor may delete the target user.
     *
     * @param actor authenticated actor
     * @param target user being deleted
     * @return {@code true} if deletion is allowed
     */
    public boolean canDelete(User actor, User target) {
        return getDeletePolicy(actor, target).canDelete();
    }

    /**
     * Resolves the delete policy for the actor and target user.
     *
     * <p>Users may not delete themselves. Admins may delete other users. Managers
     * may delete only basic users in their own department.</p>
     *
     * @param actor authenticated actor
     * @param target user being deleted
     * @return the resolved delete policy
     */
    private DeleteUserPolicy getDeletePolicy(User actor, User target) {
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

    /**
     * Validates a delete-user command.
     *
     * @param actor authenticated actor
     * @param target user being deleted
     * @throws InsufficientPermissionsException if deletion is not allowed
     * @throws LastActiveAdminDeletionException if deletion would remove the last active admin
     */
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
     * Checks whether the actor may update the target user.
     *
     * @param actor authenticated actor
     * @param target user being updated
     * @return {@code true} if updating is allowed
     */
    public boolean canUpdate(User actor, User target) {
        return getUpdatePolicy(actor, target).canUpdate();
    }

    /**
     * Resolves the update policy for the actor and target user.
     *
     * <p>Admins may update other users. Managers may update basic users in their own
     * department. Users may update themselves within a limited profile-only scope.</p>
     *
     * @param actor authenticated actor
     * @param target user being updated
     * @return the resolved update policy
     */
    private UpdateUserPolicy getUpdatePolicy(User actor, User target) {

        // Cannot update yourself beyond allowed scope (handled below, but keep this first for clarity if needed)
        if (sameUser(actor, target)) {
            return actor.isAdmin()
                    ? UpdateUserPolicy.adminSelf()
                    : UpdateUserPolicy.selfProfileOnly();
        }

        // Admins can update everything (with role restrictions handled via roleOptions)
        if (actor.isAdmin()) {
            return UpdateUserPolicy.admin();
        }

        // Managers can update basic users in their own department
        if (actor.isManager() && canManageUser(actor, target)) {
            return UpdateUserPolicy.managerManagedUser();
        }

        // Everything else is denied
        return UpdateUserPolicy.denied(
                "You do not have permission to update this user."
        );
    }

    /**
     * Validates a partial update command against the actor's update policy.
     *
     * @param actor authenticated actor
     * @param target user being updated
     * @param request requested field changes
     * @param department resolved requested department, or {@code null} if unchanged
     * @throws InsufficientPermissionsException if any requested change is not allowed
     * @throws InactiveDepartmentException if the requested department is inactive
     * @throws LastActiveAdminUpdateException if the update would remove the last active admin
     */
    public void validateUpdateRequest(
            User actor,
            User target,
            UpdateUserRequest request,
            Department department
    ) {
        UpdateUserPolicy policy = getUpdatePolicy(actor, target);

        requirePermission(
                policy.canUpdate(),
                policy.reason()
        );

        validateProfileChanges(policy, request);
        validateJobTitleChange(policy, request);
        validateRoleChange(policy, request, actor, target);
        validateDepartmentChange(policy, request, department);
        validateActiveChange(policy, request);
        validateLastActiveAdminInvariant(request, target);
    }

    private void validateProfileChanges(
            UpdateUserPolicy policy,
            UpdateUserRequest request
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
            UpdateUserRequest request
    ) {
        if (!request.jobTitle().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditJobTitle(),
                "You do not have permission to update this user's job title."
        );
    }

    /**
     * Validates requested role changes, including whether the actor may assign the
     * requested role.
     */
    private void validateRoleChange(
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            User actor,
            User target
    ) {
        if (!request.role().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditRole(),
                "You do not have permission to change this user's role."
        );

        requirePermission(
                canAssignRoleForUpdate(actor, target, request.role().get()),
                "You do not have permission to assign this role."
        );
    }

    private boolean canAssignRoleForUpdate(User actor, User target, Role role) {
        Set<Role> assignableRoles = getAssignableRolesForUpdate(actor, target);
        return assignableRoles.contains(role);
    }

    /**
     * Validates requested department changes.
     *
     * <p>When a department change is requested, the actor must be allowed to edit
     * departments and the requested department must be active.</p>
     */
    private void validateDepartmentChange(
            UpdateUserPolicy policy,
            UpdateUserRequest request,
            Department department
    ) {
        if (!request.departmentId().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditDepartment(),
                "You do not have permission to change this user's department."
        );

        if (!department.isActive()) {
            throw new InactiveDepartmentException(department.getId());
        }
    }

    private void validateActiveChange(
            UpdateUserPolicy policy,
            UpdateUserRequest request
    ) {
        if (!request.active().isPresent()) {
            return;
        }

        requirePermission(
                policy.canEditActive(),
                "You do not have permission to change this user's active status."
        );
    }

    /**
     * Prevents updates that would leave the system without an active administrator.
     */
    private void validateLastActiveAdminInvariant(
            UpdateUserRequest request,
            User target
    ) {
        if (!wouldLeaveSystemWithoutActiveAdmin(target)) {
            return;
        }

        Role resultingRole = request.role().isPresent()
                ? request.role().get()
                : target.getRole();

        boolean resultingActive = request.active().isPresent()
                ? request.active().get()
                : target.isActive();

        boolean remainsActiveAdmin =
                resultingRole == Role.ADMIN && resultingActive;

        if (!remainsActiveAdmin) {
            throw new LastActiveAdminUpdateException();
        }
    }

    /**
     * Builds the actor's update capabilities for the target user.
     *
     * <p>The result is intended for query/UI use and includes editable fields,
     * selectable roles, selectable departments, and an optional denial reason.</p>
     *
     * @param actor authenticated actor
     * @param target user being edited
     * @return update capabilities for the actor and target
     */
    public UpdateUserCapabilities getUpdateCapabilities(User actor, User target) {
        UpdateUserPolicy policy = getUpdatePolicy(actor, target);

        if (!policy.canUpdate()) {
            return UpdateUserCapabilities.none(policy.reason());
        }

        boolean isLastActiveAdmin = wouldLeaveSystemWithoutActiveAdmin(target);

        boolean canEditRole = policy.canEditRole() && !isLastActiveAdmin;
        boolean canEditActive = policy.canEditActive() && !isLastActiveAdmin;

        Set<Role> roleOptions = canEditRole
                ? getRoleOptionsForUpdate(actor, target)
                : getCurrentRoleOption(target.getRole());

        List<DepartmentOption> departmentOptions = getDepartmentOptionsForUpdate(
                actor,
                target,
                policy
        );

        return new UpdateUserCapabilities(
                true,
                policy.canEditProfile(),
                policy.canEditJobTitle(),
                canEditRole,
                policy.canEditDepartment(),
                canEditActive,
                roleOptions,
                departmentOptions,
                null
        );
    }

    /**
     * Returns department options for an update form.
     *
     * <p>If department editing is not allowed, only the current department is
     * returned so clients can display it without allowing changes.</p>
     */
    private List<DepartmentOption> getDepartmentOptionsForUpdate(
            User actor,
            User target,
            UpdateUserPolicy policy
    ) {
        if (!policy.canEditDepartment()) {
            return getCurrentDepartmentOption(target.getDepartment());
        }

        return includeCurrentDepartmentIfMissing(
                getAssignableDepartmentOptionsForUpdate(actor, target),
                target.getDepartment()
        );
    }

    /**
     * Returns departments the actor may assign during update.
     */
    private List<DepartmentOption> getAssignableDepartmentOptionsForUpdate(
            User actor,
            User target
    ) {
        if (actor.isAdmin()) {
            return departmentRepository.findActiveOrderByNameIgnoreCase().stream()
                    .map(d -> new DepartmentOption(d.getId(), d.getName()))
                    .toList();
        }

        if (actor.isManager() && canManageUser(actor, target)) {
            Department department = actor.getDepartment();

            if (department != null && department.isActive()) {
                return List.of(new DepartmentOption(department.getId(), department.getName()));
            }
        }

        return List.of();
    }

    /**
     * Adds the current department to the option list when it is not already present.
     *
     * <p>This allows clients to display the current value even if it is no longer
     * otherwise selectable.</p>
     */
    private List<DepartmentOption> includeCurrentDepartmentIfMissing(
            List<DepartmentOption> options,
            Department currentDepartment
    ) {
        if (currentDepartment == null) {
            return options;
        }

        boolean alreadyIncluded = options.stream()
                .anyMatch(option -> Objects.equals(option.id(), currentDepartment.getId()));

        if (alreadyIncluded) {
            return options;
        }

        List<DepartmentOption> result = new ArrayList<>(options);
        result.add(new DepartmentOption(currentDepartment.getId(), currentDepartment.getName()));
        return result;
    }

    private List<DepartmentOption> getCurrentDepartmentOption(Department department) {
        if (department == null) {
            return List.of();
        }

        return List.of(new DepartmentOption(department.getId(), department.getName()));
    }

    private Set<Role> getRoleOptionsForUpdate(User actor, User target) {
        return includeCurrentRoleIfMissing(
                getAssignableRolesForUpdate(actor, target),
                target.getRole()
        );
    }

    private Set<Role> getCurrentRoleOption(Role role) {
        if (role == null) {
            return EnumSet.noneOf(Role.class);
        }
        return EnumSet.of(role);
    }

    /**
     * Returns roles the actor may assign during update.
     */
    private Set<Role> getAssignableRolesForUpdate(User actor, User target) {
        if (!actor.isAdmin()) {
            return EnumSet.noneOf(Role.class);
        }

        if (sameUser(actor, target)) {
            return EnumSet.noneOf(Role.class);
        }

        return EnumSet.allOf(Role.class);
    }

    /**
     * Adds the current role to the option set when it is not already present.
     *
     * <p>This allows clients to display the current value even if it is no longer
     * otherwise assignable.</p>
     */
    private Set<Role> includeCurrentRoleIfMissing(Set<Role> options, Role currentRole) {
        if (currentRole == null || options.contains(currentRole)) {
            return options;
        }

        Set<Role> result = EnumSet.noneOf(Role.class);
        result.addAll(options);
        result.add(currentRole);
        return result;
    }

    /**
     * Checks whether two users represent the same persisted user.
     */
    private static boolean sameUser(User a, User b) {
        return a != null
                && b != null
                && Objects.equals(a.getId(), b.getId());
    }

    /**
     * Checks whether two departments represent the same persisted department.
     */
    private static boolean sameDepartment(Department a, Department b) {
        return a != null
                && b != null
                && Objects.equals(a.getId(), b.getId());
    }

    /**
     * Checks whether a manager may manage the target user.
     *
     * <p>Managers may manage only basic users in their own department.</p>
     */
    private static boolean canManageUser(User actor, User target) {
        return actor != null
                && target != null
                && actor.isManager()
                && target.isBasicUser()
                && sameDepartment(actor.getDepartment(), target.getDepartment());
    }

    /**
     * Determines whether changing or deleting the given user would leave the system
     * without any active administrators.
     *
     * @param user user being evaluated
     * @return {@code true} if the user is the last active admin
     */
    private boolean wouldLeaveSystemWithoutActiveAdmin(User user) {
        return user.isAdmin()
                && user.isActive()
                && userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1;
    }

    private void requirePermission(boolean condition, String message) {
        if (!condition) {
            throw new InsufficientPermissionsException(message);
        }
    }
}