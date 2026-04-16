package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for performing persistence operations on {@link Department} entities.
 * <p>
 * Provides standard CRUD, paging, and sorting operations through
 * {@link JpaRepository}, along with department-specific lookup methods.
 */
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Finds a department by its unique name.
     *
     * @param name the department name to search for
     * @return an {@link Optional} containing the matching department if found;
     *         otherwise an empty {@link Optional}
     */
    Optional<Department> findByName(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByNameIgnoreCase(String name);
}
