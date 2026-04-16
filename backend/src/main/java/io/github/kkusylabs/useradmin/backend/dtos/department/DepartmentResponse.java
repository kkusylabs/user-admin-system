package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Response payload representing a department.
 *
 * @param id           the unique identifier of the department
 * @param name         the department name
 * @param capabilities the actions the current user can perform on this department
 * @author kkusy
 */
public record DepartmentResponse(
        Long id,
        String name,
        DepartmentCapabilities capabilities
) {
}
