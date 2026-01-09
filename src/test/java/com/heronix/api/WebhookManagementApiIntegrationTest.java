package com.heronix.api;

import com.heronix.api.dto.WebhookRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Webhook Management API
 *
 * Tests the REST API endpoints for webhook management, including:
 * - Webhook registration with validation
 * - Webhook retrieval and updates
 * - Event type catalog
 * - Webhook delivery tracking
 *
 * Test Coverage:
 * - Bean validation (@Valid annotation)
 * - Successful webhook creation
 * - Invalid webhook data handling
 * - HTTPS URL enforcement
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
public class WebhookManagementApiIntegrationTest extends BaseApiIntegrationTest {

    private static final String BASE_URL = "/api/webhooks";

    @Test
    @DisplayName("POST /webhooks - Should register valid webhook successfully")
    public void testRegisterWebhook_Success() throws Exception {
        WebhookRequestDTO request = WebhookRequestDTO.builder()
            .name("Student Enrollment Webhook")
            .url("https://external-system.com/api/webhooks/enrollment")
            .events(Arrays.asList("student.enrolled", "student.withdrawn"))
            .secret("super-secret-key-1234567890")
            .active(true)
            .description("Webhook for student enrollment events")
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.webhook.name").value("Student Enrollment Webhook"))
            .andExpect(jsonPath("$.webhook.url").value("https://external-system.com/api/webhooks/enrollment"))
            .andExpect(jsonPath("$.webhook.events").isArray());
    }

    @Test
    @DisplayName("POST /webhooks - Should fail when name is blank")
    public void testRegisterWebhook_BlankName() throws Exception {
        WebhookRequestDTO request = WebhookRequestDTO.builder()
            .name("")
            .url("https://external-system.com/webhook")
            .events(Arrays.asList("student.enrolled"))
            .secret("super-secret-key-1234567890")
            .active(true)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Validation Failed"))
            .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("POST /webhooks - Should fail when URL is not HTTPS")
    public void testRegisterWebhook_NonHttpsUrl() throws Exception {
        WebhookRequestDTO request = WebhookRequestDTO.builder()
            .name("Test Webhook")
            .url("http://insecure-url.com/webhook")
            .events(Arrays.asList("student.enrolled"))
            .secret("super-secret-key-1234567890")
            .active(true)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.url").value("Webhook URL must use HTTPS protocol"));
    }

    @Test
    @DisplayName("POST /webhooks - Should fail when secret is too short")
    public void testRegisterWebhook_ShortSecret() throws Exception {
        WebhookRequestDTO request = WebhookRequestDTO.builder()
            .name("Test Webhook")
            .url("https://external-system.com/webhook")
            .events(Arrays.asList("student.enrolled"))
            .secret("short")
            .active(true)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.secret").value("Secret must be between 16 and 256 characters"));
    }

    @Test
    @DisplayName("POST /webhooks - Should fail when events list is empty")
    public void testRegisterWebhook_EmptyEvents() throws Exception {
        WebhookRequestDTO request = WebhookRequestDTO.builder()
            .name("Test Webhook")
            .url("https://external-system.com/webhook")
            .events(Arrays.asList())
            .secret("super-secret-key-1234567890")
            .active(true)
            .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.events").exists());
    }

    @Test
    @DisplayName("POST /webhooks - Should fail when active is null")
    public void testRegisterWebhook_NullActive() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Webhook");
        request.put("url", "https://external-system.com/webhook");
        request.put("events", Arrays.asList("student.enrolled"));
        request.put("secret", "super-secret-key-1234567890");
        // active is intentionally omitted (null)

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.active").value("Active status must be specified"));
    }

    @Test
    @DisplayName("GET /webhooks/events - Should return event types catalog")
    public void testGetEventTypes_Success() throws Exception {
        mockMvc.perform(get(BASE_URL + "/events")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.eventTypes").isArray());
    }
}
