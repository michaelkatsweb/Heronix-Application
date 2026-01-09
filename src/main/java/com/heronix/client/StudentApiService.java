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
 * Student API Service
 *
 * Service layer for student management operations via REST API.
 * Provides methods to fetch, create, update, and delete student records.
 *
 * API Endpoints:
 * - GET /students - List all students
 * - GET /students/{id} - Get student details
 * - POST /students - Create new student
 * - PUT /students/{id} - Update student
 * - DELETE /students/{id} - Delete student
 * - GET /students/search - Search students
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 47 - Additional API Services
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StudentApiService {

    private final RestTemplate restTemplate;
    private final ApiRetryHandler retryHandler;

    /**
     * Get all students
     *
     * @return List of students
     */
    public List<Map<String, Object>> getAllStudents() {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/students",
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

        }, "Get all students");
    }

    /**
     * Get student by ID
     *
     * @param studentId Student ID
     * @return Student details
     */
    public Map<String, Object> getStudentById(Long studentId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/students/" + studentId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> student = (Map<String, Object>) response.getBody().get("student");
                return student != null ? student : new HashMap<>();
            }

            return new HashMap<>();

        }, "Get student by ID");
    }

    /**
     * Search students by query
     *
     * @param query Search query
     * @return Matching students
     */
    public List<Map<String, Object>> searchStudents(String query) {
        return retryHandler.executeWithRetrySafe(() -> {
            String url = "/students/search?q=" + query;

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
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

        }, "Search students");
    }

    /**
     * Create new student
     *
     * @param studentData Student data
     * @return Created student
     */
    public Map<String, Object> createStudent(Map<String, Object> studentData) {
        return retryHandler.executeWithRetrySafe(() -> {
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response =
                (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate.postForEntity(
                    "/students",
                    studentData,
                    Map.class
                );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> student = (Map<String, Object>) response.getBody().get("student");
                return student != null ? student : new HashMap<>();
            }

            return new HashMap<>();

        }, "Create student");
    }

    /**
     * Update student
     *
     * @param studentId Student ID
     * @param studentData Updated student data
     * @return Updated student
     */
    public Map<String, Object> updateStudent(Long studentId, Map<String, Object> studentData) {
        return retryHandler.executeWithRetrySafe(() -> {
            restTemplate.put("/students/" + studentId, studentData);

            // Fetch updated student
            return getStudentById(studentId);

        }, "Update student");
    }

    /**
     * Delete student
     *
     * @param studentId Student ID
     * @return true if successful
     */
    public boolean deleteStudent(Long studentId) {
        try {
            restTemplate.delete("/students/" + studentId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting student {}: {}", studentId, e.getMessage());
            return false;
        }
    }

    /**
     * Get student attendance records
     *
     * @param studentId Student ID
     * @return Attendance records
     */
    public List<Map<String, Object>> getStudentAttendance(Long studentId) {
        return retryHandler.executeWithRetrySafe(() -> {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/students/" + studentId + "/attendance",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> attendance = (List<Map<String, Object>>) response.getBody().get("attendance");
                return attendance != null ? attendance : List.of();
            }

            return List.of();

        }, "Get student attendance");
    }
}
