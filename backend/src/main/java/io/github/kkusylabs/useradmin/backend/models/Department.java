package io.github.kkusylabs.useradmin.backend.models;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Represents a department within the system.
 * <p>
 * A department groups users under a common organizational unit,
 * such as Accounting, Human Resources, or IT.
 */
@Entity
@Table(name = "departments")
public class Department extends AuditableEntity {

    /**
     * The database-generated primary key for the department.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique name of the department.
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Creates an empty department instance.
     * <p>
     * Required by JPA.
     */
    public Department() {
    }

    /**
     * Returns the database identifier of the department.
     *
     * @return the department ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the name of the department.
     *
     * @return the department name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the department.
     *
     * @param name the department name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Compares this department to another object for equality.
     * <p>
     * Equality is based solely on the {@code id}. Two departments are considered equal
     * if they represent the same persisted entity.
     * </p>
     *
     * @param o the object to compare with
     * @return {@code true} if both objects have the same non-null identifier; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    /**
     * Returns a hash code for this department.
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
}