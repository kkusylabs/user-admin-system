package io.github.kkusylabs.useradmin.client.core.api.user;

/**
 * Request used to create a new user account.
 *
 * @param username unique account username
 * @param password initial account password
 * @param fullName user's full display name
 * @param email user's email address
 * @param phone user's phone number
 * @param jobTitle user's job title
 * @param departmentId assigned department identifier
 * @param role assigned user role
 */
public record CreateUserRequest(
		String username, 
		String password, 
		String fullName, 
		String email, 
		String phone,
		String jobTitle, 
		Long departmentId, 
		Role role) {
}