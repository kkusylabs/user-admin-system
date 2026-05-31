package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Thrown when the authenticated caller is not permitted to perform the
 * requested operation.
 *
 * <p>
 * Typically corresponds to an HTTP {@code 403 Forbidden} response.
 */
@SuppressWarnings("serial")
public class ForbiddenException extends RestClientException {

	public ForbiddenException(String message) {
		super(message);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(message, cause);
	}
}
