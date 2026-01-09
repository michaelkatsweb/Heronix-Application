package com.heronix.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Course API Service
 *
 * Service layer for course management operations via REST API.
 * Provides methods to fetch, create, update, and delete course records.
 *
 * API Endpoints:
 * - GET /courses - List all courses
 * - GET /courses/{id} - Get course details
 * - POST /courses - Create new course
 * - PUT /courses/{id} - Update course
 * - DELETE /courses/{id} - Delete course
 * - GET /courses/{id}/students - Get enrolled students
 * - GET /courses/{id}/sections - Get course sections
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 47 - Additional API Services
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CourseApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    /**
     * Get all courses
     *
     * @return List of courses
     */
    public List<Map<String, Object>> getAllCourses() {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/courses",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> courses = (List<Map<String, Object>>) response.getBody().get("courses");
                return courses != null ? courses : List.of();
            }

            return List.of();

        }, "Get all courses");
    }

    /**
     * Get course by ID
     *
     * @param courseId Course ID
     * @return Course details
     */
    public Map<String, Object> getCourseById(Long courseId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/courses/" + courseId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> course = (Map<String, Object>) response.getBody().get("course");
                return course != null ? course : new HashMap<>();
            }

            return new HashMap<>();

        }, "Get course by ID");
    }

    /**
     * Search courses by query
     *
     * @param query Search query
     * @return Matching courses
     */
    public List<Map<String, Object>> searchCourses(String query) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/courses/search?q=" + query;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> courses = (List<Map<String, Object>>) response.getBody().get("courses");
                return courses != null ? courses : List.of();
            }

            return List.of();

        }, "Search courses");
    }

    /**
     * Create new course
     *
     * @param courseData Course data
     * @return Created course
     */
    public Map<String, Object> createCourse(Map<String, Object> courseData) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                    "/courses",
                    courseData,
                    Map.class
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> course = (Map<String, Object>) response.getBody().get("course");
                return course != null ? course : new HashMap<>();
            }

            return new HashMap<>();

        }, "Create course");
    }

    /**
     * Update course
     *
     * @param courseId Course ID
     * @param courseData Updated course data
     * @return Updated course
     */
    public Map<String, Object> updateCourse(Long courseId, Map<String, Object> courseData) {
        return retryHandler.executeWithRetrySafe(() -> {
            restTemplate.put("/courses/" + courseId, courseData);

            // Fetch updated course
            return getCourseById(courseId);

        }, "Update course");
    }

    /**
     * Delete course
     *
     * @param courseId Course ID
     * @return true if successful
     */
    public boolean deleteCourse(Long courseId) {
        try {
            restTemplate.delete("/courses/" + courseId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting course {}: {}", courseId, e.getMessage());
            return false;
        }
    }

    /**
     * Get enrolled students in course
     *
     * @param courseId Course ID
     * @return List of enrolled students
     */
    public List<Map<String, Object>> getCourseStudents(Long courseId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/courses/" + courseId + "/students",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> students = (List<Map<String, Object>>) response.getBody().get("students");
                return students != null ? students : List.of();
            }

            return List.of();

        }, "Get course students");
    }

    /**
     * Get course sections
     *
     * @param courseId Course ID
     * @return List of course sections
     */
    public List<Map<String, Object>> getCourseSections(Long courseId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/courses/" + courseId + "/sections",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> sections = (List<Map<String, Object>>) response.getBody().get("sections");
                return sections != null ? sections : List.of();
            }

            return List.of();

        }, "Get course sections");
    }

    /**
     * Get courses by department
     *
     * @param department Department name
     * @return List of courses in department
     */
    public List<Map<String, Object>> getCoursesByDepartment(String department) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/courses?department=" + department;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> courses = (List<Map<String, Object>>) response.getBody().get("courses");
                return courses != null ? courses : List.of();
            }

            return List.of();

        }, "Get courses by department");
    }

    /**
     * Enroll student in course
     *
     * @param courseId Course ID
     * @param studentId Student ID
     * @return true if successful
     */
    public boolean enrollStudent(Long courseId, Long studentId) {
        return retryHandler.executeWithRetrySafe(() -> {
            Map<String, Object> enrollmentData = new HashMap<>();
            enrollmentData.put("studentId", studentId);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                    "/courses/" + courseId + "/enroll",
                    enrollmentData,
                    Map.class
                );

            return response.getStatusCode().is2xxSuccessful();

        }, "Enroll student in course");
    }

    /**
     * Unenroll student from course
     *
     * @param courseId Course ID
     * @param studentId Student ID
     * @return true if successful
     */
    public boolean unenrollStudent(Long courseId, Long studentId) {
        try {
            restTemplate.delete("/courses/" + courseId + "/students/" + studentId);
            return true;
        } catch (Exception e) {
            log.error("Error unenrolling student {} from course {}: {}", studentId, courseId, e.getMessage());
            return false;
        }
    }
}
