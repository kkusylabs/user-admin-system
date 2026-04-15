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
     *     <li>Loads the acting user</li>
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
        User actor = getActor(actorId);
        CreateUserRequest sanitized = sanitize(request);

        if (userRepository.existsByUsername(sanitized.username())) {
            throw new UsernameAlreadyExistsException(sanitized.username());
        }

        Department requestedDepartment = resolveRequestedDepartment(sanitized.departmentId());
        userAuthorizationService.validateCreation(actor, sanitized.role(), requestedDepartment);
        User user = userMapper.toEntity(sanitized, requestedDepartment);
        return userMapper.toResponse(userRepository.save(user), requestedDepartment);
    }

    /**
     * Sanitizes a create user request by normalizing string fields and applying default values.
     *
     * @param request the original request
     * @return a sanitized request with normalized values
     */
    private CreateUserRequest sanitize(CreateUserRequest request) {
        return new CreateUserRequest(
                StringUtils.normalizeUsername(request.username()),
                request.password(),
                request.fullName().trim(),
                StringUtils.normalizeEmail(request.email()),
                StringUtils.normalizeOptional(request.phone()),
                StringUtils.normalizeOptional(request.jobTitle()),
                request.active() != null ? request.active() : Boolean.TRUE,
                request.departmentId(),
                request.role() != null ? request.role() : Role.USER
        );
    }

    /**
     * Retrieves the acting user.
     *
     * @param actorId the identifier of the authenticated user
     * @return the user
     * @throws UserNotFoundException if no user exists with the given identifier
     */
    private User getActor(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
    }

    /**
     * Retrieves a paginated list of users.
     *
     * @param pageable pagination and sorting information
     * @return a page of user response DTOs
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> userMapper.toResponse(user, user.getDepartment()));
    }

    /**
     * Retrieves a user by identifier.
     *
     * @param id the identifier of the user
     * @return the user response DTO
     * @throws UserNotFoundException if the user cannot be found
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponse(user, user.getDepartment());
    }

    /**
     * Deletes a user.
     *
     * @param targetUserId the identifier of the user to delete
     * @param actorId the identifier of the authenticated user
     * @throws UserNotFoundException if the actor or target user cannot be found
     * @throws InsufficientPermissionsException if the actor is not permitted to delete the target user
     */
    @Transactional
    public void deleteById(Long targetUserId, Long actorId) {
        User actor = getActor(actorId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        userAuthorizationService.validateDeletion(actor, target);
        userRepository.delete(target);
    }

    /**
     * Updates an existing user.
     *
     * @param targetUserId the identifier of the user to update
     * @param request the update request
     * @param actorId the identifier of the authenticated user
     * @return the updated user response DTO
     * @throws UserNotFoundException if the actor or target user cannot be found
     * @throws DepartmentNotFoundException if the requested department does not exist
     * @throws InsufficientPermissionsException if the actor is not permitted to perform the update
     */
    @Transactional
    public UserResponse updateById(Long targetUserId, UpdateUserRequest request, Long actorId) {
        User actor = getActor(actorId);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        UpdateUserRequest sanitized = sanitize(request);
        Department requestedDepartment = resolveRequestedDepartment(sanitized.departmentId());
        userAuthorizationService.validateUpdate(actor, target, sanitized, requestedDepartment);
        userMapper.updateEntity(target, sanitized, requestedDepartment);
        User savedUser = userRepository.save(target);
        return userMapper.toResponse(savedUser, savedUser.getDepartment());
    }

    /**
     * Sanitizes an update user request by normalizing non-null fields.
     * <p>
     * Null values are preserved to indicate no change.
     * </p>
     *
     * @param request the original request
     * @return a sanitized request with normalized values
     */
    private UpdateUserRequest sanitize(UpdateUserRequest request) {
        return new UpdateUserRequest(
                StringUtils.normalizeOptional(request.fullName()),
                StringUtils.normalizeEmail(request.email()),
                StringUtils.normalizeOptional(request.phone()),
                StringUtils.normalizeOptional(request.jobTitle()),
                request.active(),
                request.departmentId(),
                request.role()
        );
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