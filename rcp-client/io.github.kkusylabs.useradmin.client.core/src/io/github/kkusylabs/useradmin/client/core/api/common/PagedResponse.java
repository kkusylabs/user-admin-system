package io.github.kkusylabs.useradmin.client.core.api.common;

import java.util.List;

public record PagedResponse<T>(
		List<T> content, 
		int pageNumber, 
		int pageSize, 
		long totalElements, 
		int totalPages,
		boolean first, 
		boolean last) {
}
