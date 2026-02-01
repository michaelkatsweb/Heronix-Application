package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DismissalEvent entity - tracks real-time bus arrivals and car pickup events
 * for the daily dismissal/arrival monitor board.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 2026
 */
@Entity
@Table(name = "dismissal_events", indexes = {
        @Index(name = "idx_dismissal_event_date", columnList = "eventDate"),
        @Index(name = "idx_dismissal_event_date_type", columnList = "eventDate,eventType"),
        @Index(name = "idx_dismissal_event_student", columnList = "student_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DismissalEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DismissalEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_route_id")
    private BusRoute busRoute;

    @Column(length = 50)
    private String busNumber;

    @Column(length = 100)
    private String vehicleBarcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(length = 200)
    private String studentName;

    @Column(length = 200)
    private String parentName;

    @Column(length = 200)
    private String parentVehicleInfo;

    @Column(length = 100)
    private String sportName;

    @Column(length = 100)
    private String meetingType;

    @Column(length = 200)
    private String counselorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DismissalEventStatus status;

    @Column
    private LocalDateTime arrivalTime;

    @Column
    private LocalDateTime departureTime;

    @Column
    private LocalDateTime calledTime;

    @Column
    private Integer laneNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public enum DismissalEventType {
        BUS_ARRIVAL("Bus Arrival"),
        CAR_PICKUP("Car Pickup"),
        WALKER("Walker"),
        AFTERCARE("Aftercare"),
        ATHLETICS("Athletics"),
        COUNSELOR_SUMMON("Counselor Summon");

        private final String displayName;

        DismissalEventType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum DismissalEventStatus {
        PENDING("Pending"),
        CALLED("Called"),
        ARRIVED("Arrived"),
        DEPARTED("Departed"),
        CANCELLED("Cancelled");

        private final String displayName;

        DismissalEventStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    private void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = DismissalEventStatus.PENDING;
        if (eventDate == null) eventDate = LocalDate.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
