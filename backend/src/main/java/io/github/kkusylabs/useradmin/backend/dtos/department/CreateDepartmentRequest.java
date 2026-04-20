package io.github.kkusylabs.useradmin.backend.dtos.department;

import io.github.kkusylabs.useradmin.backend.utils.StringNormalizer;
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
        String name,

        @Size(max = 255)
        String description
) {
    public CreateDepartmentRequest {
        name = StringNormalizer.trim(name);
        description = StringNormalizer.trimToNull(description);
    }
}
