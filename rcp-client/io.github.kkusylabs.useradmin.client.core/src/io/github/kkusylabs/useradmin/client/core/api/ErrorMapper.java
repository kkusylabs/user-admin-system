package io.github.kkusylabs.useradmin.client.core.api;

@FunctionalInterface
public interface ErrorMapper {
	RestClientException map(RestErrorResponse response, Throwable cause);
}
