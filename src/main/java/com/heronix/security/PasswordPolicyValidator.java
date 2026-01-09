package com.heronix.security;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Password Policy Validator for Heronix-SIS
 *
 * Enforces comprehensive password security requirements to protect user accounts
 * from unauthorized access. This validator implements industry best practices
 * including:
 *
 * - Minimum length requirements (12 characters)
 * - Complexity requirements (uppercase, lowercase, numbers, special characters)
 * - Common password blacklist (top 10,000 most common passwords)
 * - Username/email similarity checks (prevent password containing username)
 * - Sequential character detection (e.g., "123456", "abcdef")
 * - Repeated character detection (e.g., "aaaaa")
 *
 * This validator is used during:
 * - User registration
 * - Password changes
 * - Password resets
 * - Admin-initiated password updates
 *
 * FERPA/COPPA Compliance Note:
 * Strong passwords are critical for protecting student data privacy as required
 * by federal regulations.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Component
public class PasswordPolicyValidator {

    // ============================================================
    // Configuration Constants
    // ============================================================

    private static final int MIN_LENGTH = 12;
    private static final int MIN_UPPERCASE = 1;
    private static final int MIN_LOWERCASE = 1;
    private static final int MIN_DIGITS = 1;
    private static final int MIN_SPECIAL_CHARS = 1;
    private static final int MAX_REPEATED_CHARS = 3;
    private static final int MAX_SEQUENTIAL_CHARS = 3;

