package io.github.kkusylabs.useradmin.client.core.api.common;

import java.util.Objects;

public record SortSpec(String property, Direction direction) {
	public SortSpec {
		property = Objects.requireNonNull(property, "property must not be null").trim();
		direction = Objects.requireNonNull(direction, "direction must not be null");

		if (property.isEmpty()) {
			throw new IllegalArgumentException("property must not be blank");
		}
	}
	
	public static SortSpec asc(String property) {
		return new SortSpec(property, Direction.ASC);
	}

	public static SortSpec desc(String property) {
		return new SortSpec(property, Direction.DESC);
	}

	public enum Direction {
		ASC, DESC
	}

	public String toQueryValue() {
		return property + "," + direction.name().toLowerCase();
	}
}