package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.CourseSection;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentEnrollment;
import com.heronix.model.enums.EnrollmentStatus;
import com.heronix.repository.CourseSectionRepository;
import com.heronix.repository.StudentEnrollmentRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Section Enrollment REST API Controller
 *
 * Provides endpoints for managing student enrollments in course sections:
 * - Enroll students in sections
 * - Drop students from sections
 * - Waitlist management
 * - Enrollment validation
 * - Section roster viewing
 *
 * Security:
 * - All endpoints require authentication
 * - Most endpoints require ADMIN role
 * - Some read endpoints allow TEACHER role
 *
 * @author Heronix Development Team
 * @version 1.0.0
 * @since 2025-12-28
 */
@RestController
@RequestMapping("/api/section-enrollment")
@CrossOrigin(origins = "*")
public class SectionEnrollmentController {

    private final StudentEnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseSectionRepository sectionRepository;
    private final AuditService auditService;

    @Autowired
    public SectionEnrollmentController(
            StudentEnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            CourseSectionRepository sectionRepository,
            AuditService auditService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // Enrollment Management Endpoints
    // ============================================================

    /**
     * POST /api/section-enrollment/enroll
     * Enroll a student in a course section
     *
     * @param enrollmentRequest request with studentId and sectionId
     * @return created enrollment
     */
    @PostMapping("/enroll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> enrollStudent(
            @RequestBody Map<String, Long> enrollmentRequest) {

        try {
            Long studentId = enrollmentRequest.get("studentId");
            Long sectionId = enrollmentRequest.get("sectionId");

            // Validate inputs
            Student student = studentRepository.findById(studentId)
                    .orElse(null);
            if (student == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Student not found"));
            }

            CourseSection section = sectionRepository.findById(sectionId)
                    .orElse(null);
            if (section == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Section not found"));
            }

            // Check if already enrolled
            List<StudentEnrollment> existing = enrollmentRepository.findByStudentId(studentId).stream()
                    .filter(e -> e.getCourse().getId().equals(section.getCourse().getId()))
                    .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                    .collect(Collectors.toList());

            if (!existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Student already enrolled in this course"));
            }

            // Check section capacity
            int currentEnrollment = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
            int maxEnrollment = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;

            EnrollmentStatus status = EnrollmentStatus.ACTIVE;
            if (currentEnrollment >= maxEnrollment) {
                status = EnrollmentStatus.WAITLISTED;
                section.setWaitlistCount((section.getWaitlistCount() != null ? section.getWaitlistCount() : 0) + 1);
            } else {
                section.setCurrentEnrollment(currentEnrollment + 1);
            }

            // Create enrollment
            StudentEnrollment enrollment = new StudentEnrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(section.getCourse());
            enrollment.setStatus(status);
            enrollment.setEnrolledDate(LocalDateTime.now());

            StudentEnrollment saved = enrollmentRepository.save(enrollment);
            sectionRepository.save(section);

            auditService.logStudentEnroll(studentId, section.getCourse().getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "enrollmentId", saved.getId(),
                    "status", status.toString(),
                    "message", status == EnrollmentStatus.WAITLISTED
                            ? "Student added to waitlist (section full)"
                            : "Student enrolled successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to enroll student: " + e.getMessage()));
        }
    }

