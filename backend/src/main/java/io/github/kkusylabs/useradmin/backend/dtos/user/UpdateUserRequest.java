package io.github.kkusylabs.useradmin.backend.dtos.user;

import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.utils.StringNormalizer;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Request payload for updating a user.
 *
 * <p>All fields are optional. {@code null} values are treated as unchanged.</p>
 *
 * @param fullName     updated display name
 * @param email        updated email address (must be valid if provided)
 * @param phone        updated phone number
 * @param jobTitle     updated job title
 * @param active       updated active state
 * @param departmentId updated department identifier
 * @param role         updated role
 * @author kkusy
 */
public record UpdateUserRequest(
        JsonNullable<String> fullName,
        JsonNullable<String> email,
        JsonNullable<String> phone,
        JsonNullable<String> jobTitle,
        JsonNullable<Boolean> active,
        JsonNullable<Long> departmentId,
        JsonNullable<Role> role
) {
        public UpdateUserRequest {
                fullName = mapNullable(fullName, StringNormalizer::trimToNull);
                phone = mapNullable(phone, StringNormalizer::normalizePhone);
                email = mapNullable(email, StringNormalizer::normalizeEmail);
                jobTitle = mapNullable(jobTitle, StringNormalizer::trimToNull);

                role = normalizeWrapper(role);
                departmentId = normalizeWrapper(departmentId);
                active = normalizeWrapper(active);
        }

        private static <T> JsonNullable<T> mapNullable(
                JsonNullable<T> value,
                java.util.function.UnaryOperator<T> mapper
        ) {
                if (value == null || value.isUndefined()) {
                        return JsonNullable.undefined();
                }
                return JsonNullable.of(mapper.apply(value.orElse(null)));
        }

        private static <T> JsonNullable<T> normalizeWrapper(JsonNullable<T> value) {
                return value == null ? JsonNullable.undefined() : value;
        }
}