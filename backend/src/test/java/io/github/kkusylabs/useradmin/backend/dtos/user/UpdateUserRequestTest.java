package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.models.Role;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserRequestTest {

    @Test
    void nullWrappersBecomeUndefined() {
        UpdateUserRequest request = new UpdateUserRequest(
                null, null, null, null, null, null, null
        );

        assertTrue(request.fullName().isUndefined());
        assertTrue(request.email().isUndefined());
        assertTrue(request.phone().isUndefined());
        assertTrue(request.jobTitle().isUndefined());
        assertTrue(request.active().isUndefined());
        assertTrue(request.departmentId().isUndefined());
        assertTrue(request.role().isUndefined());
    }

    @Test
    void undefinedFieldsRemainUndefined() {
        UpdateUserRequest request = new UpdateUserRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );

        assertTrue(request.fullName().isUndefined());
        assertTrue(request.email().isUndefined());
        assertTrue(request.phone().isUndefined());
        assertTrue(request.jobTitle().isUndefined());
        assertTrue(request.active().isUndefined());
        assertTrue(request.departmentId().isUndefined());
        assertTrue(request.role().isUndefined());
    }

    @Test
    void normalizesProvidedStringFields() {
        UpdateUserRequest request = new UpdateUserRequest(
                JsonNullable.of("  John Doe  "),
                JsonNullable.of("  JOHN@EXAMPLE.COM  "),
                JsonNullable.of("  +1 (555) 123-4567  "),
                JsonNullable.of("  Developer  "),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );

        assertEquals("John Doe", request.fullName().get());
        assertEquals("john@example.com", request.email().get());
        assertEquals("+15551234567", request.phone().get());
        assertEquals("Developer", request.jobTitle().get());
    }

    @Test
    void blankStringFieldsBecomeNullButRemainPresent() {
        UpdateUserRequest request = new UpdateUserRequest(
                JsonNullable.of("   "),
                JsonNullable.of("   "),
                JsonNullable.of("   "),
                JsonNullable.of("   "),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );

        assertTrue(request.fullName().isPresent());
        assertTrue(request.email().isPresent());
        assertTrue(request.phone().isPresent());
        assertTrue(request.jobTitle().isPresent());

        assertNull(request.fullName().orElse(null));
        assertNull(request.email().orElse(null));
        assertNull(request.phone().orElse(null));
        assertNull(request.jobTitle().orElse(null));
    }

    @Test
    void explicitNullsRemainPresentNulls() {
        UpdateUserRequest request = new UpdateUserRequest(
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null)
        );

        assertTrue(request.fullName().isPresent());
        assertTrue(request.email().isPresent());
        assertTrue(request.phone().isPresent());
        assertTrue(request.jobTitle().isPresent());
        assertTrue(request.active().isPresent());
        assertTrue(request.departmentId().isPresent());
        assertTrue(request.role().isPresent());

        assertNull(request.fullName().orElse(null));
        assertNull(request.email().orElse(null));
        assertNull(request.phone().orElse(null));
        assertNull(request.jobTitle().orElse(null));
        assertNull(request.active().orElse(null));
        assertNull(request.departmentId().orElse(null));
        assertNull(request.role().orElse(null));
    }

    @Test
    void preservesProvidedNonStringFields() {
        UpdateUserRequest request = new UpdateUserRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.of(false),
                JsonNullable.of(10L),
                JsonNullable.of(Role.ADMIN)
        );

        assertFalse(request.active().get());
        assertEquals(10L, request.departmentId().get());
        assertEquals(Role.ADMIN, request.role().get());
    }
}