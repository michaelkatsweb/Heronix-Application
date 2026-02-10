package com.heronix.controller.api;

import com.heronix.service.RotationScheduleService;
import com.heronix.service.RotationScheduleService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * REST API Controller for Rotation Schedule Management
 *
 * Provides endpoints for generating and managing K-8 specials rotation schedules:
 * - Special subjects rotation (Art, Music, PE, Library, Technology, STEM)
 * - Multiple rotation patterns (Daily, Weekly, Alternating, Fixed)
 * - Multi-grade rotation coordination
 * - Schedule balancing and validation
 *
 * Rotation Patterns:
 * - DAILY: Different special each day (5-day cycle)
 * - WEEKLY: Same special each week on the same day (5-day cycle)
 * - ALTERNATING: A/B day rotation (10-day cycle)
 * - FIXED: No rotation, same schedule every day
 *
 * Special Subjects:
 * - ART, MUSIC, PE (Physical Education), LIBRARY, TECHNOLOGY, STEM
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 32 - December 29, 2025
 */
@RestController
@RequestMapping("/api/rotation-schedule")
@RequiredArgsConstructor
public class RotationScheduleApiController {

    private final RotationScheduleService rotationScheduleService;

    // ==================== Rotation Generation ====================

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSpecialsRotation(
            @RequestBody Map<String, Object> requestBody) {

        try {
            int gradeLevel = Integer.parseInt(requestBody.get("gradeLevel").toString());
            String rotationTypeStr = (String) requestBody.get("rotationType");
            RotationType rotationType = RotationType.valueOf(rotationTypeStr);
            int periodsPerWeek = Integer.parseInt(requestBody.get("periodsPerWeek").toString());

            @SuppressWarnings("unchecked")
            List<String> subjectNames = (List<String>) requestBody.get("subjects");
            List<SpecialSubject> subjects = subjectNames.stream()
                .map(SpecialSubject::valueOf)
                .collect(Collectors.toList());

            RotationPattern pattern = rotationScheduleService.generateSpecialsRotation(
                gradeLevel, rotationType, subjects, periodsPerWeek);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pattern", convertPatternToMap(pattern));
            response.put("formattedSchedule", pattern.getFormattedSchedule());
            response.put("message", "Rotation pattern generated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid input: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate rotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/generate-multi-grade")
    public ResponseEntity<Map<String, Object>> generateMultiGradeRotation(
            @RequestBody Map<String, Object> requestBody) {

        try {
            @SuppressWarnings("unchecked")
            List<Integer> grades = ((List<?>) requestBody.get("grades")).stream()
                .map(g -> Integer.parseInt(g.toString()))
                .collect(Collectors.toList());

            String rotationTypeStr = (String) requestBody.get("rotationType");
            RotationType rotationType = RotationType.valueOf(rotationTypeStr);
            int periodsPerWeek = Integer.parseInt(requestBody.get("periodsPerWeek").toString());

            @SuppressWarnings("unchecked")
            List<String> subjectNames = (List<String>) requestBody.get("subjects");
            List<SpecialSubject> subjects = subjectNames.stream()
                .map(SpecialSubject::valueOf)
                .collect(Collectors.toList());

            Map<Integer, RotationPattern> gradeRotations = rotationScheduleService.generateMultiGradeRotation(
                grades, rotationType, subjects, periodsPerWeek);

            // Convert to serializable format
            Map<String, Object> rotationsMap = new HashMap<>();
            for (Map.Entry<Integer, RotationPattern> entry : gradeRotations.entrySet()) {
                rotationsMap.put("Grade " + entry.getKey(), convertPatternToMap(entry.getValue()));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gradeRotations", rotationsMap);
            response.put("grades", grades);
            response.put("message", "Multi-grade rotation generated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Invalid input: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate multi-grade rotation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Schedule Balancing ====================

    @PostMapping("/balance")
    public ResponseEntity<Map<String, Object>> balanceSpecialsSchedule(
            @RequestBody Map<String, Object> patternMap) {

        try {
            RotationPattern pattern = convertMapToPattern(patternMap);
            RotationPattern balanced = rotationScheduleService.balanceSpecialsSchedule(pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("balancedPattern", convertPatternToMap(balanced));
            response.put("formattedSchedule", balanced.getFormattedSchedule());
            response.put("message", "Schedule balanced successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to balance schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Validation ====================

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateRotationPattern(
            @RequestBody Map<String, Object> patternMap) {

        try {
            RotationPattern pattern = convertMapToPattern(patternMap);
            List<String> errors = rotationScheduleService.validateRotationPattern(pattern);

            Map<String, Object> response = new HashMap<>();
            response.put("success", errors.isEmpty());
            response.put("valid", errors.isEmpty());
            response.put("errors", errors);

            if (errors.isEmpty()) {
                response.put("message", "Rotation pattern is valid");
            } else {
                response.put("message", "Rotation pattern has " + errors.size() + " validation error(s)");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to validate pattern: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Query Operations ====================

    @GetMapping("/pattern/preview")
    public ResponseEntity<Map<String, Object>> previewRotationPattern(
            @RequestParam int gradeLevel,
            @RequestParam RotationType rotationType,
            @RequestParam int periodsPerWeek) {

        try {
            // Use all available subjects for preview
            List<SpecialSubject> allSubjects = Arrays.asList(SpecialSubject.values());

            RotationPattern pattern = rotationScheduleService.generateSpecialsRotation(
                gradeLevel, rotationType, allSubjects, periodsPerWeek);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("preview", convertPatternToMap(pattern));
            response.put("formattedSchedule", pattern.getFormattedSchedule());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to generate preview: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/pattern/{gradeLevel}/day/{dayOfWeek}")
    public ResponseEntity<Map<String, Object>> getSubjectsForDay(
            @PathVariable int gradeLevel,
            @PathVariable int dayOfWeek,
            @RequestParam RotationType rotationType,
            @RequestParam int periodsPerWeek) {

        try {
            if (dayOfWeek < 1 || dayOfWeek > 5) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Day of week must be between 1 (Monday) and 5 (Friday)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            List<SpecialSubject> allSubjects = Arrays.asList(SpecialSubject.values());
            RotationPattern pattern = rotationScheduleService.generateSpecialsRotation(
                gradeLevel, rotationType, allSubjects, periodsPerWeek);

            List<SpecialSubject> daySubjects = pattern.getSubjectsForDay(dayOfWeek);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gradeLevel", gradeLevel);
            response.put("dayOfWeek", dayOfWeek);
            response.put("dayName", getDayName(dayOfWeek));
            response.put("subjects", daySubjects.stream()
                .map(SpecialSubject::getDisplayName)
                .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get subjects for day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Generate Rotation",
                "endpoint", "POST /api/rotation-schedule/generate",
                "description", "Generate rotation pattern for a single grade"
            ),
            Map.of(
                "name", "Generate Multi-Grade Rotation",
                "endpoint", "POST /api/rotation-schedule/generate-multi-grade",
                "description", "Generate coordinated rotation for multiple grades"
            ),
            Map.of(
                "name", "Balance Schedule",
                "endpoint", "POST /api/rotation-schedule/balance",
                "description", "Balance special subjects across the week"
            ),
            Map.of(
                "name", "Validate Pattern",
                "endpoint", "POST /api/rotation-schedule/validate",
                "description", "Validate rotation pattern for conflicts and balance"
            ),
            Map.of(
                "name", "Preview Pattern",
                "endpoint", "GET /api/rotation-schedule/pattern/preview",
                "description", "Preview a rotation pattern before applying"
            )
        ));

        dashboard.put("rotationTypes", Arrays.stream(RotationType.values())
            .map(rt -> Map.of(
                "type", rt.name(),
                "displayName", rt.getDisplayName(),
                "cycleDays", rt.getCycleDays(),
                "description", getRotationTypeDescription(rt)
            ))
            .collect(Collectors.toList()));

        dashboard.put("specialSubjects", Arrays.stream(SpecialSubject.values())
            .map(ss -> Map.of(
                "subject", ss.name(),
                "displayName", ss.getDisplayName(),
                "courseCode", ss.getCourseCode()
            ))
            .collect(Collectors.toList()));

        dashboard.put("features", List.of(
            "Multiple rotation patterns (Daily, Weekly, Alternating, Fixed)",
            "Multi-grade rotation coordination to avoid teacher conflicts",
            "Schedule balancing for equal subject distribution",
            "Pattern validation for conflicts and balance",
            "Support for K-8 grades and all special subjects",
            "Preview functionality before implementation"
        ));

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/rotation-types")
    public ResponseEntity<Map<String, Object>> getRotationTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("rotationTypes", Arrays.stream(RotationType.values())
            .map(rt -> Map.of(
                "type", rt.name(),
                "displayName", rt.getDisplayName(),
                "cycleDays", rt.getCycleDays(),
                "description", getRotationTypeDescription(rt)
            ))
            .collect(Collectors.toList()));
        return ResponseEntity.ok(types);
    }

    @GetMapping("/reference/special-subjects")
    public ResponseEntity<Map<String, Object>> getSpecialSubjects() {
        Map<String, Object> subjects = new HashMap<>();
        subjects.put("specialSubjects", Arrays.stream(SpecialSubject.values())
            .map(ss -> Map.of(
                "subject", ss.name(),
                "displayName", ss.getDisplayName(),
                "courseCode", ss.getCourseCode()
            ))
            .collect(Collectors.toList()));
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/reference/grade-levels")
    public ResponseEntity<Map<String, Object>> getGradeLevels() {
        Map<String, Object> grades = new HashMap<>();
        grades.put("gradeLevels", List.of(
            Map.of("level", 0, "display", "Kindergarten"),
            Map.of("level", 1, "display", "1st Grade"),
            Map.of("level", 2, "display", "2nd Grade"),
            Map.of("level", 3, "display", "3rd Grade"),
            Map.of("level", 4, "display", "4th Grade"),
            Map.of("level", 5, "display", "5th Grade"),
            Map.of("level", 6, "display", "6th Grade"),
            Map.of("level", 7, "display", "7th Grade"),
            Map.of("level", 8, "display", "8th Grade")
        ));
        return ResponseEntity.ok(grades);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 32");
        metadata.put("category", "K-8 Schedule Management");
        metadata.put("description", "Specials rotation schedule generation and management for elementary and middle schools");

        metadata.put("capabilities", List.of(
            "Generate rotation patterns (Daily, Weekly, Alternating, Fixed)",
            "Multi-grade rotation coordination",
            "Schedule balancing for equal subject distribution",
            "Pattern validation and conflict detection",
            "Support for all special subjects (Art, Music, PE, Library, Technology, STEM)",
            "K-8 grade level support"
        ));

        metadata.put("endpoints", Map.of(
            "generation", List.of("POST /generate", "POST /generate-multi-grade"),
            "balancing", List.of("POST /balance"),
            "validation", List.of("POST /validate"),
            "queries", List.of("GET /pattern/preview", "GET /pattern/{gradeLevel}/day/{dayOfWeek}"),
            "reference", List.of("GET /reference/rotation-types", "GET /reference/special-subjects", "GET /reference/grade-levels")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Rotation Schedule API - Generate and manage K-8 specials rotation schedules");

        help.put("commonWorkflows", Map.of(
            "generateRotation", List.of(
                "1. GET /api/rotation-schedule/reference/rotation-types (choose rotation type)",
                "2. GET /api/rotation-schedule/reference/special-subjects (choose subjects)",
                "3. POST /api/rotation-schedule/generate (generate pattern)",
                "4. POST /api/rotation-schedule/validate (validate pattern)",
                "5. Apply pattern to school schedule"
            ),
            "previewBeforeApply", List.of(
                "1. GET /api/rotation-schedule/pattern/preview?gradeLevel=3&rotationType=DAILY&periodsPerWeek=5",
                "2. Review the formatted schedule",
                "3. POST /api/rotation-schedule/generate with chosen settings"
            ),
            "multiGradeSetup", List.of(
                "1. POST /api/rotation-schedule/generate-multi-grade with all grade levels",
                "2. System offsets each grade to avoid teacher conflicts",
                "3. POST /api/rotation-schedule/validate for each grade pattern",
                "4. Apply all patterns to school schedule"
            )
        ));

        help.put("rotationTypes", Map.of(
            "DAILY", "Different special each day (Art Monday, Music Tuesday, etc.)",
            "WEEKLY", "Same special each week on the same day (consistent weekly schedule)",
            "ALTERNATING", "A/B day rotation (10-day cycle)",
            "FIXED", "No rotation, same schedule every day"
        ));

        help.put("examples", Map.of(
            "generateDaily", "curl -X POST http://localhost:9590/api/rotation-schedule/generate -H 'Content-Type: application/json' -d '{\"gradeLevel\":3,\"rotationType\":\"DAILY\",\"subjects\":[\"ART\",\"MUSIC\",\"PE\",\"LIBRARY\"],\"periodsPerWeek\":5}'",
            "generateMultiGrade", "curl -X POST http://localhost:9590/api/rotation-schedule/generate-multi-grade -H 'Content-Type: application/json' -d '{\"grades\":[1,2,3,4,5],\"rotationType\":\"DAILY\",\"subjects\":[\"ART\",\"MUSIC\",\"PE\",\"LIBRARY\"],\"periodsPerWeek\":5}'",
            "preview", "curl 'http://localhost:9590/api/rotation-schedule/pattern/preview?gradeLevel=3&rotationType=DAILY&periodsPerWeek=5'",
            "getDaySubjects", "curl 'http://localhost:9590/api/rotation-schedule/pattern/3/day/1?rotationType=DAILY&periodsPerWeek=5'"
        ));

        help.put("notes", Map.of(
            "gradeSupport", "Supports grades K-8 (0-8 numeric values)",
            "multiGrade", "Multi-grade rotation offsets each grade to prevent teacher conflicts",
            "balancing", "Balancing ensures each special subject gets equal time across the week",
            "validation", "Validation checks for incomplete patterns, missing subjects, and imbalance"
        ));

        return ResponseEntity.ok(help);
    }

    // ==================== Helper Methods ====================

    private Map<String, Object> convertPatternToMap(RotationPattern pattern) {
        Map<String, Object> map = new HashMap<>();
        map.put("gradeLevel", pattern.getGradeLevel());
        map.put("rotationType", pattern.getRotationType().name());
        map.put("rotationTypeDisplay", pattern.getRotationType().getDisplayName());
        map.put("subjects", pattern.getSubjects().stream()
            .map(SpecialSubject::name)
            .collect(Collectors.toList()));
        map.put("periodsPerWeek", pattern.getPeriodsPerWeek());

        // Convert schedule
        Map<String, List<String>> scheduleMap = new HashMap<>();
        for (Map.Entry<Integer, List<SpecialSubject>> entry : pattern.getSchedule().entrySet()) {
            String dayName = getDayName(entry.getKey());
            List<String> daySubjects = entry.getValue().stream()
                .map(SpecialSubject::getDisplayName)
                .collect(Collectors.toList());
            scheduleMap.put(dayName, daySubjects);
        }
        map.put("schedule", scheduleMap);

        return map;
    }

    private RotationPattern convertMapToPattern(Map<String, Object> map) {
        RotationPattern pattern = new RotationPattern();
        pattern.setGradeLevel(Integer.parseInt(map.get("gradeLevel").toString()));
        pattern.setRotationType(RotationType.valueOf((String) map.get("rotationType")));
        pattern.setPeriodsPerWeek(Integer.parseInt(map.get("periodsPerWeek").toString()));

        @SuppressWarnings("unchecked")
        List<String> subjectNames = (List<String>) map.get("subjects");
        List<SpecialSubject> subjects = subjectNames.stream()
            .map(SpecialSubject::valueOf)
            .collect(Collectors.toList());
        pattern.setSubjects(subjects);

        // Convert schedule back
        @SuppressWarnings("unchecked")
        Map<String, List<String>> scheduleMap = (Map<String, List<String>>) map.get("schedule");
        Map<Integer, List<SpecialSubject>> schedule = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : scheduleMap.entrySet()) {
            int day = getDayNumber(entry.getKey());
            List<SpecialSubject> daySubjects = entry.getValue().stream()
                .map(name -> Arrays.stream(SpecialSubject.values())
                    .filter(ss -> ss.getDisplayName().equals(name))
                    .findFirst()
                    .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            schedule.put(day, daySubjects);
        }
        pattern.setSchedule(schedule);

        return pattern;
    }

    private String getDayName(int day) {
        String[] dayNames = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        return day >= 1 && day <= 5 ? dayNames[day] : "Unknown";
    }

    private int getDayNumber(String dayName) {
        Map<String, Integer> dayMap = Map.of(
            "Monday", 1, "Tuesday", 2, "Wednesday", 3, "Thursday", 4, "Friday", 5
        );
        return dayMap.getOrDefault(dayName, 0);
    }

    private String getRotationTypeDescription(RotationType type) {
        switch (type) {
            case DAILY:
                return "Different special each day (e.g., Art Monday, Music Tuesday, PE Wednesday, etc.)";
            case WEEKLY:
                return "Same special each week on the same day (consistent weekly schedule)";
            case ALTERNATING:
                return "A/B day rotation with different subjects on alternating days (10-day cycle)";
            case FIXED:
                return "No rotation - same schedule every day";
            default:
                return "";
        }
    }
}
