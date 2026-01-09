package com.heronix.controller.api;

import com.heronix.model.domain.Medication;
import com.heronix.model.domain.MedicationAdministration;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.MedicationAdministration.AdministrationRoute;
import com.heronix.model.domain.MedicationAdministration.AdministrationReason;
import com.heronix.repository.StudentRepository;
import com.heronix.service.MedicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Medication Management
 *
 * Provides endpoints for managing student medications and administration:
 * - Medication CRUD operations (create, read, update, discontinue)
 * - Medication administration logging with audit trail
 * - Inventory management and stock alerts
 * - Expiration tracking and reorder notifications
 * - Compliance verification (authorization, controlled substances)
 * - Parent notification tracking
 *
 * Features:
 * - Scheduled vs PRN (as-needed) medication tracking
 * - Multiple administration routes (Oral, Injection, Inhalation, Topical, etc.)
 * - Student medication refusal documentation
 * - Controlled substance audit trail
 * - Low stock and expiration alerts
 * - Missing authorization tracking
 * - Daily administration requirements
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 33 - December 29, 2025
 */
@RestController
@RequestMapping("/api/medication")
@RequiredArgsConstructor
public class MedicationApiController {

    private final MedicationService medicationService;
    private final StudentRepository studentRepository;

    // ==================== Medication CRUD ====================