    /**
     * POST /api/section-enrollment/drop
     * Drop a student from a course section
     *
     * @param dropRequest request with studentId and sectionId
     * @return success response
     */
    @PostMapping("/drop")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> dropStudent(
            @RequestBody Map<String, Long> dropRequest) {

        try {
            Long studentId = dropRequest.get("studentId");
            Long sectionId = dropRequest.get("sectionId");

            CourseSection section = sectionRepository.findById(sectionId)
                    .orElse(null);
            if (section == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Section not found"));
            }

            // Find enrollment
            List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentId(studentId).stream()
                    .filter(e -> e.getCourse().getId().equals(section.getCourse().getId()))
                    .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                    .collect(Collectors.toList());

            if (enrollments.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Enrollment not found"));
            }

            StudentEnrollment enrollment = enrollments.get(0);
            enrollment.setStatus(EnrollmentStatus.DROPPED);
            // Note: dropDate field not present in entity, removed
            enrollmentRepository.save(enrollment);

            // Update section enrollment count
            int currentEnrollment = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
            if (currentEnrollment > 0) {
                section.setCurrentEnrollment(currentEnrollment - 1);
                sectionRepository.save(section);
            }

            auditService.logStudentWithdraw(studentId, section.getCourse().getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Student dropped successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to drop student: " + e.getMessage()));
        }
    }

    /**
     * POST /api/section-enrollment/transfer
     * Transfer a student from one section to another
     *
     * @param transferRequest request with studentId, fromSectionId, toSectionId
     * @return success response
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> transferStudent(
            @RequestBody Map<String, Long> transferRequest) {

        try {
            Long studentId = transferRequest.get("studentId");
            Long fromSectionId = transferRequest.get("fromSectionId");
            Long toSectionId = transferRequest.get("toSectionId");

            // Drop from current section
            ResponseEntity<Map<String, Object>> dropResult = dropStudent(
                    Map.of("studentId", studentId, "sectionId", fromSectionId));

            if (dropResult.getStatusCode() != HttpStatus.OK) {
                return dropResult;
            }

            // Enroll in new section
            ResponseEntity<Map<String, Object>> enrollResult = enrollStudent(
                    Map.of("studentId", studentId, "sectionId", toSectionId));

            if (enrollResult.getStatusCode() != HttpStatus.CREATED) {
                // Rollback: Re-enroll in original section
                enrollStudent(Map.of("studentId", studentId, "sectionId", fromSectionId));
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Failed to transfer student"));
            }

            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "StudentEnrollment", studentId,
                    String.format("Transferred from section %d to section %d", fromSectionId, toSectionId),
                    true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Student transferred successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to transfer student: " + e.getMessage()));
        }
    }

    // ============================================================
    // Roster Viewing Endpoints
    // ============================================================

    /**
     * GET /api/section-enrollment/section/{sectionId}/roster
     * Get roster (list of students) for a section
     *
     * @param sectionId section ID
     * @return list of students enrolled in the section
     */
    @GetMapping("/section/{sectionId}/roster")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Student>> getSectionRoster(@PathVariable Long sectionId) {

        CourseSection section = sectionRepository.findById(sectionId)
                .orElse(null);
        if (section == null) {
            return ResponseEntity.notFound().build();
        }

        List<Student> students = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(section.getCourse().getId()))
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .map(StudentEnrollment::getStudent)
                .collect(Collectors.toList());

        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/section-enrollment/section/{sectionId}/waitlist
     * Get waitlist for a section
     *
     * @param sectionId section ID
     * @return list of students on waitlist for the section
     */
    @GetMapping("/section/{sectionId}/waitlist")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Student>> getSectionWaitlist(@PathVariable Long sectionId) {

        CourseSection section = sectionRepository.findById(sectionId)
                .orElse(null);
        if (section == null) {
            return ResponseEntity.notFound().build();
        }

        List<Student> students = enrollmentRepository.findAll().stream()
                .filter(e -> e.getCourse().getId().equals(section.getCourse().getId()))
                .filter(e -> e.getStatus() == EnrollmentStatus.WAITLISTED)
                .map(StudentEnrollment::getStudent)
                .collect(Collectors.toList());

        return ResponseEntity.ok(students);
    }

    /**
     * GET /api/section-enrollment/student/{studentId}/sections
     * Get all sections a student is enrolled in
     *
     * @param studentId student ID
     * @return list of sections
     */
    @GetMapping("/student/{studentId}/sections")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<Map<String, Object>>> getStudentSections(@PathVariable Long studentId) {

        List<StudentEnrollment> enrollments = enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .collect(Collectors.toList());

        List<Map<String, Object>> sections = enrollments.stream()
                .map(enrollment -> {
                    // Find the section for this course
                    List<CourseSection> courseSections = sectionRepository.findAll().stream()
                            .filter(s -> s.getCourse().getId().equals(enrollment.getCourse().getId()))
                            .collect(Collectors.toList());

                    if (!courseSections.isEmpty()) {
                        CourseSection section = courseSections.get(0);
                        Map<String, Object> sectionData = new java.util.HashMap<>();
                        sectionData.put("enrollmentId", enrollment.getId());
                        sectionData.put("courseId", enrollment.getCourse().getId());
                        sectionData.put("courseName", enrollment.getCourse().getCourseName());
                        sectionData.put("courseCode", enrollment.getCourse().getCourseCode());
                        sectionData.put("sectionId", section.getId());
                        sectionData.put("sectionNumber", section.getSectionNumber());
                        sectionData.put("period", section.getAssignedPeriod() != null ? section.getAssignedPeriod() : "TBA");
                        sectionData.put("teacher", section.getAssignedTeacher() != null
                                ? section.getAssignedTeacher().getFullName()
                                : "TBA");
                        sectionData.put("room", section.getAssignedRoom() != null
                                ? section.getAssignedRoom().getRoomNumber()
                                : "TBA");
                        return sectionData;
                    }
                    return null;
                })
                .filter(obj -> obj != null)
                .collect(Collectors.toList());

        return ResponseEntity.ok(sections);
    }

    // ============================================================
    // Validation Endpoints
    // ============================================================

    /**
     * GET /api/section-enrollment/validate
     * Validate if a student can enroll in a section
     *
     * @param studentId student ID
     * @param sectionId section ID
     * @return validation result with reasons
     */
    @GetMapping("/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> validateEnrollment(
            @RequestParam Long studentId,
            @RequestParam Long sectionId) {

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return ResponseEntity.ok(Map.of(
                    "canEnroll", false,
                    "reason", "Student not found"
            ));
        }

        CourseSection section = sectionRepository.findById(sectionId).orElse(null);
        if (section == null) {
            return ResponseEntity.ok(Map.of(
                    "canEnroll", false,
                    "reason", "Section not found"
            ));
        }

        // Check if already enrolled
        List<StudentEnrollment> existing = enrollmentRepository.findByStudentId(studentId).stream()
                .filter(e -> e.getCourse().getId().equals(section.getCourse().getId()))
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.WAITLISTED)
                .collect(Collectors.toList());

