package io.github.kkusylabs.useradmin.backend.models;

import jakarta.persistence.*;

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
public class User {

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
     * The user's unique email address.
     */
    @Column(nullable = false, unique = true)
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
}