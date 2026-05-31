package io.github.kkusylabs.useradmin.client.core.api.user;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder-style helper for constructing partial user update payloads.
 *
 * <p>Only explicitly assigned fields are included in the generated patch map.
 * 
 *
 * <p>Example:
 *
 * <pre>{@code
 * UserPatch patch = new UserPatch()
 *         .fullName("Jane Smith")
 *         .email("jane.smith@example.com")
 *         .role(Role.MANAGER)
 *         .active(true);
 *
 * userApiClient.updateUser(userId, patch.asMap());
 * }</pre>
 */
public class UserPatch {
	private final Map<String, Object> values = new LinkedHashMap<>();


	/**
	 * Sets the user's full name.
	 *
	 * @param value updated full name
	 * @return current patch instance
	 */
	public UserPatch fullName(String value) {
		values.put("fullName", value);
		return this;
	}

	/**
	 * Sets the user's email address.
	 *
	 * @param value updated email address
	 * @return current patch instance
	 */
	public UserPatch email(String value) {
		values.put("email", value);
		return this;
	}

	/**
	 * Sets the user's phone number.
	 *
	 * @param value updated phone number
	 * @return current patch instance
	 */
	public UserPatch phone(String value) {
		values.put("phone", value);
		return this;
	}

	/**
	 * Sets the user's job title.
	 *
	 * @param value updated job title
	 * @return current patch instance
	 */
	public UserPatch jobTitle(String value) {
		values.put("jobTitle", value);
		return this;
	}

	/**
	 * Sets the user's active status.
	 *
	 * @param value updated active status
	 * @return current patch instance
	 */
	public UserPatch active(Boolean value) {
		values.put("active", value);
		return this;
	}

	/**
	 * Sets the user's department assignment.
	 *
	 * @param value updated department identifier
	 * @return current patch instance
	 */
	public UserPatch departmentId(Long value) {
		values.put("departmentId", value);
		return this;
	}

	/**
	 * Sets the user's assigned role.
	 *
	 * @param value updated role
	 * @return current patch instance
	 */
	public UserPatch role(Role value) {
		values.put("role", value);
		return this;
	}

	/**
	 * Returns an immutable representation of the configured patch values.
	 *
	 * @return immutable patch payload map
	 */
	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(values));
	}
}
