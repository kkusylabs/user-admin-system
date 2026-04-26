package io.github.kkusylabs.useradmin.backend.utils;

import java.util.Locale;

/**
 * Utility methods for normalizing and sanitizing string values.
 *
 * <p>Provides common operations such as trimming, null handling, and
 * case normalization for user input.</p>
 *
 * @author kkusy
 */
public class StringNormalizer {

    private StringNormalizer() {}

    /**
     * Trims the input and returns {@code null} if the result is empty.
     *
     * @param value the input value
     * @return the trimmed value, or {@code null} if blank or {@code null}
     */
    public static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Trims the input value.
     *
     * @param value the input value
     * @return the trimmed value, or {@code null} if input is {@code null}
     */
    public static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Normalizes an email address by trimming and converting to lowercase.
     *
     * @param value the input email
     * @return normalized email, or {@code null} if blank or {@code null}
     */
    public static String normalizeEmail(String value) {
        String email = trimToNull(value);
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    /**
     * Normalizes a username by trimming and converting to lowercase.
     *
     * @param value the input username
     * @return normalized username, or {@code null} if input is {@code null}
     */
    public static String normalizeUsername(String value) {
        String username = trimToNull(value);
        return username == null ? null : username.toLowerCase(Locale.ROOT);
    }

    public static String normalizePhone(String value) {
        String phone = trimToNull(value);
        if (phone == null) return null;

        String normalized = phone.replaceAll("[^0-9+]", "");

        // collapse invalid results
        return normalized.isEmpty() ? null : normalized;
    }
}