package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentEnrollment;
import com.heronix.model.domain.Teacher;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentEnrollmentRepository;
import com.heronix.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher Students REST API Controller
 * Location: src/main/java/com/heronix/controller/api/TeacherStudentsApiController.java
 *
 * Provides student roster endpoints for Heronix-Teacher desktop application
 *
 * Endpoints:
 * - GET /api/teacher/students - Get all students for a teacher
 * - GET /api/teacher/students/course/{courseId} - Get students in a specific course
 * - GET /api/teacher/courses/{teacherId} - Get teacher's courses
 *
 * @author Heronix Educational Systems
 * @version 1.0.0
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow Teacher Portal from any location
public class TeacherStudentsApiController {

    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    /**
     * Student DTO for Teacher Portal
     */
    public static class StudentDTO {
        private Long id;
        private String studentId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String gradeLevel;
        private String email;
        private boolean active;
        private String campusName;
        private List<String> courseNames;

        public StudentDTO() {}

        public StudentDTO(Student student) {
            this.id = student.getId();
            this.studentId = student.getStudentId();
            this.firstName = student.getFirstName();
            this.lastName = student.getLastName();
            this.fullName = student.getFirstName() + " " + student.getLastName();
            this.gradeLevel = student.getGradeLevel();
            this.email = student.getEmail();
            this.active = student.isActive();
            this.campusName = student.getCampus() != null ? student.getCampus().getName() : "N/A";
            this.courseNames = new ArrayList<>();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getGradeLevel() { return gradeLevel; }
        public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public String getCampusName() { return campusName; }
        public void setCampusName(String campusName) { this.campusName = campusName; }

        public List<String> getCourseNames() { return courseNames; }
        public void setCourseNames(List<String> courseNames) { this.courseNames = courseNames; }
    }

    /**
     * Course DTO for Teacher Portal
     */
    public static class CourseDTO {
        private Long id;
        private String courseCode;
        private String courseName;
        private String subject;
        private String level;
        private int currentEnrollment;
        private int maxStudents;
        private int periodNumber;
        private String roomNumber;

        public CourseDTO() {}

        public CourseDTO(Course course) {
            this.id = course.getId();
            this.courseCode = course.getCourseCode();
            this.courseName = course.getCourseName();
            this.subject = course.getSubject();
            this.level = course.getLevel() != null ? course.getLevel().toString() : "N/A";
            this.currentEnrollment = course.getCurrentEnrollment() != null ? course.getCurrentEnrollment() : 0;
            this.maxStudents = course.getMaxStudents() != null ? course.getMaxStudents() : 30;
            // Period and room would come from ScheduleSlot - for now use defaults
            this.periodNumber = 0;
            this.roomNumber = "TBD";
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCourseCode() { return courseCode; }
        public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }

        public int getCurrentEnrollment() { return currentEnrollment; }
        public void setCurrentEnrollment(int currentEnrollment) { this.currentEnrollment = currentEnrollment; }

        public int getMaxStudents() { return maxStudents; }
        public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }

        public int getPeriodNumber() { return periodNumber; }
        public void setPeriodNumber(int periodNumber) { this.periodNumber = periodNumber; }

        public String getRoomNumber() { return roomNumber; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    }

