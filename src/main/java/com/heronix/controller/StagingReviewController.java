package com.heronix.controller;

import com.heronix.service.StagingDataImportService;
import com.heronix.service.StagingDataImportService.ImportResult;
import com.heronix.service.StagingDataImportService.StagedSubmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Staging Review Controller
 *
 * Admin endpoints for reviewing and importing data from external staging server
 *
 * SECURITY:
 * - All endpoints require ADMIN role
 * - Import actions are logged with username
 * - Read-only operations available to admins
 *
 * ENDPOINTS:
 * - GET  /api/admin/staging/pending - List pending submissions
 * - POST /api/admin/staging/import/students - Import student registrations
 * - POST /api/admin/staging/import/parents - Import parent updates
 * - POST /api/admin/staging/import/teachers - Import teacher submissions
 * - GET  /api/admin/staging/statistics - Get import stats
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since December 27, 2025 - Staging Import Architecture
 */
@Slf4j
// @RestController  // Disabled for API server mode
@RequestMapping("/api/admin/staging")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StagingReviewController {

    private final StagingDataImportService stagingImportService;

    /**
     * Get pending submissions from staging server
     *
     * GET /api/admin/staging/pending?type=STUDENT_REGISTRATION
     *
     * @param type Submission type filter (optional)
     * @return List of pending submissions awaiting review/import
     */
    @GetMapping("/pending")
    public ResponseEntity<List<StagedSubmission>> getPendingSubmissions(
            @RequestParam(required = false) String type) {

        log.info("Fetching pending submissions, type filter: {}", type);

        List<StagedSubmission> submissions = stagingImportService.fetchPendingSubmissions(type);

        return ResponseEntity.ok(submissions);
    }

    /**
     * Import new student registrations from staging server
     *
     * POST /api/admin/staging/import/students
     *
     * Workflow:
     * 1. Fetch approved student registrations from staging
     * 2. Validate and import into SIS database
     * 3. Mark as imported on staging server
     * 4. Return success/failure report
     *
     * @param authentication Current logged-in admin
     * @return Import result with counts and errors
     */
    @PostMapping("/import/students")
    public ResponseEntity<Map<String, Object>> importStudentRegistrations(
            Authentication authentication) {

        String currentUser = authentication.getName();
        log.info("Admin {} initiated student registration import", currentUser);

        ImportResult result = stagingImportService.importNewStudentRegistrations(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.getFailureCount() == 0);
        response.put("successCount", result.getSuccessCount());
        response.put("failureCount", result.getFailureCount());
        response.put("totalProcessed", result.getTotalProcessed());
        response.put("importedIds", result.getImportedIds());
        response.put("errors", result.getErrors());
        response.put("message", result.toString());

        log.info("Student registration import completed: {}", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Import parent/guardian updates from staging server
     *
     * POST /api/admin/staging/import/parents
     *
     * Parents may update:
     * - Contact information (phone, email)
     * - Home address
     * - Emergency contacts
     * - Medical information
     *
     * @param authentication Current logged-in admin
     * @return Import result with counts and errors
     */
    @PostMapping("/import/parents")
    public ResponseEntity<Map<String, Object>> importParentUpdates(
            Authentication authentication) {

        String currentUser = authentication.getName();
        log.info("Admin {} initiated parent update import", currentUser);

        ImportResult result = stagingImportService.importParentUpdates(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.getFailureCount() == 0);
        response.put("successCount", result.getSuccessCount());
        response.put("failureCount", result.getFailureCount());
        response.put("totalProcessed", result.getTotalProcessed());
        response.put("importedIds", result.getImportedIds());
        response.put("errors", result.getErrors());
        response.put("message", result.toString());

        log.info("Parent update import completed: {}", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Import teacher submissions from staging server
     *
     * POST /api/admin/staging/import/teachers
     *
     * Teachers may submit:
     * - Grades
     * - Attendance
     * - Behavior incidents
     * - Assignment scores
     *
     * @param authentication Current logged-in admin
     * @return Import result with counts and errors
     */
    @PostMapping("/import/teachers")
    public ResponseEntity<Map<String, Object>> importTeacherSubmissions(
            Authentication authentication) {

        String currentUser = authentication.getName();
        log.info("Admin {} initiated teacher submission import", currentUser);

        ImportResult result = stagingImportService.importTeacherSubmissions(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.getFailureCount() == 0);
        response.put("successCount", result.getSuccessCount());
        response.put("failureCount", result.getFailureCount());
        response.put("totalProcessed", result.getTotalProcessed());
        response.put("importedIds", result.getImportedIds());
        response.put("errors", result.getErrors());
        response.put("message", result.toString());

        log.info("Teacher submission import completed: {}", result);

        return ResponseEntity.ok(response);
    }

    /**
     * Get import statistics
     *
     * GET /api/admin/staging/statistics
     *
     * Returns:
     * - Total imports this week/month
     * - Success/failure rates
     * - Recent import history
     *
     * @return Statistics summary
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getImportStatistics() {

        log.info("Fetching import statistics");

        String stats = stagingImportService.getImportStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("statistics", stats);
        response.put("message", "Import statistics retrieved successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Health check for staging server connectivity
     *
     * GET /api/admin/staging/health
     *
     * @return Server status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkStagingServerHealth() {

        log.info("Checking staging server health");

        Map<String, Object> response = new HashMap<>();
        response.put("stagingServerConnected", false);
        response.put("message", "Staging server integration not yet configured");
        response.put("note", "This endpoint will check connectivity when staging server is deployed");

        // TODO: Implement actual health check
        // try {
        //     ResponseEntity<String> healthCheck = restTemplate.getForEntity(
        //         stagingServerUrl + "/actuator/health", String.class);
        //     response.put("stagingServerConnected", healthCheck.getStatusCode().is2xxSuccessful());
        // } catch (Exception e) {
        //     response.put("stagingServerConnected", false);
        //     response.put("error", e.getMessage());
        // }

        return ResponseEntity.ok(response);
    }
}
