package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Gifted and Talented Program Management
 * Handles gifted identification, GEP management, service delivery, and compliance
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GiftedManagementService {

    private final GiftedStudentRepository giftedStudentRepository;
    private final GiftedAssessmentRepository assessmentRepository;
    private final GiftedEducationPlanRepository planRepository;
    private final GiftedServiceRepository serviceRepository;

    // Gifted Student Management

    public List<GiftedStudent> getActiveGiftedStudents() {
        log.debug("Retrieving all active gifted students");
        return giftedStudentRepository.findAllActive();
    }

    /**
     * Alias for getActiveGiftedStudents
     */
    public List<GiftedStudent> findAllGiftedStudents() {
        return getActiveGiftedStudents();
    }

    public List<GiftedStudent> getGiftedStudentsByStatus(GiftedStudent.GiftedStatus status) {
        log.debug("Retrieving gifted students with status: {}", status);
        return giftedStudentRepository.findByGiftedStatus(status);
    }

    public List<GiftedStudent> getGiftedStudentsByArea(GiftedStudent.GiftedArea area) {
        log.debug("Retrieving gifted students in area: {}", area);
        return giftedStudentRepository.findByGiftedArea(area);
    }

    public Optional<GiftedStudent> getGiftedStudentByStudentId(Long studentId) {
        log.debug("Retrieving gifted student for student ID: {}", studentId);
        return giftedStudentRepository.findByStudentId(studentId);
    }

    /**
     * Find gifted student by Student entity
     */
    public Optional<GiftedStudent> findByStudent(Student student) {
        log.debug("Retrieving gifted student for student: {}", student.getId());
        return giftedStudentRepository.findByStudentId(student.getId());
    }

    /**
     * Create a new gifted student
     */
    public GiftedStudent createGiftedStudent(GiftedStudent giftedStudent) {
        log.info("Creating gifted student for student: {}", giftedStudent.getStudent().getId());
        return giftedStudentRepository.save(giftedStudent);
    }

    /**
     * Update an existing gifted student
     */
    public GiftedStudent updateGiftedStudent(GiftedStudent giftedStudent) {
        log.info("Updating gifted student: {}", giftedStudent.getId());
        return giftedStudentRepository.save(giftedStudent);
    }

    // Screening and Identification

    public List<GiftedStudent> getStudentsAwaitingScreening() {
        log.debug("Retrieving students awaiting gifted screening");
        return giftedStudentRepository.findAwaitingScreening();
    }

    public List<GiftedStudent> getStudentsInAssessment() {
        log.debug("Retrieving students in gifted assessment process");
        return giftedStudentRepository.findInAssessment();
    }

    public List<GiftedStudent> getEligibleStudents() {
        log.debug("Retrieving eligible gifted students");
        return giftedStudentRepository.findEligible();
    }

    // Assessment Management

    public List<GiftedAssessment> getAssessmentsByStudent(Long giftedStudentId) {
        log.debug("Retrieving assessments for gifted student ID: {}", giftedStudentId);
        return assessmentRepository.findByGiftedStudentId(giftedStudentId);
    }

    public List<GiftedAssessment> getHighlyGiftedAssessments() {
        log.debug("Retrieving highly gifted assessments (IQ >= 130)");
        return assessmentRepository.findHighlyGifted();
    }

    public List<GiftedAssessment> getExceptionallyGiftedAssessments() {
        log.debug("Retrieving exceptionally gifted assessments (IQ >= 145)");
        return assessmentRepository.findExceptionallyGifted();
    }

    public List<GiftedAssessment> getAssessmentsPendingResults() {
        log.debug("Retrieving assessments pending results");
        return assessmentRepository.findPendingResults(LocalDate.now());
    }

    public List<GiftedAssessment> getAssessmentsNeedingParentNotification() {
        log.debug("Retrieving assessments needing parent notification");
        return assessmentRepository.findNeedingParentNotification();
    }

    /**
     * Create a new gifted assessment
     */
    public GiftedAssessment createAssessment(GiftedAssessment assessment) {
        log.info("Creating gifted assessment for student: {}", assessment.getGiftedStudent().getId());
        return assessmentRepository.save(assessment);
    }

    /**
     * Update an existing gifted assessment
     */
    public GiftedAssessment updateAssessment(GiftedAssessment assessment) {
        log.info("Updating gifted assessment: {}", assessment.getId());
        return assessmentRepository.save(assessment);
    }

    // GEP Management

    public List<GiftedEducationPlan> getActivePlans() {
        log.debug("Retrieving active GEPs");
        return planRepository.findActivePlans(LocalDate.now());
    }

    public List<GiftedEducationPlan> getPlansByStudent(Long giftedStudentId) {
        log.debug("Retrieving GEPs for gifted student ID: {}", giftedStudentId);
        return planRepository.findByGiftedStudentId(giftedStudentId);
    }

    public Optional<GiftedEducationPlan> getPlanByNumber(String planNumber) {
        log.debug("Retrieving GEP by plan number: {}", planNumber);
        return planRepository.findByPlanNumber(planNumber);
    }

    public List<GiftedEducationPlan> getPlansDueForReview() {
        log.debug("Retrieving GEPs due for review");
        return planRepository.findDueForReview(LocalDate.now());
    }

    public List<GiftedEducationPlan> getPlansExpiringSoon(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        log.debug("Retrieving GEPs expiring within {} days", daysAhead);
        return planRepository.findExpiringSoon(LocalDate.now(), futureDate);
    }

    public List<GiftedEducationPlan> getPlansNeedingParentConsent() {
        log.debug("Retrieving GEPs needing parent consent");
        return planRepository.findNeedingParentConsent();
    }

    public List<GiftedEducationPlan> getPlansByCaseManager(Long caseManagerId) {
        log.debug("Retrieving GEPs for case manager ID: {}", caseManagerId);
        return planRepository.findByCaseManager(caseManagerId);
    }

    // Service Delivery

    public List<GiftedService> getServicesByStudent(Long giftedStudentId) {
        log.debug("Retrieving services for gifted student ID: {}", giftedStudentId);
        return serviceRepository.findByGiftedStudentId(giftedStudentId);
    }

    public List<GiftedService> getUpcomingServices() {
        log.debug("Retrieving upcoming gifted services");
        return serviceRepository.findUpcoming(LocalDate.now());
    }

    public List<GiftedService> getServicesNeedingDocumentation() {
        log.debug("Retrieving services needing documentation");
        return serviceRepository.findNeedingDocumentation();
    }

    public List<GiftedService> getServicesByProvider(Long providerId) {
        log.debug("Retrieving services for provider ID: {}", providerId);
        return serviceRepository.findByServiceProvider(providerId);
    }

    public List<GiftedService> getServicesNeedingFollowUp() {
        log.debug("Retrieving services needing follow-up");
        return serviceRepository.findNeedingFollowUp();
    }

    /**
     * Create a new gifted service
     */
    public GiftedService createService(GiftedService service) {
        log.info("Creating gifted service for student: {}", service.getGiftedStudent().getId());
        return serviceRepository.save(service);
    }

    /**
     * Update an existing gifted service
     */
    public GiftedService updateService(GiftedService service) {
        log.info("Updating gifted service: {}", service.getId());
        return serviceRepository.save(service);
    }

    // Service Minutes Tracking

    public Long calculateServiceMinutes(Long giftedStudentId, LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating service minutes for student ID {} from {} to {}",
                  giftedStudentId, startDate, endDate);
        Long minutes = serviceRepository.calculateTotalMinutes(giftedStudentId, startDate, endDate);
        return minutes != null ? minutes : 0L;
    }

    public Map<Long, Long> calculateServiceMinutesByStudent(LocalDate startDate, LocalDate endDate) {
        log.debug("Calculating service minutes by student from {} to {}", startDate, endDate);
        List<Object[]> results = serviceRepository.calculateMinutesByStudent(startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    // Progress Monitoring

    public List<GiftedStudent> getStudentsNeedingProgressReview(int daysOverdue) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOverdue);
        log.debug("Retrieving students overdue for progress review (cutoff: {})", cutoffDate);
        return giftedStudentRepository.findOverdueForProgressReview(cutoffDate);
    }

    public List<GiftedEducationPlan> getPlansOverdueForProgressReview(int daysOverdue) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysOverdue);
        log.debug("Retrieving plans overdue for progress review (cutoff: {})", cutoffDate);
        return planRepository.findOverdueForProgressReview(cutoffDate);
    }

    // Annual Review

    public List<GiftedStudent> getStudentsNeedingAnnualReview() {
        log.debug("Retrieving students needing annual review");
        return giftedStudentRepository.findNeedingAnnualReview(LocalDate.now());
    }

    public List<GiftedStudent> getStudentsWithUpcomingAnnualReview(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        log.debug("Retrieving students with annual reviews due within {} days", daysAhead);
        return giftedStudentRepository.findUpcomingAnnualReviews(LocalDate.now(), futureDate);
    }

    // Parent Communication

    public List<GiftedStudent> getStudentsNeedingParentNotification() {
        log.debug("Retrieving students needing parent notification");
        return giftedStudentRepository.findNeedingParentNotification();
    }

    public List<GiftedStudent> getStudentsNeedingParentConsent() {
        log.debug("Retrieving students needing parent consent");
        return giftedStudentRepository.findNeedingParentConsent();
    }

    // Performance Tracking

    public List<GiftedStudent> getUnderperformingStudents() {
        log.debug("Retrieving underperforming gifted students");
        return giftedStudentRepository.findNotMeetingExpectations();
    }

    public List<GiftedStudent> getStudentsBelowGPA(Double minGpa) {
        log.debug("Retrieving gifted students below GPA: {}", minGpa);
        return giftedStudentRepository.findBelowGPA(minGpa);
    }

    public List<GiftedStudent> getStudentsWithConcerns() {
        log.debug("Retrieving gifted students with concerns");
        return giftedStudentRepository.findWithConcerns();
    }

    // Advanced Coursework

    public List<GiftedStudent> getStudentsInAPCourses() {
        log.debug("Retrieving students enrolled in AP courses");
        return giftedStudentRepository.findEnrolledInAP();
    }

    public List<GiftedStudent> getStudentsInHonorsCourses() {
        log.debug("Retrieving students enrolled in Honors courses");
        return giftedStudentRepository.findEnrolledInHonors();
    }

    public List<GiftedStudent> getDualEnrollmentStudents() {
        log.debug("Retrieving dual enrollment students");
        return giftedStudentRepository.findDualEnrollment();
    }

    public List<GiftedStudent> getGradeAcceleratedStudents() {
        log.debug("Retrieving grade accelerated students");
        return giftedStudentRepository.findGradeAccelerated();
    }

    // Engagement Tracking

    public List<GiftedService> getHighEngagementServices() {
        log.debug("Retrieving services with high engagement");
        return serviceRepository.findHighEngagement();
    }

    public List<GiftedService> getLowEngagementServices() {
        log.debug("Retrieving services with low engagement");
        return serviceRepository.findLowEngagement();
    }

    // Statistics and Reporting

    public Map<String, Object> getGiftedProgramStatistics() {
        log.debug("Generating gifted program statistics");
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalActive", giftedStudentRepository.countActive());
        stats.put("totalEligible", giftedStudentRepository.countEligible());
        stats.put("activePlans", planRepository.countActive());
        stats.put("completedServices", serviceRepository.countCompleted());
        stats.put("averageIQ", assessmentRepository.getAverageIQ());
        stats.put("averageServiceMinutes", planRepository.getAverageServiceMinutes());

        stats.put("statusBreakdown", giftedStudentRepository.countByStatus());
        stats.put("areaBreakdown", giftedStudentRepository.countByPrimaryArea());
        stats.put("programTypeBreakdown", giftedStudentRepository.countByProgramType());

        return stats;
    }

    public Map<String, Object> getAssessmentStatistics() {
        log.debug("Generating assessment statistics");
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalValid", assessmentRepository.countValidAssessments());
        stats.put("averageIQ", assessmentRepository.getAverageIQ());
        stats.put("averagePercentile", assessmentRepository.getAveragePercentile());
        stats.put("typeBreakdown", assessmentRepository.countByAssessmentType());
        stats.put("purposeBreakdown", assessmentRepository.countByPurpose());

        return stats;
    }

    public Map<String, Object> getServiceStatistics(LocalDate startDate, LocalDate endDate) {
        log.debug("Generating service statistics from {} to {}", startDate, endDate);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCompleted", serviceRepository.countCompleted());
        stats.put("totalAttended", serviceRepository.countAttended());
        stats.put("averageDuration", serviceRepository.getAverageDuration());
        stats.put("uniqueStudentsServed",
                  serviceRepository.countUniqueStudentsServed(startDate, endDate));
        stats.put("typeBreakdown", serviceRepository.countByServiceType());
        stats.put("focusAreaBreakdown", serviceRepository.countByFocusArea());
        stats.put("engagementBreakdown", serviceRepository.countByEngagementLevel());

        return stats;
    }

    // Compliance Alerts

    public Map<String, List<?>> getComplianceAlerts() {
        log.debug("Generating compliance alerts for gifted program");
        Map<String, List<?>> alerts = new HashMap<>();

        alerts.put("awaitingScreening", getStudentsAwaitingScreening());
        alerts.put("needingAnnualReview", getStudentsNeedingAnnualReview());
        alerts.put("needingParentConsent", getStudentsNeedingParentConsent());
        alerts.put("plansDueForReview", getPlansDueForReview());
        alerts.put("plansExpiringSoon", getPlansExpiringSoon(30));
        alerts.put("servicesNeedingDocumentation", getServicesNeedingDocumentation());
        alerts.put("assessmentsPendingResults", getAssessmentsPendingResults());
        alerts.put("underperformingStudents", getUnderperformingStudents());

        return alerts;
    }

    // Talent Development

    public List<GiftedStudent> getStudentsWithActiveTalentPlan() {
        log.debug("Retrieving students with active talent development plans");
        return giftedStudentRepository.findWithActiveTalentPlan();
    }

    public List<GiftedStudent> getStudentsInMentorshipProgram() {
        log.debug("Retrieving students in mentorship programs");
        return giftedStudentRepository.findInMentorshipProgram();
    }

    public List<GiftedStudent> getMultiTalentedStudents() {
        log.debug("Retrieving multi-talented students");
        return giftedStudentRepository.findMultiTalented();
    }

    // Cluster Grouping

    public List<GiftedStudent> getClusterGroupedStudents() {
        log.debug("Retrieving cluster grouped students");
        return giftedStudentRepository.findClusterGrouped();
    }

    public List<GiftedStudent> getStudentsByClusterGroup(String groupName) {
        log.debug("Retrieving students in cluster group: {}", groupName);
        return giftedStudentRepository.findByClusterGroup(groupName);
    }
}
