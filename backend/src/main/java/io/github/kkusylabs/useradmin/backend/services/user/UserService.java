package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.common.PagedResponse;
import io.github.kkusylabs.useradmin.backend.dtos.user.*;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.EmailAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.UserNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.UsernameAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for managing {@link User} entities.
 *
 * <p>Handles user lifecycle operations (create, read, delete) by:
 * <ul>
 *   <li>Resolving the authenticated actor</li>
 *   <li>Loading referenced entities (e.g. {@link Department})</li>
 *   <li>Delegating authorization checks to {@link UserAuthorizationService}</li>
 *   <li>Mapping between domain entities and DTOs</li>
 * </ul>
 *
 * <p>Business rules and authorization decisions are delegated to
 * {@link UserAuthorizationService} to keep this class focused on orchestration.
 *
 * @author kkusy
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final UserAuthorizationService userAuthorizationService;
    private final PasswordEncoder passwordEncoder;
    private final UpdateUserValidator updateUserValidator;

    /**
     * Creates a new {@code UserService}.
     *
     * @param userRepository           repository for user persistence
     * @param departmentRepository     repository for department lookups
     * @param userMapper               maps between entities and DTOs
     * @param userAuthorizationService handles authorization rules for user operations
     * @param passwordEncoder          encodes user passwords before persistence
     */
    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       UserMapper userMapper,
                       UserAuthorizationService userAuthorizationService,
                       PasswordEncoder passwordEncoder,
                       UpdateUserValidator updateUserValidator) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userMapper = userMapper;
        this.userAuthorizationService = userAuthorizationService;
        this.passwordEncoder = passwordEncoder;
        this.updateUserValidator = updateUserValidator;
    }

    /**
     * Creates a new user.
     *
     * <p>Validates uniqueness, resolves the target department, checks permissions,
     * and persists the user with an encoded password.</p>
     *
     * @param request the user creation request

     * @return the created user
     * @throws UserNotFoundException            if the actor does not exist
     * @throws UsernameAlreadyExistsException   if the username is already in use
     * @throws EmailAlreadyExistsException      if the email is already in use
     * @throws DepartmentNotFoundException      if the department does not exist
     * @throws InsufficientPermissionsException if the actor is not allowed to create the user
     */
    @Transactional
    public UserListItemResponse createUser(CreateUserRequest request, Long actorId) {
        User actor = getRequiredActor(actorId);

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Department department = getRequiredDepartment(request.departmentId());
        userAuthorizationService.validateCreateRequest(actor, request.role(), department);
        User user = userMapper.fromCreateRequest(request, department);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        return toUserListItemResponse(user, actor);
    }

    /**
     * Retrieves a paginated list of users.
     *
     * <p>Each result includes authorization capabilities relative to the actor.</p>
     *
     * @param pageable pagination and sorting information

     * @return a paged response of users
     */
    @Transactional(readOnly = true)
    public UserListResponse getUsers(Pageable pageable, Long actorId) {
        User actor = getRequiredActor(actorId);
        Page<UserListItemResponse> page = userRepository.findAll(pageable)
                .map(targetUser -> toUserListItemResponse(targetUser, actor));

        return new UserListResponse(
                PagedResponse.from(page),
                userAuthorizationService.canCreate(actor));
    }

    /**
     * Retrieves a user by ID.
     *
     * <p>Includes authorization capabilities relative to the actor.</p>
     *
     * @param targetUserId the user identifier

     * @return the user
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public UserListItemResponse getUser(Long targetUserId, Long actorId) {
        User actor = getRequiredActor(actorId);
        User targetUser = getRequiredTargetUser(targetUserId);
        return toUserListItemResponse(targetUser, actor);
    }

    @Transactional(readOnly = true)
    public EditUserResponse getUserEditData(Long targetUserId, Long actorId) {
        User actor = getRequiredActor(actorId);
        User targetUser = getRequiredTargetUser(targetUserId);

        return userMapper.toEditResponse(
                targetUser,
                userAuthorizationService.getUpdateCapabilities(actor, targetUser));
    }

    @Transactional
    public UserListItemResponse updateUser(Long targetUserId, UpdateUserRequest request, Long actorId) {
        User actor = getRequiredActor(actorId);
        updateUserValidator.validate(request);
        User targetUser = getRequiredTargetUser(targetUserId);
        Department requestedDepartment = resolveRequestedDepartment(request.departmentId());
        userAuthorizationService.validateUpdateRequest(actor, targetUser, request, requestedDepartment);
        userMapper.updateUser(targetUser, request, requestedDepartment);
        return toUserListItemResponse(targetUser, actor);
    }

    /**
     * Deletes a user.
     *
     * <p>Validates that the actor has permission before deletion.</p>
     *
     * @param targetUserId the user identifier

     * @throws UserNotFoundException            if the actor or target user does not exist
     * @throws InsufficientPermissionsException if the actor is not allowed to delete the user
     */
    @Transactional
    public void deleteUser(Long targetUserId, Long actorId) {
        User actor = getRequiredActor(actorId);
        User targetUser = getRequiredTargetUser(targetUserId);
        userAuthorizationService.validateDeletionRequest(actor, targetUser);
        userRepository.delete(targetUser);
    }

    @Transactional(readOnly = true)
    public CreateUserCapabilities getCreateUserCapabilities(Long actorId) {
        User actor = getRequiredActor(actorId);
        return userAuthorizationService.getCreateCapabilities(actor);
    }

    @Transactional(readOnly = true)
    public DeleteUserCapabilities getDeleteUserCapabilities(Long targetUserId, Long actorId) {
        User actor = getRequiredActor(actorId);
        User targetUser = getRequiredTargetUser(targetUserId);
        return userAuthorizationService.getDeleteCapabilities(actor, targetUser);
    }

    private User getRequiredActor(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
    }

    private User getRequiredTargetUser(Long targetUserId) {
        return userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));
    }

    /**
     * Resolves a department ID to a {@link Department}.
     *
     * @param departmentId the department ID, or {@code null}
     * @return the department, or {@code null} if none was provided
     * @throws DepartmentNotFoundException if the department does not exist
     */
    private Department getRequiredDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    private UserListItemResponse toUserListItemResponse(User targetUser, User actorUser) {
        return userMapper.toListItemResponse(
                targetUser,
                userAuthorizationService.canUpdate(actorUser, targetUser),
                userAuthorizationService.canDelete(actorUser, targetUser)
        );
    }

    private Department resolveRequestedDepartment(JsonNullable<Long> departmentIdNullable) {
        Department requestedDepartment = null;

        if (departmentIdNullable.isPresent()) {
            Long departmentId = departmentIdNullable.orElse(null);

            if (departmentId != null) {
                requestedDepartment = getRequiredDepartment(departmentId);
            }
        }

        return requestedDepartment;
    }

}