package com.heronix.controller.api;

import com.heronix.model.domain.Schedule;
import com.heronix.repository.ScheduleRepository;
import com.heronix.service.ScheduleExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Schedule Export and Print
 *
 * Provides endpoints for exporting schedules to various formats:
 * - PDF: Formatted printable schedules with color coding
 * - Excel (XLSX): Multi-sheet workbooks with statistics
 * - CSV: Simple comma-separated data export
 * - iCalendar (ICS): Calendar integration for Outlook, Google Calendar, etc.
 *
 * Export Features:
 * - Color-coded by subject area (PDF)
 * - Multiple sheets: schedule, conflicts, statistics (Excel)
 * - Compatible with calendar applications (iCalendar)
 * - Simple data export for analysis (CSV)
 *
 * Use Cases:
 * - Print master schedules for distribution
 * - Import into calendar applications
 * - Data analysis in Excel
 * - Archive schedules for records
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/schedule-export")
@RequiredArgsConstructor
public class ScheduleExportApiController {

    private final ScheduleExportService scheduleExportService;
    private final ScheduleRepository scheduleRepository;

    // ==================== PDF Export ====================

    @GetMapping("/schedules/{scheduleId}/pdf")
    public ResponseEntity<Resource> exportToPDF(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            File pdfFile = scheduleExportService.exportToPDF(scheduleOpt.get());
            Resource resource = new FileSystemResource(pdfFile);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFile.getName() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/schedules/{scheduleId}/pdf/info")
    public ResponseEntity<Map<String, Object>> getPDFExportInfo(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> info = new HashMap<>();
            info.put("scheduleId", scheduleId);
            info.put("scheduleName", scheduleOpt.get().getScheduleName());
            info.put("format", "PDF");
            info.put("features", List.of(
                "Color-coded by subject area",
                "Formatted for printing",
                "Professional layout",
                "Includes schedule metadata"
            ));
            info.put("downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/pdf");

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Excel Export ====================

    @GetMapping("/schedules/{scheduleId}/excel")
    public ResponseEntity<Resource> exportToExcel(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            File excelFile = scheduleExportService.exportToExcel(scheduleOpt.get());
            Resource resource = new FileSystemResource(excelFile);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + excelFile.getName() + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/schedules/{scheduleId}/excel/info")
    public ResponseEntity<Map<String, Object>> getExcelExportInfo(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> info = new HashMap<>();
            info.put("scheduleId", scheduleId);
            info.put("scheduleName", scheduleOpt.get().getScheduleName());
            info.put("format", "Excel (XLSX)");
            info.put("sheets", List.of(
                "Schedule - Main schedule data",
                "Conflicts - Detected conflicts",
                "Statistics - Schedule statistics and metrics"
            ));
            info.put("features", List.of(
                "Multiple sheets",
                "Formatted tables",
                "Statistics and analysis",
                "Sortable and filterable data"
            ));
            info.put("downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/excel");

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== CSV Export ====================

    @GetMapping("/schedules/{scheduleId}/csv")
    public ResponseEntity<Resource> exportToCSV(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            File csvFile = scheduleExportService.exportToCSV(scheduleOpt.get());
            Resource resource = new FileSystemResource(csvFile);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + csvFile.getName() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/schedules/{scheduleId}/csv/info")
    public ResponseEntity<Map<String, Object>> getCSVExportInfo(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> info = new HashMap<>();
            info.put("scheduleId", scheduleId);
            info.put("scheduleName", scheduleOpt.get().getScheduleName());
            info.put("format", "CSV");
            info.put("features", List.of(
                "Simple comma-separated format",
                "Compatible with Excel and Google Sheets",
                "Easy to import into databases",
                "Plain text format"
            ));
            info.put("useCases", List.of(
                "Data analysis",
                "Import into other systems",
                "Custom reporting",
                "Archival"
            ));
            info.put("downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/csv");

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== iCalendar Export ====================

    @GetMapping("/schedules/{scheduleId}/icalendar")
    public ResponseEntity<Resource> exportToICalendar(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            File icsFile = scheduleExportService.exportToICalendar(scheduleOpt.get());
            Resource resource = new FileSystemResource(icsFile);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + icsFile.getName() + "\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/schedules/{scheduleId}/icalendar/info")
    public ResponseEntity<Map<String, Object>> getICalendarExportInfo(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> info = new HashMap<>();
            info.put("scheduleId", scheduleId);
            info.put("scheduleName", scheduleOpt.get().getScheduleName());
            info.put("format", "iCalendar (ICS)");
            info.put("compatibleWith", List.of(
                "Google Calendar",
                "Microsoft Outlook",
                "Apple Calendar",
                "Most calendar applications"
            ));
            info.put("features", List.of(
                "Import into calendar apps",
                "Recurring event support",
                "Standard iCalendar format (RFC 5545)",
                "Cross-platform compatibility"
            ));
            info.put("useCases", List.of(
                "Personal calendar integration",
                "Share schedule with parents/students",
                "Mobile calendar sync",
                "Cross-platform schedule access"
            ));
            info.put("downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/icalendar");

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Multi-Format Export ====================

    @GetMapping("/schedules/{scheduleId}/formats")
    public ResponseEntity<Map<String, Object>> getAvailableFormats(@PathVariable Long scheduleId) {
        try {
            Optional<Schedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> formats = new HashMap<>();

            formats.put("pdf", Map.of(
                "name", "PDF",
                "description", "Printable format with color coding",
                "downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/pdf",
                "infoUrl", "/api/schedule-export/schedules/" + scheduleId + "/pdf/info",
                "recommended", true,
                "useCases", List.of("Printing", "Distribution", "Archives")
            ));

            formats.put("excel", Map.of(
                "name", "Excel (XLSX)",
                "description", "Multi-sheet workbook with statistics",
                "downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/excel",
                "infoUrl", "/api/schedule-export/schedules/" + scheduleId + "/excel/info",
                "recommended", true,
                "useCases", List.of("Analysis", "Reporting", "Editing")
            ));

            formats.put("csv", Map.of(
                "name", "CSV",
                "description", "Simple data export",
                "downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/csv",
                "infoUrl", "/api/schedule-export/schedules/" + scheduleId + "/csv/info",
                "useCases", List.of("Data analysis", "Import to other systems")
            ));

            formats.put("icalendar", Map.of(
                "name", "iCalendar (ICS)",
                "description", "Calendar application format",
                "downloadUrl", "/api/schedule-export/schedules/" + scheduleId + "/icalendar",
                "infoUrl", "/api/schedule-export/schedules/" + scheduleId + "/icalendar/info",
                "recommended", true,
                "useCases", List.of("Calendar import", "Personal scheduling", "Mobile sync")
            ));

            Map<String, Object> response = new HashMap<>();
            response.put("scheduleId", scheduleId);
            response.put("scheduleName", scheduleOpt.get().getScheduleName());
            response.put("formats", formats);
            response.put("totalFormats", 4);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Export Directory ====================

    @GetMapping("/export-directory")
    public ResponseEntity<Map<String, Object>> getExportDirectory() {
        try {
            File exportDir = scheduleExportService.getExportsDirectory();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("directory", exportDir.getAbsolutePath());
            response.put("exists", exportDir.exists());
            response.put("writable", exportDir.canWrite());

            if (exportDir.exists()) {
                File[] files = exportDir.listFiles();
                response.put("fileCount", files != null ? files.length : 0);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("supportedFormats", Map.of(
            "pdf", "Professional printable format",
            "excel", "Multi-sheet workbook with analysis",
            "csv", "Simple data export",
            "icalendar", "Calendar application integration"
        ));

        dashboard.put("features", Map.of(
            "colorCoding", "PDF exports include subject-based color coding",
            "multipleSheets", "Excel exports include schedule, conflicts, and statistics",
            "calendarIntegration", "iCalendar format works with Google Calendar, Outlook, etc.",
            "dataExport", "CSV format for easy data analysis"
        ));

        dashboard.put("quickActions", Map.of(
            "viewFormats", "GET /api/schedule-export/schedules/{id}/formats",
            "downloadPDF", "GET /api/schedule-export/schedules/{id}/pdf",
            "downloadExcel", "GET /api/schedule-export/schedules/{id}/excel",
            "downloadCalendar", "GET /api/schedule-export/schedules/{id}/icalendar"
        ));

        dashboard.put("exportDirectory", scheduleExportService.getExportsDirectory().getAbsolutePath());

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("supportedFormats", List.of("PDF", "Excel (XLSX)", "CSV", "iCalendar (ICS)"));

        metadata.put("formatDetails", Map.of(
            "PDF", Map.of(
                "mimeType", "application/pdf",
                "extension", ".pdf",
                "features", List.of("Color coding", "Professional layout", "Print-ready")
            ),
            "Excel", Map.of(
                "mimeType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "extension", ".xlsx",
                "features", List.of("Multiple sheets", "Statistics", "Sortable data")
            ),
            "CSV", Map.of(
                "mimeType", "text/csv",
                "extension", ".csv",
                "features", List.of("Simple format", "Excel compatible", "Database import")
            ),
            "iCalendar", Map.of(
                "mimeType", "text/calendar",
                "extension", ".ics",
                "standard", "RFC 5545",
                "features", List.of("Calendar import", "Recurring events", "Cross-platform")
            )
        ));

        metadata.put("useCases", Map.of(
            "printing", "Export to PDF for distribution",
            "analysis", "Export to Excel for reporting and analysis",
            "integration", "Export to iCalendar for calendar apps",
            "archiving", "Export to CSV for long-term storage"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Schedule Export Help");

        help.put("workflows", Map.of(
            "printSchedule", List.of(
                "1. Identify schedule ID",
                "2. GET /api/schedule-export/schedules/{id}/pdf",
                "3. Open PDF file",
                "4. Print from PDF viewer"
            ),
            "importToCalendar", List.of(
                "1. Download iCalendar file: GET /api/schedule-export/schedules/{id}/icalendar",
                "2. Open in Google Calendar, Outlook, or Apple Calendar",
                "3. Import the .ics file",
                "4. Schedule appears in calendar"
            ),
            "excelAnalysis", List.of(
                "1. Download Excel file: GET /api/schedule-export/schedules/{id}/excel",
                "2. Open in Microsoft Excel or Google Sheets",
                "3. Review Schedule sheet for main data",
                "4. Check Conflicts sheet for issues",
                "5. Analyze Statistics sheet for metrics"
            )
        ));

        help.put("endpoints", Map.of(
            "viewFormats", "GET /api/schedule-export/schedules/{id}/formats",
            "downloadPDF", "GET /api/schedule-export/schedules/{id}/pdf",
            "downloadExcel", "GET /api/schedule-export/schedules/{id}/excel",
            "downloadCSV", "GET /api/schedule-export/schedules/{id}/csv",
            "downloadCalendar", "GET /api/schedule-export/schedules/{id}/icalendar"
        ));

        help.put("examples", Map.of(
            "viewFormats", "curl http://localhost:9590/api/schedule-export/schedules/1/formats",
            "downloadPDF", "curl -O http://localhost:9590/api/schedule-export/schedules/1/pdf",
            "dashboard", "curl http://localhost:9590/api/schedule-export/dashboard"
        ));

        help.put("tips", Map.of(
            "pdfPrinting", "Use PDF format for the best print quality",
            "excelEditing", "Excel format allows editing and custom analysis",
            "calendarSharing", "iCalendar format can be shared with students and parents",
            "dataImport", "CSV format works best for importing into other systems"
        ));

        return ResponseEntity.ok(help);
    }
}
