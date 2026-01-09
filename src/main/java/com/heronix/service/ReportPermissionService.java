package com.heronix.service;

import com.heronix.dto.AccessDecision;
import com.heronix.dto.ReportPermission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Report Permission Service
 *
 * Manages and evaluates report access permissions.
 *
 * Features:
 * - Role-based access control
 * - Permission evaluation
 * - Data filtering
 * - Field masking
 * - Access auditing
 *
 * Permission Evaluation:
 * 1. Load user permissions
 * 2. Evaluate against request
 * 3. Apply restrictions
 * 4. Return access decision
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 68 - Report Access Control & Permissions
 */
@Service
@Slf4j
public class ReportPermissionService {

    // In-memory permission storage (in production, use database)
    private final Map<String, List<ReportPermission>> userPermissions = new ConcurrentHashMap<>();
    private final Map<String, List<ReportPermission>> rolePermissions = new ConcurrentHashMap<>();

    /**
     * Initialize default permissions
     */
    public ReportPermissionService() {
        initializeDefaultPermissions();
    }

    /**
     * Check if user has permission to perform action
     */
    public AccessDecision checkPermission(String username, String action, String reportType) {
        log.debug("Checking permission: user={}, action={}, reportType={}", username, action, reportType);

        List<ReportPermission> permissions = getUserPermissions(username);

        if (permissions.isEmpty()) {
            return AccessDecision.denied("No permissions found for user");
        }

        // Find matching permissions
        List<ReportPermission> matchingPermissions = permissions.stream()
                .filter(ReportPermission::isValid)
                .filter(p -> p.isReportTypeAllowed(reportType))
                .filter(p -> p.isActionAllowed(action))
                .sorted(Comparator.comparing(ReportPermission::getPriority, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();

        if (matchingPermissions.isEmpty()) {
            return AccessDecision.denied("No matching permissions for action: " + action);
        }

        // Use highest priority permission
        ReportPermission topPermission = matchingPermissions.get(0);

        // Build access decision
        AccessDecision decision = AccessDecision.builder()
                .decision(AccessDecision.Decision.GRANTED)
                .granted(true)
                .reason("Permission granted via: " + topPermission.getPermissionName())
                .build();

        decision.getAppliedPermissions().add(topPermission);

        // Apply data restrictions
        if (topPermission.getDataRestriction() != null &&
                topPermission.getDataRestriction() != ReportPermission.DataRestriction.NONE) {
            decision.setDecision(AccessDecision.Decision.CONDITIONAL);
            applyDataRestrictions(decision, topPermission);
        }

        // Apply field masking
        if (topPermission.getMaskedFields() != null && !topPermission.getMaskedFields().isEmpty()) {
            decision.getMaskedFields().addAll(topPermission.getMaskedFields());
        }

        log.info("Access decision for {}: {}", username, decision.getDecision());
        return decision;
    }

    /**
     * Check if user can generate report
     */
    public boolean canGenerateReport(String username, String reportType) {
        AccessDecision decision = checkPermission(username, "GENERATE", reportType);
        return decision.getGranted();
    }

    /**
     * Check if user can view report
     */
    public boolean canViewReport(String username, String reportType) {
        AccessDecision decision = checkPermission(username, "VIEW", reportType);
        return decision.getGranted();
    }

    /**
     * Check if user can export report
     */
    public boolean canExportReport(String username, String reportType, String format) {
        AccessDecision decision = checkPermission(username, "EXPORT", reportType);
        if (!decision.getGranted()) {
            return false;
        }

        // Check format restriction
        return getUserPermissions(username).stream()
                .filter(ReportPermission::isValid)
                .anyMatch(p -> p.isFormatAllowed(format));
    }

    /**
     * Get user permissions
     */
    public List<ReportPermission> getUserPermissions(String username) {
        List<ReportPermission> permissions = new ArrayList<>();

        // Get direct user permissions
        permissions.addAll(userPermissions.getOrDefault(username, Collections.emptyList()));

        // Get role-based permissions (simplified - in production, query user roles)
        String userRole = getUserRole(username);
        if (userRole != null) {
            permissions.addAll(rolePermissions.getOrDefault(userRole, Collections.emptyList()));
        }

        return permissions;
    }

    /**
     * Add permission for user
     */
    public void addUserPermission(String username, ReportPermission permission) {
        userPermissions.computeIfAbsent(username, k -> new ArrayList<>()).add(permission);
        log.info("Added permission for user {}: {}", username, permission.getPermissionName());
    }

    /**
     * Add permission for role
     */
    public void addRolePermission(String roleName, ReportPermission permission) {
        rolePermissions.computeIfAbsent(roleName, k -> new ArrayList<>()).add(permission);
        log.info("Added permission for role {}: {}", roleName, permission.getPermissionName());
    }

    /**
     * Remove user permission
     */
    public void removeUserPermission(String username, String permissionName) {
        List<ReportPermission> permissions = userPermissions.get(username);
        if (permissions != null) {
            permissions.removeIf(p -> permissionName.equals(p.getPermissionName()));
        }
    }

    /**
     * Clear all permissions for user
     */
    public void clearUserPermissions(String username) {
        userPermissions.remove(username);
        log.info("Cleared all permissions for user: {}", username);
    }

    /**
     * Get all role permissions
     */
    public Map<String, List<ReportPermission>> getAllRolePermissions() {
        return new HashMap<>(rolePermissions);
    }

    /**
     * Apply data restrictions to decision
     */
    private void applyDataRestrictions(AccessDecision decision, ReportPermission permission) {
        switch (permission.getDataRestriction()) {
            case OWN_DEPARTMENT -> decision.addDataRestriction("department=" + permission.getAllowedDepartments());
            case OWN_COURSES -> decision.addDataRestriction("courses=" + permission.getAllowedCourseIds());
            case OWN_STUDENTS -> decision.addDataRestriction("students=" + permission.getAllowedStudentIds());
            case GRADE_LEVEL -> decision.addDataRestriction("gradeLevels=" + permission.getAllowedGradeLevels());
            case CUSTOM_FILTER -> {
                if (permission.getCustomFilter() != null) {
                    decision.addDataRestriction("custom:" + permission.getCustomFilter());
                }
            }
        }
    }

    /**
     * Get user role (simplified - in production, query from User entity)
     */
    private String getUserRole(String username) {
        // Simplified role mapping
        if ("admin".equalsIgnoreCase(username) || username.startsWith("admin")) {
            return "ADMIN";
        } else if (username.contains("teacher")) {
            return "TEACHER";
        } else if (username.contains("counselor")) {
            return "COUNSELOR";
        }
        return "USER";
    }

    /**
     * Initialize default role-based permissions
     */
    private void initializeDefaultPermissions() {
        // Admin - full access
        ReportPermission adminPermission = ReportPermission.builder()
                .permissionName("ADMIN_FULL_ACCESS")
                .description("Full administrative access to all reports")
                .level(ReportPermission.PermissionLevel.ADMIN)
                .scope(ReportPermission.PermissionScope.GLOBAL)
                .active(true)
                .canGenerate(true)
                .canView(true)
                .canExport(true)
                .canSchedule(true)
                .canDelete(true)
                .canShare(true)
                .canModifyParameters(true)
                .dataRestriction(ReportPermission.DataRestriction.NONE)
                .priority(100)
                .build();
        addRolePermission("ADMIN", adminPermission);

        // Teacher - view and generate
        ReportPermission teacherPermission = ReportPermission.builder()
                .permissionName("TEACHER_REPORT_ACCESS")
                .description("Teacher access to class reports")
                .level(ReportPermission.PermissionLevel.GENERATE)
                .scope(ReportPermission.PermissionScope.COURSE)
                .active(true)
                .canGenerate(true)
                .canView(true)
                .canExport(true)
                .canSchedule(false)
                .canDelete(false)
                .canShare(true)
                .dataRestriction(ReportPermission.DataRestriction.OWN_COURSES)
                .allowedFormats(Set.of("PDF", "EXCEL"))
                .priority(50)
                .build();
        addRolePermission("TEACHER", teacherPermission);

        // Counselor - student-focused reports
        ReportPermission counselorPermission = ReportPermission.builder()
                .permissionName("COUNSELOR_REPORT_ACCESS")
                .description("Counselor access to student reports")
                .level(ReportPermission.PermissionLevel.GENERATE)
                .scope(ReportPermission.PermissionScope.STUDENT)
                .active(true)
                .canGenerate(true)
                .canView(true)
                .canExport(true)
                .canSchedule(true)
                .canDelete(false)
                .canShare(true)
                .dataRestriction(ReportPermission.DataRestriction.OWN_STUDENTS)
                .priority(50)
                .build();
        addRolePermission("COUNSELOR", counselorPermission);

        // Default user - view only
        ReportPermission userPermission = ReportPermission.builder()
                .permissionName("USER_VIEW_ACCESS")
                .description("Basic view access")
                .level(ReportPermission.PermissionLevel.VIEW)
                .scope(ReportPermission.PermissionScope.GLOBAL)
                .active(true)
                .canGenerate(false)
                .canView(true)
                .canExport(false)
                .canSchedule(false)
                .canDelete(false)
                .canShare(false)
                .priority(10)
                .build();
        addRolePermission("USER", userPermission);

        log.info("Initialized default role permissions");
    }

    /**
     * Get permission statistics
     */
    public Map<String, Object> getPermissionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userPermissions.size());
        stats.put("totalRoles", rolePermissions.size());
        stats.put("totalUserPermissions", userPermissions.values().stream()
                .mapToInt(List::size).sum());
        stats.put("totalRolePermissions", rolePermissions.values().stream()
                .mapToInt(List::size).sum());
        return stats;
    }
}
