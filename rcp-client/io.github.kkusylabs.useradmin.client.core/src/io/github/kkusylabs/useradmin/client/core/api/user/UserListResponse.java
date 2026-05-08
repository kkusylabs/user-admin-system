package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.api.common.PagedResponse;

/**
 * Paginated response for the user list endpoint.
 *
 * @param users     page of users with actor-relative action flags
 * @param canCreate whether the actor may open the create-user flow
 */
public record UserListResponse(
		PagedResponse<UserListItemResponse> users, 
		boolean canCreate) {
}
