package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Thrown when the request cannot be authenticated.
 *
 * <p>Typically corresponds to an HTTP {@code 401 Unauthorized} response.
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends RestClientException {
	
	public UnauthorizedException(String message) {
		super(message);
	}

	public UnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}
}