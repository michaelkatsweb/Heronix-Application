package com.heronix.repository;

import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.NurseVisit.Disposition;
import com.heronix.model.domain.NurseVisit.VisitReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for NurseVisit entities
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Repository
public interface NurseVisitRepository extends JpaRepository<NurseVisit, Long> {

    // Find all visits for a student
    List<NurseVisit> findByStudent(Student student);

    // Find visits for a student within date range
    List<NurseVisit> findByStudentAndVisitDateBetween(
            Student student,
            LocalDate startDate,
            LocalDate endDate
    );

    // Find all visits on a specific date
    List<NurseVisit> findByVisitDate(LocalDate visitDate);

    // Find all visits within date range
    List<NurseVisit> findByVisitDateBetween(LocalDate startDate, LocalDate endDate);

    // Find visits by reason
    List<NurseVisit> findByVisitReason(VisitReason visitReason);

    // Find visits by disposition
    List<NurseVisit> findByDisposition(Disposition disposition);

    // Find students sent home
    List<NurseVisit> findBySentHome(Boolean sentHome);

    // Find visits where parent was not notified but should be
    @Query("SELECT nv FROM NurseVisit nv WHERE nv.parentNotified = false " +
           "AND (nv.sentHome = true OR nv.hasFever = true OR nv.hasVomiting = true " +
           "OR nv.injurySeverity IN ('SERIOUS', 'EMERGENCY'))")
    List<NurseVisit> findVisitsRequiringParentNotification();

    // Find active visits (not checked out yet)
    @Query("SELECT nv FROM NurseVisit nv WHERE nv.checkOutTime IS NULL")
    List<NurseVisit> findActiveVisits();

    // Find visits requiring follow-up
    List<NurseVisit> findByRequiresFollowUp(Boolean requiresFollowUp);

    // Find emergency visits
    @Query("SELECT nv FROM NurseVisit nv WHERE nv.disposition = 'EMERGENCY_TRANSPORT' " +
           "OR nv.injurySeverity = 'EMERGENCY'")
    List<NurseVisit> findEmergencyVisits();

    // Find visits where medication was administered
    List<NurseVisit> findByMedicationAdministered(Boolean medicationAdministered);

    // Count visits by student
    Long countByStudent(Student student);

    // Count visits for a student within date range
    Long countByStudentAndVisitDateBetween(Student student, LocalDate startDate, LocalDate endDate);

    // Find frequent visitors (students with multiple visits)
    @Query("SELECT nv.student, COUNT(nv) as visitCount FROM NurseVisit nv " +
           "WHERE nv.visitDate BETWEEN :startDate AND :endDate " +
           "GROUP BY nv.student " +
           "HAVING COUNT(nv) >= :minimumVisits " +
           "ORDER BY visitCount DESC")
    List<Object[]> findFrequentVisitors(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minimumVisits") Long minimumVisits
    );

    // Find visits by attending nurse
    List<NurseVisit> findByAttendingNurseId(Long nurseId);
}
