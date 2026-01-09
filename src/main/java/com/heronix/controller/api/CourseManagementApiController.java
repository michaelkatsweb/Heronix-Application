package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.enums.EducationLevel;
import com.heronix.model.enums.ScheduleType;
import com.heronix.repository.CourseRepository;
import com.heronix.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Course Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseManagementApiController {

    private final CourseService courseService;
    private final CourseRepository courseRepository;

    // ==================== CRUD Operations ====================

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = courseService.findAllWithTeacherCoursesForUI();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Course>> getAllActiveCourses() {
        List<Course> courses = courseService.getAllActiveCourses();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<Course> getCourseByCourseCode(@PathVariable String courseCode) {
        Optional<Course> course = courseRepository.findByCourseCode(courseCode);
        return course.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course saved = courseRepository.save(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course course) {
        course.setId(id);
        Course updated = courseRepository.save(course);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Query Operations ====================

    @GetMapping("/search")
    public ResponseEntity<List<Course>> searchCoursesByName(@RequestParam String name) {
        List<Course> courses = courseRepository.findByCourseNameContaining(name);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/subject/{subject}")
    public ResponseEntity<List<Course>> getCoursesBySubject(@PathVariable String subject) {
        List<Course> courses = courseRepository.findBySubject(subject);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<Course>> getCoursesByLevel(@PathVariable EducationLevel level) {
        List<Course> courses = courseRepository.findByLevel(level);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/schedule-type/{scheduleType}")
    public ResponseEntity<List<Course>> getCoursesByScheduleType(@PathVariable ScheduleType scheduleType) {
        List<Course> courses = courseRepository.findByScheduleType(scheduleType);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Course>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<Course> courses = courseRepository.findByTeacherId(teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/requires-lab")
    public ResponseEntity<List<Course>> getCoursesRequiringLab() {
        List<Course> courses = courseRepository.findByRequiresLabTrue();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/available-seats")
    public ResponseEntity<List<Course>> getCoursesWithAvailableSeats() {
        List<Course> courses = courseRepository.findCoursesWithAvailableSeats();
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/with-students")
    public ResponseEntity<List<Course>> getActiveCoursesWithStudents() {
        List<Course> courses = courseRepository.findActiveCoursesWithStudents();
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/by-codes")
    public ResponseEntity<List<Course>> getCoursesByCourseCodes(@RequestBody List<String> courseCodes) {
        List<Course> courses = courseRepository.findAllByCourseCodeIn(courseCodes);
        return ResponseEntity.ok(courses);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getCourseOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Course> allCourses = courseService.getAllActiveCourses();
        List<Course> withAvailableSeats = courseRepository.findCoursesWithAvailableSeats();
        List<Course> requiresLab = courseRepository.findByRequiresLabTrue();
        List<Course> withStudents = courseRepository.findActiveCoursesWithStudents();

        long fullCourses = allCourses.stream()
                .filter(Course::isFull)
                .count();

        int totalEnrollment = allCourses.stream()
                .filter(c -> c.getCurrentEnrollment() != null)
                .mapToInt(Course::getCurrentEnrollment)
                .sum();

        int totalCapacity = allCourses.stream()
                .filter(c -> c.getMaxStudents() != null)
                .mapToInt(Course::getMaxStudents)
                .sum();

        dashboard.put("totalActiveCourses", allCourses.size());
        dashboard.put("coursesWithAvailableSeats", withAvailableSeats.size());
        dashboard.put("fullCourses", fullCourses);
        dashboard.put("labCourses", requiresLab.size());
        dashboard.put("coursesWithStudents", withStudents.size());
        dashboard.put("totalEnrollment", totalEnrollment);
        dashboard.put("totalCapacity", totalCapacity);
        dashboard.put("overallUtilization",
                totalCapacity > 0 ? (totalEnrollment * 100.0 / totalCapacity) : 0.0);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/course/{id}")
    public ResponseEntity<Map<String, Object>> getCourseDashboard(@PathVariable Long id) {
        Map<String, Object> dashboard = new HashMap<>();

        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        int currentEnrollment = course.getCurrentEnrollment() != null ?
                course.getCurrentEnrollment() : 0;
        int maxStudents = course.getMaxStudents() != null ?
                course.getMaxStudents() : 30;
        double utilization = maxStudents > 0 ?
                (currentEnrollment * 100.0 / maxStudents) : 0.0;

        dashboard.put("courseId", id);
        dashboard.put("courseName", course.getCourseName());
        dashboard.put("courseCode", course.getCourseCode());
        dashboard.put("subject", course.getSubject());
        dashboard.put("level", course.getLevel());
        dashboard.put("scheduleType", course.getScheduleType());
        dashboard.put("currentEnrollment", currentEnrollment);
        dashboard.put("maxStudents", maxStudents);
        dashboard.put("availableSeats", maxStudents - currentEnrollment);
        dashboard.put("utilizationPercentage", utilization);
        dashboard.put("isFull", course.isFull());
        dashboard.put("isActive", course.getActive());
        dashboard.put("requiresLab", course.getRequiresLab());
        dashboard.put("credits", course.getCredits());
        dashboard.put("durationMinutes", course.getDurationMinutes());
        dashboard.put("teacher", course.getTeacher());
        dashboard.put("room", course.getRoom());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/by-subject")
    public ResponseEntity<Map<String, Object>> getCoursesBySubjectDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Course> allCourses = courseService.getAllActiveCourses();

        Map<String, Long> bySubject = allCourses.stream()
                .filter(c -> c.getSubject() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        Course::getSubject,
                        java.util.stream.Collectors.counting()));

        dashboard.put("totalSubjects", bySubject.size());
        dashboard.put("coursesBySubject", bySubject);
        dashboard.put("totalCourses", allCourses.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/by-level")
    public ResponseEntity<Map<String, Object>> getCoursesByLevelDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Course> allCourses = courseService.getAllActiveCourses();

        Map<String, Long> byLevel = allCourses.stream()
                .filter(c -> c.getLevel() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getLevel().name(),
                        java.util.stream.Collectors.counting()));

        dashboard.put("totalLevels", byLevel.size());
        dashboard.put("coursesByLevel", byLevel);
        dashboard.put("totalCourses", allCourses.size());

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/enrollment-status")
    public ResponseEntity<Map<String, Object>> getEnrollmentStatusDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Course> allCourses = courseService.getAllActiveCourses();

        long fullCourses = allCourses.stream()
                .filter(Course::isFull)
                .count();

        long nearlyFull = allCourses.stream()
                .filter(c -> !c.isFull())
                .filter(c -> {
                    int current = c.getCurrentEnrollment() != null ? c.getCurrentEnrollment() : 0;
                    int max = c.getMaxStudents() != null ? c.getMaxStudents() : 30;
                    return max > 0 && (current * 100.0 / max) >= 90.0;
                })
                .count();

        long healthy = allCourses.stream()
                .filter(c -> {
                    int current = c.getCurrentEnrollment() != null ? c.getCurrentEnrollment() : 0;
                    int max = c.getMaxStudents() != null ? c.getMaxStudents() : 30;
                    double util = max > 0 ? (current * 100.0 / max) : 0.0;
                    return util >= 50.0 && util < 90.0;
                })
                .count();

        long underutilized = allCourses.stream()
                .filter(c -> {
                    int current = c.getCurrentEnrollment() != null ? c.getCurrentEnrollment() : 0;
                    int max = c.getMaxStudents() != null ? c.getMaxStudents() : 30;
                    return max > 0 && (current * 100.0 / max) < 50.0;
                })
                .count();

        dashboard.put("totalCourses", allCourses.size());
        dashboard.put("fullCourses", fullCourses);
        dashboard.put("nearlyFullCourses", nearlyFull);
        dashboard.put("healthyCourses", healthy);
        dashboard.put("underutilizedCourses", underutilized);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> getTeacherCoursesDashboard(@PathVariable Long teacherId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Course> teacherCourses = courseRepository.findByTeacherId(teacherId);

        int totalEnrollment = teacherCourses.stream()
                .filter(c -> c.getCurrentEnrollment() != null)
                .mapToInt(Course::getCurrentEnrollment)
                .sum();

        int totalCapacity = teacherCourses.stream()
                .filter(c -> c.getMaxStudents() != null)
                .mapToInt(Course::getMaxStudents)
                .sum();

        dashboard.put("teacherId", teacherId);
        dashboard.put("totalCourses", teacherCourses.size());
        dashboard.put("totalEnrollment", totalEnrollment);
        dashboard.put("totalCapacity", totalCapacity);
        dashboard.put("averageClassSize",
                teacherCourses.isEmpty() ? 0 : totalEnrollment / teacherCourses.size());
        dashboard.put("courses", teacherCourses);

        return ResponseEntity.ok(dashboard);
    }
}
