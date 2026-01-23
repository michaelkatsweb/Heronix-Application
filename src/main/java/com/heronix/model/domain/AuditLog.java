package com.heronix.model.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Audit Log Entity for Heronix-SIS
 *
 * Tracks all security-relevant events and user actions in the system for:
 * - Compliance (FERPA requires audit trails for student data access)
 * - Security monitoring (detect unauthorized access attempts)
 * - Forensic analysis (investigate security incidents)
 * - Accountability (track who did what and when)
 *
 * All sensitive operations are logged, including:
 * - Authentication events (login, logout, failed attempts)
 * - Student data access (view, create, update, delete)
 * - Grade modifications
 * - Attendance changes
 * - User account changes
 * - Permission/role changes
 * - Configuration changes
 *
 * RETENTION POLICY:
 * Audit logs should be retained for at least 7 years per FERPA requirements.
 * Consider archiving old logs to separate storage after 1 year.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_username", columnList = "username"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_ip", columnList = "ip_address")
})
public class AuditLog {

    // ============================================================
    // Audit Action Types (Enum)
    // ============================================================

    /**
     * Enumeration of all auditable actions in the system.
     */
    public enum AuditAction {
        // Authentication Events
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        PASSWORD_CHANGE,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,
        SESSION_TIMEOUT,
        CONCURRENT_SESSION_BLOCKED,

        // Student Data Events
        STUDENT_VIEW,
        STUDENT_CREATE,
        STUDENT_UPDATE,
        STUDENT_DELETE,
        STUDENT_ENROLL,
        STUDENT_WITHDRAW,

        // Grade Events
        GRADE_VIEW,
        GRADE_SUBMIT,
        GRADE_UPDATE,
        GRADE_DELETE,
        GRADE_APPROVE,
        GRADE_REJECT,

        // Attendance Events
        ATTENDANCE_VIEW,
        ATTENDANCE_RECORD,
        ATTENDANCE_UPDATE,
        ATTENDANCE_DELETE,

        // User Management Events
        USER_CREATE,
        USER_UPDATE,
        USER_DELETE,
        USER_ROLE_CHANGE,
        USER_PERMISSION_CHANGE,
        USER_DISABLE,
        USER_ENABLE,

        // Staging Server Events
        STAGING_IMPORT,
        STAGING_APPROVE,
        STAGING_REJECT,
        STAGING_VIEW,

        // Configuration Events
        CONFIG_CHANGE,
        SECURITY_CONFIG_CHANGE,

        // Report Events
        REPORT_GENERATE,
        REPORT_GENERATED,
        REPORT_DOWNLOAD,
        REPORT_VIEW,

        // File Upload Events
        FILE_UPLOAD,
        FILE_DOWNLOAD,
        FILE_DELETE,
        FILE_VIRUS_DETECTED,

        // Security Events
        RATE_LIMIT_EXCEEDED,
        CSRF_TOKEN_INVALID,
        INVALID_SESSION,
        SUSPICIOUS_ACTIVITY,
        SQL_INJECTION_ATTEMPT,
        XSS_ATTEMPT,

        // System Events
        SYSTEM_STARTUP,
        SYSTEM_SHUTDOWN,
        DATABASE_BACKUP,
        DATABASE_RESTORE
    }

    // ============================================================
    // Entity Fields
    // ============================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Timestamp when the event occurred (UTC).
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Username of the user who performed the action.
     * May be null for system-generated events or failed login attempts.
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * IP address from which the action was performed.
     */
    @Column(name = "ip_address", length = 45) // IPv6 max length
    private String ipAddress;

    /**
     * The action that was performed.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    /**
     * Type of entity affected (e.g., "Student", "Grade", "User").
     * Null if action doesn't affect a specific entity.
     */
    @Column(name = "entity_type", length = 100)
    private String entityType;

    /**
     * ID of the affected entity.
     * Null if action doesn't affect a specific entity.
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Additional details about the action in JSON format.
     * Examples:
     * - Failed login: {"reason": "Invalid password"}
     * - Grade update: {"old_grade": 85, "new_grade": 90}
     * - Student update: {"fields_changed": ["email", "phone"]}
     */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /**
     * Whether the action was successful.
     * - true: Action completed successfully
     * - false: Action failed or was blocked
     */
    @Column(name = "success", nullable = false)
    private boolean success;

    /**
     * User agent string from the request (browser/device info).
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Session ID associated with the action.
     * Useful for correlating multiple actions in the same session.
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * Severity level for the event.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20)
    private AuditSeverity severity;

    /**
     * Enumeration for audit event severity levels.
     */
    public enum AuditSeverity {
        INFO,       // Normal operations
        WARNING,    // Suspicious but not critical
        ERROR,      // Failed operations
        CRITICAL    // Security incidents
    }

    // ============================================================
    // Constructors
    // ============================================================

    /**
     * Default constructor (required by JPA).
     */
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
        this.severity = AuditSeverity.INFO;
    }

    /**
     * Constructor for successful actions.
     */
    public AuditLog(String username, String ipAddress, AuditAction action, String entityType, Long entityId) {
        this();
        this.username = username;
        this.ipAddress = ipAddress;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    /**
     * Full constructor.
     */
    public AuditLog(String username, String ipAddress, AuditAction action, String entityType,
                   Long entityId, String details, boolean success, AuditSeverity severity) {
        this();
        this.username = username;
        this.ipAddress = ipAddress;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.success = success;
        this.severity = severity;
    }

    // ============================================================
    // Builder Pattern (for cleaner construction)
    // ============================================================

    public static class Builder {
        private AuditLog auditLog;

        public Builder() {
            this.auditLog = new AuditLog();
        }

        public Builder username(String username) {
            this.auditLog.username = username;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.auditLog.ipAddress = ipAddress;
            return this;
        }

        public Builder action(AuditAction action) {
            this.auditLog.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            this.auditLog.entityType = entityType;
            return this;
        }

        public Builder entityId(Long entityId) {
            this.auditLog.entityId = entityId;
            return this;
        }

        public Builder details(String details) {
            this.auditLog.details = details;
            return this;
        }

        public Builder success(boolean success) {
            this.auditLog.success = success;
            return this;
        }

        public Builder severity(AuditSeverity severity) {
            this.auditLog.severity = severity;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.auditLog.userAgent = userAgent;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.auditLog.sessionId = sessionId;
            return this;
        }

        public AuditLog build() {
            return this.auditLog;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ============================================================
    // Getters and Setters
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isSuccess() {
        return success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Alias for getDetails() - some code expects getDescription()
     */
    public String getDescription() {
        return details;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public AuditSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AuditSeverity severity) {
        this.severity = severity;
    }

    // ============================================================
    // Object Methods
    // ============================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
            "id=" + id +
            ", timestamp=" + timestamp +
            ", username='" + username + '\'' +
            ", ipAddress='" + ipAddress + '\'' +
            ", action=" + action +
            ", entityType='" + entityType + '\'' +
            ", entityId=" + entityId +
            ", success=" + success +
            ", severity=" + severity +
            '}';
    }
}
