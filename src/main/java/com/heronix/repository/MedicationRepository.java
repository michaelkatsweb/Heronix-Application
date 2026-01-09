package com.heronix.repository;

import com.heronix.model.domain.Medication;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Medication.MedicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Medication entities
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    // Find all medications for a student
    List<Medication> findByStudent(Student student);

    // Find active medications for a student
    List<Medication> findByStudentAndActive(Student student, Boolean active);

    // Find all active medications
    List<Medication> findByActive(Boolean active);

    // Find medications by type
    List<Medication> findByMedicationType(MedicationType medicationType);

    // Find controlled substances
    List<Medication> findByControlledSubstance(Boolean controlledSubstance);

    // Find medications requiring refrigeration
    List<Medication> findByRequiresRefrigeration(Boolean requiresRefrigeration);

    // Find medications that are student self-administered
    List<Medication> findByStudentSelfAdministers(Boolean studentSelfAdministers);

    // Find medications missing authorization
    @Query("SELECT m FROM Medication m WHERE m.active = true " +
           "AND (m.authorizationFormOnFile = false OR m.parentConsentReceived = false)")
    List<Medication> findMedicationsMissingAuthorization();

    // Find expired medications
    @Query("SELECT m FROM Medication m WHERE m.active = true " +
           "AND m.expirationDate IS NOT NULL " +
           "AND m.expirationDate < CURRENT_DATE")
    List<Medication> findExpiredMedications();

    // Find medications expiring soon
    @Query("SELECT m FROM Medication m WHERE m.active = true " +
           "AND m.expirationDate IS NOT NULL " +
           "AND m.expirationDate BETWEEN CURRENT_DATE AND :expirationThreshold")
    List<Medication> findMedicationsExpiringSoon(@Param("expirationThreshold") LocalDate expirationThreshold);

    // Find medications with low stock
    @Query("SELECT m FROM Medication m WHERE m.active = true " +
           "AND m.quantityOnHand IS NOT NULL " +
           "AND m.reorderThreshold IS NOT NULL " +
           "AND m.quantityOnHand <= m.reorderThreshold")
    List<Medication> findLowStockMedications();

    // Find PRN (as needed) medications
    List<Medication> findByAsNeeded(Boolean asNeeded);

    // Find scheduled medications
    @Query("SELECT m FROM Medication m WHERE m.active = true AND m.asNeeded = false")
    List<Medication> findScheduledMedications();

    // Find medications ending soon
    @Query("SELECT m FROM Medication m WHERE m.active = true " +
           "AND m.endDate IS NOT NULL " +
           "AND m.endDate BETWEEN CURRENT_DATE AND :endDateThreshold")
    List<Medication> findMedicationsEndingSoon(@Param("endDateThreshold") LocalDate endDateThreshold);

    // Count active medications
    Long countByActive(Boolean active);

    // Count active medications for a student
    Long countByStudentAndActive(Student student, Boolean active);
}
