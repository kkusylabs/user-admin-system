package io.github.kkusylabs.useradmin.backend.models;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * JPA entity representing a department.
 *
 * <p>A department is an organizational unit used to group users
 * (e.g. Accounting, HR, IT).</p>
 *
 * <p>Persistence details:</p>
 * <ul>
 *   <li>Mapped to {@code departments} table</li>
 *   <li>{@code name} is unique and required</li>
 *   <li>{@code active} defaults to {@code true}</li>
 * </ul>
 *
 * <p>Equality is based on the database identifier.</p>
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
     * Optional description.
     */
    @Column
    private String description;

    /**
     * Whether the department is active.
     */
    @Column(nullable = false)
    private boolean active = true;

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
     * @return description or {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description optional description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return {@code true} if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active active flag
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Equality based on {@code id}.
     *
     * @param o other object
     * @return {@code true} if same persisted entity
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Department that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    /**
     * Stable hash code independent of persistence state.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}