package com.heronix.controller.api;

import com.heronix.model.domain.*;
import com.heronix.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Teacher Sync API Controller
 *
 * Receives synced data from Teacher Portal offline-first applications
 *
 * Endpoints:
 * - POST /api/teacher-sync/students - Sync student data
 * - POST /api/teacher-sync/categories - Sync assignment categories
 * - POST /api/teacher-sync/assignments - Sync assignments
 * - POST /api/teacher-sync/grades - Sync grades
 * - POST /api/teacher-sync/attendance - Sync attendance records
 * - POST /api/teacher-sync/hallpasses - Sync hall passes
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/api/teacher-sync")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TeacherSyncApiController {

    private final StudentRepository studentRepository;
    private final GradingCategoryRepository categoryRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentGradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    /**
     * Sync student data from Teacher Portal
     *
     * Teacher Portal cannot create students, but may update local cache
     * This endpoint validates and ignores student creation attempts
     */
    @PostMapping("/students")
    @Transactional
    public ResponseEntity<Map<String, Object>> syncStudent(@RequestBody Map<String, Object> studentData) {
        log.info("Received student sync request");

        Map<String, Object> response = new HashMap<>();

        try {
            // Students are managed by SIS admin, not teachers
            // Teacher Portal should only read student data, not create it
            response.put("success", true);
            response.put("message", "Student data sync ignored - students are read-only in Teacher Portal");
            response.put("action", "ignored");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing student data", e);
            response.put("success", false);
            response.put("message", "Error syncing student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sync assignment category (grading category)
     */
    @PostMapping("/categories")
    @Transactional
    public ResponseEntity<Map<String, Object>> syncCategory(@RequestBody Map<String, Object> categoryData) {
        log.info("Received category sync request: {}", categoryData.get("name"));

        Map<String, Object> response = new HashMap<>();

        try {
            Long courseId = getLong(categoryData, "courseId");
            String name = (String) categoryData.get("name");

            if (courseId == null || name == null) {
                response.put("success", false);
                response.put("message", "Missing required fields: courseId, name");
                return ResponseEntity.badRequest().body(response);
            }

            // Create new category
            GradingCategory category = new GradingCategory();
            category.setCourse(courseRepository.findById(courseId).orElse(null));
            category.setName(name);

            // Update fields
            if (categoryData.containsKey("weight")) {
                category.setWeight(getDouble(categoryData, "weight"));
            }
            if (categoryData.containsKey("displayOrder")) {
                category.setDisplayOrder(getInteger(categoryData, "displayOrder"));
            }

            GradingCategory saved = categoryRepository.save(category);

            response.put("success", true);
            response.put("message", "Category synced successfully");
            response.put("categoryId", saved.getId());

            log.info("Category synced successfully: {}", saved.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing category", e);
            response.put("success", false);
            response.put("message", "Error syncing category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sync assignment from Teacher Portal
     */
    @PostMapping("/assignments")
    @Transactional
    public ResponseEntity<Map<String, Object>> syncAssignment(@RequestBody Map<String, Object> assignmentData) {
        log.info("Received assignment sync request: {}", assignmentData.get("name"));

        Map<String, Object> response = new HashMap<>();

        try {
            Long courseId = getLong(assignmentData, "courseId");
            String title = (String) assignmentData.get("name");

            if (courseId == null || title == null) {
                response.put("success", false);
                response.put("message", "Missing required fields: courseId, name");
                return ResponseEntity.badRequest().body(response);
            }

            // Create new assignment
            Assignment assignment = new Assignment();
            assignment.setCourse(courseRepository.findById(courseId).orElse(null));
            assignment.setTitle(title);

            // Set optional fields
            if (assignmentData.containsKey("description")) {
                assignment.setDescription((String) assignmentData.get("description"));
            }
            if (assignmentData.containsKey("maxPoints")) {
                assignment.setMaxPoints(getDouble(assignmentData, "maxPoints"));
            }
            if (assignmentData.containsKey("dueDate")) {
                assignment.setDueDate(java.time.LocalDate.parse((String) assignmentData.get("dueDate")));
            }
            if (assignmentData.containsKey("assignedDate")) {
                assignment.setAssignedDate(java.time.LocalDate.parse((String) assignmentData.get("assignedDate")));
            }
            if (assignmentData.containsKey("published")) {
                assignment.setPublished((Boolean) assignmentData.get("published"));
            }
            if (assignmentData.containsKey("categoryId")) {
                Long categoryId = getLong(assignmentData, "categoryId");
                assignment.setCategory(categoryRepository.findById(categoryId).orElse(null));
            }

            Assignment saved = assignmentRepository.save(assignment);

            response.put("success", true);
            response.put("message", "Assignment synced successfully");
            response.put("assignmentId", saved.getId());

            log.info("Assignment synced successfully: {}", saved.getTitle());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing assignment", e);
            response.put("success", false);
            response.put("message", "Error syncing assignment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sync grade from Teacher Portal
     */
    @PostMapping("/grades")
    @Transactional
    public ResponseEntity<Map<String, Object>> syncGrade(@RequestBody Map<String, Object> gradeData) {
        log.info("Received grade sync request for student={}, assignment={}",
                gradeData.get("studentId"), gradeData.get("assignmentId"));

        Map<String, Object> response = new HashMap<>();

        try {
            Long studentId = getLong(gradeData, "studentId");
            Long assignmentId = getLong(gradeData, "assignmentId");

            if (studentId == null || assignmentId == null) {
                response.put("success", false);
                response.put("message", "Missing required fields: studentId, assignmentId");
                return ResponseEntity.badRequest().body(response);
            }

            // Find or create grade
            AssignmentGrade grade = gradeRepository.findByStudentIdAndAssignmentId(studentId, assignmentId)
                    .orElseGet(() -> {
                        AssignmentGrade newGrade = new AssignmentGrade();
                        newGrade.setStudent(studentRepository.findById(studentId).orElse(null));
                        newGrade.setAssignment(assignmentRepository.findById(assignmentId).orElse(null));
                        return newGrade;
                    });

            // Update grade fields
            if (gradeData.containsKey("score")) {
                grade.setScore(getDouble(gradeData, "score"));
            }
            if (gradeData.containsKey("comments")) {
                grade.setComments((String) gradeData.get("comments"));
            }
            if (gradeData.containsKey("status")) {
                try {
                    grade.setStatus(AssignmentGrade.GradeStatus.valueOf((String) gradeData.get("status")));
                } catch (IllegalArgumentException e) {
                    grade.setStatus(AssignmentGrade.GradeStatus.GRADED);
                }
            }
            if (gradeData.containsKey("excused")) {
                grade.setExcused((Boolean) gradeData.get("excused"));
            }
            if (gradeData.containsKey("gradedDate")) {
                grade.setGradedDate(java.time.LocalDate.parse((String) gradeData.get("gradedDate")));
            }
            if (gradeData.containsKey("submittedDate")) {
                grade.setSubmittedDate(java.time.LocalDate.parse((String) gradeData.get("submittedDate")));
            }

            grade.setUpdatedAt(java.time.LocalDateTime.now());

            AssignmentGrade saved = gradeRepository.save(grade);

            response.put("success", true);
            response.put("message", "Grade synced successfully");
            response.put("gradeId", saved.getId());

            log.info("Grade synced successfully: ID={}", saved.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing grade", e);
            response.put("success", false);
            response.put("message", "Error syncing grade: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sync attendance from Teacher Portal
     */
    @PostMapping("/attendance")
    @Transactional
    public ResponseEntity<Map<String, Object>> syncAttendance(@RequestBody Map<String, Object> attendanceData) {
        log.info("Received attendance sync request for student={}, date={}",
                attendanceData.get("studentId"), attendanceData.get("date"));

        Map<String, Object> response = new HashMap<>();

        try {
            Long studentId = getLong(attendanceData, "studentId");
            String dateStr = (String) attendanceData.get("date");

            if (studentId == null || dateStr == null) {
                response.put("success", false);
                response.put("message", "Missing required fields: studentId, date");
                return ResponseEntity.badRequest().body(response);
            }

            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);

            // Create new attendance record
            AttendanceRecord attendance = new AttendanceRecord();
            attendance.setStudent(studentRepository.findById(studentId).orElse(null));
            attendance.setAttendanceDate(date);

            if (attendanceData.containsKey("status")) {
                try {
                    attendance.setStatus(AttendanceRecord.AttendanceStatus.valueOf((String) attendanceData.get("status")));
                } catch (IllegalArgumentException e) {
                    attendance.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
                }
            }
            if (attendanceData.containsKey("notes")) {
                attendance.setNotes((String) attendanceData.get("notes"));
            }
            if (attendanceData.containsKey("courseId")) {
                Long courseId = getLong(attendanceData, "courseId");
                attendance.setCourse(courseRepository.findById(courseId).orElse(null));
            }

            AttendanceRecord saved = attendanceRepository.save(attendance);

            response.put("success", true);
            response.put("message", "Attendance synced successfully");
            response.put("attendanceId", saved.getId());

            log.info("Attendance synced successfully: ID={}", saved.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing attendance", e);
            response.put("success", false);
            response.put("message", "Error syncing attendance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Sync hall pass from Teacher Portal
     */
    @PostMapping("/hallpasses")
    @Transactional
    public ResponseEntity<Map<String, Object>> syncHallPass(@RequestBody Map<String, Object> hallPassData) {
        log.info("Received hall pass sync request");

        Map<String, Object> response = new HashMap<>();

        try {
            // Hall passes are typically real-time, not synced
            // This endpoint exists for offline mode support
            response.put("success", true);
            response.put("message", "Hall pass sync not implemented - use real-time API");
            response.put("action", "ignored");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error syncing hall pass", e);
            response.put("success", false);
            response.put("message", "Error syncing hall pass: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check for sync endpoints
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Teacher Sync API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

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

    private Double getDouble(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return null;
    }
}
