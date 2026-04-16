package io.github.kkusylabs.useradmin.backend.exceptions.department;

import io.github.kkusylabs.useradmin.backend.exceptions.ConflictException;

/**
 * Thrown when attempting to create or update a department with a name
 * that already exists.
 *
 * <p>Maps to HTTP 409 (Conflict) as the request violates a uniqueness
 * constraint on department names.</p>
 *
 * @author kkusy
 */
public class DepartmentNameAlreadyExistsException extends ConflictException {

    /**
     * Creates a new exception for a duplicate department name.
     *
     * @param name the conflicting department name
     */
    public DepartmentNameAlreadyExistsException(String name) {
        super(
                "DEPARTMENT_NAME_ALREADY_EXISTS",
                "Department name already exists: " + name
        );
    }
}