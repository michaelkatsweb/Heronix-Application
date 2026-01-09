package com.heronix.controller.api;

import com.heronix.service.GlobalSearchService;
import com.heronix.service.GlobalSearchService.SearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Global Search
 *
 * Provides endpoints for searching across all entities (students, teachers, courses, rooms, actions).
 * Powers the Command Palette (Ctrl+K) feature.
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class GlobalSearchApiController {

    private final GlobalSearchService globalSearchService;

    // ==================== Search Operations ====================

    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String category) {

        List<SearchResult> results = globalSearchService.search(query, category);

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", category);
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> searchPost(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "");
        String category = request.getOrDefault("category", "ALL");

        List<SearchResult> results = globalSearchService.search(query, category);

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", category);
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    // ==================== Category-Specific Searches ====================

    @GetMapping("/students")
    public ResponseEntity<Map<String, Object>> searchStudents(@RequestParam String query) {
        List<SearchResult> results = globalSearchService.search(query, "STUDENTS");

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", "STUDENTS");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/teachers")
    public ResponseEntity<Map<String, Object>> searchTeachers(@RequestParam String query) {
        List<SearchResult> results = globalSearchService.search(query, "TEACHERS");

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", "TEACHERS");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/courses")
    public ResponseEntity<Map<String, Object>> searchCourses(@RequestParam String query) {
        List<SearchResult> results = globalSearchService.search(query, "COURSES");

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", "COURSES");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rooms")
    public ResponseEntity<Map<String, Object>> searchRooms(@RequestParam String query) {
        List<SearchResult> results = globalSearchService.search(query, "ROOMS");

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", "ROOMS");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/actions")
    public ResponseEntity<Map<String, Object>> searchActions(@RequestParam String query) {
        List<SearchResult> results = globalSearchService.search(query, "ACTIONS");

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("category", "ACTIONS");
        response.put("count", results.size());
        response.put("results", results);

        return ResponseEntity.ok(response);
    }

    // ==================== Quick Actions ====================

    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions(
            @RequestParam(required = false, defaultValue = "ALL") String category) {

        // Empty query returns default suggestions
        List<SearchResult> results = globalSearchService.search("", category);

        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("count", results.size());
        response.put("suggestions", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/quick-actions")
    public ResponseEntity<List<SearchResult>> getQuickActions() {
        // Get action suggestions
        List<SearchResult> actions = globalSearchService.search("", "ACTIONS");
        return ResponseEntity.ok(actions);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getSearchStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get counts for each category
        List<SearchResult> students = globalSearchService.search("", "STUDENTS");
        List<SearchResult> teachers = globalSearchService.search("", "TEACHERS");
        List<SearchResult> courses = globalSearchService.search("", "COURSES");
        List<SearchResult> rooms = globalSearchService.search("", "ROOMS");
        List<SearchResult> actions = globalSearchService.search("", "ACTIONS");

        stats.put("totalStudents", students.size());
        stats.put("totalTeachers", teachers.size());
        stats.put("totalCourses", courses.size());
        stats.put("totalRooms", rooms.size());
        stats.put("availableActions", actions.size());
        stats.put("totalSearchable", students.size() + teachers.size() + courses.size() + rooms.size());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/categories")
    public ResponseEntity<Map<String, Object>> getCategoryBreakdown() {
        Map<String, Object> breakdown = new HashMap<>();

        List<SearchResult> students = globalSearchService.search("", "STUDENTS");
        List<SearchResult> teachers = globalSearchService.search("", "TEACHERS");
        List<SearchResult> courses = globalSearchService.search("", "COURSES");
        List<SearchResult> rooms = globalSearchService.search("", "ROOMS");

        breakdown.put("students", Map.of(
            "count", students.size(),
            "icon", "👨‍🎓",
            "label", "Students"
        ));

        breakdown.put("teachers", Map.of(
            "count", teachers.size(),
            "icon", "👨‍🏫",
            "label", "Teachers"
        ));

        breakdown.put("courses", Map.of(
            "count", courses.size(),
            "icon", "📚",
            "label", "Courses"
        ));

        breakdown.put("rooms", Map.of(
            "count", rooms.size(),
            "icon", "🚪",
            "label", "Rooms"
        ));

        return ResponseEntity.ok(breakdown);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "ALL") String category,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        List<SearchResult> results = globalSearchService.search(query, category);

        // Extract primary text for autocomplete
        List<String> suggestions = results.stream()
            .limit(limit)
            .map(SearchResult::getPrimaryText)
            .toList();

        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getSearchMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("availableCategories", List.of(
            "ALL", "STUDENTS", "TEACHERS", "COURSES", "ROOMS", "ACTIONS"
        ));

        metadata.put("categoryDescriptions", Map.of(
            "ALL", "Search across all entities",
            "STUDENTS", "Search students by ID, name, email, grade",
            "TEACHERS", "Search teachers by name, email, department",
            "COURSES", "Search courses by code, name, subject",
            "ROOMS", "Search rooms by number, building, capacity",
            "ACTIONS", "Search available quick actions"
        ));

        metadata.put("features", List.of(
            "Relevance scoring",
            "Multi-category search",
            "Autocomplete support",
            "Quick action shortcuts",
            "Default suggestions"
        ));

        return ResponseEntity.ok(metadata);
    }
}
