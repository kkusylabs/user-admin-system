package io.github.kkusylabs.useradmin.backend.dtos.user;

public record UserListItemResponse(
        UserDetailResponse user,
        boolean canEdit,
        boolean canDelete
) {
}