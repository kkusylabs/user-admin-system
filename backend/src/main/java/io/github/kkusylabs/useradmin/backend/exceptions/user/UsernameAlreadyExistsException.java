package io.github.kkusylabs.useradmin.backend.exceptions.user;

import io.github.kkusylabs.useradmin.backend.exceptions.ConflictException;

/**
 * Thrown when attempting to create or update a user with a username
 * that is already in use.
 *
 * <p>Maps to HTTP 409 (Conflict) as the request violates a uniqueness
 * constraint on usernames.</p>
 */
public class UsernameAlreadyExistsException extends ConflictException {

    /**
     * Creates a new exception for a duplicate username.
     *
     * @param username the conflicting username
     */
    public UsernameAlreadyExistsException(String username) {
        super(
                "USERNAME_ALREADY_EXISTS",
                "Username already exists: " + username
        );
    }
}