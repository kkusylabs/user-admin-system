package io.github.kkusylabs.useradmin.backend.dtos.user;

public record UserListItemResponse(
        UserDetailResponse user,
        boolean canUpdate,
        boolean canDelete
) {
}