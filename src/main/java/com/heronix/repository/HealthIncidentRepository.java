package com.heronix.repository;

import com.heronix.model.domain.HealthIncident;
import com.heronix.model.domain.HealthIncident.CommunicableDisease;
import com.heronix.model.domain.HealthIncident.IncidentSeverity;
import com.heronix.model.domain.HealthIncident.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Health Incident entities
 * Provides data access for health incident tracking and reporting
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface HealthIncidentRepository extends JpaRepository<HealthIncident, Long> {

    // Basic Queries
    List<HealthIncident> findByStudentId(Long studentId);

    List<HealthIncident> findByStaffId(Long staffId);

    Optional<HealthIncident> findByIncidentNumber(String incidentNumber);

    List<HealthIncident> findByIncidentType(IncidentType type);

    List<HealthIncident> findBySeverity(IncidentSeverity severity);

    // Date Queries
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentDate = :date " +
           "ORDER BY hi.incidentTime ASC")
    List<HealthIncident> findByDate(@Param("date") LocalDate date);

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY hi.incidentDate DESC, hi.incidentTime DESC")
    List<HealthIncident> findByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentDate >= :startDate " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findSinceDate(@Param("startDate") LocalDate startDate);

    // Incident Type Queries
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'INJURY' " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findInjuries();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findCommunicableDiseases();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType IN ('ALLERGIC_REACTION', 'ASTHMA_ATTACK', 'SEIZURE', 'DIABETIC_EMERGENCY') " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findMedicalEmergencies();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'HEAD_INJURY' " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findHeadInjuries();

    // Severity Queries
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.severity IN ('SERIOUS', 'CRITICAL') " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findSeriousIncidents();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.severity = 'CRITICAL' " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findCriticalIncidents();

    // Emergency Response
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.ambulanceCalled = true " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findAmbulanceTransports();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.sentToHospital = true " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findHospitalTransports();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.sentHome = true " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findSentHome();

    // Parent Notification
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.parentNotified = false " +
           "AND (hi.sentHome = true OR hi.sentToHospital = true " +
           "OR hi.severity IN ('SERIOUS', 'CRITICAL')) " +
           "ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingParentNotification();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.parentNotified = true " +
           "ORDER BY hi.parentNotificationTime DESC")
    List<HealthIncident> findWithParentNotification();

    // Communicable Disease Specific
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "AND hi.diseaseType = :diseaseType ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findByDiseaseType(@Param("diseaseType") CommunicableDisease diseaseType);

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "AND hi.isolationRequired = true ORDER BY hi.isolationStartDate ASC")
    List<HealthIncident> findRequiringIsolation();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "AND hi.isolationRequired = true " +
           "AND hi.isolationStartDate <= :today " +
           "AND (hi.isolationEndDate IS NULL OR hi.isolationEndDate >= :today) " +
           "ORDER BY hi.isolationStartDate ASC")
    List<HealthIncident> findCurrentlyInIsolation(@Param("today") LocalDate today);

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "AND hi.returnToSchoolClearanceRequired = true AND hi.clearanceReceived = false " +
           "ORDER BY hi.isolationEndDate ASC")
    List<HealthIncident> findNeedingClearance();

    // Contact Tracing
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.contactTracingRequired = true " +
           "AND hi.contactTracingCompleted = false ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingContactTracing();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.contactTracingCompleted = true " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findWithCompletedContactTracing();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.notificationsSent = false " +
           "AND hi.contactTracingRequired = true ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingExposureNotifications();

    // State Reporting
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.stateReportable = true " +
           "AND hi.stateReported = false ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingStateReporting();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.stateReported = true " +
           "ORDER BY hi.stateReportDate DESC")
    List<HealthIncident> findStateReported();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.healthDepartmentNotified = false " +
           "AND hi.stateReportable = true ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingHealthDeptNotification();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.healthDepartmentNotified = true " +
           "ORDER BY hi.healthDepartmentNotificationDate DESC")
    List<HealthIncident> findHealthDeptNotified();

    // Follow-up
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.requiresFollowUp = true " +
           "ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingFollowUp();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.physicianVisitRecommended = true " +
           "AND hi.physicianVisitCompleted = false ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingPhysicianVisit();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.physicianVisitCompleted = true " +
           "ORDER BY hi.physicianVisitDate DESC")
    List<HealthIncident> findWithPhysicianVisit();

    // Incident Reports
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentReportCompleted = false " +
           "ORDER BY hi.incidentDate ASC")
    List<HealthIncident> findNeedingIncidentReport();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentReportCompleted = true " +
           "ORDER BY hi.incidentReportCompletionDate DESC")
    List<HealthIncident> findWithIncidentReport();

    // Workers Compensation (Staff)
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.workersCompClaim = true " +
           "ORDER BY hi.workersCompFiledDate DESC")
    List<HealthIncident> findWorkersCompClaims();

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.staff IS NOT NULL " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findStaffIncidents();

    // Location Analysis
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.incidentLocation = :location " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findByLocation(@Param("location") String location);

    @Query("SELECT hi.incidentLocation, COUNT(hi) FROM HealthIncident hi " +
           "GROUP BY hi.incidentLocation ORDER BY COUNT(hi) DESC")
    List<Object[]> countByLocation();

    // Treating Nurse
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.treatingNurse.id = :nurseId " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findByTreatingNurse(@Param("nurseId") Long nurseId);

    // Student Queries
    @Query("SELECT hi FROM HealthIncident hi WHERE hi.student.id = :studentId " +
           "AND hi.incidentType = :type ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findByStudentAndType(@Param("studentId") Long studentId,
                                                @Param("type") IncidentType type);

    @Query("SELECT hi FROM HealthIncident hi WHERE hi.student.id = :studentId " +
           "AND hi.incidentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY hi.incidentDate DESC")
    List<HealthIncident> findByStudentAndDateRange(@Param("studentId") Long studentId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // Outbreak Detection
    @Query("SELECT hi.diseaseType, COUNT(hi) FROM HealthIncident hi " +
           "WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "AND hi.incidentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY hi.diseaseType ORDER BY COUNT(hi) DESC")
    List<Object[]> countDiseasesByDateRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(hi) FROM HealthIncident hi " +
           "WHERE hi.incidentType = 'COMMUNICABLE_DISEASE' " +
           "AND hi.diseaseType = :diseaseType " +
           "AND hi.incidentDate BETWEEN :startDate AND :endDate")
    Long countSpecificDiseaseInRange(@Param("diseaseType") CommunicableDisease diseaseType,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Statistics
    @Query("SELECT COUNT(hi) FROM HealthIncident hi WHERE hi.incidentDate = :today")
    Long countToday(@Param("today") LocalDate today);

    @Query("SELECT hi.incidentType, COUNT(hi) FROM HealthIncident hi " +
           "GROUP BY hi.incidentType ORDER BY COUNT(hi) DESC")
    List<Object[]> countByType();

    @Query("SELECT hi.severity, COUNT(hi) FROM HealthIncident hi " +
           "GROUP BY hi.severity")
    List<Object[]> countBySeverity();

    @Query("SELECT COUNT(hi) FROM HealthIncident hi WHERE hi.ambulanceCalled = true")
    Long countAmbulanceTransports();

    @Query("SELECT COUNT(hi) FROM HealthIncident hi WHERE hi.sentToHospital = true")
    Long countHospitalTransports();

    @Query("SELECT COUNT(hi) FROM HealthIncident hi WHERE hi.stateReportable = true")
    Long countStateReportable();
}
