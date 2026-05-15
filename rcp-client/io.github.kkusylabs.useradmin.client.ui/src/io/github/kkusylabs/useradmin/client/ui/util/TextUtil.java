package io.github.kkusylabs.useradmin.client.ui.util;

public class TextUtil {

	private TextUtil() {
	}
	
	public static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();

		return trimmed.isEmpty() ? null : trimmed;
	}

	public static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
