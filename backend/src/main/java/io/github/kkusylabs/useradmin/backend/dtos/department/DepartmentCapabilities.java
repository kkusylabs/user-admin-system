package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Describes the actions the current user can perform on a department.
 *
 * @param canRename    whether the department can be renamed
 * @param canDelete    whether the department can be deleted
 * @param deleteReason optional explanation why deletion is not allowed;
 *                     {@code null} if {@code canDelete} is {@code true}
 * @author kkusy
 */
public record DepartmentCapabilities(
        boolean canRename,
        boolean canDelete,
        String deleteReason
) {
}
