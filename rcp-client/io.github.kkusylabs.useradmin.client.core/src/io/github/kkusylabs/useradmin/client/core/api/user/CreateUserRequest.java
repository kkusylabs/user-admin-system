package io.github.kkusylabs.useradmin.client.core.api.user;

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