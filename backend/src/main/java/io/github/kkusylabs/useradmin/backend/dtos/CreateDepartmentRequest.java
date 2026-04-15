package io.github.kkusylabs.useradmin.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new department.
 * <p>
 * This DTO is used when a client submits data to create a department
 * within the system.
 * </p>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li><strong>name</strong> – The unique name of the department.
 *       Must not be blank and must not exceed 50 characters.</li>
 * </ul>
 *
 * @author kkusy
 */
public record CreateDepartmentRequest(
        @NotBlank
        @Size(max = 50)
        String name) {
}
