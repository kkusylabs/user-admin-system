package io.github.kkusylabs.useradmin.client.core.api.user;

public enum Role {

	USER("User"),
	MANAGER("Manager"),
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
