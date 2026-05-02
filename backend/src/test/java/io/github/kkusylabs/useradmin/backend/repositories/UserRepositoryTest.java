package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.TestcontainersConfiguration;
import io.github.kkusylabs.useradmin.backend.config.JpaConfig;
import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, TestcontainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void findByUsernameReturnsUser() {
        Department sales = departmentRepository.save(department("Sales"));
        User user = user("jdoe", "jdoe@example.com", Role.USER, sales, true);
        userRepository.save(user);

        assertTrue(userRepository.findByUsername("jdoe").isPresent());
        assertEquals("jdoe@example.com", userRepository.findByUsername("jdoe").orElseThrow().getEmail());
    }

    @Test
    void existsByUsernameReturnsTrueWhenUsernameExists() {
        Department sales = departmentRepository.save(department("Sales"));
        userRepository.save(user("jdoe", "jdoe@example.com", Role.USER, sales, true));

        assertTrue(userRepository.existsByUsername("jdoe"));
        assertFalse(userRepository.existsByUsername("missing"));
    }

    @Test
    void existsByEmailReturnsTrueWhenEmailExists() {
        Department sales = departmentRepository.save(department("Sales"));
        userRepository.save(user("jdoe", "jdoe@example.com", Role.USER, sales, true));

        assertTrue(userRepository.existsByEmail("jdoe@example.com"));
        assertFalse(userRepository.existsByEmail("missing@example.com"));
    }

    @Test
    void countByRoleAndActiveTrueCountsOnlyActiveUsersWithRole() {
        Department sales = departmentRepository.save(department("Sales"));

        userRepository.save(user("admin1", "admin1@example.com", Role.ADMIN, sales, true));
        userRepository.save(user("admin2", "admin2@example.com", Role.ADMIN, sales, true));
        userRepository.save(user("admin3", "admin3@example.com", Role.ADMIN, sales, false));
        userRepository.save(user("manager1", "manager1@example.com", Role.MANAGER, sales, true));

        assertEquals(2L, userRepository.countByRoleAndActiveTrue(Role.ADMIN));
        assertEquals(1L, userRepository.countByRoleAndActiveTrue(Role.MANAGER));
        assertEquals(0L, userRepository.countByRoleAndActiveTrue(Role.USER));
    }

    @Test
    void existsByDepartmentIdReturnsTrueWhenUsersBelongToDepartment() {
        Department sales = departmentRepository.save(department("Sales"));
        Department engineering = departmentRepository.save(department("Engineering"));

        userRepository.save(user("jdoe", "jdoe@example.com", Role.USER, sales, true));

        assertTrue(userRepository.existsByDepartmentId(sales.getId()));
        assertFalse(userRepository.existsByDepartmentId(engineering.getId()));
    }

    @Test
    void findAllReturnsPagedUsersWithDepartments() {
        Department sales = departmentRepository.save(department("Sales"));
        Department engineering = departmentRepository.save(department("Engineering"));

        userRepository.save(user("jdoe", "jdoe@example.com", Role.USER, sales, true));
        userRepository.save(user("asmith", "asmith@example.com", Role.MANAGER, engineering, true));

        Page<User> page = userRepository.findAll(PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());

        page.getContent().forEach(user -> {
            assertNotNull(user.getDepartment());
            assertNotNull(user.getDepartment().getName());
        });
    }

    private static Department department(String name) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(name + " department");
        department.setActive(true);
        return department;
    }

    private static User user(
            String username,
            String email,
            Role role,
            Department department,
            boolean active
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("password-hash");
        user.setFullName("Test User");
        user.setEmail(email);
        user.setPhone("+15551234567");
        user.setJobTitle("Developer");
        user.setRole(role);
        user.setDepartment(department);
        user.setActive(active);
        return user;
    }
}
