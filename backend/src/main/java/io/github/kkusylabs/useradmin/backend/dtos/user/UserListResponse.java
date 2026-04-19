package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.dtos.common.PagedResponse;

public record UserListResponse(
        PagedResponse<UserListItemResponse> users,
        boolean canCreateUser
) {
}
