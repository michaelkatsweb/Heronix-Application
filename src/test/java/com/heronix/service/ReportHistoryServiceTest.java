package com.heronix.service;

import com.heronix.model.domain.ReportHistory;
import com.heronix.model.domain.ReportHistory.ReportFormat;
import com.heronix.model.domain.ReportHistory.ReportStatus;
import com.heronix.model.domain.ReportHistory.ReportType;
import com.heronix.repository.ReportHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ReportHistoryService
 *
 * Tests the report history tracking functionality including:
 * - Recording successful report generation
 * - Recording failed report generation
 * - Tracking report access
 * - Cleaning up old reports
 * - Generating statistics
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 52 - Testing & Documentation
 */
@ExtendWith(MockitoExtension.class)
class ReportHistoryServiceTest {

    @Mock
    private ReportHistoryRepository reportHistoryRepository;

    @InjectMocks
    private ReportHistoryService reportHistoryService;

    private ReportHistory sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = ReportHistory.builder()
            .id(1L)
            .reportType(ReportType.DAILY_ATTENDANCE)
            .reportFormat(ReportFormat.EXCEL)
            .reportName("daily-attendance-2025-12-30.xlsx")
            .filePath("/reports/daily-attendance-2025-12-30.xlsx")
            .fileSize(12345L)
            .startDate(LocalDate.of(2025, 12, 30))
            .endDate(LocalDate.of(2025, 12, 30))
            .generatedBy("testuser")
            .generatedAt(LocalDateTime.now())
            .status(ReportStatus.COMPLETED)
            .scheduled(false)
            .emailed(false)
            .downloadCount(0)
            .build();
    }

    @Test
    void testRecordReportGeneration_Success() {
        // Given
        when(reportHistoryRepository.save(any(ReportHistory.class))).thenReturn(sampleReport);

        // When
        ReportHistory result = reportHistoryService.recordReportGeneration(
            ReportType.DAILY_ATTENDANCE,
            ReportFormat.EXCEL,
            "daily-attendance-2025-12-30.xlsx",
            "/reports/daily-attendance-2025-12-30.xlsx",
            12345L,
            LocalDate.of(2025, 12, 30),
            LocalDate.of(2025, 12, 30),
            "testuser",
            false
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReportType()).isEqualTo(ReportType.DAILY_ATTENDANCE);
        assertThat(result.getReportFormat()).isEqualTo(ReportFormat.EXCEL);
        assertThat(result.getStatus()).isEqualTo(ReportStatus.COMPLETED);
        verify(reportHistoryRepository, times(1)).save(any(ReportHistory.class));
    }

    @Test
    void testRecordReportFailure() {
        // Given
        ReportHistory failedReport = ReportHistory.builder()
            .id(2L)
            .reportType(ReportType.DAILY_ATTENDANCE)
            .reportFormat(ReportFormat.PDF)
            .reportName("Failed Report")
            .generatedBy("testuser")
            .status(ReportStatus.FAILED)
            .errorMessage("Database connection error")
            .build();

        when(reportHistoryRepository.save(any(ReportHistory.class))).thenReturn(failedReport);

        // When
        ReportHistory result = reportHistoryService.recordReportFailure(
            ReportType.DAILY_ATTENDANCE,
            ReportFormat.PDF,
            "Database connection error",
            "testuser"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReportStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo("Database connection error");
        verify(reportHistoryRepository, times(1)).save(any(ReportHistory.class));
    }

    @Test
    void testRecordReportAccess() {
        // Given
        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportHistoryRepository.save(any(ReportHistory.class))).thenReturn(sampleReport);

        // When
        reportHistoryService.recordReportAccess(1L);

        // Then
        verify(reportHistoryRepository, times(1)).findById(1L);
        verify(reportHistoryRepository, times(1)).save(sampleReport);
        assertThat(sampleReport.getDownloadCount()).isEqualTo(1);
        assertThat(sampleReport.getLastAccessed()).isNotNull();
    }

    @Test
    void testRecordReportAccess_NotFound() {
        // Given
        when(reportHistoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        reportHistoryService.recordReportAccess(999L);

        // Then
        verify(reportHistoryRepository, times(1)).findById(999L);
        verify(reportHistoryRepository, never()).save(any(ReportHistory.class));
    }

    @Test
    void testMarkReportAsEmailed() {
        // Given
        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(reportHistoryRepository.save(any(ReportHistory.class))).thenReturn(sampleReport);

        // When
        reportHistoryService.markReportAsEmailed(1L);

        // Then
        verify(reportHistoryRepository, times(1)).findById(1L);
        verify(reportHistoryRepository, times(1)).save(sampleReport);
        assertThat(sampleReport.getEmailed()).isTrue();
    }

    @Test
    void testGetReportHistory() {
        // Given
        when(reportHistoryRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

        // When
        Optional<ReportHistory> result = reportHistoryService.getReportHistory(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(reportHistoryRepository, times(1)).findById(1L);
    }

    @Test
    void testGetAllReportHistory() {
        // Given
        List<ReportHistory> reports = Arrays.asList(sampleReport);
        when(reportHistoryRepository.findAll()).thenReturn(reports);

        // When
        List<ReportHistory> result = reportHistoryService.getAllReportHistory();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        verify(reportHistoryRepository, times(1)).findAll();
    }

    @Test
    void testGetRecentReports() {
        // Given
        List<ReportHistory> reports = Arrays.asList(sampleReport);
        when(reportHistoryRepository.findRecentReports(any(LocalDateTime.class))).thenReturn(reports);

        // When
        List<ReportHistory> result = reportHistoryService.getRecentReports(7);

        // Then
        assertThat(result).hasSize(1);
        verify(reportHistoryRepository, times(1)).findRecentReports(any(LocalDateTime.class));
    }

    @Test
    void testGetReportsByType() {
        // Given
        List<ReportHistory> reports = Arrays.asList(sampleReport);
        when(reportHistoryRepository.findByReportType(ReportType.DAILY_ATTENDANCE)).thenReturn(reports);

        // When
        List<ReportHistory> result = reportHistoryService.getReportsByType(ReportType.DAILY_ATTENDANCE);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReportType()).isEqualTo(ReportType.DAILY_ATTENDANCE);
        verify(reportHistoryRepository, times(1)).findByReportType(ReportType.DAILY_ATTENDANCE);
    }

    @Test
    void testGetReportsByStatus() {
        // Given
        List<ReportHistory> reports = Arrays.asList(sampleReport);
        when(reportHistoryRepository.findByStatus(ReportStatus.COMPLETED)).thenReturn(reports);

        // When
        List<ReportHistory> result = reportHistoryService.getReportsByStatus(ReportStatus.COMPLETED);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(ReportStatus.COMPLETED);
        verify(reportHistoryRepository, times(1)).findByStatus(ReportStatus.COMPLETED);
    }

    @Test
    void testGetReportsByUser() {
        // Given
        List<ReportHistory> reports = Arrays.asList(sampleReport);
        when(reportHistoryRepository.findByGeneratedBy("testuser")).thenReturn(reports);

        // When
        List<ReportHistory> result = reportHistoryService.getReportsByUser("testuser");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGeneratedBy()).isEqualTo("testuser");
        verify(reportHistoryRepository, times(1)).findByGeneratedBy("testuser");
    }

    @Test
    void testGetScheduledReports() {
        // Given
        ReportHistory scheduledReport = ReportHistory.builder()
            .id(2L)
            .reportType(ReportType.WEEKLY_SUMMARY)
            .reportFormat(ReportFormat.PDF)
            .scheduled(true)
            .build();
        List<ReportHistory> reports = Arrays.asList(scheduledReport);
        when(reportHistoryRepository.findByScheduledTrue()).thenReturn(reports);

        // When
        List<ReportHistory> result = reportHistoryService.getScheduledReports();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduled()).isTrue();
        verify(reportHistoryRepository, times(1)).findByScheduledTrue();
    }

    @Test
    void testCleanupOldReports() {
        // Given
        List<ReportHistory> oldReports = Arrays.asList(sampleReport);
        when(reportHistoryRepository.findOldReportsForCleanup(any(LocalDateTime.class))).thenReturn(oldReports);
        when(reportHistoryRepository.save(any(ReportHistory.class))).thenReturn(sampleReport);

        // When
        int cleanedCount = reportHistoryService.cleanupOldReports(90);

        // Then
        assertThat(cleanedCount).isEqualTo(1);
        verify(reportHistoryRepository, times(1)).findOldReportsForCleanup(any(LocalDateTime.class));
        verify(reportHistoryRepository, times(1)).save(sampleReport);
        assertThat(sampleReport.getStatus()).isEqualTo(ReportStatus.ARCHIVED);
    }

    @Test
    void testGetReportStatistics() {
        // Given
        when(reportHistoryRepository.count()).thenReturn(100L);
        when(reportHistoryRepository.countByStatus(ReportStatus.COMPLETED)).thenReturn(85L);
        when(reportHistoryRepository.countByStatus(ReportStatus.FAILED)).thenReturn(10L);
        when(reportHistoryRepository.findByScheduledTrue()).thenReturn(Arrays.asList(sampleReport));

        // When
        ReportHistoryService.ReportStatistics stats = reportHistoryService.getReportStatistics();

        // Then
        assertThat(stats.totalReports()).isEqualTo(100L);
        assertThat(stats.completedReports()).isEqualTo(85L);
        assertThat(stats.failedReports()).isEqualTo(10L);
        assertThat(stats.scheduledReports()).isEqualTo(1L);
    }
}
