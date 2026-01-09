package com.heronix.service.integration;

import com.heronix.integration.SchedulerApiClient;
import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for importing optimized schedules FROM Heronix-SchedulerV2 into SIS
 * Handles mapping SchedulerV2 results back to SIS entities
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 25, 2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleImportService {

    private final SchedulerApiClient schedulerApiClient;

    // Repositories for persistence
    private final ScheduleRepository scheduleRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final ScheduleSlotRepository scheduleSlotRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final RoomRepository roomRepository;
    private final PeriodTimerRepository periodTimerRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // MAIN IMPORT METHOD
    // ========================================================================

    /**
     * Import optimized schedule from SchedulerV2
     *
     * @param scheduleId SIS schedule ID
     * @param jobId SchedulerV2 job ID
     * @return Import result with statistics
     * @throws IllegalArgumentException if schedule not found
     * @throws SchedulerApiClient.SchedulerApiException if import fails
     */
    @Transactional
    public ScheduleImportResult importFromScheduler(Long scheduleId, String jobId)
            throws SchedulerApiClient.SchedulerApiException {

        log.info("Starting import from SchedulerV2 for job ID: {}", jobId);

        // Find the schedule
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        // Get optimized schedule from SchedulerV2
        Map<String, Object> exportData = schedulerApiClient.exportSchedule(jobId);

        // Import the schedule data
        ImportStatistics stats = processScheduleData(schedule, exportData);

        // Update schedule metadata
        updateScheduleMetadata(schedule, exportData);

        log.info("Import from SchedulerV2 completed: {} sections created, {} slots assigned",
                stats.getSectionsCreated(), stats.getSlotsAssigned());

        return ScheduleImportResult.builder()
                .success(true)
                .scheduleId(scheduleId)
                .jobId(jobId)
                .importTimestamp(LocalDateTime.now())
                .sectionsCreated(stats.getSectionsCreated())
                .slotsAssigned(stats.getSlotsAssigned())
                .studentsScheduled(stats.getStudentsScheduled())
                .hardScore(stats.getHardScore())
                .softScore(stats.getSoftScore())
                .message("Schedule imported successfully from SchedulerV2")
                .build();
    }

    // ========================================================================
    // DATA PROCESSING METHODS
    // ========================================================================

    /**
     * Process the schedule data from SchedulerV2
     */
    private ImportStatistics processScheduleData(Schedule schedule, Map<String, Object> exportData) {
        log.debug("Processing schedule data from SchedulerV2");

        ImportStatistics stats = new ImportStatistics();

        // Extract schedule slots from export data
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> slots = (List<Map<String, Object>>) exportData.get("scheduleSlots");

        if (slots == null || slots.isEmpty()) {
            log.warn("No schedule slots found in export data");
            return stats;
        }

        // Clear existing schedule slots for this schedule
        List<ScheduleSlot> existingSlots = scheduleSlotRepository.findByScheduleId(schedule.getId());
        if (!existingSlots.isEmpty()) {
            scheduleSlotRepository.deleteAll(existingSlots);
            log.info("Cleared {} existing schedule slots for schedule ID: {}", existingSlots.size(), schedule.getId());
        }

        // Process each slot
        for (Map<String, Object> slotData : slots) {
            try {
                processScheduleSlot(schedule, slotData, stats);
            } catch (Exception e) {
                log.error("Error processing schedule slot: {}", slotData, e);
                stats.incrementErrors();
            }
        }

        // Extract and update optimization scores
        if (exportData.containsKey("hardScore")) {
            stats.setHardScore(Integer.parseInt(String.valueOf(exportData.get("hardScore"))));
        }
        if (exportData.containsKey("softScore")) {
            stats.setSoftScore(Integer.parseInt(String.valueOf(exportData.get("softScore"))));
        }

        return stats;
    }

    /**
     * Process a single schedule slot
     */
    private void processScheduleSlot(Schedule schedule, Map<String, Object> slotData, ImportStatistics stats) {
        // Extract IDs from slot data
        Long courseId = getLongValue(slotData, "courseId");
        Long teacherId = getLongValue(slotData, "teacherId");
        Long roomId = getLongValue(slotData, "roomId");
        Long timeSlotId = getLongValue(slotData, "timeSlotId");
        Integer sectionNumber = getIntValue(slotData, "sectionNumber");

        // Validate required data
        if (courseId == null || teacherId == null || roomId == null || timeSlotId == null) {
            log.warn("Incomplete slot data, skipping: {}", slotData);
            return;
        }

        // Find entities
        Course course = courseRepository.findById(courseId).orElse(null);
        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        Room room = roomRepository.findById(roomId).orElse(null);
        PeriodTimer periodTimer = periodTimerRepository.findById(timeSlotId).orElse(null);

        if (course == null || teacher == null || room == null || periodTimer == null) {
            log.warn("Could not find required entities for slot, skipping");
            return;
        }

        // Create TimeSlot from PeriodTimer
        TimeSlot timeSlot = new TimeSlot(
                java.time.DayOfWeek.MONDAY, // Default to Monday for now
                periodTimer.getStartTime(),
                periodTimer.getEndTime(),
                periodTimer.getPeriodNumber()
        );

        // Create or update course section
        CourseSection section = findOrCreateCourseSection(schedule, course, sectionNumber);
        section.setAssignedTeacher(teacher);
        section.setAssignedRoom(room);
        section.setAssignedPeriod(periodTimer.getPeriodNumber());

        courseSectionRepository.save(section);
        stats.incrementSectionsCreated();

        // Create schedule slot
        ScheduleSlot slot = new ScheduleSlot();
        slot.setSchedule(schedule);
        slot.setCourse(course);
        slot.setTeacher(teacher);
        slot.setRoom(room);
        slot.setTimeSlot(timeSlot);

        scheduleSlotRepository.save(slot);
        stats.incrementSlotsAssigned();

        // Process student enrollments if present
        @SuppressWarnings("unchecked")
        List<Long> studentIds = (List<Long>) slotData.get("enrolledStudentIds");
        if (studentIds != null && !studentIds.isEmpty()) {
            processStudentEnrollments(section, studentIds, stats);
        }
    }

    /**
     * Find or create a course section
     */
    private CourseSection findOrCreateCourseSection(Schedule schedule, Course course, Integer sectionNumber) {
        // Try to find existing section by course and section number
        // Note: CourseSection doesn't have a schedule FK, so we can't filter by schedule
        List<CourseSection> existingSections = courseSectionRepository
                .findByCourseIdWithTeacherAndRoom(course.getId());

        for (CourseSection section : existingSections) {
            if (section.getSectionNumber() != null && section.getSectionNumber().equals(String.valueOf(sectionNumber))) {
                return section;
            }
        }

        // Create new section
        CourseSection newSection = new CourseSection();
        newSection.setCourse(course);
        newSection.setSectionNumber(String.valueOf(sectionNumber));
        newSection.setMaxEnrollment(course.getMaxStudents() != null ? course.getMaxStudents() : 30);
        newSection.setCurrentEnrollment(0);
        newSection.setSectionStatus(CourseSection.SectionStatus.SCHEDULED);

        return courseSectionRepository.save(newSection);
    }

    /**
     * Process student enrollments for a section
     */
    private void processStudentEnrollments(CourseSection section, List<Long> studentIds, ImportStatistics stats) {
        // TODO: Implement student enrollment creation
        // This would create StudentEnrollment records linking students to sections
        // For now, just update the enrollment count

        int enrollmentCount = studentIds.size();
        section.setCurrentEnrollment(enrollmentCount);
        courseSectionRepository.save(section);

        stats.addStudentsScheduled(enrollmentCount);
        log.debug("Enrolled {} students in section {}", enrollmentCount, section.getId());
    }

    /**
     * Update schedule metadata with optimization results
     */
    private void updateScheduleMetadata(Schedule schedule, Map<String, Object> exportData) {
        // Update optimization scores (use existing qualityScore field)
        if (exportData.containsKey("hardScore") && exportData.containsKey("softScore")) {
            Integer hardScore = Integer.parseInt(String.valueOf(exportData.get("hardScore")));
            Integer softScore = Integer.parseInt(String.valueOf(exportData.get("softScore")));

            // Store combined score in qualityScore field (0-100 scale)
            // Hard score violations are critical, so weight them heavily
            double quality = (hardScore == 0) ? Math.min(100, softScore) : 0;
            schedule.setQualityScore(quality);
        }

        // Update last modified date
        schedule.setLastModifiedDate(LocalDate.now());
        schedule.setLastModifiedBy("AI-Scheduler");

        scheduleRepository.save(schedule);
        log.debug("Updated schedule metadata for schedule ID: {}", schedule.getId());
    }

    // ========================================================================
    // VALIDATION METHODS
    // ========================================================================

    /**
     * Validate imported schedule for conflicts
     *
     * @param scheduleId Schedule ID to validate
     * @return Validation result with conflict list
     */
    @Transactional(readOnly = true)
    public ValidationResult validateImportedSchedule(Long scheduleId) {
        log.info("Validating imported schedule ID: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + scheduleId));

        List<String> conflicts = new ArrayList<>();
        int totalSlots = 0;

        // Get all schedule slots
        List<ScheduleSlot> slots = scheduleSlotRepository.findByScheduleId(scheduleId);
        totalSlots = slots.size();

        // Check for teacher conflicts (same teacher, same time)
        Map<String, List<ScheduleSlot>> teacherTimeMap = new HashMap<>();
        for (ScheduleSlot slot : slots) {
            if (slot.getTeacher() != null && slot.getTimeSlot() != null) {
                String key = slot.getTeacher().getId() + "-" + slot.getTimeSlot().getPeriodNumber();
                teacherTimeMap.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
            }
        }

        for (Map.Entry<String, List<ScheduleSlot>> entry : teacherTimeMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                ScheduleSlot first = entry.getValue().get(0);
                conflicts.add(String.format("Teacher conflict: %s has %d classes at period %d",
                        first.getTeacher().getName(),
                        entry.getValue().size(),
                        first.getTimeSlot().getPeriodNumber()));
            }
        }

        // Check for room conflicts (same room, same time)
        Map<String, List<ScheduleSlot>> roomTimeMap = new HashMap<>();
        for (ScheduleSlot slot : slots) {
            if (slot.getRoom() != null && slot.getTimeSlot() != null) {
                String key = slot.getRoom().getId() + "-" + slot.getTimeSlot().getPeriodNumber();
                roomTimeMap.computeIfAbsent(key, k -> new ArrayList<>()).add(slot);
            }
        }

        for (Map.Entry<String, List<ScheduleSlot>> entry : roomTimeMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                ScheduleSlot first = entry.getValue().get(0);
                conflicts.add(String.format("Room conflict: %s has %d classes at period %d",
                        first.getRoom().getRoomNumber(),
                        entry.getValue().size(),
                        first.getTimeSlot().getPeriodNumber()));
            }
        }

        boolean isValid = conflicts.isEmpty();
        log.info("Validation complete: {} total slots, {} conflicts found", totalSlots, conflicts.size());

        return ValidationResult.builder()
                .isValid(isValid)
                .totalSlots(totalSlots)
                .conflictCount(conflicts.size())
                .conflicts(conflicts)
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Safely extract Long value from map
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Safely extract Integer value from map
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // ========================================================================
    // HELPER CLASSES
    // ========================================================================

    /**
     * Import statistics
     */
    private static class ImportStatistics {
        private int sectionsCreated = 0;
        private int slotsAssigned = 0;
        private int studentsScheduled = 0;
        private int errors = 0;
        private Integer hardScore;
        private Integer softScore;

        public void incrementSectionsCreated() {
            sectionsCreated++;
        }

        public void incrementSlotsAssigned() {
            slotsAssigned++;
        }

        public void addStudentsScheduled(int count) {
            studentsScheduled += count;
        }

        public void incrementErrors() {
            errors++;
        }

        public int getSectionsCreated() {
            return sectionsCreated;
        }

        public int getSlotsAssigned() {
            return slotsAssigned;
        }

        public int getStudentsScheduled() {
            return studentsScheduled;
        }

        public Integer getHardScore() {
            return hardScore;
        }

        public void setHardScore(Integer hardScore) {
            this.hardScore = hardScore;
        }

        public Integer getSoftScore() {
            return softScore;
        }

        public void setSoftScore(Integer softScore) {
            this.softScore = softScore;
        }
    }

    /**
     * Import result DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ScheduleImportResult {
        private Boolean success;
        private Long scheduleId;
        private String jobId;
        private LocalDateTime importTimestamp;
        private Integer sectionsCreated;
        private Integer slotsAssigned;
        private Integer studentsScheduled;
        private Integer hardScore;
        private Integer softScore;
        private String message;
    }

    /**
     * Validation result DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ValidationResult {
        private Boolean isValid;
        private Integer totalSlots;
        private Integer conflictCount;
        private List<String> conflicts;
    }
}
