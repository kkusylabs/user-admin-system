package io.github.kkusylabs.useradmin.client.core.api.user;

public record EditUserResponse(
		UserDetailResponse user, 
		UpdateUserCapabilities updateCapabilities) {
}
