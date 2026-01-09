package com.heronix.controller.api;

import com.heronix.model.domain.IEP;
import com.heronix.model.domain.IEPService;
import com.heronix.model.enums.IEPStatus;
import com.heronix.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * IEP Management REST API Controller
 *
 * Provides RESTful endpoints for managing IEPs, goals, services, progress monitoring,
 * and notifications via web or mobile applications.
 *
 * Key Features:
 * - Complete CRUD operations for IEPs
 * - Goal tracking and progress monitoring
 * - IEP service management
 * - Notification management
 * - Goal templates by eligibility category
 * - Compliance reporting
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - IEP Management Enhancement
 */
@Slf4j
@RestController
@RequestMapping("/api/ieps")
@RequiredArgsConstructor
@Tag(name = "IEP Management", description = "REST API for Individualized Education Program management")
public class IEPApiController {

    private final IEPManagementService iepService;
    private final IEPGoalTrackingService goalTrackingService;
    private final IEPNotificationService notificationService;

    // ========================================================================
    // IEP CRUD OPERATIONS
    // ========================================================================

    @GetMapping
    @Operation(summary = "Get all IEPs")
    public ResponseEntity<List<IEP>> getAllIEPs(@RequestParam(required = false) String status) {
        log.info("GET /api/ieps - status={}", status);
        try {
            List<IEP> ieps = status != null ?
                iepService.findByStatus(IEPStatus.valueOf(status.toUpperCase())) :
                iepService.findAllActiveIEPs();
            return ResponseEntity.ok(ieps);
        } catch (Exception e) {
            log.error("Error retrieving IEPs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get IEP by ID")
    public ResponseEntity<IEP> getIEPById(@PathVariable Long id) {
        log.info("GET /api/ieps/{}", id);
        return iepService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get active IEP for student")
    public ResponseEntity<IEP> getActiveIEPForStudent(@PathVariable Long studentId) {
        log.info("GET /api/ieps/student/{}", studentId);
        return iepService.findActiveIEPForStudent(studentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}/all")
    @Operation(summary = "Get all IEPs for student (including historical)")
    public ResponseEntity<List<IEP>> getAllIEPsForStudent(@PathVariable Long studentId) {
        log.info("GET /api/ieps/student/{}/all", studentId);
        try {
            return ResponseEntity.ok(iepService.findAllIEPsForStudent(studentId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new IEP")
    public ResponseEntity<IEP> createIEP(@Valid @RequestBody IEP iep) {
        log.info("POST /api/ieps");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(iepService.createIEP(iep));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update IEP")
    public ResponseEntity<IEP> updateIEP(@PathVariable Long id, @Valid @RequestBody IEP iep) {
        log.info("PUT /api/ieps/{}", id);
        try {
            iep.setId(id);
            return ResponseEntity.ok(iepService.updateIEP(iep));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete IEP")
    public ResponseEntity<Void> deleteIEP(@PathVariable Long id) {
        log.info("DELETE /api/ieps/{}", id);
        try {
            iepService.deleteIEP(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate IEP")
    public ResponseEntity<IEP> activateIEP(@PathVariable Long id) {
        log.info("POST /api/ieps/{}/activate", id);
        try {
            return ResponseEntity.ok(iepService.activateIEP(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/expire")
    @Operation(summary = "Expire IEP")
    public ResponseEntity<IEP> expireIEP(@PathVariable Long id) {
        log.info("POST /api/ieps/{}/expire", id);
        try {
            return ResponseEntity.ok(iepService.expireIEP(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // GOAL TRACKING
    // ========================================================================

    @GetMapping("/{iepId}/goals")
    @Operation(summary = "Get goals for IEP")
    public ResponseEntity<List<IEPGoalTrackingService.IEPGoal>> getGoals(@PathVariable Long iepId) {
        log.info("GET /api/ieps/{}/goals", iepId);
        try {
            return ResponseEntity.ok(goalTrackingService.parseGoals(iepId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{iepId}/goals/progress")
    @Operation(summary = "Get goal progress summary")
    public ResponseEntity<IEPGoalTrackingService.GoalProgressSummary> getGoalProgress(@PathVariable Long iepId) {
        log.info("GET /api/ieps/{}/goals/progress", iepId);
        try {
            return ResponseEntity.ok(goalTrackingService.getProgressSummary(iepId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{iepId}/goals/{goalNumber}/progress")
    @Operation(summary = "Record goal progress")
    public ResponseEntity<IEPGoalTrackingService.GoalProgress> recordGoalProgress(
            @PathVariable Long iepId,
            @PathVariable int goalNumber,
            @RequestBody Map<String, Object> progressData) {

        log.info("POST /api/ieps/{}/goals/{}/progress", iepId, goalNumber);
        try {
            IEPGoalTrackingService.GoalProgress progress = goalTrackingService.recordProgress(
                iepId,
                goalNumber,
                Double.parseDouble(progressData.get("progressPercentage").toString()),
                IEPGoalTrackingService.MasteryLevel.valueOf(
                    progressData.get("masteryLevel").toString().toUpperCase()
                ),
                progressData.get("observation").toString(),
                progressData.get("recordedBy").toString()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/goals/templates/{eligibilityCategory}")
    @Operation(summary = "Get goal templates by eligibility category")
    public ResponseEntity<List<IEPGoalTrackingService.GoalTemplate>> getGoalTemplates(
            @PathVariable String eligibilityCategory) {

        log.info("GET /api/ieps/goals/templates/{}", eligibilityCategory);
        try {
            return ResponseEntity.ok(goalTrackingService.getGoalTemplates(eligibilityCategory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // SERVICE MANAGEMENT
    // ========================================================================

    @PostMapping("/{iepId}/services")
    @Operation(summary = "Add service to IEP")
    public ResponseEntity<IEP> addService(
            @PathVariable Long iepId,
            @RequestBody IEPService service) {

        log.info("POST /api/ieps/{}/services", iepId);
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(iepService.addService(iepId, service));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{iepId}/services/{serviceId}")
    @Operation(summary = "Remove service from IEP")
    public ResponseEntity<IEP> removeService(
            @PathVariable Long iepId,
            @PathVariable Long serviceId) {

        log.info("DELETE /api/ieps/{}/services/{}", iepId, serviceId);
        try {
            return ResponseEntity.ok(iepService.removeService(iepId, serviceId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/services/unscheduled")
    @Operation(summary = "Get IEPs with unscheduled services")
    public ResponseEntity<List<IEP>> getIEPsWithUnscheduledServices() {
        log.info("GET /api/ieps/services/unscheduled");
        try {
            return ResponseEntity.ok(iepService.findIEPsWithUnscheduledServices());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // NOTIFICATIONS
    // ========================================================================

    @PostMapping("/{iepId}/notifications/parent-creation")
    @Operation(summary = "Notify parents of IEP creation")
    public ResponseEntity<IEPNotificationService.NotificationResult> notifyParentsOfCreation(
            @PathVariable Long iepId) {

        log.info("POST /api/ieps/{}/notifications/parent-creation", iepId);
        try {
            return ResponseEntity.ok(notificationService.notifyParentsOfIEPCreation(iepId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{iepId}/notifications/parent-review")
    @Operation(summary = "Notify parents of upcoming IEP review")
    public ResponseEntity<IEPNotificationService.NotificationResult> notifyParentsOfReview(
            @PathVariable Long iepId,
            @RequestParam LocalDate meetingDate,
            @RequestParam String meetingLocation) {

        log.info("POST /api/ieps/{}/notifications/parent-review", iepId);
        try {
            return ResponseEntity.ok(notificationService.notifyParentsOfUpcomingReview(
                    iepId, meetingDate, meetingLocation));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{iepId}/notifications/service-providers")
    @Operation(summary = "Notify service providers of new IEP")
    public ResponseEntity<List<IEPNotificationService.NotificationResult>> notifyServiceProviders(
            @PathVariable Long iepId) {

        log.info("POST /api/ieps/{}/notifications/service-providers", iepId);
        try {
            return ResponseEntity.ok(notificationService.notifyServiceProvidersOfNewIEP(iepId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/notifications/batch")
    @Operation(summary = "Send batch notifications")
    public ResponseEntity<IEPNotificationService.BatchNotificationResult> sendBatchNotifications() {
        log.info("POST /api/ieps/notifications/batch");
        try {
            return ResponseEntity.ok(notificationService.sendPendingNotifications());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // QUERIES AND REPORTS
    // ========================================================================

    @GetMapping("/case-manager/{caseManager}")
    @Operation(summary = "Get IEPs by case manager")
    public ResponseEntity<List<IEP>> getIEPsByCaseManager(@PathVariable String caseManager) {
        log.info("GET /api/ieps/case-manager/{}", caseManager);
        try {
            return ResponseEntity.ok(iepService.findByCaseManager(caseManager));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/review-due")
    @Operation(summary = "Get IEPs with review due")
    public ResponseEntity<List<IEP>> getIEPsWithReviewDue() {
        log.info("GET /api/ieps/review-due");
        try {
            return ResponseEntity.ok(iepService.findIEPsWithReviewDue());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring IEPs")
    public ResponseEntity<List<IEP>> getExpiringIEPs(
            @RequestParam(defaultValue = "30") int daysThreshold) {

        log.info("GET /api/ieps/expiring - daysThreshold={}", daysThreshold);
        try {
            return ResponseEntity.ok(iepService.findIEPsNeedingRenewal(daysThreshold));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search IEPs by student name")
    public ResponseEntity<List<IEP>> searchIEPs(@RequestParam String query) {
        log.info("GET /api/ieps/search - query={}", query);
        try {
            return ResponseEntity.ok(iepService.searchByStudentName(query));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    @GetMapping("/statistics/summary")
    @Operation(summary = "Get system statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatistics() {
        log.info("GET /api/ieps/statistics/summary");
        try {
            long activeCount = iepService.countActiveIEPs();
            List<Object[]> categoryCounts = iepService.getIEPCountByEligibilityCategory();
            int totalServiceMinutes = iepService.getTotalServiceMinutesPerWeek();

            Map<String, Object> stats = Map.of(
                "activeIEPs", activeCount,
                "iepsByEligibility", categoryCounts,
                "totalServiceMinutesPerWeek", totalServiceMinutes,
                "timestamp", LocalDate.now().toString()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
