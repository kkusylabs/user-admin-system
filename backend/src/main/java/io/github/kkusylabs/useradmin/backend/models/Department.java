package io.github.kkusylabs.useradmin.backend.models;

import jakarta.persistence.*;

/**
 * Represents a department within the system.
 * <p>
 * A department groups users under a common organizational unit,
 * such as Accounting, Human Resources, or IT.
 */
@Entity
@Table(name = "departments")
public class Department {

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
}