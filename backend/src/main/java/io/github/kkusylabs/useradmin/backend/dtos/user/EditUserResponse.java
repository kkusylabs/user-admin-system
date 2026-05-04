package io.github.kkusylabs.useradmin.backend.dtos.user;

/**
 * Response used to populate the edit-user view.
 *
 * <p>Combines the current user details with the actor-specific update
 * capabilities needed to decide which form fields should be editable.</p>
 *
 * @param user current user details
 * @param updateCapabilities fields and options the actor may update
 */
public record EditUserResponse(
        UserDetailResponse user,
        UpdateUserCapabilities updateCapabilities
) {
}
