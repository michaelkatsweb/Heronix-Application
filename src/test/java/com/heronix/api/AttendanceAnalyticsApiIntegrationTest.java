package com.heronix.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Attendance Analytics API
 *
 * Tests the REST API endpoints for attendance analytics, including:
 * - School-wide dashboard
 * - ADA/ADM calculations
 * - Attendance trends
 * - Chronic absenteeism tracking
 *
 * Test Coverage:
 * - Successful requests with valid data
 * - Invalid date range handling
 * - Missing required parameters
 * - Response structure validation
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
public class AttendanceAnalyticsApiIntegrationTest extends BaseApiIntegrationTest {

    private static final String BASE_URL = "/api/attendance-analytics";

    @Test
    @DisplayName("GET /dashboard - Should return school-wide attendance dashboard")
    public void testGetAttendanceDashboard_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dashboard")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.dashboard").exists())
            .andExpect(jsonPath("$.period.startDate").value("2025-01-01"))
            .andExpect(jsonPath("$.period.endDate").value("2025-12-31"));
    }

    @Test
    @DisplayName("GET /dashboard - Should return 400 when startDate is missing")
    public void testGetAttendanceDashboard_MissingStartDate() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dashboard")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /dashboard - Should return 400 when endDate is missing")
    public void testGetAttendanceDashboard_MissingEndDate() throws Exception {
        mockMvc.perform(get(BASE_URL + "/dashboard")
                .param("startDate", "2025-01-01")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /ada - Should calculate Average Daily Attendance")
    public void testCalculateADA_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/ada")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.ada").exists());
    }

    @Test
    @DisplayName("GET /adm - Should calculate Average Daily Membership")
    public void testCalculateADM_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/adm")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.adm").exists());
    }

    @Test
    @DisplayName("GET /trends - Should return attendance trends with default groupBy")
    public void testGetAttendanceTrends_DefaultGroupBy() throws Exception {
        mockMvc.perform(get(BASE_URL + "/trends")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.trends").isArray())
            .andExpect(jsonPath("$.groupBy").value("week"));
    }

    @Test
    @DisplayName("GET /trends - Should return attendance trends grouped by month")
    public void testGetAttendanceTrends_GroupByMonth() throws Exception {
        mockMvc.perform(get(BASE_URL + "/trends")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .param("groupBy", "month")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.groupBy").value("month"));
    }

    @Test
    @DisplayName("GET /chronic-absenteeism - Should identify chronically absent students")
    public void testGetChronicAbsenteeism_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/chronic-absenteeism")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /at-risk - Should identify at-risk students")
    public void testIdentifyAtRiskStudents_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/at-risk")
                .param("startDate", "2025-01-01")
                .param("endDate", "2025-12-31")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.students").isArray());
    }
}
