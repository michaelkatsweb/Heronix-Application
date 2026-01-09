package com.heronix.controller;

import com.heronix.dto.ReportVersion;
import com.heronix.service.ReportVersionControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Report Version Control API Controller
 *
 * REST API endpoints for report versioning and history management.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 72 - Report Version Control & History
 */
@Slf4j
@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class ReportVersionControlApiController {

    private final ReportVersionControlService versionService;

    /**
     * Create new version
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createVersion(@RequestBody ReportVersion version) {
        try {
            ReportVersion created = versionService.createVersion(version);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version created successfully");
            response.put("versionId", created.getVersionId());
            response.put("versionNumber", created.getFullVersionString());
            response.put("reportId", created.getReportId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create initial version for new report
     */
    @PostMapping("/initial")
    public ResponseEntity<Map<String, Object>> createInitialVersion(@RequestBody Map<String, Object> versionData) {
        try {
            Long reportId = Long.parseLong(versionData.get("reportId").toString());
            String createdBy = (String) versionData.get("createdBy");
            String commitMessage = (String) versionData.getOrDefault("commitMessage", "Initial version");

            ReportVersion created = versionService.createInitialVersion(reportId, createdBy, commitMessage);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Initial version created");
            response.put("versionId", created.getVersionId());
            response.put("versionNumber", created.getFullVersionString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create initial version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create new version from existing
     */
    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> createNewVersion(@RequestBody Map<String, Object> versionData) {
        try {
            Long reportId = Long.parseLong(versionData.get("reportId").toString());
            String createdBy = (String) versionData.get("createdBy");
            String commitMessage = (String) versionData.get("commitMessage");
            ReportVersion.VersionType versionType = ReportVersion.VersionType.valueOf(
                    (String) versionData.getOrDefault("versionType", "MINOR"));

            ReportVersion created = versionService.createNewVersion(reportId, createdBy, commitMessage, versionType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "New version created");
            response.put("versionId", created.getVersionId());
            response.put("versionNumber", created.getFullVersionString());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to create new version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get version by ID
     */
    @GetMapping("/{versionId}")
    public ResponseEntity<Map<String, Object>> getVersion(@PathVariable Long versionId) {
        Optional<ReportVersion> version = versionService.getVersion(versionId);

        if (version.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("version", version.get());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Version not found with ID: " + versionId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get all versions for report
     */
    @GetMapping("/report/{reportId}")
    public ResponseEntity<Map<String, Object>> getVersionsForReport(@PathVariable Long reportId) {
        List<ReportVersion> versions = versionService.getVersionsForReport(reportId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("versions", versions);
        response.put("count", versions.size());
        response.put("reportId", reportId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current version of report
     */
    @GetMapping("/report/{reportId}/current")
    public ResponseEntity<Map<String, Object>> getCurrentVersion(@PathVariable Long reportId) {
        Optional<ReportVersion> version = versionService.getCurrentVersion(reportId);

        if (version.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("version", version.get());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "No current version found for report: " + reportId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get latest version of report
     */
    @GetMapping("/report/{reportId}/latest")
    public ResponseEntity<Map<String, Object>> getLatestVersion(@PathVariable Long reportId) {
        Optional<ReportVersion> version = versionService.getLatestVersion(reportId);

        if (version.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("version", version.get());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "No versions found for report: " + reportId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Set current version
     */
    @PutMapping("/report/{reportId}/current/{versionId}")
    public ResponseEntity<Map<String, Object>> setCurrentVersion(
            @PathVariable Long reportId,
            @PathVariable Long versionId) {

        try {
            versionService.setCurrentVersion(reportId, versionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Current version set successfully");
            response.put("reportId", reportId);
            response.put("versionId", versionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to set current version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update version
     */
    @PutMapping("/{versionId}")
    public ResponseEntity<Map<String, Object>> updateVersion(
            @PathVariable Long versionId,
            @RequestBody ReportVersion version) {

        try {
            version.setVersionId(versionId);
            ReportVersion updated = versionService.updateVersion(version);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version updated successfully");
            response.put("version", updated);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to update version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete version
     */
    @DeleteMapping("/{versionId}")
    public ResponseEntity<Map<String, Object>> deleteVersion(@PathVariable Long versionId) {
        try {
            versionService.deleteVersion(versionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version deleted successfully");
            response.put("versionId", versionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to delete version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Publish version
     */
    @PostMapping("/{versionId}/publish")
    public ResponseEntity<Map<String, Object>> publishVersion(
            @PathVariable Long versionId,
            @RequestBody Map<String, Object> publishData) {

        try {
            String publishedBy = (String) publishData.get("publishedBy");
            versionService.publishVersion(versionId, publishedBy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version published successfully");
            response.put("versionId", versionId);
            response.put("publishedBy", publishedBy);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to publish version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Approve version
     */
    @PostMapping("/{versionId}/approve")
    public ResponseEntity<Map<String, Object>> approveVersion(
            @PathVariable Long versionId,
            @RequestBody Map<String, Object> approvalData) {

        try {
            String approvedBy = (String) approvalData.get("approvedBy");
            String comments = (String) approvalData.getOrDefault("comments", "");
            versionService.approveVersion(versionId, approvedBy, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version approved successfully");
            response.put("versionId", versionId);
            response.put("approvedBy", approvedBy);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to approve version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Review version
     */
    @PostMapping("/{versionId}/review")
    public ResponseEntity<Map<String, Object>> reviewVersion(
            @PathVariable Long versionId,
            @RequestBody Map<String, Object> reviewData) {

        try {
            String reviewedBy = (String) reviewData.get("reviewedBy");
            String comments = (String) reviewData.getOrDefault("comments", "");
            versionService.reviewVersion(versionId, reviewedBy, comments);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version review submitted");
            response.put("versionId", versionId);
            response.put("reviewedBy", reviewedBy);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to review version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Deprecate version
     */
    @PostMapping("/{versionId}/deprecate")
    public ResponseEntity<Map<String, Object>> deprecateVersion(@PathVariable Long versionId) {
        try {
            versionService.deprecateVersion(versionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version deprecated successfully");
            response.put("versionId", versionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to deprecate version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Archive version
     */
    @PostMapping("/{versionId}/archive")
    public ResponseEntity<Map<String, Object>> archiveVersion(@PathVariable Long versionId) {
        try {
            versionService.archiveVersion(versionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Version archived successfully");
            response.put("versionId", versionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to archive version: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Rollback to version
     */
    @PostMapping("/report/{reportId}/rollback/{versionId}")
    public ResponseEntity<Map<String, Object>> rollbackToVersion(
            @PathVariable Long reportId,
            @PathVariable Long versionId,
            @RequestBody Map<String, Object> rollbackData) {

        try {
            String username = (String) rollbackData.get("username");
            ReportVersion rollbackVersion = versionService.rollbackToVersion(reportId, versionId, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rollback successful");
            response.put("newVersionId", rollbackVersion.getVersionId());
            response.put("newVersionNumber", rollbackVersion.getFullVersionString());
            response.put("rolledBackTo", versionId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to rollback: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calculate diff between versions
     */
    @GetMapping("/diff/{fromVersionId}/{toVersionId}")
    public ResponseEntity<Map<String, Object>> calculateDiff(
            @PathVariable Long fromVersionId,
            @PathVariable Long toVersionId) {

        String diff = versionService.calculateDiff(fromVersionId, toVersionId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("diff", diff);
        response.put("fromVersionId", fromVersionId);
        response.put("toVersionId", toVersionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Compare versions
     */
    @GetMapping("/compare/{versionId1}/{versionId2}")
    public ResponseEntity<Map<String, Object>> compareVersions(
            @PathVariable Long versionId1,
            @PathVariable Long versionId2) {

        Map<String, Object> comparison = versionService.compareVersions(versionId1, versionId2);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("comparison", comparison);
        return ResponseEntity.ok(response);
    }
}
