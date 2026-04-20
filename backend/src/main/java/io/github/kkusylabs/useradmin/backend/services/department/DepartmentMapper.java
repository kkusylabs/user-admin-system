package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentDetailsResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.models.Department;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Department} entities to department-related DTOs.
 *
 * <p>Includes authorization-aware mapping for response objects.</p>
 *
 * @author kkusy
 */
@Component
public class DepartmentMapper {


    public DepartmentMapper() {
    }

    public Department fromCreateRequest(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        department.setDescription(request.description());
        return department;
    }

    public DepartmentDetailsResponse toDetailsResponse(Department department) {
        return new DepartmentDetailsResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.isActive()
        );
    }


    public DepartmentListItemResponse toListItemResponse(Department department,
                                                         boolean canEdit, boolean canDelete) {
        return new DepartmentListItemResponse(
                toDetailsResponse(department),
                canEdit,
                canDelete
        );
    }

    public void updateDepartment(Department department, UpdateDepartmentRequest request) {
        department.setName(request.name());
        department.setDescription(request.description());
        department.setActive(request.active());
    }
}