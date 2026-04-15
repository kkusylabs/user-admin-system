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

    /**
     * Checks whether a department with the given name already exists.
     *
     * @param name the department name to check
     * @return {@code true} if a department with the given name exists;
     *         {@code false} otherwise
     */
    boolean existsByName(String name);

    /**
     * Checks whether a department exists with the given name excluding the specified ID.
     *
     * @param name the department name to check
     * @param id the department ID to exclude
     * @return true if another department with the same name exists; false otherwise
     */
    boolean existsByNameAndIdNot(String name, Long id);
}
