package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.dtos.department.CreateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentDetailsResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.backend.dtos.department.UpdateDepartmentRequest;
import io.github.kkusylabs.useradmin.backend.models.Department;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Department} entities to and from department-related DTOs.
 *
 * <p>Centralizes transformation logic between persistence models and API models,
 * including creation, updates, and response mapping.</p>
 *
 * <p>This mapper is stateless and contains no business logic.</p>
 */
@Component
public class DepartmentMapper {


    public DepartmentMapper() {
    }

    /**
     * Creates a new {@link Department} entity from a create request.
     *
     * @param request request payload
     * @return new department entity (not yet persisted)
     */
    public Department fromCreateRequest(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        department.setDescription(request.description());
        return department;
    }

    /**
     * Converts a {@link Department} entity to a details response.
     *
     * @param department source entity
     * @return department details DTO
     */
    public DepartmentDetailsResponse toDetailsResponse(Department department) {
        return new DepartmentDetailsResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.isActive()
        );
    }

    /**
     * Converts a {@link Department} entity to a list item response.
     *
     * <p>Includes department details together with permission flags.</p>
     *
     * @param department source entity
     * @param canEdit whether the current user can update the department
     * @param canDelete whether the current user can delete the department
     * @return department list item DTO
     */
    public DepartmentListItemResponse toListItemResponse(Department department,
                                                         boolean canEdit, boolean canDelete) {
        return new DepartmentListItemResponse(
                toDetailsResponse(department),
                canEdit,
                canDelete
        );
    }

    /**
     * Applies update request data to an existing {@link Department} entity.
     *
     * <p>Mutates the given entity; does not persist it.</p>
     *
     * @param department target entity
     * @param request request payload with updated values
     */
    public void updateDepartment(Department department, UpdateDepartmentRequest request) {
        department.setName(request.name());
        department.setDescription(request.description());
        department.setActive(request.active());
    }
}