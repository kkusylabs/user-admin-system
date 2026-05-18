package io.github.kkusylabs.useradmin.client.ui.util;

/**
 * Utility methods for common String and text handling operations.
 */
public class TextUtil {

	private TextUtil() {
	}
	
	/**
	 * Trims the supplied value and converts blank strings to {@code null}.
	 *
	 * @param value input value
	 * @return trimmed value, or {@code null} if blank
	 */
	public static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();

		return trimmed.isEmpty() ? null : trimmed;
	}

	/**
	 * Converts a {@code null} value to an empty string.
	 *
	 * @param value input value
	 * @return empty string if {@code null}; otherwise the original value
	 */
	public static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
