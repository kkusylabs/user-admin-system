package io.github.kkusylabs.useradmin.backend.exceptions.user;

import io.github.kkusylabs.useradmin.backend.exceptions.ConflictException;

/**
 * Thrown when an update would remove or deactivate the last active administrator.
 *
 * <p>The system must always have at least one active user with the
 * administrator role. This exception enforces that invariant.</p>
 */
public class LastActiveAdminUpdateException extends ConflictException {

    public static final String CODE = "LAST_ACTIVE_ADMIN_UPDATE_NOT_ALLOWED";

    public LastActiveAdminUpdateException() {
        super(
                CODE,
                "You may not remove or deactivate the last active administrator."
        );
    }
}