package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Bus Route entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "bus_routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String routeNumber;

    @Column(nullable = false, length = 100)
    private String routeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteType routeType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Teacher driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_driver_id")
    private Teacher backupDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column
    private LocalTime departureTime;

    @Column
    private LocalTime arrivalTime;

    @Column
    private Integer estimatedDurationMinutes;

    @Column
    private Double distanceMiles;

    @Column
    private Integer capacity;

    @Column
    private Integer currentOccupancy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteStatus status;

    @Column
    private Boolean active;

    @Column(length = 50)
    private String academicYear;

    @Column(columnDefinition = "TEXT")
    private String operatingDays;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    @Builder.Default
    private Set<BusStop> stops = new HashSet<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<BusAssignment> assignments = new HashSet<>();

    public enum RouteType {
        MORNING, AFTERNOON, SPECIAL_NEEDS, FIELD_TRIP, ATHLETIC, ACTIVITY, EMERGENCY
    }

    public enum RouteStatus {
        ACTIVE, INACTIVE, DELAYED, CANCELLED, COMPLETED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (active == null) active = true;
        if (currentOccupancy == null) currentOccupancy = 0;
    }

    public boolean isFull() {
        return capacity != null && currentOccupancy >= capacity;
    }

    public Integer getAvailableSeats() {
        if (capacity == null) return null;
        return Math.max(0, capacity - currentOccupancy);
    }

    public Double getOccupancyPercentage() {
        if (capacity == null || capacity == 0) return 0.0;
        return (currentOccupancy * 100.0) / capacity;
    }
}
