package com.heronix.controller.api;

import com.heronix.model.domain.ParentGuardian;
import com.heronix.service.ParentGuardianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Parent/Guardian Management
 *
 * Provides endpoints for managing parent and guardian accounts and relationships:
 * - Parent/guardian CRUD operations
 * - Student-parent relationship management
 * - Custodial and permission settings
 * - Emergency contact authorization
 * - Search and lookup operations
 *
 * Supports:
 * - Multiple parents per student
 * - Multiple students per parent (siblings)
 * - Custodial vs non-custodial relationships
 * - Pickup authorization tracking
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 31 - December 29, 2025
 */
@RestController
@RequestMapping("/api/parent-guardian")
@RequiredArgsConstructor
public class ParentGuardianApiController {

    private final ParentGuardianService parentGuardianService;

    // ==================== Parent CRUD ====================

    @PostMapping
    public ResponseEntity<Map<String, Object>> createParent(@RequestBody Map<String, Object> requestBody) {
        try {
            String firstName = (String) requestBody.get("firstName");
            String lastName = (String) requestBody.get("lastName");
            String relationship = (String) requestBody.get("relationship");
            String primaryPhone = (String) requestBody.get("primaryPhone");
            String email = (String) requestBody.get("email");
            Long createdByStaffId = Long.valueOf(requestBody.get("createdByStaffId").toString());

            ParentGuardian created = parentGuardianService.createParent(
                firstName, lastName, relationship, primaryPhone, email, createdByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("parent", created);
            response.put("message", "Parent/guardian created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create parent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getParentById(@PathVariable Long id) {
        try {
            ParentGuardian parent = parentGuardianService.getParentById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("parent", parent);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get parent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateParent(
            @PathVariable Long id,
            @RequestBody ParentGuardian updates) {

        try {
            ParentGuardian updated = parentGuardianService.updateParent(id, updates);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("parent", updated);
            response.put("message", "Parent updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update parent: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Search & Lookup ====================

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchParents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {

        try {
            List<ParentGuardian> results;

            if (name != null) {
                results = parentGuardianService.searchByName(name);
            } else if (email != null) {
                Optional<ParentGuardian> parent = parentGuardianService.findByEmail(email);
                results = parent.map(List::of).orElse(List.of());
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "At least one search parameter required: name or email");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Create Parent/Guardian",
                "endpoint", "POST /api/parent-guardian",
                "description", "Create new parent/guardian account"
            ),
            Map.of(
                "name", "Search Parents",
                "endpoint", "GET /api/parent-guardian/search",
                "description", "Search by name or email"
            ),
            Map.of(
                "name", "Update Parent",
                "endpoint", "PUT /api/parent-guardian/{id}",
                "description", "Update parent/guardian information"
            )
        ));

        dashboard.put("features", List.of(
            "Multiple parents per student support",
            "Multiple students per parent (siblings)",
            "Custodial vs non-custodial tracking",
            "Pickup authorization management",
            "Emergency contact authorization"
        ));

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/relationships")
    public ResponseEntity<Map<String, Object>> getRelationshipTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("relationshipTypes", List.of(
            "Mother", "Father", "Legal Guardian",
            "Stepmother", "Stepfather", "Grandmother", "Grandfather",
            "Aunt", "Uncle", "Foster Parent", "Other"
        ));
        return ResponseEntity.ok(types);
    }

    @GetMapping("/reference/contact-methods")
    public ResponseEntity<Map<String, Object>> getContactMethods() {
        Map<String, Object> methods = new HashMap<>();
        methods.put("contactMethods", List.of(
            "Cell Phone", "Home Phone", "Work Phone", "Email", "Text Message"
        ));
        return ResponseEntity.ok(methods);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 31");
        metadata.put("category", "Parent & Guardian Management");
        metadata.put("description", "Comprehensive parent/guardian account and relationship management");

        metadata.put("capabilities", List.of(
            "Parent/guardian account creation and management",
            "Student-parent relationship tracking",
            "Custodial and legal custody tracking",
            "Pickup and emergency authorization",
            "Search by name or email",
            "Preferred contact method tracking"
        ));

        metadata.put("endpoints", Map.of(
            "crud", List.of("POST /", "GET /{id}", "PUT /{id}"),
            "search", List.of("GET /search"),
            "reference", List.of("GET /reference/relationships", "GET /reference/contact-methods")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Parent/Guardian Management API");

        help.put("commonWorkflows", Map.of(
            "createParent", List.of(
                "1. POST /api/parent-guardian",
                "2. Link to student via student-parent relationship endpoints",
                "3. Set custodial and authorization permissions"
            ),
            "searchParent", List.of(
                "1. GET /api/parent-guardian/search?name={name}",
                "2. OR GET /api/parent-guardian/search?email={email}",
                "3. Use returned parent ID for further operations"
            ),
            "updateContact", List.of(
                "1. GET /api/parent-guardian/{id}",
                "2. PUT /api/parent-guardian/{id} with updated data",
                "3. Verify changes in response"
            )
        ));

        help.put("endpoints", Map.of(
            "create", "POST /api/parent-guardian",
            "getById", "GET /api/parent-guardian/{id}",
            "update", "PUT /api/parent-guardian/{id}",
            "search", "GET /api/parent-guardian/search",
            "dashboard", "GET /api/parent-guardian/dashboard"
        ));

        help.put("examples", Map.of(
            "createParent", "curl -X POST http://localhost:9590/api/parent-guardian -H 'Content-Type: application/json' -d '{\"firstName\":\"John\",\"lastName\":\"Doe\",\"relationship\":\"Father\",\"primaryPhone\":\"555-1234\",\"email\":\"john@example.com\",\"createdByStaffId\":1}'",
            "searchByName", "curl http://localhost:9590/api/parent-guardian/search?name=Smith",
            "searchByEmail", "curl http://localhost:9590/api/parent-guardian/search?email=john@example.com"
        ));

        help.put("notes", Map.of(
            "multipleParents", "Students can have multiple parents/guardians",
            "siblingsSupport", "Parents can be linked to multiple students (siblings)",
            "permissions", "Track custodial status, pickup authorization, emergency contact authorization",
            "contactMethods", "Store and track preferred contact method for each parent"
        ));

        return ResponseEntity.ok(help);
    }
}
