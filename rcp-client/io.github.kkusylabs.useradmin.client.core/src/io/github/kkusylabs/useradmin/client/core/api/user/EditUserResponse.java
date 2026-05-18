package io.github.kkusylabs.useradmin.client.core.api.user;

/**
 * Response containing user details and update capability metadata required for
 * user editing workflows.
 *
 * @param user current user details
 * @param updateCapabilities available update operations and reference data
 */
public record EditUserResponse(
		UserDetailResponse user, 
		UpdateUserCapabilities updateCapabilities) {
}
