package io.github.kkusylabs.useradmin.client.core.api.user;

import io.github.kkusylabs.useradmin.client.core.api.common.SortSpec;

/**
 * Filtering and sorting options used when retrieving paginated user lists.
 *
 * @param search optional free-text search value
 * @param active optional active status filter
 * @param departmentId optional department identifier filter
 * @param role optional role filter
 * @param sort optional sort specification
 */
public record UserListFilter(
		String search, 
		Boolean active, 
		Long departmentId, 
		Role role, 
		SortSpec sort) {
}