package io.github.kkusylabs.useradmin.backend.services;

import io.github.kkusylabs.useradmin.backend.dtos.CreateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.UserResponse;
import io.github.kkusylabs.useradmin.backend.exceptions.DepartmentNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.UserNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.UsernameAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.mappers.UserMapper;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import io.github.kkusylabs.useradmin.backend.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for managing {@link User} entities.
 *
 * <p>
 * Coordinates user operations such as creation, retrieval, update, and deletion.
 * This service is responsible for:
 * </p>
 * <ul>
 *     <li>Loading and validating the acting user</li>
 *     <li>Resolving referenced entities (e.g., {@link Department})</li>
 *     <li>Delegating authorization checks to {@link UserAuthorizationService}</li>
 *     <li>Mapping between DTOs and domain entities</li>
 * </ul>
 *
 * <p>
 * Authorization rules are defined in {@link UserAuthorizationService} to keep
 * this class focused on application flow.
 * </p>
 *
 * @author kkusy
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserMapper userMapper;
    private final UserAuthorizationService userAuthorizationService;

    /**
     * Creates a new service instance.
     *
     * @param userRepository       the repository used to manage users
     * @param departmentRepository the repository used to manage departments
     */
    public UserService(UserRepository userRepository,
                       DepartmentRepository departmentRepository,
                       UserMapper userMapper,
                       UserAuthorizationService userAuthorizationService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.userMapper = userMapper;
        this.userAuthorizationService = userAuthorizationService;
    }

    /**
     * Creates a new user.
     *
     * <p>Flow:</p>
     * <ul>
     *     <li>Ensures the acting user is active</li>
     *     <li>Normalizes and validates username uniqueness</li>
     *     <li>Resolves the requested department</li>
     *     <li>Applies default role ({@link Role#USER}) if not specified</li>
     *     <li>Delegates authorization checks</li>
     *     <li>Maps and persists the user</li>
     * </ul>
     *
     * @param request the user creation request
     * @param actorId the identifier of the authenticated user
     * @return the created user
     * @throws UserNotFoundException            if the actor cannot be found
     * @throws UsernameAlreadyExistsException   if the username is already in use
     * @throws DepartmentNotFoundException      if the requested department does not exist
     * @throws InsufficientPermissionsException if the actor is not permitted to create the user
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request, Long actorId) {
        User actor = getActiveActor(actorId);

        String normalizedUsername = StringUtils.normalizeUsername(request.username());

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }

        Department department = resolveRequestedDepartment(request.departmentId());
        Role requestedRole = request.role() != null ? request.role() : Role.USER;

        userAuthorizationService.validateCreation(actor, requestedRole, department);

        User user = userMapper.toEntity(request, department, requestedRole);
        return userMapper.toResponse(userRepository.save(user), department);
    }

    /**
     * Retrieves the acting user and ensures the account is active.
     *
     * @param actorId the identifier of the authenticated user
     * @return the active user
     * @throws UserNotFoundException            if no user exists with the given identifier
     * @throws InsufficientPermissionsException if the user is inactive
     */
    private User getActiveActor(Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));

        if (!actor.isActive()) {
            throw new InsufficientPermissionsException("Inactive users may not perform this operation.");
        }

        return actor;
    }

    /**
     * Retrieves a paginated list of users.
     *
     * <p>Requires the acting user to be active.</p>
     *
     * @param pageable pagination and sorting information
     * @param actorId  the identifier of the authenticated user
     * @return a page of user response DTOs
     * @throws UserNotFoundException            if the actor cannot be found
     * @throws InsufficientPermissionsException if the actor is inactive
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable, Long actorId) {
        getActiveActor(actorId);

        return userRepository.findAll(pageable)
                .map(user -> userMapper.toResponse(user, user.getDepartment()));
    }

    /**
     * Retrieves a user by identifier.
     *
     * <p>Requires the acting user to be active.</p>
     *
     * @param id      the identifier of the user
     * @param actorId the identifier of the authenticated user
     * @return the user response DTO
     * @throws UserNotFoundException            if the actor or target user cannot be found
     * @throws InsufficientPermissionsException if the actor is inactive
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id, Long actorId) {
        getActiveActor(actorId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponse(user, user.getDepartment());
    }

    /**
     * Deletes a user.
     *
     * <p>
     * Authorization is enforced by {@link UserAuthorizationService}.
     * </p>
     *
     * @param targetUserId the identifier of the user to delete
     * @param actorId      the identifier of the authenticated user
     * @throws UserNotFoundException            if the actor or target user cannot be found
     * @throws InsufficientPermissionsException if the actor is not permitted to delete the target user
     */
    @Transactional
    public void deleteById(Long targetUserId, Long actorId) {
        User actor = getActiveActor(actorId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        userAuthorizationService.validateDeletion(actor, target);
        userRepository.delete(target);
    }

    /**
     * Updates an existing user.
     *
     * <p>
     * Applies requested changes after validating permissions through
     * {@link UserAuthorizationService}.
     * </p>
     *
     * @param targetUserId the identifier of the user to update
     * @param request      the update request
     * @param actorId      the identifier of the authenticated user
     * @return the updated user response DTO
     * @throws UserNotFoundException            if the actor or target user cannot be found
     * @throws DepartmentNotFoundException      if the requested department does not exist
     * @throws InsufficientPermissionsException if the actor is not permitted to perform the update
     */
    @Transactional
    public UserResponse updateById(Long targetUserId, UpdateUserRequest request, Long actorId) {
        User actor = getActiveActor(actorId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        Department requestedDepartment = resolveRequestedDepartment(request.departmentId());
        userAuthorizationService.validateUpdate(actor, target, request, requestedDepartment);
        userMapper.updateEntity(target, request, requestedDepartment);
        User savedUser = userRepository.save(target);
        return userMapper.toResponse(savedUser, savedUser.getDepartment());
    }

    /**
     * Resolves a department identifier to a {@link Department}.
     *
     * @param departmentId the department identifier, or {@code null}
     * @return the resolved department, or {@code null} if no department was requested
     * @throws DepartmentNotFoundException if the identifier does not correspond to an existing department
     */
    private Department resolveRequestedDepartment(Long departmentId) {
        if (departmentId == null) {
            return null;
        }

        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }
}