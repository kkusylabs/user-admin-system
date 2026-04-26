package io.github.kkusylabs.useradmin.backend.services.user;

public record DeleteUserPolicy(
        boolean canDelete,
        String reason
) {
    public static DeleteUserPolicy denied(String reason) {
        return new DeleteUserPolicy(false, reason);
    }

    public static DeleteUserPolicy allowed() {
        return new DeleteUserPolicy(true, null);
    }
}
