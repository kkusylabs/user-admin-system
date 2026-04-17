package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNameAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotEmptyException;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotFoundException;
import io.github.kkusylabs.useradmin.backend.mappers.DepartmentMapper;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public DepartmentResponse createDepartment(User actor, CreateDepartmentRequest request) {
        authorizationService.validateCreate(actor);

        String name = normalizeDepartmentName(request.name());

        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DepartmentNameAlreadyExistsException(name);
        }

        Department department = new Department();
        department.setName(name);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(department, actor);
    }

    @Transactional
    public Department renameDepartment(User actor, Long departmentId, UpdateDepartmentRequest request) {
        authorizationService.validateRename(actor);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        String name = normalizeDepartmentName(request.name());

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, department.getId())) {
            throw new DepartmentNameAlreadyExistsException(name);
        }

        department.setName(name);
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(User actor, Long departmentId) {


        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        authorizationService.validateDelete(actor, department);

        if (userRepository.existsByDepartmentId(department.getId())) {
            throw new DepartmentNotEmptyException(departmentId);
        }

        departmentRepository.delete(department);
    }

    private String normalizeDepartmentName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Department name must not be null or blank");
        }
        return value.trim();
    }
}