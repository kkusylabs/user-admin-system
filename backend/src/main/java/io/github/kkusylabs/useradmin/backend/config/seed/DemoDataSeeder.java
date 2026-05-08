package io.github.kkusylabs.useradmin.backend.config.seed;

import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

    private final SeedDataHelper seed;

    public DemoDataSeeder(SeedDataHelper seedDataHelper) {
        this.seed = seedDataHelper;
    }

    @Override
    public void run(ApplicationArguments args) {

        Department executive = seed.createDepartment(
                "Executive",
                "Company leadership and strategic planning",
                true
        );

        Department engineering = seed.createDepartment(
                "Engineering",
                "Software engineering and platform development",
                true
        );

        Department product = seed.createDepartment(
                "Product",
                "Product strategy and roadmap planning",
                true
        );

        Department design = seed.createDepartment(
                "Design",
                "User experience and interface design",
                true
        );

        Department sales = seed.createDepartment(
                "Sales",
                "Sales and customer acquisition",
                true
        );

        Department marketing = seed.createDepartment(
                "Marketing",
                "Marketing campaigns and brand management",
                true
        );

        Department customerSuccess = seed.createDepartment(
                "Customer Success",
                "Customer onboarding and support",
                true
        );

        Department finance = seed.createDepartment(
                "Finance",
                "Financial operations and reporting",
                true
        );

        Department hr = seed.createDepartment(
                "Human Resources",
                "People operations and recruiting",
                true
        );

        Department operations = seed.createDepartment(
                "Operations",
                "Internal business operations and procurement",
                true
        );

        Department archived = seed.createDepartment(
                "Archived",
                "Inactive department retained for historical records",
                false
        );

        List<Department> departments = List.of(
                engineering,
                product,
                design,
                sales,
                marketing,
                customerSuccess,
                finance,
                hr,
                operations
        );

        // Core admin accounts

        seed.createUser(
                "admin",
                "demo12345",
                "Avery Brooks",
                "avery.brooks@example.com",
                "+15550000001",
                "Chief Technology Officer",
                true,
                Role.ADMIN,
                executive
        );

        seed.createUser(
                "nora.admin",
                "demo12345",
                "Nora Patel",
                "nora.patel@example.com",
                "+15550000002",
                "Director of Operations",
                true,
                Role.ADMIN,
                operations
        );

        seed.createUser(
                "marcus.admin",
                "demo12345",
                "Marcus Reed",
                "marcus.reed@example.com",
                "+15550000003",
                "Security Administrator",
                true,
                Role.ADMIN,
                engineering
        );

        String[] firstNames = {
                "James", "Emma", "Olivia", "Liam", "Noah",
                "Sophia", "Mason", "Isabella", "Ethan", "Mia",
                "Lucas", "Charlotte", "Benjamin", "Amelia", "Henry",
                "Harper", "Alexander", "Evelyn", "Daniel", "Abigail"
        };

        String[] lastNames = {
                "Johnson", "Smith", "Williams", "Brown", "Jones",
                "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
                "Thomas", "Taylor", "Moore", "Jackson", "Martin"
        };

        String[] jobTitles = {
                "Software Engineer",
                "Senior Software Engineer",
                "Frontend Engineer",
                "Backend Engineer",
                "DevOps Engineer",
                "QA Engineer",
                "Product Analyst",
                "Project Coordinator",
                "UX Designer",
                "UI Designer",
                "Recruiter",
                "Financial Analyst",
                "Account Executive",
                "Marketing Specialist",
                "Customer Success Representative",
                "Operations Specialist",
                "Business Analyst",
                "Implementation Consultant",
                "Support Specialist",
                "Systems Administrator"
        };

        for (int i = 1; i <= 97; i++) {

            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[i % lastNames.length];

            String username =
                    (firstName + "." + lastName + i).toLowerCase();

            String fullName =
                    firstName + " " + lastName;

            String email =
                    username + "@example.com";

            String phone =
                    String.format("+1555%07d", i + 100);

            String title =
                    jobTitles[i % jobTitles.length];

            Department department =
                    departments.get(i % departments.size());

            Role role;

            if (i % 25 == 0) {
                role = Role.ADMIN;
            } else if (i % 8 == 0) {
                role = Role.MANAGER;
            } else {
                role = Role.USER;
            }

            boolean active = i % 12 != 0;

            if (!active && i % 2 == 0) {
                department = archived;
            }

            seed.createUser(
                    username,
                    "demo12345",
                    fullName,
                    email,
                    phone,
                    title,
                    active,
                    role,
                    department
            );
        }
    }
}
