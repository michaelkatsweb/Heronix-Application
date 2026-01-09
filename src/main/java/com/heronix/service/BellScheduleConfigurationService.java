package com.heronix.service;

import com.heronix.dto.BellScheduleDTO;
import com.heronix.dto.PeriodTimerDTO;
import com.heronix.model.domain.AcademicYear;
import com.heronix.model.domain.BellSchedule;
import com.heronix.model.domain.BellSchedule.ScheduleType;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.PeriodTimer;
import com.heronix.repository.AcademicYearRepository;
import com.heronix.repository.BellScheduleRepository;
import com.heronix.repository.CampusRepository;
import com.heronix.repository.PeriodTimerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bell Schedule Configuration Service
 * Manages bell schedule creation, modification, and period configuration
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BellScheduleConfigurationService {

    private final BellScheduleRepository bellScheduleRepository;
    private final PeriodTimerRepository periodTimerRepository;
    private final CampusRepository campusRepository;
    private final AcademicYearRepository academicYearRepository;

    // ========================================================================
    // CREATE & UPDATE OPERATIONS
    // ========================================================================

    /**
     * Create a new bell schedule
     *
     * @param dto Bell schedule data
     * @return Created bell schedule DTO
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public BellScheduleDTO createBellSchedule(BellScheduleDTO dto) {
        log.info("Creating bell schedule: {}", dto.getName());

        // Validate name uniqueness
        if (bellScheduleRepository.existsByNameAndCampus(dto.getName(), dto.getCampusId(), null)) {
            throw new IllegalArgumentException(
                String.format("Bell schedule with name '%s' already exists for this campus", dto.getName()));
        }

        // Validate default schedule constraint
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            if (bellScheduleRepository.existsDefaultSchedule(dto.getCampusId(), null)) {
                throw new IllegalArgumentException(
                    "A default schedule already exists for this campus. Please unset the existing default first.");
            }
        }

        // Build bell schedule entity
        BellSchedule schedule = BellSchedule.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .scheduleType(dto.getScheduleType() != null ? dto.getScheduleType() : ScheduleType.REGULAR)
                .daysOfWeek(dto.getDaysOfWeek() != null ? dto.getDaysOfWeek() : "MON,TUE,WED,THU,FRI")
                .specificDates(dto.getSpecificDates() != null ? new ArrayList<>(dto.getSpecificDates()) : new ArrayList<>())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .periods(new ArrayList<>())
                .build();

        // Set campus if provided
        if (dto.getCampusId() != null) {
            Campus campus = campusRepository.findById(dto.getCampusId())
                    .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + dto.getCampusId()));
            schedule.setCampus(campus);
        }

        // Set academic year if provided
        if (dto.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new IllegalArgumentException("Academic year not found: " + dto.getAcademicYearId()));
            schedule.setAcademicYear(academicYear);
        }

        // Save bell schedule
        BellSchedule saved = bellScheduleRepository.save(schedule);
        log.info("Created bell schedule with ID: {}", saved.getId());

        return toDTO(saved);
    }

    /**
     * Update an existing bell schedule
     *
     * @param id Bell schedule ID
     * @param dto Updated bell schedule data
     * @return Updated bell schedule DTO
     */
    @Transactional
    public BellScheduleDTO updateBellSchedule(Long id, BellScheduleDTO dto) {
        log.info("Updating bell schedule ID: {}", id);

        BellSchedule existing = bellScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bell schedule not found: " + id));

        // Validate name uniqueness (excluding current record)
        if (bellScheduleRepository.existsByNameAndCampus(dto.getName(), dto.getCampusId(), id)) {
            throw new IllegalArgumentException(
                String.format("Bell schedule with name '%s' already exists for this campus", dto.getName()));
        }

        // Validate default schedule constraint (excluding current record)
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            if (bellScheduleRepository.existsDefaultSchedule(dto.getCampusId(), id)) {
                throw new IllegalArgumentException(
                    "A default schedule already exists for this campus. Please unset the existing default first.");
            }
        }

        // Update fields
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setScheduleType(dto.getScheduleType());
        existing.setDaysOfWeek(dto.getDaysOfWeek());
        existing.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        existing.setActive(dto.getActive() != null ? dto.getActive() : true);

        // Update specific dates if provided
        if (dto.getSpecificDates() != null) {
            existing.setSpecificDates(new ArrayList<>(dto.getSpecificDates()));
        }

        // Update campus if changed
        if (dto.getCampusId() != null) {
            Campus campus = campusRepository.findById(dto.getCampusId())
                    .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + dto.getCampusId()));
            existing.setCampus(campus);
        } else {
            existing.setCampus(null);
        }

        // Update academic year if changed
        if (dto.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new IllegalArgumentException("Academic year not found: " + dto.getAcademicYearId()));
            existing.setAcademicYear(academicYear);
        } else {
            existing.setAcademicYear(null);
        }

        // Save
        BellSchedule updated = bellScheduleRepository.save(existing);
        log.info("Updated bell schedule ID: {}", updated.getId());

        return toDTO(updated);
    }

    /**
     * Delete (deactivate) a bell schedule
     *
     * @param id Bell schedule ID
     */
    @Transactional
    public void deleteBellSchedule(Long id) {
        log.info("Deleting (deactivating) bell schedule ID: {}", id);

        BellSchedule schedule = bellScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bell schedule not found: " + id));

        schedule.setActive(false);
        bellScheduleRepository.save(schedule);

        log.info("Deactivated bell schedule ID: {}", id);
    }

    // ========================================================================
    // PERIOD MANAGEMENT OPERATIONS
    // ========================================================================

    /**
     * Add a period to a bell schedule
     *
     * @param scheduleId Bell schedule ID
     * @param periodDTO Period data
     * @return Updated bell schedule DTO
     */
    @Transactional
    public BellScheduleDTO addPeriodToBellSchedule(Long scheduleId, PeriodTimerDTO periodDTO) {
        log.info("Adding period to bell schedule ID: {}", scheduleId);

        BellSchedule schedule = bellScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Bell schedule not found: " + scheduleId));

        // Create new period timer
        PeriodTimer period = PeriodTimer.builder()
                .periodNumber(periodDTO.getPeriodNumber())
                .periodName(periodDTO.getPeriodName())
                .startTime(periodDTO.getStartTime())
                .endTime(periodDTO.getEndTime())
                .attendanceWindowMinutes(periodDTO.getAttendanceWindowMinutes() != null ?
                    periodDTO.getAttendanceWindowMinutes() : 15)
                .autoMarkAbsent(periodDTO.getAutoMarkAbsent() != null ? periodDTO.getAutoMarkAbsent() : true)
                .daysOfWeek(periodDTO.getDaysOfWeek() != null ? periodDTO.getDaysOfWeek() : "MON,TUE,WED,THU,FRI")
                .active(periodDTO.getActive() != null ? periodDTO.getActive() : true)
                .build();

        // Set academic year if specified
        if (periodDTO.getAcademicYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(periodDTO.getAcademicYearId())
                    .orElseThrow(() -> new IllegalArgumentException("Academic year not found: " + periodDTO.getAcademicYearId()));
            period.setAcademicYear(academicYear);
        }

        // Add period to schedule (this will set the bell_schedule_id foreign key)
        schedule.addPeriod(period);

        // Save
        BellSchedule updated = bellScheduleRepository.save(schedule);
        log.info("Added period to bell schedule ID: {}", scheduleId);

        return toDTO(updated);
    }

    /**
     * Remove a period from a bell schedule
     *
     * @param scheduleId Bell schedule ID
     * @param periodId Period ID
     * @return Updated bell schedule DTO
     */
    @Transactional
    public BellScheduleDTO removePeriodFromBellSchedule(Long scheduleId, Long periodId) {
        log.info("Removing period {} from bell schedule ID: {}", periodId, scheduleId);

        BellSchedule schedule = bellScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Bell schedule not found: " + scheduleId));

        PeriodTimer periodToRemove = schedule.getPeriods().stream()
                .filter(p -> p.getId().equals(periodId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Period not found in this schedule: " + periodId));

        // Remove period from schedule
        schedule.removePeriod(periodToRemove);

        // Save
        BellSchedule updated = bellScheduleRepository.save(schedule);
        log.info("Removed period {} from bell schedule ID: {}", periodId, scheduleId);

        return toDTO(updated);
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Get bell schedule by ID
     *
     * @param id Bell schedule ID
     * @return Bell schedule DTO
     */
    @Transactional(readOnly = true)
    public BellScheduleDTO getBellScheduleById(Long id) {
        BellSchedule schedule = bellScheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bell schedule not found: " + id));

        return toDTO(schedule);
    }

    /**
     * Get all bell schedules
     *
     * @return List of all bell schedule DTOs
     */
    @Transactional(readOnly = true)
    public List<BellScheduleDTO> getAllBellSchedules() {
        return bellScheduleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active bell schedules
     *
     * @return List of active bell schedule DTOs
     */
    @Transactional(readOnly = true)
    public List<BellScheduleDTO> getActiveBellSchedules() {
        return bellScheduleRepository.findAllActive().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get bell schedule for a specific date and campus
     *
     * @param date Date to check
     * @param campusId Campus ID (optional)
     * @return Bell schedule DTO or null if none found
     */
    @Transactional(readOnly = true)
    public BellScheduleDTO getBellScheduleForDate(LocalDate date, Long campusId) {
        log.debug("Getting bell schedule for date {} and campus {}", date, campusId);

        // First, check for specific date schedules
        List<BellSchedule> specificDateSchedules = campusId != null
                ? bellScheduleRepository.findBySpecificDateAndCampus(date, campusId)
                : bellScheduleRepository.findBySpecificDate(date);

        if (!specificDateSchedules.isEmpty()) {
            return toDTO(specificDateSchedules.get(0));
        }

        // Fall back to default schedule that applies to this day of week
        BellSchedule defaultSchedule = campusId != null
                ? bellScheduleRepository.findDefaultByCampus(campusId).orElse(null)
                : bellScheduleRepository.findDefaultDistrictWide().orElse(null);

        if (defaultSchedule != null && defaultSchedule.appliesTo(date)) {
            return toDTO(defaultSchedule);
        }

        return null;
    }

    /**
     * Get bell schedules by campus
     *
     * @param campusId Campus ID
     * @return List of bell schedule DTOs
     */
    @Transactional(readOnly = true)
    public List<BellScheduleDTO> getBellSchedulesByCampus(Long campusId) {
        return bellScheduleRepository.findByCampusIdOrderByNameAsc(campusId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get bell schedules by academic year
     *
     * @param academicYearId Academic year ID
     * @return List of bell schedule DTOs
     */
    @Transactional(readOnly = true)
    public List<BellScheduleDTO> getBellSchedulesByAcademicYear(Long academicYearId) {
        return bellScheduleRepository.findByAcademicYearIdOrderByNameAsc(academicYearId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Convert BellSchedule entity to DTO
     */
    private BellScheduleDTO toDTO(BellSchedule schedule) {
        BellScheduleDTO dto = BellScheduleDTO.builder()
                .id(schedule.getId())
                .name(schedule.getName())
                .description(schedule.getDescription())
                .scheduleType(schedule.getScheduleType())
                .daysOfWeek(schedule.getDaysOfWeek())
                .specificDates(schedule.getSpecificDates() != null ?
                    new ArrayList<>(schedule.getSpecificDates()) : new ArrayList<>())
                .isDefault(schedule.getIsDefault())
                .active(schedule.getActive())
                .totalInstructionalMinutes(schedule.getTotalInstructionalMinutes())
                // Calculated fields
                .periodCount(schedule.getPeriodCount())
                .formattedSchedule(schedule.getFormattedSchedule())
                .displayName(schedule.getDisplayName())
                .isComplete(schedule.isComplete())
                .hasOverlappingPeriods(schedule.hasOverlappingPeriods())
                .build();

        // Set campus info
        if (schedule.getCampus() != null) {
            dto.setCampusId(schedule.getCampus().getId());
            dto.setCampusName(schedule.getCampus().getName());
        }

        // Set academic year info
        if (schedule.getAcademicYear() != null) {
            dto.setAcademicYearId(schedule.getAcademicYear().getId());
            dto.setAcademicYearName(schedule.getAcademicYear().getYearName());
        }

        // Convert periods to DTOs
        if (schedule.getPeriods() != null) {
            List<PeriodTimerDTO> periodDTOs = schedule.getPeriods().stream()
                    .map(this::toPeriodTimerDTO)
                    .collect(Collectors.toList());
            dto.setPeriods(periodDTOs);
        }

        return dto;
    }

    /**
     * Convert PeriodTimer entity to DTO
     */
    private PeriodTimerDTO toPeriodTimerDTO(PeriodTimer period) {
        PeriodTimerDTO dto = PeriodTimerDTO.builder()
                .id(period.getId())
                .periodNumber(period.getPeriodNumber())
                .periodName(period.getPeriodName())
                .startTime(period.getStartTime())
                .endTime(period.getEndTime())
                .attendanceWindowMinutes(period.getAttendanceWindowMinutes())
                .autoMarkAbsent(period.getAutoMarkAbsent())
                .daysOfWeek(period.getDaysOfWeek())
                .active(period.getActive())
                .build();

        if (period.getAcademicYear() != null) {
            dto.setAcademicYearId(period.getAcademicYear().getId());
        }

        return dto;
    }
}
