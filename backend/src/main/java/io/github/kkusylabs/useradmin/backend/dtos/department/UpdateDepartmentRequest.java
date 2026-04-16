package io.github.kkusylabs.useradmin.backend.dtos.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a department.
 *
 * <p>Contains the fields that can be modified by the client.</p>
 *
 * @param name the updated department name (must not be blank, max 50 characters)
 * @author kkusy
 */
public record UpdateDepartmentRequest(
        @NotBlank
        @Size(max = 50)
        String name
) {
}
