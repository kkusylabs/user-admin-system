package io.github.kkusylabs.useradmin.backend.mappers;

import io.github.kkusylabs.useradmin.backend.dtos.*;
import io.github.kkusylabs.useradmin.backend.models.Department;
import org.springframework.stereotype.Component;

/**
 * Maps between {@link Department} entities and department-related DTOs.
 * <p>
 * This class provides conversion methods for:
 * </p>
 * <ul>
 *   <li>Creating a {@link Department} entity from a {@link CreateDepartmentRequest}</li>
 *   <li>Updating an existing {@link Department} using an {@link UpdateDepartmentRequest}</li>
 *   <li>Converting a {@link Department} entity to a {@link DepartmentResponse}</li>
 * </ul>
 */
@Component
public class DepartmentMapper {

    /**
     * Converts a {@link Department} entity to a {@link DepartmentResponse}.
     *
     * @param department the department entity to convert
     * @return a response DTO representing the department
     */
    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName());
    }
}
