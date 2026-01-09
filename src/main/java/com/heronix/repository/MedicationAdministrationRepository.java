package com.heronix.repository;

import com.heronix.model.domain.MedicationAdministration;
import com.heronix.model.domain.Medication;
import com.heronix.model.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for MedicationAdministration entities
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Repository
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {

    // Find all administrations for a medication
    List<MedicationAdministration> findByMedication(Medication medication);

    // Find all administrations for a student
    List<MedicationAdministration> findByStudent(Student student);

    // Find administrations for a student within date range
    List<MedicationAdministration> findByStudentAndAdministrationDateBetween(
            Student student,
            LocalDate startDate,
            LocalDate endDate
    );

    // Find administrations on a specific date
    List<MedicationAdministration> findByAdministrationDate(LocalDate administrationDate);

    // Find administrations within date range
    List<MedicationAdministration> findByAdministrationDateBetween(LocalDate startDate, LocalDate endDate);

    // Find administrations by administrator (nurse)
    List<MedicationAdministration> findByAdministeredByStaffId(Long staffId);

    // Find administrations with adverse reactions
    List<MedicationAdministration> findByAdverseReactionObserved(Boolean adverseReactionObserved);

    // Find refused administrations
    List<MedicationAdministration> findByRefusedByStudent(Boolean refusedByStudent);

    // Find administrations requiring parent notification that haven't been sent
    @Query("SELECT ma FROM MedicationAdministration ma " +
           "WHERE ma.parentNotificationRequired = true AND ma.parentNotified = false")
    List<MedicationAdministration> findPendingParentNotifications();

    // Find last administration for a medication
    @Query("SELECT ma FROM MedicationAdministration ma " +
           "WHERE ma.medication = :medication " +
           "ORDER BY ma.administrationTimestamp DESC")
    List<MedicationAdministration> findLastAdministrationForMedication(@Param("medication") Medication medication);

    // Check if medication was administered today for a student
    @Query("SELECT COUNT(ma) > 0 FROM MedicationAdministration ma " +
           "WHERE ma.medication = :medication " +
           "AND ma.student = :student " +
           "AND ma.administrationDate = CURRENT_DATE")
    Boolean wasMedicationAdministeredToday(
            @Param("medication") Medication medication,
            @Param("student") Student student
    );

    // Count administrations for a medication
    Long countByMedication(Medication medication);

    // Count administrations for a student
    Long countByStudent(Student student);

    // Find administrations requiring follow-up
    @Query("SELECT ma FROM MedicationAdministration ma " +
           "WHERE ma.adverseReactionObserved = true " +
           "OR ma.studentResponse IN ('NEGATIVE', 'ADVERSE_REACTION')")
    List<MedicationAdministration> findAdministrationsRequiringFollowUp();

    // Get medication administration history for audit
    @Query("SELECT ma FROM MedicationAdministration ma " +
           "WHERE ma.medication = :medication " +
           "ORDER BY ma.administrationTimestamp DESC")
    List<MedicationAdministration> getAuditTrailForMedication(@Param("medication") Medication medication);

    // Find emergency administrations
    @Query("SELECT ma FROM MedicationAdministration ma " +
           "WHERE ma.administrationReason = 'EMERGENCY'")
    List<MedicationAdministration> findEmergencyAdministrations();
}
