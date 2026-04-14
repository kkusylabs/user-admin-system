package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Exception thrown when attempting to register a user with a username
 * that is already in use.
 *
 * <p>This typically occurs during signup when the provided username
 * conflicts with an existing user record.</p>
 *
 * @author kkusy
 */
public class UsernameAlreadyExistsException extends RuntimeException {
    /**
     * Creates a new exception for the given username.
     *
     * @param username the username that is already taken
     */
    public UsernameAlreadyExistsException(String username) {
        super("Username already taken: " + username);
    }
}
