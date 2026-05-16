package io.github.kkusylabs.useradmin.client.core.api.user;

public enum Role {

	ADMIN("Admin"),
	MANAGER("Manager"),
	USER("User");

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
