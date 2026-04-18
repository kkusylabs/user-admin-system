package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.common.AuthenticatedActor;
import io.github.kkusylabs.useradmin.backend.dtos.common.PagedResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentSummary;
import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.user.UserListResponse;
import io.github.kkusylabs.useradmin.backend.dtos.user.UserResponse;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.EmailAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.UserNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.UsernameAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

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
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userMapper = userMapper;
        this.userAuthorizationService = userAuthorizationService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user.
     *
     * <p>Validates uniqueness, resolves the target department, checks permissions,
     * and persists the user with an encoded password.</p>
     *
     * @param request the user creation request
     * @param actor   the authenticated actor performing the operation
     * @return the created user
     * @throws UserNotFoundException            if the actor does not exist
     * @throws UsernameAlreadyExistsException   if the username is already in use
     * @throws EmailAlreadyExistsException      if the email is already in use
     * @throws DepartmentNotFoundException      if the department does not exist
     * @throws InsufficientPermissionsException if the actor is not allowed to create the user
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, AuthenticatedActor actor) {
        User actorUser = getActorUser(actor);

        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        Department department = getDepartment(request.departmentId());
        userAuthorizationService.validateCreation(actorUser, request.role(), department);
        User newUser = userMapper.fromCreateRequest(request, department);
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(newUser);
        return toUserResponse(actorUser, savedUser);
    }

    /**
     * Retrieves a paginated list of users.
     *
     * <p>Each result includes authorization capabilities relative to the actor.</p>
     *
     * @param pageable pagination and sorting information
     * @param actor    the authenticated actor performing the request
     * @return a paged response of users
     */
    @Transactional(readOnly = true)
    public UserListResponse findAll(Pageable pageable, AuthenticatedActor actor) {
        User actorUser = getActorUser(actor);
        Page<UserResponse> page = userRepository.findAll(pageable)
                .map(targetUser -> toUserResponse(actorUser, targetUser));

        return new UserListResponse(
                PagedResponse.from(page),
                userAuthorizationService.getCreateCapabilities(actorUser,
                        getAllDepartmentOptions()));
    }

    /**
     * Retrieves a user by ID.
     *
     * <p>Includes authorization capabilities relative to the actor.</p>
     *
     * @param targetUserId the user identifier
     * @param actor        the authenticated actor performing the request
     * @return the user
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long targetUserId, AuthenticatedActor actor) {
        User actorUser = getActorUser(actor);
        User targetUser = getTargetUser(targetUserId);
        return toUserResponse(actorUser, targetUser);
    }

    /**
     * Deletes a user.
     *
     * <p>Validates that the actor has permission before deletion.</p>
     *
     * @param targetUserId the user identifier
     * @param actor        the authenticated actor performing the operation
     * @throws UserNotFoundException            if the actor or target user does not exist
     * @throws InsufficientPermissionsException if the actor is not allowed to delete the user
     */
    @Transactional
    public void deleteById(Long targetUserId, AuthenticatedActor actor) {
        User actorUser = getActorUser(actor);
        User targetUser = getTargetUser(targetUserId);
        userAuthorizationService.validateDeletion(actorUser, targetUser);
        userRepository.delete(targetUser);
    }

    /**
     * Resolves the authenticated actor to a {@link User}.
     *
     * @param actor the authenticated actor
     * @return the corresponding user
     * @throws UserNotFoundException if the actor does not exist
     */
    private User getActorUser(AuthenticatedActor actor) {
        return userRepository.findById(actor.actorId())
                .orElseThrow(() -> new UserNotFoundException(actor.actorId()));
    }

    /**
     * Loads a user by ID.
     *
     * @param targetUserId the user identifier
     * @return the user
     * @throws UserNotFoundException if the user does not exist
     */
    private User getTargetUser(Long targetUserId) {
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
    private Department getDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
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
     * Builds a {@link UserResponse} for the given target user, including
     * authorization capabilities relative to the actor.
     *
     * @param actorUser  the acting user
     * @param targetUser the user being returned
     * @return the response DTO with permission metadata
     */
    private UserResponse toUserResponse(User actorUser, User targetUser) {
        return userMapper.toResponse(
                targetUser,
                userAuthorizationService.getUpdateCapabilities(actorUser, targetUser),
                userAuthorizationService.getDeleteCapabilities(actorUser, targetUser)
        );
    }
}