package com.heronix.repository;

import com.heronix.model.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Vehicle entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByActiveTrue();

    List<Vehicle> findByActiveTrueOrderByVehicleNumberAsc();

    List<Vehicle> findByVehicleType(Vehicle.VehicleType vehicleType);

    List<Vehicle> findByStatus(Vehicle.VehicleStatus status);

    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.status = 'AVAILABLE' ORDER BY v.vehicleNumber")
    List<Vehicle> findAvailableVehicles();

    @Query("SELECT v FROM Vehicle v WHERE v.active = true AND v.hasWheelchairLift = true ORDER BY v.vehicleNumber")
    List<Vehicle> findWheelchairAccessibleVehicles();

    @Query("SELECT v FROM Vehicle v WHERE v.nextInspectionDue <= :date AND v.active = true ORDER BY v.nextInspectionDue")
    List<Vehicle> findVehiclesNeedingInspection(@Param("date") LocalDate date);

    @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenanceDue <= :date AND v.active = true ORDER BY v.nextMaintenanceDue")
    List<Vehicle> findVehiclesNeedingMaintenance(@Param("date") LocalDate date);

    @Query("SELECT v FROM Vehicle v WHERE (v.registrationExpiration <= :date OR v.insuranceExpiration <= :date) AND v.active = true")
    List<Vehicle> findVehiclesWithExpiringSoon(@Param("date") LocalDate date);

    @Query("SELECT v FROM Vehicle v WHERE v.status = 'MAINTENANCE' OR v.status = 'OUT_OF_SERVICE'")
    List<Vehicle> findVehiclesOutOfService();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.active = true AND v.status = 'AVAILABLE'")
    long countAvailableVehicles();

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.active = true")
    long countActiveVehicles();
}
