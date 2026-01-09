package com.heronix.service;

import com.heronix.model.domain.ReportHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ScheduledReportService
 *
 * Tests the scheduled report generation functionality including:
 * - Daily attendance report scheduling
 * - Weekly summary report scheduling
 * - Monthly chronic absenteeism report scheduling
 * - Report storage and history tracking
 * - Email notification integration
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 53 - Testing Infrastructure
 */
@ExtendWith(MockitoExtension.class)
class ScheduledReportServiceTest {

    @Mock
    private AttendanceReportExportService exportService;

    @Mock
    private AttendanceReportPdfService pdfService;

    @Mock
    private ReportHistoryService reportHistoryService;

    @Mock
    private EmailNotificationService emailService;

    @InjectMocks
    private ScheduledReportService scheduledReportService;

    @TempDir
    Path tempDir;

    private byte[] sampleExcelData;
    private byte[] samplePdfData;

    @BeforeEach
    void setUp() {
        sampleExcelData = "Sample Excel Data".getBytes();
        samplePdfData = "Sample PDF Data".getBytes();

        // Set the report storage path to temp directory
        ReflectionTestUtils.setField(scheduledReportService, "reportStoragePath", tempDir.toString());
    }

    @Test
    void testGenerateDailyAttendanceReport_Success() throws IOException {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(exportService.exportDailyAttendanceExcel(yesterday)).thenReturn(sampleExcelData);
        when(pdfService.exportDailyAttendancePdf(yesterday)).thenReturn(samplePdfData);

        ReportHistory mockHistory = ReportHistory.builder()
            .id(1L)
            .reportType(ReportHistory.ReportType.DAILY_ATTENDANCE)
            .reportFormat(ReportHistory.ReportFormat.EXCEL)
            .build();

        when(reportHistoryService.recordReportGeneration(
            any(), any(), any(), any(), anyLong(), any(), any(), any(), anyBoolean()
        )).thenReturn(mockHistory);

        // When
        scheduledReportService.generateDailyAttendanceReport();

        // Then
        verify(exportService, times(1)).exportDailyAttendanceExcel(yesterday);
        verify(pdfService, times(1)).exportDailyAttendancePdf(yesterday);
        verify(reportHistoryService, times(2)).recordReportGeneration(
            any(), any(), any(), any(), anyLong(), eq(yesterday), eq(yesterday), eq("SYSTEM"), eq(true)
        );
        verify(emailService, times(1)).sendDailyAttendanceReport(eq(yesterday), any(), any());
    }

    @Test
    void testGenerateDailyAttendanceReport_ExportServiceFailure() throws IOException {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(exportService.exportDailyAttendanceExcel(yesterday))
            .thenThrow(new IOException("Database connection error"));

        // When
        scheduledReportService.generateDailyAttendanceReport();

        // Then
        verify(exportService, times(1)).exportDailyAttendanceExcel(yesterday);
        verify(reportHistoryService, times(1)).recordReportFailure(
            eq(ReportHistory.ReportType.DAILY_ATTENDANCE),
            eq(ReportHistory.ReportFormat.EXCEL),
            contains("Database connection error"),
            eq("SYSTEM")
        );
        verify(emailService, never()).sendDailyAttendanceReport(any(), any(), any());
    }

    @Test
    void testGenerateWeeklyAttendanceSummary_Success() throws IOException {
        // Given
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6);

        when(exportService.exportStudentAttendanceSummary(startDate, endDate)).thenReturn(sampleExcelData);
        when(pdfService.exportStudentAttendanceSummaryPdf(startDate, endDate)).thenReturn(samplePdfData);

        ReportHistory mockHistory = ReportHistory.builder()
            .id(2L)
            .reportType(ReportHistory.ReportType.WEEKLY_SUMMARY)
            .reportFormat(ReportHistory.ReportFormat.EXCEL)
            .build();

        when(reportHistoryService.recordReportGeneration(
            any(), any(), any(), any(), anyLong(), any(), any(), any(), anyBoolean()
        )).thenReturn(mockHistory);

        // When
        scheduledReportService.generateWeeklyAttendanceSummary();

