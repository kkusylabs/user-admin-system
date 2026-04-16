package io.github.kkusylabs.useradmin.backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized exception handler for REST controllers.
 *
 * <p>Translates application and validation exceptions into consistent
 * HTTP responses using {@link ProblemDetail}. Ensures that all errors
 * returned by the API follow a predictable structure.</p>
 *
 * <p>Handled exception types include:</p>
 * <ul>
 *     <li>{@link ApiException} – domain/business errors with explicit HTTP semantics</li>
 *     <li>{@link MethodArgumentNotValidException} – validation errors for request bodies</li>
 *     <li>{@link HandlerMethodValidationException} – validation errors for request parameters</li>
 *     <li>{@link ConstraintViolationException} – constraint violations outside MVC binding</li>
 *     <li>{@link Exception} – fallback for unexpected errors</li>
 * </ul>
 *
 * <p>Each response includes a stable {@code code} for programmatic handling,
 * along with optional validation error details when applicable.</p>
 *
 * @author kkusy
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles domain-specific exceptions.
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
     * Handles constraint violations outside standard request binding.
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
     * Handles unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);

        ProblemDetail problem = createProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred.",
                request.getRequestURI()
        );
        problem.setProperty("code", "INTERNAL_ERROR");
        return problem;
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