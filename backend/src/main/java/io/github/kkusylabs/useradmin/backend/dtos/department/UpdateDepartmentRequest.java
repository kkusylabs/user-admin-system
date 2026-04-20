package io.github.kkusylabs.useradmin.backend.dtos.department;

import io.github.kkusylabs.useradmin.backend.utils.StringNormalizer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
