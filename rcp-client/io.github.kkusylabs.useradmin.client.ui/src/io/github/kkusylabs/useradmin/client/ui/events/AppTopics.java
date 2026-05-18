package io.github.kkusylabs.useradmin.client.ui.events;

/**
 * Application-wide event topic constants used with the Eclipse event broker.
 *
 * <p>
 * These topics coordinate authentication and session-related workflows
 * across the RCP client.
 * </p>
 */
public final class AppTopics {

	private AppTopics() {
	}

	/**
	 * Published when a user successfully authenticates.
	 */
	public static final String LOGIN_SUCCESS = "app/auth/loginSuccess";

	/**
	 * Published when a login attempt fails.
	 */
	public static final String LOGIN_FAILED = "app/auth/loginFailed";

	/**
	 * Published when a user logs out of the application.
	 */
	public static final String LOGOUT = "app/auth/logout";
	
	/**
	 * Published when the current authentication token expires or becomes
	 * invalid.
	 */
	public static final String AUTH_EXPIRED = "app/auth/expired";
}
