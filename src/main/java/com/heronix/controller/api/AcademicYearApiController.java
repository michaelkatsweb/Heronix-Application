package com.heronix.controller.api;

import com.heronix.model.domain.AcademicYear;
import com.heronix.service.AcademicYearService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Academic Year Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
public class AcademicYearApiController {

    private final AcademicYearService academicYearService;

    // ==================== CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<AcademicYear>> getAllYears() {
        List<AcademicYear> years = academicYearService.getAllYears();
        return ResponseEntity.ok(years);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicYear> getYearById(@PathVariable Long id) {
        AcademicYear year = academicYearService.getYearById(id);
        return ResponseEntity.ok(year);
    }

    @GetMapping("/name/{yearName}")
    public ResponseEntity<AcademicYear> getYearByName(@PathVariable String yearName) {
        AcademicYear year = academicYearService.getYearByName(yearName);
        if (year == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(year);
    }

    @PostMapping
    public ResponseEntity<AcademicYear> createYear(
            @RequestParam String yearName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        AcademicYear created = academicYearService.createAcademicYear(yearName, startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicYear> updateYear(
            @PathVariable Long id,
            @RequestBody AcademicYear year) {
        year.setId(id);
        AcademicYear updated = academicYearService.updateYear(year);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteYear(@PathVariable Long id) {
        academicYearService.deleteYear(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Active Year Management ====================

    @GetMapping("/active")
    public ResponseEntity<AcademicYear> getActiveYear() {
        AcademicYear activeYear = academicYearService.getActiveYear();
        if (activeYear == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(activeYear);
    }

    @PatchMapping("/{id}/set-active")
    public ResponseEntity<AcademicYear> setActiveYear(@PathVariable Long id) {
        AcademicYear activated = academicYearService.setActiveYear(id);
        return ResponseEntity.ok(activated);
    }

    // ==================== Graduation Management ====================

    @PatchMapping("/{id}/schedule-graduation")
    public ResponseEntity<AcademicYear> scheduleGraduation(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate graduationDate) {
        AcademicYear updated = academicYearService.scheduleGraduation(id, graduationDate);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/mark-graduated")
    public ResponseEntity<AcademicYear> markAsGraduated(@PathVariable Long id) {
        AcademicYear updated = academicYearService.markAsGraduated(id);
        return ResponseEntity.ok(updated);
    }

    // ==================== Validation ====================

    @GetMapping("/name/{yearName}/available")
    public ResponseEntity<Map<String, Boolean>> checkYearNameAvailable(@PathVariable String yearName) {
        boolean available = academicYearService.isYearNameAvailable(yearName);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response);
    }

    // ==================== Statistics ====================

    @GetMapping("/statistics/count")
    public ResponseEntity<Map<String, Long>> getYearCount() {
        long count = academicYearService.getYearCount();

        Map<String, Long> response = new HashMap<>();
        response.put("totalYears", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary() {
        List<AcademicYear> allYears = academicYearService.getAllYears();
        AcademicYear activeYear = academicYearService.getActiveYear();

        long graduatedCount = allYears.stream()
                .filter(AcademicYear::isGraduated)
                .count();

        long upcomingCount = allYears.stream()
                .filter(y -> !y.isGraduated() && y.getStartDate().isAfter(LocalDate.now()))
                .count();

        long currentCount = allYears.stream()
                .filter(y -> !y.isGraduated() &&
                             !y.getStartDate().isAfter(LocalDate.now()) &&
                             !y.getEndDate().isBefore(LocalDate.now()))
                .count();

        Map<String, Object> response = new HashMap<>();
        response.put("totalYears", allYears.size());
        response.put("activeYear", activeYear);
        response.put("graduatedYears", graduatedCount);
        response.put("upcomingYears", upcomingCount);
        response.put("currentYears", currentCount);

        return ResponseEntity.ok(response);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<AcademicYear> allYears = academicYearService.getAllYears();
        AcademicYear activeYear = academicYearService.getActiveYear();

        long graduatedCount = allYears.stream()
                .filter(AcademicYear::isGraduated)
                .count();

        long activeCount = allYears.stream()
                .filter(AcademicYear::isActive)
                .count();

        dashboard.put("totalYears", allYears.size());
        dashboard.put("activeYear", activeYear);
        dashboard.put("hasActiveYear", activeYear != null);
        dashboard.put("graduatedYears", graduatedCount);
        dashboard.put("activeYears", activeCount);
        dashboard.put("allYears", allYears);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/year/{id}")
    public ResponseEntity<Map<String, Object>> getYearDashboard(@PathVariable Long id) {
        Map<String, Object> dashboard = new HashMap<>();

        AcademicYear year = academicYearService.getYearById(id);

        dashboard.put("yearId", id);
        dashboard.put("yearName", year.getYearName());
        dashboard.put("startDate", year.getStartDate());
        dashboard.put("endDate", year.getEndDate());
        dashboard.put("isActive", year.isActive());
        dashboard.put("isGraduated", year.isGraduated());
        dashboard.put("graduationDate", year.getGraduationDate());

        // Calculate year status
        LocalDate now = LocalDate.now();
        String status;
        if (year.isGraduated()) {
            status = "Graduated";
        } else if (year.getStartDate().isAfter(now)) {
            status = "Upcoming";
        } else if (year.getEndDate().isBefore(now)) {
            status = "Past";
        } else {
            status = "Current";
        }
        dashboard.put("status", status);

        // Calculate days remaining
        if (!year.isGraduated() && !year.getEndDate().isBefore(now)) {
            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, year.getEndDate());
            dashboard.put("daysRemaining", daysRemaining);
        } else {
            dashboard.put("daysRemaining", 0);
        }

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/active")
    public ResponseEntity<Map<String, Object>> getActiveYearDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        AcademicYear activeYear = academicYearService.getActiveYear();

        if (activeYear == null) {
            dashboard.put("hasActiveYear", false);
            dashboard.put("message", "No active academic year set");
            return ResponseEntity.ok(dashboard);
        }

        dashboard.put("hasActiveYear", true);
        dashboard.put("activeYear", activeYear);
        dashboard.put("yearName", activeYear.getYearName());
        dashboard.put("startDate", activeYear.getStartDate());
        dashboard.put("endDate", activeYear.getEndDate());
        dashboard.put("graduationDate", activeYear.getGraduationDate());

        // Calculate progress
        LocalDate now = LocalDate.now();
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                activeYear.getStartDate(), activeYear.getEndDate());
        long daysPassed = java.time.temporal.ChronoUnit.DAYS.between(
                activeYear.getStartDate(), now);

        double progressPercentage = totalDays > 0 ? (daysPassed * 100.0 / totalDays) : 0;
        progressPercentage = Math.max(0, Math.min(100, progressPercentage));

        dashboard.put("totalDays", totalDays);
        dashboard.put("daysPassed", Math.max(0, daysPassed));
        dashboard.put("daysRemaining", Math.max(0, totalDays - daysPassed));
        dashboard.put("progressPercentage", progressPercentage);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/graduated")
    public ResponseEntity<Map<String, Object>> getGraduatedYearsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<AcademicYear> allYears = academicYearService.getAllYears();

        List<AcademicYear> graduatedYears = allYears.stream()
                .filter(AcademicYear::isGraduated)
                .toList();

        dashboard.put("graduatedYears", graduatedYears);
        dashboard.put("graduatedCount", graduatedYears.size());
        dashboard.put("totalYears", allYears.size());

        return ResponseEntity.ok(dashboard);
    }
}
