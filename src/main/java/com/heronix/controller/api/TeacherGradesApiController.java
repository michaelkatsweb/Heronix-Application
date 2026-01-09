package com.heronix.controller.api;

import com.heronix.model.domain.Assignment;
import com.heronix.model.domain.AssignmentGrade;
import com.heronix.model.domain.Course;
import com.heronix.model.domain.Student;
import com.heronix.repository.AssignmentGradeRepository;
import com.heronix.repository.AssignmentRepository;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Teacher Grades REST API Controller
 * Location: src/main/java/com/heronix/controller/api/TeacherGradesApiController.java
 *
 * Provides grading endpoints for Heronix-Teacher desktop application
 *
 * Endpoints:
 * - GET /api/grades/course/{courseId} - Get gradebook for a course
 * - POST /api/grades/entry - Enter/update a grade
 * - POST /api/grades/assignment - Create new assignment
 * - GET /api/grades/student/{studentId}/course/{courseId} - Get student grades in course
 * - PUT /api/grades/{gradeId} - Update an existing grade
 *
 * @author Heronix Educational Systems
 * @version 1.0.0
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow Teacher Portal from any location
public class TeacherGradesApiController {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentGradeRepository assignmentGradeRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    /**
     * Assignment DTO
     */
    public static class AssignmentDTO {
        private Long id;
        private String title;
        private String description;
        private String assignmentType;
        private Double maxPoints;
        private LocalDate dueDate;
        private LocalDate assignedDate;
        private boolean published;
        private String categoryName;
        private int totalStudents;
        private int gradedStudents;
        private Double classAverage;

        public AssignmentDTO() {}

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getAssignmentType() { return assignmentType; }
        public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

        public Double getMaxPoints() { return maxPoints; }
        public void setMaxPoints(Double maxPoints) { this.maxPoints = maxPoints; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public LocalDate getAssignedDate() { return assignedDate; }
        public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }

        public boolean isPublished() { return published; }
        public void setPublished(boolean published) { this.published = published; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public int getTotalStudents() { return totalStudents; }
        public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }

        public int getGradedStudents() { return gradedStudents; }
        public void setGradedStudents(int gradedStudents) { this.gradedStudents = gradedStudents; }

        public Double getClassAverage() { return classAverage; }
        public void setClassAverage(Double classAverage) { this.classAverage = classAverage; }
    }

    /**
     * Grade DTO
     */
    public static class GradeDTO {
        private Long id;
        private Long studentId;
        private String studentName;
        private Long assignmentId;
        private String assignmentTitle;
        private Double score;
        private Double maxPoints;
        private String letterGrade;
        private Double percentage;
        private String status; // SUBMITTED, GRADED, MISSING, LATE, EXCUSED
        private String feedback;
        private LocalDateTime submittedAt;
        private LocalDateTime gradedAt;
        private boolean excused;
        private boolean late;

        public GradeDTO() {}

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }

        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

        public String getAssignmentTitle() { return assignmentTitle; }
        public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public Double getMaxPoints() { return maxPoints; }
        public void setMaxPoints(Double maxPoints) { this.maxPoints = maxPoints; }

        public String getLetterGrade() { return letterGrade; }
        public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

        public Double getPercentage() { return percentage; }
        public void setPercentage(Double percentage) { this.percentage = percentage; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }

        public LocalDateTime getSubmittedAt() { return submittedAt; }
        public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

        public LocalDateTime getGradedAt() { return gradedAt; }
        public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

        public boolean isExcused() { return excused; }
        public void setExcused(boolean excused) { this.excused = excused; }

