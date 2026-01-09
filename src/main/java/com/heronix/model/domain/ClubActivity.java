package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Club Activity entity - represents club meetings, events, and activities
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "club_activities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType activityType;

    @Column(nullable = false, length = 100)
    private String activityName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalDateTime activityDate;

    @Column
    private LocalDateTime endDate;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityStatus status;

    @Column
    private Integer expectedAttendance;

    @Column
    private Integer actualAttendance;

    @Column
    private Boolean mandatory;

    @Column
    private Boolean openToNonMembers;

    @Column
    private Integer serviceHoursAwarded;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String outcomes;

    @Column
    private Boolean cancelled;

    @Column(length = 200)
    private String cancellationReason;

    public enum ActivityType {
        MEETING, EVENT, SERVICE_PROJECT, FUNDRAISER, COMPETITION,
        WORKSHOP, FIELD_TRIP, SOCIAL, RECOGNITION, OTHER
    }

    public enum ActivityStatus {
        PLANNED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, POSTPONED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (mandatory == null) mandatory = false;
        if (openToNonMembers == null) openToNonMembers = false;
        if (cancelled == null) cancelled = false;
        if (serviceHoursAwarded == null) serviceHoursAwarded = 0;
    }

    public boolean isUpcoming() {
        return activityDate != null && activityDate.isAfter(LocalDateTime.now()) &&
               status != ActivityStatus.CANCELLED && status != ActivityStatus.COMPLETED;
    }

    public boolean isPast() {
        return activityDate != null && activityDate.isBefore(LocalDateTime.now());
    }
}
