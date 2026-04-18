package io.github.kkusylabs.useradmin.backend.dtos.user;

public record DeleteUserCapabilities(
        boolean canDelete,
        String reason
) {
}
