package com.heronix.service;

import com.heronix.model.domain.ReportHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.Future;

/**
 * Async Report Generation Service
 *
 * Provides asynchronous report generation capabilities to improve
 * responsiveness and handle concurrent report requests.
 *
 * Features:
 * - Non-blocking report generation
 * - Concurrent report processing
 * - Progress tracking via Future<T>
 * - Error handling with status updates
 * - Integration with caching layer
 *
 * Benefits:
 * - API endpoints return immediately
 * - Multiple reports can generate simultaneously
 * - Better resource utilization
 * - Improved user experience
 *
 * Usage:
 * <code>
 * Future<byte[]> future = asyncService.generateDailyAttendanceExcelAsync(date);
 * byte[] result = future.get(); // Blocks until complete
 * </code>
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 55 - Performance Optimization & Caching
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncReportGenerationService {

    private final AttendanceReportExportService exportService;
    private final AttendanceReportPdfService pdfService;
    private final ReportHistoryService reportHistoryService;
    private final ReportCacheService cacheService;

    /**
     * Generate daily attendance Excel report asynchronously
     *
     * @param date Report date
     * @param cacheKey Optional cache key
     * @return Future containing report data
     */
    @Async("reportExecutor")
    public Future<byte[]> generateDailyAttendanceExcelAsync(LocalDate date, String cacheKey) {
        log.info("Starting async generation of daily attendance Excel for {}", date);

        try {
            // Check cache first
            if (cacheKey != null) {
                byte[] cached = cacheService.getCachedReport(cacheKey);
                if (cached != null) {
                    log.info("Returning cached report for {}", cacheKey);
                    return new AsyncResult<>(cached);
                }
            }

            // Generate report
            byte[] reportData = exportService.exportDailyAttendanceExcel(date);

            // Cache result
            if (cacheKey != null) {
                cacheService.cacheReport(cacheKey, reportData, null);
            }

            log.info("Completed async generation of daily attendance Excel for {} ({} bytes)",
                date, reportData.length);

            return new AsyncResult<>(reportData);

        } catch (IOException e) {
            log.error("Error generating daily attendance Excel for {}", date, e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Generate daily attendance PDF report asynchronously
     *
     * @param date Report date
     * @param cacheKey Optional cache key
     * @return Future containing report data
     */
    @Async("reportExecutor")
    public Future<byte[]> generateDailyAttendancePdfAsync(LocalDate date, String cacheKey) {
        log.info("Starting async generation of daily attendance PDF for {}", date);

        try {
            // Check cache first
            if (cacheKey != null) {
                byte[] cached = cacheService.getCachedReport(cacheKey);
                if (cached != null) {
                    log.info("Returning cached report for {}", cacheKey);
                    return new AsyncResult<>(cached);
                }
            }

            // Generate report
            byte[] reportData = pdfService.exportDailyAttendancePdf(date);

            // Cache result
            if (cacheKey != null) {
                cacheService.cacheReport(cacheKey, reportData, null);
            }

            log.info("Completed async generation of daily attendance PDF for {} ({} bytes)",
                date, reportData.length);

            return new AsyncResult<>(reportData);

        } catch (IOException e) {
            log.error("Error generating daily attendance PDF for {}", date, e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Generate student attendance summary Excel asynchronously
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @param cacheKey Optional cache key
     * @return Future containing report data
     */
    @Async("reportExecutor")
    public Future<byte[]> generateStudentSummaryExcelAsync(LocalDate startDate, LocalDate endDate, String cacheKey) {
        log.info("Starting async generation of student summary Excel for {} to {}", startDate, endDate);

        try {
            // Check cache first
            if (cacheKey != null) {
                byte[] cached = cacheService.getCachedReport(cacheKey);
                if (cached != null) {
                    log.info("Returning cached report for {}", cacheKey);
                    return new AsyncResult<>(cached);
                }
            }

            // Generate report
            byte[] reportData = exportService.exportStudentAttendanceSummary(startDate, endDate);

            // Cache result
            if (cacheKey != null) {
                cacheService.cacheReport(cacheKey, reportData, null);
            }

            log.info("Completed async generation of student summary Excel for {} to {} ({} bytes)",
                startDate, endDate, reportData.length);

            return new AsyncResult<>(reportData);

        } catch (IOException e) {
            log.error("Error generating student summary Excel for {} to {}", startDate, endDate, e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Generate student attendance summary PDF asynchronously
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @param cacheKey Optional cache key
     * @return Future containing report data
     */
    @Async("reportExecutor")
    public Future<byte[]> generateStudentSummaryPdfAsync(LocalDate startDate, LocalDate endDate, String cacheKey) {
        log.info("Starting async generation of student summary PDF for {} to {}", startDate, endDate);

        try {
            // Check cache first
            if (cacheKey != null) {
                byte[] cached = cacheService.getCachedReport(cacheKey);
                if (cached != null) {
                    log.info("Returning cached report for {}", cacheKey);
                    return new AsyncResult<>(cached);
                }
            }

            // Generate report
            byte[] reportData = pdfService.exportStudentAttendanceSummaryPdf(startDate, endDate);

            // Cache result
            if (cacheKey != null) {
                cacheService.cacheReport(cacheKey, reportData, null);
            }

            log.info("Completed async generation of student summary PDF for {} to {} ({} bytes)",
                startDate, endDate, reportData.length);

            return new AsyncResult<>(reportData);

        } catch (IOException e) {
            log.error("Error generating student summary PDF for {} to {}", startDate, endDate, e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Generate chronic absenteeism report asynchronously
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @param threshold Absenteeism threshold percentage
     * @param cacheKey Optional cache key
     * @return Future containing report data
     */
    @Async("reportExecutor")
    public Future<byte[]> generateChronicAbsenteeismReportAsync(LocalDate startDate, LocalDate endDate,
                                                                 double threshold, String cacheKey) {
        log.info("Starting async generation of chronic absenteeism report for {} to {} (threshold: {}%)",
            startDate, endDate, threshold);

        try {
            // Check cache first
            if (cacheKey != null) {
                byte[] cached = cacheService.getCachedReport(cacheKey);
                if (cached != null) {
                    log.info("Returning cached report for {}", cacheKey);
                    return new AsyncResult<>(cached);
                }
            }

            // Generate report
            byte[] reportData = exportService.exportChronicAbsenteeismReport(startDate, endDate, threshold);

            // Cache result
            if (cacheKey != null) {
                cacheService.cacheReport(cacheKey, reportData, null);
            }

            log.info("Completed async generation of chronic absenteeism report for {} to {} ({} bytes)",
                startDate, endDate, reportData.length);

            return new AsyncResult<>(reportData);

        } catch (IOException e) {
            log.error("Error generating chronic absenteeism report for {} to {}", startDate, endDate, e);
            throw new RuntimeException("Failed to generate report", e);
        }
    }
}
