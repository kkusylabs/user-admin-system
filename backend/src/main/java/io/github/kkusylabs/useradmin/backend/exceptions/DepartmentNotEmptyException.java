package io.github.kkusylabs.useradmin.backend.exceptions;

/**
 * Thrown when an attempt is made to delete a department that still has users.
 *
 * @author kkusy
 */
public class DepartmentNotEmptyException extends RuntimeException {

    /**
     * Creates a new exception for a department that cannot be deleted.
     *
     * @param departmentId the ID of the department that still contains users
     */
    public DepartmentNotEmptyException(Long departmentId) {
        super("Department cannot be deleted because it still has users: " + departmentId);
    }
}
