package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Thrown when the API rejects a request because one or more validation rules
 * were violated.
 *
 * <p>Typically corresponds to an HTTP {@code 400 Bad Request} response caused
 * by invalid field values, missing required data, or failed business rules.
 */
@SuppressWarnings("serial")
public class ValidationException extends BadRequestException {
	/**
	 * Creates a new exception instance.
	 *
	 * @param message validation error message returned by the API
	 */
	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
