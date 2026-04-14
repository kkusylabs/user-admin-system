package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Exception thrown when a user attempts to perform an operation
 * for which they do not have sufficient permissions.
 * <p>
 * Typically used to enforce authorization rules at the service layer
 * when the current user is not allowed to perform a requested action.
 *
 * @author kkusy
 */
public class InsufficientPermissionsException extends RuntimeException {

    /**
     * Creates a new exception with the specified detail message.
     *
     * @param message a detailed explanation of why the operation is not permitted
     */
    public InsufficientPermissionsException(String message) {
        super(message);
    }
}
