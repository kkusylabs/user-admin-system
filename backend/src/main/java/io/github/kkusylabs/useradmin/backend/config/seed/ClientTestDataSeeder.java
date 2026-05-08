package io.github.kkusylabs.useradmin.backend.config.seed;

import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@Profile({"client-test"})
public class ClientTestDataSeeder implements ApplicationRunner {
    private final SeedDataHelper seed;

    public ClientTestDataSeeder(
            SeedDataHelper seedDataHelper
    ) {
        this.seed = seedDataHelper;
    }

    @Override
    public void run(ApplicationArguments args) {

        Department engineering = seed.createDepartment(
                "Engineering",
                "Builds and maintains the application",
                true
        );

        Department finance = seed.createDepartment(
                "Finance",
                "Handles budgets and financial operations",
                true
        );

        Department hr = seed.createDepartment(
                "HR",
                "Human resources and recruiting",
                true
        );

        Department archived = seed.createDepartment(
                "Archived",
                "Inactive department for testing",
                false
        );

        seed.createUser(
                "admin1",
                "admin12345",
                "Admin One",
                "admin1@example.com",
                "+15550000001",
                "System Administrator",
                true,
                Role.ADMIN,
                engineering
        );

        seed.createUser(
                "manager1",
                "manager12345",
                "Manager One",
                "manager1@example.com",
                "+15550000002",
                "HR Manager",
                true,
                Role.MANAGER,
                hr
        );

        seed.createUser(
                "user1",
                "user12345",
                "User One",
                "user1@example.com",
                "+15550000003",
                "Financial Analyst",
                true,
                Role.USER,
                finance
        );

        seed.createUser(
                "inactive1",
                "inactive12345",
                "Inactive User",
                "inactive1@example.com",
                "+15550000004",
                "Former Analyst",
                false,
                Role.USER,
                finance
        );
    }
}