package com.heronix.service;

import com.heronix.model.domain.Medication;
import com.heronix.model.domain.MedicationAdministration;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.Medication.MedicationType;
import com.heronix.model.domain.MedicationAdministration.AdministrationRoute;
import com.heronix.model.domain.MedicationAdministration.AdministrationReason;
import com.heronix.repository.MedicationRepository;
import com.heronix.repository.MedicationAdministrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing student medications and medication administration.
 *
 * Handles:
 * - Medication CRUD operations
 * - Medication administration logging
 * - Inventory management
 * - Expiration and reorder alerts
 * - Compliance verification
 * - Audit trail maintenance
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Service
public class MedicationService {

    private static final Logger log = LoggerFactory.getLogger(MedicationService.class);

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private MedicationAdministrationRepository medicationAdministrationRepository;

    // ========================================================================
    // MEDICATION CRUD OPERATIONS
    // ========================================================================

    /**
     * Creates a new medication record.
     *
     * @param student the student
     * @param medicationName medication name
     * @param dosage dosage amount
     * @param purpose what it treats
     * @param prescribingPhysician physician name
     * @param startDate start date
     * @param createdByStaffId staff creating the record
     * @return created medication
     */
    @Transactional
    public Medication createMedication(
            Student student,
            String medicationName,
            String dosage,
            String purpose,
            String prescribingPhysician,
            LocalDate startDate,
            Long createdByStaffId) {

        log.info("Creating medication record for student ID {}: {}",
                student.getId(), medicationName);

        Medication medication = Medication.builder()
                .student(student)
                .medicationName(medicationName)
                .dosage(dosage)
                .purpose(purpose)
                .prescribingPhysician(prescribingPhysician)
                .startDate(startDate)
                .active(true)
                .createdByStaffId(createdByStaffId)
                .createdDate(LocalDateTime.now())
                .build();

        medication = medicationRepository.save(medication);
        log.info("Created medication ID {} for student: {} {}",
                medication.getId(), student.getFirstName(), student.getLastName());

        return medication;
    }

