package io.github.kkusylabs.useradmin.client.core.api.user;

/**
 * Supported user roles within the user administration system.
 */
public enum Role {

	/**
	 * Standard user role with basic access permissions.
	 */
	USER("User"),
	
	/**
	 * Manager role with elevated operational permissions.
	 */
	MANAGER("Manager"),
	
	/**
	 * Administrative role with full system access.
	 */
	ADMIN("Admin");

	private final String displayName;

	Role(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public static Role fromDisplayName(String displayName) {
		for (Role role : values()) {
			if (role.displayName.equals(displayName)) {
				return role;
			}
		}

		return null;
	}
}
