package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

/**
 * Detailed user information returned by the API.
 *
 * @param id unique user identifier
 * @param username account username
 * @param fullName user's full display name
 * @param email user's email address
 * @param phone user's phone number
 * @param jobTitle user's job title
 * @param active indicates whether the user account is active
 * @param role assigned user role
 * @param department assigned department
 */
public record UserDetailResponse(
		Long id, 
		String username, 
		String fullName, 
		String email, 
		String phone, 
		String jobTitle,
		boolean active, 
		Role role, 
		DepartmentOption department) {
}
