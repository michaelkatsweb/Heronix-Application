package com.heronix.repository;

import com.heronix.model.domain.DismissalEvent;
import com.heronix.model.domain.DismissalEvent.DismissalEventStatus;
import com.heronix.model.domain.DismissalEvent.DismissalEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DismissalEventRepository extends JpaRepository<DismissalEvent, Long> {

    List<DismissalEvent> findByEventDateOrderByArrivalTimeDesc(LocalDate date);

    List<DismissalEvent> findByEventDateAndEventTypeOrderByArrivalTimeDesc(LocalDate date, DismissalEventType eventType);

    List<DismissalEvent> findByEventDateAndStatusOrderByArrivalTimeDesc(LocalDate date, DismissalEventStatus status);

    List<DismissalEvent> findByEventDateAndStudentId(LocalDate date, Long studentId);

    long countByEventDateAndEventType(LocalDate date, DismissalEventType eventType);

    long countByEventDateAndStatus(LocalDate date, DismissalEventStatus status);

    @Query("SELECT de FROM DismissalEvent de LEFT JOIN FETCH de.busRoute LEFT JOIN FETCH de.student " +
           "WHERE de.eventDate = :date ORDER BY de.arrivalTime DESC")
    List<DismissalEvent> findByEventDateWithDetails(LocalDate date);

    @Query("SELECT de FROM DismissalEvent de LEFT JOIN FETCH de.busRoute " +
           "WHERE de.eventDate = :date AND de.eventType = 'BUS_ARRIVAL' ORDER BY de.arrivalTime DESC")
    List<DismissalEvent> findTodaysBusArrivals(LocalDate date);

    @Query("SELECT de FROM DismissalEvent de LEFT JOIN FETCH de.student " +
           "WHERE de.eventDate = :date AND de.eventType = 'CAR_PICKUP' ORDER BY de.arrivalTime DESC")
    List<DismissalEvent> findTodaysCarPickups(LocalDate date);

    boolean existsByEventDateAndBusNumber(LocalDate date, String busNumber);
}
