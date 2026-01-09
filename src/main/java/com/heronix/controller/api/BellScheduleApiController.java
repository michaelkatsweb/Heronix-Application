package com.heronix.controller.api;

import com.heronix.dto.BellScheduleDTO;
import com.heronix.dto.PeriodTimerDTO;
import com.heronix.service.BellScheduleConfigurationService;
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
 * REST API Controller for Bell Schedule Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/bell-schedules")
@RequiredArgsConstructor
public class BellScheduleApiController {

    private final BellScheduleConfigurationService bellScheduleService;

    // ==================== Bell Schedule CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<BellScheduleDTO>> getAllBellSchedules() {
        List<BellScheduleDTO> schedules = bellScheduleService.getAllBellSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BellScheduleDTO>> getActiveBellSchedules() {
        List<BellScheduleDTO> schedules = bellScheduleService.getActiveBellSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BellScheduleDTO> getBellScheduleById(@PathVariable Long id) {
        BellScheduleDTO schedule = bellScheduleService.getBellScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<BellScheduleDTO> createBellSchedule(@RequestBody BellScheduleDTO dto) {
        BellScheduleDTO created = bellScheduleService.createBellSchedule(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BellScheduleDTO> updateBellSchedule(
            @PathVariable Long id,
            @RequestBody BellScheduleDTO dto) {
        BellScheduleDTO updated = bellScheduleService.updateBellSchedule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBellSchedule(@PathVariable Long id) {
        bellScheduleService.deleteBellSchedule(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Period Management ====================

    @PostMapping("/{scheduleId}/periods")
    public ResponseEntity<BellScheduleDTO> addPeriod(
            @PathVariable Long scheduleId,
            @RequestBody PeriodTimerDTO periodDTO) {
        BellScheduleDTO updated = bellScheduleService.addPeriodToBellSchedule(scheduleId, periodDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(updated);
    }

    @DeleteMapping("/{scheduleId}/periods/{periodId}")
    public ResponseEntity<BellScheduleDTO> removePeriod(
            @PathVariable Long scheduleId,
            @PathVariable Long periodId) {
        BellScheduleDTO updated = bellScheduleService.removePeriodFromBellSchedule(scheduleId, periodId);
        return ResponseEntity.ok(updated);
    }

    // ==================== Query Operations ====================

    @GetMapping("/campus/{campusId}")
    public ResponseEntity<List<BellScheduleDTO>> getBellSchedulesByCampus(@PathVariable Long campusId) {
        List<BellScheduleDTO> schedules = bellScheduleService.getBellSchedulesByCampus(campusId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/academic-year/{academicYearId}")
    public ResponseEntity<List<BellScheduleDTO>> getBellSchedulesByAcademicYear(@PathVariable Long academicYearId) {
        List<BellScheduleDTO> schedules = bellScheduleService.getBellSchedulesByAcademicYear(academicYearId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/for-date")
    public ResponseEntity<BellScheduleDTO> getBellScheduleForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long campusId) {
        BellScheduleDTO schedule = bellScheduleService.getBellScheduleForDate(date, campusId);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<BellScheduleDTO> allSchedules = bellScheduleService.getAllBellSchedules();
        List<BellScheduleDTO> activeSchedules = bellScheduleService.getActiveBellSchedules();

        long regularCount = allSchedules.stream()
                .filter(s -> s.getScheduleType() == com.heronix.model.domain.BellSchedule.ScheduleType.REGULAR)
                .count();
        long earlyReleaseCount = allSchedules.stream()
                .filter(s -> s.getScheduleType() == com.heronix.model.domain.BellSchedule.ScheduleType.EARLY_RELEASE)
                .count();
        long lateStartCount = allSchedules.stream()
                .filter(s -> s.getScheduleType() == com.heronix.model.domain.BellSchedule.ScheduleType.LATE_START)
                .count();
        long blockScheduleCount = allSchedules.stream()
                .filter(s -> s.getScheduleType() == com.heronix.model.domain.BellSchedule.ScheduleType.BLOCK_SCHEDULE)
                .count();
        long assemblyCount = allSchedules.stream()
                .filter(s -> s.getScheduleType() == com.heronix.model.domain.BellSchedule.ScheduleType.ASSEMBLY)
                .count();
        long testingCount = allSchedules.stream()
                .filter(s -> s.getScheduleType() == com.heronix.model.domain.BellSchedule.ScheduleType.TESTING)
                .count();

        dashboard.put("totalSchedules", allSchedules.size());
        dashboard.put("activeSchedules", activeSchedules.size());
        dashboard.put("inactiveSchedules", allSchedules.size() - activeSchedules.size());
        dashboard.put("regularSchedules", regularCount);
        dashboard.put("earlyReleaseSchedules", earlyReleaseCount);
        dashboard.put("lateStartSchedules", lateStartCount);
        dashboard.put("blockSchedules", blockScheduleCount);
        dashboard.put("assemblySchedules", assemblyCount);
        dashboard.put("testingSchedules", testingCount);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/schedule/{id}")
    public ResponseEntity<Map<String, Object>> getScheduleDashboard(@PathVariable Long id) {
        Map<String, Object> dashboard = new HashMap<>();

        BellScheduleDTO schedule = bellScheduleService.getBellScheduleById(id);

        dashboard.put("scheduleId", id);
        dashboard.put("name", schedule.getName());
        dashboard.put("description", schedule.getDescription());
        dashboard.put("scheduleType", schedule.getScheduleType());
        dashboard.put("daysOfWeek", schedule.getDaysOfWeek());
        dashboard.put("isDefault", schedule.getIsDefault());
        dashboard.put("active", schedule.getActive());
        dashboard.put("periodCount", schedule.getPeriodCount());
        dashboard.put("totalInstructionalMinutes", schedule.getTotalInstructionalMinutes());
        dashboard.put("formattedSchedule", schedule.getFormattedSchedule());
        dashboard.put("displayName", schedule.getDisplayName());
        dashboard.put("isComplete", schedule.getIsComplete());
        dashboard.put("hasOverlappingPeriods", schedule.getHasOverlappingPeriods());
        dashboard.put("periods", schedule.getPeriods());
        dashboard.put("campusName", schedule.getCampusName());
        dashboard.put("academicYearName", schedule.getAcademicYearName());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/validation")
    public ResponseEntity<Map<String, Object>> getValidationDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<BellScheduleDTO> allSchedules = bellScheduleService.getActiveBellSchedules();

        List<BellScheduleDTO> incompleteSchedules = allSchedules.stream()
                .filter(s -> !s.getIsComplete())
                .toList();

        List<BellScheduleDTO> overlappingSchedules = allSchedules.stream()
                .filter(BellScheduleDTO::getHasOverlappingPeriods)
                .toList();

        dashboard.put("totalActiveSchedules", allSchedules.size());
        dashboard.put("incompleteSchedules", incompleteSchedules);
        dashboard.put("incompleteCount", incompleteSchedules.size());
        dashboard.put("overlappingSchedules", overlappingSchedules);
        dashboard.put("overlappingCount", overlappingSchedules.size());
        dashboard.put("validSchedulesCount", allSchedules.size() - incompleteSchedules.size() - overlappingSchedules.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/campus/{campusId}")
    public ResponseEntity<Map<String, Object>> getCampusDashboard(@PathVariable Long campusId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<BellScheduleDTO> campusSchedules = bellScheduleService.getBellSchedulesByCampus(campusId);

        long activeCount = campusSchedules.stream()
                .filter(BellScheduleDTO::getActive)
                .count();

        BellScheduleDTO defaultSchedule = campusSchedules.stream()
                .filter(BellScheduleDTO::getIsDefault)
                .findFirst()
                .orElse(null);

        dashboard.put("campusId", campusId);
        dashboard.put("totalSchedules", campusSchedules.size());
        dashboard.put("activeSchedules", activeCount);
        dashboard.put("defaultSchedule", defaultSchedule);
        dashboard.put("hasDefaultSchedule", defaultSchedule != null);
        dashboard.put("schedules", campusSchedules);

        return ResponseEntity.ok(dashboard);
    }
}
