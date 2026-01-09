package com.heronix.controller;

import com.heronix.model.domain.ReportHistory;
import com.heronix.service.ReportHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for ReportManagementApiController
 *
 * Tests the REST API endpoints for report management including:
 * - Retrieving report history
 * - Filtering reports by type, status, and user
 * - Getting report statistics
 * - Cleaning up old reports
 * - HTTP response validation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 53 - Testing Infrastructure
 */
@WebMvcTest(ReportManagementApiController.class)
class ReportManagementApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportHistoryService reportHistoryService;

    private ReportHistory createSampleReport(Long id, ReportHistory.ReportType type, ReportHistory.ReportStatus status) {
        return ReportHistory.builder()
            .id(id)
            .reportType(type)
            .reportFormat(ReportHistory.ReportFormat.EXCEL)
            .reportName("sample-report-" + id + ".xlsx")
            .filePath("/reports/sample-report-" + id + ".xlsx")
            .fileSize(12345L)
            .startDate(LocalDate.of(2025, 12, 1))
            .endDate(LocalDate.of(2025, 12, 30))
            .generatedBy("testuser")
            .generatedAt(LocalDateTime.of(2025, 12, 30, 8, 0))
            .status(status)
            .scheduled(false)
            .emailed(false)
            .downloadCount(0)
            .build();
    }

    @Test
    void testGetAllReportHistory_Success() throws Exception {
        // Given
        List<ReportHistory> reports = Arrays.asList(
            createSampleReport(1L, ReportHistory.ReportType.DAILY_ATTENDANCE, ReportHistory.ReportStatus.COMPLETED),
            createSampleReport(2L, ReportHistory.ReportType.STUDENT_SUMMARY, ReportHistory.ReportStatus.COMPLETED)
        );
        when(reportHistoryService.getAllReportHistory()).thenReturn(reports);

        // When/Then
        mockMvc.perform(get("/api/reports/management/history"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].reportType", is("DAILY_ATTENDANCE")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].reportType", is("STUDENT_SUMMARY")));
    }

    @Test
    void testGetAllReportHistory_EmptyList() throws Exception {
        // Given
        when(reportHistoryService.getAllReportHistory()).thenReturn(Arrays.asList());

        // When/Then
        mockMvc.perform(get("/api/reports/management/history"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetReportHistory_Success() throws Exception {
        // Given
        Long reportId = 1L;
        ReportHistory report = createSampleReport(reportId, ReportHistory.ReportType.DAILY_ATTENDANCE, ReportHistory.ReportStatus.COMPLETED);
        when(reportHistoryService.getReportHistory(reportId)).thenReturn(Optional.of(report));

        // When/Then
        mockMvc.perform(get("/api/reports/management/history/{id}", reportId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.reportType", is("DAILY_ATTENDANCE")))
            .andExpect(jsonPath("$.reportFormat", is("EXCEL")))
            .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void testGetReportHistory_NotFound() throws Exception {
        // Given
        Long reportId = 999L;
        when(reportHistoryService.getReportHistory(reportId)).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/reports/management/history/{id}", reportId))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetRecentReports_Success() throws Exception {
        // Given
        List<ReportHistory> reports = Arrays.asList(
            createSampleReport(1L, ReportHistory.ReportType.DAILY_ATTENDANCE, ReportHistory.ReportStatus.COMPLETED),
            createSampleReport(2L, ReportHistory.ReportType.WEEKLY_SUMMARY, ReportHistory.ReportStatus.COMPLETED)
        );
        when(reportHistoryService.getRecentReports(7)).thenReturn(reports);

        // When/Then
        mockMvc.perform(get("/api/reports/management/recent")
                .param("days", "7"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testGetRecentReports_DefaultDays() throws Exception {
        // Given
        List<ReportHistory> reports = Arrays.asList(
            createSampleReport(1L, ReportHistory.ReportType.DAILY_ATTENDANCE, ReportHistory.ReportStatus.COMPLETED)
        );
        when(reportHistoryService.getRecentReports(30)).thenReturn(reports);

        // When/Then - No days parameter, should default to 30
        mockMvc.perform(get("/api/reports/management/recent"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testGetReportsByType_Success() throws Exception {
        // Given
        ReportHistory.ReportType type = ReportHistory.ReportType.DAILY_ATTENDANCE;
        List<ReportHistory> reports = Arrays.asList(
            createSampleReport(1L, type, ReportHistory.ReportStatus.COMPLETED),
            createSampleReport(2L, type, ReportHistory.ReportStatus.COMPLETED)
        );
        when(reportHistoryService.getReportsByType(type)).thenReturn(reports);

        // When/Then
        mockMvc.perform(get("/api/reports/management/type/{type}", "DAILY_ATTENDANCE"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].reportType", is("DAILY_ATTENDANCE")))
            .andExpect(jsonPath("$[1].reportType", is("DAILY_ATTENDANCE")));
    }

    @Test
    void testGetReportsByType_InvalidType() throws Exception {
        // When/Then - Invalid report type should result in bad request
        mockMvc.perform(get("/api/reports/management/type/{type}", "INVALID_TYPE"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void testGetReportsByStatus_Success() throws Exception {
        // Given
        ReportHistory.ReportStatus status = ReportHistory.ReportStatus.COMPLETED;
        List<ReportHistory> reports = Arrays.asList(
            createSampleReport(1L, ReportHistory.ReportType.DAILY_ATTENDANCE, status),
            createSampleReport(2L, ReportHistory.ReportType.STUDENT_SUMMARY, status)
        );
        when(reportHistoryService.getReportsByStatus(status)).thenReturn(reports);

        // When/Then
        mockMvc.perform(get("/api/reports/management/status/{status}", "COMPLETED"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].status", is("COMPLETED")))
            .andExpect(jsonPath("$[1].status", is("COMPLETED")));
    }

    @Test
    void testGetReportsByStatus_FailedReports() throws Exception {
        // Given
        ReportHistory.ReportStatus status = ReportHistory.ReportStatus.FAILED;
        ReportHistory failedReport = createSampleReport(1L, ReportHistory.ReportType.DAILY_ATTENDANCE, status);
        failedReport.setErrorMessage("Database connection error");

        when(reportHistoryService.getReportsByStatus(status)).thenReturn(Arrays.asList(failedReport));

        // When/Then
        mockMvc.perform(get("/api/reports/management/status/{status}", "FAILED"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].status", is("FAILED")))
            .andExpect(jsonPath("$[0].errorMessage", is("Database connection error")));
    }

    @Test
    void testGetReportsByUser_Success() throws Exception {
        // Given
        String username = "testuser";
        List<ReportHistory> reports = Arrays.asList(
            createSampleReport(1L, ReportHistory.ReportType.DAILY_ATTENDANCE, ReportHistory.ReportStatus.COMPLETED),
            createSampleReport(2L, ReportHistory.ReportType.STUDENT_SUMMARY, ReportHistory.ReportStatus.COMPLETED)
        );
        when(reportHistoryService.getReportsByUser(username)).thenReturn(reports);

        // When/Then
        mockMvc.perform(get("/api/reports/management/user/{username}", username))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].generatedBy", is("testuser")))
            .andExpect(jsonPath("$[1].generatedBy", is("testuser")));
    }

    @Test
    void testGetScheduledReports_Success() throws Exception {
        // Given
        ReportHistory scheduledReport = createSampleReport(1L, ReportHistory.ReportType.WEEKLY_SUMMARY, ReportHistory.ReportStatus.COMPLETED);
        scheduledReport.setScheduled(true);

        when(reportHistoryService.getScheduledReports()).thenReturn(Arrays.asList(scheduledReport));

        // When/Then
        mockMvc.perform(get("/api/reports/management/scheduled"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].scheduled", is(true)));
    }

    @Test
    void testGetReportStatistics_Success() throws Exception {
        // Given
        ReportHistoryService.ReportStatistics stats = new ReportHistoryService.ReportStatistics(
            100L, 85L, 10L, 5L
        );
        when(reportHistoryService.getReportStatistics()).thenReturn(stats);

        // When/Then
        mockMvc.perform(get("/api/reports/management/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalReports", is(100)))
            .andExpect(jsonPath("$.completedReports", is(85)))
            .andExpect(jsonPath("$.failedReports", is(10)))
            .andExpect(jsonPath("$.scheduledReports", is(5)));
    }

    @Test
    void testCleanupOldReports_Success() throws Exception {
        // Given
        int retentionDays = 90;
        int cleanedCount = 25;
        when(reportHistoryService.cleanupOldReports(retentionDays)).thenReturn(cleanedCount);

        // When/Then
        mockMvc.perform(post("/api/reports/management/cleanup")
                .param("retentionDays", String.valueOf(retentionDays)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cleanedCount", is(25)))
            .andExpect(jsonPath("$.retentionDays", is(90)));
    }

    @Test
    void testCleanupOldReports_DefaultRetentionDays() throws Exception {
        // Given
        int defaultRetentionDays = 90;
        int cleanedCount = 15;
        when(reportHistoryService.cleanupOldReports(defaultRetentionDays)).thenReturn(cleanedCount);

        // When/Then - No retentionDays parameter, should default to 90
        mockMvc.perform(post("/api/reports/management/cleanup"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.cleanedCount", is(15)))
            .andExpect(jsonPath("$.retentionDays", is(90)));
    }

    @Test
    void testGetAllReportHistory_ServiceThrowsException() throws Exception {
        // Given
        when(reportHistoryService.getAllReportHistory())
            .thenThrow(new RuntimeException("Database connection error"));

        // When/Then
        mockMvc.perform(get("/api/reports/management/history"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetReportStatistics_NoReports() throws Exception {
        // Given
        ReportHistoryService.ReportStatistics emptyStats = new ReportHistoryService.ReportStatistics(
            0L, 0L, 0L, 0L
        );
        when(reportHistoryService.getReportStatistics()).thenReturn(emptyStats);

        // When/Then
        mockMvc.perform(get("/api/reports/management/statistics"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalReports", is(0)))
            .andExpect(jsonPath("$.completedReports", is(0)))
            .andExpect(jsonPath("$.failedReports", is(0)))
            .andExpect(jsonPath("$.scheduledReports", is(0)));
    }
}
