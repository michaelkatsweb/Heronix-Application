package com.heronix.controller.api;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.BehaviorIncident.BehaviorCategory;
import com.heronix.model.domain.BehaviorIncident.BehaviorType;
import com.heronix.model.domain.BehaviorIncident.ContactMethod;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.BehaviorIncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Behavior Incident Management
 *
 * Provides endpoints for managing student behavior incidents (positive and negative):
 * - Incident creation and documentation
 * - Incident tracking and retrieval
 * - Parent contact documentation
 * - Admin referral workflow
 * - Intervention tracking
 * - Evidence attachment
 * - Behavior statistics and analytics
 *
 * Supports both:
 * - POSITIVE behaviors (praise, recognition, achievements)
 * - NEGATIVE behaviors (discipline, violations, concerns)
 *
 * Behavior Categories:
 * - Academic Excellence, Leadership, Community Service (Positive)
 * - Disruption, Defiance, Bullying, Academic Dishonesty (Negative)
 *
 * Severity Levels: MINOR, MODERATE, MAJOR, SEVERE
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 31 - December 29, 2025
 */
@RestController
@RequestMapping("/api/behavior-incidents")
@RequiredArgsConstructor
public class BehaviorIncidentApiController {

    private final BehaviorIncidentService behaviorIncidentService;
    private final StudentRepository studentRepository;

    // ==================== Incident Creation ====================

