package io.github.kkusylabs.useradmin.client.core.api;

public class NotFoundException extends RestClientException {
    public NotFoundException(String message) {
        super(message);
    }
}
