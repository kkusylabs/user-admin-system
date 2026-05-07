package io.github.kkusylabs.useradmin.client.core.api;

public class ValidationException extends BadRequestException {
	public ValidationException(String message) {
		super(message);
	}
}
