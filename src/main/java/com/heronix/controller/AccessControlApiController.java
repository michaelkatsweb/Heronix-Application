package com.heronix.controller;

import com.heronix.dto.AccessDecision;
import com.heronix.dto.ReportPermission;
import com.heronix.service.ReportPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access Control API Controller
 *
 * REST API endpoints for managing report permissions and access control.
 *
 * Provides Endpoints For:
 * - Permission checking
 * - Permission management
 * - Role configuration
 * - Access auditing
 *
 * Endpoints:
 * - POST /api/access/check - Check permission
 * - GET /api/access/permissions/{username} - Get user permissions
 * - POST /api/access/permissions/{username} - Add user permission
 * - DELETE /api/access/permissions/{username} - Remove permission
 * - GET /api/access/roles - Get role permissions
 * - GET /api/access/stats - Get permission statistics
 *
 * Security:
 * - Admin-only access for management endpoints
 * - Self-service for permission checks
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 68 - Report Access Control & Permissions
 */
// @RestController  // Disabled for API server mode
@RequestMapping("/api/access")
@RequiredArgsConstructor
@Slf4j
public class AccessControlApiController {

    private final ReportPermissionService permissionService;

    /**
     * Check permission for action
     *
     * @param request Permission check request
     * @return Access decision
     */
    @PostMapping("/check")
    public ResponseEntity<AccessDecision> checkPermission(@RequestBody PermissionCheckRequest request) {
        log.info("POST /api/access/check - user: {}, action: {}, reportType: {}",
                request.username, request.action, request.reportType);

        try {
            AccessDecision decision = permissionService.checkPermission(
                    request.username,
                    request.action,
                    request.reportType
            );

            return ResponseEntity.ok(decision);

        } catch (Exception e) {
            log.error("Error checking permission", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user permissions
     *
     * @param username Username
     * @return List of permissions
     */
    @GetMapping("/permissions/{username}")
    public ResponseEntity<List<ReportPermission>> getUserPermissions(@PathVariable String username) {
        log.info("GET /api/access/permissions/{}", username);

        try {
            List<ReportPermission> permissions = permissionService.getUserPermissions(username);
            return ResponseEntity.ok(permissions);

        } catch (Exception e) {
            log.error("Error fetching permissions for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add user permission
     *
     * @param username Username
     * @param permission Permission to add
     * @return Success response
     */
    @PostMapping("/permissions/{username}")
    public ResponseEntity<Map<String, String>> addUserPermission(
            @PathVariable String username,
            @RequestBody ReportPermission permission) {

        log.info("POST /api/access/permissions/{} - adding: {}", username, permission.getPermissionName());

        try {
            permissionService.addUserPermission(username, permission);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Permission added successfully");
            response.put("username", username);
            response.put("permission", permission.getPermissionName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error adding permission for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Remove user permission
     *
     * @param username Username
     * @param permissionName Permission name to remove
     * @return Success response
     */
    @DeleteMapping("/permissions/{username}/{permissionName}")
    public ResponseEntity<Map<String, String>> removeUserPermission(
            @PathVariable String username,
            @PathVariable String permissionName) {

        log.info("DELETE /api/access/permissions/{}/{}", username, permissionName);

        try {
            permissionService.removeUserPermission(username, permissionName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Permission removed successfully");
            response.put("username", username);
            response.put("permission", permissionName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error removing permission for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clear all user permissions
     *
     * @param username Username
     * @return Success response
     */
    @DeleteMapping("/permissions/{username}")
    public ResponseEntity<Map<String, String>> clearUserPermissions(@PathVariable String username) {
        log.info("DELETE /api/access/permissions/{} - clearing all", username);

        try {
            permissionService.clearUserPermissions(username);

            Map<String, String> response = new HashMap<>();
            response.put("message", "All permissions cleared");
            response.put("username", username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error clearing permissions for user: {}", username, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all role permissions
     *
     * @return Map of role permissions
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, List<ReportPermission>>> getRolePermissions() {
        log.info("GET /api/access/roles");

        try {
            Map<String, List<ReportPermission>> rolePermissions =
                    permissionService.getAllRolePermissions();
            return ResponseEntity.ok(rolePermissions);

        } catch (Exception e) {
            log.error("Error fetching role permissions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get permission statistics
     *
     * @return Permission statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPermissionStatistics() {
        log.info("GET /api/access/stats");

        try {
            Map<String, Object> stats = permissionService.getPermissionStatistics();
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching permission statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check specific action permission
     *
     * @param username Username
     * @param action Action to check
     * @param reportType Report type
     * @return Permission check result
     */
    @GetMapping("/can/{username}/{action}/{reportType}")
    public ResponseEntity<Map<String, Object>> canPerformAction(
            @PathVariable String username,
            @PathVariable String action,
            @PathVariable String reportType) {

        log.info("GET /api/access/can/{}/{}/{}", username, action, reportType);

        try {
            boolean allowed = switch (action.toUpperCase()) {
                case "GENERATE" -> permissionService.canGenerateReport(username, reportType);
                case "VIEW" -> permissionService.canViewReport(username, reportType);
                default -> false;
            };

            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            response.put("action", action);
            response.put("reportType", reportType);
            response.put("allowed", allowed);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking action permission", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Permission check request DTO
     */
    public record PermissionCheckRequest(
            String username,
            String action,
            String reportType,
            String format,
            Map<String, Object> parameters
    ) {}
}