        public boolean isLate() { return late; }
        public void setLate(boolean late) { this.late = late; }
    }

    /**
     * Grade Entry Request
     */
    public static class GradeEntryRequest {
        private Long studentId;
        private Long assignmentId;
        private Double score;
        private String feedback;
        private String status;
        private boolean excused;

        public GradeEntryRequest() {}

        public Long getStudentId() { return studentId; }
        public void setStudentId(Long studentId) { this.studentId = studentId; }

        public Long getAssignmentId() { return assignmentId; }
        public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }

        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isExcused() { return excused; }
        public void setExcused(boolean excused) { this.excused = excused; }
    }

    /**
     * Get gradebook for a course
     *
     * Path Params:
     * - courseId: Course ID
     *
     * Response: {
     *   "courseId": 1,
     *   "courseName": "Algebra II",
     *   "assignments": [...],
     *   "grades": [...]
     * }
     *
     * @param courseId Course ID
     * @return Gradebook data
     */
    @GetMapping("/course/{courseId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getGradebook(@PathVariable Long courseId) {
        log.info("Fetching gradebook for course ID: {}", courseId);

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

            // Get assignments for this course
            List<Assignment> assignments = assignmentRepository.findByCourseIdOrderByDueDateDesc(courseId);

            // Convert assignments to DTOs
            List<AssignmentDTO> assignmentDTOs = assignments.stream()
                    .map(assignment -> {
                        AssignmentDTO dto = new AssignmentDTO();
                        dto.setId(assignment.getId());
                        dto.setTitle(assignment.getTitle());
                        dto.setDescription(assignment.getDescription());
                        dto.setAssignmentType(assignment.getCategory() != null ? assignment.getCategory().getName() : "Assignment");
                        dto.setMaxPoints(assignment.getMaxPoints());
                        dto.setDueDate(assignment.getDueDate());
                        dto.setAssignedDate(assignment.getAssignedDate());
                        dto.setPublished(assignment.getPublished() != null ? assignment.getPublished() : false);
                        dto.setCategoryName(assignment.getCategory() != null ?
                                assignment.getCategory().getName() : "Uncategorized");

                        // Get grade statistics
                        long gradedCount = assignmentGradeRepository.countGradedByAssignment(assignment.getId());
                        Double classAvg = assignmentGradeRepository.getClassAverage(assignment.getId());

                        dto.setTotalStudents(course.getCurrentEnrollment() != null ? course.getCurrentEnrollment() : 0);
                        dto.setGradedStudents((int) gradedCount);
                        dto.setClassAverage(classAvg);

                        return dto;
                    })
                    .collect(Collectors.toList());

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("courseId", courseId);
            response.put("courseName", course.getCourseName());
            response.put("courseCode", course.getCourseCode());
            response.put("assignments", assignmentDTOs);

            log.info("Returning gradebook with {} assignments for course: {}",
                    assignmentDTOs.size(), course.getCourseCode());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching gradebook for course ID: {}", courseId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch gradebook");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Enter or update a grade
     *
     * Request Body: {
     *   "studentId": 123,
     *   "assignmentId": 456,
     *   "score": 95.5,
     *   "feedback": "Great work!",
     *   "status": "GRADED",
     *   "excused": false
     * }
     *
     * @param request Grade entry request
     * @return Created/updated grade
     */
    @PostMapping("/entry")
    @Transactional
    public ResponseEntity<?> enterGrade(@RequestBody GradeEntryRequest request) {
        log.info("Entering grade: student={}, assignment={}, score={}",
                request.getStudentId(), request.getAssignmentId(), request.getScore());

        try {
            // Validate inputs
            if (request.getStudentId() == null || request.getAssignmentId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Student ID and Assignment ID are required");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if grade already exists
            Optional<AssignmentGrade> existingGradeOpt = assignmentGradeRepository
                    .findByStudentIdAndAssignmentId(request.getStudentId(), request.getAssignmentId());

            AssignmentGrade grade;
            boolean isNew = false;

            if (existingGradeOpt.isPresent()) {
                // Update existing grade
                grade = existingGradeOpt.get();
                log.info("Updating existing grade ID: {}", grade.getId());
            } else {
                // Create new grade
                grade = new AssignmentGrade();
                isNew = true;

                // Load student and assignment
                Optional<Student> studentOpt = studentRepository.findById(request.getStudentId());
                Optional<Assignment> assignmentOpt = assignmentRepository.findById(request.getAssignmentId());

                if (studentOpt.isEmpty() || assignmentOpt.isEmpty()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Student or Assignment not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
                }

                grade.setStudent(studentOpt.get());
                grade.setAssignment(assignmentOpt.get());
                log.info("Creating new grade entry");
            }

            // Update grade fields
            grade.setScore(request.getScore());
            grade.setComments(request.getFeedback());
            if (request.getStatus() != null) {
                try {
                    grade.setStatus(AssignmentGrade.GradeStatus.valueOf(request.getStatus()));
                } catch (IllegalArgumentException e) {
                    grade.setStatus(AssignmentGrade.GradeStatus.GRADED);
                }
            } else {
                grade.setStatus(AssignmentGrade.GradeStatus.GRADED);
            }
            grade.setExcused(request.isExcused());
            grade.setGradedDate(LocalDate.now());
            grade.setUpdatedAt(LocalDateTime.now());

            // Save grade
            AssignmentGrade savedGrade = assignmentGradeRepository.save(grade);

            // Convert to DTO
            GradeDTO gradeDTO = convertToGradeDTO(savedGrade);

            log.info("Grade {} successfully: ID={}", isNew ? "created" : "updated", savedGrade.getId());

            return ResponseEntity.ok(gradeDTO);

        } catch (Exception e) {
            log.error("Error entering grade", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to enter grade");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get student's grades in a course
     *
     * Path Params:
     * - studentId: Student ID
     * - courseId: Course ID
     *
     * @param studentId Student ID
     * @param courseId Course ID
     * @return List of grades
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getStudentGrades(@PathVariable Long studentId, @PathVariable Long courseId) {
        log.info("Fetching grades for student {} in course {}", studentId, courseId);

        try {
            List<AssignmentGrade> grades = assignmentGradeRepository
                    .findByStudentIdAndCourseId(studentId, courseId);

            List<GradeDTO> gradeDTOs = grades.stream()
                    .map(this::convertToGradeDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(gradeDTOs);

        } catch (Exception e) {
            log.error("Error fetching student grades", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch grades");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update an existing grade
     *
     * @param gradeId Grade ID
     * @param request Grade update request
     * @return Updated grade
     */
    @PutMapping("/{gradeId}")
    @Transactional
    public ResponseEntity<?> updateGrade(@PathVariable Long gradeId, @RequestBody GradeEntryRequest request) {
        log.info("Updating grade ID: {}", gradeId);

        try {
            Optional<AssignmentGrade> gradeOpt = assignmentGradeRepository.findById(gradeId);

            if (gradeOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Grade not found");
                error.put("gradeId", gradeId.toString());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            AssignmentGrade grade = gradeOpt.get();

            // Update fields
            if (request.getScore() != null) {
                grade.setScore(request.getScore());
            }

            if (request.getFeedback() != null) {
                grade.setComments(request.getFeedback());
            }

            if (request.getStatus() != null) {
                try {
                    grade.setStatus(AssignmentGrade.GradeStatus.valueOf(request.getStatus()));
                } catch (IllegalArgumentException e) {
                    // Keep existing status if invalid
                }
            }

            grade.setExcused(request.isExcused());
            grade.setGradedDate(LocalDate.now());
            grade.setUpdatedAt(LocalDateTime.now());

            // Save
            AssignmentGrade savedGrade = assignmentGradeRepository.save(grade);
            GradeDTO gradeDTO = convertToGradeDTO(savedGrade);

            log.info("Grade updated successfully: ID={}", savedGrade.getId());

            return ResponseEntity.ok(gradeDTO);

        } catch (Exception e) {
            log.error("Error updating grade", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update grade");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Teacher Grades API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Convert AssignmentGrade entity to DTO
     */
    private GradeDTO convertToGradeDTO(AssignmentGrade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudent().getId());
        dto.setStudentName(grade.getStudent().getFirstName() + " " + grade.getStudent().getLastName());
        dto.setAssignmentId(grade.getAssignment().getId());
        dto.setAssignmentTitle(grade.getAssignment().getTitle());
        dto.setScore(grade.getScore());
        dto.setMaxPoints(grade.getAssignment().getMaxPoints());
        dto.setLetterGrade(grade.getLetterGrade()); // Helper method
        dto.setPercentage(grade.getPercentage()); // Helper method
        dto.setStatus(grade.getStatus() != null ? grade.getStatus().name() : null);
        dto.setFeedback(grade.getComments()); // Field is 'comments'
        dto.setSubmittedAt(grade.getSubmittedDate() != null ? grade.getSubmittedDate().atStartOfDay() : null); // Field is 'submittedDate'
        dto.setGradedAt(grade.getGradedDate() != null ? grade.getGradedDate().atStartOfDay() : null); // Field is 'gradedDate'
        dto.setExcused(grade.getExcused() != null ? grade.getExcused() : false); // Field is 'excused'
        dto.setLate(grade.getStatus() == AssignmentGrade.GradeStatus.LATE);
        return dto;
    }

    /**
     * Calculate letter grade from percentage
     */
    private String calculateLetterGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
}
