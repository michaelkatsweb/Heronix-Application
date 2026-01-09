package com.heronix.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Vehicle entity - represents school buses and transportation vehicles
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @Column(length = 20)
    private String licensePlate;

    @Column(length = 20)
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleType vehicleType;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    @Column(name = "vehicle_year")
    private Integer year;

    @Column
    private String color;

    @Column
    private Integer capacity;

    @Column
    private Integer wheelchairCapacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column
    private Integer odometer;

    @Column
    private LocalDate purchaseDate;

    @Column
    private LocalDate lastInspectionDate;

    @Column
    private LocalDate nextInspectionDue;

    @Column
    private LocalDate lastMaintenanceDate;

    @Column
    private LocalDate nextMaintenanceDue;

    @Column
    private LocalDate registrationExpiration;

    @Column
    private LocalDate insuranceExpiration;

    @Column
    private Boolean hasAirConditioning;

    @Column
    private Boolean hasWheelchairLift;

    @Column
    private Boolean hasSecurityCameras;

    @Column
    private Boolean hasGPS;

    @Column(columnDefinition = "TEXT")
    private String equipmentList;

    @Column(columnDefinition = "TEXT")
    private String maintenanceNotes;

    @Column
    private Boolean active;

    public enum VehicleType {
        FULL_SIZE_BUS, MINI_BUS, VAN, ACTIVITY_BUS, SPECIAL_NEEDS, UTILITY
    }

    public enum VehicleStatus {
        AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE, RETIRED
    }

    @PrePersist
    @PreUpdate
    private void validate() {
        if (active == null) active = true;
        if (hasAirConditioning == null) hasAirConditioning = false;
        if (hasWheelchairLift == null) hasWheelchairLift = false;
        if (hasSecurityCameras == null) hasSecurityCameras = false;
        if (hasGPS == null) hasGPS = false;
        if (wheelchairCapacity == null) wheelchairCapacity = 0;
    }

    public boolean needsInspection() {
        return nextInspectionDue != null && nextInspectionDue.isBefore(LocalDate.now().plusDays(30));
    }

    public boolean needsMaintenance() {
        return nextMaintenanceDue != null && nextMaintenanceDue.isBefore(LocalDate.now().plusDays(14));
    }

    public boolean isExpiringSoon() {
        LocalDate soon = LocalDate.now().plusDays(30);
        return (registrationExpiration != null && registrationExpiration.isBefore(soon)) ||
               (insuranceExpiration != null && insuranceExpiration.isBefore(soon));
    }
}
