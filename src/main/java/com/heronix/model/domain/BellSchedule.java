package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bell Schedule Entity
 * Defines a complete daily schedule configuration with multiple periods
 *
 * Features:
 * - Multiple schedule types (Regular, Early Release, Late Start, Assembly, etc.)
 * - Aggregates PeriodTimer instances into a complete schedule
 * - Date-based and day-of-week-based scheduling
 * - Multi-campus support
 * - Special event scheduling (assemblies, testing, etc.)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "bell_schedules", indexes = {
    @Index(name = "idx_bell_schedule_name", columnList = "name"),
    @Index(name = "idx_bell_schedule_type", columnList = "schedule_type"),
    @Index(name = "idx_bell_schedule_active", columnList = "active"),
    @Index(name = "idx_bell_schedule_default", columnList = "is_default")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BellSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Schedule name (e.g., "Regular Schedule", "Early Release", "Block Schedule A")
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Description of this schedule
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Schedule type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;

    /**
     * Academic year this schedule belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    @ToString.Exclude
    private AcademicYear academicYear;

    /**
     * Campus this schedule applies to (null = district-wide)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    @ToString.Exclude
    private Campus campus;

    /**
     * Days of week this schedule applies to (comma-separated)
     * Examples: "MON,TUE,WED,THU,FRI" or "MON,WED,FRI"
     * Default: All weekdays
     */
    @Column(name = "days_of_week", length = 50)
    @Builder.Default
    private String daysOfWeek = "MON,TUE,WED,THU,FRI";

    /**
     * Specific dates this schedule applies to (for special events)
     * Overrides day-of-week settings if populated
     */
    @ElementCollection
    @CollectionTable(name = "bell_schedule_dates",
                     joinColumns = @JoinColumn(name = "bell_schedule_id"))
    @Column(name = "schedule_date")
    @Builder.Default
    private List<LocalDate> specificDates = new ArrayList<>();

    /**
     * Period timers that make up this schedule
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bell_schedule_id")
    @OrderBy("startTime ASC")
    @Builder.Default
    private List<PeriodTimer> periods = new ArrayList<>();

    /**
     * Is this the default schedule for the campus/district?
     */
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    /**
     * Is this schedule currently active?
     */
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    /**
     * Total instructional minutes per day
     */
    @Column(name = "total_instructional_minutes")
    private Integer totalInstructionalMinutes;

    /**
     * Record creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Created by user
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /**
     * Last updated by user
     */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ========================================================================
    // JPA LIFECYCLE CALLBACKS
    // ========================================================================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateTotalInstructionalMinutes();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalInstructionalMinutes();
    }

    // ========================================================================
    // BUSINESS LOGIC METHODS
    // ========================================================================

    /**
     * Check if this schedule applies to a specific date
     */
    public boolean appliesTo(LocalDate date) {
        if (!active) {
            return false;
        }

        // Specific dates override day-of-week settings
        if (specificDates != null && !specificDates.isEmpty()) {
            return specificDates.contains(date);
        }

        // Check day of week
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return true; // No restriction
        }

        String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3).toUpperCase();
        return daysOfWeek.contains(dayOfWeek);
    }

    /**
     * Add a period to this schedule
     */
    public void addPeriod(PeriodTimer period) {
        if (this.periods == null) {
            this.periods = new ArrayList<>();
        }
        this.periods.add(period);
        calculateTotalInstructionalMinutes();
    }

    /**
     * Remove a period from this schedule
     */
    public void removePeriod(PeriodTimer period) {
        if (this.periods != null) {
            this.periods.remove(period);
            calculateTotalInstructionalMinutes();
        }
    }

    /**
     * Calculate total instructional minutes
     */
    public void calculateTotalInstructionalMinutes() {
        if (this.periods == null || this.periods.isEmpty()) {
            this.totalInstructionalMinutes = 0;
            return;
        }

        this.totalInstructionalMinutes = periods.stream()
                .filter(p -> p.getActive())
                .mapToInt(PeriodTimer::getDurationMinutes)
                .sum();
    }

    /**
     * Get number of periods in this schedule
     */
    public int getPeriodCount() {
        return periods != null ? periods.size() : 0;
    }

    /**
     * Get active periods only
     */
    public List<PeriodTimer> getActivePeriods() {
        if (periods == null) {
            return new ArrayList<>();
        }
        return periods.stream()
                .filter(p -> p.getActive())
                .collect(Collectors.toList());
    }

    /**
     * Validate that this schedule is complete and valid
     */
    public boolean isComplete() {
        if (periods == null || periods.isEmpty()) {
            return false;
        }

        // Check that all periods have valid times
        for (PeriodTimer period : periods) {
            if (period.getStartTime() == null || period.getEndTime() == null) {
                return false;
            }
            if (!period.getStartTime().isBefore(period.getEndTime())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if periods overlap
     */
    public boolean hasOverlappingPeriods() {
        if (periods == null || periods.size() < 2) {
            return false;
        }

        List<PeriodTimer> activePeriods = getActivePeriods();
        for (int i = 0; i < activePeriods.size(); i++) {
            for (int j = i + 1; j < activePeriods.size(); j++) {
                PeriodTimer p1 = activePeriods.get(i);
                PeriodTimer p2 = activePeriods.get(j);

                // Check if periods overlap
                if (!(p1.getEndTime().isBefore(p2.getStartTime()) ||
                      p1.getEndTime().equals(p2.getStartTime()) ||
                      p2.getEndTime().isBefore(p1.getStartTime()) ||
                      p2.getEndTime().equals(p1.getStartTime()))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Get formatted schedule display
     */
    public String getFormattedSchedule() {
        if (periods == null || periods.isEmpty()) {
            return "No periods defined";
        }

        StringBuilder sb = new StringBuilder();
        for (PeriodTimer period : getActivePeriods()) {
            sb.append(period.getDisplayLabel()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Get display name with type
     */
    public String getDisplayName() {
        return name + " (" + scheduleType.getDisplayName() + ")";
    }

    // ========================================================================
    // ENUMS
    // ========================================================================

    /**
     * Schedule type enumeration
     */
    public enum ScheduleType {
        REGULAR("Regular Schedule"),
        EARLY_RELEASE("Early Release"),
        LATE_START("Late Start"),
        BLOCK_SCHEDULE("Block Schedule"),
        ASSEMBLY("Assembly Schedule"),
        MINIMUM_DAY("Minimum Day"),
        TESTING("Testing Schedule"),
        SHORTENED("Shortened Schedule"),
        MODIFIED("Modified Schedule"),
        SPECIAL_EVENT("Special Event");

        private final String displayName;

        ScheduleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
