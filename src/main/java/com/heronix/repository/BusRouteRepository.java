package com.heronix.repository;

import com.heronix.model.domain.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BusRoute entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface BusRouteRepository extends JpaRepository<BusRoute, Long> {

    Optional<BusRoute> findByRouteNumber(String routeNumber);

    List<BusRoute> findByActiveTrue();

    List<BusRoute> findByActiveTrueOrderByRouteNumberAsc();

    List<BusRoute> findByAcademicYear(String academicYear);

    List<BusRoute> findByAcademicYearAndActiveTrue(String academicYear);

    List<BusRoute> findByRouteType(BusRoute.RouteType routeType);

    List<BusRoute> findByRouteTypeAndActiveTrue(BusRoute.RouteType routeType);

    List<BusRoute> findByStatus(BusRoute.RouteStatus status);

    List<BusRoute> findByDriverId(Long driverId);

    List<BusRoute> findByVehicleId(Long vehicleId);

    @Query("SELECT r FROM BusRoute r WHERE r.active = true AND r.status = 'ACTIVE' ORDER BY r.routeNumber")
    List<BusRoute> findActiveRoutes();

    @Query("SELECT r FROM BusRoute r WHERE r.academicYear = :year AND r.active = true AND r.status = 'ACTIVE' ORDER BY r.routeType, r.routeNumber")
    List<BusRoute> findActiveRoutesByYear(@Param("year") String year);

    @Query("SELECT r FROM BusRoute r WHERE r.active = true AND r.capacity > r.currentOccupancy ORDER BY r.routeNumber")
    List<BusRoute> findRoutesWithAvailableSeats();

    @Query("SELECT r FROM BusRoute r WHERE r.active = true AND r.currentOccupancy >= r.capacity")
    List<BusRoute> findFullRoutes();

    @Query("SELECT COUNT(r) FROM BusRoute r WHERE r.academicYear = :year AND r.active = true")
    long countActiveRoutesByYear(@Param("year") String year);

    @Query("SELECT COALESCE(SUM(r.currentOccupancy), 0) FROM BusRoute r WHERE r.academicYear = :year AND r.active = true")
    int countTotalRiders(@Param("year") String year);
}