    @PostMapping("/medications")
    public ResponseEntity<Map<String, Object>> createMedication(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String medicationName = (String) requestBody.get("medicationName");
            String dosage = (String) requestBody.get("dosage");
            String purpose = (String) requestBody.get("purpose");
            String prescribingPhysician = (String) requestBody.get("prescribingPhysician");
            LocalDate startDate = LocalDate.parse((String) requestBody.get("startDate"));
            Long createdByStaffId = Long.valueOf(requestBody.get("createdByStaffId").toString());

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            Medication created = medicationService.createMedication(
                student, medicationName, dosage, purpose, prescribingPhysician,
                startDate, createdByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medication", created);
            response.put("message", "Medication created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create medication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/{id}")
    public ResponseEntity<Map<String, Object>> getMedicationById(@PathVariable Long id) {
        try {
            Medication medication = medicationService.getMedicationById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medication", medication);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get medication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/medications/{id}")
    public ResponseEntity<Map<String, Object>> updateMedication(
            @PathVariable Long id,
            @RequestBody Medication medication,
            @RequestParam Long updatedByStaffId) {

        try {
            medication.setId(id);
            Medication updated = medicationService.updateMedication(medication, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medication", updated);
            response.put("message", "Medication updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update medication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/medications/{id}/discontinue")
    public ResponseEntity<Map<String, Object>> discontinueMedication(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long staffId) {

        try {
            Medication updated = medicationService.discontinueMedication(id, reason, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medication", updated);
            response.put("message", "Medication discontinued successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to discontinue medication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Student Medication Queries ====================

    @GetMapping("/medications/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getMedicationsForStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<Medication> medications = medicationService.getMedicationsForStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());
            response.put("studentId", studentId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/student/{studentId}/active")
    public ResponseEntity<Map<String, Object>> getActiveMedicationsForStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<Medication> medications = medicationService.getActiveMedicationsForStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());
            response.put("studentId", studentId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get active medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Medication Administration ====================

    @PostMapping("/administration/administer")
    public ResponseEntity<Map<String, Object>> administerMedication(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long medicationId = Long.valueOf(requestBody.get("medicationId").toString());
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String doseGiven = (String) requestBody.get("doseGiven");
            String routeStr = (String) requestBody.get("administrationRoute");
            String reasonStr = (String) requestBody.get("administrationReason");
            Long staffId = Long.valueOf(requestBody.get("administeredByStaffId").toString());
            String staffName = (String) requestBody.get("administratorName");
            String staffTitle = (String) requestBody.get("administratorTitle");

            Medication medication = medicationService.getMedicationById(medicationId);
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            AdministrationRoute route = AdministrationRoute.valueOf(routeStr);
            AdministrationReason reason = AdministrationReason.valueOf(reasonStr);

            MedicationAdministration administration = medicationService.administerMedication(
                medication, student, doseGiven, route, reason, staffId, staffName, staffTitle);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("administration", administration);
            response.put("message", "Medication administered successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to administer medication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/administration/record-refusal")
    public ResponseEntity<Map<String, Object>> recordMedicationRefusal(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long medicationId = Long.valueOf(requestBody.get("medicationId").toString());
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String refusalReason = (String) requestBody.get("refusalReason");
            Long staffId = Long.valueOf(requestBody.get("staffId").toString());
            String staffName = (String) requestBody.get("staffName");

            Medication medication = medicationService.getMedicationById(medicationId);
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            MedicationAdministration administration = medicationService.recordMedicationRefusal(
                medication, student, refusalReason, staffId, staffName);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("administration", administration);
            response.put("message", "Medication refusal recorded successfully");
            response.put("parentNotificationRequired", true);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record refusal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/administration/history/medication/{medicationId}")
    public ResponseEntity<Map<String, Object>> getAdministrationHistory(@PathVariable Long medicationId) {
        try {
            Medication medication = medicationService.getMedicationById(medicationId);
            List<MedicationAdministration> history = medicationService.getAdministrationHistory(medication);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);
            response.put("count", history.size());
            response.put("medicationId", medicationId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/administration/history/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getAdministrationHistoryForStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<MedicationAdministration> history = medicationService.getAdministrationHistoryForStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);
            response.put("count", history.size());
            response.put("studentId", studentId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Inventory Management ====================

    @PostMapping("/medications/{id}/inventory/update")
    public ResponseEntity<Map<String, Object>> updateInventory(
            @PathVariable Long id,
            @RequestParam Integer quantityUsed) {

        try {
            Medication updated = medicationService.updateInventoryAfterAdministration(id, quantityUsed);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medication", updated);
            response.put("remainingQuantity", updated.getQuantityOnHand());
            response.put("message", "Inventory updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update inventory: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/medications/{id}/inventory/restock")
    public ResponseEntity<Map<String, Object>> restockMedication(
            @RequestBody Map<String, Object> requestBody,
            @PathVariable Long id) {

        try {
            Integer quantityAdded = Integer.valueOf(requestBody.get("quantityAdded").toString());
            String lotNumber = (String) requestBody.get("lotNumber");
            LocalDate expirationDate = LocalDate.parse((String) requestBody.get("expirationDate"));

            Medication updated = medicationService.restockMedication(
                id, quantityAdded, lotNumber, expirationDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medication", updated);
            response.put("newQuantity", updated.getQuantityOnHand());
            response.put("message", "Medication restocked successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to restock medication: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/alerts/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockMedications() {
        try {
            List<Medication> medications = medicationService.getLowStockMedications();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get low stock medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/alerts/expired")
    public ResponseEntity<Map<String, Object>> getExpiredMedications() {
        try {
            List<Medication> medications = medicationService.getExpiredMedications();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get expired medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/alerts/expiring-soon")
    public ResponseEntity<Map<String, Object>> getMedicationsExpiringSoon() {
        try {
            List<Medication> medications = medicationService.getMedicationsExpiringSoon();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get expiring medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Compliance & Alerts ====================

    @GetMapping("/medications/alerts/missing-authorization")
    public ResponseEntity<Map<String, Object>> getMedicationsMissingAuthorization() {
        try {
            List<Medication> medications = medicationService.getMedicationsMissingAuthorization();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/due-today")
    public ResponseEntity<Map<String, Object>> getMedicationsRequiringAdministrationToday() {
        try {
            List<Medication> medications = medicationService.getMedicationsRequiringAdministrationToday();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("medications", medications);
            response.put("count", medications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get due medications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/administration/pending-notifications")
    public ResponseEntity<Map<String, Object>> getPendingParentNotifications() {
        try {
            List<MedicationAdministration> notifications = medicationService.getPendingParentNotifications();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notifications", notifications);
            response.put("count", notifications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get pending notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/controlled-substances")
    public ResponseEntity<Map<String, Object>> getControlledSubstances() {
        try {
            List<Medication> medications = medicationService.getControlledSubstances();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("controlledSubstances", medications);
            response.put("count", medications.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get controlled substances: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/medications/{id}/audit-trail")
    public ResponseEntity<Map<String, Object>> getAuditTrail(@PathVariable Long id) {
        try {
            Medication medication = medicationService.getMedicationById(id);
            List<MedicationAdministration> auditTrail = medicationService.getAuditTrail(medication);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("auditTrail", auditTrail);
            response.put("count", auditTrail.size());
            response.put("medicationId", id);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get audit trail: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Create Medication Record",
                "endpoint", "POST /api/medication/medications",
                "description", "Add new medication for a student"
            ),
            Map.of(
                "name", "Administer Medication",
                "endpoint", "POST /api/medication/administration/administer",
                "description", "Record medication administration"
            ),
            Map.of(
                "name", "Today's Medications",
                "endpoint", "GET /api/medication/medications/due-today",
                "description", "View medications requiring administration today"
            ),
            Map.of(
                "name", "Inventory Alerts",
                "endpoint", "GET /api/medication/medications/alerts/low-stock",
                "description", "View low stock and expiring medications"
            ),
            Map.of(
                "name", "Compliance Check",
                "endpoint", "GET /api/medication/medications/alerts/missing-authorization",
                "description", "View medications needing authorization"
            )
        ));

        dashboard.put("features", List.of(
            "Medication CRUD operations with discontinuation tracking",
            "Administration logging with audit trail",
            "Scheduled and PRN (as-needed) medication tracking",
            "Student medication refusal documentation",
            "Inventory management with restock tracking",
            "Low stock, expiration, and authorization alerts",
            "Controlled substance audit trail",
            "Parent notification requirements",
            "Multiple administration routes support",
            "Daily administration requirements"
        ));

        try {
            dashboard.put("statistics", Map.of(
                "activeMedications", medicationService.getActiveMedicationCount(),
                "administrationsRecorded", medicationService.getMedicationAdministrationCount(),
                "dueToday", medicationService.getMedicationsRequiringAdministrationToday().size(),
                "lowStock", medicationService.getLowStockMedications().size(),
                "expiringSoon", medicationService.getMedicationsExpiringSoon().size(),
                "missingAuthorization", medicationService.getMedicationsMissingAuthorization().size()
            ));
        } catch (Exception e) {
            dashboard.put("statisticsError", "Failed to load statistics: " + e.getMessage());
        }

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 33");
        metadata.put("category", "Health Office Management");
        metadata.put("description", "Student medication management and administration tracking");

        metadata.put("capabilities", List.of(
            "Medication CRUD with discontinuation",
            "Administration logging and audit trail",
            "Inventory management and restocking",
            "Expiration and low stock alerts",
            "Authorization compliance tracking",
            "Controlled substance audit trail",
            "Student refusal documentation",
            "Parent notification tracking"
        ));

        metadata.put("endpoints", Map.of(
            "medications", List.of("POST /medications", "GET /medications/{id}", "PUT /medications/{id}", "POST /medications/{id}/discontinue"),
            "queries", List.of("GET /medications/student/{studentId}", "GET /medications/due-today", "GET /medications/controlled-substances"),
            "administration", List.of("POST /administration/administer", "POST /administration/record-refusal", "GET /administration/history/student/{studentId}"),
            "inventory", List.of("POST /medications/{id}/inventory/update", "POST /medications/{id}/inventory/restock", "GET /medications/alerts/low-stock"),
            "compliance", List.of("GET /medications/alerts/missing-authorization", "GET /administration/pending-notifications", "GET /medications/{id}/audit-trail")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Medication Management API - Manage student medications, administration, and compliance");

        help.put("commonWorkflows", Map.of(
            "addMedication", List.of(
                "1. POST /api/medication/medications (create medication record)",
                "2. Upload authorization forms if required",
                "3. POST /api/medication/medications/{id}/inventory/restock (initial stock)",
                "4. Medication ready for administration"
            ),
            "dailyAdministration", List.of(
                "1. GET /api/medication/medications/due-today (view today's schedule)",
                "2. POST /api/medication/administration/administer (administer each medication)",
                "3. POST /api/medication/administration/record-refusal (if student refuses)",
                "4. Review pending parent notifications"
            ),
            "inventoryManagement", List.of(
                "1. GET /api/medication/medications/alerts/low-stock",
                "2. GET /api/medication/medications/alerts/expiring-soon",
                "3. POST /api/medication/medications/{id}/inventory/restock (restock as needed)",
                "4. Monitor inventory levels regularly"
            )
        ));

        help.put("administrationRoutes", List.of(
            "ORAL - By mouth",
            "INJECTION - Injectable medication",
            "INHALATION - Inhaler or nebulizer",
            "TOPICAL - Applied to skin",
            "SUBLINGUAL - Under the tongue",
            "RECTAL - Rectal administration",
            "OPHTHALMIC - Eye drops",
            "OTIC - Ear drops",
            "NASAL - Nasal spray"
        ));

        help.put("examples", Map.of(
            "createMedication", "curl -X POST http://localhost:8080/api/medication/medications -H 'Content-Type: application/json' -d '{\"studentId\":123,\"medicationName\":\"Albuterol\",\"dosage\":\"2 puffs\",\"purpose\":\"Asthma\",\"prescribingPhysician\":\"Dr. Smith\",\"startDate\":\"2025-09-01\",\"createdByStaffId\":1}'",
            "administer", "curl -X POST http://localhost:8080/api/medication/administration/administer -H 'Content-Type: application/json' -d '{\"medicationId\":1,\"studentId\":123,\"doseGiven\":\"2 puffs\",\"administrationRoute\":\"INHALATION\",\"administrationReason\":\"SCHEDULED\",\"administeredByStaffId\":5,\"administratorName\":\"Nurse Jones\",\"administratorTitle\":\"RN\"}'",
            "getDueToday", "curl http://localhost:8080/api/medication/medications/due-today"
        ));

        help.put("notes", Map.of(
            "authorization", "Medications may require parent authorization - check missing authorization alerts",
            "controlledSubstances", "Special audit trail maintained for controlled substances",
            "inventory", "Inventory tracking is optional - set quantityOnHand to enable",
            "refusal", "Student refusals require parent notification"
        ));

        return ResponseEntity.ok(help);
    }
}
