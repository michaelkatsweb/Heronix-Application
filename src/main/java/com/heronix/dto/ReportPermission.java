package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Report Permission DTO
 *
 * Defines access permissions for reports and report data.
 *
 * Permission Levels:
 * - NONE: No access
 * - VIEW: Can view reports
 * - GENERATE: Can generate reports
 * - EXPORT: Can export reports
 * - SCHEDULE: Can schedule reports
 * - ADMIN: Full administrative access
 *
 * Permission Scope:
 * - Global: All reports
 * - Report type: Specific report types
 * - Report instance: Specific report instances
 * - Data scope: Filtered data access
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 68 - Report Access Control & Permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPermission {

    /**
     * Permission level enumeration
     */
    public enum PermissionLevel {
        NONE,           // No access
        VIEW,           // View existing reports
        GENERATE,       // Generate new reports
        EXPORT,         // Export reports
        SCHEDULE,       // Schedule automated reports
        MANAGE,         // Manage report settings
        ADMIN           // Full administrative access
    }

    /**
     * Permission scope enumeration
     */
    public enum PermissionScope {
        GLOBAL,         // All reports
        REPORT_TYPE,    // Specific report type
        DEPARTMENT,     // Department-scoped
        GRADE_LEVEL,    // Grade-level scoped
        COURSE,         // Course-scoped
        STUDENT,        // Student-scoped
        CUSTOM          // Custom scope
    }

    /**
     * Data restriction type
     */
    public enum DataRestriction {
        NONE,               // No restrictions
        OWN_DEPARTMENT,     // Only own department data
        OWN_COURSES,        // Only own courses
        OWN_STUDENTS,       // Only own students
        GRADE_LEVEL,        // Specific grade levels
        CUSTOM_FILTER       // Custom filter expression
    }

    // ============================================================
    // Basic Permission Information
    // ============================================================

    /**
     * Permission ID
     */
    private Long permissionId;

    /**
     * Permission name
     */
    private String permissionName;

    /**
     * Description
     */
    private String description;

    /**
     * Permission level
     */
    private PermissionLevel level;

    /**
     * Permission scope
     */
    private PermissionScope scope;

    /**
     * Active/inactive status
     */
    private Boolean active;

    // ============================================================
    // Target Configuration
    // ============================================================

    /**
     * Role name (if role-based)
     */
    private String roleName;

    /**
     * Username (if user-specific)
     */
    private String username;

    /**
     * User group name (if group-based)
     */
    private String groupName;

    /**
     * Report types allowed
     */
    private Set<String> allowedReportTypes;

    /**
     * Report types denied
     */
    private Set<String> deniedReportTypes;

    /**
     * Report formats allowed
     */
    private Set<String> allowedFormats;

    // ============================================================
    // Data Restrictions
    // ============================================================

    /**
     * Data restriction type
     */
    private DataRestriction dataRestriction;

    /**
     * Allowed departments
     */
    private Set<String> allowedDepartments;

    /**
     * Allowed grade levels
     */
    private Set<String> allowedGradeLevels;

    /**
     * Allowed course IDs
     */
    private Set<Long> allowedCourseIds;

    /**
     * Allowed student IDs
     */
    private Set<Long> allowedStudentIds;

    /**
     * Custom filter expression
     */
    private String customFilter;

    /**
     * Fields to mask (for sensitive data)
     */
    private Set<String> maskedFields;

    // ============================================================
    // Feature Restrictions
    // ============================================================

    /**
     * Can generate reports
     */
    private Boolean canGenerate;

    /**
     * Can view reports
     */
    private Boolean canView;

    /**
     * Can export reports
     */
    private Boolean canExport;

    /**
     * Can schedule reports
     */
    private Boolean canSchedule;

    /**
     * Can delete reports
     */
    private Boolean canDelete;

    /**
     * Can share reports
     */
    private Boolean canShare;

    /**
     * Can modify report parameters
     */
    private Boolean canModifyParameters;

    // ============================================================
    // Time-Based Restrictions
    // ============================================================

    /**
     * Permission valid from
     */
    private LocalDateTime validFrom;

    /**
     * Permission valid until
     */
    private LocalDateTime validUntil;

    /**
     * Business hours only
     */
    private Boolean businessHoursOnly;

    /**
     * Allowed days of week
     */
    private Set<String> allowedDaysOfWeek;

    // ============================================================
    // Limits and Quotas
    // ============================================================

    /**
     * Maximum reports per day
     */
    private Integer maxReportsPerDay;

    /**
     * Maximum reports per month
     */
    private Integer maxReportsPerMonth;

    /**
     * Maximum export size (bytes)
     */
    private Long maxExportSizeBytes;

    /**
     * Maximum date range (days)
     */
    private Integer maxDateRangeDays;

    // ============================================================
    // Audit and Metadata
    // ============================================================

    /**
     * Created by
     */
    private String createdBy;

    /**
     * Created at
     */
    private LocalDateTime createdAt;

    /**
     * Modified by
     */
    private String modifiedBy;

    /**
     * Modified at
     */
    private LocalDateTime modifiedAt;

    /**
     * Priority (higher = more important)
     */
    private Integer priority;

    /**
     * Additional metadata
     */
    private java.util.Map<String, Object> metadata;

    // ============================================================
    // Helper Methods
    // ============================================================

    /**
     * Check if permission is currently valid
     */
    public boolean isValid() {
        if (!Boolean.TRUE.equals(active)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();

        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }

        if (validUntil != null && now.isAfter(validUntil)) {
            return false;
        }

        return true;
    }

    /**
     * Check if report type is allowed
     */
    public boolean isReportTypeAllowed(String reportType) {
        if (deniedReportTypes != null && deniedReportTypes.contains(reportType)) {
            return false;
        }

        if (allowedReportTypes != null && !allowedReportTypes.isEmpty()) {
            return allowedReportTypes.contains(reportType);
        }

        return true; // Allow by default if no restrictions
    }

    /**
     * Check if format is allowed
     */
    public boolean isFormatAllowed(String format) {
        if (allowedFormats == null || allowedFormats.isEmpty()) {
            return true; // Allow all formats if not specified
        }

        return allowedFormats.contains(format);
    }

    /**
     * Check if action is allowed
     */
    public boolean isActionAllowed(String action) {
        return switch (action.toUpperCase()) {
            case "VIEW" -> Boolean.TRUE.equals(canView);
            case "GENERATE" -> Boolean.TRUE.equals(canGenerate);
            case "EXPORT" -> Boolean.TRUE.equals(canExport);
            case "SCHEDULE" -> Boolean.TRUE.equals(canSchedule);
            case "DELETE" -> Boolean.TRUE.equals(canDelete);
            case "SHARE" -> Boolean.TRUE.equals(canShare);
            default -> false;
        };
    }

    /**
     * Get effective permission level
     */
    public PermissionLevel getEffectiveLevel() {
        if (!isValid()) {
            return PermissionLevel.NONE;
        }

        return level != null ? level : PermissionLevel.NONE;
    }

    /**
     * Check if has admin access
     */
    public boolean hasAdminAccess() {
        return isValid() && level == PermissionLevel.ADMIN;
    }

    /**
     * Check if has at least specified level
     */
    public boolean hasMinimumLevel(PermissionLevel requiredLevel) {
        if (!isValid()) {
            return false;
        }

        PermissionLevel currentLevel = getEffectiveLevel();
        if (currentLevel == PermissionLevel.ADMIN) {
            return true; // Admin has all permissions
        }

        // Define level hierarchy
        List<PermissionLevel> hierarchy = List.of(
                PermissionLevel.NONE,
                PermissionLevel.VIEW,
                PermissionLevel.GENERATE,
                PermissionLevel.EXPORT,
                PermissionLevel.SCHEDULE,
                PermissionLevel.MANAGE,
                PermissionLevel.ADMIN
        );

        int currentIndex = hierarchy.indexOf(currentLevel);
        int requiredIndex = hierarchy.indexOf(requiredLevel);

        return currentIndex >= requiredIndex;
    }
}
