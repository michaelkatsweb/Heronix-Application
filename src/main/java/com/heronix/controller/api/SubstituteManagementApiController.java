package com.heronix.controller.api;

import com.heronix.model.domain.Substitute;
import com.heronix.model.domain.SubstituteAssignment;
import com.heronix.model.enums.SubstituteType;
import com.heronix.service.SubstituteManagementService;
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
 * REST API Controller for Substitute Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/substitutes")
@RequiredArgsConstructor
public class SubstituteManagementApiController {

    private final SubstituteManagementService substituteService;

    // ==================== Substitute CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<Substitute>> getAllSubstitutes() {
        List<Substitute> substitutes = substituteService.getAllSubstitutes();
        return ResponseEntity.ok(substitutes);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Substitute>> getActiveSubstitutes() {
        List<Substitute> substitutes = substituteService.getActiveSubstitutes();
        return ResponseEntity.ok(substitutes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Substitute> getSubstituteById(@PathVariable Long id) {
        return substituteService.getSubstituteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Substitute> getSubstituteByEmployeeId(@PathVariable String employeeId) {
        return substituteService.getSubstituteByEmployeeId(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Substitute>> getSubstitutesByType(@PathVariable SubstituteType type) {
        List<Substitute> substitutes = substituteService.getSubstitutesByType(type);
        return ResponseEntity.ok(substitutes);
    }

    @GetMapping("/type/{type}/active")
    public ResponseEntity<List<Substitute>> getActiveSubstitutesByType(@PathVariable SubstituteType type) {
        List<Substitute> substitutes = substituteService.getActiveSubstitutesByType(type);
        return ResponseEntity.ok(substitutes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Substitute>> searchSubstitutesByName(@RequestParam String name) {
        List<Substitute> substitutes = substituteService.searchSubstitutesByName(name);
        return ResponseEntity.ok(substitutes);
    }

    @GetMapping("/certification/{certification}")
    public ResponseEntity<List<Substitute>> getSubstitutesWithCertification(
            @PathVariable String certification) {
        List<Substitute> substitutes = substituteService.getSubstitutesWithCertification(certification);
        return ResponseEntity.ok(substitutes);
    }

    @PostMapping
    public ResponseEntity<Substitute> createSubstitute(@RequestBody Substitute substitute) {
        Substitute created = substituteService.saveSubstitute(substitute);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/create")
    public ResponseEntity<Substitute> createSubstituteWithDetails(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam SubstituteType type,
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber) {
        Substitute substitute = substituteService.createSubstitute(
                firstName, lastName, type, employeeId, email, phoneNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(substitute);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Substitute> updateSubstitute(
            @PathVariable Long id,
            @RequestBody Substitute substitute) {
        Substitute updated = substituteService.updateSubstitute(id, substitute);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateSubstitute(@PathVariable Long id) {
        substituteService.deactivateSubstitute(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubstitute(@PathVariable Long id) {
        substituteService.deleteSubstitute(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Assignment Operations ====================

    @GetMapping("/assignments")
    public ResponseEntity<List<SubstituteAssignment>> getAllAssignments() {
        List<SubstituteAssignment> assignments = substituteService.getAllAssignments();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/{id}")
    public ResponseEntity<SubstituteAssignment> getAssignmentById(@PathVariable Long id) {
        return substituteService.getAssignmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{substituteId}/assignments")
    public ResponseEntity<List<SubstituteAssignment>> getAssignmentsForSubstitute(
            @PathVariable Long substituteId) {
        List<SubstituteAssignment> assignments = substituteService.getAssignmentsForSubstitute(substituteId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/date/{date}")
    public ResponseEntity<List<SubstituteAssignment>> getAssignmentsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SubstituteAssignment> assignments = substituteService.getAssignmentsForDate(date);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/date-range")
    public ResponseEntity<List<SubstituteAssignment>> getAssignmentsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<SubstituteAssignment> assignments = substituteService.getAssignmentsBetweenDates(startDate, endDate);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/teacher/{teacherId}")
    public ResponseEntity<List<SubstituteAssignment>> getAssignmentsForTeacher(
            @PathVariable Long teacherId) {
        List<SubstituteAssignment> assignments = substituteService.getAssignmentsForTeacher(teacherId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/floaters/{date}")
    public ResponseEntity<List<SubstituteAssignment>> getFloaterAssignmentsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SubstituteAssignment> assignments = substituteService.getFloaterAssignmentsForDate(date);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping("/assignments")
    public ResponseEntity<SubstituteAssignment> createAssignment(
            @RequestBody SubstituteAssignment assignment) {
        SubstituteAssignment created = substituteService.saveAssignment(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/assignments/{id}")
    public ResponseEntity<SubstituteAssignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody SubstituteAssignment assignment) {
        assignment.setId(id);
        SubstituteAssignment updated = substituteService.saveAssignment(assignment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        substituteService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Statistics and Reports ====================

    @GetMapping("/statistics/active-count")
    public ResponseEntity<Long> countActiveSubstitutes() {
        long count = substituteService.countActiveSubstitutes();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/type-count/{type}")
    public ResponseEntity<Long> countSubstitutesByType(@PathVariable SubstituteType type) {
        long count = substituteService.countSubstitutesByType(type);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/assignments-count/{date}")
    public ResponseEntity<Long> countAssignmentsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        long count = substituteService.countAssignmentsForDate(date);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/floaters-count/{date}")
    public ResponseEntity<Long> countFloaterAssignmentsForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        long count = substituteService.countFloaterAssignmentsForDate(date);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{substituteId}/statistics/hours")
    public ResponseEntity<Double> getTotalHoursForSubstitute(
            @PathVariable Long substituteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double totalHours = substituteService.getTotalHoursForSubstitute(substituteId, startDate, endDate);
        return ResponseEntity.ok(totalHours);
    }

    @GetMapping("/{substituteId}/statistics/pay")
    public ResponseEntity<Double> getTotalPayForSubstitute(
            @PathVariable Long substituteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Double totalPay = substituteService.getTotalPayForSubstitute(substituteId, startDate, endDate);
        return ResponseEntity.ok(totalPay);
    }

    @GetMapping("/statistics/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> summary = new HashMap<>();

        long totalActive = substituteService.countActiveSubstitutes();
        long assignmentsCount = substituteService.countAssignmentsForDate(date);
        long floatersCount = substituteService.countFloaterAssignmentsForDate(date);

        summary.put("totalActiveSubstitutes", totalActive);
        summary.put("assignmentsForDate", assignmentsCount);
        summary.put("floatersForDate", floatersCount);
        summary.put("date", date);

        // Count by type
        Map<String, Long> typeCounts = new HashMap<>();
        for (SubstituteType type : SubstituteType.values()) {
            typeCounts.put(type.name(), substituteService.countSubstitutesByType(type));
        }
        summary.put("substitutesByType", typeCounts);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{substituteId}/statistics/summary")
    public ResponseEntity<Map<String, Object>> getSubstituteStatisticsSummary(
            @PathVariable Long substituteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        Double totalHours = substituteService.getTotalHoursForSubstitute(substituteId, startDate, endDate);
        Double totalPay = substituteService.getTotalPayForSubstitute(substituteId, startDate, endDate);
        List<SubstituteAssignment> assignments = substituteService.getAssignmentsBetweenDates(startDate, endDate)
                .stream()
                .filter(a -> a.getSubstitute() != null && substituteId.equals(a.getSubstitute().getId()))
                .toList();

        summary.put("substituteId", substituteId);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("totalHours", totalHours);
        summary.put("totalPay", totalPay);
        summary.put("assignmentCount", assignments.size());
        summary.put("assignments", assignments);

        return ResponseEntity.ok(summary);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/today")
    public ResponseEntity<Map<String, Object>> getTodayDashboard() {
        LocalDate today = LocalDate.now();
        Map<String, Object> dashboard = new HashMap<>();

        List<SubstituteAssignment> todayAssignments = substituteService.getAssignmentsForDate(today);
        List<SubstituteAssignment> floaters = substituteService.getFloaterAssignmentsForDate(today);
        long activeCount = substituteService.countActiveSubstitutes();

        dashboard.put("date", today);
        dashboard.put("totalAssignments", todayAssignments.size());
        dashboard.put("floaters", floaters.size());
        dashboard.put("totalActiveSubstitutes", activeCount);
        dashboard.put("assignments", todayAssignments);
        dashboard.put("floaterAssignments", floaters);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/week")
    public ResponseEntity<Map<String, Object>> getWeekDashboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        Map<String, Object> dashboard = new HashMap<>();

        List<SubstituteAssignment> weekAssignments = substituteService.getAssignmentsBetweenDates(startDate, endDate);
        long activeCount = substituteService.countActiveSubstitutes();

        dashboard.put("startDate", startDate);
        dashboard.put("endDate", endDate);
        dashboard.put("totalAssignments", weekAssignments.size());
        dashboard.put("totalActiveSubstitutes", activeCount);
        dashboard.put("assignments", weekAssignments);

        // Group assignments by date
        Map<LocalDate, Long> assignmentsByDate = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            long count = substituteService.countAssignmentsForDate(date);
            assignmentsByDate.put(date, count);
        }
        dashboard.put("assignmentsByDate", assignmentsByDate);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/month")
    public ResponseEntity<Map<String, Object>> getMonthDashboard(
            @RequestParam int year,
            @RequestParam int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        Map<String, Object> dashboard = new HashMap<>();

        List<SubstituteAssignment> monthAssignments = substituteService.getAssignmentsBetweenDates(startDate, endDate);

        dashboard.put("year", year);
        dashboard.put("month", month);
        dashboard.put("startDate", startDate);
        dashboard.put("endDate", endDate);
        dashboard.put("totalAssignments", monthAssignments.size());
        dashboard.put("assignments", monthAssignments);

        // Calculate total hours and pay for the month
        Map<Long, Double> substituteHours = new HashMap<>();
        Map<Long, Double> substitutePay = new HashMap<>();

        for (SubstituteAssignment assignment : monthAssignments) {
            if (assignment.getSubstitute() != null) {
                Long subId = assignment.getSubstitute().getId();
                Double hours = substituteService.getTotalHoursForSubstitute(subId, startDate, endDate);
                Double pay = substituteService.getTotalPayForSubstitute(subId, startDate, endDate);

                substituteHours.put(subId, hours);
                substitutePay.put(subId, pay);
            }
        }

        dashboard.put("substituteHours", substituteHours);
        dashboard.put("substitutePay", substitutePay);

        return ResponseEntity.ok(dashboard);
    }
}
