package io.github.kkusylabs.useradmin.backend.services.user;

/**
 * Authorization result for deleting a user.
 *
 * @param canDelete whether the actor may delete the target user
 * @param reason denial reason when {@code canDelete} is {@code false}; otherwise {@code null}
 */
public record DeleteUserPolicy(
        boolean canDelete,
        String reason
) {
    /**
     * Creates a policy that denies deletion.
     *
     * @param reason explanation for the denial
     * @return a denied deletion policy
     */
    public static DeleteUserPolicy denied(String reason) {
        return new DeleteUserPolicy(false, reason);
    }

    /**
     * Creates a policy that allows deletion.
     *
     * @return an allowed deletion policy
     */
    public static DeleteUserPolicy allowed() {
        return new DeleteUserPolicy(true, null);
    }
}
