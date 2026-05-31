package io.github.kkusylabs.useradmin.client.core.api;

/**
 * Thrown when the API returns an unexpected server-side failure.
 *
 * <p>Typically corresponds to an HTTP {@code 5xx} response.
 */
@SuppressWarnings("serial")
public class ServerErrorException extends RestClientException {
	
	public ServerErrorException(String message) {
		super(message);
	}

	public ServerErrorException(String message, Throwable cause) {
		super(message, cause);
	}
}