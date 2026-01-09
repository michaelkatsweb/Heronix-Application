package com.heronix.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Access Decision DTO
 *
 * Result of permission evaluation indicating whether access is granted.
 *
 * Contains:
 * - Grant/deny decision
 * - Reasons for decision
 * - Applied permissions
 * - Restrictions to apply
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 68 - Report Access Control & Permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDecision {

    /**
     * Decision result
     */
    public enum Decision {
        GRANTED,        // Access granted
        DENIED,         // Access denied
        CONDITIONAL     // Access granted with restrictions
    }

    /**
     * Decision result
     */
    private Decision decision;

    /**
     * Granted (true) or denied (false)
     */
    private Boolean granted;

    /**
     * Primary reason for decision
     */
    private String reason;

    /**
     * Detailed reasons
     */
    @Builder.Default
    private List<String> reasons = new ArrayList<>();

    /**
     * Applied permissions
     */
    @Builder.Default
    private List<ReportPermission> appliedPermissions = new ArrayList<>();

    /**
     * Data restrictions to apply
     */
    @Builder.Default
    private List<String> dataRestrictions = new ArrayList<>();

    /**
     * Fields to mask
     */
    @Builder.Default
    private List<String> maskedFields = new ArrayList<>();

    /**
     * Maximum records allowed
     */
    private Integer maxRecords;

    /**
     * Additional metadata
     */
    private java.util.Map<String, Object> metadata;

    /**
     * Create granted decision
     */
    public static AccessDecision granted(String reason) {
        return AccessDecision.builder()
                .decision(Decision.GRANTED)
                .granted(true)
                .reason(reason)
                .build();
    }

    /**
     * Create denied decision
     */
    public static AccessDecision denied(String reason) {
        return AccessDecision.builder()
                .decision(Decision.DENIED)
                .granted(false)
                .reason(reason)
                .build();
    }

    /**
     * Create conditional decision
     */
    public static AccessDecision conditional(String reason, List<String> restrictions) {
        return AccessDecision.builder()
                .decision(Decision.CONDITIONAL)
                .granted(true)
                .reason(reason)
                .dataRestrictions(restrictions)
                .build();
    }

    /**
     * Add reason
     */
    public void addReason(String reason) {
        if (reasons == null) {
            reasons = new ArrayList<>();
        }
        reasons.add(reason);
    }

    /**
     * Add data restriction
     */
    public void addDataRestriction(String restriction) {
        if (dataRestrictions == null) {
            dataRestrictions = new ArrayList<>();
        }
        dataRestrictions.add(restriction);
    }

    /**
     * Add masked field
     */
    public void addMaskedField(String field) {
        if (maskedFields == null) {
            maskedFields = new ArrayList<>();
        }
        maskedFields.add(field);
    }
}
