package com.heronix.repository;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.AuditLog.AuditAction;
import com.heronix.model.domain.AuditLog.AuditSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 *
 * Provides data access methods for audit log operations including:
 * - Standard CRUD operations (via JpaRepository)
 * - Querying by username, action, date range
 * - Finding failed authentication attempts
 * - Retrieving security incidents
 * - Generating compliance reports
 *
 * PERFORMANCE NOTE:
 * The audit_logs table can grow very large. All queries use indexed columns
 * (timestamp, username, action, ip_address) to ensure fast performance.
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ============================================================
    // Query by Username
    // ============================================================

    /**
     * Find all audit logs for a specific user, ordered by most recent first.
     *
     * @param username the username to search for
     * @return list of audit logs for the user
     */
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    /**
     * Find audit logs for a user within a date range.
     *
     * @param username the username to search for
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of audit logs
     */
    List<AuditLog> findByUsernameAndTimestampBetweenOrderByTimestampDesc(
        String username, LocalDateTime startDate, LocalDateTime endDate);

    // ============================================================
    // Query by Action
    // ============================================================

    /**
     * Find all audit logs for a specific action type.
     *
     * @param action the action to search for
     * @return list of audit logs for the action
     */
    List<AuditLog> findByActionOrderByTimestampDesc(AuditAction action);

    /**
     * Find audit logs for a specific action within a date range.
     *
     * @param action the action to search for
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of audit logs
     */
    List<AuditLog> findByActionAndTimestampBetweenOrderByTimestampDesc(
        AuditAction action, LocalDateTime startDate, LocalDateTime endDate);

    // ============================================================
    // Query by Entity
    // ============================================================

    /**
     * Find all audit logs related to a specific entity.
     * Useful for viewing complete history of a student, grade, etc.
     *
     * @param entityType type of entity (e.g., "Student", "Grade")
     * @param entityId ID of the entity
     * @return list of audit logs for the entity
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(
        String entityType, Long entityId);

    // ============================================================
    // Security Monitoring Queries
    // ============================================================

    /**
     * Find failed login attempts for a specific username.
     * Used to detect brute force attacks.
     *
     * @param username the username to check
     * @param since look back this far in time
     * @return list of failed login attempts
     */
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username " +
           "AND a.action = 'LOGIN_FAILURE' " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttempts(
        @Param("username") String username,
        @Param("since") LocalDateTime since);

    /**
     * Find failed login attempts from a specific IP address.
     * Used to detect distributed brute force attacks.
     *
     * @param ipAddress the IP address to check
     * @param since look back this far in time
     * @return list of failed login attempts
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress " +
           "AND a.action = 'LOGIN_FAILURE' " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttemptsByIp(
        @Param("ipAddress") String ipAddress,
        @Param("since") LocalDateTime since);

    /**
     * Find all security events (rate limits, CSRF, injection attempts, etc.).
     *
     * @param since look back this far in time
     * @return list of security events
     */
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findSecurityIncidents(@Param("since") LocalDateTime since);

    /**
     * Find all failed actions (success = false).
     *
     * @param since look back this far in time
     * @return list of failed actions
     */
    @Query("SELECT a FROM AuditLog a WHERE a.success = false " +
           "AND a.timestamp >= :since " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedActions(@Param("since") LocalDateTime since);

    // ============================================================
    // Compliance Reporting Queries
    // ============================================================

    /**
     * Find all student data access events (for FERPA compliance).
     * Includes VIEW, CREATE, UPDATE, DELETE of student records.
     *
     * @param studentId the student ID
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = 'Student' " +
           "AND a.entityId = :studentId " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findStudentAccessLogs(
        @Param("studentId") Long studentId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find all grade modifications within a date range.
     * Used for grade change reports.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of grade modification audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = 'Grade' " +
           "AND a.action IN ('GRADE_SUBMIT', 'GRADE_UPDATE', 'GRADE_DELETE') " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findGradeModifications(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * Find all attendance modifications within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of attendance modification audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action IN ('ATTENDANCE_RECORD', 'ATTENDANCE_UPDATE', 'ATTENDANCE_DELETE') " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findAttendanceModifications(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // Query by Date Range
    // ============================================================

    /**
     * Find all audit logs within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of audit logs
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate);

    // ============================================================
    // Query by Severity
    // ============================================================

    /**
     * Find all audit logs with a specific severity level.
     *
     * @param severity the severity level
     * @return list of audit logs
     */
    List<AuditLog> findBySeverityOrderByTimestampDesc(AuditSeverity severity);

    /**
     * Find critical and error severity logs within a date range.
     *
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of high-severity audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.severity IN ('CRITICAL', 'ERROR') " +
           "AND a.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> findHighSeverityLogs(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    // ============================================================
    // Statistics Queries
    // ============================================================

    /**
     * Count failed login attempts for a user within a time window.
     *
     * @param username the username
     * @param since look back this far
     * @return count of failed attempts
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username " +
           "AND a.action = 'LOGIN_FAILURE' " +
           "AND a.timestamp >= :since")
    long countFailedLoginAttempts(
        @Param("username") String username,
        @Param("since") LocalDateTime since);

    /**
     * Count failed login attempts from an IP address within a time window.
     *
     * @param ipAddress the IP address
     * @param since look back this far
     * @return count of failed attempts
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress " +
           "AND a.action = 'LOGIN_FAILURE' " +
           "AND a.timestamp >= :since")
    long countFailedLoginAttemptsByIp(
        @Param("ipAddress") String ipAddress,
        @Param("since") LocalDateTime since);

    /**
     * Count total actions by a user within a time window.
     * Used for detecting unusual activity patterns.
     *
     * @param username the username
     * @param since look back this far
     * @return count of actions
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username " +
           "AND a.timestamp >= :since")
    long countUserActions(
        @Param("username") String username,
        @Param("since") LocalDateTime since);

    // ============================================================
    // Data Cleanup Queries
    // ============================================================

    /**
     * Delete audit logs older than a specific date.
     * Used for archiving/cleanup (run carefully, consider retention policies).
     *
     * @param cutoffDate delete logs before this date
     * @return number of records deleted
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    long deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}
