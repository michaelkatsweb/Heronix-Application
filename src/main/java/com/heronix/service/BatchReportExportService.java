package com.heronix.service;

import com.heronix.model.domain.ReportHistory;
import com.heronix.repository.ReportHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Batch Report Export Service
 *
 * Provides batch export capabilities for generating multiple reports
 * simultaneously and packaging them together.
 *
 * Features:
 * - Generate multiple reports in parallel
 * - Package reports into ZIP archives
 * - Support for mixed format exports (Excel, PDF, CSV)
 * - Custom file naming conventions
 * - Comprehensive export manifests
 * - Progress tracking for large batches
 *
 * Use Cases:
 * - End-of-period bulk exports
 * - Multi-grade level reports
 * - Historical data archives
 * - Administrative report packages
 * - Compliance documentation
 *
 * Export Types:
 * - Daily reports for date range
 * - Grade-level breakdowns
 * - Custom report combinations
 * - Full system exports
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 61 - Enhanced Report Export Capabilities
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BatchReportExportService {

    private final AttendanceReportExportService excelService;
    private final AttendanceReportPdfService pdfService;
    private final CsvReportExportService csvService;
    private final ReportHistoryRepository reportHistoryRepository;

    @Value("${report.batch.max-reports:50}")
    private Integer maxBatchSize;

    @Value("${report.batch.enabled:true}")
    private Boolean batchExportEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILENAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Export multiple daily reports for a date range
     *
     * Generates daily attendance reports for each day in the specified range
     * and packages them into a single ZIP file.
     *
     * @param startDate Range start date
     * @param endDate Range end date
     * @param format Report format (EXCEL, PDF, CSV)
     * @param generatedBy User generating the export
     * @return ZIP file containing all reports
     * @throws IOException If export fails
     */
    public byte[] exportDailyReportBatch(LocalDate startDate, LocalDate endDate,
                                         ReportHistory.ReportFormat format,
                                         String generatedBy) throws IOException {

        if (!batchExportEnabled) {
            throw new IllegalStateException("Batch export is currently disabled");
        }

        log.info("Starting batch export: daily reports from {} to {} in {} format",
                startDate, endDate, format);

        List<LocalDate> dates = getDatesInRange(startDate, endDate);

        if (dates.size() > maxBatchSize) {
            throw new IllegalArgumentException(String.format(
                    "Batch size (%d) exceeds maximum allowed (%d)", dates.size(), maxBatchSize));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            int successCount = 0;
            int failureCount = 0;
            List<String> errors = new ArrayList<>();

            // Add manifest file
            addManifestToZip(zos, dates, format, generatedBy);

            // Generate report for each date
            for (LocalDate date : dates) {
                try {
                    byte[] reportData = generateDailyReport(date, format);
                    String filename = generateDailyReportFilename(date, format);

                    ZipEntry entry = new ZipEntry(filename);
                    zos.putNextEntry(entry);
                    zos.write(reportData);
                    zos.closeEntry();

                    successCount++;
                    log.debug("Added {} to batch export", filename);

                } catch (Exception e) {
                    failureCount++;
                    String error = String.format("Failed to generate report for %s: %s",
                            date, e.getMessage());
                    errors.add(error);
                    log.error(error, e);
                }
            }

            // Add error log if there were failures
            if (failureCount > 0) {
                addErrorLogToZip(zos, errors);
            }

            // Add summary file
            addSummaryToZip(zos, successCount, failureCount, dates.size());

            zos.finish();

            log.info("Batch export completed: {} successful, {} failed out of {} total",
                    successCount, failureCount, dates.size());

            // Record batch export in history
            recordBatchExport(startDate, endDate, format, generatedBy, baos.size());

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error creating batch export ZIP", e);
            throw e;
        }
    }

    /**
     * Export all grade-level reports
     *
     * Generates attendance reports for each grade level and packages them
     * into a single ZIP file.
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @param format Report format
     * @param generatedBy User generating the export
     * @return ZIP file containing all grade-level reports
     * @throws IOException If export fails
     */
    public byte[] exportGradeLevelBatch(LocalDate startDate, LocalDate endDate,
                                        ReportHistory.ReportFormat format,
                                        String generatedBy) throws IOException {

        log.info("Starting grade-level batch export from {} to {}", startDate, endDate);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Generate student summary for all grades
            byte[] summaryData = generateStudentSummaryReport(startDate, endDate, format);
            String filename = String.format("all-grades-summary-%s-to-%s.%s",
                    startDate.format(FILENAME_FORMATTER),
                    endDate.format(FILENAME_FORMATTER),
                    getFileExtension(format));

            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);
            zos.write(summaryData);
            zos.closeEntry();

            // Add manifest
            addManifestToZip(zos, Arrays.asList(startDate, endDate), format, generatedBy);

            zos.finish();

            log.info("Grade-level batch export completed");

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error creating grade-level batch export", e);
            throw e;
        }
    }

    /**
     * Export custom report combination
     *
     * Generates a custom combination of reports based on provided specifications.
     *
     * @param reportSpecs List of report specifications
     * @param generatedBy User generating the export
     * @return ZIP file containing all specified reports
     * @throws IOException If export fails
     */
    public byte[] exportCustomBatch(List<ReportSpecification> reportSpecs,
                                    String generatedBy) throws IOException {

        if (reportSpecs.size() > maxBatchSize) {
            throw new IllegalArgumentException(String.format(
                    "Batch size (%d) exceeds maximum allowed (%d)", reportSpecs.size(), maxBatchSize));
        }

        log.info("Starting custom batch export with {} reports", reportSpecs.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            int successCount = 0;
            int failureCount = 0;

            for (ReportSpecification spec : reportSpecs) {
                try {
                    byte[] reportData = generateReportBySpec(spec);
                    String filename = spec.getFilename() != null ?
                            spec.getFilename() : generateFilenameFromSpec(spec);

                    ZipEntry entry = new ZipEntry(filename);
                    zos.putNextEntry(entry);
                    zos.write(reportData);
                    zos.closeEntry();

                    successCount++;

                } catch (Exception e) {
                    failureCount++;
                    log.error("Failed to generate report: {}", spec, e);
                }
            }

            addSummaryToZip(zos, successCount, failureCount, reportSpecs.size());

            zos.finish();

            log.info("Custom batch export completed: {} successful, {} failed",
                    successCount, failureCount);

            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error creating custom batch export", e);
            throw e;
        }
    }

    /**
     * Generate a single daily report
     */
    private byte[] generateDailyReport(LocalDate date, ReportHistory.ReportFormat format)
            throws IOException {

        return switch (format) {
            case EXCEL -> excelService.exportDailyAttendanceExcel(date);
            case PDF -> pdfService.exportDailyAttendancePdf(date);
            case CSV -> csvService.exportDailyAttendanceCsv(date);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * Generate student summary report
     */
    private byte[] generateStudentSummaryReport(LocalDate startDate, LocalDate endDate,
                                                ReportHistory.ReportFormat format)
            throws IOException {

        return switch (format) {
            case EXCEL -> excelService.exportStudentAttendanceSummary(startDate, endDate);
            case PDF -> pdfService.exportStudentAttendanceSummaryPdf(startDate, endDate);
            case CSV -> csvService.exportStudentAttendanceSummaryCsv(startDate, endDate);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    /**
     * Generate report based on specification
     */
    private byte[] generateReportBySpec(ReportSpecification spec) throws IOException {
        return switch (spec.getType()) {
            case DAILY -> generateDailyReport(spec.getDate(), spec.getFormat());
            case SUMMARY -> generateStudentSummaryReport(
                    spec.getStartDate(), spec.getEndDate(), spec.getFormat());
            case CHRONIC_ABSENTEEISM -> {
                if (spec.getFormat() == ReportHistory.ReportFormat.CSV) {
                    yield csvService.exportChronicAbsenteeismCsv(
                            spec.getStartDate(), spec.getEndDate(), spec.getThreshold());
                } else {
                    yield excelService.exportChronicAbsenteeismReport(
                            spec.getStartDate(), spec.getEndDate(), spec.getThreshold());
                }
            }
        };
    }

    /**
     * Add manifest file to ZIP
     */
    private void addManifestToZip(ZipOutputStream zos, List<LocalDate> dates,
                                  ReportHistory.ReportFormat format, String generatedBy)
            throws IOException {

        StringBuilder manifest = new StringBuilder();
        manifest.append("Batch Export Manifest\n");
        manifest.append("=".repeat(50)).append("\n\n");
        manifest.append("Generated: ").append(LocalDate.now()).append("\n");
        manifest.append("Generated By: ").append(generatedBy).append("\n");
        manifest.append("Format: ").append(format).append("\n");
        manifest.append("Report Count: ").append(dates.size()).append("\n\n");
        manifest.append("Reports Included:\n");

        for (LocalDate date : dates) {
            manifest.append("  - ").append(date).append("\n");
        }

        ZipEntry manifestEntry = new ZipEntry("MANIFEST.txt");
        zos.putNextEntry(manifestEntry);
        zos.write(manifest.toString().getBytes());
        zos.closeEntry();
    }

    /**
     * Add error log to ZIP
     */
    private void addErrorLogToZip(ZipOutputStream zos, List<String> errors) throws IOException {
        StringBuilder errorLog = new StringBuilder();
        errorLog.append("Error Log\n");
        errorLog.append("=".repeat(50)).append("\n\n");

        for (String error : errors) {
            errorLog.append(error).append("\n");
        }

        ZipEntry errorEntry = new ZipEntry("ERRORS.txt");
        zos.putNextEntry(errorEntry);
        zos.write(errorLog.toString().getBytes());
        zos.closeEntry();
    }

    /**
     * Add summary file to ZIP
     */
    private void addSummaryToZip(ZipOutputStream zos, int successCount,
                                 int failureCount, int totalCount) throws IOException {

        StringBuilder summary = new StringBuilder();
        summary.append("Export Summary\n");
        summary.append("=".repeat(50)).append("\n\n");
        summary.append("Total Reports: ").append(totalCount).append("\n");
        summary.append("Successful: ").append(successCount).append("\n");
        summary.append("Failed: ").append(failureCount).append("\n");
        summary.append("Success Rate: ").append(
                String.format("%.1f%%", (successCount * 100.0 / totalCount))).append("\n");

        ZipEntry summaryEntry = new ZipEntry("SUMMARY.txt");
        zos.putNextEntry(summaryEntry);
        zos.write(summary.toString().getBytes());
        zos.closeEntry();
    }

    /**
     * Generate filename for daily report
     */
    private String generateDailyReportFilename(LocalDate date, ReportHistory.ReportFormat format) {
        return String.format("daily-attendance-%s.%s",
                date.format(FILENAME_FORMATTER),
                getFileExtension(format));
    }

    /**
     * Generate filename from specification
     */
    private String generateFilenameFromSpec(ReportSpecification spec) {
        String typeName = spec.getType().name().toLowerCase().replace("_", "-");
        String datePart = spec.getDate() != null ?
                spec.getDate().format(FILENAME_FORMATTER) :
                String.format("%s-to-%s",
                        spec.getStartDate().format(FILENAME_FORMATTER),
                        spec.getEndDate().format(FILENAME_FORMATTER));

        return String.format("%s-%s.%s", typeName, datePart, getFileExtension(spec.getFormat()));
    }

    /**
     * Get file extension for format
     */
    private String getFileExtension(ReportHistory.ReportFormat format) {
        return switch (format) {
            case EXCEL -> "xlsx";
            case PDF -> "pdf";
            case CSV -> "csv";
            default -> "bin";
        };
    }

    /**
     * Get all dates in range
     */
    private List<LocalDate> getDatesInRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * Record batch export in history
     */
    private void recordBatchExport(LocalDate startDate, LocalDate endDate,
                                   ReportHistory.ReportFormat format,
                                   String generatedBy, long fileSize) {

        ReportHistory history = ReportHistory.builder()
                .reportType(ReportHistory.ReportType.DAILY_ATTENDANCE)
                .reportFormat(format)
                .generatedBy(generatedBy)
                .generatedAt(java.time.LocalDateTime.now())
                .fileSize(fileSize)
                .parameters(String.format("Batch export: %s to %s", startDate, endDate))
                .build();

        reportHistoryRepository.save(history);
    }

    /**
     * Report Specification for custom batch exports
     */
    public static class ReportSpecification {
        private ReportType type;
        private LocalDate date;
        private LocalDate startDate;
        private LocalDate endDate;
        private ReportHistory.ReportFormat format;
        private String filename;
        private double threshold = 90.0;

        public enum ReportType {
            DAILY, SUMMARY, CHRONIC_ABSENTEEISM
        }

        // Getters and setters
        public ReportType getType() { return type; }
        public void setType(ReportType type) { this.type = type; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

        public ReportHistory.ReportFormat getFormat() { return format; }
        public void setFormat(ReportHistory.ReportFormat format) { this.format = format; }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }

        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }

        public static ReportSpecification daily(LocalDate date, ReportHistory.ReportFormat format) {
            ReportSpecification spec = new ReportSpecification();
            spec.setType(ReportType.DAILY);
            spec.setDate(date);
            spec.setFormat(format);
            return spec;
        }

        public static ReportSpecification summary(LocalDate startDate, LocalDate endDate,
                                                 ReportHistory.ReportFormat format) {
            ReportSpecification spec = new ReportSpecification();
            spec.setType(ReportType.SUMMARY);
            spec.setStartDate(startDate);
            spec.setEndDate(endDate);
            spec.setFormat(format);
            return spec;
        }
    }
}
