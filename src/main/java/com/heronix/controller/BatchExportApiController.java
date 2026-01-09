package com.heronix.controller;

import com.heronix.model.domain.ReportHistory;
import com.heronix.service.BatchReportExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Batch Export API Controller
 *
 * REST API endpoints for batch report export operations.
 *
 * Provides endpoints for:
 * - Batch daily report exports
 * - Grade-level batch exports
 * - Custom batch export configurations
 * - Bulk archive generation
 *
 * All batch exports are returned as ZIP files containing multiple reports
 * along with manifest and summary files.
 *
 * Endpoints:
 * - POST /api/reports/batch/daily - Export daily reports for date range
 * - POST /api/reports/batch/grades - Export all grade-level reports
 * - POST /api/reports/batch/custom - Export custom report combination
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 61 - Enhanced Report Export Capabilities
 */
@RestController
@RequestMapping("/api/reports/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchExportApiController {

    private final BatchReportExportService batchExportService;

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Export daily reports in batch for a date range
     *
     * Generates daily attendance reports for each day in the specified range
     * and returns them in a single ZIP archive.
     *
     * @param startDate Range start date (required)
     * @param endDate Range end date (required)
     * @param format Report format (default: EXCEL)
     * @param generatedBy User generating the export (optional)
     * @return ZIP file containing all daily reports
     */
    @PostMapping("/daily")
    public ResponseEntity<byte[]> exportDailyBatch(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "EXCEL")
            ReportHistory.ReportFormat format,

            @RequestParam(required = false, defaultValue = "System")
            String generatedBy) {

        log.info("POST /api/reports/batch/daily - startDate: {}, endDate: {}, format: {}",
                startDate, endDate, format);

        try {
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }

            // Generate batch export
            byte[] zipData = batchExportService.exportDailyReportBatch(
                    startDate, endDate, format, generatedBy);

            // Prepare response headers
            String filename = String.format("daily-reports-batch-%s-to-%s.zip",
                    startDate.format(FILENAME_FORMATTER),
                    endDate.format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipData.length);

            log.info("Daily batch export completed: {} bytes, {} to {}",
                    zipData.length, startDate, endDate);

            return new ResponseEntity<>(zipData, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Invalid batch export request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (IllegalStateException e) {
            log.error("Batch export not available: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();

        } catch (IOException e) {
            log.error("Error generating batch export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            log.error("Unexpected error in batch export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export grade-level reports in batch
     *
     * Generates attendance reports for all grade levels and returns them
     * in a single ZIP archive.
     *
     * @param startDate Period start date (required)
     * @param endDate Period end date (required)
     * @param format Report format (default: EXCEL)
     * @param generatedBy User generating the export (optional)
     * @return ZIP file containing all grade-level reports
     */
    @PostMapping("/grades")
    public ResponseEntity<byte[]> exportGradeLevelBatch(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(defaultValue = "EXCEL")
            ReportHistory.ReportFormat format,

            @RequestParam(required = false, defaultValue = "System")
            String generatedBy) {

        log.info("POST /api/reports/batch/grades - startDate: {}, endDate: {}, format: {}",
                startDate, endDate, format);

        try {
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }

            // Generate batch export
            byte[] zipData = batchExportService.exportGradeLevelBatch(
                    startDate, endDate, format, generatedBy);

            // Prepare response headers
            String filename = String.format("grade-level-reports-%s-to-%s.zip",
                    startDate.format(FILENAME_FORMATTER),
                    endDate.format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipData.length);

            log.info("Grade-level batch export completed: {} bytes", zipData.length);

            return new ResponseEntity<>(zipData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating grade-level batch export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            log.error("Unexpected error in grade-level batch export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export custom report combination in batch
     *
     * Generates a custom combination of reports based on provided specifications
     * and returns them in a single ZIP archive.
     *
     * @param reportSpecs List of report specifications
     * @param generatedBy User generating the export (optional)
     * @return ZIP file containing all specified reports
     */
    @PostMapping("/custom")
    public ResponseEntity<byte[]> exportCustomBatch(
            @RequestBody List<BatchReportExportService.ReportSpecification> reportSpecs,

            @RequestParam(required = false, defaultValue = "System")
            String generatedBy) {

        log.info("POST /api/reports/batch/custom - {} reports requested", reportSpecs.size());

        try {
            // Validate request
            if (reportSpecs == null || reportSpecs.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Generate batch export
            byte[] zipData = batchExportService.exportCustomBatch(reportSpecs, generatedBy);

            // Prepare response headers
            String filename = String.format("custom-reports-batch-%s.zip",
                    LocalDate.now().format(FILENAME_FORMATTER));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/zip"));
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(zipData.length);

            log.info("Custom batch export completed: {} bytes, {} reports",
                    zipData.length, reportSpecs.size());

            return new ResponseEntity<>(zipData, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Invalid custom batch export request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (IOException e) {
            log.error("Error generating custom batch export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (Exception e) {
            log.error("Unexpected error in custom batch export", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export weekly batch (convenience endpoint)
     *
     * Generates daily reports for the past week.
     *
     * @param format Report format (default: EXCEL)
     * @param generatedBy User generating the export (optional)
     * @return ZIP file containing weekly reports
     */
    @PostMapping("/weekly")
    public ResponseEntity<byte[]> exportWeeklyBatch(
            @RequestParam(defaultValue = "EXCEL")
            ReportHistory.ReportFormat format,

            @RequestParam(required = false, defaultValue = "System")
            String generatedBy) {

        log.info("POST /api/reports/batch/weekly - format: {}", format);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);

        return exportDailyBatch(startDate, endDate, format, generatedBy);
    }

    /**
     * Export monthly batch (convenience endpoint)
     *
     * Generates daily reports for the past month.
     *
     * @param format Report format (default: EXCEL)
     * @param generatedBy User generating the export (optional)
     * @return ZIP file containing monthly reports
     */
    @PostMapping("/monthly")
    public ResponseEntity<byte[]> exportMonthlyBatch(
            @RequestParam(defaultValue = "EXCEL")
            ReportHistory.ReportFormat format,

            @RequestParam(required = false, defaultValue = "System")
            String generatedBy) {

        log.info("POST /api/reports/batch/monthly - format: {}", format);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        return exportDailyBatch(startDate, endDate, format, generatedBy);
    }

    /**
     * Get batch export information
     *
     * Returns information about batch export capabilities and limits.
     *
     * @return Batch export information
     */
    @GetMapping("/info")
    public ResponseEntity<BatchExportInfo> getBatchExportInfo() {
        log.info("GET /api/reports/batch/info");

        BatchExportInfo info = new BatchExportInfo();
        info.setMaxBatchSize(50);
        info.setEnabled(true);
        info.setSupportedFormats(List.of("EXCEL", "PDF", "CSV"));
        info.setFeatures(List.of(
                "Daily report batches",
                "Grade-level batches",
                "Custom report combinations",
                "ZIP compression",
                "Manifest generation"
        ));

        return ResponseEntity.ok(info);
    }

    /**
     * Batch Export Information DTO
     */
    public static class BatchExportInfo {
        private Integer maxBatchSize;
        private Boolean enabled;
        private List<String> supportedFormats;
        private List<String> features;

        public Integer getMaxBatchSize() { return maxBatchSize; }
        public void setMaxBatchSize(Integer maxBatchSize) { this.maxBatchSize = maxBatchSize; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public List<String> getSupportedFormats() { return supportedFormats; }
        public void setSupportedFormats(List<String> supportedFormats) {
            this.supportedFormats = supportedFormats;
        }

        public List<String> getFeatures() { return features; }
        public void setFeatures(List<String> features) { this.features = features; }
    }
}
