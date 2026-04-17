package io.github.kkusylabs.useradmin.backend.exceptions.security;

import io.github.kkusylabs.useradmin.backend.exceptions.ForbiddenException;

/**
 * Thrown when the current user lacks permission to perform the requested action.
 *
 * <p>Maps to HTTP 403 (Forbidden). Use this when the user is authenticated
 * but does not have sufficient privileges for the operation.</p>
 *
 * @author kkusy
 */
public class InsufficientPermissionsException extends ForbiddenException {

    /**
     * Creates a new exception for insufficient permissions.
     */
    public InsufficientPermissionsException() {
        super(
                "INSUFFICIENT_PERMISSIONS",
                "You do not have permission to perform this action"
        );
    }

    /**
     * Creates a new exception for insufficient permissions.
     */
    public InsufficientPermissionsException(String message) {
        super(
                "INSUFFICIENT_PERMISSIONS",
                message
        );
    }
}
