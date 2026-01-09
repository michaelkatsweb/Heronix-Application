package com.heronix.controller.api;

import com.heronix.model.domain.CrisisIntervention;
import com.heronix.model.domain.CrisisIntervention.*;
import com.heronix.service.CrisisInterventionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Crisis Intervention Management
 *
 * Provides comprehensive endpoints for managing crisis situations including:
 * - Crisis incident documentation and tracking
 * - Suicide risk assessments and safety planning
 * - Threat assessments and violence prevention
 * - Emergency response and parent notification
 * - Follow-up care and return-to-school planning
 * - High-risk student identification and monitoring
 *
 * CONFIDENTIALITY NOTE: Crisis intervention data is highly sensitive and
 * protected under student privacy laws (FERPA, HIPAA where applicable).
 * Access should be restricted to authorized personnel only.
 *
 * All endpoints return JSON responses with standard structure:
 * {
 *   "success": true/false,
 *   "data": {...},
 *   "message": "Description of result"
 * }
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/crisis-intervention")
@RequiredArgsConstructor
public class CrisisInterventionApiController {

    private final CrisisInterventionService crisisService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create a new crisis intervention record
     *
     * POST /api/crisis-intervention
     *
     * Request Body: CrisisIntervention object
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCrisisIntervention(
            @RequestBody CrisisIntervention crisisIntervention) {
        try {
            CrisisIntervention saved = crisisService.save(crisisIntervention);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("crisisIntervention", saved);
            response.put("message", "Crisis intervention record created successfully");
            response.put("riskLevel", saved.getRiskLevel());
            response.put("requiresImmediateAction", saved.isImminentRisk());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating crisis intervention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get crisis intervention by ID
     *
     * GET /api/crisis-intervention/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCrisisInterventionById(@PathVariable Long id) {
        try {
            CrisisIntervention crisis = crisisService.findById(id);

            if (crisis == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Crisis intervention not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("crisisIntervention", crisis);
            response.put("daysSinceCrisis", crisis.getDaysSinceCrisis());
            response.put("requiresFollowUp", crisis.needsFollowUp());
            response.put("requiresClearance", crisis.needsClearanceToReturn());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving crisis intervention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all crisis interventions
     *
     * GET /api/crisis-intervention
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCrisisInterventions() {
        try {
            List<CrisisIntervention> interventions = crisisService.findAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", interventions);
            response.put("count", interventions.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving crisis interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update a crisis intervention record
     *
     * PUT /api/crisis-intervention/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCrisisIntervention(
            @PathVariable Long id,
            @RequestBody CrisisIntervention updatedCrisis) {
        try {
            CrisisIntervention existing = crisisService.findById(id);

            if (existing == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Crisis intervention not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            updatedCrisis.setId(id);
            CrisisIntervention updated = crisisService.save(updatedCrisis);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("crisisIntervention", updated);
            response.put("message", "Crisis intervention updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating crisis intervention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a crisis intervention record
     *
     * DELETE /api/crisis-intervention/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCrisisIntervention(@PathVariable Long id) {
        try {
            CrisisIntervention crisis = crisisService.findById(id);

            if (crisis == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Crisis intervention not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            crisisService.delete(crisis);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Crisis intervention deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting crisis intervention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // QUERY OPERATIONS - RISK FILTERING
    // ========================================================================

    /**
     * Get high-risk crisis interventions
     *
     * GET /api/crisis-intervention/high-risk
     */
    @GetMapping("/high-risk")
    public ResponseEntity<Map<String, Object>> getHighRiskInterventions() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> highRisk = all.stream()
                    .filter(CrisisIntervention::isHighRisk)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", highRisk);
            response.put("count", highRisk.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving high-risk interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get imminent risk crisis interventions
     *
     * GET /api/crisis-intervention/imminent-risk
     */
    @GetMapping("/imminent-risk")
    public ResponseEntity<Map<String, Object>> getImminentRiskInterventions() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> imminentRisk = all.stream()
                    .filter(CrisisIntervention::isImminentRisk)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", imminentRisk);
            response.put("count", imminentRisk.size());
            response.put("requiresImmediateAction", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving imminent risk interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get suicide risk interventions
     *
     * GET /api/crisis-intervention/suicide-risk
     */
    @GetMapping("/suicide-risk")
    public ResponseEntity<Map<String, Object>> getSuicideRiskInterventions() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> suicideRisk = all.stream()
                    .filter(CrisisIntervention::isSuicideRisk)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", suicideRisk);
            response.put("count", suicideRisk.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving suicide risk interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get violence risk interventions
     *
     * GET /api/crisis-intervention/violence-risk
     */
    @GetMapping("/violence-risk")
    public ResponseEntity<Map<String, Object>> getViolenceRiskInterventions() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> violenceRisk = all.stream()
                    .filter(CrisisIntervention::isViolenceRisk)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", violenceRisk);
            response.put("count", violenceRisk.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving violence risk interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // QUERY OPERATIONS - TYPE AND STATUS FILTERING
    // ========================================================================

    /**
     * Get interventions by crisis type
     *
     * GET /api/crisis-intervention/type/{crisisType}
     */
    @GetMapping("/type/{crisisType}")
    public ResponseEntity<Map<String, Object>> getInterventionsByType(@PathVariable CrisisType crisisType) {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> filtered = all.stream()
                    .filter(c -> c.getCrisisType() == crisisType)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", filtered);
            response.put("count", filtered.size());
            response.put("crisisType", crisisType);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions by type: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions by date range
     *
     * GET /api/crisis-intervention/date-range
     */
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getInterventionsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> filtered = all.stream()
                    .filter(c -> c.getCrisisDate() != null &&
                                !c.getCrisisDate().isBefore(startDate) &&
                                !c.getCrisisDate().isAfter(endDate))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", filtered);
            response.put("count", filtered.size());
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions by date range: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions requiring emergency services
     *
     * GET /api/crisis-intervention/needs-emergency-services
     */
    @GetMapping("/needs-emergency-services")
    public ResponseEntity<Map<String, Object>> getInterventionsNeedingEmergencyServices() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> needsEmergency = all.stream()
                    .filter(CrisisIntervention::needsEmergencyServices)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", needsEmergency);
            response.put("count", needsEmergency.size());
            response.put("urgentAction", "Emergency services should be contacted immediately");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions needing emergency services: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions requiring parent notification
     *
     * GET /api/crisis-intervention/needs-parent-notification
     */
    @GetMapping("/needs-parent-notification")
    public ResponseEntity<Map<String, Object>> getInterventionsNeedingParentNotification() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> needsNotification = all.stream()
                    .filter(CrisisIntervention::needsParentNotification)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", needsNotification);
            response.put("count", needsNotification.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions needing parent notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // FOLLOW-UP AND CLEARANCE MANAGEMENT
    // ========================================================================

    /**
     * Get interventions requiring follow-up
     *
     * GET /api/crisis-intervention/needs-follow-up
     */
    @GetMapping("/needs-follow-up")
    public ResponseEntity<Map<String, Object>> getInterventionsNeedingFollowUp() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> needsFollowUp = all.stream()
                    .filter(CrisisIntervention::needsFollowUp)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", needsFollowUp);
            response.put("count", needsFollowUp.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions needing follow-up: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions requiring clearance to return
     *
     * GET /api/crisis-intervention/needs-clearance
     */
    @GetMapping("/needs-clearance")
    public ResponseEntity<Map<String, Object>> getInterventionsNeedingClearance() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> needsClearance = all.stream()
                    .filter(CrisisIntervention::needsClearanceToReturn)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", needsClearance);
            response.put("count", needsClearance.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions needing clearance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions with safety plans
     *
     * GET /api/crisis-intervention/with-safety-plan
     */
    @GetMapping("/with-safety-plan")
    public ResponseEntity<Map<String, Object>> getInterventionsWithSafetyPlan() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> withPlan = all.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getSafetyPlanCreated()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", withPlan);
            response.put("count", withPlan.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions with safety plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions without safety plans (high risk only)
     *
     * GET /api/crisis-intervention/missing-safety-plan
     */
    @GetMapping("/missing-safety-plan")
    public ResponseEntity<Map<String, Object>> getInterventionsMissingSafetyPlan() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> missingSafetyPlan = all.stream()
                    .filter(CrisisIntervention::isHighRisk)
                    .filter(c -> !Boolean.TRUE.equals(c.getSafetyPlanCreated()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", missingSafetyPlan);
            response.put("count", missingSafetyPlan.size());
            response.put("warning", "High-risk interventions without safety plans require immediate attention");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions missing safety plan: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // EMERGENCY RESPONSE TRACKING
    // ========================================================================

    /**
     * Get interventions with emergency services called
     *
     * GET /api/crisis-intervention/emergency-services-called
     */
    @GetMapping("/emergency-services-called")
    public ResponseEntity<Map<String, Object>> getInterventionsWithEmergencyServices() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> emergencyServices = all.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getEmergencyServicesCalled()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", emergencyServices);
            response.put("count", emergencyServices.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions with emergency services: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions resulting in hospitalization
     *
     * GET /api/crisis-intervention/hospitalized
     */
    @GetMapping("/hospitalized")
    public ResponseEntity<Map<String, Object>> getHospitalizedInterventions() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> hospitalized = all.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getHospitalTransport()) ||
                                Boolean.TRUE.equals(c.getPsychiatricHold()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", hospitalized);
            response.put("count", hospitalized.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving hospitalized interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get interventions with police involvement
     *
     * GET /api/crisis-intervention/police-involved
     */
    @GetMapping("/police-involved")
    public ResponseEntity<Map<String, Object>> getInterventionsWithPoliceInvolvement() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> policeInvolved = all.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getPoliceInvolved()))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", policeInvolved);
            response.put("count", policeInvolved.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving interventions with police involvement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // STATISTICS AND DASHBOARD
    // ========================================================================

    /**
     * Get crisis intervention dashboard statistics
     *
     * GET /api/crisis-intervention/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            List<CrisisIntervention> all = crisisService.findAll();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalInterventions", all.size());
            stats.put("highRiskCount", all.stream().filter(CrisisIntervention::isHighRisk).count());
            stats.put("imminentRiskCount", all.stream().filter(CrisisIntervention::isImminentRisk).count());
            stats.put("suicideRiskCount", all.stream().filter(CrisisIntervention::isSuicideRisk).count());
            stats.put("violenceRiskCount", all.stream().filter(CrisisIntervention::isViolenceRisk).count());
            stats.put("needsFollowUpCount", all.stream().filter(CrisisIntervention::needsFollowUp).count());
            stats.put("needsClearanceCount", all.stream().filter(CrisisIntervention::needsClearanceToReturn).count());
            stats.put("needsParentNotificationCount", all.stream().filter(CrisisIntervention::needsParentNotification).count());
            stats.put("emergencyServicesCount", all.stream().filter(c -> Boolean.TRUE.equals(c.getEmergencyServicesCalled())).count());
            stats.put("hospitalizedCount", all.stream().filter(c -> Boolean.TRUE.equals(c.getHospitalTransport())).count());
            stats.put("withSafetyPlanCount", all.stream().filter(c -> Boolean.TRUE.equals(c.getSafetyPlanCreated())).count());

            // Crisis type breakdown
            Map<String, Long> typeBreakdown = all.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getCrisisType() != null ? c.getCrisisType().name() : "UNKNOWN",
                            Collectors.counting()
                    ));
            stats.put("crisisTypeBreakdown", typeBreakdown);

            // Risk level breakdown
            Map<String, Long> riskBreakdown = all.stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getRiskLevel() != null ? c.getRiskLevel().name() : "UNKNOWN",
                            Collectors.counting()
                    ));
            stats.put("riskLevelBreakdown", riskBreakdown);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get recent crisis interventions (last 30 days)
     *
     * GET /api/crisis-intervention/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentInterventions(
            @RequestParam(defaultValue = "30") int days) {
        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(days);
            List<CrisisIntervention> all = crisisService.findAll();
            List<CrisisIntervention> recent = all.stream()
                    .filter(c -> c.getCrisisDate() != null && !c.getCrisisDate().isBefore(cutoffDate))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("interventions", recent);
            response.put("count", recent.size());
            response.put("days", days);
            response.put("cutoffDate", cutoffDate);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving recent interventions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // METADATA AND HELP
    // ========================================================================

    /**
     * Get available crisis types
     *
     * GET /api/crisis-intervention/metadata/crisis-types
     */
    @GetMapping("/metadata/crisis-types")
    public ResponseEntity<Map<String, Object>> getCrisisTypes() {
        Map<String, Object> types = new HashMap<>();
        for (CrisisType type : CrisisType.values()) {
            types.put(type.name(), type.getDisplayName());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("crisisTypes", types);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available risk levels
     *
     * GET /api/crisis-intervention/metadata/risk-levels
     */
    @GetMapping("/metadata/risk-levels")
    public ResponseEntity<Map<String, Object>> getRiskLevels() {
        Map<String, Object> levels = new HashMap<>();
        for (RiskLevel level : RiskLevel.values()) {
            levels.put(level.name(), level.getDisplayName());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("riskLevels", levels);
        return ResponseEntity.ok(response);
    }

    /**
     * Get API metadata
     *
     * GET /api/crisis-intervention/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("description", "Crisis Intervention Management API");
        metadata.put("confidentiality", "HIGHLY CONFIDENTIAL - Protected under FERPA and applicable privacy laws");
        metadata.put("features", Arrays.asList(
                "Crisis incident documentation and tracking",
                "Suicide risk assessments and safety planning",
                "Threat assessments and violence prevention",
                "Emergency response and parent notification",
                "Follow-up care and return-to-school planning",
                "High-risk student identification and monitoring"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("metadata", metadata);
        return ResponseEntity.ok(response);
    }

    /**
     * Get API help and usage information
     *
     * GET /api/crisis-intervention/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Crisis Intervention Management API - Comprehensive endpoints for managing crisis situations");
        help.put("confidentiality", "HIGHLY CONFIDENTIAL - Access restricted to authorized personnel only");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("POST /api/crisis-intervention", "Create new crisis intervention record");
        endpoints.put("GET /api/crisis-intervention/{id}", "Get crisis intervention by ID");
        endpoints.put("GET /api/crisis-intervention", "Get all crisis interventions");
        endpoints.put("PUT /api/crisis-intervention/{id}", "Update crisis intervention");
        endpoints.put("GET /api/crisis-intervention/high-risk", "Get high-risk interventions");
        endpoints.put("GET /api/crisis-intervention/suicide-risk", "Get suicide risk interventions");
        endpoints.put("GET /api/crisis-intervention/violence-risk", "Get violence risk interventions");
        endpoints.put("GET /api/crisis-intervention/needs-follow-up", "Get interventions needing follow-up");
        endpoints.put("GET /api/crisis-intervention/needs-clearance", "Get interventions needing clearance");
        endpoints.put("GET /api/crisis-intervention/hospitalized", "Get hospitalized interventions");
        endpoints.put("GET /api/crisis-intervention/dashboard", "Get dashboard statistics");

        help.put("endpoints", endpoints);

        Map<String, String> examples = new LinkedHashMap<>();
        examples.put("Create Intervention", "POST /api/crisis-intervention with CrisisIntervention object");
        examples.put("Get High Risk", "GET /api/crisis-intervention/high-risk");
        examples.put("Get Recent (last 7 days)", "GET /api/crisis-intervention/recent?days=7");
        examples.put("Get By Type", "GET /api/crisis-intervention/type/SUICIDAL_IDEATION");
        examples.put("Get Dashboard", "GET /api/crisis-intervention/dashboard");

        help.put("examples", examples);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("help", help);
        return ResponseEntity.ok(response);
    }
}
