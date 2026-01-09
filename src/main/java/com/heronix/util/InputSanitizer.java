package com.heronix.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Input Sanitizer Utility for Heronix-SIS
 *
 * Provides comprehensive input sanitization to prevent:
 * - Cross-Site Scripting (XSS) attacks
 * - SQL Injection attempts
 * - HTML injection
 * - Script injection
 * - Path traversal attacks
 *
 * USAGE:
 * All user input should be sanitized before:
 * - Storing in the database
 * - Displaying in the UI
 * - Using in file paths
 * - Including in reports
 *
 * Example:
 * <pre>
 * {@code
 * String userInput = request.getParameter("name");
 * String sanitized = InputSanitizer.sanitizeText(userInput);
 * student.setName(sanitized);
 * }
 * </pre>
 *
 * DEFENSE IN DEPTH:
 * This is one layer of security. Also ensure:
 * - Parameterized SQL queries (prevent SQL injection)
 * - Content Security Policy headers (prevent XSS)
 * - Output encoding in templates (prevent XSS)
 * - Proper file path validation (prevent path traversal)
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Component
public class InputSanitizer {

    private static final Logger logger = LoggerFactory.getLogger(InputSanitizer.class);

    // ============================================================
    // HTML Sanitization (OWASP Java HTML Sanitizer)
    // ============================================================

    /**
     * Strict HTML policy - removes ALL HTML tags.
     * Use for plain text fields (names, addresses, etc.).
     */
    private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder()
        .toFactory();

    /**
     * Basic HTML policy - allows only safe formatting tags.
     * Use for rich text fields where some formatting is needed.
     * Allows: <b>, <i>, <u>, <p>, <br>, <ul>, <ol>, <li>
     */
    private static final PolicyFactory BASIC_HTML_POLICY = new HtmlPolicyBuilder()
        .allowElements("b", "i", "u", "p", "br", "ul", "ol", "li", "strong", "em")
        .toFactory();

    /**
     * Extended HTML policy - allows more formatting but still safe.
     * Use for rich text editors (assignment descriptions, announcements, etc.).
     * Allows: formatting tags + links + tables
     */
    private static final PolicyFactory EXTENDED_HTML_POLICY = new HtmlPolicyBuilder()
        .allowElements(
            "b", "i", "u", "p", "br", "ul", "ol", "li", "strong", "em",
            "h1", "h2", "h3", "h4", "h5", "h6",
            "blockquote", "pre", "code",
            "table", "thead", "tbody", "tr", "th", "td",
            "a", "img"
        )
        .allowAttributes("href").onElements("a")
        .allowAttributes("src", "alt", "width", "height").onElements("img")
        .allowAttributes("colspan", "rowspan").onElements("td", "th")
        .requireRelNofollowOnLinks() // Prevent SEO manipulation
        .toFactory();

    // ============================================================
    // Regular Expression Patterns
    // ============================================================

