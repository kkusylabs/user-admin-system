package io.github.kkusylabs.useradmin.backend.dtos.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a department.
 *
 * @param name the department name (must be unique, not blank, max 50 characters)
 * @author kkusy
 */
public record CreateDepartmentRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {
}
