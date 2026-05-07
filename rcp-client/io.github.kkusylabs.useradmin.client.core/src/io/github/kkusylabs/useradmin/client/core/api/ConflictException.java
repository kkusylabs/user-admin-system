package io.github.kkusylabs.useradmin.client.core.api;

public class ConflictException extends RestClientException {
	public ConflictException(String message) {
		super(message);
	}
}