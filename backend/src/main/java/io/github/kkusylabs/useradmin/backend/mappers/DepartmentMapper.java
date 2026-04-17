package io.github.kkusylabs.useradmin.backend.mappers;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentResponse;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.services.department.DepartmentAuthorizationService;
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

    private final DepartmentAuthorizationService authorizationService;

    public DepartmentMapper(DepartmentAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Converts a department to a response DTO, including capabilities
     * for the given actor.
     *
     * @param department the department to convert
     * @param actor      the current user (used to determine capabilities)
     * @return the mapped response
     */
    public DepartmentResponse toResponse(Department department, User actor) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                authorizationService.getCapabilities(actor, department)
        );
    }
}