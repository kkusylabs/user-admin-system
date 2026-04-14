package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for performing persistence operations on {@link User} entities.
 * <p>
 * Provides standard CRUD, paging, and sorting operations through
 * {@link JpaRepository}, along with user-specific lookup methods.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the matching user if found;
     *         otherwise an empty {@link Optional}
     */
    Optional<User> findByUsername(String username);

    /**
     * Returns a paginated list of users.
     *
     * @param pageable pagination and sorting information
     * @return a page of users
     */
    @EntityGraph(attributePaths = "department")
    Page<User> findAll(Pageable pageable);

    /**
     * Checks whether a user exists with the given username.
     *
     * @param username the username to check
     * @return {@code true} if a user with the given username exists, {@code false} otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Counts the number of users with the given role that are currently active.
     *
     * @param role the role to filter by
     * @return the number of active users with the specified role
     */
    long countByRoleAndActiveTrue(Role role);
}
