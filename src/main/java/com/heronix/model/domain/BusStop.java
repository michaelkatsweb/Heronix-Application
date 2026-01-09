package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Bus Stop entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "bus_stops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private BusRoute route;

    @Column(nullable = false, length = 100)
    private String stopName;

    @Column(nullable = false)
    private Integer stopOrder;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String state;

    @Column(length = 20)
    private String zipCode;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private LocalTime scheduledTime;

    @Column
    private Integer estimatedWaitMinutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StopType stopType;

    @Column
    private Boolean accessible;

    @Column(columnDefinition = "TEXT")
    private String landmarks;

    @Column(columnDefinition = "TEXT")
    private String safetyNotes;

    @Column
    private Boolean active;

    public enum StopType {
        RESIDENTIAL, SCHOOL, PARK_AND_RIDE, SPECIAL_NEEDS, EMERGENCY
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (active == null) active = true;
        if (accessible == null) accessible = true;
        if (estimatedWaitMinutes == null) estimatedWaitMinutes = 5;
    }
}
