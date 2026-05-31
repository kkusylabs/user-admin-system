package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Thrown when the requested resource does not exist or cannot be located.
 *
 * <p>
 * Typically corresponds to an HTTP {@code 404 Not Found} response.
 */
@SuppressWarnings("serial")
public class NotFoundException extends RestClientException {

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
