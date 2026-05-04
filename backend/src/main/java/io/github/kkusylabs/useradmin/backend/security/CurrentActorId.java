package io.github.kkusylabs.useradmin.backend.security;

import java.lang.annotation.*;

/**
 * Marks a controller method parameter to be resolved as the current actor's identifier.
 *
 * <p>Used with {@link CurrentActorIdArgumentResolver} to inject the authenticated
 * user's ID from the security context.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentActorId {
}
