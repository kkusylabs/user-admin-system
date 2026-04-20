package io.github.kkusylabs.useradmin.backend.exceptions.department;


import io.github.kkusylabs.useradmin.backend.exceptions.BadRequestException;

/**
 * Thrown when an operation targets an inactive department.
 *
 * <p>Indicates that the requested action cannot be performed because the
 * department is not active.</p>
 *
 * @author kkusy
 */
public class InactiveDepartmentException extends BadRequestException {

    /**
     * Creates a new exception for the given department.
     *
     * @param departmentId id of the inactive department
     */
    public InactiveDepartmentException(Long departmentId) {
        super(
                "DEPARTMENT_INACTIVE",
                "Department " + departmentId + " is inactive."
        );
    }
}
