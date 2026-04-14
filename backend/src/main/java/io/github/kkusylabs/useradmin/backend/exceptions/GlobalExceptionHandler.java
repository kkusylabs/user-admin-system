package io.github.kkusylabs.useradmin.backend.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * <p>
 * Translates application-specific exceptions into HTTP responses using
 * {@link ProblemDetail}.
 * </p>
 *
 * @author kkusy
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles cases where a requested user cannot be found.
     *
     * @param ex the exception
     * @return problem detail with HTTP 404 status
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage()
        );
    }

    /**
     * Handles cases where a requested department cannot be found.
     *
     * @param ex the exception
     * @return problem detail with HTTP 404 status
     */
    @ExceptionHandler(DepartmentNotFoundException.class)
    public ProblemDetail handleDepartmentNotFound(DepartmentNotFoundException ex) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage()
        );
    }

    /**
     * Handles cases where a username is already in use.
     *
     * @param ex the exception
     * @return problem detail with HTTP 409 status
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Username Already Exists",
                ex.getMessage()
        );
    }

    /**
     * Handles cases where a user attempts to perform an operation
     * without sufficient permissions.
     *
     * @param ex the exception
     * @return problem detail with HTTP 403 status
     */
    @ExceptionHandler(InsufficientPermissionsException.class)
    public ProblemDetail handleInsufficientPermissions(InsufficientPermissionsException ex) {
        return createProblem(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage()
        );
    }

    /**
     * Handles validation failures for request bodies annotated with {@code @Valid}.
     *
     * @param ex the exception
     * @return problem detail with HTTP 400 status and field-level validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request fields are invalid."
        );

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Handles validation failures for request parameters, path variables, and similar inputs.
     *
     * @param ex the exception
     * @return problem detail with HTTP 400 status and validation error details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request parameters are invalid."
        );

        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage());
        }

        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Handles unexpected exceptions not matched by more specific handlers.
     *
     * @param ex the exception
     * @return problem detail with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred."
        );
    }

    /**
     * Creates a {@link ProblemDetail} instance with the given status, title, and detail message.
     *
     * <p>This is a convenience method used to standardize error responses across the application.
     * It ensures that all exceptions are converted into a consistent RFC 9457-style problem response.</p>
     *
     * @param status the HTTP status to associate with the problem
     * @param title  a short, human-readable summary of the problem
     * @param detail a detailed explanation of the problem
     * @return a populated {@link ProblemDetail} instance
     */
    private ProblemDetail createProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle(title);
        problem.setDetail(detail);
        return problem;
    }
}
