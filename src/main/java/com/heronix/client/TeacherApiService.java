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
 * Teacher API Service
 *
 * Service layer for teacher management operations via REST API.
 * Provides methods to fetch, create, update, and delete teacher records.
 *
 * API Endpoints:
 * - GET /teachers - List all teachers
 * - GET /teachers/{id} - Get teacher details
 * - POST /teachers - Create new teacher
 * - PUT /teachers/{id} - Update teacher
 * - DELETE /teachers/{id} - Delete teacher
 * - GET /teachers/{id}/schedule - Get teacher schedule
 * - GET /teachers/{id}/courses - Get teacher courses
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 47 - Additional API Services
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    /**
     * Get all teachers
     *
     * @return List of teachers
     */
    public List<Map<String, Object>> getAllTeachers() {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/teachers",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> teachers = (List<Map<String, Object>>) response.getBody().get("teachers");
                return teachers != null ? teachers : List.of();
            }

            return List.of();

        }, "Get all teachers");
    }

    /**
     * Get teacher by ID
     *
     * @param teacherId Teacher ID
     * @return Teacher details
     */
    public Map<String, Object> getTeacherById(Long teacherId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/teachers/" + teacherId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> teacher = (Map<String, Object>) response.getBody().get("teacher");
                return teacher != null ? teacher : new HashMap<>();
            }

            return new HashMap<>();

        }, "Get teacher by ID");
    }

    /**
     * Search teachers by query
     *
     * @param query Search query
     * @return Matching teachers
     */
    public List<Map<String, Object>> searchTeachers(String query) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/teachers/search?q=" + query;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> teachers = (List<Map<String, Object>>) response.getBody().get("teachers");
                return teachers != null ? teachers : List.of();
            }

            return List.of();

        }, "Search teachers");
    }

    /**
     * Create new teacher
     *
     * @param teacherData Teacher data
     * @return Created teacher
     */
    public Map<String, Object> createTeacher(Map<String, Object> teacherData) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                    "/teachers",
                    teacherData,
                    Map.class
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> teacher = (Map<String, Object>) response.getBody().get("teacher");
                return teacher != null ? teacher : new HashMap<>();
            }

            return new HashMap<>();

        }, "Create teacher");
    }

    /**
     * Update teacher
     *
     * @param teacherId Teacher ID
     * @param teacherData Updated teacher data
     * @return Updated teacher
     */
    public Map<String, Object> updateTeacher(Long teacherId, Map<String, Object> teacherData) {
        return retryHandler.executeWithRetrySafe(() -> {
            restTemplate.put("/teachers/" + teacherId, teacherData);

            // Fetch updated teacher
            return getTeacherById(teacherId);

        }, "Update teacher");
    }

    /**
     * Delete teacher
     *
     * @param teacherId Teacher ID
     * @return true if successful
     */
    public boolean deleteTeacher(Long teacherId) {
        try {
            restTemplate.delete("/teachers/" + teacherId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting teacher {}: {}", teacherId, e.getMessage());
            return false;
        }
    }

    /**
     * Get teacher schedule
     *
     * @param teacherId Teacher ID
     * @return Schedule data
     */
    public List<Map<String, Object>> getTeacherSchedule(Long teacherId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/teachers/" + teacherId + "/schedule",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> schedule = (List<Map<String, Object>>) response.getBody().get("schedule");
                return schedule != null ? schedule : List.of();
            }

            return List.of();

        }, "Get teacher schedule");
    }

    /**
     * Get teacher courses
     *
     * @param teacherId Teacher ID
     * @return List of courses
     */
    public List<Map<String, Object>> getTeacherCourses(Long teacherId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/teachers/" + teacherId + "/courses",
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

        }, "Get teacher courses");
    }

    /**
     * Get teachers by department
     *
     * @param department Department name
     * @return List of teachers in department
     */
    public List<Map<String, Object>> getTeachersByDepartment(String department) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/teachers?department=" + department;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> teachers = (List<Map<String, Object>>) response.getBody().get("teachers");
                return teachers != null ? teachers : List.of();
            }

            return List.of();

        }, "Get teachers by department");
    }
}
