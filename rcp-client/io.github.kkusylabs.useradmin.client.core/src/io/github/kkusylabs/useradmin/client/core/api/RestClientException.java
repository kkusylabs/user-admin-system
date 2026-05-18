package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Base exception type for HTTP client and API interaction failures.
 *
 * <p>Used for transport errors, serialization failures, unexpected HTTP
 * responses, and API-specific error conditions.
 */
@SuppressWarnings("serial")
public class RestClientException extends RuntimeException {
	
	/**
	 * Creates a new exception instance.
	 *
	 * @param message exception message
	 */
	public RestClientException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception instance with an underlying cause.
	 *
	 * @param message exception message
	 * @param cause underlying failure cause
	 */
	public RestClientException(String message, Throwable cause) {
		super(message, cause);
	}
}