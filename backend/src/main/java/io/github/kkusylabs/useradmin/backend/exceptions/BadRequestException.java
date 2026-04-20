package io.github.kkusylabs.useradmin.backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for client-side request errors (HTTP 400).
 *
 * <p>Used for exceptions caused by invalid input or business rule violations.</p>
 *
 * <p>Subclasses should provide a domain-specific error code and message.</p>
 *
 * @author kkusy
 */
public abstract class BadRequestException extends ApiException {
    /**
     * Creates a new bad request exception.
     *
     * @param code application-specific error code
     * @param message human-readable error message
     */
    protected BadRequestException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}