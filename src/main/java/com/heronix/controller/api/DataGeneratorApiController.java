package com.heronix.controller.api;

import com.heronix.service.MockDataGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Data Generator
 *
 * Provides endpoints for generating realistic test data for the attendance system.
 *
 * Features:
 * - Generate 500 students across grades 9-12
 * - Generate 15 teachers with certifications
 * - Generate courses (core + electives) with sections
 * - Generate bell schedule with 7 periods
 * - Enroll students in appropriate courses
 *
 * @author Heronix SIS Team
 * @version 2.0.0
 * @since January 2026 - Attendance System Enhancement
 */
@RestController
@RequestMapping("/api/data-generator")
@RequiredArgsConstructor
public class DataGeneratorApiController {

    private final MockDataGeneratorService mockDataGeneratorService;

    // ==================== Generator Status ====================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGeneratorStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("enabled", true);
        status.put("status", "ACTIVE");
        status.put("version", "2.0.0");
        status.put("description", "Mock data generator for Attendance System testing");

        status.put("capabilities", Map.of(
            "students", "Generate 500 students across grades 9-12",
            "teachers", "Generate 15 teachers with certifications",
            "courses", "Generate core and elective courses",
            "sections", "Create course sections with assignments",
            "bellSchedule", "Create 7-period bell schedule",
            "enrollments", "Enroll students in appropriate courses"
        ));

