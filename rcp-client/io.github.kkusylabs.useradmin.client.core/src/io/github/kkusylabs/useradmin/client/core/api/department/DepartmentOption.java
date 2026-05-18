package io.github.kkusylabs.useradmin.client.core.api.department;


/**
 * Lightweight department representation intended for selection lists and
 * reference data.
 *
 * @param id unique department identifier
 * @param name department display name
 */
public record DepartmentOption(
		Long id, 
		String name) {
}