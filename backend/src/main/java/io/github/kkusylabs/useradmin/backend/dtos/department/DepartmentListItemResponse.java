package io.github.kkusylabs.useradmin.backend.dtos.department;

public record DepartmentListItemResponse (
        DepartmentDetailsResponse department,
        boolean canUpdate,
        boolean canDelete
) {
}
