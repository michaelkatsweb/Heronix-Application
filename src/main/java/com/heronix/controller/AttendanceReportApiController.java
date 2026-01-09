package com.heronix.controller;

import com.heronix.model.domain.EmailTemplate;
import com.heronix.model.domain.ReportHistory;
import com.heronix.service.AttendanceReportExportService;
import com.heronix.service.AttendanceReportPdfService;
import com.heronix.service.CsvReportExportService;
import com.heronix.service.EmailService;
import com.heronix.service.ReportCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Attendance Report API Controller
 *
 * Provides REST API endpoints for generating and downloading attendance reports.
 * Supports multiple formats (Excel, PDF) and various report types.
 *
 * Endpoints:
 * - GET /api/reports/attendance/daily/excel - Daily attendance Excel report
 * - GET /api/reports/attendance/daily/pdf - Daily attendance PDF report
 * - GET /api/reports/attendance/summary/excel - Student summary Excel report
 * - GET /api/reports/attendance/summary/pdf - Student summary PDF report
 * - GET /api/reports/attendance/chronic-absenteeism - Chronic absenteeism Excel report
 *
 * Query Parameters:
 * - date: Report date (format: yyyy-MM-dd)
 * - startDate: Period start date (format: yyyy-MM-dd)
 * - endDate: Period end date (format: yyyy-MM-dd)
 * - threshold: Absenteeism threshold percentage (default: 10.0)
 *
 * Response:
 * - Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet (Excel)
 * - Content-Type: application/pdf (PDF)
 * - Content-Disposition: attachment with filename
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 50 - Report API Endpoints
 */
