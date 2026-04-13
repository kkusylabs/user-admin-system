package io.github.kkusylabs.useradmin.backend.repositories;

import io.github.kkusylabs.useradmin.backend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<User> findAll(Pageable pageable);
}
