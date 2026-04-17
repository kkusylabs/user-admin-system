package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link User} persistence and lookups.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return the matching user, if present
     */
    Optional<User> findByUsername(String username);

    /**
     * Returns a page of users with their department eagerly loaded.
     */
    @EntityGraph(attributePaths = "department")
    Page<User> findAll(Pageable pageable);

    /**
     * Checks if a user exists with the given username.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Counts active users with the given role.
     */
    long countByRoleAndActiveTrue(Role role);

    /**
     * Checks if any users belong to the given department.
     */
    boolean existsByDepartmentId(Long departmentId);
}