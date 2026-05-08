package io.github.kkusylabs.useradmin.backend.services.user;

import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

/**
 * Factory methods for building {@link Specification} instances for {@link User} queries.
 *
 * <p>These specifications are intended for list and search endpoints where the caller
 * may optionally filter users by free-text search, active status, department, and role.</p>
 *
 * <p>Missing filters are ignored by returning {@link CriteriaBuilder#conjunction()},
 * which behaves as an always-true predicate.</p>
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    /**
     * Builds a combined specification for the supported user-list filters.
     *
     * <p>The resulting specification applies all provided filters together:
     * free-text search, active status, department, and role.</p>
     *
     * @param search free-text search applied to username, full name, and email;
     *               ignored when {@code null} or blank
     * @param active filters by active status; ignored when {@code null}
     * @param departmentId filters by department id; ignored when {@code null}
     * @param role filters by assigned role; ignored when {@code null}
     * @return a combined specification containing all requested predicates
     */
    public static Specification<User> filter(
            String search,
            Boolean active,
            Long departmentId,
            Role role
    ) {
        return (root, query, cb) -> cb.and(
                usernameOrNameOrEmailContainsPredicate(search, root, cb),
                activePredicate(active, root, cb),
                departmentPredicate(departmentId, root, cb),
                rolePredicate(role, root, cb)
        );
    }

    /**
     * Builds a case-insensitive free-text predicate against username,
     * full name, and email.
     *
     * @param search free-text search value; ignored when {@code null} or blank
     * @param root entity root
     * @param cb criteria builder
     * @return a matching predicate or an always-true predicate when absent
     */
    private static Predicate usernameOrNameOrEmailContainsPredicate(
            String search,
            Root<User> root,
            CriteriaBuilder cb
    ) {
        if (search == null || search.isBlank()) {
            return cb.conjunction();
        }

        String pattern = "%" + search.trim().toLowerCase() + "%";

        return cb.or(
                cb.like(cb.lower(root.get("username")), pattern),
                cb.like(cb.lower(root.get("fullName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)
        );
    }

    /**
     * Builds a predicate that filters users by active status.
     *
     * @param active required active value; ignored when {@code null}
     * @param root entity root
     * @param cb criteria builder
     * @return a matching predicate or an always-true predicate when absent
     */
    private static Predicate activePredicate(
            Boolean active,
            Root<User> root,
            CriteriaBuilder cb
    ) {
        return active == null
                ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    /**
     * Builds a predicate that filters users by department id.
     *
     * @param departmentId department identifier; ignored when {@code null}
     * @param root entity root
     * @param cb criteria builder
     * @return a matching predicate or an always-true predicate when absent
     */
    private static Predicate departmentPredicate(
            Long departmentId,
            Root<User> root,
            CriteriaBuilder cb
    ) {
        return departmentId == null
                ? cb.conjunction()
                : cb.equal(root.get("department").get("id"), departmentId);
    }

    /**
     * Builds a predicate that filters users by role.
     *
     * @param role role to match; ignored when {@code null}
     * @param root entity root
     * @param cb criteria builder
     * @return a matching predicate or an always-true predicate when absent
     */
    private static Predicate rolePredicate(
            Role role,
            Root<User> root,
            CriteriaBuilder cb
    ) {
        return role == null
                ? cb.conjunction()
                : cb.equal(root.get("role"), role);
    }
}