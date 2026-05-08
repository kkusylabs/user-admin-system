package io.github.kkusylabs.useradmin.client.core.api.user;

public record UserListFilter(
		String search, 
		Boolean active, 
		Long departmentId, 
		Role role) {
}
