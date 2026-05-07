package io.github.kkusylabs.useradmin.client.core.api;

public class ServerErrorException extends RestClientException {
	public ServerErrorException(String message) {
		super(message);
	}
}