package io.github.kkusylabs.useradmin.client.core.api;

import java.util.List;
import java.util.Map;

public record RestErrorResponse(
		int status,
		String body,
		Map<String, List<String>> headers) {
}