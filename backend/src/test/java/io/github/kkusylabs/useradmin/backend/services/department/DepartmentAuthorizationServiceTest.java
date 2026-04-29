package io.github.kkusylabs.useradmin.backend.services.department;

import io.github.kkusylabs.useradmin.backend.exceptions.ValidationException;
import io.github.kkusylabs.useradmin.backend.exceptions.department.DepartmentNotEmptyException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentAuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    private DepartmentAuthorizationService service;

    private Department sales;
    private User admin;
    private User manager;
    private User basicUser;

    @BeforeEach
    void setUp() {
        service = new DepartmentAuthorizationService(userRepository);

        sales = department(1L, "Sales", true);
        admin = user(10L, Role.ADMIN, sales, true);
        manager = user(20L, Role.MANAGER, sales, true);
        basicUser = user(30L, Role.USER, sales, true);
    }

    @Nested
    class CreateAuthorization {

        @Test
        void adminCanCreateDepartment() {
            assertTrue(service.canCreate(admin));
            assertDoesNotThrow(() -> service.validateCreateRequest(admin));
        }

        @Test
        void managerCannotCreateDepartment() {
            assertFalse(service.canCreate(manager));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(manager));
        }

        @Test
        void basicUserCannotCreateDepartment() {
            assertFalse(service.canCreate(basicUser));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(basicUser));
        }
    }

    @Nested
    class UpdateAuthorization {

        @Test
        void adminCanUpdateDepartment() {
            assertTrue(service.canUpdate(admin, sales));
            assertDoesNotThrow(() -> service.validateUpdateRequest(admin, sales));
        }

        @Test
        void managerCannotUpdateDepartment() {
            assertFalse(service.canUpdate(manager, sales));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(manager, sales));
        }

        @Test
        void basicUserCannotUpdateDepartment() {
            assertFalse(service.canUpdate(basicUser, sales));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(basicUser, sales));
        }

        @Test
        void validateUpdateRequestRequiresDepartment() {
            assertThrows(ValidationException.class,
                    () -> service.validateUpdateRequest(admin, null));
        }

        @Test
        void canUpdateOnlyChecksActorRoleAndDoesNotRequireDepartment() {
            assertTrue(service.canUpdate(admin, null));
            assertFalse(service.canUpdate(manager, null));
        }
    }

    @Nested
    class DeleteAuthorization {

        @Test
        void adminCanDeleteEmptyDepartment() {
            when(userRepository.existsByDepartmentId(sales.getId())).thenReturn(false);

            assertTrue(service.canDelete(admin, sales));
            assertDoesNotThrow(() -> service.validateDeleteRequest(admin, sales));

            verify(userRepository, times(2)).existsByDepartmentId(sales.getId());
        }

        @Test
        void adminCannotDeleteNonEmptyDepartment() {
            when(userRepository.existsByDepartmentId(sales.getId())).thenReturn(true);

            assertFalse(service.canDelete(admin, sales));
            assertThrows(DepartmentNotEmptyException.class,
                    () -> service.validateDeleteRequest(admin, sales));

            verify(userRepository, times(2)).existsByDepartmentId(sales.getId());
        }

        @Test
        void managerCannotDeleteDepartmentEvenWhenEmpty() {
            assertFalse(service.canDelete(manager, sales));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateDeleteRequest(manager, sales));

            verifyNoInteractions(userRepository);
        }

        @Test
        void basicUserCannotDeleteDepartmentEvenWhenEmpty() {
            assertFalse(service.canDelete(basicUser, sales));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateDeleteRequest(basicUser, sales));

            verifyNoInteractions(userRepository);
        }

        @Test
        void validateDeleteRequestRequiresDepartment() {
            assertThrows(ValidationException.class,
                    () -> service.validateDeleteRequest(admin, null));

            verifyNoInteractions(userRepository);
        }

        @Test
        void canDeleteReturnsFalseForNullDepartment() {
            assertFalse(service.canDelete(admin, null));

            verifyNoInteractions(userRepository);
        }
    }

    @Nested
    class DeletableChecks {

        @Test
        void departmentIsDeletableWhenNoUsersAreAssigned() {
            when(userRepository.existsByDepartmentId(sales.getId())).thenReturn(false);

            assertTrue(service.isDeletable(sales));

            verify(userRepository).existsByDepartmentId(sales.getId());
        }

        @Test
        void departmentIsNotDeletableWhenUsersAreAssigned() {
            when(userRepository.existsByDepartmentId(sales.getId())).thenReturn(true);

            assertFalse(service.isDeletable(sales));

            verify(userRepository).existsByDepartmentId(sales.getId());
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
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(active);
        return user;
    }

    private static void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Could not set id on " + target.getClass().getSimpleName(), ex);
        }
    }
}
