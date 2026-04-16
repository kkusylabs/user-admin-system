package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Response payload representing a department.
 * <p>
 * This DTO is returned to clients when department data is requested
 * from the system.
 * </p>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li><strong>id</strong> – The unique identifier of the department.</li>
 *   <li><strong>name</strong> – The name of the department.</li>
 * </ul>
 *
 * @author kkusy
 */
public record DepartmentResponse(
        Long id,
        String name,
        DepartmentCapabilities capabilities
) {}
