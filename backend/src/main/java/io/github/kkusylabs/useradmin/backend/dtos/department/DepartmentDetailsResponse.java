package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Response model with full department details.
 *
 * <p>Represents the current state of a department as returned by the API.</p>
 *
 * <ul>
 *   <li><b>id</b> – unique identifier</li>
 *   <li><b>name</b> – department name</li>
 *   <li><b>description</b> – optional description</li>
 *   <li><b>active</b> – whether the department is active</li>
 * </ul>
 *
 * <p>Used within higher-level responses (e.g. list items) rather than returned standalone.</p>
 *
 * @author kkusy
 */
public record DepartmentDetailsResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}