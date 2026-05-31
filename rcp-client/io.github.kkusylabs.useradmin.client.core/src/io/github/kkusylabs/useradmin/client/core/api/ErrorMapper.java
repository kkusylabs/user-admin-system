package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Strategy interface for mapping HTTP error responses to
 * {@link RestClientException} instances.
 */
@FunctionalInterface
public interface ErrorMapper {
	RestClientException map(RestErrorResponse response, Throwable cause);
}
