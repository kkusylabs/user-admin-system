package io.github.kkusylabs.useradmin.backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for exceptions caused by a conflict with the current resource state.
 *
 * <p>Maps to HTTP {@link HttpStatus#CONFLICT} (409). Typically used when a request
 * cannot be completed due to a constraint or business rule, such as duplicate values
 * or invalid state transitions.</p>
 *
 * <p>Examples include "username already exists" or "department not empty".</p>
 *
 * @author kkusy
 */
public abstract class ConflictException extends ApiException {

    /**
     * Creates a new conflict exception.
     *
     * @param code    stable, machine-readable error identifier
     * @param message human-readable error message
     */
    protected ConflictException(String code, String message) {
        super(HttpStatus.CONFLICT, code, message);
    }
}