        // Then
        verify(exportService, times(1)).exportStudentAttendanceSummary(startDate, endDate);
        verify(pdfService, times(1)).exportStudentAttendanceSummaryPdf(startDate, endDate);
        verify(reportHistoryService, times(2)).recordReportGeneration(
            any(), any(), any(), any(), anyLong(), eq(startDate), eq(endDate), eq("SYSTEM"), eq(true)
        );
        verify(emailService, times(1)).sendWeeklyAttendanceSummary(eq(startDate), eq(endDate), any(), any());
    }

    @Test
    void testGenerateMonthlyChronicAbsenteeismReport_Success() throws IOException {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1).minusMonths(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        when(exportService.exportChronicAbsenteeismReport(startDate, endDate, 10.0)).thenReturn(sampleExcelData);

        ReportHistory mockHistory = ReportHistory.builder()
            .id(3L)
            .reportType(ReportHistory.ReportType.CHRONIC_ABSENTEEISM)
            .reportFormat(ReportHistory.ReportFormat.EXCEL)
            .build();

        when(reportHistoryService.recordReportGeneration(
            any(), any(), any(), any(), anyLong(), any(), any(), any(), anyBoolean()
        )).thenReturn(mockHistory);

        // When
        scheduledReportService.generateMonthlyChronicAbsenteeismReport();

        // Then
        verify(exportService, times(1)).exportChronicAbsenteeismReport(startDate, endDate, 10.0);
        verify(reportHistoryService, times(1)).recordReportGeneration(
            eq(ReportHistory.ReportType.CHRONIC_ABSENTEEISM),
            eq(ReportHistory.ReportFormat.EXCEL),
            any(), any(), anyLong(), eq(startDate), eq(endDate), eq("SYSTEM"), eq(true)
        );
        verify(emailService, times(1)).sendChronicAbsenteeismReport(eq(startDate), eq(endDate), any());
    }

    @Test
    void testGenerateMonthlyChronicAbsenteeismReport_PdfServiceFailure() throws IOException {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1).minusMonths(1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        when(exportService.exportChronicAbsenteeismReport(startDate, endDate, 10.0))
            .thenThrow(new IOException("PDF generation failed"));

        // When
        scheduledReportService.generateMonthlyChronicAbsenteeismReport();

        // Then
        verify(exportService, times(1)).exportChronicAbsenteeismReport(startDate, endDate, 10.0);
        verify(reportHistoryService, times(1)).recordReportFailure(
            eq(ReportHistory.ReportType.CHRONIC_ABSENTEEISM),
            eq(ReportHistory.ReportFormat.EXCEL),
            contains("PDF generation failed"),
            eq("SYSTEM")
        );
        verify(emailService, never()).sendChronicAbsenteeismReport(any(), any(), any());
    }


    @Test
    void testGenerateDailyAttendanceReport_EmailNotificationFailure() throws IOException {
        // Given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        when(exportService.exportDailyAttendanceExcel(yesterday)).thenReturn(sampleExcelData);
        when(pdfService.exportDailyAttendancePdf(yesterday)).thenReturn(samplePdfData);

        ReportHistory mockHistory = ReportHistory.builder()
            .id(1L)
            .reportType(ReportHistory.ReportType.DAILY_ATTENDANCE)
            .reportFormat(ReportHistory.ReportFormat.EXCEL)
            .build();

        when(reportHistoryService.recordReportGeneration(
            any(), any(), any(), any(), anyLong(), any(), any(), any(), anyBoolean()
        )).thenReturn(mockHistory);

        doThrow(new RuntimeException("Email server unavailable"))
            .when(emailService).sendDailyAttendanceReport(any(), any(), any());

        // When
        scheduledReportService.generateDailyAttendanceReport();

        // Then - should still complete report generation despite email failure
        verify(exportService, times(1)).exportDailyAttendanceExcel(yesterday);
        verify(pdfService, times(1)).exportDailyAttendancePdf(yesterday);
        verify(reportHistoryService, times(2)).recordReportGeneration(
            any(), any(), any(), any(), anyLong(), eq(yesterday), eq(yesterday), eq("SYSTEM"), eq(true)
        );
        verify(emailService, times(1)).sendDailyAttendanceReport(eq(yesterday), any(), any());
    }
}
