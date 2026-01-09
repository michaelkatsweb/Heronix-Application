package com.heronix.service;

import com.heronix.dto.GradingPeriodDTO;
import com.heronix.model.domain.Campus;
import com.heronix.model.domain.GradingPeriod;
import com.heronix.repository.CampusRepository;
import com.heronix.repository.GradingPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing grading periods
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradingPeriodManagementService {

    private final GradingPeriodRepository gradingPeriodRepository;
    private final CampusRepository campusRepository;

    /**
     * Create a new grading period
     */
    @Transactional
    public GradingPeriodDTO createGradingPeriod(GradingPeriodDTO dto) {
        log.info("Creating grading period: {} for year {}", dto.getName(), dto.getAcademicYear());

        // Validate non-overlapping
        validateNonOverlapping(dto);

        // Build entity
        GradingPeriod period = buildEntityFromDTO(dto);

        // Save
        GradingPeriod saved = gradingPeriodRepository.save(period);

        log.info("Created grading period with ID: {}", saved.getId());
        return buildDTOFromEntity(saved);
    }

    /**
     * Update an existing grading period
     */
    @Transactional
    public GradingPeriodDTO updateGradingPeriod(Long id, GradingPeriodDTO dto) {
        log.info("Updating grading period ID: {}", id);

        GradingPeriod existing = gradingPeriodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grading period not found: " + id));

        // Validate non-overlapping (excluding this period)
        validateNonOverlapping(dto, id);

        // Update fields
        existing.setName(dto.getName());
        existing.setAcademicYear(dto.getAcademicYear());
        existing.setPeriodType(GradingPeriod.PeriodType.valueOf(dto.getPeriodType()));
        existing.setPeriodNumber(dto.getPeriodNumber());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setInstructionalDays(dto.getInstructionalDays());
        existing.setActive(dto.getActive());
        existing.setNotes(dto.getNotes());

        if (dto.getCampusId() != null) {
            Campus campus = campusRepository.findById(dto.getCampusId())
                    .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + dto.getCampusId()));
            existing.setCampus(campus);
        }

        GradingPeriod updated = gradingPeriodRepository.save(existing);

        log.info("Updated grading period ID: {}", id);
        return buildDTOFromEntity(updated);
    }

    /**
     * Delete a grading period
     */
    @Transactional
    public void deleteGradingPeriod(Long id) {
        log.info("Deleting grading period ID: {}", id);
        gradingPeriodRepository.deleteById(id);
    }

    /**
     * Get grading period by ID
     */
    @Transactional(readOnly = true)
    public GradingPeriodDTO getGradingPeriod(Long id) {
        GradingPeriod period = gradingPeriodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grading period not found: " + id));
        return buildDTOFromEntity(period);
    }

    /**
     * Get all grading periods for an academic year
     */
    @Transactional(readOnly = true)
    public List<GradingPeriodDTO> getGradingPeriodsForYear(String academicYear) {
        List<GradingPeriod> periods = gradingPeriodRepository
                .findByAcademicYearAndActiveTrueOrderByPeriodNumberAsc(academicYear);
        return periods.stream()
                .map(this::buildDTOFromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get grading period by ID (alias for getGradingPeriod)
     */
    @Transactional(readOnly = true)
    public GradingPeriodDTO getGradingPeriodById(Long id) {
        return getGradingPeriod(id);
    }

    /**
     * Get all grading periods
     */
    @Transactional(readOnly = true)
    public List<GradingPeriodDTO> getAllGradingPeriods() {
        return gradingPeriodRepository.findAll()
                .stream()
                .map(this::buildDTOFromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get the current grading period
     */
    @Transactional(readOnly = true)
    public GradingPeriodDTO getCurrentGradingPeriod() {
        return gradingPeriodRepository.findCurrentPeriod(LocalDate.now())
                .map(this::buildDTOFromEntity)
                .orElse(null);
    }

    /**
     * Get all distinct academic years
     */
    @Transactional(readOnly = true)
    public List<String> getAcademicYears() {
        return gradingPeriodRepository.findDistinctAcademicYears();
    }

    /**
     * Validate that a grading period does not overlap with existing periods
     */
    private void validateNonOverlapping(GradingPeriodDTO dto) {
        validateNonOverlapping(dto, null);
    }

    private void validateNonOverlapping(GradingPeriodDTO dto, Long excludeId) {
        List<GradingPeriod> overlapping = gradingPeriodRepository.findOverlappingPeriods(
                dto.getAcademicYear(),
                dto.getStartDate(),
                dto.getEndDate()
        );

        // Filter out the period being updated
        if (excludeId != null) {
            overlapping = overlapping.stream()
                    .filter(p -> !p.getId().equals(excludeId))
                    .collect(Collectors.toList());
        }

        // Filter by campus if specified
        if (dto.getCampusId() != null) {
            overlapping = overlapping.stream()
                    .filter(p -> p.getCampus() != null && p.getCampus().getId().equals(dto.getCampusId()))
                    .collect(Collectors.toList());
        }

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Grading period overlaps with existing period: %s",
                            overlapping.get(0).getName()));
        }
    }

    /**
     * Build entity from DTO
     */
    private GradingPeriod buildEntityFromDTO(GradingPeriodDTO dto) {
        GradingPeriod.GradingPeriodBuilder builder = GradingPeriod.builder()
                .name(dto.getName())
                .academicYear(dto.getAcademicYear())
                .periodType(GradingPeriod.PeriodType.valueOf(dto.getPeriodType()))
                .periodNumber(dto.getPeriodNumber())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .instructionalDays(dto.getInstructionalDays())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .notes(dto.getNotes());

        if (dto.getCampusId() != null) {
            Campus campus = campusRepository.findById(dto.getCampusId())
                    .orElseThrow(() -> new IllegalArgumentException("Campus not found: " + dto.getCampusId()));
            builder.campus(campus);
        }

        return builder.build();
    }

    /**
     * Build DTO from entity
     */
    private GradingPeriodDTO buildDTOFromEntity(GradingPeriod period) {
        return GradingPeriodDTO.builder()
                .id(period.getId())
                .name(period.getName())
                .academicYear(period.getAcademicYear())
                .periodType(period.getPeriodType().name())
                .periodNumber(period.getPeriodNumber())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .instructionalDays(period.getInstructionalDays())
                .active(period.getActive())
                .campusId(period.getCampus() != null ? period.getCampus().getId() : null)
                .campusName(period.getCampus() != null ? period.getCampus().getName() : null)
                .notes(period.getNotes())
                .displayName(period.getDisplayName())
                .percentComplete(period.getPercentComplete())
                .duration(period.getDuration())
                .isCurrent(period.isCurrentPeriod())
                .build();
    }
}
