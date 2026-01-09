package com.heronix.controller.api;

import com.heronix.service.RealisticDataGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Data Generator
 *
 * Provides endpoints for generating realistic test data for the system.
 * Note: Data generator is currently disabled due to entity structure mismatches.
 * Recommended approach: Use CSV import from sample data files.
 *
 * Alternative: CSV Import
 * - Location: H:\Heronix Scheduler\sample_data\
 * - Files: teachers.csv (75), courses.csv (271), rooms.csv (82), students.csv (1,500), events.csv (27)
 * - Use File Import API: POST /api/import/{entityType} with multipart file
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/data-generator")
@RequiredArgsConstructor
public class DataGeneratorApiController {

    private final RealisticDataGeneratorService dataGeneratorService;

    // ==================== Generator Status ====================

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getGeneratorStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("enabled", false);
        status.put("status", "DISABLED");
        status.put("reason", "Entity structure mismatches - pending refactoring");
        status.put("recommendation", "Use CSV import from sample data files");

        status.put("alternativeApproach", Map.of(
            "method", "CSV Import",
            "location", "H:\\Heronix Scheduler\\sample_data\\",
            "endpoint", "/api/import/{entityType}",
            "documentation", "See FileImportApiController for import endpoints"
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

    // ==================== Future Generator Endpoints (Disabled) ====================

    @PostMapping("/generate/teachers")
    public ResponseEntity<Map<String, Object>> generateTeachers(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Data generator is currently disabled");
        response.put("alternative", "Use CSV import: POST /api/import/teachers with teachers.csv");
        response.put("sampleFile", "H:\\Heronix Scheduler\\sample_data\\teachers.csv (75 records)");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/courses")
    public ResponseEntity<Map<String, Object>> generateCourses(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Data generator is currently disabled");
        response.put("alternative", "Use CSV import: POST /api/import/courses with courses.csv");
        response.put("sampleFile", "H:\\Heronix Scheduler\\sample_data\\courses.csv (271 records)");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/rooms")
    public ResponseEntity<Map<String, Object>> generateRooms(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Data generator is currently disabled");
        response.put("alternative", "Use CSV import: POST /api/import/rooms with rooms.csv");
        response.put("sampleFile", "H:\\Heronix Scheduler\\sample_data\\rooms.csv (82 records)");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/students")
    public ResponseEntity<Map<String, Object>> generateStudents(@RequestParam(defaultValue = "10") int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Data generator is currently disabled");
        response.put("alternative", "Use CSV import: POST /api/import/students with students.csv");
        response.put("sampleFile", "H:\\Heronix Scheduler\\sample_data\\students.csv (1,500 records)");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate/all")
    public ResponseEntity<Map<String, Object>> generateAllData() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Data generator is currently disabled");

        response.put("alternative", Map.of(
            "method", "Import all sample CSV files in order",
            "order", List.of(
                "1. POST /api/import/teachers with teachers.csv",
                "2. POST /api/import/rooms with rooms.csv",
                "3. POST /api/import/courses with courses.csv",
                "4. POST /api/import/students with students.csv",
                "5. POST /api/import/events with events.csv"
            ),
            "totalRecords", 1955
        ));

        return ResponseEntity.ok(response);
    }
}
