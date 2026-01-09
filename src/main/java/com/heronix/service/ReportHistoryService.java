package com.heronix.service;

import com.heronix.model.domain.ReportHistory;
import com.heronix.model.domain.ReportHistory.ReportFormat;
import com.heronix.model.domain.ReportHistory.ReportStatus;
import com.heronix.model.domain.ReportHistory.ReportType;
import com.heronix.repository.ReportHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Report History Service
 *
 * Manages report generation history and metadata.
 * Tracks report creation, downloads, and cleanup.
 *
 * Features:
 * - Track report generation history
 * - Record report metadata (type, format, size, parameters)
 * - Track download counts and access times
 * - Automatic cleanup of old reports
 * - Report statistics and analytics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 50 - Report API Endpoints
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportHistoryService {

    private final ReportHistoryRepository reportHistoryRepository;

    /**
     * Record a new report generation
     *
     * @param reportType Type of report
     * @param reportFormat Format of report
     * @param reportName Report filename
     * @param filePath Path where report is stored
     * @param fileSize Size of report file in bytes
     * @param startDate Report period start date
     * @param endDate Report period end date
     * @param generatedBy User who generated the report
     * @param scheduled Whether this was a scheduled report
     * @return Created ReportHistory entity
     */
    @Transactional
    public ReportHistory recordReportGeneration(
            ReportType reportType,
            ReportFormat reportFormat,
            String reportName,
            String filePath,
            Long fileSize,
            LocalDate startDate,
            LocalDate endDate,
            String generatedBy,
            boolean scheduled) {

        ReportHistory history = ReportHistory.builder()
            .reportType(reportType)
            .reportFormat(reportFormat)
            .reportName(reportName)
            .filePath(filePath)
            .fileSize(fileSize)
            .startDate(startDate)
            .endDate(endDate)
            .generatedBy(generatedBy)
            .generatedAt(LocalDateTime.now())
            .status(ReportStatus.COMPLETED)
            .scheduled(scheduled)
            .emailed(false)
            .downloadCount(0)
            .build();

        ReportHistory saved = reportHistoryRepository.save(history);
        log.info("Recorded report generation: {} - {} (ID: {})", reportType, reportName, saved.getId());
        return saved;
    }

    /**
     * Record report generation failure
     *
     * @param reportType Type of report
     * @param reportFormat Format of report
     * @param errorMessage Error message
     * @param generatedBy User who attempted to generate the report
     * @return Created ReportHistory entity
     */
    @Transactional
    public ReportHistory recordReportFailure(
            ReportType reportType,
            ReportFormat reportFormat,
            String errorMessage,
            String generatedBy) {

        ReportHistory history = ReportHistory.builder()
            .reportType(reportType)
            .reportFormat(reportFormat)
            .reportName("Failed Report")
            .generatedBy(generatedBy)
            .generatedAt(LocalDateTime.now())
            .status(ReportStatus.FAILED)
            .errorMessage(errorMessage)
            .scheduled(false)
            .build();

        ReportHistory saved = reportHistoryRepository.save(history);
        log.warn("Recorded report generation failure: {} - {}", reportType, errorMessage);
        return saved;
    }

    /**
     * Record a report download/access
     *
     * @param reportId Report history ID
     */
    @Transactional
    public void recordReportAccess(Long reportId) {
        Optional<ReportHistory> reportOpt = reportHistoryRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            ReportHistory report = reportOpt.get();
            report.setDownloadCount(report.getDownloadCount() + 1);
            report.setLastAccessed(LocalDateTime.now());
            reportHistoryRepository.save(report);
            log.debug("Recorded access for report ID: {} (total downloads: {})", reportId, report.getDownloadCount());
        }
    }

    /**
     * Mark report as emailed
     *
     * @param reportId Report history ID
     */
    @Transactional
    public void markReportAsEmailed(Long reportId) {
        Optional<ReportHistory> reportOpt = reportHistoryRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            ReportHistory report = reportOpt.get();
            report.setEmailed(true);
            reportHistoryRepository.save(report);
            log.info("Marked report ID {} as emailed", reportId);
        }
    }

    /**
     * Get report history by ID
     *
     * @param id Report history ID
     * @return ReportHistory entity
     */
    public Optional<ReportHistory> getReportHistory(Long id) {
        return reportHistoryRepository.findById(id);
    }

    /**
     * Get all report history
     *
     * @return List of all report history entries
     */
    public List<ReportHistory> getAllReportHistory() {
        return reportHistoryRepository.findAll();
    }

    /**
     * Get recent reports (last N days)
     *
     * @param days Number of days to look back
     * @return List of recent report history entries
     */
    public List<ReportHistory> getRecentReports(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return reportHistoryRepository.findRecentReports(sinceDate);
    }

    /**
     * Get reports by type
     *
     * @param reportType Report type
     * @return List of report history entries
     */
    public List<ReportHistory> getReportsByType(ReportType reportType) {
        return reportHistoryRepository.findByReportType(reportType);
    }

    /**
     * Get reports by status
     *
     * @param status Report status
     * @return List of report history entries
     */
    public List<ReportHistory> getReportsByStatus(ReportStatus status) {
        return reportHistoryRepository.findByStatus(status);
    }

    /**
     * Get reports by user
     *
     * @param username Username
     * @return List of report history entries
     */
    public List<ReportHistory> getReportsByUser(String username) {
        return reportHistoryRepository.findByGeneratedBy(username);
    }

    /**
     * Get scheduled reports
     *
     * @return List of scheduled report history entries
     */
    public List<ReportHistory> getScheduledReports() {
        return reportHistoryRepository.findByScheduledTrue();
    }

    /**
     * Clean up old reports (older than N days)
     *
     * @param retentionDays Number of days to retain reports
     * @return Number of reports cleaned up
     */
    @Transactional
    public int cleanupOldReports(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        List<ReportHistory> oldReports = reportHistoryRepository.findOldReportsForCleanup(cutoffDate);

        int cleanedCount = 0;
        for (ReportHistory report : oldReports) {
            // Archive or delete the physical file if needed
            // File cleanup logic here

            report.setStatus(ReportStatus.ARCHIVED);
            reportHistoryRepository.save(report);
            cleanedCount++;
        }

        log.info("Cleaned up {} old reports (older than {} days)", cleanedCount, retentionDays);
        return cleanedCount;
    }

    /**
     * Get report statistics
     *
     * @return Map of report statistics
     */
    public ReportStatistics getReportStatistics() {
        long totalReports = reportHistoryRepository.count();
        long completedReports = reportHistoryRepository.countByStatus(ReportStatus.COMPLETED);
        long failedReports = reportHistoryRepository.countByStatus(ReportStatus.FAILED);
        long scheduledReports = reportHistoryRepository.findByScheduledTrue().size();

        return new ReportStatistics(totalReports, completedReports, failedReports, scheduledReports);
    }

    /**
     * Report Statistics DTO
     */
    public record ReportStatistics(
        long totalReports,
        long completedReports,
        long failedReports,
        long scheduledReports
    ) {}

    /**
     * Get paginated report history
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of report history entries
     */
    public PagedReports getReportHistoryPaginated(int page, int size) {
        long totalCount = reportHistoryRepository.count();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // Simple pagination - fetch all and slice (for now)
        List<ReportHistory> allReports = reportHistoryRepository.findAll();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, allReports.size());

        List<ReportHistory> pageContent = allReports.subList(fromIndex, toIndex);

        return new PagedReports(pageContent, page, size, totalCount, totalPages);
    }

    /**
     * Paged Reports DTO
     */
    public record PagedReports(
        List<ReportHistory> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages
    ) {
        public boolean hasNext() {
            return currentPage < totalPages - 1;
        }

        public boolean hasPrevious() {
            return currentPage > 0;
        }
    }
}
