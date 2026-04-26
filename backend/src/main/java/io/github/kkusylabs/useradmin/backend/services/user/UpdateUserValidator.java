package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.ValidationException;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for validating {@link UpdateUserRequest} instances.
 *
 * <p>This validator is designed specifically for PATCH-style updates using
 * {@link JsonNullable}, where each field can be in one of three states:
 * <ul>
 *     <li><b>undefined</b> – field not provided, no validation applied</li>
 *     <li><b>present with value</b> – field is validated</li>
 *     <li><b>present with null</b> – treated as explicit clearing or rejected depending on field rules</li>
 * </ul>
 *
 * <p>Validation rules:
 * <ul>
 *     <li>Required fields (e.g., fullName, email) must not be null when present</li>
 *     <li>Optional fields (e.g., phone, jobTitle) may be null</li>
 *     <li>Some fields (e.g., role, active) must not be null if present</li>
 *     <li>Length and format constraints are enforced on non-null values</li>
 * </ul>
 *
 * <p>This class performs structural validation only. Business rules such as
 * existence checks (e.g., departmentId refers to a real entity) should be handled
 * in the service layer.</p>
 */
@Component
public final class UpdateUserValidator {

    /** Pattern for validating phone numbers (optional leading '+' followed by 7–15 digits). */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{7,15}$");

    /** Simple pattern for validating email format. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public UpdateUserValidator() {
        // Utility class; prevent instantiation
    }

    /**
     * Validates the given {@link UpdateUserRequest}.
     *
     * <p>Only fields that are {@link JsonNullable#isPresent() present} are validated.
     * Fields that are undefined are ignored.</p>
     *
     * @param request the update request to validate
     * @throws IllegalArgumentException if any field violates validation rules
     */
    public void validate(UpdateUserRequest request) {
        requiredString(request.fullName(), "fullName", 100);
        requiredEmail(request.email(), "email", 255);
        optionalPhone(request.phone(), "phone");
        optionalString(request.jobTitle(), "jobTitle", 100);
        requiredPositiveLong(request.departmentId(), "departmentId");
        requiredIfPresent(request.role(), "role");
        requiredIfPresent(request.active(), "active");
    }

    /**
     * Validates a required string field when present.
     *
     * <p>If the field is present, it must not be null and must not exceed the given length.</p>
     *
     * @param field the field to validate
     * @param name the field name (used in error messages)
     * @param maxLength maximum allowed length
     */
    private void requiredString(JsonNullable<String> field, String name, int maxLength) {
        if (!field.isPresent()) return;

        String value = field.orElse(null);
        if (value == null) {
            throw new ValidationException(name, "cannot be null or blank");
        }
        if (value.length() > maxLength) {
            throw new ValidationException(name, "must be at most " + maxLength + " characters");
        }
    }

    /**
     * Validates a required email field when present.
     *
     * <p>If the field is present, it must not be null, must not exceed the given length,
     * and must match the email pattern.</p>
     *
     * @param field the email field
     * @param name the field name
     * @param maxLength maximum allowed length
     */
    private void requiredEmail(JsonNullable<String> field, String name, int maxLength) {
        if (!field.isPresent()) return;

        String value = field.orElse(null);
        if (value == null) {
            throw new ValidationException(name, "cannot be null or blank");
        }
        if (value.length() > maxLength) {
            throw new ValidationException(name, "must be at most " + maxLength + " characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new ValidationException(name, "is invalid");
        }
    }

    /**
     * Validates an optional phone field when present.
     *
     * <p>If the field is present and non-null, it must match the phone pattern.</p>
     *
     * @param field the phone field
     * @param name the field name
     */
    private void optionalPhone(JsonNullable<String> field, String name) {
        if (!field.isPresent()) return;

        String value = field.orElse(null);
        if (value != null && !PHONE_PATTERN.matcher(value).matches()) {
            throw new ValidationException(name, "is invalid");
        }
    }

    /**
     * Validates an optional string field when present.
     *
     * <p>If the field is present and non-null, it must not exceed the given length.</p>
     *
     * @param field the field to validate
     * @param name the field name
     * @param maxLength maximum allowed length
     */
    private void optionalString(JsonNullable<String> field, String name, int maxLength) {
        if (!field.isPresent()) return;

        String value = field.orElse(null);
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(name, "must be at most " + maxLength + " characters");
        }
    }

    /**
     * Validates a required numeric field when present.
     *
     * <p>If the field is present, it must not be null and must be a positive number.</p>
     *
     * @param field the numeric field
     * @param name the field name
     */
    private void requiredPositiveLong(JsonNullable<Long> field, String name) {
        if (!field.isPresent()) return;

        Long value = field.orElse(null);
        if (value == null || value <= 0) {
            throw new ValidationException(name, "must be a positive number");
        }
    }

    /**
     * Validates that a field, if present, is not null.
     *
     * <p>This is used for fields where null is not allowed when explicitly provided.</p>
     *
     * @param field the field to validate
     * @param name the field name
     * @param <T> the field type
     */
    private <T> void requiredIfPresent(JsonNullable<T> field, String name) {
        if (!field.isPresent()) return;

        if (field.orElse(null) == null) {
            throw new ValidationException(name, "cannot be null");
        }
    }
}