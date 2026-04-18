package io.github.kkusylabs.useradmin.backend.dtos.user;

public record UpdateUserCapabilities(
        boolean canEditFullName,
        boolean canEditEmail,
        boolean canEditPhone,
        boolean canEditJobTitle,
        boolean canEditActive,
        boolean canEditDepartment,
        boolean canEditRole
) {
}