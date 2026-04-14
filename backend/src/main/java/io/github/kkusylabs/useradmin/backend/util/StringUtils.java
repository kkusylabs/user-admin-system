package io.github.kkusylabs.useradmin.backend.util;

/**
 * Utility methods for working with strings.
 */
public final class StringUtils {

    private StringUtils() {
        // utility class
    }

    /**
     * Trims a string and converts blank values to {@code null}.
     *
     * @param value the input value
     * @return the trimmed value, or {@code null} if blank
     */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalizes an email address by trimming and converting it to lowercase.
     *
     * @param email the raw email address
     * @return the normalized email, or {@code null} if blank
     */
    public static String normalizeEmail(String email) {
        String value = trimToNull(email);
        return value != null ? value.toLowerCase() : null;
    }

    /**
     * Normalizes a username by trimming and converting it to lowercase.
     *
     * @param username the raw username
     * @return the normalized username
     */
    public static String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }
}