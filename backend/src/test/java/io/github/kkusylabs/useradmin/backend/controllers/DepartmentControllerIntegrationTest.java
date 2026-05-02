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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class DepartmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private Department sales;
    private Department engineering;
    private User admin;
    private User manager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        sales = departmentRepository.save(department("Sales", "Sales department", true));
        engineering = departmentRepository.save(department("Engineering", "Engineering department", true));

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
    }

    @Test
    void getDepartmentsReturnsDepartmentsForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments", hasSize(2)))
                .andExpect(jsonPath("$.departments[0].department.name").value("Engineering"))
                .andExpect(jsonPath("$.departments[1].department.name").value("Sales"))
                .andExpect(jsonPath("$.canCreate").value(true));
    }

    @Test
    void getDepartmentReturnsDepartmentForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/departments/{id}", sales.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department.id").value(sales.getId()))
                .andExpect(jsonPath("$.department.name").value("Sales"))
                .andExpect(jsonPath("$.department.description").value("Sales department"))
                .andExpect(jsonPath("$.department.active").value(true))
                .andExpect(jsonPath("$.canUpdate").value(true))
                .andExpect(jsonPath("$.canDelete").value(false));
    }

    @Test
    void createDepartmentPersistsDepartmentWhenAdminIsAuthenticated() throws Exception {
        String requestJson = """
                {
                  "name": "  Support  ",
                  "description": "  Customer support  "
                }
                """;

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.department.name").value("Support"))
                .andExpect(jsonPath("$.department.description").value("Customer support"))
                .andExpect(jsonPath("$.canUpdate").value(true))
                .andExpect(jsonPath("$.canDelete").value(true));

        Department saved = departmentRepository.findByName("Support").orElseThrow();
        assertEquals("Customer support", saved.getDescription());
    }

    @Test
    void createDepartmentReturnsForbiddenWhenManagerIsAuthenticated() throws Exception {
        String requestJson = """
                {
                  "name": "Support",
                  "description": "Customer support"
                }
                """;

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        assertTrue(departmentRepository.findByName("Support").isEmpty());
    }

    @Test
    void createDepartmentReturnsConflictForDuplicateName() throws Exception {
        String requestJson = """
                {
                  "name": "sales",
                  "description": "Duplicate sales department"
                }
                """;

        mockMvc.perform(post("/api/departments")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEPARTMENT_NAME_ALREADY_EXISTS"));
    }

    @Test
    void updateDepartmentUpdatesDepartmentWhenAdminIsAuthenticated() throws Exception {
        String requestJson = """
                {
                  "name": "  Product Engineering  ",
                  "description": "  Builds the product  ",
                  "active": false
                }
                """;

        mockMvc.perform(put("/api/departments/{id}", engineering.getId())
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department.id").value(engineering.getId()))
                .andExpect(jsonPath("$.department.name").value("Product Engineering"))
                .andExpect(jsonPath("$.department.description").value("Builds the product"))
                .andExpect(jsonPath("$.department.active").value(false));

        Department updated = departmentRepository.findById(engineering.getId()).orElseThrow();
        assertEquals("Product Engineering", updated.getName());
        assertEquals("Builds the product", updated.getDescription());
        assertFalse(updated.isActive());
    }

    @Test
    void updateDepartmentReturnsForbiddenWhenManagerIsAuthenticated() throws Exception {
        String requestJson = """
                {
                  "name": "Product Engineering",
                  "description": "Builds the product",
                  "active": true
                }
                """;

        mockMvc.perform(put("/api/departments/{id}", engineering.getId())
                        .header("Authorization", bearerToken(manager))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isForbidden());

        Department unchanged = departmentRepository.findById(engineering.getId()).orElseThrow();
        assertEquals("Engineering", unchanged.getName());
    }

    @Test
    void deleteDepartmentDeletesEmptyDepartmentWhenAdminIsAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", engineering.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNoContent());

        assertTrue(departmentRepository.findById(engineering.getId()).isEmpty());
    }

    @Test
    void deleteDepartmentReturnsConflictWhenDepartmentContainsUsers() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", sales.getId())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEPARTMENT_NOT_EMPTY"));

        assertTrue(departmentRepository.findById(sales.getId()).isPresent());
    }

    @Test
    void deleteDepartmentReturnsForbiddenWhenManagerIsAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/departments/{id}", engineering.getId())
                        .header("Authorization", bearerToken(manager)))
                .andExpect(status().isForbidden());

        assertTrue(departmentRepository.findById(engineering.getId()).isPresent());
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private static Department department(String name, String description, boolean active) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
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
