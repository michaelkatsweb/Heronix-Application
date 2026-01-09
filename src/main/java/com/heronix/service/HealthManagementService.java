package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Service for Health Services Management
 * Handles health screenings, health plans, and incidents
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HealthManagementService {

    private final HealthScreeningRepository screeningRepository;
    private final HealthPlanRepository healthPlanRepository;
    private final HealthIncidentRepository incidentRepository;

    // Health Screening Management

    public List<HealthScreening> getScheduledScreenings() {
        log.debug("Retrieving all scheduled health screenings");
        return screeningRepository.findScheduled();
    }

    public List<HealthScreening> getScheduledScreeningsForDate(LocalDate date) {
        log.debug("Retrieving screenings scheduled for date: {}", date);
        return screeningRepository.findScheduledForDate(date);
    }

    public List<HealthScreening> getOverdueScreenings() {
        log.debug("Retrieving overdue health screenings");
        return screeningRepository.findOverdueScreenings(LocalDate.now());
    }

    public List<HealthScreening> getScreeningsNeedingFollowUp() {
        log.debug("Retrieving screenings needing follow-up");
        return screeningRepository.findNeedingFollowUp();
    }

    public List<HealthScreening> getVisionImpairments() {
        log.debug("Retrieving students with vision impairments");
        return screeningRepository.findVisionImpairments();
    }

    public List<HealthScreening> getHearingImpairments() {
        log.debug("Retrieving students with hearing impairments");
        return screeningRepository.findHearingImpairments();
    }

    public List<HealthScreening> getScreeningsNeedingParentNotification() {
        log.debug("Retrieving screenings needing parent notification");
        return screeningRepository.findNeedingParentNotification();
    }

    public List<HealthScreening> getScreeningsNeedingStateReporting() {
        log.debug("Retrieving screenings needing state reporting");
        return screeningRepository.findNeedingStateReporting();
    }

    // Health Plan Management

    public List<HealthPlan> getActiveHealthPlans() {
        log.debug("Retrieving all active health plans");
        return healthPlanRepository.findActivePlans(LocalDate.now());
    }

    public List<HealthPlan> getHealthPlansDueForReview() {
        log.debug("Retrieving health plans due for review");
        return healthPlanRepository.findDueForReview(LocalDate.now());
    }

    public List<HealthPlan> getHealthPlansNeedingStaffTraining() {
        log.debug("Retrieving health plans needing staff training");
        return healthPlanRepository.findNeedingStaffTraining();
    }

    public List<HealthPlan> getHealthPlansNeedingDistribution() {
        log.debug("Retrieving health plans needing distribution");
        return healthPlanRepository.findNeedingDistribution();
    }

    public List<HealthPlan> getActiveAsthmaPlans() {
        log.debug("Retrieving active asthma action plans");
        return healthPlanRepository.findActiveAsthmaPlans();
    }

    public List<HealthPlan> getActiveAllergyPlans() {
        log.debug("Retrieving active allergy action plans");
        return healthPlanRepository.findActiveAllergyPlans();
    }

    public List<HealthPlan> getActiveSeizurePlans() {
        log.debug("Retrieving active seizure action plans");
        return healthPlanRepository.findActiveSeizurePlans();
    }

    public List<HealthPlan> getActiveDiabetesPlans() {
        log.debug("Retrieving active diabetes management plans");
        return healthPlanRepository.findActiveDiabetesPlans();
    }

    public List<HealthPlan> getStudentsWithEpiPen() {
        log.debug("Retrieving students with EpiPens");
        return healthPlanRepository.findWithEpiPen();
    }

    public List<HealthPlan> getExpiredEpiPens() {
        log.debug("Retrieving expired EpiPens");
        return healthPlanRepository.findExpiredEpiPens(LocalDate.now());
    }

    public List<HealthPlan> getExpiringEpiPens(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        log.debug("Retrieving EpiPens expiring within {} days", daysAhead);
        return healthPlanRepository.findExpiringEpiPens(LocalDate.now(), futureDate);
    }

    public List<HealthPlan> getLifeThreateningConditions() {
        log.debug("Retrieving students with life-threatening conditions");
        return healthPlanRepository.findLifeThreateningConditions();
    }

    // Health Incident Management

    public List<HealthIncident> getTodayIncidents() {
        log.debug("Retrieving today's health incidents");
        return incidentRepository.findByDate(LocalDate.now());
    }

    public List<HealthIncident> getRecentIncidents(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        log.debug("Retrieving incidents from the past {} days", days);
        return incidentRepository.findSinceDate(startDate);
    }

    public List<HealthIncident> getSeriousIncidents() {
        log.debug("Retrieving serious and critical incidents");
        return incidentRepository.findSeriousIncidents();
    }

    public List<HealthIncident> getAmbulanceTransports() {
        log.debug("Retrieving ambulance transport incidents");
        return incidentRepository.findAmbulanceTransports();
    }

    public List<HealthIncident> getIncidentsNeedingParentNotification() {
        log.debug("Retrieving incidents needing parent notification");
        return incidentRepository.findNeedingParentNotification();
    }

    public List<HealthIncident> getIncidentsNeedingStateReporting() {
        log.debug("Retrieving incidents needing state reporting");
        return incidentRepository.findNeedingStateReporting();
    }

    public List<HealthIncident> getCommunicableDiseases() {
        log.debug("Retrieving communicable disease incidents");
        return incidentRepository.findCommunicableDiseases();
    }

    public List<HealthIncident> getStudentsInIsolation() {
        log.debug("Retrieving students currently in isolation");
        return incidentRepository.findCurrentlyInIsolation(LocalDate.now());
    }

    public List<HealthIncident> getIncidentsNeedingContactTracing() {
        log.debug("Retrieving incidents needing contact tracing");
        return incidentRepository.findNeedingContactTracing();
    }

    public List<HealthIncident> getIncidentsNeedingClearance() {
        log.debug("Retrieving incidents needing return-to-school clearance");
        return incidentRepository.findNeedingClearance();
    }

    // Compliance and Alerts

    public Map<String, List<?>> getHealthComplianceAlerts() {
        log.debug("Generating health compliance alerts");
        Map<String, List<?>> alerts = new HashMap<>();

        alerts.put("overdueScreenings", getOverdueScreenings());
        alerts.put("screeningsNeedingParentNotification", getScreeningsNeedingParentNotification());
        alerts.put("healthPlansDueForReview", getHealthPlansDueForReview());
        alerts.put("expiredEpiPens", getExpiredEpiPens());
        alerts.put("healthPlansNeedingTraining", getHealthPlansNeedingStaffTraining());
        alerts.put("incidentsNeedingStateReporting", getIncidentsNeedingStateReporting());
        alerts.put("incidentsNeedingContactTracing", getIncidentsNeedingContactTracing());

        return alerts;
    }

    // Statistics and Reporting

    public Map<String, Object> getScreeningStatistics() {
        log.debug("Generating screening statistics");
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCompleted", screeningRepository.countCompleted());
        stats.put("byType", screeningRepository.countByType());
        stats.put("byResult", screeningRepository.countByResult());
        stats.put("byStatus", screeningRepository.countByStatus());

        return stats;
    }

    public Map<String, Object> getIncidentStatistics(LocalDate startDate, LocalDate endDate) {
        log.debug("Generating incident statistics from {} to {}", startDate, endDate);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalIncidents", incidentRepository.findByDateRange(startDate, endDate).size());
        stats.put("byType", incidentRepository.countByType());
        stats.put("bySeverity", incidentRepository.countBySeverity());
        stats.put("ambulanceTransports", incidentRepository.countAmbulanceTransports());
        stats.put("hospitalTransports", incidentRepository.countHospitalTransports());
        stats.put("byLocation", incidentRepository.countByLocation());

        return stats;
    }

    public Map<String, Object> getHealthPlanStatistics() {
        log.debug("Generating health plan statistics");
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalActive", healthPlanRepository.countActive());
        stats.put("byType", healthPlanRepository.countByType());
        stats.put("byStatus", healthPlanRepository.countByStatus());
        stats.put("withEpiPen", healthPlanRepository.countWithEpiPen());
        stats.put("needingTraining", healthPlanRepository.countNeedingTraining());

        return stats;
    }

    // Outbreak Detection

    public List<Object[]> detectPotentialOutbreaks(int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        log.debug("Analyzing disease patterns for potential outbreaks in past {} days", days);
        return incidentRepository.countDiseasesByDateRange(startDate, LocalDate.now());
    }

    public boolean isPotentialOutbreak(HealthIncident.CommunicableDisease disease, int days, int threshold) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        Long count = incidentRepository.countSpecificDiseaseInRange(disease, startDate, LocalDate.now());
        log.debug("Checking outbreak threshold for {}: {} cases in {} days (threshold: {})",
                  disease, count, days, threshold);
        return count >= threshold;
    }

    // Student Health Summary

    public Map<String, Object> getStudentHealthSummary(Long studentId) {
        log.debug("Generating health summary for student ID: {}", studentId);
        Map<String, Object> summary = new HashMap<>();

        summary.put("healthPlans", healthPlanRepository.findByStudentId(studentId));
        summary.put("screenings", screeningRepository.findByStudentId(studentId));
        summary.put("incidents", incidentRepository.findByStudentId(studentId));

        return summary;
    }
}
