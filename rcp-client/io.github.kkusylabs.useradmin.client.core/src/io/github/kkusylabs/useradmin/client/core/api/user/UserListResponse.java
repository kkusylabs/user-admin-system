package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.api.common.PagedResponse;

/**
 * Paginated response returned by the user list endpoint.
 *
 * @param users paginated collection of users with actor-relative action flags
 * @param canCreate indicates whether the current user can create users
 */
public record UserListResponse(
		PagedResponse<UserListItemResponse> users, 
		boolean canCreate) {
}
