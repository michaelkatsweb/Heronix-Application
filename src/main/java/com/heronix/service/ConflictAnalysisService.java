package com.heronix.service;

import com.heronix.model.domain.*;
import com.heronix.model.dto.ConflictDetail;
import com.heronix.model.dto.ConflictDetail.ManualOverrideOption;
import com.heronix.model.enums.ConflictSeverity;
import com.heronix.model.enums.ConflictType;
import com.heronix.repository.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Conflict Analysis Service
 *
 * Analyzes scheduling solutions to identify and categorize conflicts,
 * providing detailed diagnostic information and suggested solutions.
 *
 * This service is critical for the partial scheduling feature - it enables
 * the system to generate schedules even when hard constraints can't be fully
 * satisfied, by providing users with actionable conflict reports.
 *
 * Location: src/main/java/com/heronix/service/ConflictAnalysisService.java
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-11-18
 */
@Slf4j
@Service
public class ConflictAnalysisService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ScheduleSlotRepository scheduleSlotRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Analyze schedule slots to identify all conflicts
     *
     * @param slots The schedule slots to analyze
     * @return List of detailed conflict information
     */
    public List<ConflictDetail> analyzeSlotConflicts(List<ScheduleSlot> slots) {
        log.info("Starting conflict analysis for schedule slots");

        List<ConflictDetail> conflicts = new ArrayList<>();

        if (slots == null || slots.isEmpty()) {
            log.warn("Cannot analyze null or empty slot list");
            return conflicts;
        }

        log.info("Analyzing {} schedule slots for conflicts", slots.size());

        // Analyze each slot for assignment issues
        for (ScheduleSlot slot : slots) {
            if (!isSlotFullyAssigned(slot)) {
                ConflictDetail conflict = analyzeUnassignedSlot(slot);
                if (conflict != null) {
                    conflicts.add(conflict);
                }
            } else {
                // Check for constraint violations even in assigned slots
                List<ConflictDetail> slotConflicts = analyzeAssignedSlot(slot);
                conflicts.addAll(slotConflicts);
            }
        }

        // Analyze cross-slot conflicts (teacher/room double-booking)
        List<ConflictDetail> crossSlotConflicts = analyzeCrossSlotConflicts(slots);
        conflicts.addAll(crossSlotConflicts);

        // Sort by severity (most severe first)
        conflicts.sort((c1, c2) -> {
            if (c1.getSeverity() == null) return 1;
            if (c2.getSeverity() == null) return -1;
            return Integer.compare(
                c2.getSeverity().getPriorityScore(),
                c1.getSeverity().getPriorityScore()
            );
        });

        log.info("Conflict analysis complete: {} conflicts identified", conflicts.size());
        return conflicts;
    }

    /**
     * Check if a slot is fully assigned (has teacher, room, and time)
     */
    private boolean isSlotFullyAssigned(ScheduleSlot slot) {
        return slot.getTeacher() != null &&
               slot.getRoom() != null &&
               slot.getTimeSlot() != null;
    }

    /**
     * Analyze an unassigned slot to determine why it couldn't be scheduled
     */
    private ConflictDetail analyzeUnassignedSlot(ScheduleSlot slot) {
        if (slot.getCourse() == null) {
            log.warn("Slot {} has no course assigned", slot.getId());
            return null;
        }

        Course course = slot.getCourse();
        ConflictDetail.ConflictDetailBuilder builder = ConflictDetail.builder()
            .slotId(slot.getId())
            .courseId(course.getId())
            .courseName(course.getCourseName())
            .courseCode(course.getCourseCode())
            .studentsAffected(slot.getStudents() != null ? slot.getStudents().size() : 0)
            .blocking(true);

        // Determine specific reason for non-assignment
        if (slot.getTeacher() == null) {
            return analyzeNoTeacherConflict(slot, course, builder);
        } else if (slot.getRoom() == null) {
            return analyzeNoRoomConflict(slot, course, builder);
        } else if (slot.getTimeSlot() == null) {
            return analyzeNoTimeSlotConflict(slot, course, builder);
        }

        return null;
    }

    /**
     * Analyze why no teacher could be assigned
     */
    private ConflictDetail analyzeNoTeacherConflict(
            ScheduleSlot slot,
            Course course,
            ConflictDetail.ConflictDetailBuilder builder) {

        // Check if course has a teacher assigned in database
        if (course.getTeacher() == null || course.getTeacher().getId() == null) {
            return builder
                .type(ConflictType.TEACHER_OVERLOAD) // Using existing enum value
                .severity(ConflictSeverity.CRITICAL)
                .description("This course has no teacher assigned")
                .violatedConstraint("Course requires assigned teacher")
                .estimatedFixTimeMinutes(5)
                .possibleSolutions(List.of(
                    "Assign a qualified teacher to " + course.getCourseCode() + " in the Courses section",
                    "Import teachers with course assignments using CSV import",
                    "Run the automated course assignment utility (ASSIGN_COURSES_NOW.bat)"
                ))
                .overrideOptions(List.of(
                    ManualOverrideOption.builder()
                        .label("Assign any available teacher")
                        .action("ASSIGN_ANY_TEACHER")
                        .requiresConfirmation(true)
                        .warningMessage("Teacher may not be qualified for this subject")
                        .build()
                ))
                .build();
        }

        // Teacher exists but is overloaded
        List<Teacher> availableTeachers = teacherRepository.findAllActive().stream()
            .filter(t -> Boolean.TRUE.equals(t.getActive()))
            .collect(Collectors.toList());

        return builder
            .type(ConflictType.TEACHER_OVERLOAD)
            .severity(ConflictSeverity.HIGH)
            .description("All qualified teachers are at maximum capacity")
            .violatedConstraint("Teacher maximum periods per day exceeded")
            .estimatedFixTimeMinutes(10)
            .possibleSolutions(List.of(
                "Hire additional " + course.getSubject() + " teachers",
                "Increase max periods per day for existing teachers",
                "Reduce number of course sections",
                availableTeachers.isEmpty() ?
                    "Add teachers to the system" :
                    "Consider assigning to: " + availableTeachers.stream()
                        .limit(3)
                        .map(t -> t.getFirstName() + " " + t.getLastName())
                        .collect(Collectors.joining(", "))
            ))
            .build();
    }

    /**
     * Analyze why no room could be assigned
     */
    private ConflictDetail analyzeNoRoomConflict(
            ScheduleSlot slot,
            Course course,
            ConflictDetail.ConflictDetailBuilder builder) {

        // Check for special room requirements
        if (Boolean.TRUE.equals(course.getRequiresLab())) {
            long labCount = roomRepository.findAll().stream()
                .filter(r -> r.getType() != null && r.getType().isLab())
                .count();

            if (labCount == 0) {
                return builder
                    .type(ConflictType.ROOM_TYPE_MISMATCH)
                    .severity(ConflictSeverity.CRITICAL)
                    .description("Course requires a lab but no lab rooms exist in the system")
                    .violatedConstraint("Lab course requires lab room")
                    .estimatedFixTimeMinutes(15)
                    .possibleSolutions(List.of(
                        "Add a lab room in the Rooms section (Type: LAB or SCIENCE_LAB)",
                        "Convert an existing room to a lab",
                        "Mark course as not requiring a lab (if incorrect)"
                    ))
                    .overrideOptions(List.of(
                        ManualOverrideOption.builder()
                            .label("Schedule in regular classroom anyway")
                            .action("OVERRIDE_LAB_REQUIREMENT")
                            .requiresConfirmation(true)
                            .warningMessage("This course requires lab equipment which may not be available in a regular classroom")
                            .build()
                    ))
                    .build();
            }
        }

        // Check for PE/Gym requirement
        String subject = course.getSubject();
        if (subject != null && (subject.toLowerCase().contains("physical education") ||
                                subject.toLowerCase().contains("pe") ||
                                subject.toLowerCase().contains("gym"))) {
            long gymCount = roomRepository.findAll().stream()
                .filter(r -> r.getType() != null &&
                           (r.getType().name().equals("GYMNASIUM") || r.getType().name().equals("GYM")))
                .count();

            if (gymCount == 0) {
                return builder
                    .type(ConflictType.ROOM_TYPE_MISMATCH)
                    .severity(ConflictSeverity.CRITICAL)
                    .description("PE course requires a gymnasium but none exist in the system")
                    .violatedConstraint("PE courses require gymnasium")
                    .estimatedFixTimeMinutes(10)
                    .possibleSolutions(List.of(
                        "Add a gymnasium in the Rooms section (Type: GYMNASIUM)",
                        "Convert an existing room to gymnasium type",
                        "Schedule PE classes off-campus (if applicable)"
                    ))
                    .overrideOptions(List.of(
                        ManualOverrideOption.builder()
                            .label("Schedule in regular classroom")
                            .action("OVERRIDE_GYM_REQUIREMENT")
                            .requiresConfirmation(true)
                            .warningMessage("PE classes typically require a gymnasium for proper instruction")
                            .build()
                    ))
                    .build();
            }
        }

        // General room capacity issue
        int studentCount = slot.getStudents() != null ? slot.getStudents().size() : 0;
        List<Room> availableRooms = roomRepository.findAll().stream()
            .filter(r -> r.getCapacity() != null && r.getCapacity() >= studentCount)
            .collect(Collectors.toList());

        if (availableRooms.isEmpty()) {
            return builder
                .type(ConflictType.ROOM_CAPACITY_EXCEEDED)
                .severity(ConflictSeverity.HIGH)
                .description("No room large enough for " + studentCount + " students")
                .violatedConstraint("Room capacity must accommodate all enrolled students")
                .estimatedFixTimeMinutes(10)
                .possibleSolutions(List.of(
                    "Add a larger room (capacity: " + studentCount + "+)",
                    "Split course into multiple sections",
                    "Reduce course enrollment",
                    "Increase capacity of existing rooms"
                ))
                .build();
        }

        // All rooms at capacity at this time
        return builder
            .type(ConflictType.ROOM_DOUBLE_BOOKING)
            .severity(ConflictSeverity.HIGH)
            .description("All suitable rooms are occupied at available time slots")
            .violatedConstraint("Room availability exhausted")
            .estimatedFixTimeMinutes(5)
            .possibleSolutions(List.of(
                "Add more rooms to the system",
                "Extend school hours to create more time slots",
                availableRooms.isEmpty() ? "No suggestions available" :
                    "Consider using: " + availableRooms.stream()
                        .limit(3)
                        .map(Room::getRoomNumber)
                        .collect(Collectors.joining(", "))
            ))
            .build();
    }

    /**
     * Analyze why no time slot could be assigned
     */
    private ConflictDetail analyzeNoTimeSlotConflict(
            ScheduleSlot slot,
            Course course,
            ConflictDetail.ConflictDetailBuilder builder) {

        return builder
            .type(ConflictType.TIME_OVERLAP)
            .severity(ConflictSeverity.HIGH)
            .description("All available time slots have been exhausted")
            .violatedConstraint("No available time slots remain")
            .estimatedFixTimeMinutes(5)
            .possibleSolutions(List.of(
                "Extend school hours (add periods to start/end of day)",
                "Reduce number of course sections",
                "Enable lunch period scheduling",
                "Add more rooms to increase capacity"
            ))
            .build();
    }

    /**
     * Analyze assigned slot for constraint violations
     */
    private List<ConflictDetail> analyzeAssignedSlot(ScheduleSlot slot) {
        List<ConflictDetail> conflicts = new ArrayList<>();

        if (slot.getCourse() == null || slot.getRoom() == null) {
            return conflicts;
        }

        // Check room capacity
        int studentCount = slot.getStudents() != null ? slot.getStudents().size() : 0;
        Integer roomCapacity = slot.getRoom().getCapacity();

        if (roomCapacity != null && studentCount > roomCapacity) {
            int overflow = studentCount - roomCapacity;
            conflicts.add(ConflictDetail.builder()
                .slotId(slot.getId())
                .courseId(slot.getCourse().getId())
                .courseName(slot.getCourse().getCourseName())
                .courseCode(slot.getCourse().getCourseCode())
                .type(ConflictType.ROOM_CAPACITY_EXCEEDED)
                .severity(ConflictSeverity.MEDIUM)
                .description("Room " + slot.getRoom().getRoomNumber() + " capacity exceeded by " + overflow + " students")
                .violatedConstraint("Room capacity: " + roomCapacity + ", Students: " + studentCount)
                .studentsAffected(overflow)
                .blocking(false)
                .estimatedFixTimeMinutes(5)
                .possibleSolutions(List.of(
                    "Move to larger room",
                    "Split into 2 sections",
                    "Reduce enrollment by " + overflow + " students",
                    "Temporarily allow overflow (accept the risk)"
                ))
                .relatedEntityIds(List.of(slot.getRoom().getId()))
                .build()
            );
        }

        return conflicts;
    }

    /**
     * Analyze cross-slot conflicts (teacher/room double-booking)
     */
    private List<ConflictDetail> analyzeCrossSlotConflicts(List<ScheduleSlot> slots) {
        List<ConflictDetail> conflicts = new ArrayList<>();

        // Group slots by day and period
        Map<String, List<ScheduleSlot>> slotsByTime = new HashMap<>();

        for (ScheduleSlot slot : slots) {
            if (slot.getTimeSlot() == null) continue;

            String timeKey = slot.getDayOfWeek() + "_" + slot.getPeriodNumber();
            slotsByTime.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(slot);
        }

        // Check each time slot for conflicts
        for (Map.Entry<String, List<ScheduleSlot>> entry : slotsByTime.entrySet()) {
            List<ScheduleSlot> slotsAtTime = entry.getValue();
            if (slotsAtTime.size() <= 1) continue;

            // Check for teacher conflicts
            Map<Long, List<ScheduleSlot>> slotsByTeacher = slotsAtTime.stream()
                .filter(s -> s.getTeacher() != null && s.getTeacher().getId() != null)
                .collect(Collectors.groupingBy(s -> s.getTeacher().getId()));

            for (Map.Entry<Long, List<ScheduleSlot>> teacherEntry : slotsByTeacher.entrySet()) {
                if (teacherEntry.getValue().size() > 1) {
                    conflicts.add(createTeacherConflict(teacherEntry.getValue()));
                }
            }

            // Check for room conflicts
            Map<Long, List<ScheduleSlot>> slotsByRoom = slotsAtTime.stream()
                .filter(s -> s.getRoom() != null && s.getRoom().getId() != null)
                .collect(Collectors.groupingBy(s -> s.getRoom().getId()));

            for (Map.Entry<Long, List<ScheduleSlot>> roomEntry : slotsByRoom.entrySet()) {
                if (roomEntry.getValue().size() > 1) {
                    conflicts.add(createRoomConflict(roomEntry.getValue()));
                }
            }
        }

        return conflicts;
    }

    /**
     * Create conflict detail for teacher double-booking
     */
    private ConflictDetail createTeacherConflict(List<ScheduleSlot> conflictingSlots) {
        ScheduleSlot firstSlot = conflictingSlots.get(0);
        Teacher teacher = firstSlot.getTeacher();

        String coursesList = conflictingSlots.stream()
            .map(s -> s.getCourse() != null ? s.getCourse().getCourseCode() : "Unknown")
            .collect(Collectors.joining(", "));

        int totalStudents = conflictingSlots.stream()
            .mapToInt(s -> s.getStudents() != null ? s.getStudents().size() : 0)
            .sum();

        return ConflictDetail.builder()
            .slotId(firstSlot.getId())
            .courseId(firstSlot.getCourse() != null ? firstSlot.getCourse().getId() : null)
            .courseName(coursesList)
            .type(ConflictType.TEACHER_OVERLOAD)
            .severity(ConflictSeverity.CRITICAL)
            .description("Teacher " + teacher.getFirstName() + " " + teacher.getLastName() +
                        " is assigned to " + conflictingSlots.size() + " classes at the same time")
            .violatedConstraint("Teacher can only teach one class at a time")
            .studentsAffected(totalStudents)
            .blocking(true)
            .estimatedFixTimeMinutes(10)
            .possibleSolutions(List.of(
                "Reassign one of these courses to a different teacher",
                "Move one course to a different time slot",
                "Cancel or combine duplicate sections"
            ))
            .relatedEntityIds(conflictingSlots.stream()
                .map(ScheduleSlot::getId)
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Create conflict detail for room double-booking
     */
    private ConflictDetail createRoomConflict(List<ScheduleSlot> conflictingSlots) {
        ScheduleSlot firstSlot = conflictingSlots.get(0);
        Room room = firstSlot.getRoom();

        String coursesList = conflictingSlots.stream()
            .map(s -> s.getCourse() != null ? s.getCourse().getCourseCode() : "Unknown")
            .collect(Collectors.joining(", "));

        int totalStudents = conflictingSlots.stream()
            .mapToInt(s -> s.getStudents() != null ? s.getStudents().size() : 0)
            .sum();

        return ConflictDetail.builder()
            .slotId(firstSlot.getId())
            .courseId(firstSlot.getCourse() != null ? firstSlot.getCourse().getId() : null)
            .courseName(coursesList)
            .type(ConflictType.ROOM_DOUBLE_BOOKING)
            .severity(ConflictSeverity.CRITICAL)
            .description("Room " + room.getRoomNumber() + " is assigned to " +
                        conflictingSlots.size() + " classes at the same time")
            .violatedConstraint("Room can only host one class at a time")
            .studentsAffected(totalStudents)
            .blocking(true)
            .estimatedFixTimeMinutes(5)
            .possibleSolutions(List.of(
                "Move one course to a different room",
                "Move one course to a different time slot",
                "Add more rooms to the system"
            ))
            .relatedEntityIds(conflictingSlots.stream()
                .map(ScheduleSlot::getId)
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Calculate completion percentage for a list of slots
     *
     * @param slots Schedule slots to evaluate
     * @return Percentage of slots fully assigned (0-100)
     */
    public double calculateCompletionPercentage(List<ScheduleSlot> slots) {
        if (slots == null || slots.isEmpty()) {
            return 0.0;
        }

        long assignedCount = slots.stream()
            .filter(this::isSlotFullyAssigned)
            .count();

        return (assignedCount * 100.0) / slots.size();
    }

    // ========================================================================
    // NEW API-FACING METHODS
    // ========================================================================

    /**
     * Get student schedule conflicts for a specific term
     */
    public StudentConflictsResult getStudentConflicts(Long studentId, Long termId) {
        log.info("Analyzing conflicts for student {} in term {}", studentId, termId);

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return StudentConflictsResult.builder()
                .studentId(studentId)
                .conflicts(Collections.emptyList())
                .hasConflicts(false)
                .build();
        }

        List<ScheduleSlot> studentSlots = scheduleSlotRepository.findAll().stream()
            .filter(slot -> slot.getStudents() != null &&
                           slot.getStudents().stream().anyMatch(s -> s.getId().equals(studentId)))
            .collect(Collectors.toList());

        List<StudentConflictInfo> conflicts = new ArrayList<>();

        // Check for time overlaps
        Map<String, List<ScheduleSlot>> slotsByTime = new HashMap<>();
        for (ScheduleSlot slot : studentSlots) {
            if (slot.getDayOfWeek() == null || slot.getStartTime() == null) continue;
            String timeKey = slot.getDayOfWeek() + "_" + slot.getPeriodNumber();
            slotsByTime.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(slot);
        }

        for (Map.Entry<String, List<ScheduleSlot>> entry : slotsByTime.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<ScheduleSlot> conflictingSlots = entry.getValue();
                conflicts.add(StudentConflictInfo.builder()
                    .conflictType("TIME_OVERLAP")
                    .severity("CRITICAL")
                    .description("Student is enrolled in " + conflictingSlots.size() +
                                " classes at the same time")
                    .dayOfWeek(conflictingSlots.get(0).getDayOfWeek() != null ?
                              conflictingSlots.get(0).getDayOfWeek().name() : null)
                    .periodNumber(conflictingSlots.get(0).getPeriodNumber())
                    .conflictingCourses(conflictingSlots.stream()
                        .filter(s -> s.getCourse() != null)
                        .map(s -> s.getCourse().getCourseCode())
                        .collect(Collectors.toList()))
                    .slotIds(conflictingSlots.stream()
                        .map(ScheduleSlot::getId)
                        .collect(Collectors.toList()))
                    .suggestions(List.of(
                        "Drop one of the conflicting courses",
                        "Move to a different section of one course",
                        "Request schedule adjustment from counselor"
                    ))
                    .build());
            }
        }

        return StudentConflictsResult.builder()
            .studentId(studentId)
            .studentName(student.getFirstName() + " " + student.getLastName())
            .termId(termId)
            .conflicts(conflicts)
            .hasConflicts(!conflicts.isEmpty())
            .conflictCount(conflicts.size())
            .build();
    }

    /**
     * Check if adding a course would create conflicts for a student
     */
    public CourseAdditionCheck checkCourseAdditionConflicts(Long studentId, Long courseId,
                                                            Long sectionId, Long termId) {
        log.info("Checking course addition conflicts: student={}, course={}", studentId, courseId);

        Student student = studentRepository.findById(studentId).orElse(null);
        Course course = courseRepository.findById(courseId).orElse(null);

        if (student == null || course == null) {
            return CourseAdditionCheck.builder()
                .canEnroll(false)
                .hasConflicts(true)
                .conflicts(List.of(ConflictInfo.builder()
                    .type("INVALID_DATA")
                    .description("Student or course not found")
                    .build()))
                .build();
        }

        // Get the course's schedule slots
        List<ScheduleSlot> courseSlots = scheduleSlotRepository.findByCourseIdWithDetails(courseId);

        // Get student's current schedule
        List<ScheduleSlot> studentSlots = scheduleSlotRepository.findAll().stream()
            .filter(slot -> slot.getStudents() != null &&
                           slot.getStudents().stream().anyMatch(s -> s.getId().equals(studentId)))
            .collect(Collectors.toList());

        List<ConflictInfo> conflicts = new ArrayList<>();

        // Check for time conflicts
        for (ScheduleSlot courseSlot : courseSlots) {
            if (courseSlot.getDayOfWeek() == null || courseSlot.getStartTime() == null) continue;

            for (ScheduleSlot studentSlot : studentSlots) {
                if (studentSlot.getDayOfWeek() == null || studentSlot.getStartTime() == null) continue;

                if (courseSlot.getDayOfWeek().equals(studentSlot.getDayOfWeek()) &&
                    timesOverlap(courseSlot.getStartTime(), courseSlot.getEndTime(),
                                studentSlot.getStartTime(), studentSlot.getEndTime())) {

                    conflicts.add(ConflictInfo.builder()
                        .type("TIME_CONFLICT")
                        .severity("CRITICAL")
                        .description("Conflicts with " +
                            (studentSlot.getCourse() != null ? studentSlot.getCourse().getCourseCode() : "existing class") +
                            " on " + courseSlot.getDayOfWeek())
                        .conflictingSlotId(studentSlot.getId())
                        .conflictingCourseCode(studentSlot.getCourse() != null ?
                            studentSlot.getCourse().getCourseCode() : null)
                        .dayOfWeek(courseSlot.getDayOfWeek().name())
                        .time(courseSlot.getStartTime() + " - " + courseSlot.getEndTime())
                        .build());
                }
            }
        }

        // Check room capacity
        for (ScheduleSlot courseSlot : courseSlots) {
            if (courseSlot.getRoom() != null && courseSlot.getRoom().getCapacity() != null) {
                int currentEnrollment = courseSlot.getStudents() != null ? courseSlot.getStudents().size() : 0;
                if (currentEnrollment >= courseSlot.getRoom().getCapacity()) {
                    conflicts.add(ConflictInfo.builder()
                        .type("CAPACITY_EXCEEDED")
                        .severity("MEDIUM")
                        .description("Section is at capacity (" + currentEnrollment + "/" +
                                    courseSlot.getRoom().getCapacity() + ")")
                        .build());
                }
            }
        }

        return CourseAdditionCheck.builder()
            .studentId(studentId)
            .courseId(courseId)
            .sectionId(sectionId)
            .termId(termId)
            .canEnroll(conflicts.isEmpty())
            .hasConflicts(!conflicts.isEmpty())
            .conflicts(conflicts)
            .alternativeSections(conflicts.isEmpty() ? Collections.emptyList() :
                findAlternativeSections(courseId, studentSlots))
            .build();
    }

    /**
     * Get teacher schedule conflicts
     */
    public TeacherConflictsResult getTeacherConflicts(Long teacherId, Long termId) {
        log.info("Analyzing conflicts for teacher {} in term {}", teacherId, termId);

        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        if (teacher == null) {
            return TeacherConflictsResult.builder()
                .teacherId(teacherId)
                .conflicts(Collections.emptyList())
                .hasConflicts(false)
                .build();
        }

        List<ScheduleSlot> teacherSlots = scheduleSlotRepository.findByTeacherIdWithDetails(teacherId);
        List<TeacherConflictInfo> conflicts = new ArrayList<>();

        // Check for double-booking
        Map<String, List<ScheduleSlot>> slotsByTime = new HashMap<>();
        for (ScheduleSlot slot : teacherSlots) {
            if (slot.getDayOfWeek() == null || slot.getPeriodNumber() == null) continue;
            String timeKey = slot.getDayOfWeek() + "_" + slot.getPeriodNumber();
            slotsByTime.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(slot);
        }

        for (Map.Entry<String, List<ScheduleSlot>> entry : slotsByTime.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<ScheduleSlot> conflictingSlots = entry.getValue();
                conflicts.add(TeacherConflictInfo.builder()
                    .conflictType("DOUBLE_BOOKING")
                    .severity("CRITICAL")
                    .description("Teacher assigned to " + conflictingSlots.size() +
                                " classes at the same time")
                    .dayOfWeek(conflictingSlots.get(0).getDayOfWeek() != null ?
                              conflictingSlots.get(0).getDayOfWeek().name() : null)
                    .periodNumber(conflictingSlots.get(0).getPeriodNumber())
                    .conflictingCourses(conflictingSlots.stream()
                        .filter(s -> s.getCourse() != null)
                        .map(s -> s.getCourse().getCourseCode())
                        .collect(Collectors.toList()))
                    .slotIds(conflictingSlots.stream()
                        .map(ScheduleSlot::getId)
                        .collect(Collectors.toList()))
                    .suggestions(List.of(
                        "Assign a different teacher to one of the courses",
                        "Move one course to a different time slot",
                        "Find a substitute teacher"
                    ))
                    .build());
            }
        }

        // Check for overload (too many periods per day)
        Map<DayOfWeek, Long> periodsPerDay = teacherSlots.stream()
            .filter(s -> s.getDayOfWeek() != null)
            .collect(Collectors.groupingBy(ScheduleSlot::getDayOfWeek, Collectors.counting()));

        int maxPeriodsPerDay = teacher.getMaxPeriodsPerDay() != null ? teacher.getMaxPeriodsPerDay() : 8;
        for (Map.Entry<DayOfWeek, Long> entry : periodsPerDay.entrySet()) {
            if (entry.getValue() > maxPeriodsPerDay) {
                conflicts.add(TeacherConflictInfo.builder()
                    .conflictType("OVERLOAD")
                    .severity("HIGH")
                    .description("Teacher has " + entry.getValue() + " periods on " +
                                entry.getKey() + " (max: " + maxPeriodsPerDay + ")")
                    .dayOfWeek(entry.getKey().name())
                    .suggestions(List.of(
                        "Reduce teacher's course load",
                        "Redistribute classes across days",
                        "Increase teacher's max periods setting"
                    ))
                    .build());
            }
        }

        return TeacherConflictsResult.builder()
            .teacherId(teacherId)
            .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
            .termId(termId)
            .conflicts(conflicts)
            .hasConflicts(!conflicts.isEmpty())
            .conflictCount(conflicts.size())
            .totalPeriodsPerWeek(teacherSlots.size())
            .build();
    }

    /**
     * Get teacher availability
     */
    public TeacherAvailabilityResult getTeacherAvailability(Long teacherId, Long termId, String dayOfWeekStr) {
        log.info("Getting availability for teacher {} on {}", teacherId, dayOfWeekStr);

        Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
        if (teacher == null) {
            return TeacherAvailabilityResult.builder()
                .teacherId(teacherId)
                .availableSlots(Collections.emptyList())
                .build();
        }

        List<ScheduleSlot> teacherSlots = scheduleSlotRepository.findByTeacherIdWithDetails(teacherId);

        // Filter by day if specified
        if (dayOfWeekStr != null && !dayOfWeekStr.isEmpty()) {
            try {
                DayOfWeek day = DayOfWeek.valueOf(dayOfWeekStr.toUpperCase());
                teacherSlots = teacherSlots.stream()
                    .filter(s -> day.equals(s.getDayOfWeek()))
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid day of week: {}", dayOfWeekStr);
            }
        }

        // Build occupied periods
        Set<String> occupiedPeriods = teacherSlots.stream()
            .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
            .map(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber())
            .collect(Collectors.toSet());

        // Generate available slots (periods 1-8 for each day)
        List<AvailableSlot> availableSlots = new ArrayList<>();
        List<DayOfWeek> daysToCheck = dayOfWeekStr != null ?
            List.of(DayOfWeek.valueOf(dayOfWeekStr.toUpperCase())) :
            List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                   DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

        for (DayOfWeek day : daysToCheck) {
            for (int period = 1; period <= 8; period++) {
                String key = day + "_" + period;
                if (!occupiedPeriods.contains(key)) {
                    availableSlots.add(AvailableSlot.builder()
                        .dayOfWeek(day.name())
                        .periodNumber(period)
                        .startTime(getDefaultPeriodStartTime(period))
                        .endTime(getDefaultPeriodEndTime(period))
                        .available(true)
                        .build());
                }
            }
        }

        return TeacherAvailabilityResult.builder()
            .teacherId(teacherId)
            .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
            .termId(termId)
            .dayOfWeek(dayOfWeekStr)
            .availableSlots(availableSlots)
            .totalAvailable(availableSlots.size())
            .occupiedSlots(occupiedPeriods.size())
            .build();
    }

    /**
     * Get room conflicts
     */
    public RoomConflictsResult getRoomConflicts(Long roomId, Long termId) {
        log.info("Analyzing conflicts for room {} in term {}", roomId, termId);

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return RoomConflictsResult.builder()
                .roomId(roomId)
                .conflicts(Collections.emptyList())
                .hasConflicts(false)
                .build();
        }

        List<ScheduleSlot> roomSlots = scheduleSlotRepository.findByRoomIdWithDetails(roomId);
        List<RoomConflictInfo> conflicts = new ArrayList<>();

        // Check for double-booking
        Map<String, List<ScheduleSlot>> slotsByTime = new HashMap<>();
        for (ScheduleSlot slot : roomSlots) {
            if (slot.getDayOfWeek() == null || slot.getPeriodNumber() == null) continue;
            String timeKey = slot.getDayOfWeek() + "_" + slot.getPeriodNumber();
            slotsByTime.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(slot);
        }

        for (Map.Entry<String, List<ScheduleSlot>> entry : slotsByTime.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<ScheduleSlot> conflictingSlots = entry.getValue();
                conflicts.add(RoomConflictInfo.builder()
                    .conflictType("DOUBLE_BOOKING")
                    .severity("CRITICAL")
                    .description("Room booked for " + conflictingSlots.size() +
                                " classes at the same time")
                    .dayOfWeek(conflictingSlots.get(0).getDayOfWeek() != null ?
                              conflictingSlots.get(0).getDayOfWeek().name() : null)
                    .periodNumber(conflictingSlots.get(0).getPeriodNumber())
                    .conflictingCourses(conflictingSlots.stream()
                        .filter(s -> s.getCourse() != null)
                        .map(s -> s.getCourse().getCourseCode())
                        .collect(Collectors.toList()))
                    .slotIds(conflictingSlots.stream()
                        .map(ScheduleSlot::getId)
                        .collect(Collectors.toList()))
                    .suggestions(List.of(
                        "Move one class to a different room",
                        "Reschedule one class to a different time",
                        "Combine sections if appropriate"
                    ))
                    .build());
            }
        }

        // Check for capacity issues
        for (ScheduleSlot slot : roomSlots) {
            int studentCount = slot.getStudents() != null ? slot.getStudents().size() : 0;
            if (room.getCapacity() != null && studentCount > room.getCapacity()) {
                conflicts.add(RoomConflictInfo.builder()
                    .conflictType("CAPACITY_EXCEEDED")
                    .severity("MEDIUM")
                    .description("Room capacity exceeded: " + studentCount + "/" + room.getCapacity() +
                                " for " + (slot.getCourse() != null ? slot.getCourse().getCourseCode() : "class"))
                    .dayOfWeek(slot.getDayOfWeek() != null ? slot.getDayOfWeek().name() : null)
                    .periodNumber(slot.getPeriodNumber())
                    .slotIds(List.of(slot.getId()))
                    .suggestions(List.of(
                        "Move to a larger room",
                        "Split into multiple sections",
                        "Reduce enrollment"
                    ))
                    .build());
            }
        }

        return RoomConflictsResult.builder()
            .roomId(roomId)
            .roomNumber(room.getRoomNumber())
            .capacity(room.getCapacity())
            .termId(termId)
            .conflicts(conflicts)
            .hasConflicts(!conflicts.isEmpty())
            .conflictCount(conflicts.size())
            .build();
    }

    /**
     * Get room availability
     */
    public RoomAvailabilityResult getRoomAvailability(Long roomId, Long termId, String dayOfWeekStr) {
        log.info("Getting availability for room {} on {}", roomId, dayOfWeekStr);

        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return RoomAvailabilityResult.builder()
                .roomId(roomId)
                .availableSlots(Collections.emptyList())
                .build();
        }

        List<ScheduleSlot> roomSlots = scheduleSlotRepository.findByRoomIdWithDetails(roomId);

        // Build occupied periods
        Set<String> occupiedPeriods = roomSlots.stream()
            .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
            .map(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber())
            .collect(Collectors.toSet());

        // Generate available slots
        List<AvailableSlot> availableSlots = new ArrayList<>();
        List<DayOfWeek> daysToCheck = dayOfWeekStr != null && !dayOfWeekStr.isEmpty() ?
            List.of(DayOfWeek.valueOf(dayOfWeekStr.toUpperCase())) :
            List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                   DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

        for (DayOfWeek day : daysToCheck) {
            for (int period = 1; period <= 8; period++) {
                String key = day + "_" + period;
                if (!occupiedPeriods.contains(key)) {
                    availableSlots.add(AvailableSlot.builder()
                        .dayOfWeek(day.name())
                        .periodNumber(period)
                        .startTime(getDefaultPeriodStartTime(period))
                        .endTime(getDefaultPeriodEndTime(period))
                        .available(true)
                        .build());
                }
            }
        }

        return RoomAvailabilityResult.builder()
            .roomId(roomId)
            .roomNumber(room.getRoomNumber())
            .capacity(room.getCapacity())
            .termId(termId)
            .dayOfWeek(dayOfWeekStr)
            .availableSlots(availableSlots)
            .totalAvailable(availableSlots.size())
            .occupiedSlots(occupiedPeriods.size())
            .build();
    }

    /**
     * Get all conflicts for a term
     */
    public AllConflictsResult getAllConflicts(Long termId, String severityFilter) {
        log.info("Getting all conflicts for term {} with severity filter {}", termId, severityFilter);

        List<ScheduleSlot> allSlots = scheduleSlotRepository.findAll();
        List<AllConflictInfo> conflicts = new ArrayList<>();

        // Analyze teacher conflicts
        Map<Long, List<ScheduleSlot>> slotsByTeacher = allSlots.stream()
            .filter(s -> s.getTeacher() != null)
            .collect(Collectors.groupingBy(s -> s.getTeacher().getId()));

        for (Map.Entry<Long, List<ScheduleSlot>> entry : slotsByTeacher.entrySet()) {
            Map<String, List<ScheduleSlot>> slotsByTime = entry.getValue().stream()
                .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
                .collect(Collectors.groupingBy(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber()));

            for (Map.Entry<String, List<ScheduleSlot>> timeEntry : slotsByTime.entrySet()) {
                if (timeEntry.getValue().size() > 1) {
                    ScheduleSlot first = timeEntry.getValue().get(0);
                    conflicts.add(AllConflictInfo.builder()
                        .conflictType("TEACHER_DOUBLE_BOOKING")
                        .entityType("TEACHER")
                        .entityId(entry.getKey())
                        .entityName(first.getTeacher().getFirstName() + " " + first.getTeacher().getLastName())
                        .severity("CRITICAL")
                        .description("Teacher double-booked at " + timeEntry.getKey())
                        .affectedSlots(timeEntry.getValue().size())
                        .build());
                }
            }
        }

        // Analyze room conflicts
        Map<Long, List<ScheduleSlot>> slotsByRoom = allSlots.stream()
            .filter(s -> s.getRoom() != null)
            .collect(Collectors.groupingBy(s -> s.getRoom().getId()));

        for (Map.Entry<Long, List<ScheduleSlot>> entry : slotsByRoom.entrySet()) {
            Map<String, List<ScheduleSlot>> slotsByTime = entry.getValue().stream()
                .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
                .collect(Collectors.groupingBy(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber()));

            for (Map.Entry<String, List<ScheduleSlot>> timeEntry : slotsByTime.entrySet()) {
                if (timeEntry.getValue().size() > 1) {
                    ScheduleSlot first = timeEntry.getValue().get(0);
                    conflicts.add(AllConflictInfo.builder()
                        .conflictType("ROOM_DOUBLE_BOOKING")
                        .entityType("ROOM")
                        .entityId(entry.getKey())
                        .entityName(first.getRoom().getRoomNumber())
                        .severity("CRITICAL")
                        .description("Room double-booked at " + timeEntry.getKey())
                        .affectedSlots(timeEntry.getValue().size())
                        .build());
                }
            }
        }

        // Filter by severity if specified
        if (severityFilter != null && !severityFilter.isEmpty()) {
            String upperSeverity = severityFilter.toUpperCase();
            conflicts = conflicts.stream()
                .filter(c -> upperSeverity.equals(c.getSeverity()))
                .collect(Collectors.toList());
        }

        return AllConflictsResult.builder()
            .termId(termId)
            .conflicts(conflicts)
            .totalCount(conflicts.size())
            .criticalCount((int) conflicts.stream().filter(c -> "CRITICAL".equals(c.getSeverity())).count())
            .highCount((int) conflicts.stream().filter(c -> "HIGH".equals(c.getSeverity())).count())
            .mediumCount((int) conflicts.stream().filter(c -> "MEDIUM".equals(c.getSeverity())).count())
            .lowCount((int) conflicts.stream().filter(c -> "LOW".equals(c.getSeverity())).count())
            .build();
    }

    /**
     * Get conflict dashboard summary
     */
    public ConflictDashboard getConflictDashboard(Long termId) {
        log.info("Generating conflict dashboard for term {}", termId);

        AllConflictsResult allConflicts = getAllConflicts(termId, null);

        int teacherConflicts = (int) allConflicts.getConflicts().stream()
            .filter(c -> "TEACHER".equals(c.getEntityType()))
            .count();
        int roomConflicts = (int) allConflicts.getConflicts().stream()
            .filter(c -> "ROOM".equals(c.getEntityType()))
            .count();
        int studentConflicts = (int) allConflicts.getConflicts().stream()
            .filter(c -> "STUDENT".equals(c.getEntityType()))
            .count();

        return ConflictDashboard.builder()
            .termId(termId)
            .totalConflicts(allConflicts.getTotalCount())
            .studentConflicts(studentConflicts)
            .teacherConflicts(teacherConflicts)
            .roomConflicts(roomConflicts)
            .criticalConflicts(allConflicts.getCriticalCount())
            .highSeverityConflicts(allConflicts.getHighCount())
            .mediumSeverityConflicts(allConflicts.getMediumCount())
            .lowSeverityConflicts(allConflicts.getLowCount())
            .recentConflicts(allConflicts.getConflicts().stream()
                .limit(10)
                .collect(Collectors.toList()))
            .build();
    }

    /**
     * Get constraint violations
     */
    public ConstraintViolationsResult getConstraintViolations(Long termId, String violationType) {
        log.info("Getting constraint violations for term {} of type {}", termId, violationType);

        List<ScheduleSlot> allSlots = scheduleSlotRepository.findAll();
        List<ConstraintViolation> violations = new ArrayList<>();

        // Check capacity violations
        if (violationType == null || "CAPACITY".equalsIgnoreCase(violationType)) {
            for (ScheduleSlot slot : allSlots) {
                if (slot.getRoom() != null && slot.getRoom().getCapacity() != null) {
                    int studentCount = slot.getStudents() != null ? slot.getStudents().size() : 0;
                    if (studentCount > slot.getRoom().getCapacity()) {
                        violations.add(ConstraintViolation.builder()
                            .violationType("CAPACITY")
                            .severity("MEDIUM")
                            .description("Room capacity exceeded: " + studentCount + "/" +
                                        slot.getRoom().getCapacity())
                            .slotId(slot.getId())
                            .courseCode(slot.getCourse() != null ? slot.getCourse().getCourseCode() : null)
                            .roomNumber(slot.getRoom().getRoomNumber())
                            .constraint("Room capacity must not be exceeded")
                            .currentValue(String.valueOf(studentCount))
                            .allowedValue(String.valueOf(slot.getRoom().getCapacity()))
                            .build());
                    }
                }
            }
        }

        // Check teacher load violations
        if (violationType == null || "TEACHER_LOAD".equalsIgnoreCase(violationType)) {
            Map<Long, List<ScheduleSlot>> slotsByTeacher = allSlots.stream()
                .filter(s -> s.getTeacher() != null)
                .collect(Collectors.groupingBy(s -> s.getTeacher().getId()));

            for (Map.Entry<Long, List<ScheduleSlot>> entry : slotsByTeacher.entrySet()) {
                Teacher teacher = entry.getValue().get(0).getTeacher();
                int maxPeriods = teacher.getMaxPeriodsPerDay() != null ? teacher.getMaxPeriodsPerDay() : 8;

                Map<DayOfWeek, Long> periodsPerDay = entry.getValue().stream()
                    .filter(s -> s.getDayOfWeek() != null)
                    .collect(Collectors.groupingBy(ScheduleSlot::getDayOfWeek, Collectors.counting()));

                for (Map.Entry<DayOfWeek, Long> dayEntry : periodsPerDay.entrySet()) {
                    if (dayEntry.getValue() > maxPeriods) {
                        violations.add(ConstraintViolation.builder()
                            .violationType("TEACHER_LOAD")
                            .severity("HIGH")
                            .description("Teacher " + teacher.getFirstName() + " " + teacher.getLastName() +
                                        " exceeds max periods on " + dayEntry.getKey())
                            .teacherId(entry.getKey())
                            .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                            .constraint("Teacher max periods per day")
                            .currentValue(String.valueOf(dayEntry.getValue()))
                            .allowedValue(String.valueOf(maxPeriods))
                            .build());
                    }
                }
            }
        }

        return ConstraintViolationsResult.builder()
            .termId(termId)
            .violationType(violationType)
            .violations(violations)
            .totalCount(violations.size())
            .build();
    }

    /**
     * Find alternative time slots without conflicts
     */
    public AlternativeSlotsResult findAlternativeSlots(Long courseId, Long teacherId,
                                                       Long roomId, Long termId,
                                                       Integer duration, List<String> preferredDays) {
        log.info("Finding alternative slots for course {} with teacher {} in room {}",
                courseId, teacherId, roomId);

        List<AvailableSlot> alternatives = new ArrayList<>();

        // Get teacher's occupied slots
        Set<String> teacherOccupied = new HashSet<>();
        if (teacherId != null) {
            List<ScheduleSlot> teacherSlots = scheduleSlotRepository.findByTeacherIdWithDetails(teacherId);
            teacherOccupied = teacherSlots.stream()
                .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
                .map(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber())
                .collect(Collectors.toSet());
        }

        // Get room's occupied slots
        Set<String> roomOccupied = new HashSet<>();
        if (roomId != null) {
            List<ScheduleSlot> roomSlots = scheduleSlotRepository.findByRoomIdWithDetails(roomId);
            roomOccupied = roomSlots.stream()
                .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
                .map(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber())
                .collect(Collectors.toSet());
        }

        // Determine which days to check
        List<DayOfWeek> daysToCheck;
        if (preferredDays != null && !preferredDays.isEmpty()) {
            daysToCheck = preferredDays.stream()
                .map(d -> DayOfWeek.valueOf(d.toUpperCase()))
                .collect(Collectors.toList());
        } else {
            daysToCheck = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                 DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        }

        // Find available slots
        for (DayOfWeek day : daysToCheck) {
            for (int period = 1; period <= 8; period++) {
                String key = day + "_" + period;
                if (!teacherOccupied.contains(key) && !roomOccupied.contains(key)) {
                    alternatives.add(AvailableSlot.builder()
                        .dayOfWeek(day.name())
                        .periodNumber(period)
                        .startTime(getDefaultPeriodStartTime(period))
                        .endTime(getDefaultPeriodEndTime(period))
                        .available(true)
                        .teacherAvailable(true)
                        .roomAvailable(true)
                        .build());
                }
            }
        }

        return AlternativeSlotsResult.builder()
            .courseId(courseId)
            .teacherId(teacherId)
            .roomId(roomId)
            .termId(termId)
            .alternativeSlots(alternatives)
            .count(alternatives.size())
            .build();
    }

    /**
     * Get optimization opportunities
     */
    public OptimizationOpportunitiesResult getOptimizationOpportunities(Long termId) {
        log.info("Analyzing optimization opportunities for term {}", termId);

        List<ScheduleSlot> allSlots = scheduleSlotRepository.findAll();
        List<OptimizationOpportunity> opportunities = new ArrayList<>();

        // Check for room utilization optimization
        Map<Long, List<ScheduleSlot>> slotsByRoom = allSlots.stream()
            .filter(s -> s.getRoom() != null)
            .collect(Collectors.groupingBy(s -> s.getRoom().getId()));

        for (Map.Entry<Long, List<ScheduleSlot>> entry : slotsByRoom.entrySet()) {
            Room room = entry.getValue().get(0).getRoom();
            int totalPossibleSlots = 40; // 8 periods x 5 days
            int usedSlots = entry.getValue().size();
            double utilization = (usedSlots * 100.0) / totalPossibleSlots;

            if (utilization < 30) {
                opportunities.add(OptimizationOpportunity.builder()
                    .opportunityType("LOW_ROOM_UTILIZATION")
                    .description("Room " + room.getRoomNumber() + " has low utilization (" +
                                String.format("%.1f", utilization) + "%)")
                    .entityType("ROOM")
                    .entityId(room.getId())
                    .entityName(room.getRoomNumber())
                    .potentialImprovement("Move classes from overcrowded rooms to this room")
                    .estimatedImpact("MEDIUM")
                    .build());
            }
        }

        // Check for teacher load balancing
        Map<Long, List<ScheduleSlot>> slotsByTeacher = allSlots.stream()
            .filter(s -> s.getTeacher() != null)
            .collect(Collectors.groupingBy(s -> s.getTeacher().getId()));

        double avgLoad = slotsByTeacher.values().stream()
            .mapToInt(List::size)
            .average()
            .orElse(0);

        for (Map.Entry<Long, List<ScheduleSlot>> entry : slotsByTeacher.entrySet()) {
            Teacher teacher = entry.getValue().get(0).getTeacher();
            int load = entry.getValue().size();

            if (load > avgLoad * 1.5) {
                opportunities.add(OptimizationOpportunity.builder()
                    .opportunityType("UNBALANCED_TEACHER_LOAD")
                    .description("Teacher " + teacher.getFirstName() + " " + teacher.getLastName() +
                                " has " + load + " periods (avg: " + String.format("%.1f", avgLoad) + ")")
                    .entityType("TEACHER")
                    .entityId(teacher.getId())
                    .entityName(teacher.getFirstName() + " " + teacher.getLastName())
                    .potentialImprovement("Redistribute some classes to other teachers")
                    .estimatedImpact("HIGH")
                    .build());
            }
        }

        return OptimizationOpportunitiesResult.builder()
            .termId(termId)
            .opportunities(opportunities)
            .totalCount(opportunities.size())
            .build();
    }

    /**
     * Get schedule quality metrics
     */
    public ScheduleQualityMetrics getScheduleQualityMetrics(Long termId) {
        log.info("Calculating schedule quality metrics for term {}", termId);

        List<ScheduleSlot> allSlots = scheduleSlotRepository.findAll();
        AllConflictsResult conflicts = getAllConflicts(termId, null);

        // Calculate conflict rate
        double conflictRate = allSlots.isEmpty() ? 0 :
            (conflicts.getTotalCount() * 100.0) / allSlots.size();

        // Calculate room utilization
        Map<Long, List<ScheduleSlot>> slotsByRoom = allSlots.stream()
            .filter(s -> s.getRoom() != null)
            .collect(Collectors.groupingBy(s -> s.getRoom().getId()));

        double avgRoomUtilization = slotsByRoom.values().stream()
            .mapToDouble(slots -> (slots.size() * 100.0) / 40) // 40 = 8 periods x 5 days
            .average()
            .orElse(0);

        // Calculate teacher load balance
        Map<Long, List<ScheduleSlot>> slotsByTeacher = allSlots.stream()
            .filter(s -> s.getTeacher() != null)
            .collect(Collectors.groupingBy(s -> s.getTeacher().getId()));

        double avgTeacherLoad = slotsByTeacher.values().stream()
            .mapToInt(List::size)
            .average()
            .orElse(0);

        double loadVariance = slotsByTeacher.values().stream()
            .mapToDouble(slots -> Math.pow(slots.size() - avgTeacherLoad, 2))
            .average()
            .orElse(0);

        double loadBalance = 100 - Math.min(100, Math.sqrt(loadVariance) * 10);

        // Calculate overall score
        double overallScore = (100 - conflictRate) * 0.4 +
                             avgRoomUtilization * 0.3 +
                             loadBalance * 0.3;

        return ScheduleQualityMetrics.builder()
            .termId(termId)
            .conflictRate(String.format("%.2f%%", conflictRate))
            .roomUtilization(String.format("%.2f%%", avgRoomUtilization))
            .teacherLoadBalance(String.format("%.2f%%", loadBalance))
            .studentSatisfaction("N/A") // Would need survey data
            .overallScore((int) Math.round(overallScore))
            .totalSlots(allSlots.size())
            .totalConflicts(conflicts.getTotalCount())
            .roomCount(slotsByRoom.size())
            .teacherCount(slotsByTeacher.size())
            .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private List<AlternativeSectionInfo> findAlternativeSections(Long courseId, List<ScheduleSlot> studentSlots) {
        List<ScheduleSlot> allCourseSections = scheduleSlotRepository.findByCourseIdWithDetails(courseId);
        Set<String> studentOccupiedTimes = studentSlots.stream()
            .filter(s -> s.getDayOfWeek() != null && s.getPeriodNumber() != null)
            .map(s -> s.getDayOfWeek() + "_" + s.getPeriodNumber())
            .collect(Collectors.toSet());

        return allCourseSections.stream()
            .filter(slot -> {
                if (slot.getDayOfWeek() == null || slot.getPeriodNumber() == null) return false;
                String key = slot.getDayOfWeek() + "_" + slot.getPeriodNumber();
                return !studentOccupiedTimes.contains(key);
            })
            .map(slot -> AlternativeSectionInfo.builder()
                .slotId(slot.getId())
                .dayOfWeek(slot.getDayOfWeek().name())
                .periodNumber(slot.getPeriodNumber())
                .teacherName(slot.getTeacher() != null ?
                    slot.getTeacher().getFirstName() + " " + slot.getTeacher().getLastName() : null)
                .roomNumber(slot.getRoom() != null ? slot.getRoom().getRoomNumber() : null)
                .currentEnrollment(slot.getStudents() != null ? slot.getStudents().size() : 0)
                .build())
            .collect(Collectors.toList());
    }

    private String getDefaultPeriodStartTime(int period) {
        String[] startTimes = {"08:00", "08:55", "09:50", "10:45", "11:40", "12:35", "13:30", "14:25"};
        return period >= 1 && period <= 8 ? startTimes[period - 1] : "08:00";
    }

    private String getDefaultPeriodEndTime(int period) {
        String[] endTimes = {"08:50", "09:45", "10:40", "11:35", "12:30", "13:25", "14:20", "15:15"};
        return period >= 1 && period <= 8 ? endTimes[period - 1] : "08:50";
    }

    // ========================================================================
    // DTOs FOR API RESPONSES
    // ========================================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentConflictsResult {
        private Long studentId;
        private String studentName;
        private Long termId;
        private List<StudentConflictInfo> conflicts;
        private boolean hasConflicts;
        private int conflictCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentConflictInfo {
        private String conflictType;
        private String severity;
        private String description;
        private String dayOfWeek;
        private Integer periodNumber;
        private List<String> conflictingCourses;
        private List<Long> slotIds;
        private List<String> suggestions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseAdditionCheck {
        private Long studentId;
        private Long courseId;
        private Long sectionId;
        private Long termId;
        private boolean canEnroll;
        private boolean hasConflicts;
        private List<ConflictInfo> conflicts;
        private List<AlternativeSectionInfo> alternativeSections;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private String type;
        private String severity;
        private String description;
        private Long conflictingSlotId;
        private String conflictingCourseCode;
        private String dayOfWeek;
        private String time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeSectionInfo {
        private Long slotId;
        private String dayOfWeek;
        private Integer periodNumber;
        private String teacherName;
        private String roomNumber;
        private int currentEnrollment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherConflictsResult {
        private Long teacherId;
        private String teacherName;
        private Long termId;
        private List<TeacherConflictInfo> conflicts;
        private boolean hasConflicts;
        private int conflictCount;
        private int totalPeriodsPerWeek;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherConflictInfo {
        private String conflictType;
        private String severity;
        private String description;
        private String dayOfWeek;
        private Integer periodNumber;
        private List<String> conflictingCourses;
        private List<Long> slotIds;
        private List<String> suggestions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherAvailabilityResult {
        private Long teacherId;
        private String teacherName;
        private Long termId;
        private String dayOfWeek;
        private List<AvailableSlot> availableSlots;
        private int totalAvailable;
        private int occupiedSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomConflictsResult {
        private Long roomId;
        private String roomNumber;
        private Integer capacity;
        private Long termId;
        private List<RoomConflictInfo> conflicts;
        private boolean hasConflicts;
        private int conflictCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomConflictInfo {
        private String conflictType;
        private String severity;
        private String description;
        private String dayOfWeek;
        private Integer periodNumber;
        private List<String> conflictingCourses;
        private List<Long> slotIds;
        private List<String> suggestions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomAvailabilityResult {
        private Long roomId;
        private String roomNumber;
        private Integer capacity;
        private Long termId;
        private String dayOfWeek;
        private List<AvailableSlot> availableSlots;
        private int totalAvailable;
        private int occupiedSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableSlot {
        private String dayOfWeek;
        private Integer periodNumber;
        private String startTime;
        private String endTime;
        private boolean available;
        private boolean teacherAvailable;
        private boolean roomAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllConflictsResult {
        private Long termId;
        private List<AllConflictInfo> conflicts;
        private int totalCount;
        private int criticalCount;
        private int highCount;
        private int mediumCount;
        private int lowCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllConflictInfo {
        private String conflictType;
        private String entityType;
        private Long entityId;
        private String entityName;
        private String severity;
        private String description;
        private int affectedSlots;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDashboard {
        private Long termId;
        private int totalConflicts;
        private int studentConflicts;
        private int teacherConflicts;
        private int roomConflicts;
        private int criticalConflicts;
        private int highSeverityConflicts;
        private int mediumSeverityConflicts;
        private int lowSeverityConflicts;
        private List<AllConflictInfo> recentConflicts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConstraintViolationsResult {
        private Long termId;
        private String violationType;
        private List<ConstraintViolation> violations;
        private int totalCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConstraintViolation {
        private String violationType;
        private String severity;
        private String description;
        private Long slotId;
        private String courseCode;
        private String roomNumber;
        private Long teacherId;
        private String teacherName;
        private String constraint;
        private String currentValue;
        private String allowedValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeSlotsResult {
        private Long courseId;
        private Long teacherId;
        private Long roomId;
        private Long termId;
        private List<AvailableSlot> alternativeSlots;
        private int count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationOpportunitiesResult {
        private Long termId;
        private List<OptimizationOpportunity> opportunities;
        private int totalCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationOpportunity {
        private String opportunityType;
        private String description;
        private String entityType;
        private Long entityId;
        private String entityName;
        private String potentialImprovement;
        private String estimatedImpact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleQualityMetrics {
        private Long termId;
        private String conflictRate;
        private String roomUtilization;
        private String teacherLoadBalance;
        private String studentSatisfaction;
        private int overallScore;
        private int totalSlots;
        private int totalConflicts;
        private int roomCount;
        private int teacherCount;
    }
}
