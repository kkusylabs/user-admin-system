package io.github.kkusylabs.useradmin.backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized exception handler for REST controllers.
 *
 * <p>Maps application, validation, and security exceptions to consistent
 * HTTP error responses using {@link ProblemDetail}.</p>
 *
 * <p>All responses include a stable {@code code} for programmatic handling
 * and may include field-level validation errors when applicable.</p>
 *
 * <p>Handled categories include:</p>
 * <ul>
 *   <li>{@link ApiException} – domain/business errors with defined HTTP semantics</li>
 *   <li>Validation errors – request body, parameters, and constraint violations</li>
 *   <li>Security errors – authentication and authorization failures</li>
 *   <li>Fallback – unexpected exceptions</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles domain-specific exceptions.
     *
     * <p>Uses the status and error code defined by the exception.</p>
     */
    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest request) {
        ProblemDetail problem = createProblem(
                ex.getStatus(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        problem.setProperty("code", ex.getCode());
        return problem;
    }

    /**
     * Handles validation errors for {@code @Valid @RequestBody}.
     *
     * <p>Collects field-level errors and returns them as a map of field names to messages.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request fields are invalid.",
                request.getRequestURI()
        );

        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Handles validation errors for controller method parameters
     * (e.g. {@code @RequestParam}, {@code @PathVariable}).
     *
     * <p>Maps parameter names to validation messages.</p>
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more request parameters are invalid.",
                request.getRequestURI()
        );

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getParameterValidationResults().forEach(result -> {
            String name = Optional.ofNullable(result.getMethodParameter().getParameterName())
                    .orElse("param");

            result.getResolvableErrors().forEach(error ->
                    errors.putIfAbsent(name, error.getDefaultMessage())
            );
        });

        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Handles constraint violations raised outside standard request binding.
     *
     * <p>Typically triggered by validation on method parameters or service-layer
     * constraints. Each violation is mapped to a property path and message.</p>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "One or more constraints were violated.",
                request.getRequestURI()
        );

        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage());
        }

        problem.setProperty("code", "VALIDATION_ERROR");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Handles authentication failures.
     *
     * <p>Returned when credentials are missing or invalid.</p>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ProblemDetail problem = createProblem(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid username or password.",
                request.getRequestURI()
        );
        problem.setProperty("code", "INVALID_CREDENTIALS");
        return problem;
    }

    /**
     * Handles authorization failures.
     *
     * <p>Returned when the authenticated user lacks permission to perform the
     * requested operation.</p>
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail problem = createProblem(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "Access denied.",
                request.getRequestURI()
        );
        problem.setProperty("code", "INSUFFICIENT_PERMISSIONS");
        return problem;
    }

    /**
     * Handles unexpected exceptions.
     *
     * <p>If the exception implements {@link ErrorResponse}, its {@link ProblemDetail}
     * is reused. Otherwise, a generic internal server error response is returned.</p>
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problem = errorResponse.getBody();
            problem.setInstance(URI.create(request.getRequestURI()));
            problem.setProperty("timestamp", Instant.now());

            if (problem.getProperties() == null || !problem.getProperties().containsKey("code")) {
                problem.setProperty("code", "SPRING_ERROR");
            }

            return problem;
        }

        return createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getRequestURI()
        );
    }

    /**
     * Creates a {@link ProblemDetail} with common fields.
     */
    private ProblemDetail createProblem(
            HttpStatus status,
            String title,
            String detail,
            String path
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(path));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}