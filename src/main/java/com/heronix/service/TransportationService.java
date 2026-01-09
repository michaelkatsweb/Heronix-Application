package com.heronix.service;

import com.heronix.model.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Transportation Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
public interface TransportationService {

    // Route Management
    BusRoute createRoute(BusRoute route);
    BusRoute updateRoute(Long routeId, BusRoute route);
    void deleteRoute(Long routeId);
    BusRoute getRouteById(Long routeId);
    BusRoute getRouteByNumber(String routeNumber);
    List<BusRoute> getAllRoutes();
    List<BusRoute> getActiveRoutes();
    List<BusRoute> getRoutesByYear(String academicYear);
    List<BusRoute> getRoutesByType(BusRoute.RouteType routeType);
    List<BusRoute> getRoutesWithAvailableSeats();
    BusRoute assignDriver(Long routeId, Long driverId);
    BusRoute assignVehicle(Long routeId, Long vehicleId);
    BusRoute updateRouteStatus(Long routeId, BusRoute.RouteStatus status);

    // Stop Management
    BusStop createStop(BusStop stop);
    BusStop updateStop(Long stopId, BusStop stop);
    void deleteStop(Long stopId);
    BusStop getStopById(Long stopId);
    List<BusStop> getStopsByRoute(Long routeId);
    List<BusStop> getAccessibleStops();
    List<BusStop> getStopsByCity(String city);

    // Student Assignment Management
    BusAssignment assignStudentToRoute(Long studentId, Long routeId, Long morningStopId, Long afternoonStopId, String academicYear);
    BusAssignment updateAssignment(Long assignmentId, BusAssignment assignment);
    void removeStudentFromRoute(Long assignmentId);
    BusAssignment approveAssignment(Long assignmentId);
    BusAssignment updateSpecialAccommodations(Long assignmentId, String notes);
    List<BusAssignment> getStudentAssignments(Long studentId);
    List<BusAssignment> getActiveStudentAssignments(Long studentId);
    List<BusAssignment> getRouteAssignments(Long routeId);
    List<BusAssignment> getActiveRouteAssignments(Long routeId);
    List<BusAssignment> getStopAssignments(Long stopId, boolean isMorning);
    List<BusAssignment> getAssignmentsNeedingApproval();
    List<BusAssignment> getAssignmentsWithSpecialAccommodations();

    // Vehicle Management
    Vehicle createVehicle(Vehicle vehicle);
    Vehicle updateVehicle(Long vehicleId, Vehicle vehicle);
    void deleteVehicle(Long vehicleId);
    Vehicle getVehicleById(Long vehicleId);
    Vehicle getVehicleByNumber(String vehicleNumber);
    List<Vehicle> getAllVehicles();
    List<Vehicle> getActiveVehicles();
    List<Vehicle> getAvailableVehicles();
    List<Vehicle> getWheelchairAccessibleVehicles();
    List<Vehicle> getVehiclesNeedingInspection();
    List<Vehicle> getVehiclesNeedingMaintenance();
    List<Vehicle> getVehiclesExpiringSoon();
    Vehicle updateVehicleStatus(Long vehicleId, Vehicle.VehicleStatus status);
    Vehicle recordInspection(Long vehicleId, LocalDate inspectionDate, LocalDate nextDue);
    Vehicle recordMaintenance(Long vehicleId, LocalDate maintenanceDate, LocalDate nextDue, String notes);

    // Reporting and Statistics
    Map<String, Object> getTransportationOverview(String academicYear);
    Map<String, Object> getRouteStatistics(Long routeId);
    Map<String, Object> getVehicleStatistics(Long vehicleId);
    List<Map<String, Object>> getRouteUtilization(String academicYear);
}