    /**
     * Get all students assigned to teacher's courses
     *
     * Query Params:
     * - teacherId: Teacher ID (required)
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "studentId": "S12345",
     *     "firstName": "John",
     *     "lastName": "Smith",
     *     "fullName": "John Smith",
     *     "gradeLevel": "10",
     *     "email": "jsmith@school.edu",
     *     "active": true,
     *     "campusName": "Main Campus",
     *     "courseNames": ["Algebra II", "Chemistry"]
     *   }
     * ]
     *
     * @param teacherId Teacher ID
     * @return List of students
     */
    @GetMapping("/students")
    public ResponseEntity<?> getStudentsByTeacher(@RequestParam Long teacherId) {
        log.info("Fetching students for teacher ID: {}", teacherId);

        try {
            // Find teacher
            Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
            if (teacherOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Teacher not found");
                error.put("teacherId", teacherId.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Teacher teacher = teacherOpt.get();

            // Get teacher's courses
            List<Course> teacherCourses = courseRepository.findByTeacherId(teacherId);

            if (teacherCourses.isEmpty()) {
                log.info("No courses found for teacher ID: {}", teacherId);
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Get all students enrolled in teacher's courses
            Set<Student> uniqueStudents = new HashSet<>();
            Map<Long, List<String>> studentCourseMap = new HashMap<>();

            for (Course course : teacherCourses) {
                // Get enrollments for this course
                List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByCourseId(course.getId());

                for (StudentEnrollment enrollment : enrollments) {
                    Student student = enrollment.getStudent();
                    if (student != null && student.isActive()) {
                        uniqueStudents.add(student);

                        // Track which courses each student is enrolled in
                        studentCourseMap.computeIfAbsent(student.getId(), k -> new ArrayList<>())
                                .add(course.getCourseName());
                    }
                }
            }

            // Convert to DTOs
            List<StudentDTO> studentDTOs = uniqueStudents.stream()
                    .map(student -> {
                        StudentDTO dto = new StudentDTO(student);
                        dto.setCourseNames(studentCourseMap.getOrDefault(student.getId(), new ArrayList<>()));
                        return dto;
                    })
                    .sorted(Comparator.comparing(StudentDTO::getLastName)
                            .thenComparing(StudentDTO::getFirstName))
                    .collect(Collectors.toList());

            log.info("Returning {} unique students for teacher: {} ({})",
                    studentDTOs.size(), teacher.getName(), teacher.getEmployeeId());

            return ResponseEntity.ok(studentDTOs);

        } catch (Exception e) {
            log.error("Error fetching students for teacher ID: {}", teacherId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch students");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get students enrolled in a specific course
     *
     * Path Params:
     * - courseId: Course ID
     *
     * @param courseId Course ID
     * @return List of students in the course
     */
    @GetMapping("/students/course/{courseId}")
    public ResponseEntity<?> getStudentsByCourse(@PathVariable Long courseId) {
        log.info("Fetching students for course ID: {}", courseId);

        try {
            // Verify course exists
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Course not found");
                error.put("courseId", courseId.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Course course = courseOpt.get();

            // Get enrollments for this course
            List<StudentEnrollment> enrollments = studentEnrollmentRepository.findByCourseId(courseId);

            // Convert to DTOs
            List<StudentDTO> studentDTOs = enrollments.stream()
                    .map(StudentEnrollment::getStudent)
                    .filter(student -> student != null && student.isActive())
                    .map(student -> {
                        StudentDTO dto = new StudentDTO(student);
                        dto.setCourseNames(List.of(course.getCourseName()));
                        return dto;
                    })
                    .sorted(Comparator.comparing(StudentDTO::getLastName)
                            .thenComparing(StudentDTO::getFirstName))
                    .collect(Collectors.toList());

            log.info("Returning {} students for course: {} ({})",
                    studentDTOs.size(), course.getCourseCode(), course.getCourseName());

            return ResponseEntity.ok(studentDTOs);

        } catch (Exception e) {
            log.error("Error fetching students for course ID: {}", courseId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch students");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all courses taught by a teacher
     *
     * Path Params:
     * - teacherId: Teacher ID
     *
     * Response:
     * [
     *   {
     *     "id": 1,
     *     "courseCode": "ALG2",
     *     "courseName": "Algebra II",
     *     "subject": "Mathematics",
     *     "level": "GRADE_10",
     *     "currentEnrollment": 28,
     *     "maxStudents": 30,
     *     "periodNumber": 3,
     *     "roomNumber": "201"
     *   }
     * ]
     *
     * @param teacherId Teacher ID
     * @return List of courses
     */
    @GetMapping("/courses/{teacherId}")
    public ResponseEntity<?> getCoursesByTeacher(@PathVariable Long teacherId) {
        log.info("Fetching courses for teacher ID: {}", teacherId);

        try {
            // Verify teacher exists
            Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
            if (teacherOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Teacher not found");
                error.put("teacherId", teacherId.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            Teacher teacher = teacherOpt.get();

            // Get teacher's courses
            List<Course> courses = courseRepository.findByTeacherId(teacherId);

            // Convert to DTOs
            List<CourseDTO> courseDTOs = courses.stream()
                    .map(CourseDTO::new)
                    .sorted(Comparator.comparing(CourseDTO::getCourseCode))
                    .collect(Collectors.toList());

            log.info("Returning {} courses for teacher: {} ({})",
                    courseDTOs.size(), teacher.getName(), teacher.getEmployeeId());

            return ResponseEntity.ok(courseDTOs);

        } catch (Exception e) {
            log.error("Error fetching courses for teacher ID: {}", teacherId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch courses");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/students/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Teacher Students API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}
