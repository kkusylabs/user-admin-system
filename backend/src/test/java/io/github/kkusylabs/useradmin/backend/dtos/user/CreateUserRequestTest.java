package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.models.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestTest {

    @Test
    void normalizesAllFields() {
        CreateUserRequest request = new CreateUserRequest(
                "  John.Doe_1  ",
                "password123",
                "  John Doe  ",
                "  JOHN@EXAMPLE.COM  ",
                "  +1 (555) 123-4567  ",
                "  Developer  ",
                1L,
                Role.USER
        );

        assertEquals("john.doe_1", request.username());
        assertEquals("John Doe", request.fullName());
        assertEquals("john@example.com", request.email());
        assertEquals("+15551234567", request.phone());
        assertEquals("Developer", request.jobTitle());
    }

    @Test
    void convertsBlankOptionalFieldsToNull() {
        CreateUserRequest request = new CreateUserRequest(
                "jdoe",
                "password123",
                "John Doe",
                "john@example.com",
                "   ",
                "   ",
                1L,
                Role.USER
        );

        assertNull(request.phone());
        assertNull(request.jobTitle());
    }

    @Test
    void preservesValidValuesWithoutChange() {
        CreateUserRequest request = new CreateUserRequest(
                "jdoe",
                "password123",
                "John Doe",
                "john@example.com",
                "+15551234567",
                "Developer",
                1L,
                Role.USER
        );

        assertEquals("jdoe", request.username());
        assertEquals("John Doe", request.fullName());
        assertEquals("john@example.com", request.email());
        assertEquals("+15551234567", request.phone());
        assertEquals("Developer", request.jobTitle());
    }

    @Test
    void handlesNullOptionalFields() {
        CreateUserRequest request = new CreateUserRequest(
                "jdoe",
                "password123",
                "John Doe",
                "john@example.com",
                null,
                null,
                1L,
                Role.USER
        );

        assertNull(request.phone());
        assertNull(request.jobTitle());
    }
}