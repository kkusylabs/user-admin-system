package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.config.JpaConfig;
import io.github.kkusylabs.useradmin.backend.models.Department;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import io.github.kkusylabs.useradmin.backend.TestcontainersConfiguration;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaConfig.class, TestcontainersConfiguration.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DepartmentRepositoryTest {

    @Autowired
    DepartmentRepository repository;

    @Test
    void existsByNameIgnoreCaseReturnsTrueIgnoringCase() {
        repository.save(department("Sales", true));

        assertTrue(repository.existsByNameIgnoreCase("sales"));
        assertTrue(repository.existsByNameIgnoreCase("SALES"));
    }

    @Test
    void existsByNameIgnoreCaseAndIdNotExcludesGivenDepartment() {
        Department sales = repository.save(department("Sales", true));

        assertFalse(repository.existsByNameIgnoreCaseAndIdNot("sales", sales.getId()));
    }

    @Test
    void findAllOrderByNameIgnoreCaseSortsCaseInsensitively() {
        repository.save(department("beta", true));
        repository.save(department("Alpha", true));

        assertEquals(
                List.of("Alpha", "beta"),
                repository.findAllOrderByNameIgnoreCase()
                        .stream()
                        .map(Department::getName)
                        .toList()
        );
    }

    @Test
    void findActiveOrderByNameIgnoreCaseReturnsOnlyActiveSortedByName() {
        repository.save(department("beta", true));
        repository.save(department("Alpha", true));
        repository.save(department("Gamma", false));

        assertEquals(
                List.of("Alpha", "beta"),
                repository.findActiveOrderByNameIgnoreCase()
                        .stream()
                        .map(Department::getName)
                        .toList()
        );
    }

    private static Department department(String name, boolean active) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(name + " department");
        department.setActive(active);
        return department;
    }
}
