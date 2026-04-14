package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Exception thrown when a requested department cannot be found.
 * <p>
 * Typically used when attempting to reference a department by its identifier
 * and no matching entity exists in the system.
 *
 * @author kkusy
 */
public class DepartmentNotFoundException extends RuntimeException {

    /**
     * Creates a new exception for a missing department.
     *
     * @param departmentId the identifier of the department that was not found
     */
    public DepartmentNotFoundException(Long departmentId) {
        super("Department with id " + departmentId + " was not found");
    }
}
