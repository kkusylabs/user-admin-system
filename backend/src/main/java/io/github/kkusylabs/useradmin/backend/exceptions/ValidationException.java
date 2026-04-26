package io.github.kkusylabs.useradmin.backend.exceptions;

public class ValidationException extends BadRequestException {

    public ValidationException(String message) {
        this("VALIDATION_ERROR", message);
    }

    public ValidationException(String code, String message) {
        super(code, message);
    }

    public static ValidationException field(String field, String message) {
        return new ValidationException(
                "VALIDATION_ERROR",
                field + ": " + message
        );
    }
}
