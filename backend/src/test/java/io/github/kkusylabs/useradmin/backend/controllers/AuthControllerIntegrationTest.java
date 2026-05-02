package io.github.kkusylabs.useradmin.backend.controllers;

import io.github.kkusylabs.useradmin.backend.TestcontainersConfiguration;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Department department;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        departmentRepository.deleteAll();

        department = departmentRepository.save(department("Engineering"));
    }

    @Test
    void loginReturnsBearerTokenForValidCredentials() throws Exception {
        seedUser("admin", "password123", Role.ADMIN, true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginNormalizesUsernameBeforeAuthentication() throws Exception {
        seedUser("admin", "password123", Role.ADMIN, true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "  ADMIN  ",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() throws Exception {
        seedUser("admin", "password123", Role.ADMIN, true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin",
                                "password", "wrong-password"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenAllowsAccessToProtectedEndpoint() throws Exception {
        User admin = seedUser("admin", "password123", Role.ADMIN, true);

        String token = loginAndReturnToken("admin", "password123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.content").isArray())
                .andExpect(jsonPath("$.users.content[0].user.id").value(admin.getId()))
                .andExpect(jsonPath("$.canCreate").value(true));
    }

    @Test
    void protectedEndpointRejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsTokenForInactiveUser() throws Exception {
        seedUser("admin", "password123", Role.ADMIN, false);

        String token = loginAndReturnToken("admin", "password123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndReturnToken(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthController.AuthResponse auth = objectMapper.readValue(response, AuthController.AuthResponse.class);

        String token = auth.accessToken();

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(token.split("\\.").length >= 3);

        return token;
    }

    private User seedUser(String username, String password, Role role, boolean active) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(username + " User");
        user.setEmail(username + "@example.com");
        user.setPhone("+15551234567");
        user.setJobTitle("Developer");
        user.setDepartment(department);
        user.setRole(role);
        user.setActive(active);
        return userRepository.save(user);
    }

    private static Department department(String name) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(name + " department");
        department.setActive(true);
        return department;
    }
}