        if (!existing.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "canEnroll", false,
                    "reason", "Already enrolled in this course"
            ));
        }

        // Check section capacity
        int currentEnrollment = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
        int maxEnrollment = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;

        if (currentEnrollment >= maxEnrollment) {
            return ResponseEntity.ok(Map.of(
                    "canEnroll", true,
                    "willBeWaitlisted", true,
                    "reason", "Section full - will be added to waitlist",
                    "availableSeats", 0,
                    "waitlistCount", section.getWaitlistCount() != null ? section.getWaitlistCount() : 0
            ));
        }

        return ResponseEntity.ok(Map.of(
                "canEnroll", true,
                "willBeWaitlisted", false,
                "availableSeats", maxEnrollment - currentEnrollment
        ));
    }

    // ============================================================
    // Bulk Operations Endpoints
    // ============================================================

    /**
     * POST /api/section-enrollment/bulk-enroll
     * Enroll multiple students in a section
     *
     * @param bulkRequest request with sectionId and list of studentIds
     * @return summary of enrollment results
     */
    @PostMapping("/bulk-enroll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkEnrollStudents(
            @RequestBody Map<String, Object> bulkRequest) {

        try {
            Long sectionId = ((Number) bulkRequest.get("sectionId")).longValue();
            @SuppressWarnings("unchecked")
            List<Number> studentIds = (List<Number>) bulkRequest.get("studentIds");

            int successCount = 0;
            int failureCount = 0;
            int waitlistCount = 0;

            for (Number studentIdNum : studentIds) {
                Long studentId = studentIdNum.longValue();
                ResponseEntity<Map<String, Object>> result = enrollStudent(
                        Map.of("studentId", studentId, "sectionId", sectionId));

                if (result.getStatusCode() == HttpStatus.CREATED) {
                    Map<String, Object> body = result.getBody();
                    if (body != null && "WAITLISTED".equals(body.get("status"))) {
                        waitlistCount++;
                    } else {
                        successCount++;
                    }
                } else {
                    failureCount++;
                }
            }

            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "SectionEnrollment", sectionId,
                    String.format("Bulk enrollment: %d total, %d enrolled, %d waitlisted, %d failed",
                            studentIds.size(), successCount, waitlistCount, failureCount),
                    true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(Map.of(
                    "totalStudents", studentIds.size(),
                    "enrolled", successCount,
                    "waitlisted", waitlistCount,
                    "failed", failureCount
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Bulk enrollment failed: " + e.getMessage()));
        }
    }
}
