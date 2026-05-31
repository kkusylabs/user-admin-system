package io.github.kkusylabs.useradmin.client.core.api;

import java.util.Map;

/**
 * Thrown when the API rejects a request because one or more validation rules
 * were violated.
 *
 * <p>Typically corresponds to an HTTP {@code 400 Bad Request} response caused
 * by invalid field values, missing required data, or failed business rules.
 */
@SuppressWarnings("serial")
public class ValidationException extends BadRequestException {

	private final Map<String, String> errors;

	public ValidationException(
			String message,
			Map<String, String> errors) {

		super(message);
		this.errors = errors == null
				? Map.of()
				: Map.copyOf(errors);
	}

	public Map<String, String> getErrors() {
		return errors;
	}
}
