package io.github.kkusylabs.useradmin.backend.exceptions.department;

import io.github.kkusylabs.useradmin.backend.exceptions.NotFoundException;

/**
 * Thrown when a department with the specified identifier does not exist.
 *
 * <p>Maps to HTTP 404 (Not Found).</p>
 *
 * @author kkusy
 */
public class DepartmentNotFoundException extends NotFoundException {

    /**
     * Creates a new exception for a missing department.
     *
     * @param id the identifier of the department
     */
    public DepartmentNotFoundException(Long id) {
        super(
                "DEPARTMENT_NOT_FOUND",
                "Department with id " + id + " not found"
        );
    }
}