    /**
     * Gets a medication by ID.
     *
     * @param id the medication ID
     * @return medication
     */
    public Medication getMedicationById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found: " + id));
    }

    /**
     * Gets all medications for a student.
     *
     * @param student the student
     * @return list of medications
     */
    public List<Medication> getMedicationsForStudent(Student student) {
        log.debug("Fetching medications for student ID {}", student.getId());
        return medicationRepository.findByStudent(student);
    }

    /**
     * Gets active medications for a student.
     *
     * @param student the student
     * @return list of active medications
     */
    public List<Medication> getActiveMedicationsForStudent(Student student) {
        log.debug("Fetching active medications for student ID {}", student.getId());
        return medicationRepository.findByStudentAndActive(student, true);
    }

    /**
     * Updates a medication record.
     *
     * @param medication the medication to update
     * @param updatedByStaffId staff updating the record
     * @return updated medication
     */
    @Transactional
    public Medication updateMedication(Medication medication, Long updatedByStaffId) {
        log.info("Updating medication ID {}", medication.getId());

        medication.setLastUpdated(LocalDateTime.now());
        medication.setUpdatedByStaffId(updatedByStaffId);

        medication = medicationRepository.save(medication);
        log.info("Updated medication ID {}", medication.getId());

        return medication;
    }

    /**
     * Discontinues a medication.
     *
     * @param medicationId the medication ID
     * @param reason reason for discontinuation
     * @param staffId staff discontinuing the medication
     * @return updated medication
     */
    @Transactional
    public Medication discontinueMedication(Long medicationId, String reason, Long staffId) {
        log.info("Discontinuing medication ID {}", medicationId);

        Medication medication = getMedicationById(medicationId);
        medication.setActive(false);
        medication.setDiscontinuationDate(LocalDate.now());
        medication.setDiscontinuationReason(reason);
        medication.setLastUpdated(LocalDateTime.now());
        medication.setUpdatedByStaffId(staffId);

        medication = medicationRepository.save(medication);
        log.info("Discontinued medication ID {}: {}", medicationId, reason);

        return medication;
    }

    // ========================================================================
    // MEDICATION ADMINISTRATION
    // ========================================================================

    /**
     * Records medication administration.
     *
     * @param medication the medication
     * @param student the student
     * @param doseGiven dose administered
     * @param route administration route
     * @param reason reason for administration
     * @param administeredByStaffId nurse staff ID
     * @param administratorName nurse name
     * @param administratorTitle nurse title
     * @return created medication administration record
     */
    @Transactional
    public MedicationAdministration administerMedication(
            Medication medication,
            Student student,
            String doseGiven,
            AdministrationRoute route,
            AdministrationReason reason,
            Long administeredByStaffId,
            String administratorName,
            String administratorTitle) {

        log.info("Administering medication ID {} to student ID {}",
                medication.getId(), student.getId());

        // Verify medication is active
        if (!medication.getActive()) {
            throw new IllegalStateException("Cannot administer inactive medication ID " + medication.getId());
        }

        // Check if already administered today for scheduled medications
        if (!medication.getAsNeeded()) {
            Boolean alreadyGiven = medicationAdministrationRepository
                    .wasMedicationAdministeredToday(medication, student);

            if (alreadyGiven) {
                log.warn("Medication ID {} already administered to student ID {} today",
                        medication.getId(), student.getId());
            }
        }

        MedicationAdministration administration = MedicationAdministration.builder()
                .medication(medication)
                .student(student)
                .administrationDate(LocalDate.now())
                .administrationTime(LocalTime.now())
                .administrationTimestamp(LocalDateTime.now())
                .doseGiven(doseGiven)
                .administrationRoute(route)
                .administrationReason(reason)
                .administeredByStaffId(administeredByStaffId)
                .administratorName(administratorName)
                .administratorTitle(administratorTitle)
                .build();

        administration = medicationAdministrationRepository.save(administration);

        // Update medication inventory if tracked
        if (medication.getQuantityOnHand() != null) {
            updateInventoryAfterAdministration(medication.getId(), 1);
        }

        log.info("Medication administered - Record ID {}", administration.getId());

        return administration;
    }

    /**
     * Records medication administration during a nurse visit.
     *
     * @param medication the medication
     * @param student the student
     * @param nurseVisit the nurse visit
     * @param doseGiven dose administered
     * @param route administration route
     * @param reason reason for administration
     * @param administeredByStaffId nurse staff ID
     * @param administratorName nurse name
     * @param administratorTitle nurse title
     * @return created medication administration record
     */
    @Transactional
    public MedicationAdministration administerMedicationDuringVisit(
            Medication medication,
            Student student,
            NurseVisit nurseVisit,
            String doseGiven,
            AdministrationRoute route,
            AdministrationReason reason,
            Long administeredByStaffId,
            String administratorName,
            String administratorTitle) {

        log.info("Administering medication ID {} during nurse visit ID {}",
                medication.getId(), nurseVisit.getId());

        MedicationAdministration administration = administerMedication(
                medication, student, doseGiven, route, reason,
                administeredByStaffId, administratorName, administratorTitle);

        administration.setNurseVisit(nurseVisit);
        administration = medicationAdministrationRepository.save(administration);

        return administration;
    }

    /**
     * Records student refusal of medication.
     *
     * @param medication the medication
     * @param student the student
     * @param refusalReason reason for refusal
     * @param staffId staff attempting administration
     * @param staffName staff name
     * @return created medication administration record
     */
    @Transactional
    public MedicationAdministration recordMedicationRefusal(
            Medication medication,
            Student student,
            String refusalReason,
            Long staffId,
            String staffName) {

        log.info("Recording medication refusal for medication ID {} by student ID {}",
                medication.getId(), student.getId());

        MedicationAdministration administration = MedicationAdministration.builder()
                .medication(medication)
                .student(student)
                .administrationDate(LocalDate.now())
                .administrationTime(LocalTime.now())
                .administrationTimestamp(LocalDateTime.now())
                .doseGiven("NOT ADMINISTERED")
                .administrationRoute(AdministrationRoute.ORAL)
                .administrationReason(AdministrationReason.SCHEDULED)
                .administeredByStaffId(staffId)
                .administratorName(staffName)
                .administratorTitle("School Nurse")
                .refusedByStudent(true)
                .refusalReason(refusalReason)
                .parentNotificationRequired(true)
                .build();

        administration = medicationAdministrationRepository.save(administration);
        log.info("Medication refusal recorded - Record ID {}", administration.getId());

        return administration;
    }

    /**
     * Gets administration history for a medication.
     *
     * @param medication the medication
     * @return list of administration records
     */
    public List<MedicationAdministration> getAdministrationHistory(Medication medication) {
        log.debug("Fetching administration history for medication ID {}", medication.getId());
        return medicationAdministrationRepository.findByMedication(medication);
    }

    /**
     * Gets administration history for a student.
     *
     * @param student the student
     * @return list of administration records
     */
    public List<MedicationAdministration> getAdministrationHistoryForStudent(Student student) {
        log.debug("Fetching administration history for student ID {}", student.getId());
        return medicationAdministrationRepository.findByStudent(student);
    }

    // ========================================================================
    // INVENTORY MANAGEMENT
    // ========================================================================

    /**
     * Updates medication inventory after administration.
     *
     * @param medicationId the medication ID
     * @param quantityUsed quantity consumed
     * @return updated medication
     */
    @Transactional
    public Medication updateInventoryAfterAdministration(Long medicationId, Integer quantityUsed) {
        log.info("Updating inventory for medication ID {}: -{} units", medicationId, quantityUsed);

        Medication medication = getMedicationById(medicationId);

        if (medication.getQuantityOnHand() != null) {
            int newQuantity = medication.getQuantityOnHand() - quantityUsed;
            medication.setQuantityOnHand(Math.max(0, newQuantity));
            medication = medicationRepository.save(medication);

            if (medication.isLowStock()) {
                log.warn("Medication ID {} is now low stock: {} units remaining",
                        medicationId, medication.getQuantityOnHand());
            }
        }

        return medication;
    }

    /**
     * Restocks medication inventory.
     *
     * @param medicationId the medication ID
     * @param quantityAdded quantity to add
     * @param lotNumber new lot number
     * @param expirationDate new expiration date
     * @return updated medication
     */
    @Transactional
    public Medication restockMedication(
            Long medicationId,
            Integer quantityAdded,
            String lotNumber,
            LocalDate expirationDate) {

        log.info("Restocking medication ID {}: +{} units", medicationId, quantityAdded);

        Medication medication = getMedicationById(medicationId);
        int currentQuantity = medication.getQuantityOnHand() != null ? medication.getQuantityOnHand() : 0;
        medication.setQuantityOnHand(currentQuantity + quantityAdded);
        medication.setLotNumber(lotNumber);
        medication.setExpirationDate(expirationDate);

        medication = medicationRepository.save(medication);
        log.info("Medication ID {} restocked: {} total units", medicationId, medication.getQuantityOnHand());

        return medication;
    }

    /**
     * Gets medications with low stock.
     *
     * @return list of medications needing reorder
     */
    public List<Medication> getLowStockMedications() {
        log.debug("Fetching low stock medications");
        return medicationRepository.findLowStockMedications();
    }

    /**
     * Gets expired medications.
     *
     * @return list of expired medications
     */
    public List<Medication> getExpiredMedications() {
        log.debug("Fetching expired medications");
        return medicationRepository.findExpiredMedications();
    }

    /**
     * Gets medications expiring soon (within 30 days).
     *
     * @return list of medications expiring soon
     */
    public List<Medication> getMedicationsExpiringSoon() {
        log.debug("Fetching medications expiring soon");
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return medicationRepository.findMedicationsExpiringSoon(thirtyDaysFromNow);
    }

    // ========================================================================
    // COMPLIANCE AND ALERTS
    // ========================================================================

    /**
     * Gets medications missing authorization.
     *
     * @return list of medications without proper authorization
     */
    public List<Medication> getMedicationsMissingAuthorization() {
        log.debug("Fetching medications missing authorization");
        return medicationRepository.findMedicationsMissingAuthorization();
    }

    /**
     * Gets medications requiring administration today.
     *
     * @return list of scheduled medications for today
     */
    public List<Medication> getMedicationsRequiringAdministrationToday() {
        log.debug("Fetching medications requiring administration today");

        return medicationRepository.findScheduledMedications().stream()
                .filter(Medication::requiresAdministrationToday)
                .toList();
    }

    /**
     * Gets pending parent notifications for medication administrations.
     *
     * @return list of administrations needing parent notification
     */
    public List<MedicationAdministration> getPendingParentNotifications() {
        log.debug("Fetching pending parent notifications for medication administrations");
        return medicationAdministrationRepository.findPendingParentNotifications();
    }

    /**
     * Gets controlled substances.
     *
     * @return list of controlled substance medications
     */
    public List<Medication> getControlledSubstances() {
        log.debug("Fetching controlled substances");
        return medicationRepository.findByControlledSubstance(true);
    }

    /**
     * Gets audit trail for a medication.
     *
     * @param medication the medication
     * @return complete administration history
     */
    public List<MedicationAdministration> getAuditTrail(Medication medication) {
        log.debug("Fetching audit trail for medication ID {}", medication.getId());
        return medicationAdministrationRepository.getAuditTrailForMedication(medication);
    }

    /**
     * Gets total count of active medications.
     *
     * @return count
     */
    public long getActiveMedicationCount() {
        return medicationRepository.countByActive(true);
    }

    /**
     * Gets total count of medication administrations.
     *
     * @return count
     */
    public long getMedicationAdministrationCount() {
        return medicationAdministrationRepository.count();
    }

    /**
     * Records a medication administration event.
     *
     * @param administration the medication administration record
     * @return saved medication administration with ID
     */
    @Transactional
    public MedicationAdministration recordAdministration(MedicationAdministration administration) {
        log.info("Recording medication administration for student ID {}",
                administration.getStudent().getId());

        MedicationAdministration saved = medicationAdministrationRepository.save(administration);

        log.info("Medication administration recorded - ID {}: {} administered to student {} by staff ID {}",
                saved.getId(),
                saved.getMedication().getMedicationName(),
                saved.getStudent().getId(),
                saved.getAdministeredByStaffId());

        return saved;
    }
}
