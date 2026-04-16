package io.github.kkusylabs.useradmin.backend.exceptions.user;

import io.github.kkusylabs.useradmin.backend.exceptions.NotFoundException;

/**
 * Thrown when a user with the specified identifier does not exist.
 *
 * <p>Maps to HTTP 404 (Not Found).</p>
 */
public class UserNotFoundException extends NotFoundException {

    /**
     * Creates a new exception for a missing user.
     *
     * @param id the identifier of the user
     */
    public UserNotFoundException(Long id) {
        super(
                "USER_NOT_FOUND",
                "User with id " + id + " not found"
        );
    }
}
