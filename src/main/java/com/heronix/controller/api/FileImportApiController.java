package com.heronix.controller.api;

import com.heronix.exception.ImportException;
import com.heronix.model.dto.ImportResult;
import com.heronix.service.FileImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for File Import
 *
 * Provides endpoints for importing data from CSV and Excel files:
 * - Teachers
 * - Courses
 * - Rooms
 * - Students
 * - Schedules
 * - Events
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class FileImportApiController {

    private final FileImportService fileImportService;

    // ==================== Entity-Specific Import ====================

    @PostMapping("/teachers")
    public ResponseEntity<Map<String, Object>> importTeachers(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.importTeachers(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", "TEACHERS");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/courses")
    public ResponseEntity<Map<String, Object>> importCourses(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.importCourses(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", "COURSES");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/rooms")
    public ResponseEntity<Map<String, Object>> importRooms(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.importRooms(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", "ROOMS");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/students")
    public ResponseEntity<Map<String, Object>> importStudents(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.importStudents(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", "STUDENTS");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> importSchedule(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.importSchedule(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", "SCHEDULE");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> importEvents(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.importEvents(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", "EVENTS");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== Generic Import ====================

    @PostMapping("/generic")
    public ResponseEntity<Map<String, Object>> importGeneric(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType) {

        try {
            ImportResult result = fileImportService.importFile(file, entityType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("entityType", entityType);
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/excel")
    public ResponseEntity<Map<String, Object>> importExcel(
            @RequestParam("file") MultipartFile file) {

        try {
            ImportResult result = fileImportService.processExcelFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("fileType", "EXCEL");
            response.put("imported", result.getSuccessCount());
            response.put("failed", result.getErrorCount());
            response.put("total", result.getTotalProcessed());
            response.put("warnings", result.getWarnings());
            response.put("errors", result.getErrors());

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ==================== Validation ====================

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateFile(
            @RequestParam("file") MultipartFile file) {

        try {
            boolean isValid = fileImportService.validateFile(file);
            String fileType = fileImportService.getFileType(file.getOriginalFilename());
            boolean isSupported = fileImportService.isSupportedFileType(file.getOriginalFilename());

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("filename", file.getOriginalFilename());
            response.put("fileType", fileType);
            response.put("supported", isSupported);
            response.put("size", file.getSize());
            response.put("sizeKB", file.getSize() / 1024.0);

            return ResponseEntity.ok(response);
        } catch (ImportException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("valid", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ==================== Configuration ====================

    @GetMapping("/supported-formats")
    public ResponseEntity<Map<String, Object>> getSupportedFormats() {
        List<String> formats = fileImportService.getSupportedFormats();

        Map<String, Object> response = new HashMap<>();
        response.put("supportedFormats", formats);
        response.put("count", formats.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/entity-types")
    public ResponseEntity<Map<String, Object>> getEntityTypes() {
        Map<String, Object> entityTypes = new HashMap<>();

        entityTypes.put("TEACHERS", Map.of(
            "endpoint", "/api/import/teachers",
            "description", "Import teacher records",
            "sampleData", "H:\\Heronix Scheduler\\sample_data\\teachers.csv"
        ));

        entityTypes.put("COURSES", Map.of(
            "endpoint", "/api/import/courses",
            "description", "Import course sections",
            "sampleData", "H:\\Heronix Scheduler\\sample_data\\courses.csv"
        ));

        entityTypes.put("ROOMS", Map.of(
            "endpoint", "/api/import/rooms",
            "description", "Import classroom/room records",
            "sampleData", "H:\\Heronix Scheduler\\sample_data\\rooms.csv"
        ));

        entityTypes.put("STUDENTS", Map.of(
            "endpoint", "/api/import/students",
            "description", "Import student records",
            "sampleData", "H:\\Heronix Scheduler\\sample_data\\students.csv"
        ));

        entityTypes.put("SCHEDULE", Map.of(
            "endpoint", "/api/import/schedule",
            "description", "Import schedule data"
        ));

        entityTypes.put("EVENTS", Map.of(
            "endpoint", "/api/import/events",
            "description", "Import calendar events",
            "sampleData", "H:\\Heronix Scheduler\\sample_data\\events.csv"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("entityTypes", entityTypes);
        response.put("totalTypes", entityTypes.size());

        return ResponseEntity.ok(response);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "csvImport", "Import data from CSV files",
            "excelImport", "Import data from Excel files (.xlsx)",
            "fileValidation", "Validate files before import",
            "entityTypeSupport", "Import teachers, courses, rooms, students, events"
        ));

        metadata.put("limits", Map.of(
            "maxFileSize", "10MB",
            "supportedFormats", "CSV, XLSX"
        ));

        metadata.put("sampleData", Map.of(
            "location", "H:\\Heronix Scheduler\\sample_data\\",
            "files", List.of(
                "rooms.csv (82 rooms)",
                "teachers.csv (75 teachers)",
                "courses.csv (271 course sections)",
                "students.csv (1,500 students)",
                "events.csv (27 calendar events)"
            )
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("usage", Map.of(
            "import", "POST /api/import/{entityType} with multipart file",
            "validate", "POST /api/import/validate with multipart file",
            "formats", "GET /api/import/supported-formats"
        ));

        help.put("examples", Map.of(
            "importTeachers", "curl -F 'file=@teachers.csv' /api/import/teachers",
            "importCourses", "curl -F 'file=@courses.csv' /api/import/courses",
            "validateFile", "curl -F 'file=@data.csv' /api/import/validate"
        ));

        help.put("documentation", "See TEST_DATA_CSV_IMPORT_GUIDE.md for detailed instructions");

        return ResponseEntity.ok(help);
    }
}
