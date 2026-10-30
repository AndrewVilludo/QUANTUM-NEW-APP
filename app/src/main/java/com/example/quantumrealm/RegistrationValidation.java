package com.example.quantumrealm;

import java.util.regex.Pattern;

/** Shared registration field rules (matches Laravel {@code ValidRegistrationName}). */
public final class RegistrationValidation {

    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern VALID_NAME = Pattern.compile("^[\\p{L}\\s]+$");

    private RegistrationValidation() {
    }

    public static boolean containsDigit(CharSequence value) {
        return value != null && DIGIT.matcher(value).find();
    }

    public static boolean isValidName(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        return VALID_NAME.matcher(trimmed).matches();
    }

    public static String stripDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\d", "");
    }

    public static String stripInvalidNameChars(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^\\p{L}\\s]", "");
    }

    public static boolean isPasswordLongEnough(String password) {
        return password != null && password.length() >= 8;
    }
}
