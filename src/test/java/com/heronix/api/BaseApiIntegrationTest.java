package com.heronix.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base Integration Test Class for REST API Testing
 *
 * Provides common setup and utilities for API integration tests.
 * All API integration tests should extend this class.
 *
 * Features:
 * - Spring Boot test context with MockMvc
 * - JSON serialization/deserialization with ObjectMapper
 * - Test database configuration
 * - Common test utilities and assertions
 *
 * Test Profile:
 * - Uses 'test' profile with in-memory H2 database
 * - Isolated test data for each test
 * - No security for easier testing (can be enabled per test)
 *
 * Usage Example:
 * <pre>
 * public class AttendanceAnalyticsApiIntegrationTest extends BaseApiIntegrationTest {
 *     @Test
 *     public void testGetDashboard() throws Exception {
 *         mockMvc.perform(get("/api/attendance-analytics/dashboard")
 *             .param("startDate", "2025-01-01")
 *             .param("endDate", "2025-12-31"))
 *             .andExpect(status().isOk())
 *             .andExpect(jsonPath("$.success").value(true));
 *     }
 * }
 * </pre>
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseApiIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Setup method run before each test
     * Override in subclasses to add test-specific setup
     */
    @BeforeEach
    public void setUp() {
        // Common setup for all API tests
        // Subclasses can override and call super.setUp() for additional setup
    }

    /**
     * Helper method to convert object to JSON string
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Helper method to convert JSON string to object
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
