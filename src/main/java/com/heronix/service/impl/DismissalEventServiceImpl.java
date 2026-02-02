package com.heronix.service.impl;

import com.heronix.model.domain.BusRoute;
import com.heronix.model.domain.DismissalEvent;
import com.heronix.model.domain.DismissalEvent.DismissalEventStatus;
import com.heronix.model.domain.DismissalEvent.DismissalEventType;
import com.heronix.model.domain.Student;
import com.heronix.repository.BusRouteRepository;
import com.heronix.repository.DismissalEventRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.DismissalEventService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DismissalEventServiceImpl implements DismissalEventService {

    private final DismissalEventRepository dismissalEventRepository;
    private final BusRouteRepository busRouteRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public DismissalEvent registerBusArrival(String busNumber, String barcode) {
        LocalDate today = LocalDate.now();

        if (dismissalEventRepository.existsByEventDateAndBusNumber(today, busNumber)) {
            throw new IllegalStateException("Bus " + busNumber + " already registered for today");
        }

        DismissalEvent.DismissalEventBuilder builder = DismissalEvent.builder()
                .eventDate(today)
                .eventType(DismissalEventType.BUS_ARRIVAL)
                .busNumber(busNumber)
                .vehicleBarcode(barcode)
                .status(DismissalEventStatus.ARRIVED)
                .arrivalTime(LocalDateTime.now());

        busRouteRepository.findByRouteNumber(busNumber).ifPresent(builder::busRoute);

        DismissalEvent event = builder.build();
        log.info("Registered bus arrival: Bus #{}", busNumber);
        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent registerCarPickup(Long studentId, String parentName, String vehicleInfo) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        DismissalEvent event = DismissalEvent.builder()
                .eventDate(LocalDate.now())
                .eventType(DismissalEventType.CAR_PICKUP)
                .student(student)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .parentName(parentName)
                .parentVehicleInfo(vehicleInfo)
                .status(DismissalEventStatus.PENDING)
                .build();

        log.info("Registered car pickup for student: {}", event.getStudentName());
        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent registerWalker(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        DismissalEvent event = DismissalEvent.builder()
                .eventDate(LocalDate.now())
                .eventType(DismissalEventType.WALKER)
                .student(student)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .status(DismissalEventStatus.PENDING)
                .build();

        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent registerAftercare(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        DismissalEvent event = DismissalEvent.builder()
                .eventDate(LocalDate.now())
                .eventType(DismissalEventType.AFTERCARE)
                .student(student)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .status(DismissalEventStatus.PENDING)
                .build();

        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent registerAthletics(Long studentId, String sportName) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        DismissalEvent event = DismissalEvent.builder()
                .eventDate(LocalDate.now())
                .eventType(DismissalEventType.ATHLETICS)
                .student(student)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .sportName(sportName)
                .status(DismissalEventStatus.PENDING)
                .build();

        log.info("Registered athletics dismissal for student: {} - {}", event.getStudentName(), sportName);
        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent registerCounselorSummon(Long studentId, String counselorName, String meetingType) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        DismissalEvent event = DismissalEvent.builder()
                .eventDate(LocalDate.now())
                .eventType(DismissalEventType.COUNSELOR_SUMMON)
                .student(student)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .counselorName(counselorName)
                .meetingType(meetingType)
                .status(DismissalEventStatus.PENDING)
                .build();

        log.info("Counselor summon for student: {} - {} ({})", event.getStudentName(), meetingType, counselorName);
        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent callStudent(Long eventId) {
        DismissalEvent event = getEventById(eventId);
        event.setStatus(DismissalEventStatus.CALLED);
        event.setCalledTime(LocalDateTime.now());
        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent markDeparted(Long eventId) {
        DismissalEvent event = getEventById(eventId);
        event.setStatus(DismissalEventStatus.DEPARTED);
        event.setDepartureTime(LocalDateTime.now());
        return dismissalEventRepository.save(event);
    }

    @Override
    @Transactional
    public DismissalEvent cancelEvent(Long eventId) {
        DismissalEvent event = getEventById(eventId);
        event.setStatus(DismissalEventStatus.CANCELLED);
        return dismissalEventRepository.save(event);
    }

    @Override
    public List<DismissalEvent> getTodaysEvents() {
        return dismissalEventRepository.findByEventDateWithDetails(LocalDate.now());
    }

    @Override
    public List<DismissalEvent> getTodaysBusArrivals() {
        return dismissalEventRepository.findTodaysBusArrivals(LocalDate.now());
    }

    @Override
    public List<DismissalEvent> getTodaysCarPickups() {
        return dismissalEventRepository.findTodaysCarPickups(LocalDate.now());
    }

    @Override
    public List<DismissalEvent> getEventsByDateAndType(LocalDate date, DismissalEventType type) {
        return dismissalEventRepository.findByEventDateAndEventTypeOrderByArrivalTimeDesc(date, type);
    }

    @Override
    public DismissalEvent getStudentDismissalStatus(Long studentId) {
        List<DismissalEvent> events = dismissalEventRepository.findByEventDateAndStudentId(LocalDate.now(), studentId);
        return events.isEmpty() ? null : events.get(0);
    }

    @Override
    public Map<String, Object> getTodaysBoardStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new HashMap<>();
        stats.put("busArrivals", dismissalEventRepository.countByEventDateAndEventType(today, DismissalEventType.BUS_ARRIVAL));
        stats.put("carPickups", dismissalEventRepository.countByEventDateAndEventType(today, DismissalEventType.CAR_PICKUP));
        stats.put("walkers", dismissalEventRepository.countByEventDateAndEventType(today, DismissalEventType.WALKER));
        stats.put("aftercare", dismissalEventRepository.countByEventDateAndEventType(today, DismissalEventType.AFTERCARE));
        stats.put("athletics", dismissalEventRepository.countByEventDateAndEventType(today, DismissalEventType.ATHLETICS));
        stats.put("counselorSummons", dismissalEventRepository.countByEventDateAndEventType(today, DismissalEventType.COUNSELOR_SUMMON));
        stats.put("pending", dismissalEventRepository.countByEventDateAndStatus(today, DismissalEventStatus.PENDING));
        stats.put("departed", dismissalEventRepository.countByEventDateAndStatus(today, DismissalEventStatus.DEPARTED));
        return stats;
    }

    @Override
    @Transactional
    public DismissalEvent parentArrivedNotification(Long studentId) {
        // Find the student's pending car pickup event for today
        List<DismissalEvent> events = dismissalEventRepository.findByEventDateAndStudentId(LocalDate.now(), studentId);
        DismissalEvent pickupEvent = events.stream()
                .filter(e -> e.getEventType() == DismissalEventType.CAR_PICKUP)
                .filter(e -> e.getStatus() == DismissalEventStatus.PENDING)
                .findFirst()
                .orElse(null);

        if (pickupEvent != null) {
            pickupEvent.setStatus(DismissalEventStatus.ARRIVED);
            pickupEvent.setArrivalTime(LocalDateTime.now());
            log.info("Parent arrived for student pickup: {}", pickupEvent.getStudentName());
            return dismissalEventRepository.save(pickupEvent);
        }

        // No pending pickup — create a new car pickup event
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        DismissalEvent event = DismissalEvent.builder()
                .eventDate(LocalDate.now())
                .eventType(DismissalEventType.CAR_PICKUP)
                .student(student)
                .studentName(student.getFirstName() + " " + student.getLastName())
                .status(DismissalEventStatus.ARRIVED)
                .arrivalTime(LocalDateTime.now())
                .build();

        log.info("Parent arrived (new event) for student: {}", event.getStudentName());
        return dismissalEventRepository.save(event);
    }

    private DismissalEvent getEventById(Long eventId) {
        return dismissalEventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Dismissal event not found: " + eventId));
    }
}
