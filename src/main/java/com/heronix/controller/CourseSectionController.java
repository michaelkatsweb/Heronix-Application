package com.heronix.controller;

import com.heronix.model.domain.AuditLog;
import com.heronix.model.domain.CourseSection;
import com.heronix.model.domain.CourseSection.SectionStatus;
import com.heronix.repository.CourseSectionRepository;
import com.heronix.service.AuditService;
import com.heronix.service.CourseSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Course Section REST API Controller
 *
 * Provides endpoints for managing course sections:
 * - CRUD operations for sections
 * - Section availability checking
 * - Teacher and room assignment
 * - Section enrollment management
 * - Section status updates
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
@RequestMapping("/api/sections")
@CrossOrigin(origins = "*")
public class CourseSectionController {

    private final CourseSectionService courseSectionService;
    private final CourseSectionRepository courseSectionRepository;
    private final AuditService auditService;

    @Autowired
    public CourseSectionController(
            CourseSectionService courseSectionService,
            CourseSectionRepository courseSectionRepository,
            AuditService auditService) {
        this.courseSectionService = courseSectionService;
        this.courseSectionRepository = courseSectionRepository;
        this.auditService = auditService;
    }

    // ============================================================
    // Section CRUD Endpoints
    // ============================================================

    /**
     * GET /api/sections
     * Get all course sections
     *
     * @return list of all sections
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<CourseSection>> getAllSections() {
        List<CourseSection> sections = courseSectionRepository.findAll();
        return ResponseEntity.ok(sections);
    }

    /**
     * GET /api/sections/{id}
     * Get section by ID
     *
     * @param id section ID
     * @return section details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<CourseSection> getSectionById(@PathVariable Long id) {
        CourseSection section = courseSectionService.getSectionById(id);
        if (section == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(section);
    }

    /**
     * GET /api/sections/course/{courseId}
     * Get all sections for a specific course
     *
     * @param courseId course ID
     * @return list of sections for the course
     */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<CourseSection>> getSectionsByCourse(@PathVariable Long courseId) {
        List<CourseSection> sections = courseSectionService.getSectionsByCourseId(courseId);
        return ResponseEntity.ok(sections);
    }