@RestController
@RequestMapping("/api/reports/attendance")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AttendanceReportApiController {

    private final AttendanceReportExportService exportService;
    private final AttendanceReportPdfService pdfService;
    private final CsvReportExportService csvExportService;
    private final ReportCacheService cacheService;
    private final EmailService emailService;

    @Value("${email.report.notification-enabled:false}")
    private Boolean emailNotificationEnabled;

    private static final DateTimeFormatter FILENAME_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate daily attendance report in Excel format
     *
     * GET /api/reports/attendance/daily/excel?date=2025-12-30&email=user@example.com
     *
     * @param date Report date (defaults to yesterday)
     * @param email Optional email address to send report to
     * @return Excel file as byte array
     */
    @GetMapping("/daily/excel")
    public ResponseEntity<byte[]> generateDailyAttendanceExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String email) {

        try {
            LocalDate reportDate = date != null ? date : LocalDate.now().minusDays(1);

            // Generate cache key
            String cacheKey = cacheService.generateCacheKey(
                ReportHistory.ReportType.DAILY_ATTENDANCE,
                ReportHistory.ReportFormat.EXCEL,
                reportDate.toString(),
                reportDate.toString()
            );

            // Check cache first
            byte[] excelData = cacheService.getCachedReport(cacheKey);

            if (excelData == null) {
                log.info("Generating daily attendance Excel report for {}", reportDate);
                excelData = exportService.exportDailyAttendanceExcel(reportDate);

                // Cache the result
                cacheService.cacheReport(cacheKey, excelData, null);
            } else {
                log.info("Returning cached daily attendance Excel report for {}", reportDate);
            }

            // Send email if requested
            if (emailNotificationEnabled && email != null && !email.isBlank()) {
                sendReportEmail(email, excelData, reportDate,
                    ReportHistory.ReportType.DAILY_ATTENDANCE,
                    ReportHistory.ReportFormat.EXCEL,
                    String.format("daily-attendance-%s.xlsx", reportDate.format(FILENAME_DATE_FORMAT)));
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                String.format("daily-attendance-%s.xlsx", reportDate.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating daily attendance Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate daily attendance report in PDF format
     *
     * GET /api/reports/attendance/daily/pdf?date=2025-12-30
     *
     * @param date Report date (defaults to yesterday)
     * @return PDF file as byte array
     */
    @GetMapping("/daily/pdf")
    public ResponseEntity<byte[]> generateDailyAttendancePdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDate reportDate = date != null ? date : LocalDate.now().minusDays(1);
            log.info("Generating daily attendance PDF report for {}", reportDate);

            byte[] pdfData = pdfService.exportDailyAttendancePdf(reportDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                String.format("daily-attendance-%s.pdf", reportDate.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(pdfData.length);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating daily attendance PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate student attendance summary report in Excel format
     *
     * GET /api/reports/attendance/summary/excel?startDate=2025-12-01&endDate=2025-12-30
     *
     * @param startDate Period start date (defaults to first day of current month)
     * @param endDate Period end date (defaults to today)
     * @return Excel file as byte array
     */
    @GetMapping("/summary/excel")
    public ResponseEntity<byte[]> generateAttendanceSummaryExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            log.info("Generating attendance summary Excel report from {} to {}", start, end);

            byte[] excelData = exportService.exportStudentAttendanceSummary(start, end);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                String.format("attendance-summary-%s-to-%s.xlsx",
                    start.format(FILENAME_DATE_FORMAT),
                    end.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating attendance summary Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate student attendance summary report in PDF format
     *
     * GET /api/reports/attendance/summary/pdf?startDate=2025-12-01&endDate=2025-12-30
     *
     * @param startDate Period start date (defaults to first day of current month)
     * @param endDate Period end date (defaults to today)
     * @return PDF file as byte array
     */
    @GetMapping("/summary/pdf")
    public ResponseEntity<byte[]> generateAttendanceSummaryPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            log.info("Generating attendance summary PDF report from {} to {}", start, end);

            byte[] pdfData = pdfService.exportStudentAttendanceSummaryPdf(start, end);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                String.format("attendance-summary-%s-to-%s.pdf",
                    start.format(FILENAME_DATE_FORMAT),
                    end.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(pdfData.length);

            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating attendance summary PDF report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate chronic absenteeism report in Excel format
     *
     * GET /api/reports/attendance/chronic-absenteeism?startDate=2025-12-01&endDate=2025-12-30&threshold=10.0
     *
     * @param startDate Period start date (defaults to first day of current month)
     * @param endDate Period end date (defaults to today)
     * @param threshold Absenteeism threshold percentage (defaults to 10.0)
     * @return Excel file as byte array
     */
    @GetMapping("/chronic-absenteeism")
    public ResponseEntity<byte[]> generateChronicAbsenteeismReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10.0") double threshold) {

        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            log.info("Generating chronic absenteeism Excel report from {} to {} with threshold {}%", start, end, threshold);

            byte[] excelData = exportService.exportChronicAbsenteeismReport(start, end, threshold);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment",
                String.format("chronic-absenteeism-%s-to-%s.xlsx",
                    start.format(FILENAME_DATE_FORMAT),
                    end.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(excelData.length);

            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating chronic absenteeism Excel report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate daily attendance report in CSV format
     *
     * GET /api/reports/attendance/daily/csv?date=2025-12-30
     *
     * @param date Report date (defaults to yesterday)
     * @return CSV file as byte array
     */
    @GetMapping("/daily/csv")
    public ResponseEntity<byte[]> generateDailyAttendanceCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            LocalDate reportDate = date != null ? date : LocalDate.now().minusDays(1);
            log.info("Generating daily attendance CSV report for {}", reportDate);

            byte[] csvData = csvExportService.exportDailyAttendanceCsv(reportDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment",
                String.format("daily-attendance-%s.csv", reportDate.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating daily attendance CSV report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate student attendance summary report in CSV format
     *
     * GET /api/reports/attendance/summary/csv?startDate=2025-12-01&endDate=2025-12-30
     *
     * @param startDate Period start date (defaults to first day of current month)
     * @param endDate Period end date (defaults to today)
     * @return CSV file as byte array
     */
    @GetMapping("/summary/csv")
    public ResponseEntity<byte[]> generateAttendanceSummaryCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            log.info("Generating attendance summary CSV report from {} to {}", start, end);

            byte[] csvData = csvExportService.exportStudentAttendanceSummaryCsv(start, end);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment",
                String.format("attendance-summary-%s-to-%s.csv",
                    start.format(FILENAME_DATE_FORMAT),
                    end.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating attendance summary CSV report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate chronic absenteeism report in CSV format
     *
     * GET /api/reports/attendance/chronic-absenteeism/csv?startDate=2025-12-01&endDate=2025-12-30&threshold=10.0
     *
     * @param startDate Period start date (defaults to first day of current month)
     * @param endDate Period end date (defaults to today)
     * @param threshold Absenteeism threshold percentage (defaults to 10.0)
     * @return CSV file as byte array
     */
    @GetMapping("/chronic-absenteeism/csv")
    public ResponseEntity<byte[]> generateChronicAbsenteeismCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "10.0") double threshold) {

        try {
            LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            log.info("Generating chronic absenteeism CSV report from {} to {} with threshold {}%", start, end, threshold);

            byte[] csvData = csvExportService.exportChronicAbsenteeismCsv(start, end, threshold);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment",
                String.format("chronic-absenteeism-%s-to-%s.csv",
                    start.format(FILENAME_DATE_FORMAT),
                    end.format(FILENAME_DATE_FORMAT)));
            headers.setContentLength(csvData.length);

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error("Error generating chronic absenteeism CSV report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     *
     * GET /api/reports/attendance/health
     *
     * @return OK status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Attendance Report API is running");
    }

    /**
     * Helper method to send report via email
     *
     * @param recipientEmail Recipient email address
     * @param reportData Report data bytes
     * @param reportDate Report date
     * @param reportType Report type
     * @param reportFormat Report format
     * @param fileName File name
     */
    private void sendReportEmail(String recipientEmail, byte[] reportData, LocalDate reportDate,
                                ReportHistory.ReportType reportType, ReportHistory.ReportFormat reportFormat,
                                String fileName) {
        try {
            // Create mock ReportHistory for email template
            ReportHistory mockHistory = ReportHistory.builder()
                .reportType(reportType)
                .reportFormat(reportFormat)
                .generatedAt(LocalDateTime.now())
                .generatedBy("API")
                .fileSize((long) reportData.length)
                .build();

            // Determine email template type based on report type
            EmailTemplate.TemplateType templateType = switch (reportType) {
                case DAILY_ATTENDANCE -> EmailTemplate.TemplateType.DAILY_REPORT_NOTIFICATION;
                case STUDENT_SUMMARY -> EmailTemplate.TemplateType.STUDENT_SUMMARY_NOTIFICATION;
                case CHRONIC_ABSENTEEISM -> EmailTemplate.TemplateType.CHRONIC_ABSENTEEISM_NOTIFICATION;
                default -> EmailTemplate.TemplateType.REPORT_READY_NOTIFICATION;
            };

            // Send email asynchronously
            emailService.sendReportEmail(templateType, recipientEmail, mockHistory, reportData, fileName);

            log.info("Email notification queued for {} (report: {})", recipientEmail, fileName);

        } catch (Exception e) {
            log.error("Failed to queue email notification for {}", recipientEmail, e);
            // Don't fail the request if email fails
        }
    }
}
