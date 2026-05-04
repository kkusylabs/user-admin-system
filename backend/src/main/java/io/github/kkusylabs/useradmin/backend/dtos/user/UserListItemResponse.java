package io.github.kkusylabs.useradmin.backend.dtos.user;

/**
 * User data with actor-specific permissions.
 *
 * <p>Includes flags indicating whether the current actor may update or delete
 * the user.</p>
 *
 * @param user user details
 * @param canUpdate whether the actor may update this user
 * @param canDelete whether the actor may delete this user
 */
public record UserListItemResponse(
        UserDetailResponse user,
        boolean canUpdate,
        boolean canDelete
) {
}