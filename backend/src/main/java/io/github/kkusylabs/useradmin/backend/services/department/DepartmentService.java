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

    @Transactional(readOnly = true)
    public DepartmentListItemResponse getDepartment(Long departmentId, Long actorId) {
        User actor = getRequiredActor(actorId);
        Department department = getRequiredDepartment(departmentId);
        return toListItemResponse(department, actor);
    }

    @Transactional
    public void deleteDepartment(Long departmentId, Long actorId) {
        User actor = getRequiredActor(actorId);
        Department department = getRequiredDepartment(departmentId);
        authorizationService.validateDeleteRequest(actor, department);
        departmentRepository.delete(department);
    }

    public DepartmentListItemResponse updateDepartment(Long departmentId, UpdateDepartmentRequest request, Long actorId) {
        User actor = getRequiredActor(actorId);
        Department department = getRequiredDepartment(departmentId);
        authorizationService.validateUpdateRequest(actor, department);
        departmentMapper.updateDepartment(department, request);
        department = departmentRepository.save(department);
        return toListItemResponse(department, actor);
    }

    private User getRequiredActor(Long actorId) {
        return userRepository.findById(actorId)
                .orElseThrow(() -> new UserNotFoundException(actorId));
    }

    private Department getRequiredDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    private DepartmentListItemResponse toListItemResponse(Department department, User actor) {
        return departmentMapper.toListItemResponse(
                department,
                authorizationService.canUpdate(actor, department),
                authorizationService.canDelete(actor, department)
        );
    }

}