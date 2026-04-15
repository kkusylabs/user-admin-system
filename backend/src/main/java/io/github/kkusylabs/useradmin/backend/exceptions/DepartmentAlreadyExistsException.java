package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Thrown when an attempt is made to create or update a department
 * with a name that already exists.
 * <p>
 * This exception indicates a violation of the system's uniqueness
 * constraint for department names.
 * </p>
 *
 * @author kkusy
 */
public class DepartmentAlreadyExistsException extends RuntimeException {

    /**
     * Creates a new exception for a duplicate department name.
     *
     * @param departmentName the name that is already in use
     */
    public DepartmentAlreadyExistsException(String departmentName) {
        super("Department name already taken: " + departmentName);
    }
}