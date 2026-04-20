package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("SELECT d FROM Department d ORDER BY LOWER(d.name)")
    List<Department> findAllOrderByNameIgnoreCase();

    @Query("""
                SELECT d
                FROM Department d
                WHERE d.active = true
                ORDER BY LOWER(d.name)
            """)
    List<Department> findActiveOrderByNameIgnoreCase();
}