package io.github.kkusylabs.useradmin.client.core.api.common;

import java.util.List;

/**
 * Generic paginated API response wrapper.
 *
 * @param content items returned for the current page
 * @param pageNumber zero-based page index
 * @param pageSize maximum number of items per page
 * @param totalElements total number of matching items across all pages
 * @param totalPages total number of available pages
 * @param first indicates whether the current page is the first page
 * @param last indicates whether the current page is the last page
 * @param <T> response item type
 */
public record PagedResponse<T>(
		List<T> content, 
		int pageNumber, 
		int pageSize, 
		long totalElements, 
		int totalPages,
		boolean first, 
		boolean last) {
}
