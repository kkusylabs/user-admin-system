package io.github.kkusylabs.useradmin.client.core.api.user;

public record UserListItemResponse(
		UserDetailResponse user, 
		boolean canUpdate, 
		boolean canDelete) {
}
