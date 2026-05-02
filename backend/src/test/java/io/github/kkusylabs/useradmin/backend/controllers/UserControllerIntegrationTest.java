package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.TestcontainersConfiguration;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import io.github.kkusylabs.useradmin.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JwtService jwtService;

    private Department sales;
    private Department engineering;
    private User admin;
    private User manager;
    private User basicUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        sales = departmentRepository.save(department("Sales", true));
        engineering = departmentRepository.save(department("Engineering", true));

        admin = userRepository.save(user(
                "admin",
                "admin@example.com",
                "System Admin",
                Role.ADMIN,
                sales,
                true
        ));

        manager = userRepository.save(user(
                "manager",
                "manager@example.com",
                "Sales Manager",
                Role.MANAGER,
                sales,
                true
        ));

        basicUser = userRepository.save(user(
                "basicuser",
                "basic@example.com",
                "Basic User",
                Role.USER,
                sales,
                true
        ));
    }

    @Test
    void getUsersReturnsPagedUsersForAuthenticatedAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.content", hasSize(3)))
                .andExpect(jsonPath("$.users.totalElements").value(3))
                .andExpect(jsonPath("$.canCreate").value(true));
    }

    @Test
    void getUserReturnsUserWithCapabilitiesForAuthenticatedAdmin() throws Exception {
        mockMvc.perform(get("/api/users/{id}", basicUser.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(basicUser.getId()))
                .andExpect(jsonPath("$.user.username").value("basicuser"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.canUpdate").value(true))
                .andExpect(jsonPath("$.canDelete").value(true));
    }

    @Test
    void getUserEditDataReturnsUserAndUpdateCapabilities() throws Exception {
        mockMvc.perform(get("/api/users/{id}/edit", basicUser.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(basicUser.getId()))
                .andExpect(jsonPath("$.user.username").value("basicuser"))
                .andExpect(jsonPath("$.updateCapabilities.canUpdate").value(true))
                .andExpect(jsonPath("$.updateCapabilities.canEditProfile").value(true))
                .andExpect(jsonPath("$.updateCapabilities.canEditJobTitle").value(true))
                .andExpect(jsonPath("$.updateCapabilities.canEditRole").value(true))
                .andExpect(jsonPath("$.updateCapabilities.canEditDepartment").value(true))
                .andExpect(jsonPath("$.updateCapabilities.canEditActive").value(true))
                .andExpect(jsonPath("$.updateCapabilities.roleOptions", containsInAnyOrder("ADMIN", "MANAGER", "USER")))
                .andExpect(jsonPath("$.updateCapabilities.departmentOptions", hasSize(2)));
    }

    @Test
    void getCreateUserCapabilitiesReturnsCapabilitiesForAdmin() throws Exception {
        mockMvc.perform(get("/api/users/create-capabilities")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canCreate").value(true))
                .andExpect(jsonPath("$.assignableRoles", containsInAnyOrder("ADMIN", "MANAGER", "USER")))
                .andExpect(jsonPath("$.assignableDepartments", hasSize(2)))
                .andExpect(jsonPath("$.assignableDepartments[*].name", containsInAnyOrder("Engineering", "Sales")));
    }

    @Test
    void getDeleteUserCapabilitiesReturnsCapabilitiesForAdminDeletingAnotherUser() throws Exception {
        mockMvc.perform(get("/api/users/delete-capabilities/{id}", basicUser.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDelete").value(true))
                .andExpect(jsonPath("$.reason").doesNotExist());
    }

    @Test
    void createUserPersistsUserWhenAdminIsAuthenticated() throws Exception {
        String requestJson = """
                {
                  "username": "new.user",
                  "password": "password123",
                  "fullName": "New User",
                  "email": "new.user@example.com",
                  "phone": "+15551234567",
                  "jobTitle": "Developer",
                  "departmentId": %d,
                  "role": "USER"
                }
                """.formatted(engineering.getId());

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value("new.user"))
                .andExpect(jsonPath("$.user.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.department.id").value(engineering.getId()))
                .andExpect(jsonPath("$.canUpdate").value(true))
                .andExpect(jsonPath("$.canDelete").value(true));

        User saved = userRepository.findByUsername("new.user").orElseThrow();
        assertEquals("new.user@example.com", saved.getEmail());
        assertEquals(Role.USER, saved.getRole());
        assertEquals(engineering.getId(), saved.getDepartment().getId());
        assertNotEquals("password123", saved.getPasswordHash());
    }

    @Test
    void createUserReturnsForbiddenWhenManagerTriesToCreateManager() throws Exception {
        String requestJson = """
                {
                  "username": "new.manager",
                  "password": "password123",
                  "fullName": "New Manager",
                  "email": "new.manager@example.com",
                  "phone": "+15551234567",
                  "jobTitle": "Manager",
                  "departmentId": %d,
                  "role": "MANAGER"
                }
                """.formatted(sales.getId());

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_PERMISSIONS"));

        assertTrue(userRepository.findByUsername("new.manager").isEmpty());
    }

    @Test
    void updateUserAllowsUserToEditOwnProfile() throws Exception {
        String requestJson = """
                {
                  "fullName": "Updated User",
                  "email": "UPDATED.USER@EXAMPLE.COM",
                  "phone": "+15557654321"
                }
                """;

        mockMvc.perform(patch("/api/users/{id}", basicUser.getId())
                        .with(csrf())
                        .header("Authorization", bearerToken(basicUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.fullName").value("Updated User"))
                .andExpect(jsonPath("$.user.email").value("updated.user@example.com"))
                .andExpect(jsonPath("$.user.phone").value("+15557654321"));

        User updated = userRepository.findById(basicUser.getId()).orElseThrow();
        assertEquals("Updated User", updated.getFullName());
        assertEquals("updated.user@example.com", updated.getEmail());
        assertEquals("+15557654321", updated.getPhone());
    }

    @Test
    void deleteUserDeletesTargetWhenAdminIsAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", basicUser.getId())
                        .with(csrf())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNoContent());

        assertTrue(userRepository.findById(basicUser.getId()).isEmpty());
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private static Department department(String name, boolean active) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(name + " department");
        department.setActive(active);
        return department;
    }

    private static User user(
            String username,
            String email,
            String fullName,
            Role role,
            Department department,
            boolean active
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash("password-hash");
        user.setPhone("+15551234567");
        user.setJobTitle("Developer");
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(active);
        return user;
    }
}
