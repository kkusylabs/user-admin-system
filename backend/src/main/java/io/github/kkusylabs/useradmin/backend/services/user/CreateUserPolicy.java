package io.github.kkusylabs.useradmin.backend.services.user;

/**
 * Authorization policy for user creation.
 *
 * <p>Represents whether an actor may create users and, if not, why the action is denied.
 * Used by application services to validate create operations before execution.
 *
 * @param canCreate whether the actor is allowed to create users
 * @param reason explanation for denial when {@code canCreate} is {@code false}, otherwise {@code null}
 */
public record CreateUserPolicy(
        boolean canCreate,
        String reason
) {

    /**
     * Returns a policy that denies user creation.
     *
     * @param reason explanation for denial
     */
    public static CreateUserPolicy denied(String reason) {
        return new CreateUserPolicy(false, reason);
    }

    /**
     * Returns a policy that allows user creation.
     */
    public static CreateUserPolicy allowed() {
        return new CreateUserPolicy(true, null);
    }
}
