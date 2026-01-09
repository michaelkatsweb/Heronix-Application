package com.heronix.api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for Webhook Registration Requests
 *
 * Defines the structure and validation rules for creating or updating webhook subscriptions.
 * Webhooks enable real-time notifications to external systems when events occur in Heronix.
 *
 * Validation Rules:
 * - Name: Required, 3-100 characters
 * - URL: Required, valid HTTPS URL format
 * - Events: At least one event type must be subscribed
 * - Secret: Required for HMAC signature verification, 16-256 characters
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 41 - API Documentation & Testing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequestDTO {

    /**
     * Human-readable name for the webhook subscription
     */
    @NotBlank(message = "Webhook name is required")
    @Size(min = 3, max = 100, message = "Webhook name must be between 3 and 100 characters")
    private String name;

    /**
     * Target URL where webhook events will be delivered
     * Must use HTTPS for security
     */
    @NotBlank(message = "Webhook URL is required")
    @Pattern(regexp = "^https://.*", message = "Webhook URL must use HTTPS protocol")
    private String url;

    /**
     * List of event types to subscribe to
     * Examples: student.enrolled, grade.entered, attendance.recorded
     */
    @NotEmpty(message = "At least one event type must be specified")
    @Size(min = 1, max = 50, message = "Must subscribe to 1-50 event types")
    private List<@NotBlank(message = "Event type cannot be blank") String> events;

    /**
     * Secret key for HMAC signature verification
     * Used to ensure webhook authenticity
     */
    @NotBlank(message = "Webhook secret is required")
    @Size(min = 16, max = 256, message = "Secret must be between 16 and 256 characters")
    private String secret;

    /**
     * Whether the webhook is active and should receive events
     */
    @NotNull(message = "Active status must be specified")
    private Boolean active;

    /**
     * Custom HTTP headers to include in webhook requests
     * Commonly used for authentication tokens
     */
    private Map<String, String> headers;

    /**
     * Optional description of webhook purpose
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
