package com.heronix.service;

import com.heronix.model.domain.MedicalRecord;
import com.heronix.model.domain.MedicalRecord.AllergySeverity;
import com.heronix.model.domain.Student;
import com.heronix.repository.MedicalRecordRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for MedicalRecord management
 *
 * Handles all business logic for medical information including:
 * - Allergies management (food, medication, environmental)
 * - Chronic conditions tracking (diabetes, asthma, seizures, heart)
 * - Medications management
 * - Medical alerts
 * - Emergency action plans
 * - Physician and insurance information
 * - Review scheduling
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create or get medical record for student
     */
    public MedicalRecord getOrCreateMedicalRecord(Long studentId) {
        log.info("Getting or creating medical record for student ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Optional<MedicalRecord> existing = medicalRecordRepository.findByStudentId(studentId);
        if (existing.isPresent()) {
            return existing.get();
        }

        // Create new medical record
        MedicalRecord record = new MedicalRecord();
        record.setStudent(student);
        record.setAllergySeverity(AllergySeverity.NONE);
        record.setHasDiabetes(false);
        record.setHasAsthma(false);
        record.setHasSeizureDisorder(false);
        record.setHasHeartCondition(false);
        record.setHasEpiPen(false);
        record.setHasInhaler(false);
        record.setSelfAdministers(false);
        record.setMedicationInNursesOffice(true);
        record.setNurseVerified(false);
        record.setParentSignatureOnFile(false);
        record.setLastReviewDate(LocalDate.now());

        return medicalRecordRepository.save(record);
    }

    /**
     * Get all medical records
     */
    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    /**
     * Get medical record by ID
     */
    public Optional<MedicalRecord> getMedicalRecordById(Long id) {
        return medicalRecordRepository.findById(id);
    }

    /**
     * Get medical record for student
     */
    public Optional<MedicalRecord> getMedicalRecordByStudent(Long studentId) {
        return medicalRecordRepository.findByStudentId(studentId);
    }

    /**
     * Update medical record
     */
    public MedicalRecord updateMedicalRecord(Long id, MedicalRecord updatedRecord, String updatedBy) {
        log.info("Updating medical record ID: {} by {}", id, updatedBy);

        MedicalRecord existing = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + id));

        // Update allergies
        existing.setFoodAllergies(updatedRecord.getFoodAllergies());
        existing.setMedicationAllergies(updatedRecord.getMedicationAllergies());
        existing.setEnvironmentalAllergies(updatedRecord.getEnvironmentalAllergies());
        existing.setAllergySeverity(updatedRecord.getAllergySeverity());
        existing.setHasEpiPen(updatedRecord.getHasEpiPen());
        existing.setEpiPenLocation(updatedRecord.getEpiPenLocation());

        // Update chronic conditions
        existing.setHasDiabetes(updatedRecord.getHasDiabetes());
        existing.setDiabetesType(updatedRecord.getDiabetesType());
        existing.setDiabetesManagement(updatedRecord.getDiabetesManagement());
        existing.setHasAsthma(updatedRecord.getHasAsthma());
        existing.setAsthmaSeverity(updatedRecord.getAsthmaSeverity());
        existing.setAsthmaTriggers(updatedRecord.getAsthmaTriggers());
        existing.setHasInhaler(updatedRecord.getHasInhaler());
        existing.setInhalerLocation(updatedRecord.getInhalerLocation());
        existing.setHasSeizureDisorder(updatedRecord.getHasSeizureDisorder());
        existing.setSeizureDetails(updatedRecord.getSeizureDetails());
        existing.setHasHeartCondition(updatedRecord.getHasHeartCondition());
        existing.setHeartConditionDetails(updatedRecord.getHeartConditionDetails());
        existing.setOtherConditions(updatedRecord.getOtherConditions());

        // Update medications
        existing.setCurrentMedications(updatedRecord.getCurrentMedications());
        existing.setMedicationSchedule(updatedRecord.getMedicationSchedule());
        existing.setSelfAdministers(updatedRecord.getSelfAdministers());
        existing.setMedicationInNursesOffice(updatedRecord.getMedicationInNursesOffice());

        // Update emergency information
        existing.setEmergencyActionPlan(updatedRecord.getEmergencyActionPlan());
        existing.setMedicalAlert(updatedRecord.getMedicalAlert());
        existing.setPhysicalRestrictions(updatedRecord.getPhysicalRestrictions());
        existing.setDietaryRestrictions(updatedRecord.getDietaryRestrictions());

        // Update contacts & insurance
        existing.setPrimaryPhysicianName(updatedRecord.getPrimaryPhysicianName());
        existing.setPrimaryPhysicianPhone(updatedRecord.getPrimaryPhysicianPhone());
        existing.setSpecialistName(updatedRecord.getSpecialistName());
        existing.setSpecialistPhone(updatedRecord.getSpecialistPhone());
        existing.setInsuranceProvider(updatedRecord.getInsuranceProvider());
        existing.setInsurancePolicyNumber(updatedRecord.getInsurancePolicyNumber());

        // Update tracking
        existing.setLastUpdatedBy(updatedBy);
        existing.setLastUpdatedDate(LocalDate.now());
        existing.setAdditionalNotes(updatedRecord.getAdditionalNotes());

        return medicalRecordRepository.save(existing);
    }

    /**
     * Update medical record (simple overload)
     */
    public MedicalRecord updateMedicalRecord(MedicalRecord record) {
        log.info("Updating medical record ID: {}", record.getId());
        return medicalRecordRepository.save(record);
    }

    /**
     * Delete medical record
     */
    public void deleteMedicalRecord(Long id) {
        log.info("Deleting medical record ID: {}", id);
        medicalRecordRepository.deleteById(id);
    }

    /**
     * Get incomplete medical records (missing critical information)
     */
    public List<MedicalRecord> getIncompleteMedicalRecords() {
        // Consider a record incomplete if it lacks basic information
        return medicalRecordRepository.findAll().stream()
            .filter(record -> {
                // Missing allergies info and chronic conditions info
                boolean missingAllergyInfo = record.getFoodAllergies() == null &&
                                            record.getMedicationAllergies() == null &&
                                            record.getEnvironmentalAllergies() == null;

                boolean missingConditionInfo = !Boolean.TRUE.equals(record.getHasDiabetes()) &&
                                              !Boolean.TRUE.equals(record.getHasAsthma()) &&
                                              !Boolean.TRUE.equals(record.getHasSeizureDisorder());

                return missingAllergyInfo && missingConditionInfo;
            })
            .toList();
    }

    // ========================================================================
    // ALLERGY MANAGEMENT
    // ========================================================================

    /**
     * Add food allergy
     */
    public MedicalRecord addFoodAllergy(Long recordId, String allergy, AllergySeverity severity) {
        log.info("Adding food allergy to record {}: {}", recordId, allergy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getFoodAllergies();
        if (current == null || current.trim().isEmpty()) {
            record.setFoodAllergies(allergy);
        } else if (!current.contains(allergy)) {
            record.setFoodAllergies(current + ", " + allergy);
        }

        // Update severity if more severe
        if (record.getAllergySeverity() == null ||
            severity.ordinal() > record.getAllergySeverity().ordinal()) {
            record.setAllergySeverity(severity);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Add medication allergy
     */
    public MedicalRecord addMedicationAllergy(Long recordId, String allergy, AllergySeverity severity) {
        log.info("Adding medication allergy to record {}: {}", recordId, allergy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getMedicationAllergies();
        if (current == null || current.trim().isEmpty()) {
            record.setMedicationAllergies(allergy);
        } else if (!current.contains(allergy)) {
            record.setMedicationAllergies(current + ", " + allergy);
        }

        // Update severity if more severe
        if (record.getAllergySeverity() == null ||
            severity.ordinal() > record.getAllergySeverity().ordinal()) {
            record.setAllergySeverity(severity);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Add environmental allergy
     */
    public MedicalRecord addEnvironmentalAllergy(Long recordId, String allergy, AllergySeverity severity) {
        log.info("Adding environmental allergy to record {}: {}", recordId, allergy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getEnvironmentalAllergies();
        if (current == null || current.trim().isEmpty()) {
            record.setEnvironmentalAllergies(allergy);
        } else if (!current.contains(allergy)) {
            record.setEnvironmentalAllergies(current + ", " + allergy);
        }

        // Update severity if more severe
        if (record.getAllergySeverity() == null ||
            severity.ordinal() > record.getAllergySeverity().ordinal()) {
            record.setAllergySeverity(severity);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Remove food allergy
     */
    public MedicalRecord removeFoodAllergy(Long recordId, String allergy) {
        log.info("Removing food allergy from record {}: {}", recordId, allergy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getFoodAllergies();
        if (current != null && !current.trim().isEmpty()) {
            String updated = current.replaceAll("(?i)\\b" + allergy + "\\b,?\\s*", "")
                    .replaceAll(",\\s*,", ",")
                    .replaceAll("^,\\s*|\\s*,$", "")
                    .trim();
            record.setFoodAllergies(updated.isEmpty() ? null : updated);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Remove medication allergy
     */
    public MedicalRecord removeMedicationAllergy(Long recordId, String allergy) {
        log.info("Removing medication allergy from record {}: {}", recordId, allergy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getMedicationAllergies();
        if (current != null && !current.trim().isEmpty()) {
            String updated = current.replaceAll("(?i)\\b" + allergy + "\\b,?\\s*", "")
                    .replaceAll(",\\s*,", ",")
                    .replaceAll("^,\\s*|\\s*,$", "")
                    .trim();
            record.setMedicationAllergies(updated.isEmpty() ? null : updated);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Remove environmental allergy
     */
    public MedicalRecord removeEnvironmentalAllergy(Long recordId, String allergy) {
        log.info("Removing environmental allergy from record {}: {}", recordId, allergy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getEnvironmentalAllergies();
        if (current != null && !current.trim().isEmpty()) {
            String updated = current.replaceAll("(?i)\\b" + allergy + "\\b,?\\s*", "")
                    .replaceAll(",\\s*,", ",")
                    .replaceAll("^,\\s*|\\s*,$", "")
                    .trim();
            record.setEnvironmentalAllergies(updated.isEmpty() ? null : updated);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students with food allergies
     */
    public List<MedicalRecord> getStudentsWithFoodAllergies() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getFoodAllergies() != null && !r.getFoodAllergies().trim().isEmpty())
                .toList();
    }

    /**
     * Get students with medication allergies
     */
    public List<MedicalRecord> getStudentsWithMedicationAllergies() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getMedicationAllergies() != null && !r.getMedicationAllergies().trim().isEmpty())
                .toList();
    }

    /**
     * Get students with severe allergies
     */
    public List<MedicalRecord> getStudentsWithSevereAllergies() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getAllergySeverity() == AllergySeverity.SEVERE ||
                            r.getAllergySeverity() == AllergySeverity.LIFE_THREATENING)
                .toList();
    }

    /**
     * Get students with specific allergy
     */
    public List<MedicalRecord> getStudentsWithSpecificAllergy(String allergen) {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> {
                    String food = r.getFoodAllergies();
                    String medication = r.getMedicationAllergies();
                    String environmental = r.getEnvironmentalAllergies();

                    return (food != null && food.toLowerCase().contains(allergen.toLowerCase())) ||
                           (medication != null && medication.toLowerCase().contains(allergen.toLowerCase())) ||
                           (environmental != null && environmental.toLowerCase().contains(allergen.toLowerCase()));
                })
                .toList();
    }

    /**
     * Set allergy severity
     */
    public MedicalRecord setAllergySeverity(Long recordId, AllergySeverity severity) {
        log.info("Setting allergy severity for record {} to {}", recordId, severity);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setAllergySeverity(severity);
        return medicalRecordRepository.save(record);
    }

    // ========================================================================
    // MEDICAL ALERT MANAGEMENT
    // ========================================================================

    /**
     * Set medical alert
     */
    public MedicalRecord setMedicalAlert(Long recordId, String alert) {
        log.info("Setting medical alert for record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setMedicalAlert(alert);
        return medicalRecordRepository.save(record);
    }

    /**
     * Clear medical alert
     */
    public MedicalRecord clearMedicalAlert(Long recordId) {
        log.info("Clearing medical alert for record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setMedicalAlert(null);
        return medicalRecordRepository.save(record);
    }

    /**
     * Get all critical medical cases
     */
    public List<MedicalRecord> getCriticalCases() {
        return medicalRecordRepository.findAll().stream()
                .filter(MedicalRecord::isCriticalCase)
                .toList();
    }

    /**
     * Get records with medical alerts
     */
    public List<MedicalRecord> getRecordsWithAlerts() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getMedicalAlert() != null && !r.getMedicalAlert().trim().isEmpty())
                .toList();
    }

    /**
     * Add medical alert
     */
    public MedicalRecord addMedicalAlert(Long recordId, String alert) {
        log.info("Adding medical alert to record {}: {}", recordId, alert);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getMedicalAlert();
        if (current == null || current.trim().isEmpty()) {
            record.setMedicalAlert(alert);
        } else if (!current.contains(alert)) {
            record.setMedicalAlert(current + "; " + alert);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Remove medical alert
     */
    public MedicalRecord removeMedicalAlert(Long recordId, String alert) {
        log.info("Removing medical alert from record {}: {}", recordId, alert);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getMedicalAlert();
        if (current != null && !current.trim().isEmpty()) {
            String updated = current.replaceAll("(?i)" + alert + ";?\\s*", "")
                    .replaceAll(";\\s*;", ";")
                    .replaceAll("^;\\s*|\\s*;$", "")
                    .trim();
            record.setMedicalAlert(updated.isEmpty() ? null : updated);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students with medical alerts
     */
    public List<MedicalRecord> getStudentsWithMedicalAlerts() {
        return getRecordsWithAlerts();
    }

    // ========================================================================
    // CHRONIC CONDITION MANAGEMENT
    // ========================================================================

    /**
     * Set diabetes status
     */
    public MedicalRecord setDiabetesStatus(Long recordId, boolean hasDiabetes, String diabetesType, String management) {
        log.info("Setting diabetes status for record {}: {}", recordId, hasDiabetes);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setHasDiabetes(hasDiabetes);
        record.setDiabetesType(diabetesType);
        record.setDiabetesManagement(management);

        return medicalRecordRepository.save(record);
    }

    /**
     * Set asthma status
     */
    public MedicalRecord setAsthmaStatus(Long recordId, boolean hasAsthma, String severity, String triggers) {
        log.info("Setting asthma status for record {}: {}", recordId, hasAsthma);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setHasAsthma(hasAsthma);
        record.setAsthmaSeverity(severity);
        record.setAsthmaTriggers(triggers);

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students with diabetes
     */
    public List<MedicalRecord> getStudentsWithDiabetes() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasDiabetes()))
                .toList();
    }

    /**
     * Get students with asthma
     */
    public List<MedicalRecord> getStudentsWithAsthma() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasAsthma()))
                .toList();
    }

    /**
     * Get students with seizure disorders
     */
    public List<MedicalRecord> getStudentsWithSeizureDisorders() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasSeizureDisorder()))
                .toList();
    }

    /**
     * Get students with heart conditions
     */
    public List<MedicalRecord> getStudentsWithHeartConditions() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasHeartCondition()))
                .toList();
    }

    /**
     * Add chronic condition
     */
    public MedicalRecord addChronicCondition(Long recordId, String condition) {
        log.info("Adding chronic condition to record {}: {}", recordId, condition);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getOtherConditions();
        if (current == null || current.trim().isEmpty()) {
            record.setOtherConditions(condition);
        } else if (!current.contains(condition)) {
            record.setOtherConditions(current + ", " + condition);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Remove chronic condition
     */
    public MedicalRecord removeChronicCondition(Long recordId, String condition) {
        log.info("Removing chronic condition from record {}: {}", recordId, condition);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getOtherConditions();
        if (current != null && !current.trim().isEmpty()) {
            String updated = current.replaceAll("(?i)\\b" + condition + "\\b,?\\s*", "")
                    .replaceAll(",\\s*,", ",")
                    .replaceAll("^,\\s*|\\s*,$", "")
                    .trim();
            record.setOtherConditions(updated.isEmpty() ? null : updated);
        }

        return medicalRecordRepository.save(record);
    }

    // ========================================================================
    // MEDICATION MANAGEMENT
    // ========================================================================

    /**
     * Add medication
     */
    public MedicalRecord addMedication(Long recordId, String medication) {
        log.info("Adding medication to record {}: {}", recordId, medication);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getCurrentMedications();
        if (current == null || current.trim().isEmpty()) {
            record.setCurrentMedications(medication);
        } else {
            record.setCurrentMedications(current + "\n" + medication);
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Set medication schedule
     */
    public MedicalRecord setMedicationSchedule(Long recordId, String schedule) {
        log.info("Setting medication schedule for record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setMedicationSchedule(schedule);
        return medicalRecordRepository.save(record);
    }

    /**
     * Get students with medications
     */
    public List<MedicalRecord> getStudentsWithMedications() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getCurrentMedications() != null && !r.getCurrentMedications().trim().isEmpty())
                .toList();
    }

    /**
     * Get students with medications in nurse's office
     */
    public List<MedicalRecord> getStudentsWithMedicationsInNursesOffice() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getMedicationInNursesOffice()) &&
                            r.getCurrentMedications() != null && !r.getCurrentMedications().trim().isEmpty())
                .toList();
    }

    /**
     * Remove medication
     */
    public MedicalRecord removeMedication(Long recordId, String medication) {
        log.info("Removing medication from record {}: {}", recordId, medication);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        String current = record.getCurrentMedications();
        if (current != null && !current.trim().isEmpty()) {
            String[] lines = current.split("\n");
            StringBuilder updated = new StringBuilder();
            for (String line : lines) {
                if (!line.toLowerCase().contains(medication.toLowerCase())) {
                    if (updated.length() > 0) {
                        updated.append("\n");
                    }
                    updated.append(line);
                }
            }
            record.setCurrentMedications(updated.length() == 0 ? null : updated.toString());
        }

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students on medications
     */
    public List<MedicalRecord> getStudentsOnMedications() {
        return getStudentsWithMedications();
    }

    /**
     * Get students by medication
     */
    public List<MedicalRecord> getStudentsByMedication(String medication) {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getCurrentMedications() != null &&
                            r.getCurrentMedications().toLowerCase().contains(medication.toLowerCase()))
                .toList();
    }

    // ========================================================================
    // REVIEW MANAGEMENT
    // ========================================================================

    /**
     * Schedule next review
     */
    public MedicalRecord scheduleReview(Long recordId, LocalDate reviewDate) {
        log.info("Scheduling medical review for record {} on {}", recordId, reviewDate);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setNextReviewDate(reviewDate);
        return medicalRecordRepository.save(record);
    }

    /**
     * Complete review
     */
    public MedicalRecord completeReview(Long recordId, String reviewedBy) {
        log.info("Completing medical review for record {} by {}", recordId, reviewedBy);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setLastReviewDate(LocalDate.now());
        record.setLastUpdatedBy(reviewedBy);
        record.setLastUpdatedDate(LocalDate.now());

        // Schedule next annual review
        record.setNextReviewDate(LocalDate.now().plusYears(1));

        return medicalRecordRepository.save(record);
    }

    /**
     * Get records with overdue reviews
     */
    public List<MedicalRecord> getOverdueReviews() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(MedicalRecord::isReviewOverdue)
                .toList();
    }

    // ========================================================================
    // VERIFICATION
    // ========================================================================

    /**
     * Mark as nurse verified
     */
    public MedicalRecord markAsNurseVerified(Long recordId, String nurseName) {
        log.info("Marking record {} as nurse verified by {}", recordId, nurseName);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setNurseVerified(true);
        record.setLastUpdatedBy(nurseName);
        record.setLastUpdatedDate(LocalDate.now());

        return medicalRecordRepository.save(record);
    }

    /**
     * Mark as parent signature on file
     */
    public MedicalRecord markAsParentSignatureOnFile(Long recordId, boolean onFile) {
        log.info("Marking record {} parent signature on file: {}", recordId, onFile);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setParentSignatureOnFile(onFile);
        record.setLastUpdatedDate(LocalDate.now());

        return medicalRecordRepository.save(record);
    }

    /**
     * Get unverified records
     */
    public List<MedicalRecord> getUnverifiedRecords() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getNurseVerified()))
                .toList();
    }

    // ========================================================================
    // STATISTICS
    // ========================================================================

    /**
     * Count students with medical conditions
     */
    public long countStudentsWithMedicalConditions() {
        return medicalRecordRepository.findAll().stream()
                .filter(MedicalRecord::hasMedicalConditions)
                .count();
    }

    /**
     * Count critical medical cases
     */
    public long countCriticalCases() {
        return medicalRecordRepository.findAll().stream()
                .filter(MedicalRecord::isCriticalCase)
                .count();
    }

    /**
     * Get students with EpiPens
     */
    public List<MedicalRecord> getStudentsWithEpiPens() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasEpiPen()))
                .toList();
    }

    /**
     * Get students with inhalers
     */
    public List<MedicalRecord> getStudentsWithInhalers() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasInhaler()))
                .toList();
    }

    // ========================================================================
    // PHYSICIAN & INSURANCE INFORMATION
    // ========================================================================

    /**
     * Update physician information
     */
    public MedicalRecord updatePhysicianInfo(Long recordId, String physicianName, String physicianPhone, String physicianAddress) {
        log.info("Updating physician info for record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setPrimaryPhysicianName(physicianName);
        record.setPrimaryPhysicianPhone(physicianPhone);
        // Note: physicianAddress is not currently stored in MedicalRecord entity
        // This would require adding a new field to the entity

        return medicalRecordRepository.save(record);
    }

    /**
     * Update insurance information
     */
    public MedicalRecord updateInsuranceInfo(Long recordId, String insuranceProvider, String insurancePolicyNumber, String insuranceGroupNumber) {
        log.info("Updating insurance info for record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setInsuranceProvider(insuranceProvider);
        record.setInsurancePolicyNumber(insurancePolicyNumber);
        // Note: insuranceGroupNumber is not currently stored in MedicalRecord entity
        // This would require adding a new field to the entity

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students without insurance
     */
    public List<MedicalRecord> getStudentsWithoutInsurance() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> r.getInsuranceProvider() == null || r.getInsuranceProvider().trim().isEmpty())
                .toList();
    }

    // ========================================================================
    // IMMUNIZATION & PHYSICAL EXAM MANAGEMENT
    // ========================================================================

    /**
     * Get students with incomplete immunizations
     */
    public List<MedicalRecord> getStudentsWithIncompleteImmunizations() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getImmunizationsComplete()))
                .toList();
    }

    /**
     * Update immunization compliance
     */
    public MedicalRecord updateImmunizationCompliance(Long recordId, Boolean compliant) {
        log.info("Updating immunization compliance for record {}: {}", recordId, compliant);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setImmunizationsComplete(compliant);
        return medicalRecordRepository.save(record);
    }

    /**
     * Update physical exam information
     */
    public MedicalRecord updatePhysicalExamInfo(Long recordId, LocalDate examDate, LocalDate expirationDate) {
        log.info("Updating physical exam info for record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setLastPhysicalDate(examDate);
        record.setPhysicalRequiredDate(expirationDate);

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students with expired physical exams
     */
    public List<MedicalRecord> getStudentsWithExpiredPhysicals() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        LocalDate today = LocalDate.now();
        return all.stream()
                .filter(r -> r.getPhysicalRequiredDate() != null &&
                            r.getPhysicalRequiredDate().isBefore(today))
                .toList();
    }

    /**
     * Get students with physical exams expiring soon
     */
    public List<MedicalRecord> getStudentsWithPhysicalsExpiringSoon(int days) {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return all.stream()
                .filter(r -> r.getPhysicalRequiredDate() != null &&
                            r.getPhysicalRequiredDate().isAfter(LocalDate.now()) &&
                            r.getPhysicalRequiredDate().isBefore(cutoffDate))
                .toList();
    }

    // ========================================================================
    // ATHLETIC CLEARANCE
    // ========================================================================

    /**
     * Update athletic clearance
     */
    public MedicalRecord updateAthleticClearance(Long recordId, Boolean cleared, LocalDate clearanceDate, LocalDate expirationDate) {
        log.info("Updating athletic clearance for record {}: {}", recordId, cleared);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setAthleticClearance(cleared);
        record.setAthleticClearanceDate(clearanceDate);
        // Note: expirationDate parameter is not used as the entity doesn't have this field

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students cleared for athletics
     */
    public List<MedicalRecord> getStudentsClearedForAthletics() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getAthleticClearance()))
                .toList();
    }

    /**
     * Get students not cleared for athletics
     */
    public List<MedicalRecord> getStudentsNotClearedForAthletics() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getAthleticClearance()))
                .toList();
    }

    // ========================================================================
    // EMERGENCY AUTHORIZATION
    // ========================================================================

    /**
     * Update emergency authorization
     */
    public MedicalRecord updateEmergencyAuthorization(Long recordId, Boolean authorized, LocalDate authorizationDate) {
        log.info("Updating emergency authorization for record {}: {}", recordId, authorized);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setEmergencyTreatmentAuthorized(authorized);
        record.setEmergencyAuthorizationDate(authorizationDate);

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students without emergency authorization
     */
    public List<MedicalRecord> getStudentsWithoutEmergencyAuthorization() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getEmergencyTreatmentAuthorized()))
                .toList();
    }

    // ========================================================================
    // CONCUSSION PROTOCOL
    // ========================================================================

    /**
     * Update concussion protocol
     */
    public MedicalRecord updateConcussionProtocol(Long recordId, Boolean inProtocol, LocalDate incidentDate) {
        log.info("Updating concussion protocol for record {}: {}", recordId, inProtocol);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setConcussionProtocol(inProtocol);
        // Note: incidentDate parameter is not used as the entity doesn't have this field

        return medicalRecordRepository.save(record);
    }

    /**
     * Get students in concussion protocol
     */
    public List<MedicalRecord> getStudentsInConcussionProtocol() {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        return all.stream()
                .filter(r -> Boolean.TRUE.equals(r.getConcussionProtocol()))
                .toList();
    }

    // ========================================================================
    // VERIFICATION & REVIEW (ADDITIONAL METHODS)
    // ========================================================================

    /**
     * Verify medical record
     */
    public MedicalRecord verifyMedicalRecord(Long recordId) {
        log.info("Verifying medical record {}", recordId);

        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Medical record not found: " + recordId));

        record.setNurseVerified(true);
        record.setLastUpdatedDate(LocalDate.now());

        return medicalRecordRepository.save(record);
    }

    /**
     * Get unverified medical records
     */
    public List<MedicalRecord> getUnverifiedMedicalRecords() {
        return getUnverifiedRecords();
    }

    /**
     * Get medical records needing review
     */
    public List<MedicalRecord> getMedicalRecordsNeedingReview(int days) {
        List<MedicalRecord> all = medicalRecordRepository.findAll();
        LocalDate cutoffDate = LocalDate.now().minusDays(days);
        return all.stream()
                .filter(r -> r.getLastReviewDate() == null || r.getLastReviewDate().isBefore(cutoffDate))
                .toList();
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Bulk verify medical records
     */
    public List<MedicalRecord> bulkVerifyMedicalRecords(List<Long> recordIds) {
        log.info("Bulk verifying {} medical records", recordIds.size());

        return recordIds.stream()
                .map(this::verifyMedicalRecord)
                .toList();
    }

    /**
     * Bulk schedule reviews
     */
    public List<MedicalRecord> bulkScheduleReviews(List<Long> recordIds, LocalDate reviewDate) {
        log.info("Bulk scheduling reviews for {} medical records on {}", recordIds.size(), reviewDate);

        return recordIds.stream()
                .map(id -> scheduleReview(id, reviewDate))
                .toList();
    }

    // ========================================================================
    // STATISTICS (ADDITIONAL COUNTS)
    // ========================================================================

    /**
     * Count students with allergies
     */
    public long countStudentsWithAllergies() {
        return medicalRecordRepository.findAll().stream()
                .filter(r -> (r.getFoodAllergies() != null && !r.getFoodAllergies().trim().isEmpty()) ||
                            (r.getMedicationAllergies() != null && !r.getMedicationAllergies().trim().isEmpty()) ||
                            (r.getEnvironmentalAllergies() != null && !r.getEnvironmentalAllergies().trim().isEmpty()))
                .count();
    }

    /**
     * Count students with chronic conditions
     */
    public long countStudentsWithChronicConditions() {
        return medicalRecordRepository.findAll().stream()
                .filter(r -> Boolean.TRUE.equals(r.getHasDiabetes()) ||
                            Boolean.TRUE.equals(r.getHasAsthma()) ||
                            Boolean.TRUE.equals(r.getHasSeizureDisorder()) ||
                            Boolean.TRUE.equals(r.getHasHeartCondition()) ||
                            (r.getOtherConditions() != null && !r.getOtherConditions().trim().isEmpty()))
                .count();
    }

    /**
     * Count students on medications
     */
    public long countStudentsOnMedications() {
        return medicalRecordRepository.findAll().stream()
                .filter(r -> r.getCurrentMedications() != null && !r.getCurrentMedications().trim().isEmpty())
                .count();
    }
}
