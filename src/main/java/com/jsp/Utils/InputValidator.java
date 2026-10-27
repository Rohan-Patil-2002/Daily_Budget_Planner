package com.jsp.Utils;

import java.math.BigDecimal;
import java.time.Month;
import java.util.regex.Pattern;

public class InputValidator {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,50}$");
    private static final BigDecimal MAX_MONETARY_AMOUNT = new BigDecimal("10000000.00");

    /**
     * Validates username format (3-50 chars, alphanumeric plus underscores, dots, hyphens).
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        String trimmed = username.trim();
        return USERNAME_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates password strength (at least 6 characters, max 100).
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        int len = password.length();
        return len >= 6 && len <= 100;
    }

    /**
     * Validates that month string matches a valid java.time.Month.
     */
    public static boolean isValidMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            return false;
        }
        try {
            Month.valueOf(month.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates year is reasonable (between 2020 and 2099).
     */
    public static boolean isValidYear(int year) {
        return year >= 2020 && year <= 2099;
    }

    /**
     * Validates monetary amount (positive, non-zero, <= MAX_MONETARY_AMOUNT, max 2 decimals).
     */
    public static boolean isValidBudgetAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(MAX_MONETARY_AMOUNT) <= 0;
    }

    /**
     * Validates spending amount (non-negative, <= MAX_MONETARY_AMOUNT).
     */
    public static boolean isValidSpendAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(BigDecimal.ZERO) >= 0 && amount.compareTo(MAX_MONETARY_AMOUNT) <= 0;
    }

    /**
     * Sanitizes note text to max 255 chars, removing control chars.
     */
    public static String sanitizeNote(String note) {
        if (note == null) {
            return "General Spend";
        }
        String trimmed = note.trim();
        if (trimmed.isEmpty()) {
            return "General Spend";
        }
        if (trimmed.length() > 255) {
            trimmed = trimmed.substring(0, 255);
        }
        return trimmed;
    }
}
