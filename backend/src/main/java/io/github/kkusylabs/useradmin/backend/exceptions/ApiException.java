package io.github.kkusylabs.useradmin.backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for all application-specific exceptions exposed via the API.
 *
 * <p>Encapsulates HTTP semantics ({@link HttpStatus}) and a stable error {@code code}
 * that can be used by clients for programmatic handling.</p>
 *
 * <p>Subclasses should represent meaningful business or domain errors
 * (e.g. "user not found", "username already exists") and define appropriate
 * HTTP status and error codes.</p>
 *
 * @author kkusy
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    /**
     * Creates a new API exception.
     *
     * @param status  HTTP status to be returned to the client
     * @param code    stable, machine-readable error identifier (e.g. {@code USER_NOT_FOUND})
     * @param message human-readable error message
     */
    protected ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    /**
     * @return HTTP status associated with this exception
     */
    public HttpStatus getStatus() {
        return status;
    }

    /**
     * @return application-specific error code for client-side handling
     */
    public String getCode() {
        return code;
    }
}
