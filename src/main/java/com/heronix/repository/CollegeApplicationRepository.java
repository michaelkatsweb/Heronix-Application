package com.heronix.repository;

import com.heronix.model.domain.CollegeApplication;
import com.heronix.model.domain.CollegeApplication.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for College Application entities
 * Provides data access for college application tracking and planning
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Repository
public interface CollegeApplicationRepository extends JpaRepository<CollegeApplication, Long> {

    // Basic Queries
    List<CollegeApplication> findByStudentId(Long studentId);

    List<CollegeApplication> findByCounselorId(Long counselorId);

    List<CollegeApplication> findByApplicationStatus(ApplicationStatus applicationStatus);

    List<CollegeApplication> findByAdmissionDecision(AdmissionDecision admissionDecision);

    // Student Applications
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "ORDER BY ca.applicationPriority ASC, ca.collegeName ASC")
    List<CollegeApplication> findByStudentOrderedByPriority(@Param("studentId") Long studentId);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.entryYear = :year " +
           "ORDER BY ca.applicationPriority ASC")
    List<CollegeApplication> findByStudentAndYear(@Param("studentId") Long studentId,
                                                    @Param("year") Integer year);

    // Application Category Queries
    List<CollegeApplication> findByApplicationCategory(ApplicationCategory category);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.applicationCategory = :category " +
           "ORDER BY ca.collegeName ASC")
    List<CollegeApplication> findByStudentAndCategory(@Param("studentId") Long studentId,
                                                        @Param("category") ApplicationCategory category);

    // College Type Queries
    List<CollegeApplication> findByCollegeType(CollegeType collegeType);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.collegeType IN " +
           "('FOUR_YEAR_PUBLIC', 'FOUR_YEAR_PRIVATE', 'LIBERAL_ARTS') " +
           "ORDER BY ca.collegeName ASC")
    List<CollegeApplication> findFourYearColleges();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.collegeType IN " +
           "('TWO_YEAR_PUBLIC', 'TWO_YEAR_PRIVATE', 'COMMUNITY_COLLEGE') " +
           "ORDER BY ca.collegeName ASC")
    List<CollegeApplication> findTwoYearColleges();

    // Application Type and Plan Queries
    List<CollegeApplication> findByApplicationType(ApplicationType applicationType);

    List<CollegeApplication> findByApplicationPlan(ApplicationPlan applicationPlan);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationPlan IN " +
           "('EARLY_DECISION', 'EARLY_DECISION_II') " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findEarlyDecisionApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationPlan = 'EARLY_ACTION' " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findEarlyActionApplications();

    // Status Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationStatus = 'PLANNING' " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findPlannedApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationStatus = 'IN_PROGRESS' " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findInProgressApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationStatus = 'SUBMITTED' " +
           "ORDER BY ca.applicationSubmittedDate DESC")
    List<CollegeApplication> findSubmittedApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationStatus = 'DECISION_RECEIVED' " +
           "ORDER BY ca.decisionDate DESC")
    List<CollegeApplication> findDecisionReceivedApplications();

    // Deadline Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationDeadline BETWEEN :startDate AND :endDate " +
           "AND ca.applicationSubmittedDate IS NULL " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findUpcomingDeadlines(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationDeadline <= :date " +
           "AND ca.applicationSubmittedDate IS NULL " +
           "AND ca.applicationStatus != 'WITHDRAWN' " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findOverdueApplications(@Param("date") LocalDate date);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationDeadline BETWEEN CURRENT_DATE AND :futureDate " +
           "AND ca.applicationSubmittedDate IS NULL " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findDeadlinesApproaching(@Param("futureDate") LocalDate futureDate);

    // Requirements Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.applicationStatus IN ('PLANNING', 'IN_PROGRESS') " +
           "AND ((ca.essayRequired = true AND ca.essaySubmitted = false) " +
           "OR (ca.transcriptRequested = true AND ca.transcriptSentDate IS NULL) " +
           "OR (ca.testScoresRequired = true AND ca.testScoresSent = false) " +
           "OR (ca.lettersOfRecommendationRequired IS NOT NULL " +
           "    AND ca.lettersOfRecommendationSubmitted < ca.lettersOfRecommendationRequired) " +
           "OR (ca.portfolioRequired = true AND ca.portfolioSubmitted = false) " +
           "OR (ca.interviewRequired = true AND ca.interviewCompleted = false)) " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findWithMissingRequirements();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.essayRequired = true " +
           "AND ca.essaySubmitted = false " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findNeedingEssay();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.transcriptRequested = true " +
           "AND ca.transcriptSentDate IS NULL " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findNeedingTranscript();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.testScoresRequired = true " +
           "AND ca.testScoresSent = false " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findNeedingTestScores();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.lettersOfRecommendationRequired IS NOT NULL " +
           "AND ca.lettersOfRecommendationSubmitted < ca.lettersOfRecommendationRequired " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findNeedingRecommendations();

    // Admission Decision Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.admissionDecision = 'ACCEPTED' " +
           "ORDER BY ca.decisionDate DESC")
    List<CollegeApplication> findAcceptedApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.admissionDecision = 'ACCEPTED' " +
           "ORDER BY ca.applicationPriority ASC")
    List<CollegeApplication> findAcceptedByStudent(@Param("studentId") Long studentId);

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.admissionDecision = 'WAITLISTED' " +
           "ORDER BY ca.collegeName ASC")
    List<CollegeApplication> findWaitlistedApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.admissionDecision = 'DEFERRED' " +
           "ORDER BY ca.collegeName ASC")
    List<CollegeApplication> findDeferredApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.admissionDecision = 'DENIED' " +
           "ORDER BY ca.decisionDate DESC")
    List<CollegeApplication> findDeniedApplications();

    // Decision Response Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.admissionDecision IN ('ACCEPTED', 'CONDITIONAL_ACCEPTANCE') " +
           "AND ca.enrollmentConfirmed = false " +
           "AND ca.studentDeclined = false " +
           "AND ca.decisionDeadline IS NOT NULL " +
           "ORDER BY ca.decisionDeadline ASC")
    List<CollegeApplication> findNeedingDecisionResponse();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.admissionDecision IN ('ACCEPTED', 'CONDITIONAL_ACCEPTANCE') " +
           "AND ca.enrollmentDepositRequired = true " +
           "AND ca.enrollmentDepositPaid = false " +
           "ORDER BY ca.enrollmentDepositDeadline ASC")
    List<CollegeApplication> findNeedingDepositPayment();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.enrollmentConfirmed = true " +
           "ORDER BY ca.enrollmentConfirmationDate DESC")
    List<CollegeApplication> findEnrolledApplications();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.enrollmentConfirmed = true")
    List<CollegeApplication> findFinalCollegeChoice(@Param("studentId") Long studentId);

    // Financial Aid Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.fafsaRequired = true " +
           "AND ca.financialAidApplied = false " +
           "ORDER BY ca.financialAidDeadline ASC")
    List<CollegeApplication> findNeedingFAFSA();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.cssProfileRequired = true " +
           "AND ca.financialAidApplied = false " +
           "ORDER BY ca.financialAidDeadline ASC")
    List<CollegeApplication> findNeedingCSSProfile();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.financialAidOffered = true " +
           "ORDER BY ca.financialAidPackageAmount DESC")
    List<CollegeApplication> findWithFinancialAid();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.financialAidOffered = true " +
           "ORDER BY ca.netCost ASC")
    List<CollegeApplication> findByStudentOrderedByAffordability(@Param("studentId") Long studentId);

    // Scholarship Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.meritScholarshipOffered = true " +
           "ORDER BY ca.meritScholarshipAmount DESC")
    List<CollegeApplication> findWithMeritScholarships();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.athleticScholarshipOffered = true " +
           "ORDER BY ca.athleticScholarshipAmount DESC")
    List<CollegeApplication> findWithAthleticScholarships();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND (ca.meritScholarshipOffered = true OR ca.athleticScholarshipOffered = true) " +
           "ORDER BY ca.meritScholarshipAmount + ca.athleticScholarshipAmount DESC")
    List<CollegeApplication> findScholarshipsForStudent(@Param("studentId") Long studentId);

    // Campus Visit Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.campusVisitCompleted = false " +
           "AND ca.applicationStatus IN ('PLANNING', 'IN_PROGRESS') " +
           "ORDER BY ca.applicationDeadline ASC")
    List<CollegeApplication> findNeedingCampusVisit();

    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.acceptedStudentsDayAttended = false " +
           "AND ca.admissionDecision = 'ACCEPTED' " +
           "ORDER BY ca.decisionDeadline ASC")
    List<CollegeApplication> findAcceptedNeedingVisit();

    // Housing Queries
    @Query("SELECT ca FROM CollegeApplication ca WHERE ca.housingApplicationRequired = true " +
           "AND ca.housingApplicationSubmitted = false " +
           "ORDER BY ca.housingApplicationDeadline ASC")
    List<CollegeApplication> findNeedingHousingApplication();

    // Statistics
    @Query("SELECT COUNT(ca) FROM CollegeApplication ca WHERE ca.student.id = :studentId")
    Long countByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(ca) FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.applicationStatus = 'SUBMITTED'")
    Long countSubmittedByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(ca) FROM CollegeApplication ca WHERE ca.student.id = :studentId " +
           "AND ca.admissionDecision = 'ACCEPTED'")
    Long countAcceptedByStudent(@Param("studentId") Long studentId);

    @Query("SELECT ca.collegeType, COUNT(ca) FROM CollegeApplication ca " +
           "GROUP BY ca.collegeType ORDER BY COUNT(ca) DESC")
    List<Object[]> countByCollegeType();

    @Query("SELECT ca.applicationPlan, COUNT(ca) FROM CollegeApplication ca " +
           "GROUP BY ca.applicationPlan ORDER BY COUNT(ca) DESC")
    List<Object[]> countByApplicationPlan();

    @Query("SELECT ca.admissionDecision, COUNT(ca) FROM CollegeApplication ca " +
           "WHERE ca.admissionDecision IS NOT NULL " +
           "GROUP BY ca.admissionDecision")
    List<Object[]> countByAdmissionDecision();

    @Query("SELECT AVG(ca.satScore) FROM CollegeApplication ca WHERE ca.satScore IS NOT NULL")
    Double getAverageSATScore();

    @Query("SELECT AVG(ca.actScore) FROM CollegeApplication ca WHERE ca.actScore IS NOT NULL")
    Double getAverageACTScore();
}
