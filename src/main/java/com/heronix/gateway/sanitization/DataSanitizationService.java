package com.heronix.gateway.sanitization;

import com.heronix.gateway.device.RegisteredDevice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Data Sanitization Service
 *
 * Strips and anonymizes sensitive information before any data leaves the SIS.
 * All external communications MUST pass through this service.
 *
 * Sanitization includes:
 * - Removing direct student identifiers (SSN, full addresses, etc.)
 * - Anonymizing or pseudonymizing personal information
 * - Removing location-specific data
 * - Stripping internal system identifiers
 * - Masking contact information based on permissions
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Secure Gateway Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataSanitizationService {

    // Patterns for detecting sensitive data
    private static final Pattern SSN_PATTERN = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\(?\\d{3}\\)?[-\\s.]?\\d{3}[-\\s.]?\\d{4}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("\\d+\\s+[A-Za-z0-9\\s,]+\\s+(Street|St|Avenue|Ave|Road|Rd|Drive|Dr|Lane|Ln|Court|Ct|Boulevard|Blvd|Way|Place|Pl)");
    private static final Pattern ZIP_PATTERN = Pattern.compile("\\b\\d{5}(-\\d{4})?\\b");
    private static final Pattern IP_PATTERN = Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b");

    // Fields that should always be removed
    private static final Set<String> ALWAYS_REMOVE_FIELDS = Set.of(
        "ssn", "socialSecurityNumber", "social_security_number",
        "password", "passwordHash", "password_hash",
        "pin", "pinCode", "pin_code",
        "securityQuestion", "security_question",
        "securityAnswer", "security_answer",
        "internalId", "internal_id", "systemId", "system_id",
        "databaseId", "database_id", "dbId", "db_id",
        "serverIp", "server_ip", "hostIp", "host_ip",
        "macAddress", "mac_address",
        "gpsCoordinates", "gps_coordinates", "latitude", "longitude",
        "ipAddress", "ip_address", "clientIp", "client_ip"
    );

    // Fields that can be partially masked
    private static final Set<String> MASKABLE_FIELDS = Set.of(
        "email", "emailAddress", "email_address",
        "phone", "phoneNumber", "phone_number", "mobilePhone", "mobile_phone",
        "address", "streetAddress", "street_address", "homeAddress", "home_address",
        "birthDate", "birth_date", "dateOfBirth", "date_of_birth",
        "studentId", "student_id"
    );

    /**
     * Sanitize data based on device permissions
     */
    public Map<String, Object> sanitizeData(Map<String, Object> data,
                                             RegisteredDevice device,
                                             SanitizationContext context) {
        log.debug("Sanitizing data for device: {} (type: {})",
            device.getDeviceId(), device.getDeviceType());

        Map<String, Object> sanitized = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Always remove sensitive fields
            if (shouldRemoveField(key)) {
                log.trace("Removed sensitive field: {}", key);
                continue;
            }

            // Process the value
            Object sanitizedValue = sanitizeValue(key, value, device, context);

            if (sanitizedValue != null) {
                sanitized.put(key, sanitizedValue);
            }
        }

        // Add sanitization metadata
        sanitized.put("_sanitized", true);
        sanitized.put("_sanitizedAt", LocalDateTime.now().toString());
        sanitized.put("_sanitizationVersion", "1.0");

        return sanitized;
    }

    /**
     * Sanitize a student record for external transmission
     */
    public Map<String, Object> sanitizeStudentRecord(Map<String, Object> studentData,
                                                      RegisteredDevice device) {
        Map<String, Object> sanitized = new LinkedHashMap<>();

        // Create a pseudonymous ID instead of real student ID
        String pseudoId = generatePseudonymousId(
            (String) studentData.get("studentId"),
            device.getDeviceId()
        );
        sanitized.put("referenceId", pseudoId);

        // Include name based on permissions
        if (device.hasPermission(RegisteredDevice.DataPermission.STUDENT_BASIC_INFO)) {
            String firstName = (String) studentData.get("firstName");
            String lastName = (String) studentData.get("lastName");

            // Only include first initial and last name
            sanitized.put("studentName", (firstName != null ? firstName.charAt(0) + ". " : "") +
                (lastName != null ? lastName : ""));

            // Include grade level (non-identifying)
            sanitized.put("gradeLevel", studentData.get("gradeLevel"));
        }

        // Include contact info only if permitted (masked)
        if (device.hasPermission(RegisteredDevice.DataPermission.STUDENT_CONTACT_INFO)) {
            String email = (String) studentData.get("parentEmail");
            if (email != null) {
                sanitized.put("contactEmail", maskEmail(email));
            }

            String phone = (String) studentData.get("parentPhone");
            if (phone != null) {
                sanitized.put("contactPhone", maskPhone(phone));
            }
        }

        // Never include: SSN, full address, birth date, medical info
        // These are stripped regardless of permissions

        return sanitized;
    }

    /**
     * Sanitize attendance data for external transmission
     */
    public Map<String, Object> sanitizeAttendanceData(Map<String, Object> attendanceData,
                                                       RegisteredDevice device) {
        Map<String, Object> sanitized = new LinkedHashMap<>();

        if (device.hasPermission(RegisteredDevice.DataPermission.STUDENT_ATTENDANCE)) {
            // Pseudonymous student reference
            String pseudoId = generatePseudonymousId(
                (String) attendanceData.get("studentId"),
                device.getDeviceId()
            );
            sanitized.put("studentRef", pseudoId);

            // Date (without time for privacy)
            Object date = attendanceData.get("date");
            if (date instanceof LocalDateTime) {
                sanitized.put("date", ((LocalDateTime) date).toLocalDate().toString());
            } else if (date instanceof LocalDate) {
                sanitized.put("date", date.toString());
            } else {
                sanitized.put("date", date);
            }

            // Status (present, absent, tardy)
            sanitized.put("status", attendanceData.get("status"));

            // Do NOT include: specific times, location, class periods, teacher names
        }

        return sanitized;
    }

    /**
     * Sanitize notification content
     */
    public NotificationContent sanitizeNotification(NotificationContent notification,
                                                     RegisteredDevice device) {
        NotificationContent sanitized = new NotificationContent();

        // Sanitize recipient
        if (notification.getRecipientEmail() != null) {
            sanitized.setRecipientEmail(notification.getRecipientEmail()); // Keep for delivery
        }

        // Sanitize subject line (remove identifying info)
        String subject = notification.getSubject();
        subject = removePatterns(subject, SSN_PATTERN, "[REDACTED]");
        subject = removePatterns(subject, ADDRESS_PATTERN, "[SCHOOL]");
        sanitized.setSubject(subject);

        // Sanitize body
        String body = notification.getBody();
        body = removePatterns(body, SSN_PATTERN, "[REDACTED]");
        body = removePatterns(body, ADDRESS_PATTERN, "[SCHOOL ADDRESS]");
        body = removePatterns(body, IP_PATTERN, "[INTERNAL]");

        // Remove any school-identifying information
        body = sanitizeSchoolInfo(body);

        sanitized.setBody(body);

        // Remove any attachments with sensitive data
        if (notification.getAttachments() != null) {
            sanitized.setAttachments(sanitizeAttachments(notification.getAttachments()));
        }

        return sanitized;
    }

    /**
     * Sanitize aggregate/statistical data
     */
    public Map<String, Object> sanitizeAggregateData(Map<String, Object> data,
                                                      RegisteredDevice device) {
        if (!device.hasPermission(RegisteredDevice.DataPermission.AGGREGATE_STATISTICS)) {
            log.warn("Device {} does not have permission for aggregate statistics",
                device.getDeviceId());
            return Collections.emptyMap();
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();

        // Include only aggregate values
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Only include numeric aggregates
            if (value instanceof Number) {
                // Round to prevent re-identification through exact values
                if (value instanceof Double || value instanceof Float) {
                    sanitized.put(key, Math.round((Double) value * 100.0) / 100.0);
                } else {
                    sanitized.put(key, value);
                }
            }

            // Include date ranges (not specific dates)
            if (key.contains("Period") || key.contains("Range")) {
                sanitized.put(key, value);
            }
        }

        // Add noise to small counts (k-anonymity protection)
        for (String key : sanitized.keySet()) {
            Object value = sanitized.get(key);
            if (value instanceof Integer && ((Integer) value) < 5) {
                sanitized.put(key, "< 5"); // Protect small groups
            }
        }

        return sanitized;
    }

    // ================== Private Helper Methods ==================

    private boolean shouldRemoveField(String fieldName) {
        String lowerField = fieldName.toLowerCase();
        return ALWAYS_REMOVE_FIELDS.stream()
            .anyMatch(f -> lowerField.contains(f.toLowerCase()));
    }

    private Object sanitizeValue(String key, Object value,
                                  RegisteredDevice device,
                                  SanitizationContext context) {
        if (value == null) {
            return null;
        }

        // Handle nested maps recursively
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = (Map<String, Object>) value;
            return sanitizeData(nested, device, context);
        }

        // Handle lists
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            List<Object> sanitizedList = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mapItem = (Map<String, Object>) item;
                    sanitizedList.add(sanitizeData(mapItem, device, context));
                } else {
                    sanitizedList.add(sanitizeStringValue(key, item));
                }
            }
            return sanitizedList;
        }

        // Handle string values
        if (value instanceof String) {
            return sanitizeStringValue(key, value);
        }

        return value;
    }

    private Object sanitizeStringValue(String key, Object value) {
        if (!(value instanceof String)) {
            return value;
        }

        String str = (String) value;
        String lowerKey = key.toLowerCase();

        // Check if this is a maskable field
        for (String maskable : MASKABLE_FIELDS) {
            if (lowerKey.contains(maskable.toLowerCase())) {
                if (lowerKey.contains("email")) {
                    return maskEmail(str);
                }
                if (lowerKey.contains("phone")) {
                    return maskPhone(str);
                }
                if (lowerKey.contains("address")) {
                    return "[ADDRESS REDACTED]";
                }
                if (lowerKey.contains("birth") || lowerKey.contains("dob")) {
                    return maskBirthDate(str);
                }
                if (lowerKey.contains("studentid")) {
                    return "[ID:" + str.hashCode() + "]";
                }
            }
        }

        // Scan for sensitive patterns in any string
        str = removePatterns(str, SSN_PATTERN, "[SSN-REDACTED]");
        str = removePatterns(str, IP_PATTERN, "[IP-REDACTED]");

        return str;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "[EMAIL REDACTED]";
        }

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        // Show first 2 chars of local part
        String maskedLocal = local.length() > 2
            ? local.substring(0, 2) + "***"
            : "***";

        // Show only domain extension
        String[] domainParts = domain.split("\\.");
        String maskedDomain = domainParts.length > 1
            ? "***." + domainParts[domainParts.length - 1]
            : "***";

        return maskedLocal + "@" + maskedDomain;
    }

    private String maskPhone(String phone) {
        if (phone == null) {
            return "[PHONE REDACTED]";
        }

        // Remove all non-digits
        String digits = phone.replaceAll("\\D", "");

        if (digits.length() >= 10) {
            // Show only last 4 digits
            return "***-***-" + digits.substring(digits.length() - 4);
        }

        return "[PHONE REDACTED]";
    }

    private String maskBirthDate(String date) {
        // Only show year for privacy
        if (date != null && date.length() >= 4) {
            try {
                String year = date.substring(0, 4);
                Integer.parseInt(year);
                return year + "-**-**";
            } catch (NumberFormatException e) {
                // Not a standard format
            }
        }
        return "[DOB REDACTED]";
    }

    private String removePatterns(String text, Pattern pattern, String replacement) {
        if (text == null) {
            return null;
        }
        return pattern.matcher(text).replaceAll(replacement);
    }

    private String sanitizeSchoolInfo(String text) {
        if (text == null) {
            return null;
        }

        // Remove specific school identifiers
        // This is configurable per deployment
        return text
            .replaceAll("(?i)server\\s*:\\s*\\S+", "server: [INTERNAL]")
            .replaceAll("(?i)database\\s*:\\s*\\S+", "database: [INTERNAL]")
            .replaceAll("(?i)schema\\s*:\\s*\\S+", "schema: [INTERNAL]");
    }

    private List<Map<String, Object>> sanitizeAttachments(List<Map<String, Object>> attachments) {
        // For now, remove all attachments - they could contain sensitive data
        // In production, implement file scanning
        log.debug("Attachments removed during sanitization");
        return Collections.emptyList();
    }

    /**
     * Generate a consistent pseudonymous ID for a student
     * The same student+device combination always produces the same pseudo ID
     */
    private String generatePseudonymousId(String studentId, String deviceId) {
        if (studentId == null) {
            return "ANON-" + UUID.randomUUID().toString().substring(0, 8);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combined = studentId + ":" + deviceId + ":heronix-salt";
            byte[] hash = digest.digest(combined.getBytes());

            // Use first 12 chars of base64 encoded hash
            String encoded = Base64.getEncoder().encodeToString(hash);
            return "REF-" + encoded.substring(0, 12).replace("/", "X").replace("+", "Y");
        } catch (Exception e) {
            log.error("Failed to generate pseudonymous ID", e);
            return "ANON-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
