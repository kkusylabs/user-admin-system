package io.github.kkusylabs.useradmin.backend.dtos.department;

/**
 * Lightweight department reference.
 *
 * <p>Contains only the fields needed for selection controls (e.g. dropdowns).</p>
 *
 * <ul>
 *   <li><b>id</b> – unique identifier</li>
 *   <li><b>name</b> – display name</li>
 * </ul>
 *
 * <p>Typically derived from a projection and mapped before returning to clients.</p>
 *
 * @author kkusy
 */
public record DepartmentOption(
        Long id,
        String name
) {
}