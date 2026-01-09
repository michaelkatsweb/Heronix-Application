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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * School Calendar Entity
 * Defines the academic calendar including holidays, breaks, and special events
 *
 * Features:
 * - Academic year boundaries
 * - Holiday tracking
 * - Break periods (Spring Break, Winter Break, etc.)
 * - Special event days (Teacher In-Service, Testing Days, etc.)
 * - Instructional day calculations
 * - Multi-campus support
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Entity
@Table(name = "school_calendars", indexes = {
    @Index(name = "idx_school_calendar_year", columnList = "academic_year"),
    @Index(name = "idx_school_calendar_campus", columnList = "campus_id"),
    @Index(name = "idx_school_calendar_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Academic year (e.g., "2024-2025")
     */
    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    /**
     * School year start date (first day of school)
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * School year end date (last day of school)
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Campus this calendar applies to (null = district-wide)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    @ToString.Exclude
    private Campus campus;

    /**
     * Calendar events (holidays, breaks, special events)
     */
    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CalendarEvent> events = new ArrayList<>();

    /**
     * Instructional days per week (default: 5 for Mon-Fri)
     */
    @Column(name = "instructional_days_per_week")
    @Builder.Default
    private Integer instructionalDaysPerWeek = 5;

    /**
     * Total planned instructional days for the year
     */
    @Column(name = "total_instructional_days")
    private Integer totalInstructionalDays;

    /**
     * Is this calendar currently active?
     */
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

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
        if (this.totalInstructionalDays == null) {
            calculateInstructionalDays();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========================================================================
    // BUSINESS LOGIC METHODS
    // ========================================================================

    /**
     * Add an event to the calendar
     */
    public void addEvent(CalendarEvent event) {
        if (this.events == null) {
            this.events = new ArrayList<>();
        }
        event.setCalendar(this);
        this.events.add(event);
    }

    /**
     * Remove an event from the calendar
     */
    public void removeEvent(CalendarEvent event) {
        if (this.events != null) {
            this.events.remove(event);
            event.setCalendar(null);
        }
    }

    /**
     * Get all holidays
     */
    public List<CalendarEvent> getHolidays() {
        if (events == null) {
            return new ArrayList<>();
        }
        return events.stream()
                .filter(e -> e.getEventType() == CalendarEvent.EventType.HOLIDAY)
                .collect(Collectors.toList());
    }

    /**
     * Get all break periods
     */
    public List<CalendarEvent> getBreaks() {
        if (events == null) {
            return new ArrayList<>();
        }
        return events.stream()
                .filter(e -> e.getEventType() == CalendarEvent.EventType.BREAK)
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific date is a school day
     */
    public boolean isSchoolDay(LocalDate date) {
        // Check if date is within school year
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            return false;
        }

        // Check if it's a weekend (unless configured otherwise)
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (instructionalDaysPerWeek == 5) {
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                return false;
            }
        }

        // Check if it's a holiday or non-instructional day
        if (events != null) {
            for (CalendarEvent event : events) {
                if (event.getNonInstructionalDay() && event.occursOn(date)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Calculate total instructional days between two dates
     */
    public int getInstructionalDaysBetween(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            return 0;
        }

        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (isSchoolDay(current)) {
                count++;
            }
            current = current.plusDays(1);
        }

        return count;
    }

    /**
     * Calculate total instructional days for the entire school year
     */
    public void calculateInstructionalDays() {
        this.totalInstructionalDays = getInstructionalDaysBetween(startDate, endDate);
    }

    /**
     * Get events occurring on a specific date
     */
    public List<CalendarEvent> getEventsOnDate(LocalDate date) {
        if (events == null) {
            return new ArrayList<>();
        }
        return events.stream()
                .filter(e -> e.occursOn(date))
                .collect(Collectors.toList());
    }

    /**
     * Get events occurring within a date range
     */
    public List<CalendarEvent> getEventsInRange(LocalDate start, LocalDate end) {
        if (events == null) {
            return new ArrayList<>();
        }
        return events.stream()
                .filter(e -> e.occursInRange(start, end))
                .collect(Collectors.toList());
    }

    /**
     * Get total calendar days
     */
    public long getTotalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Check if calendar is currently active (today falls within the year)
     */
    public boolean isCurrentYear() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Validate calendar data
     */
    public boolean isValid() {
        if (startDate == null || endDate == null) {
            return false;
        }
        if (!startDate.isBefore(endDate)) {
            return false;
        }
        if (academicYear == null || academicYear.isEmpty()) {
            return false;
        }
        return true;
    }

    // ========================================================================
    // NESTED ENTITY: CalendarEvent
    // ========================================================================

    /**
     * Calendar Event Entity
     * Represents holidays, breaks, and special events
     */
    @Entity
    @Table(name = "calendar_events", indexes = {
        @Index(name = "idx_calendar_event_date", columnList = "event_date"),
        @Index(name = "idx_calendar_event_type", columnList = "event_type")
    })
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarEvent {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        /**
         * Parent calendar
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "calendar_id", nullable = false)
        @ToString.Exclude
        private SchoolCalendar calendar;

        /**
         * Event name (e.g., "Labor Day", "Spring Break", "Teacher In-Service")
         */
        @Column(name = "event_name", nullable = false, length = 200)
        private String eventName;

        /**
         * Event description
         */
        @Column(name = "description", columnDefinition = "TEXT")
        private String description;

        /**
         * Event type
         */
        @Enumerated(EnumType.STRING)
        @Column(name = "event_type", nullable = false)
        private EventType eventType;

        /**
         * Event date (for single-day events)
         */
        @Column(name = "event_date")
        private LocalDate eventDate;

        /**
         * Start date (for multi-day events)
         */
        @Column(name = "start_date")
        private LocalDate startDate;

        /**
         * End date (for multi-day events)
         */
        @Column(name = "end_date")
        private LocalDate endDate;

        /**
         * Is this a non-instructional day? (students don't attend)
         */
        @Column(name = "non_instructional_day")
        @Builder.Default
        private Boolean nonInstructionalDay = true;

        /**
         * Is this event recurring annually?
         */
        @Column(name = "recurring")
        @Builder.Default
        private Boolean recurring = false;

        /**
         * Record creation timestamp
         */
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
        }

        /**
         * Check if event occurs on a specific date
         */
        public boolean occursOn(LocalDate date) {
            // Single-day event
            if (eventDate != null) {
                return eventDate.equals(date);
            }

            // Multi-day event
            if (startDate != null && endDate != null) {
                return !date.isBefore(startDate) && !date.isAfter(endDate);
            }

            return false;
        }

        /**
         * Check if event occurs within a date range
         */
        public boolean occursInRange(LocalDate start, LocalDate end) {
            // Single-day event
            if (eventDate != null) {
                return !eventDate.isBefore(start) && !eventDate.isAfter(end);
            }

            // Multi-day event - check for any overlap
            if (startDate != null && endDate != null) {
                return !(this.endDate.isBefore(start) || this.startDate.isAfter(end));
            }

            return false;
        }

        /**
         * Get event duration in days
         */
        public long getDurationDays() {
            if (eventDate != null) {
                return 1;
            }
            if (startDate != null && endDate != null) {
                return ChronoUnit.DAYS.between(startDate, endDate) + 1;
            }
            return 0;
        }

        /**
         * Event type enumeration
         */
        public enum EventType {
            HOLIDAY("Holiday"),
            BREAK("Break Period"),
            TEACHER_INSERVICE("Teacher In-Service"),
            TESTING("Testing Day"),
            EARLY_RELEASE("Early Release"),
            LATE_START("Late Start"),
            SPECIAL_EVENT("Special Event"),
            ASSEMBLY("Assembly"),
            PARENT_CONFERENCE("Parent-Teacher Conferences"),
            OTHER("Other");

            private final String displayName;

            EventType(String displayName) {
                this.displayName = displayName;
            }

            public String getDisplayName() {
                return displayName;
            }
        }
    }
}
