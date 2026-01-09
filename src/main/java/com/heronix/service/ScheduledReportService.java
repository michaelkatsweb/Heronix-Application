package com.heronix.service;

import com.heronix.model.domain.ReportHistory.ReportFormat;
import com.heronix.model.domain.ReportHistory.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Scheduled Report Service (Legacy - Hardcoded Schedules)
 *
 * Automatically generates and distributes attendance reports on a schedule.
 * Uses Spring @Scheduled annotation for cron-based execution.
 *
 * NOTE: This service provides hardcoded report schedules for backward compatibility.
 * For dynamic, user-configurable schedules, use DynamicScheduledReportService (Phase 57).
 *
 * Scheduled Reports:
 * - Daily Attendance Report (generated every morning at 8 AM)
 * - Weekly Summary Report (generated every Monday at 7 AM)
 * - Monthly Chronic Absenteeism Report (1st of month at 6 AM)
 * - Quarterly Attendance Trends (1st day of quarter at 5 AM)
 *
 * Features:
 * - Automatic generation
 * - File storage in reports directory
 * - Email distribution to administrators
 * - Error handling and retry logic
 * - Report archival
 *
 * Configuration:
 * - Enable/disable via application properties
 * - Customizable cron expressions
 * - Configurable recipients
 * - Report retention period
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 49 - Report Export & Scheduling
 * @deprecated Use DynamicScheduledReportService for user-configurable schedules (Phase 57)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledReportService {

    private final AttendanceReportExportService exportService;
    private final AttendanceReportPdfService pdfService;
    private final EmailNotificationService emailService;
    private final ReportHistoryService reportHistoryService;

    private static final String REPORTS_DIR = "reports/attendance";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate daily attendance report
     * Runs every day at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailyAttendanceReport() {
        log.info("Starting scheduled daily attendance report generation");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // Generate Excel report
            byte[] excelData = exportService.exportDailyAttendanceExcel(yesterday);
            String excelFileName = String.format("daily-attendance-%s.xlsx",
                yesterday.format(FILE_DATE_FORMAT));
            Path excelPath = saveReport(excelFileName, excelData);

            // Generate PDF report
            byte[] pdfData = pdfService.exportDailyAttendancePdf(yesterday);
            String pdfFileName = String.format("daily-attendance-%s.pdf",
                yesterday.format(FILE_DATE_FORMAT));
            Path pdfPath = saveReport(pdfFileName, pdfData);

            log.info("Daily attendance report generated successfully: Excel={}, PDF={}",
                excelPath, pdfPath);

            // Record report generation in history
            reportHistoryService.recordReportGeneration(
                ReportType.DAILY_ATTENDANCE,
                ReportFormat.EXCEL,
                excelFileName,
                excelPath.toString(),
                (long) excelData.length,
                yesterday,
                yesterday,
                "SYSTEM",
                true
            );

            reportHistoryService.recordReportGeneration(
                ReportType.DAILY_ATTENDANCE,
                ReportFormat.PDF,
                pdfFileName,
                pdfPath.toString(),
                (long) pdfData.length,
                yesterday,
                yesterday,
                "SYSTEM",
                true
            );

            // Send email notification
            emailService.sendDailyAttendanceReport(
                yesterday,
                excelPath.toString(),
                pdfPath.toString()
            );

        } catch (Exception e) {
            log.error("Error generating daily attendance report: {}", e.getMessage(), e);

            // Record failure in history
            reportHistoryService.recordReportFailure(
                ReportType.DAILY_ATTENDANCE,
                ReportFormat.EXCEL,
                e.getMessage(),
                "SYSTEM"
            );

            emailService.sendErrorNotification(
                "Daily Attendance Report Generation Failed",
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Generate weekly attendance summary
     * Runs every Monday at 7:00 AM
     */
    @Scheduled(cron = "0 0 7 ? * MON")
    public void generateWeeklyAttendanceSummary() {
        log.info("Starting scheduled weekly attendance summary generation");

        try {
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(6); // Last 7 days

            // Generate Excel report
            byte[] excelData = exportService.exportStudentAttendanceSummary(startDate, endDate);
            String excelFileName = String.format("weekly-summary-%s-to-%s.xlsx",
                startDate.format(FILE_DATE_FORMAT),
                endDate.format(FILE_DATE_FORMAT));
            Path excelPath = saveReport(excelFileName, excelData);

            // Generate PDF report
            byte[] pdfData = pdfService.exportStudentAttendanceSummaryPdf(startDate, endDate);
            String pdfFileName = String.format("weekly-summary-%s-to-%s.pdf",
                startDate.format(FILE_DATE_FORMAT),
                endDate.format(FILE_DATE_FORMAT));
            Path pdfPath = saveReport(pdfFileName, pdfData);

            log.info("Weekly attendance summary generated successfully: Excel={}, PDF={}",
                excelPath, pdfPath);

            // Record report generation in history
            reportHistoryService.recordReportGeneration(
                ReportType.WEEKLY_SUMMARY,
                ReportFormat.EXCEL,
                excelFileName,
                excelPath.toString(),
                (long) excelData.length,
                startDate,
                endDate,
                "SYSTEM",
                true
            );

            reportHistoryService.recordReportGeneration(
                ReportType.WEEKLY_SUMMARY,
                ReportFormat.PDF,
                pdfFileName,
                pdfPath.toString(),
                (long) pdfData.length,
                startDate,
                endDate,
                "SYSTEM",
                true
            );

            // Send email notification
            emailService.sendWeeklyAttendanceSummary(
                startDate,
                endDate,
                excelPath.toString(),
                pdfPath.toString()
            );

        } catch (Exception e) {
            log.error("Error generating weekly attendance summary: {}", e.getMessage(), e);

            // Record failure in history
            reportHistoryService.recordReportFailure(
                ReportType.WEEKLY_SUMMARY,
                ReportFormat.EXCEL,
                e.getMessage(),
                "SYSTEM"
            );

            emailService.sendErrorNotification(
                "Weekly Attendance Summary Generation Failed",
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Generate monthly chronic absenteeism report
     * Runs on the 1st of every month at 6:00 AM
     */
    @Scheduled(cron = "0 0 6 1 * ?")
    public void generateMonthlyChronicAbsenteeismReport() {
        log.info("Starting scheduled monthly chronic absenteeism report generation");

        try {
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(1); // Previous month

            // Generate Excel report with 10% threshold
            byte[] excelData = exportService.exportChronicAbsenteeismReport(startDate, endDate, 10.0);
            String excelFileName = String.format("chronic-absenteeism-%s.xlsx",
                startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            Path excelPath = saveReport(excelFileName, excelData);

            log.info("Monthly chronic absenteeism report generated successfully: {}", excelPath);

            // Record report generation in history
            reportHistoryService.recordReportGeneration(
                ReportType.CHRONIC_ABSENTEEISM,
                ReportFormat.EXCEL,
                excelFileName,
                excelPath.toString(),
                (long) excelData.length,
                startDate,
                endDate,
                "SYSTEM",
                true
            );

            // Send email notification
            emailService.sendChronicAbsenteeismReport(
                startDate,
                endDate,
                excelPath.toString()
            );

        } catch (Exception e) {
            log.error("Error generating monthly chronic absenteeism report: {}", e.getMessage(), e);

            // Record failure in history
            reportHistoryService.recordReportFailure(
                ReportType.CHRONIC_ABSENTEEISM,
                ReportFormat.EXCEL,
                e.getMessage(),
                "SYSTEM"
            );

            emailService.sendErrorNotification(
                "Monthly Chronic Absenteeism Report Generation Failed",
                "Error: " + e.getMessage()
            );
        }
    }

    /**
     * Save report to file system
     *
     * @param fileName File name
     * @param data Report data
     * @return Path to saved file
     */
    private Path saveReport(String fileName, byte[] data) throws IOException {
        // Create reports directory if it doesn't exist
        Path reportsDir = Paths.get(REPORTS_DIR);
        if (!Files.exists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }

        // Save file
        Path filePath = reportsDir.resolve(fileName);
        Files.write(filePath, data);

        log.debug("Report saved to: {}", filePath.toAbsolutePath());
        return filePath;
    }

    /**
     * Archive old reports
     * Runs on the 1st of every month at 2:00 AM
     * Deletes reports older than 90 days
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void archiveOldReports() {
        log.info("Starting scheduled report archival");

        try {
            Path reportsDir = Paths.get(REPORTS_DIR);
            if (!Files.exists(reportsDir)) {
                log.debug("Reports directory does not exist, skipping archival");
                return;
            }

            LocalDate cutoffDate = LocalDate.now().minusDays(90);

            Files.list(reportsDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path)
                            .toInstant()
                            .isBefore(cutoffDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC));
                    } catch (IOException e) {
                        log.error("Error checking file modification time: {}", e.getMessage());
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted old report: {}", path.getFileName());
                    } catch (IOException e) {
                        log.error("Error deleting old report {}: {}", path.getFileName(), e.getMessage());
                    }
                });

            log.info("Report archival completed successfully");

        } catch (Exception e) {
            log.error("Error archiving old reports: {}", e.getMessage(), e);
        }
    }
}
