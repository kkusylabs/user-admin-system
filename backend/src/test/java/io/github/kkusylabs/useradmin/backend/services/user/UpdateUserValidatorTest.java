package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.ValidationException;
import io.github.kkusylabs.useradmin.backend.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import static io.github.kkusylabs.useradmin.backend.testsupport.UpdateUserRequests.*;
import static org.junit.jupiter.api.Assertions.*;


class UpdateUserValidatorTest {

    private UpdateUserValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UpdateUserValidator();
    }

    @Test
    void acceptsEmptyPatchRequest() {
        assertDoesNotThrow(() -> validator.validate(noChanges()));
    }

    @Test
    void treatsNullJsonNullableWrappersAsUndefined() {
        UpdateUserRequest request = new UpdateUserRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void acceptsValidRequestWithAllFieldsPresent() {
        UpdateUserRequest request = new UpdateUserRequest(
                JsonNullable.of("Jane User"),
                JsonNullable.of("JANE.USER@Example.COM"),
                JsonNullable.of("+15551234567"),
                JsonNullable.of("Senior Engineer"),
                JsonNullable.of(true),
                JsonNullable.of(1L),
                JsonNullable.of(Role.MANAGER)
        );

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Nested
    class FullNameValidation {

        @Test
        void acceptsFullNameAtMaxLength() {
            assertDoesNotThrow(() -> validator.validate(withFullName("a".repeat(100))));
        }

        @Test
        void rejectsNullFullNameWhenPresent() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withFullName(null)));

            assertEquals("fullName cannot be null or blank", exception.getMessage());
        }

        @Test
        void rejectsBlankFullNameAfterNormalization() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withFullName("   ")));

            assertEquals("fullName cannot be null or blank", exception.getMessage());
        }

        @Test
        void rejectsFullNameOverMaxLength() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withFullName("a".repeat(101))));

            assertEquals("fullName must be at most 100 characters", exception.getMessage());
        }
    }

    @Nested
    class EmailValidation {

        @Test
        void acceptsValidEmailAtMaxLength() {
            String localPart = "a".repeat(243);
            String email = localPart + "@example.com"; // 255 chars

            assertEquals(255, email.length());
            assertDoesNotThrow(() -> validator.validate(withEmail(email)));
        }

        @Test
        void rejectsNullEmailWhenPresent() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withEmail(null)));

            assertEquals("email cannot be null or blank", exception.getMessage());
        }

        @Test
        void rejectsBlankEmailAfterNormalization() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withEmail("   ")));

            assertEquals("email cannot be null or blank", exception.getMessage());
        }

        @Test
        void rejectsEmailOverMaxLength() {
            String localPart = "a".repeat(244);
            String email = localPart + "@example.com"; // 256 chars

            assertEquals(256, email.length());
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withEmail(email)));

            assertEquals("email must be at most 255 characters", exception.getMessage());
        }

        @Test
        void rejectsInvalidEmailFormat() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withEmail("not-an-email")));

            assertEquals("email is invalid", exception.getMessage());
        }
    }

    @Nested
    class PhoneValidation {

        @Test
        void acceptsNullPhoneWhenPresentBecausePhoneIsOptional() {
            assertDoesNotThrow(() -> validator.validate(withPhone(null)));
        }

        @Test
        void acceptsPhoneWithSevenDigits() {
            assertDoesNotThrow(() -> validator.validate(withPhone("1234567")));
        }

        @Test
        void acceptsPhoneWithPlusAndFifteenDigits() {
            assertDoesNotThrow(() -> validator.validate(withPhone("+123456789012345")));
        }

        @Test
        void acceptsFormattedPhoneAfterNormalization() {
            assertDoesNotThrow(() -> validator.validate(withPhone("+1 (555) 123-4567")));
        }

        @Test
        void rejectsPhoneWithTooFewDigits() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withPhone("123456")));

            assertEquals("phone is invalid", exception.getMessage());
        }

        @Test
        void rejectsPhoneWithTooManyDigits() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withPhone("1234567890123456")));

            assertEquals("phone is invalid", exception.getMessage());
        }
    }

    @Nested
    class JobTitleValidation {

        @Test
        void acceptsNullJobTitleWhenPresentBecauseJobTitleIsOptional() {
            assertDoesNotThrow(() -> validator.validate(withJobTitle(null)));
        }

        @Test
        void acceptsBlankJobTitleAfterNormalizationBecauseJobTitleIsOptional() {
            assertDoesNotThrow(() -> validator.validate(withJobTitle("   ")));
        }

        @Test
        void acceptsJobTitleAtMaxLength() {
            assertDoesNotThrow(() -> validator.validate(withJobTitle("a".repeat(100))));
        }

        @Test
        void rejectsJobTitleOverMaxLength() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withJobTitle("a".repeat(101))));

            assertEquals("jobTitle must be at most 100 characters", exception.getMessage());
        }
    }

    @Nested
    class DepartmentValidation {

        @Test
        void acceptsPositiveDepartmentId() {
            assertDoesNotThrow(() -> validator.validate(withDepartment(1L)));
        }

        @Test
        void rejectsNullDepartmentIdWhenPresent() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withDepartment(null)));

            assertEquals("departmentId must be a positive number", exception.getMessage());
        }

        @Test
        void rejectsZeroDepartmentId() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withDepartment(0L)));

            assertEquals("departmentId must be a positive number", exception.getMessage());
        }

        @Test
        void rejectsNegativeDepartmentId() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withDepartment(-1L)));

            assertEquals("departmentId must be a positive number", exception.getMessage());
        }
    }

    @Nested
    class RoleAndActiveValidation {

        @Test
        void acceptsNonNullRoleWhenPresent() {
            assertDoesNotThrow(() -> validator.validate(withRole(Role.ADMIN)));
        }

        @Test
        void rejectsNullRoleWhenPresent() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withRole(null)));

            assertEquals("role cannot be null", exception.getMessage());
        }

        @Test
        void acceptsNonNullActiveWhenPresent() {
            assertDoesNotThrow(() -> validator.validate(withActive(false)));
        }

        @Test
        void rejectsNullActiveWhenPresent() {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> validator.validate(withActive(null)));

            assertEquals("active cannot be null", exception.getMessage());
        }
    }
}
