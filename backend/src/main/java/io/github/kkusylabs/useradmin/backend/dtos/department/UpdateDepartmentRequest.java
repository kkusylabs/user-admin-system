package io.github.kkusylabs.useradmin.backend.dtos.department;

import io.github.kkusylabs.useradmin.backend.utils.StringNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a department.
 *
 * <p>Applies validation and input normalization before updating an existing department.</p>
 *
 * <ul>
 *   <li><b>name</b> – required, max 50 chars (uniqueness enforced in service layer)</li>
 *   <li><b>description</b> – optional, max 255 chars</li>
 *   <li><b>active</b> – required flag indicating whether the department is active</li>
 * </ul>
 *
 * <p>Text fields are trimmed; blank descriptions become {@code null}.</p>
 *
 * @param name        department name
 * @param description optional description
 * @param active      active status
 * @author kkusy
 */
public record UpdateDepartmentRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @Size(max = 255)
        String description,

        @NotNull
        Boolean active
) {
    public UpdateDepartmentRequest {
        name = StringNormalizer.trim(name);
        description = StringNormalizer.trimToNull(description);
    }
}
