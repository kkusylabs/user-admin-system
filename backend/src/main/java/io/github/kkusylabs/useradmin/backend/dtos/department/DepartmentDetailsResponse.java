package io.github.kkusylabs.useradmin.backend.dtos.department;


public record DepartmentDetailsResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}