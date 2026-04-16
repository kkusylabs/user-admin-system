package io.github.kkusylabs.useradmin.backend.dtos.department;

public record DepartmentCapabilities(
        boolean canRename,
        boolean canDelete,
        String deleteReason
) {
}
