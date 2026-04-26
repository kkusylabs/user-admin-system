package io.github.kkusylabs.useradmin.backend.dtos.user;

public record DeleteUserCapabilities(
        boolean canDelete,
        String reason
) {
    public static DeleteUserCapabilities none(String reason) {
        return new DeleteUserCapabilities(
                false,
                reason
        );
    }
}
