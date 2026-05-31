package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Thrown when the API rejects a request because it conflicts with the current
 * state of the target resource.
 *
 * <p>
 * Typically corresponds to an HTTP {@code 409 Conflict} response.
 */
@SuppressWarnings("serial")
public class ConflictException extends RestClientException {

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}

}