package com.example.shopverse_customer_app.utils;

import android.util.Patterns;

/**
 * Utility class for input validation
 */
public class ValidationUtils {

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate phone number (basic check)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // Remove spaces and special characters
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        // Check if it contains only digits and has reasonable length
        return cleaned.matches("\\d{9,15}");
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Validate password match
     */
    public static boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Check if string is empty
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Get password strength level (weak, medium, strong)
     */
    public static PasswordStrength getPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return PasswordStrength.WEAK;
        }

        int score = 0;

        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;

        // Contains lowercase
        if (password.matches(".*[a-z].*")) score++;

        // Contains uppercase
        if (password.matches(".*[A-Z].*")) score++;

        // Contains digit
        if (password.matches(".*\\d.*")) score++;

        // Contains special character
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        if (score <= 2) return PasswordStrength.WEAK;
        if (score <= 4) return PasswordStrength.MEDIUM;
        return PasswordStrength.STRONG;
    }

    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }
}
