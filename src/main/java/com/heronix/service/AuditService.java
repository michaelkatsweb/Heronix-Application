package com.heronix.service;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.AuditLog.AuditAction;
import com.heronix.model.domain.AuditLog.AuditSeverity;
import com.heronix.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Service for Heronix-SIS
 *
 * Provides centralized audit logging for all security-relevant events.
 * This service ensures comprehensive tracking of user actions for:
 *
 * - FERPA Compliance: Track all access to student data
 * - Security Monitoring: Detect suspicious activity patterns
 * - Forensic Analysis: Investigate security incidents
 * - Accountability: Maintain complete audit trail
 *
 * USAGE PATTERN:
 * All services should inject AuditService and call appropriate log methods:
 *
 * Example:
 * <pre>
 * {@code
 * @Service
 * public class StudentService {
 *     @Autowired
 *     private AuditService auditService;
 *
 *     public Student getStudent(Long id) {
 *         Student student = studentRepository.findById(id);
 *         auditService.logStudentView(id);
 *         return student;
 *     }
 * }
 * }
 * </pre>
 *
 * ASYNCHRONOUS LOGGING:
 * All audit log writes are asynchronous to avoid impacting application performance.
 * Uses @Async with separate thread pool to ensure non-blocking operation.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // ============================================================
    // Core Audit Logging Methods
    // ============================================================

    /**
     * Log an audit event with full details.
     * This is the main method that all other audit methods delegate to.
     *
     * @param action the action being audited
     * @param entityType type of entity (Student, Grade, User, etc.)
     * @param entityId ID of the entity
     * @param details additional details in JSON format
     * @param success whether the action succeeded
     * @param severity severity level of the event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, Long entityId,
                   String details, boolean success, AuditSeverity severity) {
        try {
            String username = getCurrentUsername();
            String ipAddress = getCurrentIpAddress();
            String userAgent = getCurrentUserAgent();
            String sessionId = getCurrentSessionId();

            AuditLog auditLog = AuditLog.builder()
                .username(username)
                .ipAddress(ipAddress)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .success(success)
                .severity(severity)
                .userAgent(userAgent)
                .sessionId(sessionId)
                .build();

            auditLogRepository.save(auditLog);

            // Also log to application log for immediate visibility
            if (severity == AuditSeverity.CRITICAL || severity == AuditSeverity.ERROR) {
                logger.warn("AUDIT [{}]: {} by {} from {} - {}",
                    severity, action, username, ipAddress, details);
            } else {
                logger.info("AUDIT: {} by {} - {}", action, username, entityType);
            }

        } catch (Exception e) {
            // CRITICAL: Never let audit logging failure break the application
            logger.error("Failed to write audit log: " + action, e);
        }
    }

    /**
     * Simplified audit log for successful actions without details.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String entityType, Long entityId) {
        log(action, entityType, entityId, null, true, AuditSeverity.INFO);
    }

    /**
     * Audit log for actions without a specific entity.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action, String details, boolean success, AuditSeverity severity) {
        log(action, null, null, details, success, severity);
    }

    /**
     * Simplified audit log for successful system events.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditAction action) {
        log(action, null, null, null, true, AuditSeverity.INFO);
    }

    // ============================================================
    // Authentication Event Logging
    // ============================================================

    public void logLoginSuccess(String username) {
        log(AuditAction.LOGIN_SUCCESS, null, null,
            String.format("{\"username\":\"%s\"}", username),
            true, AuditSeverity.INFO);
    }

    public void logLoginFailure(String username, String reason) {
        log(AuditAction.LOGIN_FAILURE, null, null,
            String.format("{\"username\":\"%s\",\"reason\":\"%s\"}", username, reason),
            false, AuditSeverity.WARNING);
    }

    public void logLogout() {
        log(AuditAction.LOGOUT);
    }

    public void logPasswordChange(Long userId) {
        log(AuditAction.PASSWORD_CHANGE, "User", userId);
    }

    public void logPasswordResetRequest(String username) {
        log(AuditAction.PASSWORD_RESET_REQUEST, null, null,
            String.format("{\"username\":\"%s\"}", username),
            true, AuditSeverity.INFO);
    }

    public void logSessionTimeout() {
        log(AuditAction.SESSION_TIMEOUT, null, null,
            null, true, AuditSeverity.INFO);
    }

    // ============================================================
    // Student Data Access Logging (FERPA Compliance)
    // ============================================================

    public void logStudentView(Long studentId) {
        log(AuditAction.STUDENT_VIEW, "Student", studentId);
    }

    public void logStudentCreate(Long studentId, String details) {
        log(AuditAction.STUDENT_CREATE, "Student", studentId, details, true, AuditSeverity.INFO);
    }

    public void logStudentUpdate(Long studentId, String fieldsChanged) {
        log(AuditAction.STUDENT_UPDATE, "Student", studentId,
            String.format("{\"fields_changed\":%s}", fieldsChanged),
            true, AuditSeverity.INFO);
    }

    public void logStudentDelete(Long studentId) {
        log(AuditAction.STUDENT_DELETE, "Student", studentId,
            null, true, AuditSeverity.WARNING);
    }

    public void logStudentEnroll(Long studentId, Long courseId) {
        log(AuditAction.STUDENT_ENROLL, "Student", studentId,
            String.format("{\"course_id\":%d}", courseId),
            true, AuditSeverity.INFO);
    }

    public void logStudentWithdraw(Long studentId, Long courseId) {
        log(AuditAction.STUDENT_WITHDRAW, "Student", studentId,
            String.format("{\"course_id\":%d}", courseId),
            true, AuditSeverity.INFO);
    }

    // ============================================================
    // Grade Event Logging
    // ============================================================

    public void logGradeView(Long gradeId) {
        log(AuditAction.GRADE_VIEW, "Grade", gradeId);
    }

    public void logGradeSubmit(Long gradeId, String details) {
        log(AuditAction.GRADE_SUBMIT, "Grade", gradeId, details, true, AuditSeverity.INFO);
    }

    public void logGradeUpdate(Long gradeId, String oldValue, String newValue) {
        log(AuditAction.GRADE_UPDATE, "Grade", gradeId,
            String.format("{\"old_grade\":\"%s\",\"new_grade\":\"%s\"}", oldValue, newValue),
            true, AuditSeverity.INFO);
    }

    public void logGradeDelete(Long gradeId) {
        log(AuditAction.GRADE_DELETE, "Grade", gradeId, null, true, AuditSeverity.WARNING);
    }

    public void logGradeApprove(Long submissionId) {
        log(AuditAction.GRADE_APPROVE, "GradeSubmission", submissionId);
    }

    public void logGradeReject(Long submissionId, String reason) {
        log(AuditAction.GRADE_REJECT, "GradeSubmission", submissionId,
            String.format("{\"reason\":\"%s\"}", reason),
            true, AuditSeverity.INFO);
    }

    // ============================================================
    // Attendance Event Logging
    // ============================================================

    public void logAttendanceView(Long attendanceId) {
        log(AuditAction.ATTENDANCE_VIEW, "Attendance", attendanceId);
    }

    public void logAttendanceRecord(Long attendanceId, String status) {
        log(AuditAction.ATTENDANCE_RECORD, "Attendance", attendanceId,
            String.format("{\"status\":\"%s\"}", status),
            true, AuditSeverity.INFO);
    }

    public void logAttendanceUpdate(Long attendanceId, String oldStatus, String newStatus) {
        log(AuditAction.ATTENDANCE_UPDATE, "Attendance", attendanceId,
            String.format("{\"old_status\":\"%s\",\"new_status\":\"%s\"}", oldStatus, newStatus),
            true, AuditSeverity.INFO);
    }

    public void logAttendanceDelete(Long attendanceId) {
        log(AuditAction.ATTENDANCE_DELETE, "Attendance", attendanceId,
            null, true, AuditSeverity.WARNING);
    }

    /**
     * Log an attendance adjustment (Phase 58 - Attendance Enhancement)
     *
     * @param adjustment the attendance adjustment
     * @param action the action taken (EXCUSE_CREATED, APPROVED, REJECTED, etc.)
     */
    public void logAttendanceAdjustment(com.heronix.model.domain.AttendanceAdjustment adjustment, String action) {
        String details = String.format(
            "{\"adjustment_id\":%d,\"student_id\":%d,\"date\":\"%s\",\"type\":\"%s\",\"action\":\"%s\",\"status\":\"%s\"}",
            adjustment.getId(),
            adjustment.getStudent().getId(),
            adjustment.getAttendanceDate(),
            adjustment.getAdjustmentType(),
            action,
            adjustment.getNewStatus()
        );
        log(AuditAction.ATTENDANCE_UPDATE, "AttendanceAdjustment", adjustment.getId(),
            details, true, AuditSeverity.INFO);
    }

    // ============================================================
    // User Management Event Logging
    // ============================================================

    public void logUserCreate(Long userId, String username, String role) {
        log(AuditAction.USER_CREATE, "User", userId,
            String.format("{\"username\":\"%s\",\"role\":\"%s\"}", username, role),
            true, AuditSeverity.INFO);
    }

    public void logUserUpdate(Long userId, String fieldsChanged) {
        log(AuditAction.USER_UPDATE, "User", userId,
            String.format("{\"fields_changed\":%s}", fieldsChanged),
            true, AuditSeverity.INFO);
    }

    public void logUserDelete(Long userId) {
        log(AuditAction.USER_DELETE, "User", userId, null, true, AuditSeverity.WARNING);
    }

    public void logUserRoleChange(Long userId, String oldRole, String newRole) {
        log(AuditAction.USER_ROLE_CHANGE, "User", userId,
            String.format("{\"old_role\":\"%s\",\"new_role\":\"%s\"}", oldRole, newRole),
            true, AuditSeverity.WARNING);
    }

    public void logUserDisable(Long userId) {
        log(AuditAction.USER_DISABLE, "User", userId, null, true, AuditSeverity.WARNING);
    }

    public void logUserEnable(Long userId) {
        log(AuditAction.USER_ENABLE, "User", userId);
    }

    // ============================================================
    // Security Event Logging
    // ============================================================

    public void logRateLimitExceeded(String identifier) {
        log(AuditAction.RATE_LIMIT_EXCEEDED, null, null,
            String.format("{\"identifier\":\"%s\"}", identifier),
            false, AuditSeverity.WARNING);
    }

    public void logCsrfTokenInvalid() {
        log(AuditAction.CSRF_TOKEN_INVALID, null, null,
            null, false, AuditSeverity.WARNING);
    }

    public void logSuspiciousActivity(String description) {
        log(AuditAction.SUSPICIOUS_ACTIVITY, null, null,
            String.format("{\"description\":\"%s\"}", description),
            false, AuditSeverity.CRITICAL);
    }

    public void logSqlInjectionAttempt(String query) {
        log(AuditAction.SQL_INJECTION_ATTEMPT, null, null,
            String.format("{\"query\":\"%s\"}", query.replace("\"", "\\\"")),
            false, AuditSeverity.CRITICAL);
    }

    public void logXssAttempt(String input) {
        log(AuditAction.XSS_ATTEMPT, null, null,
            String.format("{\"input\":\"%s\"}", input.replace("\"", "\\\"")),
            false, AuditSeverity.CRITICAL);
    }

    public void logFileVirusDetected(String filename, String virusName) {
        log(AuditAction.FILE_VIRUS_DETECTED, null, null,
            String.format("{\"filename\":\"%s\",\"virus\":\"%s\"}", filename, virusName),
            false, AuditSeverity.CRITICAL);
    }

    // ============================================================
    // Report Event Logging
    // ============================================================

    public void logReportGenerate(String reportType) {
        log(AuditAction.REPORT_GENERATE, null, null,
            String.format("{\"report_type\":\"%s\"}", reportType),
            true, AuditSeverity.INFO);
    }

    public void logReportDownload(String reportType, String filename) {
        log(AuditAction.REPORT_DOWNLOAD, null, null,
            String.format("{\"report_type\":\"%s\",\"filename\":\"%s\"}", reportType, filename),
            true, AuditSeverity.INFO);
    }

    // ============================================================
    // Query Methods (Retrieve Audit Logs)
    // ============================================================

    public List<AuditLog> getAuditLogsForUser(String username) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    public List<AuditLog> getFailedLoginAttempts(String username, int lastMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lastMinutes);
        return auditLogRepository.findFailedLoginAttempts(username, since);
    }

    public List<AuditLog> getSecurityIncidents(int lastHours) {
        LocalDateTime since = LocalDateTime.now().minusHours(lastHours);
        return auditLogRepository.findSecurityIncidents(since);
    }

    public long countFailedLoginAttempts(String username, int lastMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lastMinutes);
        return auditLogRepository.countFailedLoginAttempts(username, since);
    }

    public long countFailedLoginAttemptsByIp(String ipAddress, int lastMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lastMinutes);
        return auditLogRepository.countFailedLoginAttemptsByIp(ipAddress, since);
    }

    // ============================================================
    // Helper Methods (Extract Request Context)
    // ============================================================

    /**
     * Get the current authenticated username.
     * Returns "system" if no authentication context available.
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            logger.debug("Could not get current username", e);
        }
        return "system";
    }

    /**
     * Get the IP address of the current request.
     * Returns "unknown" if no request context available.
     */
    private String getCurrentIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                // Check for proxy headers first (X-Forwarded-For)
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }

                // Check X-Real-IP header
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }

                // Fall back to remote address
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            logger.debug("Could not get current IP address", e);
        }
        return "unknown";
    }

    /**
     * Get the User-Agent string from the current request.
     */
    private String getCurrentUserAgent() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    return userAgent.substring(0, 500);
                }
                return userAgent;
            }
        } catch (Exception e) {
            logger.debug("Could not get user agent", e);
        }
        return null;
    }

    /**
     * Get the session ID from the current request.
     */
    private String getCurrentSessionId() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null && request.getSession(false) != null) {
                return request.getSession(false).getId();
            }
        } catch (Exception e) {
            logger.debug("Could not get session ID", e);
        }
        return null;
    }

    /**
     * Get the current HTTP servlet request.
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
}
