package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Thrown when a request fails validation outside standard framework handling.
 *
 * <p>Used for programmatic or service-layer validation where field-level
 * constraints are not automatically enforced by the framework.</p>
 */
public class ValidationException extends BadRequestException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}
