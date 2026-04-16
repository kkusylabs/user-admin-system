package io.github.kkusylabs.useradmin.backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for exceptions indicating that the current user is not allowed
 * to perform the requested operation.
 *
 * <p>Maps to HTTP {@link HttpStatus#FORBIDDEN} (403). Use this when the request
 * is understood and the user is authenticated, but lacks the necessary
 * permissions or access rights.</p>
 *
 * <p>Examples include attempting to access another user's data or performing
 * an admin-only action without sufficient privileges.</p>
 *
 * @author kkusy
 */
public abstract class ForbiddenException extends ApiException {

    /**
     * Creates a new forbidden exception.
     *
     * @param code    stable, machine-readable error identifier
     * @param message human-readable error message
     */
    protected ForbiddenException(String code, String message) {
        super(HttpStatus.FORBIDDEN, code, message);
    }
}
