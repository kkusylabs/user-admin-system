package io.github.kkusylabs.useradmin.backend.config.seed;

import io.github.kkusylabs.useradmin.backend.models.Department;
import io.github.kkusylabs.useradmin.backend.models.Role;
import io.github.kkusylabs.useradmin.backend.models.User;
import io.github.kkusylabs.useradmin.backend.repositories.DepartmentRepository;
import io.github.kkusylabs.useradmin.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SeedDataHelper {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataHelper(
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean hasAnySeedData() {
        return userRepository.count() > 0 || departmentRepository.count() > 0;
    }

    public Department createDepartment(String name, String description, boolean active) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setActive(active);
        return departmentRepository.save(department);
    }

    public Department findOrCreateDepartment(String name, String description, boolean active) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> createDepartment(name, description, active));
    }

    public Department findOrUpdateDepartment(String name, String description, boolean active) {
        return departmentRepository.findByName(name)
                .map(existing -> updateDepartment(existing, description, active))
                .orElseGet(() -> createDepartment(name, description, active));
    }

    private Department updateDepartment(Department department, String description, boolean active) {
        department.setDescription(description);
        department.setActive(active);
        return departmentRepository.save(department);
    }

    public User createUser(
            String username,
            String rawPassword,
            String fullName,
            String email,
            String phone,
            String jobTitle,
            boolean active,
            Role role,
            Department department
    ) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setJobTitle(jobTitle);
        user.setActive(active);
        user.setRole(role);
        user.setDepartment(department);
        return userRepository.save(user);
    }

    public User findOrCreateUser(
            String username,
            String rawPassword,
            String fullName,
            String email,
            String phone,
            String jobTitle,
            boolean active,
            Role role,
            Department department
    ) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> createUser(
                        username,
                        rawPassword,
                        fullName,
                        email,
                        phone,
                        jobTitle,
                        active,
                        role,
                        department
                ));
    }

    public User findOrUpdateUser(
            String username,
            String rawPassword,
            String fullName,
            String email,
            String phone,
            String jobTitle,
            boolean active,
            Role role,
            Department department
    ) {
        return userRepository.findByUsername(username)
                .map(existing -> updateUser(
                        existing,
                        rawPassword,
                        fullName,
                        email,
                        phone,
                        jobTitle,
                        active,
                        role,
                        department
                ))
                .orElseGet(() -> createUser(
                        username,
                        rawPassword,
                        fullName,
                        email,
                        phone,
                        jobTitle,
                        active,
                        role,
                        department
                ));
    }

    private User updateUser(
            User user,
            String rawPassword,
            String fullName,
            String email,
            String phone,
            String jobTitle,
            boolean active,
            Role role,
            Department department
    ) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setJobTitle(jobTitle);
        user.setActive(active);
        user.setRole(role);
        user.setDepartment(department);
        return userRepository.save(user);
    }
}