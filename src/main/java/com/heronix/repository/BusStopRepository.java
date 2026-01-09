package com.heronix.repository;

import com.heronix.model.domain.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BusStop entity
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 28, 2025
 */
@Repository
public interface BusStopRepository extends JpaRepository<BusStop, Long> {

    List<BusStop> findByRouteId(Long routeId);

    List<BusStop> findByRouteIdOrderByStopOrderAsc(Long routeId);

    List<BusStop> findByActiveTrue();

    List<BusStop> findByStopType(BusStop.StopType stopType);

    @Query("SELECT s FROM BusStop s WHERE s.route.id = :routeId AND s.active = true ORDER BY s.stopOrder")
    List<BusStop> findActiveStopsByRoute(@Param("routeId") Long routeId);

    @Query("SELECT s FROM BusStop s WHERE s.accessible = true AND s.active = true ORDER BY s.stopName")
    List<BusStop> findAccessibleStops();

    @Query("SELECT s FROM BusStop s WHERE LOWER(s.city) = LOWER(:city) AND s.active = true ORDER BY s.stopName")
    List<BusStop> findStopsByCity(@Param("city") String city);

    @Query("SELECT COUNT(s) FROM BusStop s WHERE s.route.id = :routeId AND s.active = true")
    long countStopsByRoute(@Param("routeId") Long routeId);
}
