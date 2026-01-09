package com.heronix.controller.api;

import com.heronix.model.domain.Course;
import com.heronix.model.domain.CourseEnrollmentRequest;
import com.heronix.model.domain.Student;
import com.heronix.model.enums.EnrollmentRequestStatus;
import com.heronix.repository.CourseRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.EnrollmentRequestManagementService;
import com.heronix.service.EnrollmentRequestManagementService.SearchCriteria;
import com.heronix.service.EnrollmentRequestManagementService.RequestStatistics;
import com.heronix.service.EnrollmentRequestManagementService.CourseRequestCount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Enrollment Request Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/enrollment-requests")
@RequiredArgsConstructor
public class EnrollmentRequestApiController {

    private final EnrollmentRequestManagementService enrollmentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    // ==================== Request Retrieval Operations ====================

    @GetMapping
    public ResponseEntity<List<CourseEnrollmentRequest>> getAllRequests() {
        List<CourseEnrollmentRequest> requests = enrollmentService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseEnrollmentRequest> getRequestById(@PathVariable Long id) {
        return enrollmentService.getRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CourseEnrollmentRequest>> getRequestsByStatus(
            @PathVariable EnrollmentRequestStatus status) {
        List<CourseEnrollmentRequest> requests = enrollmentService.getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseEnrollmentRequest>> getRequestsByStudent(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        List<CourseEnrollmentRequest> requests = enrollmentService.getRequestsByStudent(student);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseEnrollmentRequest>> getRequestsByCourse(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        List<CourseEnrollmentRequest> requests = enrollmentService.getRequestsByCourse(course);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/search")
    public ResponseEntity<List<CourseEnrollmentRequest>> searchRequests(@RequestBody SearchCriteria criteria) {
        List<CourseEnrollmentRequest> requests = enrollmentService.searchRequests(criteria);
        return ResponseEntity.ok(requests);
    }

    // ==================== Request Update Operations ====================

    @PutMapping("/{id}")
    public ResponseEntity<CourseEnrollmentRequest> updateRequest(
            @PathVariable Long id,
            @RequestBody CourseEnrollmentRequest request) {
        request.setId(id);
        CourseEnrollmentRequest updated = enrollmentService.updateRequest(request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/preference-rank")
    public ResponseEntity<Void> updatePreferenceRank(
            @PathVariable Long id,
            @RequestParam int newPreferenceRank) {
        enrollmentService.updatePreferenceRank(id, newPreferenceRank);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/priority-score")
    public ResponseEntity<Void> updatePriorityScore(
            @PathVariable Long id,
            @RequestParam int newPriorityScore,
            @RequestParam(required = false) String justification) {
        enrollmentService.updatePriorityScore(id, newPriorityScore, justification);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long id,
            @RequestParam String reason) {
        enrollmentService.cancelRequest(id, reason);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        enrollmentService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Bulk Operations ====================

    @PostMapping("/bulk/cancel")
    public ResponseEntity<Map<String, Integer>> bulkCancelRequests(
            @RequestBody List<Long> requestIds,
            @RequestParam String reason) {
        int canceledCount = enrollmentService.bulkCancelRequests(requestIds, reason);
        Map<String, Integer> result = new HashMap<>();
        result.put("canceledCount", canceledCount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bulk/delete")
    public ResponseEntity<Map<String, Integer>> bulkDeleteRequests(@RequestBody List<Long> requestIds) {
        int deletedCount = enrollmentService.bulkDeleteRequests(requestIds);
        Map<String, Integer> result = new HashMap<>();
        result.put("deletedCount", deletedCount);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bulk/update-status")
    public ResponseEntity<Map<String, Integer>> bulkUpdateStatus(
            @RequestBody List<Long> requestIds,
            @RequestParam EnrollmentRequestStatus newStatus) {
        int updatedCount = enrollmentService.bulkUpdateStatus(requestIds, newStatus);
        Map<String, Integer> result = new HashMap<>();
        result.put("updatedCount", updatedCount);
        return ResponseEntity.ok(result);
    }

    // ==================== Statistics and Analytics ====================

    @GetMapping("/statistics")
    public ResponseEntity<RequestStatistics> getStatistics() {
        RequestStatistics statistics = enrollmentService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/most-requested-courses")
    public ResponseEntity<List<CourseRequestCount>> getMostRequestedCourses(
            @RequestParam(defaultValue = "10") int limit) {
        List<CourseRequestCount> courses = enrollmentService.getMostRequestedCourses(limit);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/statistics/students-without-requests")
    public ResponseEntity<List<Student>> getStudentsWithoutRequests() {
        List<Student> students = enrollmentService.getStudentsWithoutRequests();
        return ResponseEntity.ok(students);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        RequestStatistics statistics = enrollmentService.getStatistics();
        List<CourseRequestCount> topCourses = enrollmentService.getMostRequestedCourses(5);
        List<Student> studentsWithoutRequests = enrollmentService.getStudentsWithoutRequests();

        List<CourseEnrollmentRequest> pendingRequests = enrollmentService.getRequestsByStatus(
                EnrollmentRequestStatus.PENDING);
        List<CourseEnrollmentRequest> approvedRequests = enrollmentService.getRequestsByStatus(
                EnrollmentRequestStatus.APPROVED);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("statistics", statistics);
        dashboard.put("topRequestedCourses", topCourses);
        dashboard.put("studentsWithoutRequests", studentsWithoutRequests);
        dashboard.put("studentsWithoutRequestsCount", studentsWithoutRequests.size());
        dashboard.put("pendingRequestsCount", pendingRequests.size());
        dashboard.put("approvedRequestsCount", approvedRequests.size());
        dashboard.put("pendingRequests", pendingRequests);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        List<CourseEnrollmentRequest> allRequests = enrollmentService.getRequestsByStudent(student);

        long pendingCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.PENDING)
                .count();
        long approvedCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .count();
        long assignedCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .count();
        long deniedCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.DENIED)
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("studentId", studentId);
        dashboard.put("allRequests", allRequests);
        dashboard.put("totalRequests", allRequests.size());
        dashboard.put("pendingCount", pendingCount);
        dashboard.put("approvedCount", approvedCount);
        dashboard.put("assignedCount", assignedCount);
        dashboard.put("deniedCount", deniedCount);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getCourseDashboard(@PathVariable Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));

        List<CourseEnrollmentRequest> allRequests = enrollmentService.getRequestsByCourse(course);

        long pendingCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.PENDING)
                .count();
        long approvedCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .count();
        long assignedCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == EnrollmentRequestStatus.APPROVED)
                .count();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("courseId", courseId);
        dashboard.put("courseName", course.getCourseName());
        dashboard.put("courseCode", course.getCourseCode());
        dashboard.put("allRequests", allRequests);
        dashboard.put("totalRequests", allRequests.size());
        dashboard.put("pendingCount", pendingCount);
        dashboard.put("approvedCount", approvedCount);
        dashboard.put("assignedCount", assignedCount);

        return ResponseEntity.ok(dashboard);
    }
}
