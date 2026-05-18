package io.github.kkusylabs.useradmin.client.core.api.common;

import java.util.Objects;

/**
 * Sort specification used for paginated and filtered API requests.
 *
 * @param property entity property used for sorting
 * @param direction sort direction
 */
public record SortSpec(String property, Direction direction) {
	
	/**
	 * Creates a validated sort specification.
	 *
	 * @throws IllegalArgumentException if {@code property} is blank
	 * @throws NullPointerException if {@code property} or {@code direction} is
	 *                              {@code null}
	 */
	public SortSpec {
		property = Objects.requireNonNull(property, "property must not be null").trim();
		direction = Objects.requireNonNull(direction, "direction must not be null");

		if (property.isEmpty()) {
			throw new IllegalArgumentException("property must not be blank");
		}
	}
	
	/**
	 * Creates an ascending sort specification.
	 *
	 * @param property entity property used for sorting
	 * @return ascending sort specification
	 */
	public static SortSpec asc(String property) {
		return new SortSpec(property, Direction.ASC);
	}

	/**
	 * Creates a descending sort specification.
	 *
	 * @param property entity property used for sorting
	 * @return descending sort specification
	 */
	public static SortSpec desc(String property) {
		return new SortSpec(property, Direction.DESC);
	}

	/**
	 * Supported sort directions.
	 */
	public enum Direction {
		ASC, DESC
	}


	/**
	 * Returns the sort specification formatted for use as a query parameter.
	 *
	 * <p>Produces values in the format {@code property,direction}.
	 *
	 * @return query parameter representation of the sort specification
	 */
	public String toQueryValue() {
		return property + "," + direction.name().toLowerCase();
	}
}