package io.github.kkusylabs.useradmin.client.core.api;

public class BadRequestException extends RestClientException {
	public BadRequestException(String message) {
		super(message);
	}
}