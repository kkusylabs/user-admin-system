package io.github.kkusylabs.useradmin.backend.config;

import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            Department adminDept = departmentRepository.findByName("Administration")
                    .orElseGet(() -> {
                        Department d = new Department();
                        d.setName("Administration");
                        return departmentRepository.save(d);
                    });

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setFullName("System Admin");
                admin.setEmail("admin@example.com");
                admin.setPhone("555-0100");
                admin.setJobTitle("Administrator");
                admin.setActive(true);
                admin.setRole(Role.ADMIN);
                admin.setDepartment(adminDept);
                admin.setPasswordHash(passwordEncoder.encode("admin")); // replace later with real password encoding
                userRepository.save(admin);
            }
        };
    }
}