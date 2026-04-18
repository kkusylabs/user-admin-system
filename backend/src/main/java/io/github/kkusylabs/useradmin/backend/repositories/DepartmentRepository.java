package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.dtos.department.DepartmentSummary;
import io.github.kkusylabs.useradmin.backend.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Department} persistence and lookups.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Finds a department by its unique name.
     *
     * @param name the department name
     * @return the matching department, if present
     */
    Optional<Department> findByName(String name);

    /**
     * Checks if a department name already exists (case-insensitive),
     * excluding a specific department id.
     */
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    /**
     * Checks if a department name already exists (case-insensitive).
     */
    boolean existsByNameIgnoreCase(String name);

    List<DepartmentSummary> findAllBy();
}