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
 * Report Permission API Controller
 *
 * REST API endpoints for report access control and permission management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 68 - Report Access Control & Permissions
 */
@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class ReportPermissionApiController {

    private final ReportPermissionService permissionService;

    /**
     * Check permission
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkPermission(@RequestBody Map<String, Object> permissionRequest) {
        try {
            String username = (String) permissionRequest.get("username");
            String action = (String) permissionRequest.get("action");
            String reportType = (String) permissionRequest.get("reportType");

            AccessDecision decision = permissionService.checkPermission(username, action, reportType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("decision", decision);
            response.put("granted", decision.getGranted());
            response.put("reason", decision.getReason());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to check permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Check if user can generate report
     */
    @GetMapping("/can-generate/{username}/{reportType}")
    public ResponseEntity<Map<String, Object>> canGenerateReport(
            @PathVariable String username,
            @PathVariable String reportType) {

        boolean canGenerate = permissionService.canGenerateReport(username, reportType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("canGenerate", canGenerate);
        response.put("username", username);
        response.put("reportType", reportType);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user can view report
     */
    @GetMapping("/can-view/{username}/{reportType}")
    public ResponseEntity<Map<String, Object>> canViewReport(
            @PathVariable String username,
            @PathVariable String reportType) {

        boolean canView = permissionService.canViewReport(username, reportType);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("canView", canView);
        response.put("username", username);
        response.put("reportType", reportType);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user can export report
     */
    @GetMapping("/can-export/{username}/{reportType}/{format}")
    public ResponseEntity<Map<String, Object>> canExportReport(
            @PathVariable String username,
            @PathVariable String reportType,
            @PathVariable String format) {

        boolean canExport = permissionService.canExportReport(username, reportType, format);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("canExport", canExport);
        response.put("username", username);
        response.put("reportType", reportType);
        response.put("format", format);
        return ResponseEntity.ok(response);
    }

    /**
     * Get user permissions
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> getUserPermissions(@PathVariable String username) {
        List<ReportPermission> permissions = permissionService.getUserPermissions(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("permissions", permissions);
        response.put("count", permissions.size());
        response.put("username", username);
        return ResponseEntity.ok(response);
    }

    /**
     * Add user permission
     */
    @PostMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> addUserPermission(
            @PathVariable String username,
            @RequestBody ReportPermission permission) {

        try {
            permissionService.addUserPermission(username, permission);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission added successfully");
            response.put("username", username);
            response.put("permissionName", permission.getPermissionName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add role permission
     */
    @PostMapping("/role/{roleName}")
    public ResponseEntity<Map<String, Object>> addRolePermission(
            @PathVariable String roleName,
            @RequestBody ReportPermission permission) {

        try {
            permissionService.addRolePermission(roleName, permission);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Role permission added successfully");
            response.put("roleName", roleName);
            response.put("permissionName", permission.getPermissionName());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add role permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Remove user permission
     */
    @DeleteMapping("/user/{username}/{permissionName}")
    public ResponseEntity<Map<String, Object>> removeUserPermission(
            @PathVariable String username,
            @PathVariable String permissionName) {

        try {
            permissionService.removeUserPermission(username, permissionName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission removed successfully");
            response.put("username", username);
            response.put("permissionName", permissionName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to remove permission: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Clear all user permissions
     */
    @DeleteMapping("/user/{username}")
    public ResponseEntity<Map<String, Object>> clearUserPermissions(@PathVariable String username) {
        try {
            permissionService.clearUserPermissions(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All permissions cleared for user");
            response.put("username", username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to clear permissions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all role permissions
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getAllRolePermissions() {
        Map<String, List<ReportPermission>> rolePermissions = permissionService.getAllRolePermissions();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("rolePermissions", rolePermissions);
        response.put("roleCount", rolePermissions.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Get permission statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPermissionStatistics() {
        Map<String, Object> stats = permissionService.getPermissionStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("statistics", stats);
        return ResponseEntity.ok(response);
    }
}
