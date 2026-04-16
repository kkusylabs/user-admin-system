package io.github.kkusylabs.useradmin.backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for exceptions indicating that a requested resource was not found.
 *
 * <p>Maps to HTTP {@link HttpStatus#NOT_FOUND} (404). Use this when a requested
 * entity does not exist or cannot be located.</p>
 *
 * <p>Examples include "user not found" or "department not found".</p>
 */
public abstract class NotFoundException extends ApiException {

    /**
     * Creates a new not-found exception.
     *
     * @param code    stable, machine-readable error identifier
     * @param message human-readable error message
     */
    protected NotFoundException(String code, String message) {
        super(HttpStatus.NOT_FOUND, code, message);
    }
}
