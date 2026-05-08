package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserPatch {
	private final Map<String, Object> values = new LinkedHashMap<>();

	public UserPatch fullName(String value) {
		values.put("fullName", value);
		return this;
	}

	public UserPatch email(String value) {
		values.put("email", value);
		return this;
	}

	public UserPatch phone(String value) {
		values.put("phone", value);
		return this;
	}

	public UserPatch jobTitle(String value) {
		values.put("jobTitle", value);
		return this;
	}

	public UserPatch active(Boolean value) {
		values.put("active", value);
		return this;
	}

	public UserPatch departmentId(Long value) {
		values.put("departmentId", value);
		return this;
	}

	public UserPatch role(Role value) {
		values.put("role", value);
		return this;
	}

	public Map<String, Object> asMap() {
		return Map.copyOf(values);
	}
}
