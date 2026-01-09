package com.heronix.repository;

import com.heronix.model.domain.HealthPlan;
import com.heronix.model.domain.HealthPlan.PlanStatus;
import com.heronix.model.domain.HealthPlan.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Health Plan entities
 * Provides data access for health care plan management and compliance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface HealthPlanRepository extends JpaRepository<HealthPlan, Long> {

    // Basic Queries
    List<HealthPlan> findByStudentId(Long studentId);

    Optional<HealthPlan> findByPlanNumber(String planNumber);

    List<HealthPlan> findByPlanType(PlanType type);

    List<HealthPlan> findByPlanStatus(PlanStatus status);

    // Active Plans
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus = 'ACTIVE' " +
           "AND (hp.endDate IS NULL OR hp.endDate >= :today) " +
           "ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findActivePlans(@Param("today") LocalDate today);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus = 'ACTIVE' " +
           "ORDER BY hp.startDate DESC")
    List<HealthPlan> findByActiveStatus();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus IN ('ACTIVE', 'UNDER_REVIEW') " +
           "ORDER BY hp.startDate DESC")
    List<HealthPlan> findActiveAndUnderReview();

    // Plan Type Queries
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planType = :type " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findActiveByType(@Param("type") PlanType type);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planType = 'ASTHMA' " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findActiveAsthmaPlans();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planType IN ('ALLERGY', 'FOOD_ALLERGY', 'ANAPHYLAXIS') " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findActiveAllergyPlans();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planType = 'SEIZURE' " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findActiveSeizurePlans();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planType IN ('DIABETES_TYPE_1', 'DIABETES_TYPE_2') " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findActiveDiabetesPlans();

    // EpiPen Tracking
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.hasEpipen = true " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.epipenExpirationDate ASC")
    List<HealthPlan> findWithEpiPen();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.hasEpipen = true " +
           "AND hp.epipenExpirationDate <= :today AND hp.planStatus = 'ACTIVE' " +
           "ORDER BY hp.epipenExpirationDate ASC")
    List<HealthPlan> findExpiredEpiPens(@Param("today") LocalDate today);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.hasEpipen = true " +
           "AND hp.epipenExpirationDate BETWEEN :today AND :futureDate " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.epipenExpirationDate ASC")
    List<HealthPlan> findExpiringEpiPens(@Param("today") LocalDate today,
                                          @Param("futureDate") LocalDate futureDate);

    // Plan Status
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus = 'DRAFT' " +
           "ORDER BY hp.createdAt DESC")
    List<HealthPlan> findDrafts();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus = 'PENDING_PHYSICIAN_APPROVAL' " +
           "ORDER BY hp.createdAt ASC")
    List<HealthPlan> findPendingPhysicianApproval();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus = 'PENDING_PARENT_CONSENT' " +
           "ORDER BY hp.createdAt ASC")
    List<HealthPlan> findPendingParentConsent();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.planStatus = 'EXPIRED' " +
           "ORDER BY hp.endDate DESC")
    List<HealthPlan> findExpired();

    // Expiration and Review
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.endDate <= :today " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.endDate ASC")
    List<HealthPlan> findExpiring(@Param("today") LocalDate today);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.endDate BETWEEN :today AND :futureDate " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.endDate ASC")
    List<HealthPlan> findExpiringSoon(@Param("today") LocalDate today,
                                       @Param("futureDate") LocalDate futureDate);

    @Query("SELECT hp FROM HealthPlan hp WHERE " +
           "(hp.nextReviewDate IS NULL OR hp.nextReviewDate <= :today) " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.nextReviewDate ASC NULLS FIRST")
    List<HealthPlan> findDueForReview(@Param("today") LocalDate today);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.nextReviewDate BETWEEN :today AND :futureDate " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.nextReviewDate ASC")
    List<HealthPlan> findUpcomingReviews(@Param("today") LocalDate today,
                                          @Param("futureDate") LocalDate futureDate);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.annualReviewRequired = true " +
           "AND hp.planStatus = 'ACTIVE' AND hp.lastReviewDate IS NOT NULL " +
           "AND hp.lastReviewDate < :cutoffDate ORDER BY hp.lastReviewDate ASC")
    List<HealthPlan> findOverdueForReview(@Param("cutoffDate") LocalDate cutoffDate);

    // Physician Documentation
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.physicianOrdersOnFile = false " +
           "AND hp.planStatus IN ('DRAFT', 'PENDING_PHYSICIAN_APPROVAL', 'PENDING_PARENT_CONSENT') " +
           "ORDER BY hp.createdAt ASC")
    List<HealthPlan> findNeedingPhysicianOrders();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.physicianOrdersOnFile = true " +
           "ORDER BY hp.physicianSignatureDate DESC")
    List<HealthPlan> findWithPhysicianOrders();

    // Parent Consent
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.parentConsentReceived = false " +
           "AND hp.planStatus IN ('PENDING_PARENT_CONSENT', 'ACTIVE') " +
           "ORDER BY hp.createdAt ASC")
    List<HealthPlan> findNeedingParentConsent();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.parentConsentReceived = true " +
           "ORDER BY hp.parentConsentDate DESC")
    List<HealthPlan> findWithParentConsent();

    // Staff Training
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.staffTrainingRequired = true " +
           "AND hp.staffTrainingCompleted = false AND hp.planStatus = 'ACTIVE' " +
           "ORDER BY hp.startDate ASC")
    List<HealthPlan> findNeedingStaffTraining();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.staffTrainingCompleted = true " +
           "ORDER BY hp.staffTrainingCompletionDate DESC")
    List<HealthPlan> findWithCompletedTraining();

    // Distribution
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.distributedToTeachers = false " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.startDate ASC")
    List<HealthPlan> findNeedingDistribution();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.distributedToTeachers = true " +
           "ORDER BY hp.distributionDate DESC")
    List<HealthPlan> findDistributed();

    // Life-Threatening Conditions
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.conditionSeverity = 'LIFE_THREATENING' " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findLifeThreateningConditions();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.allergySeverity IN ('SEVERE', 'LIFE_THREATENING') " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findSevereAllergies();

    // Medication Tracking
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.studentSelfCarriesMedication = true " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findSelfCarryMedication();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.hasInhaler = true " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findWithInhaler();

    // Diabetes Specific
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.hasInsulinPump = true " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findWithInsulinPump();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.hasCgm = true " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findWithCGM();

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.carbCountingRequired = true " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.student.lastName ASC")
    List<HealthPlan> findRequiringCarbCounting();

    // Student Queries
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.student.id = :studentId " +
           "AND hp.planStatus = 'ACTIVE' ORDER BY hp.startDate DESC")
    List<HealthPlan> findActiveByStudent(@Param("studentId") Long studentId);

    @Query("SELECT hp FROM HealthPlan hp WHERE hp.student.id = :studentId " +
           "AND hp.planType = :type ORDER BY hp.startDate DESC")
    List<HealthPlan> findByStudentAndType(@Param("studentId") Long studentId,
                                           @Param("type") PlanType type);

    // Date Range Queries
    @Query("SELECT hp FROM HealthPlan hp WHERE hp.startDate BETWEEN :startDate AND :endDate " +
           "ORDER BY hp.startDate DESC")
    List<HealthPlan> findByStartDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT hp FROM HealthPlan hp WHERE :date BETWEEN hp.startDate " +
           "AND COALESCE(hp.endDate, :date) AND hp.planStatus = 'ACTIVE' " +
           "ORDER BY hp.startDate ASC")
    List<HealthPlan> findActiveOnDate(@Param("date") LocalDate date);

    // Statistics
    @Query("SELECT COUNT(hp) FROM HealthPlan hp WHERE hp.planStatus = 'ACTIVE'")
    Long countActive();

    @Query("SELECT hp.planStatus, COUNT(hp) FROM HealthPlan hp GROUP BY hp.planStatus")
    List<Object[]> countByStatus();

    @Query("SELECT hp.planType, COUNT(hp) FROM HealthPlan hp " +
           "WHERE hp.planStatus = 'ACTIVE' GROUP BY hp.planType")
    List<Object[]> countByType();

    @Query("SELECT COUNT(hp) FROM HealthPlan hp WHERE hp.hasEpipen = true " +
           "AND hp.planStatus = 'ACTIVE'")
    Long countWithEpiPen();

    @Query("SELECT COUNT(hp) FROM HealthPlan hp WHERE hp.staffTrainingRequired = true " +
           "AND hp.staffTrainingCompleted = false AND hp.planStatus = 'ACTIVE'")
    Long countNeedingTraining();
}
