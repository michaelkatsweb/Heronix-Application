package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Excuse Code Entity
 *
 * Standardized codes for excusing student absences.
 * Used by clerks and administrators to categorize absence reasons.
 *
 * Features:
 * - Predefined excuse categories (Medical, Family, School Activity, etc.)
 * - State-specific compliance codes
 * - Documentation requirements per code
 * - Auto-excuse vs requires-approval workflow
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Entity
@Table(name = "excuse_codes", indexes = {
    @Index(name = "idx_excuse_code", columnList = "code"),
    @Index(name = "idx_excuse_category", columnList = "category"),
    @Index(name = "idx_excuse_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExcuseCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Short code for the excuse (e.g., "MED", "FAM", "SCH")
     * Used in quick entry and reports
     */
    @Column(name = "code", nullable = false, unique = true, length = 10)
    private String code;

    /**
     * Full name/description of the excuse type
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Detailed description of when this code applies
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Category grouping for UI organization
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Builder.Default
    private ExcuseCategory category = ExcuseCategory.OTHER;

    /**
     * Whether this excuse counts as excused for state reporting
     */
    @Column(name = "counts_as_excused")
    @Builder.Default
    private Boolean countsAsExcused = true;

    /**
     * Whether documentation is required (doctor's note, etc.)
     */
    @Column(name = "documentation_required")
    @Builder.Default
    private Boolean documentationRequired = false;

    /**
     * Maximum consecutive days this excuse can be used
     * without additional approval (0 = unlimited)
     */
    @Column(name = "max_consecutive_days")
    @Builder.Default
    private Integer maxConsecutiveDays = 0;

    /**
     * Whether this excuse requires administrator approval
     */
    @Column(name = "requires_approval")
    @Builder.Default
    private Boolean requiresApproval = false;

    /**
     * State compliance code (for state reporting)
     * Maps to state-specific absence codes
     */
    @Column(name = "state_code", length = 20)
    private String stateCode;

    /**
     * Whether this code is currently active
     */
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    /**
     * Sort order for display
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Color for UI display (hex code)
     */
    @Column(name = "color", length = 7)
    @Builder.Default
    private String color = "#9E9E9E";

    /**
     * Created timestamp
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Updated timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // EXCUSE CATEGORY ENUM
    // ========================================================================

    /**
     * Categories for grouping excuse codes
     */
    public enum ExcuseCategory {
        MEDICAL("Medical", "#4CAF50"),
        FAMILY("Family", "#2196F3"),
        SCHOOL_ACTIVITY("School Activity", "#9C27B0"),
        RELIGIOUS("Religious", "#FF9800"),
        LEGAL("Legal/Court", "#795548"),
        WEATHER("Weather/Emergency", "#607D8B"),
        SUSPENSION("Disciplinary", "#F44336"),
        OTHER("Other", "#9E9E9E");

        private final String displayName;
        private final String color;

        ExcuseCategory(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getColor() {
            return color;
        }
    }

    // ========================================================================
    // PREDEFINED EXCUSE CODES (Static Factory Methods)
    // ========================================================================

    /**
     * Create standard excuse codes for initial setup
     */
    public static ExcuseCode[] getDefaultExcuseCodes() {
        return new ExcuseCode[] {
            // Medical
            ExcuseCode.builder()
                .code("MED")
                .name("Medical Appointment")
                .description("Doctor, dentist, or other medical appointment")
                .category(ExcuseCategory.MEDICAL)
                .countsAsExcused(true)
                .documentationRequired(true)
                .sortOrder(1)
                .color("#4CAF50")
                .build(),
            ExcuseCode.builder()
                .code("ILL")
                .name("Illness")
                .description("Student is sick or not feeling well")
                .category(ExcuseCategory.MEDICAL)
                .countsAsExcused(true)
                .documentationRequired(false)
                .maxConsecutiveDays(3)
                .sortOrder(2)
                .color("#66BB6A")
                .build(),
            ExcuseCode.builder()
                .code("HOS")
                .name("Hospitalization")
                .description("Student is hospitalized")
                .category(ExcuseCategory.MEDICAL)
                .countsAsExcused(true)
                .documentationRequired(true)
                .sortOrder(3)
                .color("#43A047")
                .build(),
            ExcuseCode.builder()
                .code("MEN")
                .name("Mental Health Day")
                .description("Mental health or wellness day")
                .category(ExcuseCategory.MEDICAL)
                .countsAsExcused(true)
                .documentationRequired(false)
                .maxConsecutiveDays(2)
                .sortOrder(4)
                .color("#81C784")
                .build(),

            // Family
            ExcuseCode.builder()
                .code("FAM")
                .name("Family Emergency")
                .description("Family emergency or urgent family matter")
                .category(ExcuseCategory.FAMILY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(10)
                .color("#2196F3")
                .build(),
            ExcuseCode.builder()
                .code("FUN")
                .name("Funeral/Bereavement")
                .description("Death in family or close friend")
                .category(ExcuseCategory.FAMILY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .maxConsecutiveDays(5)
                .sortOrder(11)
                .color("#1976D2")
                .build(),
            ExcuseCode.builder()
                .code("VAC")
                .name("Family Vacation")
                .description("Pre-approved family travel/vacation")
                .category(ExcuseCategory.FAMILY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .requiresApproval(true)
                .maxConsecutiveDays(10)
                .sortOrder(12)
                .color("#42A5F5")
                .build(),
            ExcuseCode.builder()
                .code("MOV")
                .name("Family Move")
                .description("Family relocation")
                .category(ExcuseCategory.FAMILY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(13)
                .color("#64B5F6")
                .build(),

            // School Activity
            ExcuseCode.builder()
                .code("FLD")
                .name("Field Trip")
                .description("School-sponsored field trip")
                .category(ExcuseCategory.SCHOOL_ACTIVITY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(20)
                .color("#9C27B0")
                .build(),
            ExcuseCode.builder()
                .code("ATH")
                .name("Athletic Event")
                .description("School sports game or competition")
                .category(ExcuseCategory.SCHOOL_ACTIVITY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(21)
                .color("#AB47BC")
                .build(),
            ExcuseCode.builder()
                .code("ACA")
                .name("Academic Competition")
                .description("Academic bowl, science fair, debate, etc.")
                .category(ExcuseCategory.SCHOOL_ACTIVITY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(22)
                .color("#BA68C8")
                .build(),
            ExcuseCode.builder()
                .code("ART")
                .name("Arts/Performance")
                .description("Band, choir, drama, art show")
                .category(ExcuseCategory.SCHOOL_ACTIVITY)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(23)
                .color("#CE93D8")
                .build(),
            ExcuseCode.builder()
                .code("COL")
                .name("College Visit")
                .description("College/university campus visit")
                .category(ExcuseCategory.SCHOOL_ACTIVITY)
                .countsAsExcused(true)
                .documentationRequired(true)
                .maxConsecutiveDays(3)
                .sortOrder(24)
                .color("#7B1FA2")
                .build(),

            // Religious
            ExcuseCode.builder()
                .code("REL")
                .name("Religious Observance")
                .description("Religious holiday or observance")
                .category(ExcuseCategory.RELIGIOUS)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(30)
                .color("#FF9800")
                .build(),

            // Legal
            ExcuseCode.builder()
                .code("CRT")
                .name("Court Appearance")
                .description("Required court appearance")
                .category(ExcuseCategory.LEGAL)
                .countsAsExcused(true)
                .documentationRequired(true)
                .sortOrder(40)
                .color("#795548")
                .build(),
            ExcuseCode.builder()
                .code("CPS")
                .name("Child Services")
                .description("DCF/CPS meeting or placement")
                .category(ExcuseCategory.LEGAL)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(41)
                .color("#8D6E63")
                .build(),

            // Weather/Emergency
            ExcuseCode.builder()
                .code("WEA")
                .name("Weather/Road Conditions")
                .description("Unsafe weather or road conditions")
                .category(ExcuseCategory.WEATHER)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(50)
                .color("#607D8B")
                .build(),
            ExcuseCode.builder()
                .code("EMG")
                .name("Emergency")
                .description("Other emergency situation")
                .category(ExcuseCategory.WEATHER)
                .countsAsExcused(true)
                .documentationRequired(false)
                .sortOrder(51)
                .color("#78909C")
                .build(),

            // Disciplinary
            ExcuseCode.builder()
                .code("ISS")
                .name("In-School Suspension")
                .description("Serving in-school suspension")
                .category(ExcuseCategory.SUSPENSION)
                .countsAsExcused(false)
                .documentationRequired(false)
                .sortOrder(60)
                .color("#F44336")
                .build(),
            ExcuseCode.builder()
                .code("OSS")
                .name("Out-of-School Suspension")
                .description("Serving out-of-school suspension")
                .category(ExcuseCategory.SUSPENSION)
                .countsAsExcused(false)
                .documentationRequired(false)
                .sortOrder(61)
                .color("#E53935")
                .build(),
            ExcuseCode.builder()
                .code("EXP")
                .name("Expulsion")
                .description("Student is expelled")
                .category(ExcuseCategory.SUSPENSION)
                .countsAsExcused(false)
                .documentationRequired(false)
                .sortOrder(62)
                .color("#C62828")
                .build(),

            // Other
            ExcuseCode.builder()
                .code("OTH")
                .name("Other Excused")
                .description("Other excused absence with explanation")
                .category(ExcuseCategory.OTHER)
                .countsAsExcused(true)
                .documentationRequired(false)
                .requiresApproval(true)
                .sortOrder(90)
                .color("#9E9E9E")
                .build(),
            ExcuseCode.builder()
                .code("UNX")
                .name("Unexcused")
                .description("Unexcused absence - no valid reason")
                .category(ExcuseCategory.OTHER)
                .countsAsExcused(false)
                .documentationRequired(false)
                .sortOrder(99)
                .color("#757575")
                .build()
        };
    }

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
