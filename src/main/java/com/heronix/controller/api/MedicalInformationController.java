package com.heronix.controller.api;

import com.heronix.model.domain.MedicalRecord;
import com.heronix.model.domain.MedicalRecord.AllergySeverity;
import com.heronix.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API Controller for Medical Information
 *
 * Provides endpoints for managing:
 * - Medical conditions and chronic illnesses
 * - Allergies (food, medication, environmental)
 * - Medications and prescriptions
 * - Medical alerts and flags
 * - Physician information
 * - Medical insurance
 * - Emergency medical authorization
 * - Health screening results
 * - Immunization records
 * - Physical examinations
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class MedicalInformationController {

    private final MedicalRecordService medicalRecordService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Get or create medical record for student
     * GET /api/medical-records/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<MedicalRecord> getOrCreateMedicalRecord(@PathVariable Long studentId) {
        try {
            MedicalRecord record = medicalRecordService.getOrCreateMedicalRecord(studentId);
            return ResponseEntity.ok(record);
        } catch (IllegalArgumentException e) {
            log.error("Student not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get medical record by ID
     * GET /api/medical-records/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(@PathVariable Long id) {
        return medicalRecordService.getMedicalRecordById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update medical record
     * PUT /api/medical-records/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MedicalRecord> updateMedicalRecord(
            @PathVariable Long id,
            @RequestBody MedicalRecord record) {
        try {
            record.setId(id);
            MedicalRecord updated = medicalRecordService.updateMedicalRecord(record);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete medical record
     * DELETE /api/medical-records/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable Long id) {
        try {
            medicalRecordService.deleteMedicalRecord(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all medical records
     * GET /api/medical-records
     */
    @GetMapping
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());
    }

    // ========================================================================
    // ALLERGY MANAGEMENT
    // ========================================================================

    /**
     * Add food allergy
     * POST /api/medical-records/{id}/allergies/food
     */
    @PostMapping("/{id}/allergies/food")
    public ResponseEntity<MedicalRecord> addFoodAllergy(
            @PathVariable Long id,
            @RequestParam String allergy,
            @RequestParam AllergySeverity severity) {
        try {
            MedicalRecord updated = medicalRecordService.addFoodAllergy(id, allergy, severity);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove food allergy
     * DELETE /api/medical-records/{id}/allergies/food
     */
    @DeleteMapping("/{id}/allergies/food")
    public ResponseEntity<MedicalRecord> removeFoodAllergy(
            @PathVariable Long id,
            @RequestParam String allergy) {
        try {
            MedicalRecord updated = medicalRecordService.removeFoodAllergy(id, allergy);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add medication allergy
     * POST /api/medical-records/{id}/allergies/medication
     */
    @PostMapping("/{id}/allergies/medication")
    public ResponseEntity<MedicalRecord> addMedicationAllergy(
            @PathVariable Long id,
            @RequestParam String allergy,
            @RequestParam AllergySeverity severity) {
        try {
            MedicalRecord updated = medicalRecordService.addMedicationAllergy(id, allergy, severity);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove medication allergy
     * DELETE /api/medical-records/{id}/allergies/medication
     */
    @DeleteMapping("/{id}/allergies/medication")
    public ResponseEntity<MedicalRecord> removeMedicationAllergy(
            @PathVariable Long id,
            @RequestParam String allergy) {
        try {
            MedicalRecord updated = medicalRecordService.removeMedicationAllergy(id, allergy);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add environmental allergy
     * POST /api/medical-records/{id}/allergies/environmental
     */
    @PostMapping("/{id}/allergies/environmental")
    public ResponseEntity<MedicalRecord> addEnvironmentalAllergy(
            @PathVariable Long id,
            @RequestParam String allergy,
            @RequestParam AllergySeverity severity) {
        try {
            MedicalRecord updated = medicalRecordService.addEnvironmentalAllergy(id, allergy, severity);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove environmental allergy
     * DELETE /api/medical-records/{id}/allergies/environmental
     */
    @DeleteMapping("/{id}/allergies/environmental")
    public ResponseEntity<MedicalRecord> removeEnvironmentalAllergy(
            @PathVariable Long id,
            @RequestParam String allergy) {
        try {
            MedicalRecord updated = medicalRecordService.removeEnvironmentalAllergy(id, allergy);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students with food allergies
     * GET /api/medical-records/allergies/food
     */
    @GetMapping("/allergies/food")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithFoodAllergies() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithFoodAllergies());
    }

    /**
     * Get students with medication allergies
     * GET /api/medical-records/allergies/medication
     */
    @GetMapping("/allergies/medication")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithMedicationAllergies() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithMedicationAllergies());
    }

    /**
     * Get students with severe allergies
     * GET /api/medical-records/allergies/severe
     */
    @GetMapping("/allergies/severe")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithSevereAllergies() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithSevereAllergies());
    }

    /**
     * Get students with specific allergy
     * GET /api/medical-records/allergies/search
     */
    @GetMapping("/allergies/search")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithSpecificAllergy(@RequestParam String allergen) {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithSpecificAllergy(allergen));
    }

    // ========================================================================
    // CHRONIC CONDITIONS
    // ========================================================================

    /**
     * Add chronic condition
     * POST /api/medical-records/{id}/conditions
     */
    @PostMapping("/{id}/conditions")
    public ResponseEntity<MedicalRecord> addChronicCondition(
            @PathVariable Long id,
            @RequestParam String condition) {
        try {
            MedicalRecord updated = medicalRecordService.addChronicCondition(id, condition);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove chronic condition
     * DELETE /api/medical-records/{id}/conditions
     */
    @DeleteMapping("/{id}/conditions")
    public ResponseEntity<MedicalRecord> removeChronicCondition(
            @PathVariable Long id,
            @RequestParam String condition) {
        try {
            MedicalRecord updated = medicalRecordService.removeChronicCondition(id, condition);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students with diabetes
     * GET /api/medical-records/conditions/diabetes
     */
    @GetMapping("/conditions/diabetes")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithDiabetes() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithDiabetes());
    }

    /**
     * Get students with asthma
     * GET /api/medical-records/conditions/asthma
     */
    @GetMapping("/conditions/asthma")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithAsthma() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithAsthma());
    }

    /**
     * Get students with seizure disorders
     * GET /api/medical-records/conditions/seizures
     */
    @GetMapping("/conditions/seizures")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithSeizureDisorders() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithSeizureDisorders());
    }

    /**
     * Get students with heart conditions
     * GET /api/medical-records/conditions/heart
     */
    @GetMapping("/conditions/heart")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithHeartConditions() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithHeartConditions());
    }

    // ========================================================================
    // MEDICATIONS
    // ========================================================================

    /**
     * Add medication
     * POST /api/medical-records/{id}/medications
     */
    @PostMapping("/{id}/medications")
    public ResponseEntity<MedicalRecord> addMedication(
            @PathVariable Long id,
            @RequestParam String medication) {
        try {
            MedicalRecord updated = medicalRecordService.addMedication(id, medication);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove medication
     * DELETE /api/medical-records/{id}/medications
     */
    @DeleteMapping("/{id}/medications")
    public ResponseEntity<MedicalRecord> removeMedication(
            @PathVariable Long id,
            @RequestParam String medication) {
        try {
            MedicalRecord updated = medicalRecordService.removeMedication(id, medication);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students on medications
     * GET /api/medical-records/medications/students
     */
    @GetMapping("/medications/students")
    public ResponseEntity<List<MedicalRecord>> getStudentsOnMedications() {
        return ResponseEntity.ok(medicalRecordService.getStudentsOnMedications());
    }

    /**
     * Search students by medication
     * GET /api/medical-records/medications/search
     */
    @GetMapping("/medications/search")
    public ResponseEntity<List<MedicalRecord>> getStudentsByMedication(@RequestParam String medication) {
        return ResponseEntity.ok(medicalRecordService.getStudentsByMedication(medication));
    }

    // ========================================================================
    // MEDICAL ALERTS
    // ========================================================================

    /**
     * Add medical alert
     * POST /api/medical-records/{id}/alerts
     */
    @PostMapping("/{id}/alerts")
    public ResponseEntity<MedicalRecord> addMedicalAlert(
            @PathVariable Long id,
            @RequestParam String alert) {
        try {
            MedicalRecord updated = medicalRecordService.addMedicalAlert(id, alert);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove medical alert
     * DELETE /api/medical-records/{id}/alerts
     */
    @DeleteMapping("/{id}/alerts")
    public ResponseEntity<MedicalRecord> removeMedicalAlert(
            @PathVariable Long id,
            @RequestParam String alert) {
        try {
            MedicalRecord updated = medicalRecordService.removeMedicalAlert(id, alert);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students with medical alerts
     * GET /api/medical-records/alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithMedicalAlerts() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithMedicalAlerts());
    }

    // ========================================================================
    // PHYSICIAN INFORMATION
    // ========================================================================

    /**
     * Update physician information
     * PUT /api/medical-records/{id}/physician
     */
    @PutMapping("/{id}/physician")
    public ResponseEntity<MedicalRecord> updatePhysicianInfo(
            @PathVariable Long id,
            @RequestParam String physicianName,
            @RequestParam(required = false) String physicianPhone,
            @RequestParam(required = false) String physicianAddress) {
        try {
            MedicalRecord updated = medicalRecordService.updatePhysicianInfo(
                    id, physicianName, physicianPhone, physicianAddress);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // INSURANCE INFORMATION
    // ========================================================================

    /**
     * Update insurance information
     * PUT /api/medical-records/{id}/insurance
     */
    @PutMapping("/{id}/insurance")
    public ResponseEntity<MedicalRecord> updateInsuranceInfo(
            @PathVariable Long id,
            @RequestParam String insuranceProvider,
            @RequestParam String insurancePolicyNumber,
            @RequestParam(required = false) String insuranceGroupNumber) {
        try {
            MedicalRecord updated = medicalRecordService.updateInsuranceInfo(
                    id, insuranceProvider, insurancePolicyNumber, insuranceGroupNumber);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students without insurance
     * GET /api/medical-records/insurance/none
     */
    @GetMapping("/insurance/none")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithoutInsurance() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithoutInsurance());
    }

    // ========================================================================
    // IMMUNIZATIONS
    // ========================================================================

    /**
     * Get students with incomplete immunizations
     * GET /api/medical-records/immunizations/incomplete
     */
    @GetMapping("/immunizations/incomplete")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithIncompleteImmunizations() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithIncompleteImmunizations());
    }

    /**
     * Update immunization compliance
     * POST /api/medical-records/{id}/immunizations/compliance
     */
    @PostMapping("/{id}/immunizations/compliance")
    public ResponseEntity<MedicalRecord> updateImmunizationCompliance(
            @PathVariable Long id,
            @RequestParam Boolean compliant) {
        try {
            MedicalRecord updated = medicalRecordService.updateImmunizationCompliance(id, compliant);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // PHYSICAL EXAMINATIONS
    // ========================================================================

    /**
     * Update physical exam information
     * PUT /api/medical-records/{id}/physical-exam
     */
    @PutMapping("/{id}/physical-exam")
    public ResponseEntity<MedicalRecord> updatePhysicalExamInfo(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {
        try {
            MedicalRecord updated = medicalRecordService.updatePhysicalExamInfo(id, examDate, expirationDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students with expired physical exams
     * GET /api/medical-records/physical-exam/expired
     */
    @GetMapping("/physical-exam/expired")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithExpiredPhysicals() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithExpiredPhysicals());
    }

    /**
     * Get students with physical exams expiring soon
     * GET /api/medical-records/physical-exam/expiring-soon
     */
    @GetMapping("/physical-exam/expiring-soon")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithPhysicalsExpiringSoon(@RequestParam int days) {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithPhysicalsExpiringSoon(days));
    }

    // ========================================================================
    // ATHLETIC CLEARANCE
    // ========================================================================

    /**
     * Update athletic clearance
     * POST /api/medical-records/{id}/athletic-clearance
     */
    @PostMapping("/{id}/athletic-clearance")
    public ResponseEntity<MedicalRecord> updateAthleticClearance(
            @PathVariable Long id,
            @RequestParam Boolean cleared,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate clearanceDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expirationDate) {
        try {
            MedicalRecord updated = medicalRecordService.updateAthleticClearance(
                    id, cleared, clearanceDate, expirationDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students cleared for athletics
     * GET /api/medical-records/athletic-clearance/cleared
     */
    @GetMapping("/athletic-clearance/cleared")
    public ResponseEntity<List<MedicalRecord>> getStudentsClearedForAthletics() {
        return ResponseEntity.ok(medicalRecordService.getStudentsClearedForAthletics());
    }

    /**
     * Get students not cleared for athletics
     * GET /api/medical-records/athletic-clearance/not-cleared
     */
    @GetMapping("/athletic-clearance/not-cleared")
    public ResponseEntity<List<MedicalRecord>> getStudentsNotClearedForAthletics() {
        return ResponseEntity.ok(medicalRecordService.getStudentsNotClearedForAthletics());
    }

    // ========================================================================
    // EMERGENCY AUTHORIZATION
    // ========================================================================

    /**
     * Update emergency medical authorization
     * POST /api/medical-records/{id}/emergency-authorization
     */
    @PostMapping("/{id}/emergency-authorization")
    public ResponseEntity<MedicalRecord> updateEmergencyAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate authorizationDate) {
        try {
            MedicalRecord updated = medicalRecordService.updateEmergencyAuthorization(
                    id, authorized, authorizationDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students without emergency medical authorization
     * GET /api/medical-records/emergency-authorization/missing
     */
    @GetMapping("/emergency-authorization/missing")
    public ResponseEntity<List<MedicalRecord>> getStudentsWithoutEmergencyAuthorization() {
        return ResponseEntity.ok(medicalRecordService.getStudentsWithoutEmergencyAuthorization());
    }

    // ========================================================================
    // CONCUSSION PROTOCOL
    // ========================================================================

    /**
     * Update concussion protocol status
     * POST /api/medical-records/{id}/concussion-protocol
     */
    @PostMapping("/{id}/concussion-protocol")
    public ResponseEntity<MedicalRecord> updateConcussionProtocol(
            @PathVariable Long id,
            @RequestParam Boolean inProtocol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate incidentDate) {
        try {
            MedicalRecord updated = medicalRecordService.updateConcussionProtocol(id, inProtocol, incidentDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get students in concussion protocol
     * GET /api/medical-records/concussion-protocol/active
     */
    @GetMapping("/concussion-protocol/active")
    public ResponseEntity<List<MedicalRecord>> getStudentsInConcussionProtocol() {
        return ResponseEntity.ok(medicalRecordService.getStudentsInConcussionProtocol());
    }

    // ========================================================================
    // REVIEW & VERIFICATION
    // ========================================================================

    /**
     * Verify medical record
     * POST /api/medical-records/{id}/verify
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<MedicalRecord> verifyMedicalRecord(@PathVariable Long id) {
        try {
            MedicalRecord verified = medicalRecordService.verifyMedicalRecord(id);
            return ResponseEntity.ok(verified);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get unverified medical records
     * GET /api/medical-records/unverified
     */
    @GetMapping("/unverified")
    public ResponseEntity<List<MedicalRecord>> getUnverifiedMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getUnverifiedMedicalRecords());
    }

    /**
     * Get medical records requiring review
     * GET /api/medical-records/review-needed
     */
    @GetMapping("/review-needed")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsNeedingReview(@RequestParam int days) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordsNeedingReview(days));
    }

    /**
     * Schedule review
     * POST /api/medical-records/{id}/schedule-review
     */
    @PostMapping("/{id}/schedule-review")
    public ResponseEntity<MedicalRecord> scheduleReview(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reviewDate) {
        try {
            MedicalRecord updated = medicalRecordService.scheduleReview(id, reviewDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Bulk verify medical records
     * POST /api/medical-records/bulk/verify
     */
    @PostMapping("/bulk/verify")
    public ResponseEntity<List<MedicalRecord>> bulkVerifyMedicalRecords(@RequestBody List<Long> recordIds) {
        try {
            List<MedicalRecord> verified = medicalRecordService.bulkVerifyMedicalRecords(recordIds);
            return ResponseEntity.ok(verified);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bulk schedule reviews
     * POST /api/medical-records/bulk/schedule-review
     */
    @PostMapping("/bulk/schedule-review")
    public ResponseEntity<List<MedicalRecord>> bulkScheduleReviews(
            @RequestBody List<Long> recordIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reviewDate) {
        try {
            List<MedicalRecord> updated = medicalRecordService.bulkScheduleReviews(recordIds, reviewDate);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Count students with allergies
     * GET /api/medical-records/statistics/allergies/count
     */
    @GetMapping("/statistics/allergies/count")
    public ResponseEntity<Long> countStudentsWithAllergies() {
        return ResponseEntity.ok(medicalRecordService.countStudentsWithAllergies());
    }

    /**
     * Count students with chronic conditions
     * GET /api/medical-records/statistics/conditions/count
     */
    @GetMapping("/statistics/conditions/count")
    public ResponseEntity<Long> countStudentsWithChronicConditions() {
        return ResponseEntity.ok(medicalRecordService.countStudentsWithChronicConditions());
    }

    /**
     * Count students on medications
     * GET /api/medical-records/statistics/medications/count
     */
    @GetMapping("/statistics/medications/count")
    public ResponseEntity<Long> countStudentsOnMedications() {
        return ResponseEntity.ok(medicalRecordService.countStudentsOnMedications());
    }
}
