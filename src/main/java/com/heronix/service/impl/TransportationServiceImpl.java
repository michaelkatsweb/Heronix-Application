package com.heronix.service.impl;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import com.heronix.service.TransportationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TransportationService
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Service
@RequiredArgsConstructor
public class TransportationServiceImpl implements TransportationService {

    private final BusRouteRepository routeRepository;
    private final BusStopRepository stopRepository;
    private final BusAssignmentRepository assignmentRepository;
    private final VehicleRepository vehicleRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    // ========== Route Management ==========

    @Override
    @Transactional
    public BusRoute createRoute(BusRoute route) {
        route.setActive(true);
        route.setCurrentOccupancy(0);
        return routeRepository.save(route);
    }

    @Override
    @Transactional
    public BusRoute updateRoute(Long routeId, BusRoute route) {
        BusRoute existing = getRouteById(routeId);
        existing.setRouteNumber(route.getRouteNumber());
        existing.setRouteName(route.getRouteName());
        existing.setRouteType(route.getRouteType());
        existing.setDescription(route.getDescription());
        existing.setDepartureTime(route.getDepartureTime());
        existing.setArrivalTime(route.getArrivalTime());
        existing.setEstimatedDurationMinutes(route.getEstimatedDurationMinutes());
        existing.setDistanceMiles(route.getDistanceMiles());
        existing.setCapacity(route.getCapacity());
        existing.setOperatingDays(route.getOperatingDays());
        existing.setSpecialInstructions(route.getSpecialInstructions());
        return routeRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteRoute(Long routeId) {
        BusRoute route = getRouteById(routeId);
        route.setActive(false);
        routeRepository.save(route);
    }

    @Override
    @Transactional(readOnly = true)
    public BusRoute getRouteById(Long routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id: " + routeId));
    }

    @Override
    @Transactional(readOnly = true)
    public BusRoute getRouteByNumber(String routeNumber) {
        return routeRepository.findByRouteNumber(routeNumber)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with number: " + routeNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusRoute> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusRoute> getActiveRoutes() {
        return routeRepository.findActiveRoutes();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusRoute> getRoutesByYear(String academicYear) {
        return routeRepository.findActiveRoutesByYear(academicYear);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusRoute> getRoutesByType(BusRoute.RouteType routeType) {
        return routeRepository.findByRouteTypeAndActiveTrue(routeType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusRoute> getRoutesWithAvailableSeats() {
        return routeRepository.findRoutesWithAvailableSeats();
    }

    @Override
    @Transactional
    public BusRoute assignDriver(Long routeId, Long driverId) {
        BusRoute route = getRouteById(routeId);
        Teacher driver = teacherRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with id: " + driverId));
        route.setDriver(driver);
        return routeRepository.save(route);
    }

    @Override
    @Transactional
    public BusRoute assignVehicle(Long routeId, Long vehicleId) {
        BusRoute route = getRouteById(routeId);
        Vehicle vehicle = getVehicleById(vehicleId);

        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle is not available for assignment");
        }

        route.setVehicle(vehicle);
        vehicle.setStatus(Vehicle.VehicleStatus.IN_USE);
        vehicleRepository.save(vehicle);

        return routeRepository.save(route);
    }

    @Override
    @Transactional
    public BusRoute updateRouteStatus(Long routeId, BusRoute.RouteStatus status) {
        BusRoute route = getRouteById(routeId);
        route.setStatus(status);
        return routeRepository.save(route);
    }

    // ========== Stop Management ==========

    @Override
    @Transactional
    public BusStop createStop(BusStop stop) {
        stop.setActive(true);
        return stopRepository.save(stop);
    }

    @Override
    @Transactional
    public BusStop updateStop(Long stopId, BusStop stop) {
        BusStop existing = getStopById(stopId);
        existing.setStopName(stop.getStopName());
        existing.setStopOrder(stop.getStopOrder());
        existing.setAddress(stop.getAddress());
        existing.setCity(stop.getCity());
        existing.setState(stop.getState());
        existing.setZipCode(stop.getZipCode());
        existing.setLatitude(stop.getLatitude());
        existing.setLongitude(stop.getLongitude());
        existing.setScheduledTime(stop.getScheduledTime());
        existing.setEstimatedWaitMinutes(stop.getEstimatedWaitMinutes());
        existing.setStopType(stop.getStopType());
        existing.setAccessible(stop.getAccessible());
        existing.setLandmarks(stop.getLandmarks());
        existing.setSafetyNotes(stop.getSafetyNotes());
        return stopRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteStop(Long stopId) {
        BusStop stop = getStopById(stopId);
        stop.setActive(false);
        stopRepository.save(stop);
    }

    @Override
    @Transactional(readOnly = true)
    public BusStop getStopById(Long stopId) {
        return stopRepository.findById(stopId)
                .orElseThrow(() -> new EntityNotFoundException("Stop not found with id: " + stopId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusStop> getStopsByRoute(Long routeId) {
        return stopRepository.findActiveStopsByRoute(routeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusStop> getAccessibleStops() {
        return stopRepository.findAccessibleStops();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusStop> getStopsByCity(String city) {
        return stopRepository.findStopsByCity(city);
    }

    // ========== Student Assignment Management ==========

    @Override
    @Transactional
    public BusAssignment assignStudentToRoute(Long studentId, Long routeId, Long morningStopId,
                                               Long afternoonStopId, String academicYear) {
        BusRoute route = getRouteById(routeId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

        if (route.isFull()) {
            throw new IllegalStateException("Route is at full capacity");
        }

        BusStop morningStop = morningStopId != null ? getStopById(morningStopId) : null;
        BusStop afternoonStop = afternoonStopId != null ? getStopById(afternoonStopId) : null;

        BusAssignment assignment = BusAssignment.builder()
                .student(student)
                .route(route)
                .morningStop(morningStop)
                .afternoonStop(afternoonStop)
                .academicYear(academicYear)
                .status(BusAssignment.AssignmentStatus.PENDING)
                .startDate(LocalDate.now())
                .build();

        BusAssignment saved = assignmentRepository.save(assignment);

        route.setCurrentOccupancy(route.getCurrentOccupancy() + 1);
        routeRepository.save(route);

        return saved;
    }

    @Override
    @Transactional
    public BusAssignment updateAssignment(Long assignmentId, BusAssignment assignment) {
        BusAssignment existing = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        existing.setMorningStop(assignment.getMorningStop());
        existing.setAfternoonStop(assignment.getAfternoonStop());
        existing.setRequiresSpecialAccommodations(assignment.getRequiresSpecialAccommodations());
        existing.setAccommodationNotes(assignment.getAccommodationNotes());
        existing.setEmergencyContactInfo(assignment.getEmergencyContactInfo());
        existing.setNotes(assignment.getNotes());

        return assignmentRepository.save(existing);
    }

    @Override
    @Transactional
    public void removeStudentFromRoute(Long assignmentId) {
        BusAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        assignment.setStatus(BusAssignment.AssignmentStatus.CANCELLED);
        assignment.setEndDate(LocalDate.now());
        assignmentRepository.save(assignment);

        BusRoute route = assignment.getRoute();
        route.setCurrentOccupancy(Math.max(0, route.getCurrentOccupancy() - 1));
        routeRepository.save(route);
    }

    @Override
    @Transactional
    public BusAssignment approveAssignment(Long assignmentId) {
        BusAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        assignment.setStatus(BusAssignment.AssignmentStatus.ACTIVE);
        assignment.setParentApprovalReceived(true);
        assignment.setApprovalDate(LocalDate.now());

        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public BusAssignment updateSpecialAccommodations(Long assignmentId, String notes) {
        BusAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found with id: " + assignmentId));

        assignment.setRequiresSpecialAccommodations(true);
        assignment.setAccommodationNotes(notes);

        return assignmentRepository.save(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getStudentAssignments(Long studentId) {
        return assignmentRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getActiveStudentAssignments(Long studentId) {
        return assignmentRepository.findActiveByStudent(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getRouteAssignments(Long routeId) {
        return assignmentRepository.findByRouteId(routeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getActiveRouteAssignments(Long routeId) {
        return assignmentRepository.findActiveByRoute(routeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getStopAssignments(Long stopId, boolean isMorning) {
        if (isMorning) {
            return assignmentRepository.findActiveByMorningStop(stopId);
        } else {
            return assignmentRepository.findActiveByAfternoonStop(stopId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getAssignmentsNeedingApproval() {
        return assignmentRepository.findPendingApprovals();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusAssignment> getAssignmentsWithSpecialAccommodations() {
        return assignmentRepository.findActiveWithSpecialAccommodations();
    }

    // ========== Vehicle Management ==========

    @Override
    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        vehicle.setActive(true);
        vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Long vehicleId, Vehicle vehicle) {
        Vehicle existing = getVehicleById(vehicleId);
        existing.setVehicleNumber(vehicle.getVehicleNumber());
        existing.setLicensePlate(vehicle.getLicensePlate());
        existing.setVin(vehicle.getVin());
        existing.setVehicleType(vehicle.getVehicleType());
        existing.setMake(vehicle.getMake());
        existing.setModel(vehicle.getModel());
        existing.setYear(vehicle.getYear());
        existing.setColor(vehicle.getColor());
        existing.setCapacity(vehicle.getCapacity());
        existing.setWheelchairCapacity(vehicle.getWheelchairCapacity());
        existing.setHasAirConditioning(vehicle.getHasAirConditioning());
        existing.setHasWheelchairLift(vehicle.getHasWheelchairLift());
        existing.setHasSecurityCameras(vehicle.getHasSecurityCameras());
        existing.setHasGPS(vehicle.getHasGPS());
        existing.setEquipmentList(vehicle.getEquipmentList());
        return vehicleRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicle.setActive(false);
        vehicle.setStatus(Vehicle.VehicleStatus.RETIRED);
        vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));
    }

    @Override
    @Transactional(readOnly = true)
    public Vehicle getVehicleByNumber(String vehicleNumber) {
        return vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with number: " + vehicleNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getActiveVehicles() {
        return vehicleRepository.findByActiveTrueOrderByVehicleNumberAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAvailableVehicles();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getWheelchairAccessibleVehicles() {
        return vehicleRepository.findWheelchairAccessibleVehicles();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesNeedingInspection() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return vehicleRepository.findVehiclesNeedingInspection(thirtyDaysFromNow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesNeedingMaintenance() {
        LocalDate fourteenDaysFromNow = LocalDate.now().plusDays(14);
        return vehicleRepository.findVehiclesNeedingMaintenance(fourteenDaysFromNow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesExpiringSoon() {
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return vehicleRepository.findVehiclesWithExpiringSoon(thirtyDaysFromNow);
    }

    @Override
    @Transactional
    public Vehicle updateVehicleStatus(Long vehicleId, Vehicle.VehicleStatus status) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicle.setStatus(status);
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle recordInspection(Long vehicleId, LocalDate inspectionDate, LocalDate nextDue) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicle.setLastInspectionDate(inspectionDate);
        vehicle.setNextInspectionDue(nextDue);
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle recordMaintenance(Long vehicleId, LocalDate maintenanceDate, LocalDate nextDue, String notes) {
        Vehicle vehicle = getVehicleById(vehicleId);
        vehicle.setLastMaintenanceDate(maintenanceDate);
        vehicle.setNextMaintenanceDue(nextDue);

        String existingNotes = vehicle.getMaintenanceNotes();
        String updatedNotes = existingNotes != null ?
            existingNotes + "\n\n" + maintenanceDate + ": " + notes :
            maintenanceDate + ": " + notes;
        vehicle.setMaintenanceNotes(updatedNotes);

        return vehicleRepository.save(vehicle);
    }

    // ========== Reporting and Statistics ==========

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTransportationOverview(String academicYear) {
        Map<String, Object> overview = new HashMap<>();

        long totalRoutes = routeRepository.countActiveRoutesByYear(academicYear);
        int totalRiders = routeRepository.countTotalRiders(academicYear);
        long totalVehicles = vehicleRepository.countActiveVehicles();
        long availableVehicles = vehicleRepository.countAvailableVehicles();

        overview.put("academicYear", academicYear);
        overview.put("totalRoutes", totalRoutes);
        overview.put("totalRiders", totalRiders);
        overview.put("totalVehicles", totalVehicles);
        overview.put("availableVehicles", availableVehicles);

        List<Vehicle> needingInspection = getVehiclesNeedingInspection();
        List<Vehicle> needingMaintenance = getVehiclesNeedingMaintenance();
        overview.put("vehiclesNeedingInspection", needingInspection.size());
        overview.put("vehiclesNeedingMaintenance", needingMaintenance.size());

        List<BusAssignment> pendingApprovals = getAssignmentsNeedingApproval();
        overview.put("pendingApprovals", pendingApprovals.size());

        return overview;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRouteStatistics(Long routeId) {
        BusRoute route = getRouteById(routeId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("routeId", routeId);
        stats.put("routeNumber", route.getRouteNumber());
        stats.put("routeName", route.getRouteName());
        stats.put("routeType", route.getRouteType());
        stats.put("capacity", route.getCapacity());
        stats.put("currentOccupancy", route.getCurrentOccupancy());
        stats.put("availableSeats", route.getAvailableSeats());
        stats.put("occupancyPercentage", route.getOccupancyPercentage());

        long stopCount = stopRepository.countStopsByRoute(routeId);
        stats.put("numberOfStops", stopCount);

        long activeAssignments = assignmentRepository.countActiveByRoute(routeId);
        stats.put("activeAssignments", activeAssignments);

        stats.put("status", route.getStatus());
        stats.put("departureTime", route.getDepartureTime());
        stats.put("arrivalTime", route.getArrivalTime());
        stats.put("estimatedDuration", route.getEstimatedDurationMinutes());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getVehicleStatistics(Long vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        Map<String, Object> stats = new HashMap<>();

        stats.put("vehicleId", vehicleId);
        stats.put("vehicleNumber", vehicle.getVehicleNumber());
        stats.put("vehicleType", vehicle.getVehicleType());
        stats.put("make", vehicle.getMake());
        stats.put("model", vehicle.getModel());
        stats.put("year", vehicle.getYear());
        stats.put("capacity", vehicle.getCapacity());
        stats.put("status", vehicle.getStatus());
        stats.put("odometer", vehicle.getOdometer());
        stats.put("needsInspection", vehicle.needsInspection());
        stats.put("needsMaintenance", vehicle.needsMaintenance());
        stats.put("isExpiringSoon", vehicle.isExpiringSoon());
        stats.put("nextInspectionDue", vehicle.getNextInspectionDue());
        stats.put("nextMaintenanceDue", vehicle.getNextMaintenanceDue());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRouteUtilization(String academicYear) {
        return routeRepository.findActiveRoutesByYear(academicYear).stream()
                .map(route -> {
                    Map<String, Object> utilization = new HashMap<>();
                    utilization.put("routeNumber", route.getRouteNumber());
                    utilization.put("routeName", route.getRouteName());
                    utilization.put("capacity", route.getCapacity());
                    utilization.put("currentOccupancy", route.getCurrentOccupancy());
                    utilization.put("availableSeats", route.getAvailableSeats());
                    utilization.put("occupancyPercentage", route.getOccupancyPercentage());
                    return utilization;
                })
                .collect(Collectors.toList());
    }
}
