package io.github.kkusylabs.useradmin.backend.exceptions.department;

import io.github.kkusylabs.useradmin.backend.exceptions.ConflictException;

/**
 * Thrown when an operation cannot be performed because the department
 * still contains dependent entities.
 *
 * <p>Maps to HTTP 409 (Conflict) as the current state of the department
 * prevents the requested action (e.g. deleting a non-empty department).</p>
 *
 * @author kkusy
 */
public class DepartmentNotEmptyException extends ConflictException {

    /**
     * Creates a new exception for a non-empty department.
     *
     * @param departmentId the identifier of the department
     */
    public DepartmentNotEmptyException(Long departmentId) {
        super(
                "DEPARTMENT_NOT_EMPTY",
                "Department with id " + departmentId + " is not empty"
        );
    }
}
