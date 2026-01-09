package com.heronix.controller.api;

import com.heronix.model.domain.Campus;
import com.heronix.model.domain.District;
import com.heronix.service.CampusService;
import com.heronix.service.impl.CampusFederationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Campus Management
 * Includes basic CRUD and multi-campus federation features
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
public class CampusApiController {

    private final CampusService campusService;
    private final CampusFederationService federationService;

    // ==================== Basic CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<Campus>> getAllCampuses() {
        List<Campus> campuses = campusService.getAllCampuses();
        return ResponseEntity.ok(campuses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campus> getCampusById(@PathVariable Long id) {
        Campus campus = campusService.findById(id);
        if (campus == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(campus);
    }

    @GetMapping("/code/{campusCode}")
    public ResponseEntity<Campus> getCampusByCode(@PathVariable String campusCode) {
        return federationService.getCampusByCode(campusCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Campus> createCampus(@RequestBody Campus campus) {
        Campus created = campusService.save(campus);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampus(@PathVariable Long id) {
        Campus campus = campusService.findById(id);
        if (campus == null) {
            return ResponseEntity.notFound().build();
        }
        campusService.delete(campus);
        return ResponseEntity.noContent().build();
    }

    // ==================== District Operations ====================

    @PostMapping("/districts")
    public ResponseEntity<District> createDistrict(@RequestBody District district) {
        District created = federationService.createDistrict(district);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/districts")
    public ResponseEntity<List<District>> getAllActiveDistricts() {
        List<District> districts = federationService.getAllActiveDistricts();
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/districts/{id}")
    public ResponseEntity<District> getDistrictById(@PathVariable Long id) {
        return federationService.getDistrictById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/districts/{districtId}/summary")
    public ResponseEntity<CampusFederationService.DistrictSummary> getDistrictSummary(@PathVariable Long districtId) {
        CampusFederationService.DistrictSummary summary = federationService.getDistrictSummary(districtId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/districts/{districtId}/campuses")
    public ResponseEntity<Campus> createCampusInDistrict(
            @PathVariable Long districtId,
            @RequestBody Campus campus) {
        Campus created = federationService.createCampus(districtId, campus);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/districts/{districtId}/campuses")
    public ResponseEntity<List<Campus>> getCampusesByDistrict(@PathVariable Long districtId) {
        List<Campus> campuses = federationService.getCampusesByDistrict(districtId);
        return ResponseEntity.ok(campuses);
    }

    // ==================== Cross-Campus Teacher Sharing ====================

    @GetMapping("/districts/{districtId}/shared-teachers")
    public ResponseEntity<List<CampusFederationService.SharedTeacherInfo>> getAvailableSharedTeachers(@PathVariable Long districtId) {
        List<CampusFederationService.SharedTeacherInfo> teachers = federationService.getAvailableSharedTeachers(districtId);
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/shared-teachers/assign")
    public ResponseEntity<CampusFederationService.SharedTeacherAssignment> assignSharedTeacher(
            @RequestParam Long teacherId,
            @RequestParam Long targetCampusId,
            @RequestParam int periodsPerWeek) {
        CampusFederationService.SharedTeacherAssignment assignment =
                federationService.assignSharedTeacher(teacherId, targetCampusId, periodsPerWeek);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    // ==================== Cross-Campus Enrollment ====================

    @GetMapping("/cross-campus-options/{studentId}")
    public ResponseEntity<List<CampusFederationService.CrossCampusEnrollmentOption>> getCrossCampusEnrollmentOptions(
            @PathVariable Long studentId) {
        List<CampusFederationService.CrossCampusEnrollmentOption> options =
                federationService.getCrossCampusEnrollmentOptions(studentId);
        return ResponseEntity.ok(options);
    }

    @PostMapping("/cross-campus-enrollment")
    public ResponseEntity<CampusFederationService.CrossCampusEnrollmentResult> enrollStudentCrossCampus(
            @RequestParam Long studentId,
            @RequestParam Long targetCampusId,
            @RequestParam Long courseId) {
        CampusFederationService.CrossCampusEnrollmentResult result =
                federationService.enrollStudentCrossCampus(studentId, targetCampusId, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ==================== District Analytics ====================

    @GetMapping("/districts/{districtId}/capacity-analysis")
    public ResponseEntity<CampusFederationService.DistrictCapacityAnalysis> analyzeDistrictCapacity(@PathVariable Long districtId) {
        CampusFederationService.DistrictCapacityAnalysis analysis = federationService.analyzeDistrictCapacity(districtId);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/districts/{districtId}/resource-recommendations")
    public ResponseEntity<List<CampusFederationService.ResourceSharingRecommendation>> getResourceSharingRecommendations(
            @PathVariable Long districtId) {
        List<CampusFederationService.ResourceSharingRecommendation> recommendations =
                federationService.getResourceSharingRecommendations(districtId);
        return ResponseEntity.ok(recommendations);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getCampusOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Campus> allCampuses = campusService.getAllCampuses();
        List<District> allDistricts = federationService.getAllActiveDistricts();

        long activeCampuses = allCampuses.stream()
                .filter(c -> c.getActive() != null && c.getActive())
                .count();

        int totalEnrollment = allCampuses.stream()
                .filter(c -> c != null)
                .mapToInt(Campus::getCurrentEnrollment)
                .sum();

        int totalCapacity = allCampuses.stream()
                .filter(c -> c != null && c.getMaxStudents() != null)
                .mapToInt(Campus::getMaxStudents)
                .sum();

        dashboard.put("totalCampuses", allCampuses.size());
        dashboard.put("activeCampuses", activeCampuses);
        dashboard.put("inactiveCampuses", allCampuses.size() - activeCampuses);
        dashboard.put("totalDistricts", allDistricts.size());
        dashboard.put("totalEnrollment", totalEnrollment);
        dashboard.put("totalCapacity", totalCapacity);
        dashboard.put("averageUtilization",
                totalCapacity > 0 ? (totalEnrollment * 100.0 / totalCapacity) : 0.0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/campus/{id}")
    public ResponseEntity<Map<String, Object>> getCampusDashboard(@PathVariable Long id) {
        Map<String, Object> dashboard = new HashMap<>();

        Campus campus = campusService.findById(id);
        if (campus == null) {
            return ResponseEntity.notFound().build();
        }

        int maxStudents = campus.getMaxStudents() != null ? campus.getMaxStudents() : 0;
        int currentEnrollment = campus.getCurrentEnrollment();
        double utilization = maxStudents > 0 ? (currentEnrollment * 100.0 / maxStudents) : 0.0;

        dashboard.put("campusId", id);
        dashboard.put("campusName", campus.getName());
        dashboard.put("campusCode", campus.getCampusCode());
        dashboard.put("campusType", campus.getCampusType());
        dashboard.put("currentEnrollment", currentEnrollment);
        dashboard.put("maxStudents", maxStudents);
        dashboard.put("availableSeats", maxStudents - currentEnrollment);
        dashboard.put("utilizationPercentage", utilization);
        dashboard.put("gradeLevels", campus.getGradeLevels());
        dashboard.put("address", campus.getAddress());
        dashboard.put("active", campus.getActive());
        dashboard.put("hasDistrict", campus.getDistrict() != null);
        dashboard.put("districtName", campus.getDistrict() != null ? campus.getDistrict().getName() : null);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/district/{districtId}")
    public ResponseEntity<Map<String, Object>> getDistrictDashboard(@PathVariable Long districtId) {
        Map<String, Object> dashboard = new HashMap<>();

        CampusFederationService.DistrictSummary summary = federationService.getDistrictSummary(districtId);
        CampusFederationService.DistrictCapacityAnalysis capacity = federationService.analyzeDistrictCapacity(districtId);

        dashboard.put("districtId", districtId);
        dashboard.put("districtName", summary.getDistrictName());
        dashboard.put("districtCode", summary.getDistrictCode());
        dashboard.put("totalCampuses", summary.getTotalCampuses());
        dashboard.put("totalStudents", summary.getTotalStudents());
        dashboard.put("totalCapacity", summary.getTotalCapacity());
        dashboard.put("utilizationRate", summary.getUtilizationRate());
        dashboard.put("allowsCrossCampusEnrollment", summary.isAllowsCrossCampusEnrollment());
        dashboard.put("allowsSharedTeachers", summary.isAllowsSharedTeachers());
        dashboard.put("centralizedScheduling", summary.isCentralizedScheduling());
        dashboard.put("overcrowdedCampuses", capacity.getOvercrowdedCampuses());
        dashboard.put("underutilizedCampuses", capacity.getUnderutilizedCampuses());
        dashboard.put("campuses", summary.getCampusSummaries());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/capacity-status")
    public ResponseEntity<Map<String, Object>> getCapacityStatusDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Campus> allCampuses = campusService.getAllCampuses();

        long overcrowded = allCampuses.stream()
                .filter(c -> c != null && c.getMaxStudents() != null && c.getMaxStudents() > 0)
                .filter(c -> (c.getCurrentEnrollment() * 100.0 / c.getMaxStudents()) > 95)
                .count();

        long nearCapacity = allCampuses.stream()
                .filter(c -> c != null && c.getMaxStudents() != null && c.getMaxStudents() > 0)
                .filter(c -> {
                    double util = c.getCurrentEnrollment() * 100.0 / c.getMaxStudents();
                    return util > 85 && util <= 95;
                })
                .count();

        long healthy = allCampuses.stream()
                .filter(c -> c != null && c.getMaxStudents() != null && c.getMaxStudents() > 0)
                .filter(c -> {
                    double util = c.getCurrentEnrollment() * 100.0 / c.getMaxStudents();
                    return util > 50 && util <= 85;
                })
                .count();

        long underutilized = allCampuses.stream()
                .filter(c -> c != null && c.getMaxStudents() != null && c.getMaxStudents() > 0)
                .filter(c -> (c.getCurrentEnrollment() * 100.0 / c.getMaxStudents()) <= 50)
                .count();

        dashboard.put("totalCampuses", allCampuses.size());
        dashboard.put("overcrowded", overcrowded);
        dashboard.put("nearCapacity", nearCapacity);
        dashboard.put("healthy", healthy);
        dashboard.put("underutilized", underutilized);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/federation/{districtId}")
    public ResponseEntity<Map<String, Object>> getFederationDashboard(@PathVariable Long districtId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<CampusFederationService.SharedTeacherInfo> sharedTeachers =
                federationService.getAvailableSharedTeachers(districtId);
        List<CampusFederationService.ResourceSharingRecommendation> recommendations =
                federationService.getResourceSharingRecommendations(districtId);

        long teachersAvailableForSharing = sharedTeachers.stream()
                .filter(CampusFederationService.SharedTeacherInfo::isAvailableForSharing)
                .count();

        long highPriorityRecommendations = recommendations.stream()
                .filter(r -> "HIGH".equals(r.getPriority()))
                .count();

        dashboard.put("districtId", districtId);
        dashboard.put("totalSharedTeachers", sharedTeachers.size());
        dashboard.put("teachersAvailableForSharing", teachersAvailableForSharing);
        dashboard.put("totalRecommendations", recommendations.size());
        dashboard.put("highPriorityRecommendations", highPriorityRecommendations);
        dashboard.put("sharedTeachers", sharedTeachers);
        dashboard.put("recommendations", recommendations);

        return ResponseEntity.ok(dashboard);
    }
}
