package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNameAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotFoundException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.UserNotFoundException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing department operations.
 *
 * <p>Handles department CRUD workflows, including authorization, validation,
 * persistence, and mapping to API response models.</p>
 *
 * <p>This service is responsible for:</p>
 * <ul>
 *   <li>creating, retrieving, updating, and deleting departments</li>
 *   <li>enforcing department-specific business rules</li>
 *   <li>loading the current actor and validating permissions</li>
 *   <li>mapping entities to authorization-aware response DTOs</li>
 * </ul>
 *
 * @author kkusy
 */

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DepartmentAuthorizationService authorizationService;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepository departmentRepository,
                             UserRepository userRepository,
                             DepartmentAuthorizationService authorizationService,
                             DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
        this.departmentMapper = departmentMapper;
    }

    /**
     * Creates a new department.
     *
     * <p>Validates that the current actor can create departments and that the
     * requested department name is unique before saving.</p>
     *
     * @param request request payload containing department data
     * @param actorId id of the current user
     * @return created department with permissions for the current user
     * @throws UserNotFoundException if the current user does not exist
     * @throws DepartmentNameAlreadyExistsException if a department with the same name already exists
     */
    @Transactional
    public DepartmentListItemResponse createDepartment(CreateDepartmentRequest request, Long actorId) {
        User actor = getRequiredActor(actorId);
        authorizationService.validateCreateRequest(actor);

        if (departmentRepository.existsByNameIgnoreCase(request.name())) {
            throw new DepartmentNameAlreadyExistsException(request.name());
        }

        Department department = departmentMapper.fromCreateRequest(request);
        Department saved = departmentRepository.save(department);
        return toListItemResponse(saved, actor);
    }

    /**
     * Returns all departments visible to the current user.
     *
     * <p>Each department entry includes department details and the current
     * user's permissions for that department.</p>
     *
     * @param actorId id of the current user
     * @return list response containing departments and create capability
     * @throws UserNotFoundException if the current user does not exist
     */
    @Transactional(readOnly = true)
    public DepartmentListResponse getDepartments(Long actorId) {
        User actor = getRequiredActor(actorId);
        List<DepartmentListItemResponse> departments = departmentRepository.findAllOrderByNameIgnoreCase()
                .stream()
                .map(department -> toListItemResponse(department, actor))
                .toList();

        return new DepartmentListResponse(
                departments,
                authorizationService.canCreate(actor));
    }

    /**
     * Returns a single department by id.
     *
     * <p>The response includes department details and the current user's
     * permissions for that department.</p>
     *
     * @param departmentId department id
     * @param actorId id of the current user
     * @return department details with permissions
     * @throws UserNotFoundException if the current user does not exist
     * @throws DepartmentNotFoundException if the department does not exist
     */
    @Transactional(readOnly = true)
    public DepartmentListItemResponse getDepartment(Long departmentId, Long actorId) {
        User actor = getRequiredActor(actorId);
        Department department = getRequiredDepartment(departmentId);
        return toListItemResponse(department, actor);
    }

    /**
     * Deletes a department.
     *
     * <p>Validates that the current actor can delete the department before
     * removing it.</p>
     *
     * @param departmentId department id
     * @param actorId id of the current user
     * @throws UserNotFoundException if the current user does not exist
     * @throws DepartmentNotFoundException if the department does not exist
     */
    @Transactional
    public void deleteDepartment(Long departmentId, Long actorId) {
        User actor = getRequiredActor(actorId);
        Department department = getRequiredDepartment(departmentId);
        authorizationService.validateDeleteRequest(actor, department);
        departmentRepository.delete(department);
    }

    /**
     * Updates an existing department.
     *
     * <p>Validates that the current actor can update the department, applies
     * the requested changes, and saves the updated entity.</p>
     *
     * @param departmentId department id
     * @param request request payload containing updated data
     * @param actorId id of the current user
     * @return updated department with permissions for the current user
     * @throws UserNotFoundException if the current user does not exist
     * @throws DepartmentNotFoundException if the department does not exist
     */
    public DepartmentListItemResponse updateDepartment(Long departmentId, UpdateDepartmentRequest request, Long actorId) {
        User actor = getRequiredActor(actorId);
        Department department = getRequiredDepartment(departmentId);
        authorizationService.validateUpdateRequest(actor, department);
        departmentMapper.updateDepartment(department, request);
        department = departmentRepository.save(department);
        return toListItemResponse(department, actor);
    }

    /**
     * Loads the current actor or fails if not found.
     *
     * @param actorId user id
     * @return resolved user
     * @throws UserNotFoundException if no user exists for the given id
     */
    private User getRequiredActor(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
    }

    /**
     * Loads a department or fails if not found.
     *
     * @param departmentId department id
     * @return resolved department
     * @throws DepartmentNotFoundException if no department exists for the given id
     */
    private Department getRequiredDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    /**
     * Maps a department to a list item response for the given actor.
     *
     * <p>Includes department details together with the actor's update and
     * delete permissions.</p>
     *
     * @param department department to map
     * @param actor current user
     * @return department response with permissions
     */
    private DepartmentListItemResponse toListItemResponse(Department department, User actor) {
        return departmentMapper.toListItemResponse(
                department,
                authorizationService.canUpdate(actor, department),
                authorizationService.canDelete(actor, department)
        );
    }

}