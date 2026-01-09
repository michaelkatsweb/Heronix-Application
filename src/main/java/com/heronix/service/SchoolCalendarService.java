package com.heronix.service;

import com.heronix.dto.CalendarEventDTO;
import com.heronix.dto.SchoolCalendarDTO;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.SchoolCalendar;
import com.heronix.model.domain.SchoolCalendar.CalendarEvent;
import com.heronix.repository.CampusRepository;
import com.heronix.repository.SchoolCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing school calendars
 * Handles CRUD operations and business logic for school calendar configuration
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolCalendarService {

    private final SchoolCalendarRepository schoolCalendarRepository;
    private final CampusRepository campusRepository;

    // ========================================================================
    // CREATE & UPDATE OPERATIONS
    // ========================================================================

    /**
     * Set up a school year calendar
     *
     * @param academicYear Academic year (e.g., "2024-2025")
     * @param startDate First day of school
     * @param endDate Last day of school
     * @param campusId Campus ID (null for district-wide)
     * @return Created school calendar DTO
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public SchoolCalendarDTO setSchoolYear(String academicYear, LocalDate startDate,
                                           LocalDate endDate, Long campusId) {
        log.info("Setting school year: {} from {} to {}", academicYear, startDate, endDate);

        // Validate dates
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Check for existing calendar
        if (schoolCalendarRepository.existsByAcademicYearAndCampus(academicYear, campusId, null)) {
            throw new IllegalArgumentException(
                String.format("School calendar for academic year '%s' already exists for this campus", academicYear));
        }

        // Create calendar
        SchoolCalendar calendar = SchoolCalendar.builder()
                .academicYear(academicYear)
                .startDate(startDate)
                .endDate(endDate)
                .instructionalDaysPerWeek(5) // Default Monday-Friday
                .active(true)
                .build();

        // Set campus if provided
        if (campusId != null) {
            Campus campus = campusRepository.findById(campusId)
                    .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + campusId));
            calendar.setCampus(campus);
        }

        // Calculate instructional days
        calendar.calculateInstructionalDays();

        // Save
        SchoolCalendar saved = schoolCalendarRepository.save(calendar);
        log.info("Created school calendar with ID: {}", saved.getId());

        return toDTO(saved);
    }

    /**
     * Update an existing school calendar
     *
     * @param id Calendar ID
     * @param dto Updated calendar data
     * @return Updated school calendar DTO
     */
    @Transactional
    public SchoolCalendarDTO updateSchoolCalendar(Long id, SchoolCalendarDTO dto) {
        log.info("Updating school calendar ID: {}", id);

        SchoolCalendar existing = schoolCalendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + id));

        // Validate dates
        if (!dto.getStartDate().isBefore(dto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Update fields
        existing.setAcademicYear(dto.getAcademicYear());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setInstructionalDaysPerWeek(dto.getInstructionalDaysPerWeek());
        existing.setActive(dto.getActive() != null ? dto.getActive() : true);

        // Update campus if changed
        if (dto.getCampusId() != null) {
            Campus campus = campusRepository.findById(dto.getCampusId())
                    .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + dto.getCampusId()));
            existing.setCampus(campus);
        } else {
            existing.setCampus(null);
        }

        // Recalculate instructional days
        existing.calculateInstructionalDays();

        // Save
        SchoolCalendar updated = schoolCalendarRepository.save(existing);
        log.info("Updated school calendar ID: {}", updated.getId());

        return toDTO(updated);
    }

    /**
     * Delete a school calendar
     *
     * @param id Calendar ID
     */
    @Transactional
    public void deleteSchoolCalendar(Long id) {
        log.info("Deleting (deactivating) school calendar ID: {}", id);

        SchoolCalendar calendar = schoolCalendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + id));

        calendar.setActive(false);
        schoolCalendarRepository.save(calendar);

        log.info("Deactivated school calendar ID: {}", id);
    }

    // ========================================================================
    // EVENT OPERATIONS
    // ========================================================================

    /**
     * Add a holiday to the calendar
     *
     * @param calendarId Calendar ID
     * @param name Holiday name
     * @param date Holiday date
     * @return Updated calendar DTO
     */
    @Transactional
    public SchoolCalendarDTO addHoliday(Long calendarId, String name, LocalDate date) {
        log.info("Adding holiday '{}' on {} to calendar ID: {}", name, date, calendarId);

        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        CalendarEvent holiday = CalendarEvent.builder()
                .eventName(name)
                .eventType(CalendarEvent.EventType.HOLIDAY)
                .eventDate(date)
                .nonInstructionalDay(true)
                .build();

        calendar.addEvent(holiday);

        // Recalculate instructional days
        calendar.calculateInstructionalDays();

        SchoolCalendar updated = schoolCalendarRepository.save(calendar);
        return toDTO(updated);
    }

    /**
     * Add a break period to the calendar
     *
     * @param calendarId Calendar ID
     * @param name Break name (e.g., "Spring Break", "Winter Break")
     * @param startDate Break start date
     * @param endDate Break end date
     * @return Updated calendar DTO
     */
    @Transactional
    public SchoolCalendarDTO addBreakPeriod(Long calendarId, String name,
                                            LocalDate startDate, LocalDate endDate) {
        log.info("Adding break '{}' from {} to {} to calendar ID: {}",
                 name, startDate, endDate, calendarId);

        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Break start date must be before end date");
        }

        CalendarEvent breakPeriod = CalendarEvent.builder()
                .eventName(name)
                .eventType(CalendarEvent.EventType.BREAK)
                .startDate(startDate)
                .endDate(endDate)
                .nonInstructionalDay(true)
                .build();

        calendar.addEvent(breakPeriod);

        // Recalculate instructional days
        calendar.calculateInstructionalDays();

        SchoolCalendar updated = schoolCalendarRepository.save(calendar);
        return toDTO(updated);
    }

    /**
     * Add a calendar event
     *
     * @param calendarId Calendar ID
     * @param eventDTO Event data
     * @return Updated calendar DTO
     */
    @Transactional
    public SchoolCalendarDTO addEvent(Long calendarId, CalendarEventDTO eventDTO) {
        log.info("Adding event '{}' to calendar ID: {}", eventDTO.getEventName(), calendarId);

        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        CalendarEvent event = toEventEntity(eventDTO);
        calendar.addEvent(event);

        // Recalculate instructional days if it's a non-instructional day
        if (event.getNonInstructionalDay()) {
            calendar.calculateInstructionalDays();
        }

        SchoolCalendar updated = schoolCalendarRepository.save(calendar);
        return toDTO(updated);
    }

    /**
     * Remove an event from the calendar
     *
     * @param calendarId Calendar ID
     * @param eventId Event ID
     * @return Updated calendar DTO
     */
    @Transactional
    public SchoolCalendarDTO removeEvent(Long calendarId, Long eventId) {
        log.info("Removing event {} from calendar ID: {}", eventId, calendarId);

        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        CalendarEvent eventToRemove = calendar.getEvents().stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        calendar.removeEvent(eventToRemove);

        // Recalculate instructional days
        calendar.calculateInstructionalDays();

        SchoolCalendar updated = schoolCalendarRepository.save(calendar);
        return toDTO(updated);
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Get school calendar by ID
     *
     * @param id Calendar ID
     * @return School calendar DTO
     */
    @Transactional(readOnly = true)
    public SchoolCalendarDTO getSchoolCalendarById(Long id) {
        SchoolCalendar calendar = schoolCalendarRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + id));

        return toDTO(calendar);
    }

    /**
     * Get all school calendars
     *
     * @return List of all school calendar DTOs
     */
    @Transactional(readOnly = true)
    public List<SchoolCalendarDTO> getAllSchoolCalendars() {
        return schoolCalendarRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active school calendars
     *
     * @return List of active school calendar DTOs
     */
    @Transactional(readOnly = true)
    public List<SchoolCalendarDTO> getActiveSchoolCalendars() {
        return schoolCalendarRepository.findAllActive()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get school calendar for an academic year
     *
     * @param academicYear Academic year
     * @return List of school calendar DTOs
     */
    @Transactional(readOnly = true)
    public List<SchoolCalendarDTO> getCalendarForYear(String academicYear) {
        return schoolCalendarRepository.findByAcademicYear(academicYear)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get current school calendar
     *
     * @return Current school calendar DTO or null if none found
     */
    @Transactional(readOnly = true)
    public SchoolCalendarDTO getCurrentCalendar() {
        List<SchoolCalendar> current = schoolCalendarRepository.findCurrentCalendar(LocalDate.now());

        if (current.isEmpty()) {
            return null;
        }

        return toDTO(current.get(0));
    }

    /**
     * Get current school calendar for a campus
     *
     * @param campusId Campus ID
     * @return Current school calendar DTO or null if none found
     */
    @Transactional(readOnly = true)
    public SchoolCalendarDTO getCurrentCalendarForCampus(Long campusId) {
        return schoolCalendarRepository.findCurrentCalendarByCampus(campusId, LocalDate.now())
                .map(this::toDTO)
                .orElse(null);
    }

    /**
     * Get instructional days between two dates
     *
     * @param calendarId Calendar ID
     * @param startDate Start date
     * @param endDate End date
     * @return Number of instructional days
     */
    @Transactional(readOnly = true)
    public int getInstructionalDays(Long calendarId, LocalDate startDate, LocalDate endDate) {
        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        return calendar.getInstructionalDaysBetween(startDate, endDate);
    }

    /**
     * Check if a date is a school day
     *
     * @param calendarId Calendar ID
     * @param date Date to check
     * @return True if it's a school day, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isSchoolDay(Long calendarId, LocalDate date) {
        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        return calendar.isSchoolDay(date);
    }

    /**
     * Get events for a specific date
     *
     * @param calendarId Calendar ID
     * @param date Date to check
     * @return List of events on that date
     */
    @Transactional(readOnly = true)
    public List<CalendarEventDTO> getEventsOnDate(Long calendarId, LocalDate date) {
        SchoolCalendar calendar = schoolCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new IllegalArgumentException("School calendar not found: " + calendarId));

        return calendar.getEventsOnDate(date).stream()
                .map(this::toEventDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Convert entity to DTO
     */
    private SchoolCalendarDTO toDTO(SchoolCalendar calendar) {
        SchoolCalendarDTO dto = SchoolCalendarDTO.builder()
                .id(calendar.getId())
                .academicYear(calendar.getAcademicYear())
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .instructionalDaysPerWeek(calendar.getInstructionalDaysPerWeek())
                .totalInstructionalDays(calendar.getTotalInstructionalDays())
                .active(calendar.getActive())
                .totalDays(calendar.getTotalDays())
                .isCurrentYear(calendar.isCurrentYear())
                .isValid(calendar.isValid())
                .holidayCount(calendar.getHolidays().size())
                .breakCount(calendar.getBreaks().size())
                .build();

        if (calendar.getCampus() != null) {
            dto.setCampusId(calendar.getCampus().getId());
            dto.setCampusName(calendar.getCampus().getName());
        }

        if (calendar.getEvents() != null) {
            List<CalendarEventDTO> eventDTOs = calendar.getEvents().stream()
                    .map(this::toEventDTO)
                    .collect(Collectors.toList());
            dto.setEvents(eventDTOs);
        }

        return dto;
    }

    /**
     * Convert CalendarEvent entity to DTO
     */
    private CalendarEventDTO toEventDTO(CalendarEvent event) {
        return CalendarEventDTO.builder()
                .id(event.getId())
                .calendarId(event.getCalendar() != null ? event.getCalendar().getId() : null)
                .eventName(event.getEventName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .eventDate(event.getEventDate())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .nonInstructionalDay(event.getNonInstructionalDay())
                .recurring(event.getRecurring())
                .durationDays(event.getDurationDays())
                .build();
    }

    /**
     * Convert CalendarEventDTO to entity
     */
    private CalendarEvent toEventEntity(CalendarEventDTO dto) {
        return CalendarEvent.builder()
                .eventName(dto.getEventName())
                .description(dto.getDescription())
                .eventType(dto.getEventType())
                .eventDate(dto.getEventDate())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .nonInstructionalDay(dto.getNonInstructionalDay() != null ? dto.getNonInstructionalDay() : true)
                .recurring(dto.getRecurring() != null ? dto.getRecurring() : false)
                .build();
    }
}
