package com.heronix.service;

import com.heronix.model.domain.DismissalEvent;
import com.heronix.model.domain.DismissalEvent.DismissalEventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Dismissal Event Management
 */
public interface DismissalEventService {

    DismissalEvent registerBusArrival(String busNumber, String barcode);

    DismissalEvent registerCarPickup(Long studentId, String parentName, String vehicleInfo);

    DismissalEvent registerWalker(Long studentId);

    DismissalEvent registerAftercare(Long studentId);

    DismissalEvent registerAthletics(Long studentId, String sportName);

    DismissalEvent registerCounselorSummon(Long studentId, String counselorName, String meetingType);

    DismissalEvent callStudent(Long eventId);

    DismissalEvent markDeparted(Long eventId);

    DismissalEvent cancelEvent(Long eventId);

    List<DismissalEvent> getTodaysEvents();

    List<DismissalEvent> getTodaysBusArrivals();

    List<DismissalEvent> getTodaysCarPickups();

    List<DismissalEvent> getEventsByDateAndType(LocalDate date, DismissalEventType type);

    DismissalEvent getStudentDismissalStatus(Long studentId);

    Map<String, Object> getTodaysBoardStats();
}
