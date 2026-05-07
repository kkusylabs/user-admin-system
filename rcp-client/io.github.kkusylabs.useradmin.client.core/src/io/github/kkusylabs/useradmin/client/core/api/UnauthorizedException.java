package io.github.kkusylabs.useradmin.client.core.api;

public class UnauthorizedException extends RestClientException {
	public UnauthorizedException(String message) {
		super(message);
	}
}