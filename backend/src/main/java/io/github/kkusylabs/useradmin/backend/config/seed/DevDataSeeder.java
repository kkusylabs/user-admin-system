package io.github.kkusylabs.useradmin.backend.config.seed;

import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes default data on application startup.
 *
 * <p>Ensures that a default "Administration" department and an initial
 * administrator user exist. Intended for development or bootstrap scenarios
 * and not for production-grade data seeding.</p>
 */
@Component
@Profile("dev")
@Transactional
public class DevDataSeeder implements ApplicationRunner {
    private final SeedDataHelper seed;

    public DevDataSeeder(SeedDataHelper seedDataHelper) {
        this.seed = seedDataHelper;
    }

    @Override
    public void run(ApplicationArguments args) {
        Department admin = seed.findOrCreateDepartment(
                "Administration",
                "Default admin department",
                true
        );

        seed.findOrCreateUser(
                "admin",
                "admin",
                "System Admin",
                "admin@example.com",
                "555-0100",
                "Administrator",
                true,
                Role.ADMIN,
                admin
        );
    }
}