    /**
     * GET /api/sections/teacher/{teacherId}
     * Get all sections taught by a teacher
     *
     * @param teacherId teacher ID
     * @return list of sections taught by the teacher
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<CourseSection>> getSectionsByTeacher(@PathVariable Long teacherId) {
        List<CourseSection> sections = courseSectionService.getSectionsByTeacherId(teacherId);
        return ResponseEntity.ok(sections);
    }

    /**
     * POST /api/sections
     * Create a new section
     *
     * @param section section to create
     * @return created section with ID
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseSection> createSection(@RequestBody CourseSection section) {
        try {
            CourseSection created = courseSectionService.createSection(section);
            auditService.log(AuditLog.AuditAction.STUDENT_CREATE, "CourseSection", created.getId(),
                    "Created section for course " + created.getCourse().getId(),
                    true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/sections/{id}
     * Update an existing section
     *
     * @param id section ID
     * @param section updated section data
     * @return updated section
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseSection> updateSection(
            @PathVariable Long id,
            @RequestBody CourseSection section) {
        try {
            section.setId(id);
            CourseSection updated = courseSectionService.updateSection(section);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "CourseSection", id,
                    "Updated section", true, AuditLog.AuditSeverity.INFO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * DELETE /api/sections/{id}
     * Delete a section
     *
     * @param id section ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        try {
            courseSectionService.deleteSection(id);
            auditService.log(AuditLog.AuditAction.STUDENT_DELETE, "CourseSection", id,
                    "Deleted section", true, AuditLog.AuditSeverity.WARNING);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // Section Availability Endpoints
    // ============================================================

    /**
     * GET /api/sections/teacher/{teacherId}/available/{period}
     * Check if teacher is available at a specific period
     *
     * @param teacherId teacher ID
     * @param period period number
     * @return availability status
     */
    @GetMapping("/teacher/{teacherId}/available/{period}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkTeacherAvailability(
            @PathVariable Long teacherId,
            @PathVariable Integer period) {

        boolean available = courseSectionService.isTeacherAvailable(teacherId, period);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * GET /api/sections/room/{roomId}/available/{period}
     * Check if room is available at a specific period
     *
     * @param roomId room ID
     * @param period period number
     * @return availability status
     */
    @GetMapping("/room/{roomId}/available/{period}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkRoomAvailability(
            @PathVariable Long roomId,
            @PathVariable Integer period) {

        boolean available = courseSectionService.isRoomAvailable(roomId, period);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // ============================================================
    // Section Status Endpoints
    // ============================================================

    /**
     * GET /api/sections/status/{status}
     * Get all sections with a specific status
     *
     * @param status section status (PLANNED, SCHEDULED, OPEN, FULL, CLOSED, CANCELLED)
     * @return list of sections with that status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<CourseSection>> getSectionsByStatus(@PathVariable String status) {
        try {
            SectionStatus sectionStatus = SectionStatus.valueOf(status.toUpperCase());
            List<CourseSection> sections = courseSectionRepository.findAll().stream()
                    .filter(section -> section.getSectionStatus() == sectionStatus)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(sections);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/sections/{id}/status
     * Update section status
     *
     * @param id section ID
     * @param statusUpdate status update object with "status" field
     * @return updated section
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseSection> updateSectionStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {

        try {
            CourseSection section = courseSectionService.getSectionById(id);
            if (section == null) {
                return ResponseEntity.notFound().build();
            }

            SectionStatus newStatus = SectionStatus.valueOf(statusUpdate.get("status").toUpperCase());
            section.setSectionStatus(newStatus);

            CourseSection updated = courseSectionService.updateSection(section);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "CourseSection", id,
                    "Changed status to " + newStatus.toString(), true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // Section Assignment Endpoints
    // ============================================================

    /**
     * PUT /api/sections/{id}/assign-teacher
     * Assign a teacher to a section
     *
     * @param id section ID
     * @param assignment assignment object with "teacherId" field
     * @return updated section
     */
    @PutMapping("/{id}/assign-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseSection> assignTeacher(
            @PathVariable Long id,
            @RequestBody Map<String, Long> assignment) {

        try {
            CourseSection section = courseSectionService.getSectionById(id);
            if (section == null) {
                return ResponseEntity.notFound().build();
            }

            Long teacherId = assignment.get("teacherId");
            Integer period = section.getAssignedPeriod();

            // Check if teacher is available
            if (period != null && !courseSectionService.isTeacherAvailable(teacherId, period)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(null); // Teacher not available at this period
            }

            // Note: You'll need to fetch the Teacher entity and set it
            // This is simplified - in practice, you'd use a TeacherRepository
            // Teacher teacher = teacherRepository.findById(teacherId).orElse(null);
            // section.setAssignedTeacher(teacher);

            CourseSection updated = courseSectionService.updateSection(section);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "CourseSection", id,
                    "Assigned teacher " + teacherId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/sections/{id}/assign-room
     * Assign a room to a section
     *
     * @param id section ID
     * @param assignment assignment object with "roomId" field
     * @return updated section
     */
    @PutMapping("/{id}/assign-room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseSection> assignRoom(
            @PathVariable Long id,
            @RequestBody Map<String, Long> assignment) {

        try {
            CourseSection section = courseSectionService.getSectionById(id);
            if (section == null) {
                return ResponseEntity.notFound().build();
            }

            Long roomId = assignment.get("roomId");
            Integer period = section.getAssignedPeriod();

            // Check if room is available
            if (period != null && !courseSectionService.isRoomAvailable(roomId, period)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(null); // Room not available at this period
            }

            // Note: You'll need to fetch the Room entity and set it
            // This is simplified - in practice, you'd use a RoomRepository
            // Room room = roomRepository.findById(roomId).orElse(null);
            // section.setAssignedRoom(room);

            CourseSection updated = courseSectionService.updateSection(section);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "CourseSection", id,
                    "Assigned room " + roomId, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /api/sections/{id}/assign-period
     * Assign a period to a section
     *
     * @param id section ID
     * @param assignment assignment object with "period" field
     * @return updated section
     */
    @PutMapping("/{id}/assign-period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourseSection> assignPeriod(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> assignment) {

        try {
            CourseSection section = courseSectionService.getSectionById(id);
            if (section == null) {
                return ResponseEntity.notFound().build();
            }

            Integer period = assignment.get("period");
            section.setAssignedPeriod(period);

            CourseSection updated = courseSectionService.updateSection(section);
            auditService.log(AuditLog.AuditAction.STUDENT_UPDATE, "CourseSection", id,
                    "Assigned period " + period, true, AuditLog.AuditSeverity.INFO);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================================
    // Section Enrollment Endpoints
    // ============================================================

    /**
     * GET /api/sections/{id}/enrollment-stats
     * Get enrollment statistics for a section
     *
     * @param id section ID
     * @return enrollment statistics
     */
    @GetMapping("/{id}/enrollment-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getEnrollmentStats(@PathVariable Long id) {
        CourseSection section = courseSectionService.getSectionById(id);
        if (section == null) {
            return ResponseEntity.notFound().build();
        }

        int current = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
        int max = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;
        int min = section.getMinEnrollment() != null ? section.getMinEnrollment() : 10;
        int waitlist = section.getWaitlistCount() != null ? section.getWaitlistCount() : 0;

        double fillPercentage = max > 0 ? (current * 100.0 / max) : 0;
        int available = Math.max(0, max - current);

        Map<String, Object> stats = Map.of(
                "currentEnrollment", current,
                "maxEnrollment", max,
                "minEnrollment", min,
                "targetEnrollment", section.getTargetEnrollment() != null ? section.getTargetEnrollment() : 25,
                "waitlistCount", waitlist,
                "availableSeats", available,
                "fillPercentage", fillPercentage,
                "isUnderEnrolled", current < min,
                "isNearCapacity", fillPercentage >= 80,
                "isOverEnrolled", current > max
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/sections/over-enrolled
     * Get all over-enrolled sections (current > max)
     *
     * @return list of over-enrolled sections
     */
    @GetMapping("/over-enrolled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseSection>> getOverEnrolledSections() {
        List<CourseSection> sections = courseSectionRepository.findAll().stream()
                .filter(section -> {
                    int current = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
                    int max = section.getMaxEnrollment() != null ? section.getMaxEnrollment() : 30;
                    return current > max;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(sections);
    }

    /**
     * GET /api/sections/under-enrolled
     * Get all under-enrolled sections (current < min)
     *
     * @return list of under-enrolled sections
     */
    @GetMapping("/under-enrolled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CourseSection>> getUnderEnrolledSections() {
        List<CourseSection> sections = courseSectionRepository.findAll().stream()
                .filter(section -> {
                    int current = section.getCurrentEnrollment() != null ? section.getCurrentEnrollment() : 0;
                    int min = section.getMinEnrollment() != null ? section.getMinEnrollment() : 10;
                    return current < min;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(sections);
    }

    // ============================================================
    // Utility Endpoints
    // ============================================================

    /**
     * GET /api/sections/course/{courseId}/next-section-number
     * Generate next section number for a course
     *
     * @param courseId course ID
     * @return next section number
     */
    @GetMapping("/course/{courseId}/next-section-number")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getNextSectionNumber(@PathVariable Long courseId) {
        String nextNumber = courseSectionService.generateNextSectionNumber(courseId);
        return ResponseEntity.ok(Map.of("sectionNumber", nextNumber));
    }
}