    @PostMapping
    public ResponseEntity<Map<String, Object>> createIncident(@RequestBody BehaviorIncident incident) {
        try {
            BehaviorIncident created = behaviorIncidentService.saveIncident(incident);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", created);
            response.put("message", "Behavior incident created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create incident: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Incident Retrieval ====================

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getIncidentById(@PathVariable Long id) {
        try {
            BehaviorIncident incident = behaviorIncidentService.getIncidentById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", incident);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incident: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<Map<String, Object>> getIncidentsByStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("incidents", incidents);
            response.put("totalIncidents", incidents.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/by-type")
    public ResponseEntity<Map<String, Object>> getIncidentsByType(
            @PathVariable Long studentId,
            @RequestParam BehaviorType behaviorType) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByStudentAndType(student, behaviorType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("behaviorType", behaviorType);
            response.put("incidents", incidents);
            response.put("count", incidents.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/positive")
    public ResponseEntity<Map<String, Object>> getPositiveIncidents(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<BehaviorIncident> incidents = behaviorIncidentService.getPositiveIncidents(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("positiveIncidents", incidents);
            response.put("count", incidents.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/negative")
    public ResponseEntity<Map<String, Object>> getNegativeIncidents(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<BehaviorIncident> incidents = behaviorIncidentService.getNegativeIncidents(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("negativeIncidents", incidents);
            response.put("count", incidents.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/date-range")
    public ResponseEntity<Map<String, Object>> getIncidentsByDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<BehaviorIncident> incidents = behaviorIncidentService.getIncidentsByStudentAndDateRange(
                student, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("incidents", incidents);
            response.put("count", incidents.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/critical")
    public ResponseEntity<Map<String, Object>> getCriticalIncidents(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "30") int daysBack) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            LocalDate sinceDate = LocalDate.now().minusDays(daysBack);
            List<BehaviorIncident> incidents = behaviorIncidentService.getCriticalIncidentsSince(student, sinceDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("daysBack", daysBack);
            response.put("sinceDate", sinceDate);
            response.put("criticalIncidents", incidents);
            response.put("count", incidents.size());
            response.put("hasCriticalIncidents", !incidents.isEmpty());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/uncontacted")
    public ResponseEntity<Map<String, Object>> getUncontactedIncidents(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<BehaviorIncident> incidents = behaviorIncidentService.getUncontactedParentIncidents(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("uncontactedIncidents", incidents);
            response.put("count", incidents.size());
            response.put("needsAction", !incidents.isEmpty());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incidents: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Incident Update ====================

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateIncident(
            @PathVariable Long id,
            @RequestBody BehaviorIncident incident) {

        try {
            incident.setId(id);
            BehaviorIncident updated = behaviorIncidentService.updateIncident(incident);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", updated);
            response.put("message", "Incident updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update incident: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/parent-contact")
    public ResponseEntity<Map<String, Object>> recordParentContact(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {

        try {
            LocalDate contactDate = LocalDate.parse((String) requestBody.get("contactDate"));
            ContactMethod contactMethod = ContactMethod.valueOf((String) requestBody.get("contactMethod"));

            BehaviorIncident updated = behaviorIncidentService.recordParentContact(id, contactDate, contactMethod);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", updated);
            response.put("message", "Parent contact recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record parent contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/referral")
    public ResponseEntity<Map<String, Object>> markAsRequiringReferral(@PathVariable Long id) {
        try {
            BehaviorIncident updated = behaviorIncidentService.markAsRequiringReferral(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", updated);
            response.put("message", "Incident marked as requiring admin referral");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to mark for referral: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/referral-outcome")
    public ResponseEntity<Map<String, Object>> recordReferralOutcome(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {

        try {
            String outcome = requestBody.get("outcome");
            if (outcome == null || outcome.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Outcome is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            BehaviorIncident updated = behaviorIncidentService.recordReferralOutcome(id, outcome);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", updated);
            response.put("message", "Referral outcome recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record outcome: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/intervention")
    public ResponseEntity<Map<String, Object>> recordIntervention(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {

        try {
            String intervention = requestBody.get("intervention");
            if (intervention == null || intervention.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Intervention is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            BehaviorIncident updated = behaviorIncidentService.recordIntervention(id, intervention);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", updated);
            response.put("message", "Intervention recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record intervention: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{id}/evidence")
    public ResponseEntity<Map<String, Object>> attachEvidence(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {

        try {
            String evidenceFilePath = requestBody.get("evidenceFilePath");
            if (evidenceFilePath == null || evidenceFilePath.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "evidenceFilePath is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            BehaviorIncident updated = behaviorIncidentService.attachEvidence(id, evidenceFilePath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("incident", updated);
            response.put("message", "Evidence attached successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to attach evidence: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Statistics & Analytics ====================

    @GetMapping("/students/{studentId}/statistics")
    public ResponseEntity<Map<String, Object>> getStudentStatistics(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            Long positiveCount = behaviorIncidentService.countPositiveIncidents(student, startDate, endDate);
            Long negativeCount = behaviorIncidentService.countNegativeIncidents(student, startDate, endDate);
            Double behaviorRatio = behaviorIncidentService.calculateBehaviorRatio(student, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("studentId", studentId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("positiveIncidents", positiveCount);
            response.put("negativeIncidents", negativeCount);
            response.put("totalIncidents", positiveCount + negativeCount);
            response.put("behaviorRatio", behaviorRatio);
            response.put("behaviorTrend", positiveCount > negativeCount ? "POSITIVE" :
                                         positiveCount < negativeCount ? "NEGATIVE" : "NEUTRAL");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            long totalIncidents = behaviorIncidentService.getTotalIncidentCount();
            List<Long> studentsWithUncontacted = behaviorIncidentService.getStudentsWithUncontactedIncidents();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalIncidents", totalIncidents);
            dashboard.put("studentsNeedingParentContact", studentsWithUncontacted.size());
            dashboard.put("studentIdsNeedingContact", studentsWithUncontacted);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/behavior-types")
    public ResponseEntity<Map<String, Object>> getBehaviorTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("behaviorTypes", List.of(
            Map.of("type", "POSITIVE", "description", "Positive behaviors - praise, recognition, achievements"),
            Map.of("type", "NEGATIVE", "description", "Negative behaviors - discipline, violations, concerns")
        ));
        return ResponseEntity.ok(types);
    }

    @GetMapping("/reference/contact-methods")
    public ResponseEntity<Map<String, Object>> getContactMethods() {
        Map<String, Object> methods = new HashMap<>();
        methods.put("contactMethods", List.of("PHONE", "EMAIL", "IN_PERSON", "TEXT_MESSAGE", "LETTER"));
        return ResponseEntity.ok(methods);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 31");
        metadata.put("category", "Student Behavior Management");
        metadata.put("description", "Comprehensive behavior incident tracking and management");

        metadata.put("capabilities", List.of(
            "Track positive and negative behavior incidents",
            "Document parent contact and outcomes",
            "Admin referral workflow",
            "Intervention tracking",
            "Evidence attachment",
            "Behavior statistics and analytics",
            "Critical incident identification"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Behavior Incident Management API");

        help.put("commonWorkflows", Map.of(
            "documentIncident", List.of(
                "1. POST /api/behavior-incidents (create incident)",
                "2. POST /api/behavior-incidents/{id}/parent-contact (document contact)",
                "3. POST /api/behavior-incidents/{id}/intervention (record intervention)"
            ),
            "trackStudent", List.of(
                "1. GET /api/behavior-incidents/students/{id}",
                "2. GET /api/behavior-incidents/students/{id}/statistics",
                "3. GET /api/behavior-incidents/students/{id}/critical"
            )
        ));

        help.put("endpoints", Map.of(
            "create", "POST /api/behavior-incidents",
            "getByStudent", "GET /api/behavior-incidents/students/{id}",
            "getStatistics", "GET /api/behavior-incidents/students/{id}/statistics",
            "recordParentContact", "POST /api/behavior-incidents/{id}/parent-contact",
            "markReferral", "POST /api/behavior-incidents/{id}/referral"
        ));

        return ResponseEntity.ok(help);
    }
}
