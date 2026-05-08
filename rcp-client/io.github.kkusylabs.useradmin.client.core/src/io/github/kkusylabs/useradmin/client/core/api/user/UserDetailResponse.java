package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentOption;

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
