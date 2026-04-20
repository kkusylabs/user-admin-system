package io.github.kkusylabs.useradmin.backend.dtos.department;

import io.github.kkusylabs.useradmin.backend.utils.StringNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a department.
 *
 * <p>Applies basic validation and input normalization before reaching the service layer.</p>
 *
 * <ul>
 *   <li><b>name</b> – required, max 50 chars (uniqueness enforced in service layer)</li>
 *   <li><b>description</b> – optional, max 255 chars</li>
 * </ul>
 *
 * <p>Both fields are trimmed; blank descriptions become {@code null}.</p>
 *
 * @param name        department name
 * @param description optional description
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
