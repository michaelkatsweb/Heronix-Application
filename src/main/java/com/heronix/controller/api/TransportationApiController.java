package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.service.TransportationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Transportation Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@RestController
@RequestMapping("/api/transportation")
@RequiredArgsConstructor
public class TransportationApiController {

    private final TransportationService transportationService;

    // ========== Route Management ==========

    @PostMapping("/routes")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusRoute> createRoute(@RequestBody BusRoute route) {
        BusRoute created = transportationService.createRoute(route);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/routes/{routeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusRoute> updateRoute(
            @PathVariable Long routeId,
            @RequestBody BusRoute route) {
        BusRoute updated = transportationService.updateRoute(routeId, route);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/routes/{routeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long routeId) {
        transportationService.deleteRoute(routeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/routes/{routeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<BusRoute> getRouteById(@PathVariable Long routeId) {
        BusRoute route = transportationService.getRouteById(routeId);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/routes/number/{routeNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<BusRoute> getRouteByNumber(@PathVariable String routeNumber) {
        BusRoute route = transportationService.getRouteByNumber(routeNumber);
        return ResponseEntity.ok(route);
    }

    @GetMapping("/routes")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusRoute>> getAllRoutes() {
        List<BusRoute> routes = transportationService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusRoute>> getActiveRoutes() {
        List<BusRoute> routes = transportationService.getActiveRoutes();
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/year/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusRoute>> getRoutesByYear(@PathVariable String academicYear) {
        List<BusRoute> routes = transportationService.getRoutesByYear(academicYear);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/type/{routeType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusRoute>> getRoutesByType(@PathVariable BusRoute.RouteType routeType) {
        List<BusRoute> routes = transportationService.getRoutesByType(routeType);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/routes/available-seats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusRoute>> getRoutesWithAvailableSeats() {
        List<BusRoute> routes = transportationService.getRoutesWithAvailableSeats();
        return ResponseEntity.ok(routes);
    }

    @PostMapping("/routes/{routeId}/assign-driver")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusRoute> assignDriver(
            @PathVariable Long routeId,
            @RequestParam Long driverId) {
        BusRoute updated = transportationService.assignDriver(routeId, driverId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/routes/{routeId}/assign-vehicle")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusRoute> assignVehicle(
            @PathVariable Long routeId,
            @RequestParam Long vehicleId) {
        BusRoute updated = transportationService.assignVehicle(routeId, vehicleId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/routes/{routeId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusRoute> updateRouteStatus(
            @PathVariable Long routeId,
            @RequestParam BusRoute.RouteStatus status) {
        BusRoute updated = transportationService.updateRouteStatus(routeId, status);
        return ResponseEntity.ok(updated);
    }

    // ========== Stop Management ==========

    @PostMapping("/stops")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusStop> createStop(@RequestBody BusStop stop) {
        BusStop created = transportationService.createStop(stop);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/stops/{stopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusStop> updateStop(
            @PathVariable Long stopId,
            @RequestBody BusStop stop) {
        BusStop updated = transportationService.updateStop(stopId, stop);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/stops/{stopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Void> deleteStop(@PathVariable Long stopId) {
        transportationService.deleteStop(stopId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stops/{stopId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<BusStop> getStopById(@PathVariable Long stopId) {
        BusStop stop = transportationService.getStopById(stopId);
        return ResponseEntity.ok(stop);
    }

    @GetMapping("/routes/{routeId}/stops")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusStop>> getStopsByRoute(@PathVariable Long routeId) {
        List<BusStop> stops = transportationService.getStopsByRoute(routeId);
        return ResponseEntity.ok(stops);
    }

    @GetMapping("/stops/accessible")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusStop>> getAccessibleStops() {
        List<BusStop> stops = transportationService.getAccessibleStops();
        return ResponseEntity.ok(stops);
    }

    @GetMapping("/stops/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusStop>> getStopsByCity(@PathVariable String city) {
        List<BusStop> stops = transportationService.getStopsByCity(city);
        return ResponseEntity.ok(stops);
    }

    // ========== Student Assignment Management ==========

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'PARENT')")
    public ResponseEntity<BusAssignment> assignStudentToRoute(
            @RequestParam Long studentId,
            @RequestParam Long routeId,
            @RequestParam(required = false) Long morningStopId,
            @RequestParam(required = false) Long afternoonStopId,
            @RequestParam String academicYear) {
        BusAssignment assignment = transportationService.assignStudentToRoute(
                studentId, routeId, morningStopId, afternoonStopId, academicYear);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

    @PutMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'PARENT')")
    public ResponseEntity<BusAssignment> updateAssignment(
            @PathVariable Long assignmentId,
            @RequestBody BusAssignment assignment) {
        BusAssignment updated = transportationService.updateAssignment(assignmentId, assignment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'PARENT')")
    public ResponseEntity<Void> removeStudentFromRoute(@PathVariable Long assignmentId) {
        transportationService.removeStudentFromRoute(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assignments/{assignmentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusAssignment> approveAssignment(@PathVariable Long assignmentId) {
        BusAssignment approved = transportationService.approveAssignment(assignmentId);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/assignments/{assignmentId}/accommodations")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<BusAssignment> updateSpecialAccommodations(
            @PathVariable Long assignmentId,
            @RequestParam String notes) {
        BusAssignment updated = transportationService.updateSpecialAccommodations(assignmentId, notes);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/students/{studentId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusAssignment>> getStudentAssignments(@PathVariable Long studentId) {
        List<BusAssignment> assignments = transportationService.getStudentAssignments(studentId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/students/{studentId}/assignments/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<List<BusAssignment>> getActiveStudentAssignments(@PathVariable Long studentId) {
        List<BusAssignment> assignments = transportationService.getActiveStudentAssignments(studentId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/routes/{routeId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<BusAssignment>> getRouteAssignments(@PathVariable Long routeId) {
        List<BusAssignment> assignments = transportationService.getRouteAssignments(routeId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/routes/{routeId}/assignments/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<BusAssignment>> getActiveRouteAssignments(@PathVariable Long routeId) {
        List<BusAssignment> assignments = transportationService.getActiveRouteAssignments(routeId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/stops/{stopId}/assignments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<BusAssignment>> getStopAssignments(
            @PathVariable Long stopId,
            @RequestParam boolean isMorning) {
        List<BusAssignment> assignments = transportationService.getStopAssignments(stopId, isMorning);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<List<BusAssignment>> getAssignmentsNeedingApproval() {
        List<BusAssignment> assignments = transportationService.getAssignmentsNeedingApproval();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/special-accommodations")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<List<BusAssignment>> getAssignmentsWithSpecialAccommodations() {
        List<BusAssignment> assignments = transportationService.getAssignmentsWithSpecialAccommodations();
        return ResponseEntity.ok(assignments);
    }

    // ========== Vehicle Management ==========

    @PostMapping("/vehicles")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle created = transportationService.createVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long vehicleId,
            @RequestBody Vehicle vehicle) {
        Vehicle updated = transportationService.updateVehicle(vehicleId, vehicle);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId) {
        transportationService.deleteVehicle(vehicleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{vehicleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long vehicleId) {
        Vehicle vehicle = transportationService.getVehicleById(vehicleId);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/vehicles/number/{vehicleNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<Vehicle> getVehicleByNumber(@PathVariable String vehicleNumber) {
        Vehicle vehicle = transportationService.getVehicleByNumber(vehicleNumber);
        return ResponseEntity.ok(vehicle);
    }

    @GetMapping("/vehicles")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = transportationService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<Vehicle>> getActiveVehicles() {
        List<Vehicle> vehicles = transportationService.getActiveVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        List<Vehicle> vehicles = transportationService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/wheelchair-accessible")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<List<Vehicle>> getWheelchairAccessibleVehicles() {
        List<Vehicle> vehicles = transportationService.getWheelchairAccessibleVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/needs-inspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<List<Vehicle>> getVehiclesNeedingInspection() {
        List<Vehicle> vehicles = transportationService.getVehiclesNeedingInspection();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/needs-maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<List<Vehicle>> getVehiclesNeedingMaintenance() {
        List<Vehicle> vehicles = transportationService.getVehiclesNeedingMaintenance();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/vehicles/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<List<Vehicle>> getVehiclesExpiringSoon() {
        List<Vehicle> vehicles = transportationService.getVehiclesExpiringSoon();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/vehicles/{vehicleId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Vehicle> updateVehicleStatus(
            @PathVariable Long vehicleId,
            @RequestParam Vehicle.VehicleStatus status) {
        Vehicle updated = transportationService.updateVehicleStatus(vehicleId, status);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/vehicles/{vehicleId}/inspection")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Vehicle> recordInspection(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inspectionDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextDue) {
        Vehicle updated = transportationService.recordInspection(vehicleId, inspectionDate, nextDue);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/vehicles/{vehicleId}/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Vehicle> recordMaintenance(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maintenanceDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextDue,
            @RequestParam String notes) {
        Vehicle updated = transportationService.recordMaintenance(vehicleId, maintenanceDate, nextDue, notes);
        return ResponseEntity.ok(updated);
    }

    // ========== Reporting and Statistics ==========

    @GetMapping("/reports/overview/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Map<String, Object>> getTransportationOverview(@PathVariable String academicYear) {
        Map<String, Object> overview = transportationService.getTransportationOverview(academicYear);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/routes/{routeId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getRouteStatistics(@PathVariable Long routeId) {
        Map<String, Object> stats = transportationService.getRouteStatistics(routeId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/vehicles/{vehicleId}/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<Map<String, Object>> getVehicleStatistics(@PathVariable Long vehicleId) {
        Map<String, Object> stats = transportationService.getVehicleStatistics(vehicleId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/route-utilization/{academicYear}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSPORTATION_DIRECTOR')")
    public ResponseEntity<List<Map<String, Object>>> getRouteUtilization(@PathVariable String academicYear) {
        List<Map<String, Object>> utilization = transportationService.getRouteUtilization(academicYear);
        return ResponseEntity.ok(utilization);
    }
}
