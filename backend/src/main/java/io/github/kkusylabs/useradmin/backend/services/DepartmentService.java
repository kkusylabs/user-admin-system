package io.github.kkusylabs.useradmin.backend.services;

import io.github.kkusylabs.useradmin.backend.dtos.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.DepartmentResponse;
import io.github.kkusylabs.useradmin.backend.dtos.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.DepartmentAlreadyExistsException;
import io.github.kkusylabs.useradmin.backend.exceptions.DepartmentNotEmptyException;
import io.github.kkusylabs.useradmin.backend.exceptions.DepartmentNotFoundException;
import io.github.kkusylabs.useradmin.backend.mappers.DepartmentMapper;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepartmentService {
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(UserRepository userRepository, DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        String normalizedName = request.name().trim();

        if (departmentRepository.existsByName(normalizedName)) {
            throw new DepartmentAlreadyExistsException(normalizedName);
        }

        Department department = new Department();
        department.setName(normalizedName);

        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(departmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateById(Long departmentId, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        String normalizedName = request.name().trim();

        if (!department.getName().equals(normalizedName)
                && departmentRepository.existsByNameAndIdNot(normalizedName, departmentId)) {
            throw new DepartmentAlreadyExistsException(normalizedName);
        }

        department.setName(normalizedName);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    @Transactional
    public void deleteById(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        if (userRepository.existsByDepartmentId(departmentId)) {
            throw new DepartmentNotEmptyException(departmentId);
        }

        departmentRepository.delete(department);
    }
}
