package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Projection for retrieving basic department information.
 *
 * <p>Used by the repository layer to fetch only the fields required for
 * read operations, avoiding loading full {@code Department} entities.</p>
 *
 * <p>This projection is intended for internal use and should be mapped to
 * API DTOs (e.g. {@code DepartmentOption}) before returning to clients.</p>
 */
public record DepartmentOption(
        Long id,
        String name
) {
}