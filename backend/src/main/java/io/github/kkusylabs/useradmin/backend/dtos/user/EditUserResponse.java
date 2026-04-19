package io.github.kkusylabs.useradmin.backend.dtos.user;

public record EditUserResponse(
        UserDetailResponse user,
        UpdateUserCapabilities updateCapabilities
) {
}
