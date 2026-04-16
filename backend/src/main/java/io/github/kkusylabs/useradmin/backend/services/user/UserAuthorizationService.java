package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Service responsible for enforcing authorization rules related to {@link User} management.
 * <p>
 * This component centralizes permission checks for user creation, deletion, and updates.
 * It evaluates whether an acting user ("actor") is allowed to perform an operation on
 * a target user or resource, based on role, department, and business constraints.
 * </p>
 *
 * <p>Rules are expressed in terms of business intent rather than low-level conditions,
 * keeping authorization logic isolated from application flow.</p>
 *
 * @author kkusy
 */
@Component
public class UserAuthorizationService {

    private final UserRepository userRepository;

    /**
     * Creates a new authorization service.
     *
     * @param userRepository repository used for queries required by authorization rules
     */
    public UserAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Validates whether the actor is permitted to create a user.
     *
     * <p>Rules:</p>
     * <ul>
     *     <li>Administrators may create users with any role in any department.</li>
     *     <li>Managers may create only users with role {@link Role#USER}
     *         and only within their own department.</li>
     *     <li>All other roles are not permitted to create users.</li>
     * </ul>
     *
     * @param actor               the authenticated user performing the operation
     * @param requestedRole       the role requested for the new user
     * @param requestedDepartment the department for the new user
     * @throws InsufficientPermissionsException if the operation is not permitted
     */
    public void validateCreation(User actor, Role requestedRole, Department requestedDepartment) {
        if (actor.isAdmin()) {
            return;
        }

        if (actor.isManager()) {
            validateManagerCreationRules(actor, requestedRole, requestedDepartment);
            return;
        }

        throw new InsufficientPermissionsException("You do not have permission to create users.");
    }

    /**
     * Applies manager-specific constraints for user creation.
     */
    private void validateManagerCreationRules(User actor, Role requestedRole, Department requestedDepartment) {
        if (requestedRole != Role.USER) {
            throw new InsufficientPermissionsException("Managers may only create users with the role 'USER'.");
        }

        if (isDifferentDepartment(actor.getDepartment(), requestedDepartment)) {
            throw new InsufficientPermissionsException("Managers may only create users within their own department.");
        }
    }

    /**
     * Validates whether the actor is permitted to delete a user.
     *
     * <p>Rules:</p>
     * <ul>
     *     <li>No user may delete their own account.</li>
     *     <li>Administrators may delete any user except when it would remove
     *         the last active administrator.</li>
     *     <li>Managers may delete only basic users in their own department.</li>
     * </ul>
     *
     * @param actor the authenticated user performing the operation
     * @param target the user to be deleted
     * @throws InsufficientPermissionsException if the operation is not permitted
     */
    public void validateDeletion(User actor, User target) {

        if (isSameUser(actor, target)) {
            throw new InsufficientPermissionsException("You may not delete your own account.");
        }

        if (actor.isAdmin()) {
            validateAdminDeletionRules(target);
            return;
        }

        if (actor.isManager()) {
            validateManagerDeletionRules(actor, target);
            return;
        }

        throw new InsufficientPermissionsException("You are not allowed to delete this user.");
    }

    /**
     * Applies administrator-specific constraints for user deletion.
     */
    private void validateAdminDeletionRules(User target) {
        if (wouldLeaveSystemWithoutActiveAdmin(target)) {
            throw new InsufficientPermissionsException(
                    "You may not remove the last active admin."
            );
        }
    }

    /**
     * Applies manager-specific constraints for user deletion.
     */
    private void validateManagerDeletionRules(User actor, User target) {
        if (!target.isBasicUser() || isDifferentDepartment(actor, target)) {
            throw new InsufficientPermissionsException(
                    "Managers can only delete basic users in their own department."
            );
        }
    }

    /**
     * Determines whether two users refer to the same persisted user.
     *
     * @return {@code true} if both users have the same identifier; {@code false} otherwise
     */
    private boolean isSameUser(User a, User b) {
        return Objects.equals(a != null ? a.getId() : null,
                b != null ? b.getId() : null);
    }

    /**
     * Determines whether removing the given user would leave the system without
     * any active administrators.
     *
     * @param user the user being removed or deactivated
     * @return {@code true} if this would remove the last active administrator; {@code false} otherwise
     */
    private boolean wouldLeaveSystemWithoutActiveAdmin(User user) {
        return user.isAdmin()
                && user.isActive()
                && userRepository.countByRoleAndActiveTrue(Role.ADMIN) <= 1;
    }

    /**
     * Determines whether two departments differ.
     *
     * @return {@code true} if departments are not equal or either is {@code null}
     */
    private boolean isDifferentDepartment(Department left, Department right) {
        return left == null
                || right == null
                || !Objects.equals(left.getId(), right.getId());
    }

    /**
     * Determines whether two users belong to different departments.
     */
    private boolean isDifferentDepartment(User a, User b) {
        return isDifferentDepartment(
                a != null ? a.getDepartment() : null,
                b != null ? b.getDepartment() : null
        );
    }

    /**
     * Validates whether the actor is permitted to update a user.
     *
     * <p>Rules vary by role:</p>
     * <ul>
     *     <li>Basic users may update only their own profile and may not change role,
     *         department, or active status.</li>
     *     <li>Managers may update their own profile and basic users in their department,
     *         but may not change role or department.</li>
     *     <li>Administrators may update any user but may not change their own role
     *         or deactivate themselves or the last active administrator.</li>
     * </ul>
     *
     * @param actor               the authenticated user performing the operation
     * @param target              the user being updated
     * @param request             the update request
     * @param requestedDepartment the requested department, if any
     * @throws InsufficientPermissionsException if the operation is not permitted
     */
    public void validateUpdate(User actor,
                               User target,
                               UpdateUserRequest request,
                               Department requestedDepartment) {

        UpdateIntent intent = buildUpdateIntent(actor, target, request, requestedDepartment);

        if (actor.isAdmin()) {
            validateAdminUpdateRules(target, intent);
            return;
        }

        if (actor.isManager()) {
            validateManagerUpdateRules(actor, target, intent);
            return;
        }

        if (actor.isBasicUser()) {
            validateBasicUserUpdateRules(intent);
            return;
        }

        throw new InsufficientPermissionsException("You do not have permission to update users.");
    }

    /**
     * Applies user-specific constraints for user update.
     */
    private void validateBasicUserUpdateRules(UpdateIntent intent) {
        if (!intent.self()) {
            throw new InsufficientPermissionsException("Users can only update their own profile.");
        }

        if (intent.roleChangeRequested()
                || intent.departmentChangeRequested()
                || intent.activeChangeRequested()) {
            throw new InsufficientPermissionsException(
                    "Users cannot change their own role, department, or status."
            );
        }
    }

    /**
     * Applies manager-specific constraints for user update.
     */
    private void validateManagerUpdateRules(User actor, User target, UpdateIntent intent) {

        if (intent.self()) {
            if (intent.roleChangeRequested() || intent.departmentChangeRequested()) {
                throw new InsufficientPermissionsException(
                        "Managers cannot change their own role or department."
                );
            }
            if (intent.activeChangeRequested()) {
                throw new InsufficientPermissionsException(
                        "Managers cannot change their own active status."
                );
            }
            return;
        }

        if (!target.isBasicUser() || isDifferentDepartment(actor, target)) {
            throw new InsufficientPermissionsException(
                    "Managers can only update basic users in their own department."
            );
        }

        if (intent.roleChangeRequested() || intent.departmentChangeRequested()) {
            throw new InsufficientPermissionsException(
                    "Managers cannot change the role or department of other users."
            );
        }
    }

    /**
     * Applies admin-specific constraints for user update.
     */
    private void validateAdminUpdateRules(User target, UpdateIntent intent) {
        if (intent.self() && intent.roleChangeRequested()) {
            throw new InsufficientPermissionsException(
                    "Administrators cannot change their own role."
            );
        }

        if (intent.deactivationRequested()) {
            if (intent.self()) {
                throw new InsufficientPermissionsException(
                        "You cannot deactivate your own admin account."
                );
            }
            if (wouldLeaveSystemWithoutActiveAdmin(target)) {
                throw new InsufficientPermissionsException(
                        "Cannot deactivate the last active administrator."
                );
            }
        }
    }

    /**
     * Builds an {@link UpdateIntent} describing the effective changes requested.
     */
    private UpdateIntent buildUpdateIntent(User actor,
                                           User target,
                                           UpdateUserRequest request,
                                           Department requestedDepartment) {

        boolean self = isSameUser(actor, target);

        boolean roleChangeRequested =
                request.role() != null && request.role() != target.getRole();

        boolean departmentChangeRequested = requestedDepartment != null
                && (target.getDepartment() == null
                || !Objects.equals(requestedDepartment.getId(), target.getDepartment().getId()));

        boolean activeChangeRequested =
                request.active() != null && request.active() != target.isActive();

        boolean deactivationRequested =
                request.active() != null && !request.active() && target.isActive();

        return new UpdateIntent(
                self,
                roleChangeRequested,
                departmentChangeRequested,
                activeChangeRequested,
                deactivationRequested
        );
    }

    /**
     * Describes the effective intent of an update request.
     *
     * @param self                      whether the actor is updating their own account
     * @param roleChangeRequested       whether a role change was requested
     * @param departmentChangeRequested whether a department change was requested
     * @param activeChangeRequested     whether an active status change was requested
     * @param deactivationRequested     whether a deactivation was requested
     */
    private record UpdateIntent(
            boolean self,
            boolean roleChangeRequested,
            boolean departmentChangeRequested,
            boolean activeChangeRequested,
            boolean deactivationRequested
    ) {
    }
}