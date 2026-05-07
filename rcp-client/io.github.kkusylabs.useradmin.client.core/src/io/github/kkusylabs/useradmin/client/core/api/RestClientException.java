package io.github.kkusylabs.useradmin.client.core.api;

public class RestClientException extends RuntimeException {
	public RestClientException(String message) {
		super(message);
	}

	public RestClientException(String message, Throwable cause) {
		super(message, cause);
	}
}