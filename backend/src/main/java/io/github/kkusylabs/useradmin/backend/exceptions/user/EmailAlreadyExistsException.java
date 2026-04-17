package io.github.kkusylabs.useradmin.backend.exceptions.user;

import io.github.kkusylabs.useradmin.backend.exceptions.ConflictException;

/**
 * Thrown when attempting to create or update a user with an email
 * that is already in use.
 *
 * <p>This exception maps to a conflict (HTTP 409) and is typically
 * triggered by unique constraint violations or pre-checks in the
 * service layer.</p>
 */
public class EmailAlreadyExistsException extends ConflictException {

    /**
     * Creates an exception for a duplicate email.
     *
     * @param email the email address that already exists
     */
    public EmailAlreadyExistsException(String email) {
        super(
                "EMAIL_ALREADY_EXISTS",
                "Email already exists: " + email
        );
    }
}
