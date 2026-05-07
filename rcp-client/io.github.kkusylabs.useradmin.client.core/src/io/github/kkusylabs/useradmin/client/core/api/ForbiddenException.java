package io.github.kkusylabs.useradmin.client.core.api;

public class ForbiddenException extends RestClientException {
	public ForbiddenException(String message) {
		super(message);
	}
}