    // Detect potential SQL injection patterns
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "('(''|[^'])*')|(;)|(\\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE)?|INSERT( +INTO)?|" +
        "MERGE|SELECT|UPDATE|UNION( +ALL)?)\\b)",
        Pattern.CASE_INSENSITIVE
    );

    // Detect potential XSS patterns
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script|javascript:|onerror=|onload=|onclick=|<iframe|eval\\(|expression\\(",
        Pattern.CASE_INSENSITIVE
    );

    // Detect path traversal attempts
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "\\.\\./|\\.\\.\\\\|%2e%2e/|%2e%2e\\\\",
        Pattern.CASE_INSENSITIVE
    );

    // Allowed characters for usernames (alphanumeric + underscore + dot)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._]{3,50}$");

    // Allowed characters for email addresses
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // Allowed characters for phone numbers (digits, spaces, dashes, parentheses, plus)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9\\s()+-]{7,20}$");

    // Allowed characters for file names (alphanumeric + underscore + dash + dot)
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{1,255}$");

    // ============================================================
    // Text Sanitization Methods
    // ============================================================

    /**
     * Sanitize plain text input (removes ALL HTML tags).
     * Use for: names, addresses, general text fields.
     *
     * @param input the user input
     * @return sanitized text with all HTML removed
     */
    public static String sanitizeText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove all HTML tags
        String sanitized = STRICT_POLICY.sanitize(input);

        // Trim whitespace
        sanitized = sanitized.trim();

        // Log if significant sanitization occurred
        if (!input.equals(sanitized) && containsDangerousPatterns(input)) {
            logger.warn("Sanitized potentially malicious input: {} -> {}", input, sanitized);
        }

        return sanitized;
    }

    /**
     * Sanitize rich text input (allows safe HTML formatting).
     * Use for: assignment descriptions, announcements, rich text fields.
     *
     * @param input the user input
     * @return sanitized HTML with only safe tags allowed
     */
    public static String sanitizeRichText(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Allow basic HTML formatting
        String sanitized = BASIC_HTML_POLICY.sanitize(input);

        // Log if dangerous content was removed
        if (!input.equals(sanitized) && containsDangerousPatterns(input)) {
            logger.warn("Sanitized potentially malicious rich text");
        }

        return sanitized;
    }

    /**
     * Sanitize extended HTML input (allows more HTML but still safe).
     * Use for: rich text editors with links and tables.
     *
     * @param input the user input
     * @return sanitized HTML with extended safe tags allowed
     */
    public static String sanitizeExtendedHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        return EXTENDED_HTML_POLICY.sanitize(input);
    }

    // ============================================================
    // Validation Methods
    // ============================================================

    /**
     * Validate and sanitize username.
     * Only allows alphanumeric characters, underscores, and dots (3-50 chars).
     *
     * @param username the username to validate
     * @return true if valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Validate and sanitize email address.
     *
     * @param email the email to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate and sanitize phone number.
     *
     * @param phone the phone number to validate
     * @return true if valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate file name (prevent path traversal and special characters).
     *
     * @param filename the file name to validate
     * @return true if valid
     */
    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        // Check for path traversal
        if (PATH_TRAVERSAL_PATTERN.matcher(filename).find()) {
            logger.warn("Path traversal attempt detected in filename: {}", filename);
            return false;
        }

        // Check against allowed pattern
        return FILENAME_PATTERN.matcher(filename).matches();
    }

    // ============================================================
    // Threat Detection Methods
    // ============================================================

    /**
     * Check if input contains potential SQL injection patterns.
     * Note: This is NOT a substitute for parameterized queries.
     * Always use PreparedStatement or JPA for database queries.
     *
     * @param input the input to check
     * @return true if SQL injection pattern detected
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Check if input contains potential XSS patterns.
     *
     * @param input the input to check
     * @return true if XSS pattern detected
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Check if input contains path traversal patterns.
     *
     * @param input the input to check
     * @return true if path traversal pattern detected
     */
    public static boolean containsPathTraversal(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    /**
     * Check if input contains any dangerous patterns.
     *
     * @param input the input to check
     * @return true if any dangerous pattern detected
     */
    public static boolean containsDangerousPatterns(String input) {
        return containsSqlInjection(input)
            || containsXss(input)
            || containsPathTraversal(input);
    }

    // ============================================================
    // Sanitization for Specific Field Types
    // ============================================================

    /**
     * Sanitize student name (allows letters, spaces, hyphens, apostrophes).
     *
     * @param name the name to sanitize
     * @return sanitized name
     */
    public static String sanitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        // Remove HTML
        String sanitized = STRICT_POLICY.sanitize(name);

        // Remove any characters that aren't letters, spaces, hyphens, or apostrophes
        sanitized = sanitized.replaceAll("[^a-zA-Z\\s'-]", "");

        // Trim and collapse multiple spaces
        sanitized = sanitized.trim().replaceAll("\\s+", " ");

        return sanitized;
    }

    /**
     * Sanitize address field.
     *
     * @param address the address to sanitize
     * @return sanitized address
     */
    public static String sanitizeAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }

        // Remove HTML
        String sanitized = STRICT_POLICY.sanitize(address);

        // Allow letters, numbers, spaces, and common address characters
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s.,#-]", "");

        return sanitized.trim();
    }

    /**
     * Sanitize numeric input (student ID, grade, etc.).
     *
     * @param input the numeric input
     * @return sanitized numeric string
     */
    public static String sanitizeNumeric(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Keep only digits
        return input.replaceAll("[^0-9]", "");
    }

    /**
     * Sanitize alphanumeric input (course codes, room numbers).
     *
     * @param input the alphanumeric input
     * @return sanitized alphanumeric string
     */
    public static String sanitizeAlphanumeric(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Remove HTML
        String sanitized = STRICT_POLICY.sanitize(input);

        // Keep only alphanumeric characters
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9]", "");

        return sanitized.trim();
    }

    // ============================================================
    // Output Encoding (for display in UI)
    // ============================================================

    /**
     * Encode text for safe HTML output.
     * Escapes <, >, &, ", ' characters.
     *
     * @param text the text to encode
     * @return HTML-encoded text
     */
    public static String encodeHtml(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("/", "&#x2F;");
    }

    /**
     * Encode text for use in JavaScript strings.
     *
     * @param text the text to encode
     * @return JavaScript-safe encoded text
     */
    public static String encodeJavaScript(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("<", "\\x3C")
            .replace(">", "\\x3E");
    }

    // ============================================================
    // Utility Methods
    // ============================================================

    /**
     * Limit string length to prevent buffer overflow attacks.
     *
     * @param input the input string
     * @param maxLength maximum allowed length
     * @return truncated string
     */
    public static String limitLength(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        if (input.length() <= maxLength) {
            return input;
        }
        logger.warn("Input exceeded max length ({} chars), truncated to {}", input.length(), maxLength);
        return input.substring(0, maxLength);
    }

    /**
     * Remove all whitespace from input.
     *
     * @param input the input string
     * @return string without whitespace
     */
    public static String removeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\s+", "");
    }

    /**
     * Normalize whitespace (collapse multiple spaces into one).
     *
     * @param input the input string
     * @return string with normalized whitespace
     */
    public static String normalizeWhitespace(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }
}
