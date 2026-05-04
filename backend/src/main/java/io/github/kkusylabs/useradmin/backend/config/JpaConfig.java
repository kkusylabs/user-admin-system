package io.github.kkusylabs.useradmin.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing support.
 *
 * <p>Allows entities to automatically populate audit fields
 * such as created/modified timestamps via Spring Data JPA.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