    // ============================================================
    // Regular Expression Patterns
    // ============================================================

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`]");

    // ============================================================
    // Common Password Blacklist
    // ============================================================

    /**
     * Top 100 most commonly used passwords (subset of top 10,000).
     * In production, this should be loaded from a file or database.
     */
    private static final Set<String> COMMON_PASSWORDS = new HashSet<>(Arrays.asList(
        "password", "123456", "123456789", "12345678", "12345", "1234567", "password1",
        "123123", "1234567890", "000000", "abc123", "qwerty", "iloveyou", "monkey",
        "dragon", "111111", "123321", "letmein", "baseball", "master", "sunshine",
        "ashley", "bailey", "shadow", "superman", "qazwsx", "michael", "football",
        "welcome", "jesus", "ninja", "mustang", "password123", "admin", "solo",
        "starwars", "passw0rd", "batman", "trustno1", "zaq1zaq1", "qwertyuiop",
        "login", "princess", "azerty", "000000", "loveme", "whatever", "donald",
        "charlie", "aa123456", "metallica", "august", "hello", "welcome123",
        "ferrari", "cheese", "computer", "corvette", "mercedes", "samsung",
        "freedom", "jordan23", "hunter", "buster", "Soccer", "harley", "ranger",
        "jennifer", "michelle", "daniel", "asdfghjkl", "123qwe", "1q2w3e4r",
        "password!", "qwerty123", "admin123", "root", "toor", "pass", "test",
        "guest", "oracle", "administrator", "1234", "12345678910", "1qaz2wsx",
        "zxcvbnm", "fuckyou", "asdfgh", "abcd1234", "changeme", "master123",
        "student", "teacher", "school", "heronix", "education", "learning"
    ));

    // ============================================================
    // Validation Result Inner Class
    // ============================================================

    /**
     * Encapsulates the result of password validation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

    // ============================================================
    // Main Validation Methods
    // ============================================================

    /**
     * Validates a password against all security policies.
     *
     * @param password the password to validate
     * @param username the username (used to prevent password containing username)
     * @return ValidationResult containing validation status and error messages
     */
    public ValidationResult validate(String password, String username) {
        List<String> errors = new ArrayList<>();

        // Null/empty check
        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new ValidationResult(false, errors);
        }

        if (username == null || username.isEmpty()) {
            errors.add("Username cannot be empty");
            return new ValidationResult(false, errors);
        }

        // Length requirement
        validateLength(password, errors);

        // Complexity requirements
        validateComplexity(password, errors);

        // Common password check
        validateNotCommonPassword(password, errors);

        // Username similarity check
        validateNotSimilarToUsername(password, username, errors);

        // Sequential characters check
        validateNoSequentialCharacters(password, errors);

        // Repeated characters check
        validateNoRepeatedCharacters(password, errors);

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Simplified validation for password changes (without username check).
     *
     * @param password the password to validate
     * @return ValidationResult containing validation status and error messages
     */
    public ValidationResult validatePasswordOnly(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password cannot be empty");
            return new ValidationResult(false, errors);
        }

        validateLength(password, errors);
        validateComplexity(password, errors);
        validateNotCommonPassword(password, errors);
        validateNoSequentialCharacters(password, errors);
        validateNoRepeatedCharacters(password, errors);

        return new ValidationResult(errors.isEmpty(), errors);
    }

    // ============================================================
    // Individual Validation Rules
    // ============================================================

    /**
     * Validates password length.
     */
    private void validateLength(String password, List<String> errors) {
        if (password.length() < MIN_LENGTH) {
            errors.add(String.format("Password must be at least %d characters long", MIN_LENGTH));
        }
    }

    /**
     * Validates password complexity (uppercase, lowercase, digits, special chars).
     */
    private void validateComplexity(String password, List<String> errors) {
        long uppercaseCount = password.chars()
            .filter(ch -> UPPERCASE_PATTERN.matcher(String.valueOf((char) ch)).matches())
            .count();

        long lowercaseCount = password.chars()
            .filter(ch -> LOWERCASE_PATTERN.matcher(String.valueOf((char) ch)).matches())
            .count();

        long digitCount = password.chars()
            .filter(ch -> DIGIT_PATTERN.matcher(String.valueOf((char) ch)).matches())
            .count();

        long specialCharCount = password.chars()
            .filter(ch -> SPECIAL_CHAR_PATTERN.matcher(String.valueOf((char) ch)).matches())
            .count();

        if (uppercaseCount < MIN_UPPERCASE) {
            errors.add(String.format("Password must contain at least %d uppercase letter(s)", MIN_UPPERCASE));
        }

        if (lowercaseCount < MIN_LOWERCASE) {
            errors.add(String.format("Password must contain at least %d lowercase letter(s)", MIN_LOWERCASE));
        }

        if (digitCount < MIN_DIGITS) {
            errors.add(String.format("Password must contain at least %d digit(s)", MIN_DIGITS));
        }

        if (specialCharCount < MIN_SPECIAL_CHARS) {
            errors.add(String.format("Password must contain at least %d special character(s) (!@#$%%^&*...)", MIN_SPECIAL_CHARS));
        }
    }

    /**
     * Checks if password is in the common password blacklist.
     */
    private void validateNotCommonPassword(String password, List<String> errors) {
        String passwordLower = password.toLowerCase();

        if (COMMON_PASSWORDS.contains(passwordLower)) {
            errors.add("This password is too common and easily guessable. Please choose a more unique password");
        }

        // Also check if password is just a common password with numbers/symbols appended
        for (String commonPassword : COMMON_PASSWORDS) {
            if (passwordLower.startsWith(commonPassword) && passwordLower.length() <= commonPassword.length() + 4) {
                errors.add("Password appears to be a common password with simple modifications. Please choose a more unique password");
                break;
            }
        }
    }

    /**
     * Ensures password doesn't contain the username.
     */
    private void validateNotSimilarToUsername(String password, String username, List<String> errors) {
        String passwordLower = password.toLowerCase();
        String usernameLower = username.toLowerCase();

        if (passwordLower.contains(usernameLower)) {
            errors.add("Password cannot contain your username");
        }

        // Check reverse (username in password)
        if (usernameLower.contains(passwordLower) && password.length() >= 4) {
            errors.add("Password is too similar to your username");
        }
    }

    /**
     * Detects sequential characters (e.g., "123456", "abcdef", "fedcba").
     */
    private void validateNoSequentialCharacters(String password, List<String> errors) {
        String passwordLower = password.toLowerCase();

        for (int i = 0; i < passwordLower.length() - MAX_SEQUENTIAL_CHARS; i++) {
            // Check ascending sequence
            boolean isAscending = true;
            for (int j = 0; j < MAX_SEQUENTIAL_CHARS; j++) {
                if (passwordLower.charAt(i + j + 1) != passwordLower.charAt(i + j) + 1) {
                    isAscending = false;
                    break;
                }
            }

            // Check descending sequence
            boolean isDescending = true;
            for (int j = 0; j < MAX_SEQUENTIAL_CHARS; j++) {
                if (passwordLower.charAt(i + j + 1) != passwordLower.charAt(i + j) - 1) {
                    isDescending = false;
                    break;
                }
            }

            if (isAscending || isDescending) {
                errors.add(String.format("Password contains sequential characters (e.g., '123', 'abc'). Please avoid sequences of %d or more characters", MAX_SEQUENTIAL_CHARS + 1));
                break;
            }
        }
    }

    /**
     * Detects repeated characters (e.g., "aaaa", "1111").
     */
    private void validateNoRepeatedCharacters(String password, List<String> errors) {
        for (int i = 0; i < password.length() - MAX_REPEATED_CHARS; i++) {
            char currentChar = password.charAt(i);
            boolean allSame = true;

            for (int j = 1; j <= MAX_REPEATED_CHARS; j++) {
                if (password.charAt(i + j) != currentChar) {
                    allSame = false;
                    break;
                }
            }

            if (allSame) {
                errors.add(String.format("Password contains too many repeated characters. Please avoid repeating the same character %d or more times", MAX_REPEATED_CHARS + 1));
                break;
            }
        }
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Gets a list of password requirements as user-friendly strings.
     *
     * @return List of password requirement descriptions
     */
    public List<String> getPasswordRequirements() {
        return Arrays.asList(
            String.format("At least %d characters long", MIN_LENGTH),
            String.format("At least %d uppercase letter (A-Z)", MIN_UPPERCASE),
            String.format("At least %d lowercase letter (a-z)", MIN_LOWERCASE),
            String.format("At least %d number (0-9)", MIN_DIGITS),
            String.format("At least %d special character (!@#$%%^&*...)", MIN_SPECIAL_CHARS),
            "Cannot be a common password",
            "Cannot contain your username",
            String.format("No more than %d repeated characters", MAX_REPEATED_CHARS),
            String.format("No sequential characters (%d+ in a row)", MAX_SEQUENTIAL_CHARS + 1)
        );
    }

    /**
     * Calculates password strength score (0-100).
     *
     * @param password the password to score
     * @return strength score from 0 (very weak) to 100 (very strong)
     */
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length score (max 30 points)
        score += Math.min(password.length() * 2, 30);

        // Complexity score (max 40 points)
        if (UPPERCASE_PATTERN.matcher(password).find()) score += 10;
        if (LOWERCASE_PATTERN.matcher(password).find()) score += 10;
        if (DIGIT_PATTERN.matcher(password).find()) score += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) score += 10;

        // Variety score (max 20 points)
        Set<Character> uniqueChars = new HashSet<>();
        for (char c : password.toCharArray()) {
            uniqueChars.add(c);
        }
        score += Math.min(uniqueChars.size(), 20);

        // Penalty for common patterns
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            score -= 50;
        }

        return Math.max(0, Math.min(100, score));
    }
}
