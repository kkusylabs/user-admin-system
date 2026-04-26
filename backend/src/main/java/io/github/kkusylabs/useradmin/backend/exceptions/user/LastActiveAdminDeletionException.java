package io.github.kkusylabs.useradmin.backend.exceptions.user;

import io.github.kkusylabs.useradmin.backend.exceptions.ConflictException;

public class LastActiveAdminDeletionException extends ConflictException {

    public static final String CODE = "LAST_ACTIVE_ADMIN_DELETION_NOT_ALLOWED";

    public LastActiveAdminDeletionException() {
        super(
                CODE,
                "You may not delete the last active administrator."
        );
    }
}
