package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentOption;
import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.user.EditUserResponse;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.dtos.user.UserDetailResponse;
import io.github.kkusylabs.useradmin.backend.dtos.user.UserListItemResponse;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper mapper;

    private Department sales;
    private Department engineering;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
        sales = department(1L, "Sales", true);
        engineering = department(2L, "Engineering", true);
    }

    @Nested
    class FromCreateRequest {

        @Test
        void mapsCreateRequestToUserEntity() {
            CreateUserRequest request = new CreateUserRequest(
                    "jdoe",
                    "password123",
                    "John Doe",
                    "john.doe@example.com",
                    "+15551234567",
                    "Sales Representative",
                    sales.getId(),
                    Role.USER
            );

            User user = mapper.fromCreateRequest(request, sales);

            assertNull(user.getId());
            assertEquals("jdoe", user.getUsername());
            assertNull(user.getPasswordHash(), "Password hashing is handled outside the mapper");
            assertEquals("John Doe", user.getFullName());
            assertEquals("john.doe@example.com", user.getEmail());
            assertEquals("+15551234567", user.getPhone());
            assertEquals("Sales Representative", user.getJobTitle());
            assertEquals(Role.USER, user.getRole());
            assertSame(sales, user.getDepartment());
            assertTrue(user.isActive(), "New User entities should keep their default active value");
        }

        @Test
        void mapsCreateRequestWithNullOptionalFields() {
            CreateUserRequest request = new CreateUserRequest(
                    "jdoe",
                    "password123",
                    "John Doe",
                    "john.doe@example.com",
                    null,
                    null,
                    sales.getId(),
                    Role.USER
            );

            User user = mapper.fromCreateRequest(request, sales);

            assertNull(user.getPhone());
            assertNull(user.getJobTitle());
            assertSame(sales, user.getDepartment());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void appliesOnlyProvidedUpdateFields() {
            User user = user(10L, Role.USER, sales, true);

            UpdateUserRequest request = new UpdateUserRequest(
                    JsonNullable.of("Jane Doe"),
                    JsonNullable.of("jane.doe@example.com"),
                    JsonNullable.of("+15557654321"),
                    JsonNullable.of("Engineering Lead"),
                    JsonNullable.of(false),
                    JsonNullable.of(engineering.getId()),
                    JsonNullable.of(Role.MANAGER)
            );

            mapper.updateUser(user, request, engineering);

            assertEquals("Jane Doe", user.getFullName());
            assertEquals("jane.doe@example.com", user.getEmail());
            assertEquals("+15557654321", user.getPhone());
            assertEquals("Engineering Lead", user.getJobTitle());
            assertFalse(user.isActive());
            assertEquals(Role.MANAGER, user.getRole());
            assertSame(engineering, user.getDepartment());
        }

        @Test
        void leavesUndefinedFieldsUnchanged() {
            User user = user(10L, Role.USER, sales, true);

            UpdateUserRequest request = new UpdateUserRequest(
                    JsonNullable.of("Jane Doe"),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined()
            );

            mapper.updateUser(user, request, null);

            assertEquals("Jane Doe", user.getFullName());
            assertEquals("user10@example.com", user.getEmail());
            assertEquals("+15550000010", user.getPhone());
            assertEquals("Original Job Title", user.getJobTitle());
            assertTrue(user.isActive());
            assertEquals(Role.USER, user.getRole());
            assertSame(sales, user.getDepartment());
        }

        @Test
        void clearsNullableOptionalFieldsWhenExplicitNullIsProvided() {
            User user = user(10L, Role.USER, sales, true);

            UpdateUserRequest request = new UpdateUserRequest(
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.of(null),
                    JsonNullable.of(null),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined()
            );

            mapper.updateUser(user, request, null);

            assertNull(user.getPhone());
            assertNull(user.getJobTitle());
            assertEquals("User 10", user.getFullName());
            assertEquals("user10@example.com", user.getEmail());
            assertSame(sales, user.getDepartment());
        }

        @Test
        void assignsNullDepartmentWhenDepartmentIdIsProvidedAndRequestedDepartmentIsNull() {
            User user = user(10L, Role.USER, sales, true);

            UpdateUserRequest request = new UpdateUserRequest(
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.undefined(),
                    JsonNullable.of(99L),
                    JsonNullable.undefined()
            );

            mapper.updateUser(user, request, null);

            assertNull(user.getDepartment());
        }
    }

    @Nested
    class Responses {

        @Test
        void mapsUserToDetailResponseWithDepartmentOption() {
            User user = user(10L, Role.MANAGER, sales, false);

            UserDetailResponse response = mapper.toDetailResponse(user);

            assertEquals(10L, response.id());
            assertEquals("user10", response.username());
            assertEquals("User 10", response.fullName());
            assertEquals("user10@example.com", response.email());
            assertEquals("+15550000010", response.phone());
            assertEquals("Original Job Title", response.jobTitle());
            assertFalse(response.active());
            assertEquals(Role.MANAGER, response.role());
            assertNotNull(response.department());
            assertEquals(1L, response.department().id());
            assertEquals("Sales", response.department().name());
        }

        @Test
        void mapsUserToDetailResponseWithNullDepartment() {
            User user = user(10L, Role.USER, null, true);

            UserDetailResponse response = mapper.toDetailResponse(user);

            assertNull(response.department());
        }

        @Test
        void mapsUserToListItemResponse() {
            User user = user(10L, Role.USER, sales, true);

            UserListItemResponse response = mapper.toListItemResponse(user, true, false);

            assertEquals(10L, response.user().id());
            assertTrue(response.canUpdate());
            assertFalse(response.canDelete());
        }

        @Test
        void mapsUserAndCapabilitiesToEditResponse() {
            User user = user(10L, Role.USER, sales, true);
            UpdateUserCapabilities capabilities = new UpdateUserCapabilities(
                    true,
                    true,
                    true,
                    false,
                    false,
                    true,
                    Set.of(),
                    List.of(new DepartmentOption(sales.getId(), sales.getName())),
                    null
            );

            EditUserResponse response = mapper.toEditResponse(user, capabilities);

            assertEquals(10L, response.user().id());
            assertSame(capabilities, response.updateCapabilities());
        }
    }

    private static Department department(Long id, String name, boolean active) {
        Department department = new Department();
        setId(department, id);
        department.setName(name);
        department.setActive(active);
        return department;
    }

    private static User user(Long id, Role role, Department department, boolean active) {
        User user = new User();
        setId(user, id);
        user.setUsername("user" + id);
        user.setPasswordHash("password-hash");
        user.setFullName("User " + id);
        user.setEmail("user" + id + "@example.com");
        user.setPhone("+155500000" + id);
        user.setJobTitle("Original Job Title");
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(active);
        return user;
    }

    private static void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException("Unable to set id for test entity", exception);
        }
    }
}
