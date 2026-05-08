package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.api.common.SortSpec;

//UserListFilter filter = new UserListFilter(
//        "smith",
//        true,
//        3L,
//        Role.ADMIN,
//        new SortSpec("username", SortSpec.Direction.ASC)
//);

public record UserListFilter(
		String search, 
		Boolean active, 
		Long departmentId, 
		Role role, 
		SortSpec sort) {
}