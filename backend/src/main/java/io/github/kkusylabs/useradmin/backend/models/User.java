package io.github.kkusylabs.useradmin.backend.models;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Represents an application user.
 * <p>
 * A user has login-related fields such as username and password hash,
 * along with profile-style fields such as full name and email address.
 * Each user belongs to exactly one department and has exactly one role.
 *
 * @author kkusy
 */
@Entity
@Table(name = "users")
public class User extends AuditableEntity {

    /**
     * The database-generated primary key for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique username used to identify the user during login.
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * The securely stored password hash for the user.
     * <p>
     * This field should contain a hashed password, not a plain-text password.
     */
    @Column(nullable = false)
    private String passwordHash;

    /**
     * The user's full display name.
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * The user's email address, if provided.
     */
    @Column
    private String email;

    /**
     * The department to which this user belongs.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * The user's assigned role.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * The user's phone number.
     */
    @Column
    private String phone;

    /**
     * The user's job title.
     */
    @Column
    private String jobTitle;

    /**
     * Whether the user is active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Creates an empty user instance.
     * <p>
     * Required by JPA.
     */
    public User() {
    }

    /**
     * Returns the database identifier of the user.
     *
     * @return the user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the username used by this user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username used by this user.
     *
     * @param username the username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the stored password hash.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the stored password hash.
     *
     * @param passwordHash the password hash to store
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the user's full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the user's full name.
     *
     * @param fullName the full name to assign
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address to assign
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the department to which the user belongs.
     *
     * @return the user's department
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * Sets the department to which the user belongs.
     *
     * @param department the department to assign
     */
    public void setDepartment(Department department) {
        this.department = department;
    }

    /**
     * Returns the role assigned to the user.
     *
     * @return the user's role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the role assigned to the user.
     *
     * @param role the role to assign
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Returns the user's phone number.
     *
     * @return the phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone the phone number to assign
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the user's job title.
     *
     * @return the job title
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the user's job title.
     *
     * @param jobTitle the job title to assign
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Indicates whether the user is active.
     *
     * @return {@code true} if the user is active; {@code false} otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Compares this user to another object for equality.
     * <p>
     * Equality is based solely on the {@code id}. Two users are considered equal
     * if they represent the same persisted entity.
     * </p>
     *
     * @param o the object to compare with
     * @return {@code true} if both objects have the same non-null identifier; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    /**
     * Indicates whether this user has administrator privileges.
     *
     * @return {@code true} if the user's role is {@link Role#ADMIN}; {@code false} otherwise
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Indicates whether this user has manager-level privileges.
     *
     * @return {@code true} if the user's role is {@link Role#MANAGER}; {@code false} otherwise
     */
    public boolean isManager() {
        return role == Role.MANAGER;
    }

    /**
     * Indicates whether this user is a basic user.
     * <p>
     * A {@code null} role is treated as {@link Role#USER} for backward compatibility
     * or defaulting behavior.
     *
     * @return {@code true} if the role is {@code null} or {@link Role#USER}; {@code false} otherwise
     */
    public boolean isBasicUser() {
        return role == null || role == Role.USER;
    }

    /**
     * Returns a hash code for this user.
     * <p>
     * The hash code is based on the class type to remain stable before and after
     * persistence when the {@code id} may not yet be assigned.
     * </p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Sets whether the user is active.
     *
     * @param active {@code true} to mark the user as active; {@code false} otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}