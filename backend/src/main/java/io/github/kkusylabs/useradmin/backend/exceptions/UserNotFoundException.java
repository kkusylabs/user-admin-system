package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Exception thrown when a requested user cannot be found.
 * <p>
 * Typically used when a user lookup by ID does not return a result.
 * </p>
 *
 * @author kkusy
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing user.
     *
     * @param userId the ID of the user that was not found
     */
    public UserNotFoundException(Long userId) {
        super("User with id " + userId + " was not found");
    }
}
