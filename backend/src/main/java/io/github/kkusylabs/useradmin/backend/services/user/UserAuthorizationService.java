package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.DeleteUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.department.InactiveDepartmentException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.LastActiveAdminDeletionException;
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

    public UpdateUserPolicy getUpdatePolicy(User actor, User target) {

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

    public UpdateUserCapabilities getUpdateCapabilities(User actor, User target) {
        UpdateUserPolicy policy = getUpdatePolicy(actor, target);

        if (!policy.canUpdate()) {
            return UpdateUserCapabilities.none(policy.reason());
        }

        List<DepartmentOption> departmentOptions = getDepartmentOptionsForUpdate(
                actor,
                target,
                policy
        );

        Set<Role> roleOptions = getRoleOptionsForUpdate(
                actor,
                target,
                policy
        );

        return new UpdateUserCapabilities(
                true,
                policy.canEditProfile(),
                policy.canEditJobTitle(),
                policy.canEditRole(),
                policy.canEditDepartment(),
                policy.canEditActive(),
                roleOptions,
                departmentOptions,
                null
        );
    }

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

    private Set<Role> getRoleOptionsForUpdate(
            User actor,
            User target,
            UpdateUserPolicy policy
    ) {
        if (!policy.canEditRole()) {
            return getCurrentRoleOption(target.getRole());
        }

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

    private Set<Role> getAssignableRolesForUpdate(User actor, User target) {
        if (actor.isAdmin()) {
            return getAssignableRolesForAdmin(actor, target);
        }

        return EnumSet.noneOf(Role.class);
    }

    private Set<Role> getAssignableRolesForAdmin(User actor, User target) {
        if (sameUser(actor, target)) {
            return EnumSet.of(Role.ADMIN);
        }

        if (target.isAdmin() && wouldLeaveSystemWithoutActiveAdmin(target)) {
            return EnumSet.of(Role.ADMIN);
        }

        return EnumSet.allOf(Role.class);
    }

    private Set<Role> includeCurrentRoleIfMissing(Set<Role> options, Role currentRole) {
        if (currentRole == null || options.contains(currentRole)) {
            return options;
        }

        Set<Role> result = EnumSet.copyOf(options);
        result.add(currentRole);
        return result;
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

    private void requirePermission(boolean condition, String message) {
        if (!condition) {
            throw new InsufficientPermissionsException(message);
        }
    }
}