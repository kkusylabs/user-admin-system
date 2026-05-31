package io.github.kkusylabs.useradmin.client.core.api;

import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP error response returned by a REST API.
 *
 * @param status HTTP status code
 * @param body response body
 * @param headers response headers
 */
public record RestErrorResponse(
		int status,
		String body,
		Map<String, List<String>> headers) {
}