        return ResponseEntity.ok(status);
    }

    // ==================== Available Sample Data ====================

    @GetMapping("/sample-data")
    public ResponseEntity<Map<String, Object>> getSampleDataInfo() {
        Map<String, Object> sampleData = new HashMap<>();

        sampleData.put("location", "H:\\Heronix Scheduler\\sample_data\\");

        sampleData.put("files", Map.of(
            "teachers.csv", Map.of(
                "records", 75,
                "description", "Teacher records with certifications",
                "importEndpoint", "POST /api/import/teachers"
            ),
            "courses.csv", Map.of(
                "records", 271,
                "description", "Course sections with metadata",
                "importEndpoint", "POST /api/import/courses"
            ),
            "rooms.csv", Map.of(
                "records", 82,
                "description", "Classroom/room records",
                "importEndpoint", "POST /api/import/rooms"
            ),
            "students.csv", Map.of(
                "records", 1500,
                "description", "Student records",
                "importEndpoint", "POST /api/import/students"
            ),
            "events.csv", Map.of(
                "records", 27,
                "description", "Calendar events",
                "importEndpoint", "POST /api/import/events"
            )
        ));

        sampleData.put("totalRecords", 1955);
        sampleData.put("recommendedImportOrder", List.of(
            "1. teachers.csv",
            "2. rooms.csv",
            "3. courses.csv",
            "4. students.csv",
            "5. events.csv"
        ));

        return ResponseEntity.ok(sampleData);
    }

    // ==================== Import Instructions ====================

    @GetMapping("/import-guide")
    public ResponseEntity<Map<String, Object>> getImportGuide() {
        Map<String, Object> guide = new HashMap<>();

        guide.put("title", "CSV Import Guide");
        guide.put("overview", "Import realistic test data using CSV files instead of data generator");

        guide.put("steps", List.of(
            "1. Validate file: POST /api/import/validate with multipart file",
            "2. Import entity: POST /api/import/{entityType} with multipart file",
            "3. Check result: Response includes imported/failed counts and error messages",
            "4. Repeat for each entity type in recommended order"
        ));

        guide.put("exampleCurl", Map.of(
            "validateFile", "curl -F 'file=@teachers.csv' http://localhost:8080/api/import/validate",
            "importTeachers", "curl -F 'file=@teachers.csv' http://localhost:8080/api/import/teachers",
            "importCourses", "curl -F 'file=@courses.csv' http://localhost:8080/api/import/courses"
        ));

        guide.put("supportedFormats", List.of("CSV", "XLSX"));

        guide.put("validationEndpoints", Map.of(
            "checkFormats", "GET /api/import/supported-formats",
            "checkEntityTypes", "GET /api/import/entity-types",
            "validateFile", "POST /api/import/validate"
        ));

        return ResponseEntity.ok(guide);
    }

    // ==================== Generator Configuration (Future) ====================

    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getGeneratorConfiguration() {
        Map<String, Object> config = new HashMap<>();

        config.put("status", "DISABLED");
        config.put("message", "Data generator temporarily disabled");

        config.put("futureCapabilities", Map.of(
            "teachers", "Generate teachers with realistic certifications and experience",
            "courses", "Generate course sections with proper sequencing",
            "rooms", "Generate classrooms with capacity and type constraints",
            "students", "Generate student records with grade levels",
            "schedules", "Generate complete schedules with conflict detection",
            "enrollments", "Generate student enrollments with prerequisites"
        ));

        config.put("currentStatus", Map.of(
            "teachers", "Use CSV import - 75 records available",
            "courses", "Use CSV import - 271 records available",
            "rooms", "Use CSV import - 82 records available",
            "students", "Use CSV import - 1,500 records available",
            "events", "Use CSV import - 27 records available"
        ));

        return ResponseEntity.ok(config);
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("generatorStatus", "DISABLED");
        dashboard.put("availableAlternative", "CSV Import");
        dashboard.put("sampleDataLocation", "H:\\Heronix Scheduler\\sample_data\\");
        dashboard.put("totalSampleRecords", 1955);

        dashboard.put("quickLinks", Map.of(
            "importAPI", "/api/import",
            "validateFile", "/api/import/validate",
            "supportedFormats", "/api/import/supported-formats",
            "entityTypes", "/api/import/entity-types"
        ));

        dashboard.put("recommendation",
            "Use File Import API with provided sample CSV files for best results");

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("generatorEnabled", false);
        stats.put("generatorCalls", 0);
        stats.put("status", "Use CSV import for data population");

        stats.put("availableData", Map.of(
            "teachersAvailable", 75,
            "coursesAvailable", 271,
            "roomsAvailable", 82,
            "studentsAvailable", 1500,
            "eventsAvailable", 27
        ));

        return ResponseEntity.ok(stats);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("status", "DISABLED");

        metadata.put("disabledReason", Map.of(
            "issue", "Entity structure mismatches",
            "description", "Generator implementation needs refactoring to match current entities",
            "timeline", "Pending future enhancement"
        ));

        metadata.put("recommendedAlternative", Map.of(
            "method", "CSV File Import",
            "api", "FileImportApiController",
            "endpoint", "/api/import",
            "dataLocation", "H:\\Heronix Scheduler\\sample_data\\",
            "documentation", "See TEST_DATA_CSV_IMPORT_GUIDE.md"
        ));

        metadata.put("sampleDataSummary", Map.of(
            "totalFiles", 5,
            "totalRecords", 1955,
            "formats", List.of("CSV", "XLSX"),
            "quality", "Realistic production-ready test data"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Data Generator Help");
        help.put("currentStatus", "Generator is disabled - use CSV import instead");

        help.put("alternative", Map.of(
            "method", "CSV Import via File Import API",
            "location", "H:\\Heronix Scheduler\\sample_data\\",
            "steps", List.of(
                "1. Check supported formats: GET /api/import/supported-formats",
                "2. Validate your file: POST /api/import/validate",
                "3. Import data: POST /api/import/{entityType}",
                "4. Check results in response (imported/failed counts)"
            )
        ));

        help.put("availableEndpoints", Map.of(
            "status", "GET /api/data-generator/status - Check generator status",
            "sampleData", "GET /api/data-generator/sample-data - List available sample data",
            "importGuide", "GET /api/data-generator/import-guide - Get CSV import instructions",
            "fileImportAPI", "/api/import - File Import API endpoints"
        ));

        help.put("documentation",
            "See TEST_DATA_CSV_IMPORT_GUIDE.md for detailed CSV import instructions");

        return ResponseEntity.ok(help);
    }

    @PostMapping("/generate/all")
    public ResponseEntity<Map<String, Object>> generateAllData() {
        try {
            Map<String, Object> results = mockDataGeneratorService.generateAllMockData();
            results.put("success", true);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==================== Individual Generator Endpoints ====================

    @PostMapping("/generate/bell-schedule")
    public ResponseEntity<Map<String, Object>> generateBellSchedule() {
        try {
            var schedule = mockDataGeneratorService.generateBellSchedule();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("scheduleName", schedule.getName());
            response.put("periodCount", schedule.getPeriodCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate/rooms")
    public ResponseEntity<Map<String, Object>> generateRoomsNew() {
        try {
            var rooms = mockDataGeneratorService.generateRooms();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("roomsCreated", rooms.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate/teachers")
    public ResponseEntity<Map<String, Object>> generateTeachersNew(@RequestParam(defaultValue = "15") int count) {
        try {
            var teachers = mockDataGeneratorService.generateTeachers();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("teachersCreated", teachers.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate/courses")
    public ResponseEntity<Map<String, Object>> generateCoursesNew() {
        try {
            var courses = mockDataGeneratorService.generateCourses();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("coursesCreated", courses.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate/students")
    public ResponseEntity<Map<String, Object>> generateStudentsNew(@RequestParam(defaultValue = "500") int count) {
        try {
            var students = mockDataGeneratorService.generateStudents(count);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentsCreated", students.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllMockData() {
        try {
            mockDataGeneratorService.clearAllMockData();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All mock data cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
