package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher Attendance API Controller
 *
 * Provides REST endpoints for the Heronix-Teacher app to:
 * - Get class rosters with student lists
 * - Record attendance for class periods
 * - Retrieve existing attendance records
 * - Get bell schedule information
 *
 * Endpoints:
 * - GET  /api/teacher/attendance/roster/{teacherId} - Get all classes/rosters for a teacher
 * - GET  /api/teacher/attendance/roster/{teacherId}/period/{period} - Get roster for specific period
 * - GET  /api/teacher/attendance/class/{sectionId}/date/{date} - Get attendance for a class on a date
 * - POST /api/teacher/attendance/record - Record attendance for a student
 * - POST /api/teacher/attendance/bulk - Record bulk attendance for a class
 * - GET  /api/teacher/attendance/bell-schedule - Get the active bell schedule
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since January 2026 - Attendance System Enhancement
 */
@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherAttendanceApiController {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final AttendanceRepository attendanceRepository;
    private final BellScheduleRepository bellScheduleRepository;
    private final PeriodTimerRepository periodTimerRepository;

    // ========================================================================
    // ROSTER ENDPOINTS
    // ========================================================================

    /**
     * Get all class rosters for a teacher
     * Returns all sections assigned to this teacher with their enrolled students
     */
    @GetMapping("/roster/{teacherId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getTeacherRosters(@PathVariable Long teacherId) {
        log.info("Getting rosters for teacher ID: {}", teacherId);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
            if (teacherOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Teacher not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Teacher teacher = teacherOpt.get();

            // Get all sections assigned to this teacher
            List<CourseSection> sections = courseSectionRepository.findByAssignedTeacherId(teacherId);

            List<Map<String, Object>> rosterList = new ArrayList<>();
            for (CourseSection section : sections) {
                Map<String, Object> rosterData = new HashMap<>();
                rosterData.put("sectionId", section.getId());
                rosterData.put("courseCode", section.getCourse().getCourseCode());
                rosterData.put("courseName", section.getCourse().getCourseName());
                rosterData.put("sectionNumber", section.getSectionNumber());
                rosterData.put("period", section.getAssignedPeriod());
                rosterData.put("roomNumber", section.getAssignedRoom() != null ?
                        section.getAssignedRoom().getRoomNumber() : "TBD");
                rosterData.put("currentEnrollment", section.getCurrentEnrollment());
                rosterData.put("maxEnrollment", section.getMaxEnrollment());

                // Get enrolled students for this section's course
                Course course = section.getCourse();
                List<Student> students = course.getStudents();

                List<Map<String, Object>> studentList = students.stream()
                        .filter(Student::getActive)
                        .map(this::mapStudentToDto)
                        .collect(Collectors.toList());

                rosterData.put("students", studentList);
                rosterData.put("studentCount", studentList.size());

                rosterList.add(rosterData);
            }

            response.put("success", true);
            response.put("teacherId", teacherId);
            response.put("teacherName", teacher.getName());
            response.put("rosters", rosterList);
            response.put("totalSections", rosterList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting teacher rosters: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error getting rosters: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get roster for a specific period
     */
    @GetMapping("/roster/{teacherId}/period/{period}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getRosterForPeriod(
            @PathVariable Long teacherId,
            @PathVariable Integer period) {

        log.info("Getting roster for teacher ID: {}, period: {}", teacherId, period);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
            if (teacherOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Teacher not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Find section for this teacher and period
            List<CourseSection> sections = courseSectionRepository.findByAssignedTeacherIdAndAssignedPeriod(teacherId, period);

            if (sections.isEmpty()) {
                response.put("success", true);
                response.put("message", "No class scheduled for this period");
                response.put("period", period);
                response.put("roster", null);
                return ResponseEntity.ok(response);
            }

            // Usually only one section per period per teacher
            CourseSection section = sections.get(0);

            Map<String, Object> rosterData = new HashMap<>();
            rosterData.put("sectionId", section.getId());
            rosterData.put("courseCode", section.getCourse().getCourseCode());
            rosterData.put("courseName", section.getCourse().getCourseName());
            rosterData.put("sectionNumber", section.getSectionNumber());
            rosterData.put("period", section.getAssignedPeriod());
            rosterData.put("roomNumber", section.getAssignedRoom() != null ?
                    section.getAssignedRoom().getRoomNumber() : "TBD");

            // Get students
            List<Student> students = section.getCourse().getStudents();
            List<Map<String, Object>> studentList = students.stream()
                    .filter(Student::getActive)
                    .map(this::mapStudentToDto)
                    .collect(Collectors.toList());

            rosterData.put("students", studentList);
            rosterData.put("studentCount", studentList.size());

            response.put("success", true);
            response.put("period", period);
            response.put("roster", rosterData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting roster for period: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error getting roster: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // ATTENDANCE RETRIEVAL
    // ========================================================================

    /**
     * Get attendance records for a class section on a specific date
     */
    @GetMapping("/class/{sectionId}/date/{date}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getClassAttendance(
            @PathVariable Long sectionId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Getting attendance for section ID: {}, date: {}", sectionId, date);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<CourseSection> sectionOpt = courseSectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Section not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            CourseSection section = sectionOpt.get();
            Course course = section.getCourse();

            // Get attendance records for this course and date
            List<AttendanceRecord> attendanceRecords = attendanceRepository
                    .findByCourseIdAndAttendanceDate(course.getId(), date);

            // Map student ID to attendance status
            Map<Long, AttendanceRecord> attendanceMap = attendanceRecords.stream()
                    .collect(Collectors.toMap(
                            ar -> ar.getStudent().getId(),
                            ar -> ar,
                            (a, b) -> a // If duplicates, take first
                    ));

            // Get all students in the course
            List<Student> students = course.getStudents();

            List<Map<String, Object>> attendanceList = students.stream()
                    .filter(Student::getActive)
                    .map(student -> {
                        Map<String, Object> record = mapStudentToDto(student);
                        AttendanceRecord ar = attendanceMap.get(student.getId());

                        if (ar != null) {
                            record.put("status", ar.getStatus().name());
                            record.put("arrivalTime", ar.getArrivalTime());
                            record.put("notes", ar.getNotes());
                            record.put("recordId", ar.getId());
                        } else {
                            record.put("status", "NOT_RECORDED");
                            record.put("arrivalTime", null);
                            record.put("notes", null);
                            record.put("recordId", null);
                        }

                        return record;
                    })
                    .collect(Collectors.toList());

            // Calculate summary statistics
            long presentCount = attendanceRecords.stream()
                    .filter(ar -> ar.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                    .count();
            long absentCount = attendanceRecords.stream()
                    .filter(AttendanceRecord::isAbsent)
                    .count();
            long tardyCount = attendanceRecords.stream()
                    .filter(ar -> ar.getStatus() == AttendanceRecord.AttendanceStatus.TARDY)
                    .count();

            response.put("success", true);
            response.put("sectionId", sectionId);
            response.put("courseCode", course.getCourseCode());
            response.put("courseName", course.getCourseName());
            response.put("date", date.toString());
            response.put("period", section.getAssignedPeriod());
            response.put("attendance", attendanceList);
            response.put("summary", Map.of(
                    "totalStudents", attendanceList.size(),
                    "present", presentCount,
                    "absent", absentCount,
                    "tardy", tardyCount,
                    "notRecorded", attendanceList.size() - attendanceRecords.size()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting class attendance: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error getting attendance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // ATTENDANCE RECORDING
    // ========================================================================

    /**
     * Record attendance for a single student
     */
    @PostMapping("/record")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordAttendance(@RequestBody Map<String, Object> attendanceData) {
        log.info("Recording attendance for student: {}", attendanceData.get("studentId"));

        Map<String, Object> response = new HashMap<>();

        try {
            Long studentId = getLong(attendanceData, "studentId");
            Long courseId = getLong(attendanceData, "courseId");
            String dateStr = (String) attendanceData.get("date");
            String statusStr = (String) attendanceData.get("status");
            Integer periodNumber = getInteger(attendanceData, "periodNumber");

            if (studentId == null || courseId == null || dateStr == null || statusStr == null) {
                response.put("success", false);
                response.put("message", "Missing required fields: studentId, courseId, date, status");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate date = LocalDate.parse(dateStr);
            AttendanceRecord.AttendanceStatus status;
            try {
                status = AttendanceRecord.AttendanceStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", "Invalid status: " + statusStr);
                return ResponseEntity.badRequest().body(response);
            }

            // Check if record already exists
            List<AttendanceRecord> existing = attendanceRepository
                    .findByStudentIdAndAttendanceDate(studentId, date);

            Optional<AttendanceRecord> existingRecord = existing.stream()
                    .filter(ar -> ar.getCourse().getId().equals(courseId))
                    .findFirst();

            AttendanceRecord record;
            if (existingRecord.isPresent()) {
                // Update existing record
                record = existingRecord.get();
                record.setStatus(status);
            } else {
                // Create new record
                record = new AttendanceRecord();
                record.setStudent(studentRepository.findById(studentId).orElse(null));
                record.setCourse(courseRepository.findById(courseId).orElse(null));
                record.setAttendanceDate(date);
                record.setStatus(status);
            }

            // Set optional fields
            record.setPeriodNumber(periodNumber);

            if (attendanceData.containsKey("arrivalTime")) {
                record.setArrivalTime(LocalTime.parse((String) attendanceData.get("arrivalTime")));
            }
            if (attendanceData.containsKey("notes")) {
                record.setNotes((String) attendanceData.get("notes"));
            }
            if (attendanceData.containsKey("recordedBy")) {
                record.setRecordedBy((String) attendanceData.get("recordedBy"));
            }

            AttendanceRecord saved = attendanceRepository.save(record);

            response.put("success", true);
            response.put("message", "Attendance recorded successfully");
            response.put("recordId", saved.getId());
            response.put("studentId", studentId);
            response.put("status", saved.getStatus().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording attendance: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error recording attendance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Record bulk attendance for an entire class
     */
    @PostMapping("/bulk")
    @Transactional
    public ResponseEntity<Map<String, Object>> recordBulkAttendance(@RequestBody Map<String, Object> bulkData) {
        log.info("Recording bulk attendance for section: {}", bulkData.get("sectionId"));

        Map<String, Object> response = new HashMap<>();

        try {
            Long sectionId = getLong(bulkData, "sectionId");
            String dateStr = (String) bulkData.get("date");
            Integer periodNumber = getInteger(bulkData, "periodNumber");
            String recordedBy = (String) bulkData.get("recordedBy");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> records = (List<Map<String, Object>>) bulkData.get("records");

            if (sectionId == null || dateStr == null || records == null || records.isEmpty()) {
                response.put("success", false);
                response.put("message", "Missing required fields: sectionId, date, records");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate date = LocalDate.parse(dateStr);

            Optional<CourseSection> sectionOpt = courseSectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Section not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Course course = sectionOpt.get().getCourse();

            int successCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();

            for (Map<String, Object> recordData : records) {
                try {
                    Long studentId = getLong(recordData, "studentId");
                    String statusStr = (String) recordData.get("status");

                    if (studentId == null || statusStr == null) {
                        errorCount++;
                        errors.add("Missing studentId or status");
                        continue;
                    }

                    AttendanceRecord.AttendanceStatus status;
                    try {
                        status = AttendanceRecord.AttendanceStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errorCount++;
                        errors.add("Invalid status for student " + studentId + ": " + statusStr);
                        continue;
                    }

                    // Check if record already exists
                    List<AttendanceRecord> existing = attendanceRepository
                            .findByStudentIdAndAttendanceDate(studentId, date);

                    Optional<AttendanceRecord> existingRecord = existing.stream()
                            .filter(ar -> ar.getCourse().getId().equals(course.getId()))
                            .findFirst();

                    AttendanceRecord record;
                    if (existingRecord.isPresent()) {
                        record = existingRecord.get();
                        record.setStatus(status);
                    } else {
                        record = new AttendanceRecord();
                        record.setStudent(studentRepository.findById(studentId).orElse(null));
                        record.setCourse(course);
                        record.setAttendanceDate(date);
                        record.setStatus(status);
                    }

                    record.setPeriodNumber(periodNumber);
                    record.setRecordedBy(recordedBy);

                    if (recordData.containsKey("arrivalTime") && recordData.get("arrivalTime") != null) {
                        record.setArrivalTime(LocalTime.parse((String) recordData.get("arrivalTime")));
                    }
                    if (recordData.containsKey("notes")) {
                        record.setNotes((String) recordData.get("notes"));
                    }

                    attendanceRepository.save(record);
                    successCount++;

                } catch (Exception e) {
                    errorCount++;
                    errors.add("Error processing record: " + e.getMessage());
                }
            }

            response.put("success", errorCount == 0);
            response.put("message", String.format("Processed %d records: %d success, %d errors",
                    records.size(), successCount, errorCount));
            response.put("successCount", successCount);
            response.put("errorCount", errorCount);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error recording bulk attendance: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error recording bulk attendance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // BELL SCHEDULE
    // ========================================================================

    /**
     * Get the active bell schedule with period times
     */
    @GetMapping("/bell-schedule")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getBellSchedule() {
        log.info("Getting active bell schedule");

        Map<String, Object> response = new HashMap<>();

        try {
            // Find default/active bell schedule
            List<BellSchedule> schedules = bellScheduleRepository.findAll();
            BellSchedule activeSchedule = schedules.stream()
                    .filter(bs -> Boolean.TRUE.equals(bs.getIsDefault()) && Boolean.TRUE.equals(bs.getActive()))
                    .findFirst()
                    .orElse(schedules.isEmpty() ? null : schedules.get(0));

            if (activeSchedule == null) {
                response.put("success", false);
                response.put("message", "No bell schedule configured");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> periods = activeSchedule.getPeriods().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .map(period -> {
                        Map<String, Object> periodData = new HashMap<>();
                        periodData.put("periodNumber", period.getPeriodNumber());
                        periodData.put("periodName", period.getPeriodName());
                        periodData.put("startTime", period.getStartTime().toString());
                        periodData.put("endTime", period.getEndTime().toString());
                        periodData.put("durationMinutes", period.getDurationMinutes());
                        periodData.put("attendanceWindowMinutes", period.getAttendanceWindowMinutes());
                        return periodData;
                    })
                    .sorted(Comparator.comparing(m -> (Integer) m.get("periodNumber")))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("scheduleId", activeSchedule.getId());
            response.put("scheduleName", activeSchedule.getName());
            response.put("scheduleType", activeSchedule.getScheduleType().name());
            response.put("daysOfWeek", activeSchedule.getDaysOfWeek());
            response.put("periods", periods);
            response.put("totalPeriods", periods.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting bell schedule: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error getting bell schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get current period based on current time
     */
    @GetMapping("/current-period")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getCurrentPeriod() {
        Map<String, Object> response = new HashMap<>();

        try {
            LocalTime now = LocalTime.now();
            LocalDate today = LocalDate.now();

            // Find active periods
            List<PeriodTimer> periods = periodTimerRepository.findAll();
            PeriodTimer currentPeriod = periods.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getActive()))
                    .filter(p -> p.appliesTo(today))
                    .filter(p -> p.isWithinPeriod(now))
                    .findFirst()
                    .orElse(null);

            if (currentPeriod != null) {
                response.put("success", true);
                response.put("currentPeriod", currentPeriod.getPeriodNumber());
                response.put("periodName", currentPeriod.getPeriodName());
                response.put("startTime", currentPeriod.getStartTime().toString());
                response.put("endTime", currentPeriod.getEndTime().toString());
                response.put("withinAttendanceWindow", currentPeriod.isWithinAttendanceWindow(now));
            } else {
                response.put("success", true);
                response.put("currentPeriod", null);
                response.put("message", "No class period currently in session");
            }

            response.put("currentTime", now.toString());
            response.put("currentDate", today.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting current period: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Map<String, Object> mapStudentToDto(Student student) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", student.getId());
        dto.put("studentId", student.getStudentId());
        dto.put("firstName", student.getFirstName());
        dto.put("lastName", student.getLastName());
        dto.put("fullName", student.getFirstName() + " " + student.getLastName());
        dto.put("gradeLevel", student.getGradeLevel());
        dto.put("email", student.getEmail());
        dto.put("qrCodeId", student.getQrCodeId());
        dto.put("hasIEP", student.getHasIEP());
        dto.put("has504Plan", student.getHas504Plan());
        return dto;
    }

    private Long getLong(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        return null;
    }

    private Integer getInteger(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        return null;
    }
}
