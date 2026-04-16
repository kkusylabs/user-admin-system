package io.github.kkusylabs.useradmin.backend.dtos.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Request payload for updating an existing department.
 * <p>
 * This DTO is used when a client submits updated data for a department.
 * </p>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li><strong>name</strong> – The updated name of the department.
 *       Must not be blank and must not exceed 50 characters.</li>
 * </ul>
 *
 * @author kkusy
 */
public record UpdateDepartmentRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {
}
