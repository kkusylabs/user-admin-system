package io.github.kkusylabs.useradmin.client.core.api.user;


/**
 * User entry returned in user list and lookup responses.
 *
 * @param user user details
 * @param canUpdate indicates whether the current user can update the user
 * @param canDelete indicates whether the current user can delete the user
 */
public record UserListItemResponse(
		UserDetailResponse user, 
		boolean canUpdate, 
		boolean canDelete) {
}
