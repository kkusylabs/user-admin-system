package io.github.kkusylabs.useradmin.backend.exceptions;

public class ValidationException extends BadRequestException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
