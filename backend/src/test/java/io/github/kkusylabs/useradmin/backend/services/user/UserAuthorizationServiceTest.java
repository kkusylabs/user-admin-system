package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.dtos.user.CreateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserCapabilities;
import io.github.kkusylabs.useradmin.backend.dtos.user.UpdateUserRequest;
import io.github.kkusylabs.useradmin.backend.exceptions.department.InactiveDepartmentException;
import io.github.kkusylabs.useradmin.backend.exceptions.security.InsufficientPermissionsException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.LastActiveAdminDeletionException;
import io.github.kkusylabs.useradmin.backend.exceptions.user.LastActiveAdminUpdateException;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static io.github.kkusylabs.useradmin.backend.testsupport.UpdateUserRequests.*;

@ExtendWith(MockitoExtension.class)
class UserAuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private UserAuthorizationService service;

    private Department sales;
    private Department engineering;
    private Department inactiveDepartment;

    @BeforeEach
    void setUp() {
        service = new UserAuthorizationService(userRepository, departmentRepository);

        sales = department(1L, "Sales", true);
        engineering = department(2L, "Engineering", true);
        inactiveDepartment = department(3L, "Inactive", false);
    }

    @Nested
    class CreateAuthorization {

        @Test
        void adminCanCreateAnyRoleInActiveDepartment() {
            User admin = user(10L, Role.ADMIN, sales, true);

            assertDoesNotThrow(() -> service.validateCreateRequest(admin, Role.ADMIN, engineering));
            assertDoesNotThrow(() -> service.validateCreateRequest(admin, Role.MANAGER, engineering));
            assertDoesNotThrow(() -> service.validateCreateRequest(admin, Role.USER, engineering));
        }

        @Test
        void adminCannotCreateUserInInactiveDepartment() {
            User admin = user(10L, Role.ADMIN, sales, true);

            assertThrows(InactiveDepartmentException.class,
                    () -> service.validateCreateRequest(admin, Role.USER, inactiveDepartment));
        }

        @Test
        void managerCanCreateBasicUserInOwnActiveDepartment() {
            User manager = user(20L, Role.MANAGER, sales, true);

            assertDoesNotThrow(() -> service.validateCreateRequest(manager, Role.USER, sales));
        }

        @Test
        void managerCannotCreateUserInAnotherDepartment() {
            User manager = user(20L, Role.MANAGER, sales, true);

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(manager, Role.USER, engineering));
        }

        @Test
        void managerCannotAssignPrivilegedRole() {
            User manager = user(20L, Role.MANAGER, sales, true);

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(manager, Role.MANAGER, sales));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(manager, Role.ADMIN, sales));
        }

        @Test
        void managerWithoutActiveDepartmentCannotCreateUsers() {
            User managerWithInactiveDepartment = user(20L, Role.MANAGER, inactiveDepartment, true);
            User managerWithoutDepartment = user(21L, Role.MANAGER, null, true);

            assertFalse(service.canCreate(managerWithInactiveDepartment));
            assertFalse(service.canCreate(managerWithoutDepartment));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(managerWithInactiveDepartment, Role.USER, inactiveDepartment));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(managerWithoutDepartment, Role.USER, sales));
        }

        @Test
        void basicUserCannotCreateUsers() {
            User actor = user(30L, Role.USER, sales, true);

            assertFalse(service.canCreate(actor));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateCreateRequest(actor, Role.USER, sales));
        }

        @Test
        void createCapabilitiesForAdminIncludeAllRolesAndActiveDepartments() {
            User admin = user(10L, Role.ADMIN, sales, true);
            when(departmentRepository.findActiveOrderByNameIgnoreCase()).thenReturn(List.of(engineering, sales));

            CreateUserCapabilities capabilities = service.getCreateCapabilities(admin);

            assertTrue(capabilities.canCreate());
            assertEquals(Set.of(Role.ADMIN, Role.MANAGER, Role.USER), capabilities.assignableRoles());
            assertEquals(List.of(engineering.getId(), sales.getId()),
                    capabilities.assignableDepartments().stream().map(option -> option.id()).toList());
        }

        @Test
        void createCapabilitiesForManagerAreLimitedToOwnDepartmentAndUserRole() {
            User manager = user(20L, Role.MANAGER, sales, true);

            CreateUserCapabilities capabilities = service.getCreateCapabilities(manager);

            assertTrue(capabilities.canCreate());
            assertEquals(Set.of(Role.USER), capabilities.assignableRoles());
            assertEquals(List.of(sales.getId()),
                    capabilities.assignableDepartments().stream().map(option -> option.id()).toList());
            verifyNoInteractions(departmentRepository);
        }
    }

    @Nested
    class DeleteAuthorization {

        @Test
        void adminCanDeleteAnotherUser() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(30L, Role.USER, engineering, true);

            assertTrue(service.canDelete(admin, target));
            assertDoesNotThrow(() -> service.validateDeletionRequest(admin, target));
        }

        @Test
        void adminCannotDeleteSelf() {
            User admin = user(10L, Role.ADMIN, sales, true);

            assertFalse(service.canDelete(admin, admin));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateDeletionRequest(admin, admin));
        }

        @Test
        void managerCanDeleteBasicUserInOwnDepartmentOnly() {
            User manager = user(20L, Role.MANAGER, sales, true);
            User sameDepartmentUser = user(30L, Role.USER, sales, true);
            User otherDepartmentUser = user(31L, Role.USER, engineering, true);
            User sameDepartmentManager = user(32L, Role.MANAGER, sales, true);
            User sameDepartmentAdmin = user(33L, Role.ADMIN, sales, true);

            assertTrue(service.canDelete(manager, sameDepartmentUser));
            assertFalse(service.canDelete(manager, otherDepartmentUser));
            assertFalse(service.canDelete(manager, sameDepartmentManager));
            assertFalse(service.canDelete(manager, sameDepartmentAdmin));
        }

        @Test
        void basicUserCannotDeleteUsers() {
            User actor = user(30L, Role.USER, sales, true);
            User target = user(31L, Role.USER, sales, true);

            assertFalse(service.canDelete(actor, target));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateDeletionRequest(actor, target));
        }

        @Test
        void cannotDeleteLastActiveAdmin() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(11L, Role.ADMIN, engineering, true);
            when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

            assertThrows(LastActiveAdminDeletionException.class,
                    () -> service.validateDeletionRequest(admin, target));
        }

    }

    @Nested
    class UpdateAuthorization {

        @Test
        void adminCanUpdateAnotherUserAllFields() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(30L, Role.USER, engineering, true);
            UpdateUserRequest request = updateRequest()
                    .fullName("Updated Name")
                    .email("updated@example.com")
                    .phone("+15551234567")
                    .jobTitle("Lead")
                    .active(false)
                    .departmentId(sales.getId())
                    .role(Role.MANAGER).build();

            assertDoesNotThrow(() -> service.validateUpdateRequest(admin, target, request, sales));
        }

        @Test
        void adminSelfCanEditProfileJobTitleAndDepartmentButNotRoleOrActive() {
            User admin = user(10L, Role.ADMIN, sales, true);
            UpdateUserPolicy policy = service.getUpdatePolicy(admin, admin);

            assertTrue(policy.canUpdate());
            assertTrue(policy.canEditProfile());
            assertTrue(policy.canEditJobTitle());
            assertTrue(policy.canEditDepartment());
            assertFalse(policy.canEditRole());
            assertFalse(policy.canEditActive());

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    admin, admin, withDepartment(engineering.getId()), engineering));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(admin, admin, withRole(Role.USER), sales));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(admin, admin, withActive(false), sales));
        }

        @Test
        void managerCanUpdateManagedBasicUserProfileJobTitleAndActive() {
            User manager = user(20L, Role.MANAGER, sales, true);
            User target = user(30L, Role.USER, sales, true);

            UpdateUserRequest request = updateRequest()
                    .fullName("Updated Name")
                    .email("updated@example.com")
                    .phone("+15551234567")
                    .jobTitle("Specialist")
                    .active(false).build();

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    manager,
                    target,
                    request,
                    sales
            ));
        }

        @Test
        void managerCanDeactivateBasicUserInOwnDepartment() {
            User manager = user(20L, Role.MANAGER, sales, true);
            User target = user(30L, Role.USER, sales, true);

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    manager,
                    target,
                    withActive(false),
                    sales
            ));
        }

        @Test
        void managerCanActivateBasicUserInOwnDepartment() {
            User manager = user(20L, Role.MANAGER, sales, true);
            User target = user(30L, Role.USER, sales, false);

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    manager,
                    target,
                    withActive(true),
                    sales
            ));
        }

        @Test
        void managerCannotUpdateRoleOrDepartment() {
            User manager = user(20L, Role.MANAGER, sales, true);
            User target = user(30L, Role.USER, sales, true);

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(manager, target, withRole(Role.MANAGER), sales));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(manager, target, withDepartment(engineering.getId()), engineering));
        }

        @Test
        void managerCannotUpdateUserOutsideOwnDepartmentOrPrivilegedUser() {
            User manager = user(20L, Role.MANAGER, sales, true);
            User userInOtherDepartment = user(30L, Role.USER, engineering, true);
            User managerInSameDepartment = user(31L, Role.MANAGER, sales, true);

            assertFalse(service.canUpdate(manager, userInOtherDepartment));
            assertFalse(service.canUpdate(manager, managerInSameDepartment));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(manager, userInOtherDepartment, withFullName("Updated"), engineering));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(manager, managerInSameDepartment, withFullName("Updated"), sales));
        }

        @Test
        void basicUserCanUpdateOwnProfileOnly() {
            User actor = user(30L, Role.USER, sales, true);

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    actor,
                    actor,
                    withProfile("Updated Name", "updated@example.com", "+15551234567"),
                    sales
            ));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(actor, actor, withJobTitle("Lead"), sales));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(actor, actor, withRole(Role.ADMIN), sales));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(actor, actor, withDepartment(engineering.getId()), engineering));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(actor, actor, withActive(false), sales));
        }

        @Test
        void basicUserCannotUpdateAnotherUser() {
            User actor = user(30L, Role.USER, sales, true);
            User target = user(31L, Role.USER, sales, true);

            assertFalse(service.canUpdate(actor, target));
            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(actor, target, withFullName("Updated"), sales));
        }

        @Test
        void cannotChangeUserToInactiveDepartment() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(30L, Role.USER, sales, true);

            assertThrows(InactiveDepartmentException.class,
                    () -> service.validateUpdateRequest(admin, target, withDepartment(inactiveDepartment.getId()), inactiveDepartment));
        }

        @Test
        void cannotDeactivateLastActiveAdmin() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(11L, Role.ADMIN, engineering, true);
            when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

            assertThrows(LastActiveAdminUpdateException.class,
                    () -> service.validateUpdateRequest(admin, target, withActive(false), engineering));
        }

        @Test
        void cannotDemoteLastActiveAdmin() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(11L, Role.ADMIN, engineering, true);
            when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

            assertThrows(LastActiveAdminUpdateException.class,
                    () -> service.validateUpdateRequest(admin, target, withRole(Role.USER), engineering));
        }

        @Test
        void lastActiveAdminCanBeUpdatedIfStillActiveAdmin() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(11L, Role.ADMIN, engineering, true);
            when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);

            UpdateUserRequest request = updateRequest()
                    .jobTitle("Principal Admin")
                    .active(true)
                    .role(Role.ADMIN).build();

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    admin,
                    target,
                    request,
                    engineering
            ));
        }

        @Test
        void updateCapabilitiesDisableRoleAndActiveForLastActiveAdmin() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(11L, Role.ADMIN, engineering, true);
            when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(1L);
            when(departmentRepository.findActiveOrderByNameIgnoreCase()).thenReturn(List.of(sales, engineering));

            UpdateUserCapabilities capabilities = service.getUpdateCapabilities(admin, target);

            assertTrue(capabilities.canUpdate());
            assertTrue(capabilities.canEditProfile());
            assertTrue(capabilities.canEditJobTitle());
            assertTrue(capabilities.canEditDepartment());
            assertFalse(capabilities.canEditRole());
            assertFalse(capabilities.canEditActive());
            assertEquals(Set.of(Role.ADMIN), capabilities.roleOptions());
        }

        @Test
        void adminCanActivateAnotherUserButNotSelf() {
            User admin = user(10L, Role.ADMIN, sales, true);
            User target = user(30L, Role.USER, sales, false);

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    admin,
                    target,
                    withActive(true),
                    sales
            ));

            assertThrows(InsufficientPermissionsException.class,
                    () -> service.validateUpdateRequest(admin, admin, withActive(true), sales));
        }

        @Test
        void adminCanChangeOwnJobTitle() {
            User admin = user(10L, Role.ADMIN, sales, true);

            assertDoesNotThrow(() -> service.validateUpdateRequest(
                    admin,
                    admin,
                    withJobTitle("Chief Administrator"),
                    sales
            ));
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
