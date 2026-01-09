package com.heronix.controller.api;

import com.heronix.model.domain.FamilyHousehold;
import com.heronix.model.domain.Student;
import com.heronix.service.FamilyManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Family Household Management
 *
 * Provides endpoints for managing family households and sibling relationships:
 * - Family household CRUD operations
 * - Sibling linking and unlinking
 * - Family discount management (sibling, early bird)
 * - Family status management (active, inactive, archived)
 * - Family statistics and reporting
 *
 * Family Household Features:
 * - Link multiple students as siblings under one household
 * - Automatic discount calculations based on enrolled children
 * - Support for various discount types
 * - Primary student designation
 * - Family status lifecycle management
 *
 * Discount Types:
 * - 2nd Child Discount
 * - 3rd+ Children Discount
 * - Early Bird Discount
 * - Technology Fee Waiver for 3+ children
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 30 - December 29, 2025
 */
@RestController
@RequestMapping("/api/family-management")
@RequiredArgsConstructor
public class FamilyManagementApiController {

    private final FamilyManagementService familyManagementService;

    // ==================== Family Household CRUD ====================

    @PostMapping("/families")
    public ResponseEntity<Map<String, Object>> createFamilyHousehold(
            @RequestBody Map<String, Object> requestBody,
            @RequestParam Long createdByStaffId) {

        try {
            String familyName = (String) requestBody.get("familyName");
            if (familyName == null || familyName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "familyName is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            FamilyHousehold family = familyManagementService.createFamilyHousehold(familyName, createdByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", family);
            response.put("message", "Family household created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/families/{id}")
    public ResponseEntity<Map<String, Object>> getFamilyById(@PathVariable Long id) {
        try {
            FamilyHousehold family = familyManagementService.getFamilyById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", family);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/families/by-family-id/{familyId}")
    public ResponseEntity<Map<String, Object>> getByFamilyId(@PathVariable String familyId) {
        try {
            FamilyHousehold family = familyManagementService.getByFamilyId(familyId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", family);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/families")
    public ResponseEntity<Map<String, Object>> getAllActiveFamilies() {
        try {
            List<FamilyHousehold> families = familyManagementService.getAllActiveFamilies();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("families", families);
            response.put("totalActive", families.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get families: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/families/{id}")
    public ResponseEntity<Map<String, Object>> updateFamilyHousehold(
            @PathVariable Long id,
            @RequestBody FamilyHousehold family,
            @RequestParam Long updatedByStaffId) {

        try {
            family.setId(id);
            FamilyHousehold updated = familyManagementService.updateFamilyHousehold(family, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Family household updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/families/{id}")
    public ResponseEntity<Map<String, Object>> deleteFamilyHousehold(@PathVariable Long id) {
        try {
            familyManagementService.deleteFamilyHousehold(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Family household deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Cannot delete: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Student Management ====================

    @PostMapping("/families/{familyId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> addStudentToFamily(
            @PathVariable Long familyId,
            @PathVariable Long studentId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.addStudentToFamily(familyId, studentId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Student added to family successfully");
            response.put("note", "Child counts and discounts have been recalculated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to add student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/families/{familyId}/students/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudentFromFamily(
            @PathVariable Long familyId,
            @PathVariable Long studentId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.removeStudentFromFamily(familyId, studentId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Student removed from family successfully");
            response.put("note", "Child counts and discounts have been recalculated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to remove student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/families/{familyId}/primary-student/{studentId}")
    public ResponseEntity<Map<String, Object>> setPrimaryStudent(
            @PathVariable Long familyId,
            @PathVariable Long studentId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.setPrimaryStudent(familyId, studentId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Primary student set successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to set primary student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Sibling Operations ====================

    @PostMapping("/siblings/link")
    public ResponseEntity<Map<String, Object>> linkSiblings(
            @RequestBody Map<String, Object> requestBody,
            @RequestParam Long createdByStaffId) {

        try {
            @SuppressWarnings("unchecked")
            List<Long> studentIds = (List<Long>) requestBody.get("studentIds");
            String familyName = (String) requestBody.get("familyName");

            if (studentIds == null || studentIds.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "studentIds are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            if (familyName == null || familyName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "familyName is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            FamilyHousehold family = familyManagementService.linkSiblings(studentIds, familyName, createdByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", family);
            response.put("studentsLinked", studentIds.size());
            response.put("message", "Students linked as siblings successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to link siblings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/family")
    public ResponseEntity<Map<String, Object>> getFamilyByStudent(@PathVariable Long studentId) {
        try {
            FamilyHousehold family = familyManagementService.getFamilyByStudentId(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("family", family);
            response.put("hasFamily", family != null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students/{studentId}/siblings")
    public ResponseEntity<Map<String, Object>> getSiblingsForStudent(@PathVariable Long studentId) {
        try {
            List<Student> siblings = familyManagementService.getSiblingsForStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("siblings", siblings);
            response.put("siblingCount", siblings.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get siblings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Discount Management ====================

    @PostMapping("/families/{familyId}/calculate-discounts")
    public ResponseEntity<Map<String, Object>> calculateDiscounts(
            @PathVariable Long familyId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.calculateFamilyDiscounts(familyId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Discounts calculated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to calculate discounts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/families/{familyId}/sibling-discount")
    public ResponseEntity<Map<String, Object>> applySiblingDiscount(
            @PathVariable Long familyId,
            @RequestBody Map<String, Boolean> requestBody,
            @RequestParam Long updatedByStaffId) {

        try {
            Boolean apply2ndChild = requestBody.getOrDefault("apply2ndChild", false);
            Boolean apply3rdPlus = requestBody.getOrDefault("apply3rdPlus", false);

            FamilyHousehold updated = familyManagementService.applySiblingDiscount(
                familyId, apply2ndChild, apply3rdPlus, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Sibling discount applied successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to apply discount: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/families/{familyId}/early-bird-discount")
    public ResponseEntity<Map<String, Object>> applyEarlyBirdDiscount(
            @PathVariable Long familyId,
            @RequestBody Map<String, Boolean> requestBody,
            @RequestParam Long updatedByStaffId) {

        try {
            Boolean apply = requestBody.getOrDefault("apply", false);

            FamilyHousehold updated = familyManagementService.applyEarlyBirdDiscount(familyId, apply, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", apply ? "Early bird discount applied" : "Early bird discount removed");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to apply discount: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/discounts/apply-to-eligible")
    public ResponseEntity<Map<String, Object>> applyDiscountsToEligibleFamilies(@RequestParam Long updatedByStaffId) {
        try {
            int count = familyManagementService.applyDiscountsToEligibleFamilies(updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("familiesUpdated", count);
            response.put("message", "Discounts applied to " + count + " eligible families");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to apply discounts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/discounts/eligible")
    public ResponseEntity<Map<String, Object>> getEligibleFamilies() {
        try {
            List<FamilyHousehold> eligible = familyManagementService.getFamiliesEligibleForDiscounts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("eligibleFamilies", eligible);
            response.put("count", eligible.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get eligible families: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Family Status Management ====================

    @PostMapping("/families/{familyId}/activate")
    public ResponseEntity<Map<String, Object>> activateFamily(
            @PathVariable Long familyId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.activateFamily(familyId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Family household activated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to activate family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/families/{familyId}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateFamily(
            @PathVariable Long familyId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.deactivateFamily(familyId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Family household deactivated");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to deactivate family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/families/{familyId}/archive")
    public ResponseEntity<Map<String, Object>> archiveFamily(
            @PathVariable Long familyId,
            @RequestParam Long updatedByStaffId) {

        try {
            FamilyHousehold updated = familyManagementService.archiveFamily(familyId, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("family", updated);
            response.put("message", "Family household archived");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to archive family: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Search & Queries ====================

    @GetMapping("/families/search")
    public ResponseEntity<Map<String, Object>> searchFamilies(
            @RequestParam(required = false) String familyName,
            @RequestParam(required = false) String parentName) {

        try {
            List<FamilyHousehold> results;

            if (familyName != null) {
                results = familyManagementService.searchFamiliesByName(familyName);
            } else if (parentName != null) {
                results = familyManagementService.searchFamiliesByParentName(parentName);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "At least one search parameter required: familyName or parentName");
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

    // ==================== Statistics & Reporting ====================

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            FamilyManagementService.FamilyStatistics stats = familyManagementService.getStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("totalFamilies", stats.totalFamilies());
            response.put("activeFamilies", stats.activeFamilies());
            response.put("familiesWithMultipleChildren", stats.familiesWithMultipleChildren());
            response.put("totalEnrolledChildren", stats.totalEnrolledChildren());
            response.put("totalFamilyDiscounts", stats.totalFamilyDiscounts());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/reports/discount-report")
    public ResponseEntity<Map<String, Object>> getDiscountReport() {
        try {
            List<FamilyHousehold> families = familyManagementService.getFamiliesForDiscountReport();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("families", families);
            response.put("count", families.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get discount report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            FamilyManagementService.FamilyStatistics stats = familyManagementService.getStatistics();
            List<FamilyHousehold> eligible = familyManagementService.getFamiliesEligibleForDiscounts();
            List<FamilyHousehold> activeFamilies = familyManagementService.getAllActiveFamilies();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("overview", Map.of(
                "totalFamilies", stats.totalFamilies(),
                "activeFamilies", stats.activeFamilies(),
                "totalEnrolledChildren", stats.totalEnrolledChildren(),
                "averageChildrenPerFamily", stats.totalFamilies() > 0 ?
                    (double) stats.totalEnrolledChildren() / stats.totalFamilies() : 0.0
            ));

            dashboard.put("discounts", Map.of(
                "eligibleForDiscounts", eligible.size(),
                "totalDiscountAmount", stats.totalFamilyDiscounts(),
                "familiesWithMultipleChildren", stats.familiesWithMultipleChildren()
            ));

            dashboard.put("quickStats", Map.of(
                "needsAttention", eligible.size() + " families eligible for discounts not yet applied",
                "activeFamilyCount", activeFamilies.size(),
                "discountPotential", eligible.size() + " families"
            ));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/discount-types")
    public ResponseEntity<Map<String, Object>> getDiscountTypes() {
        Map<String, Object> types = new HashMap<>();
        types.put("discountTypes", List.of(
            Map.of(
                "type", "2ND_CHILD",
                "field", "discount2ndChild",
                "description", "Discount for families with 2 enrolled children",
                "endpoint", "POST /families/{id}/sibling-discount"
            ),
            Map.of(
                "type", "3RD_PLUS_CHILDREN",
                "field", "discount3rdPlusChildren",
                "description", "Discount for families with 3+ enrolled children",
                "endpoint", "POST /families/{id}/sibling-discount"
            ),
            Map.of(
                "type", "EARLY_BIRD",
                "field", "earlyBirdDiscountApplied",
                "description", "Early registration discount",
                "endpoint", "POST /families/{id}/early-bird-discount"
            ),
            Map.of(
                "type", "TECH_FEE_WAIVER",
                "field", "waiveTechFees3rdPlus",
                "description", "Waive technology fees for 3+ children",
                "automatic", true
            )
        ));

        return ResponseEntity.ok(types);
    }

    @GetMapping("/reference/family-statuses")
    public ResponseEntity<Map<String, Object>> getFamilyStatuses() {
        Map<String, Object> statuses = new HashMap<>();
        statuses.put("statuses", List.of(
            Map.of("status", "ACTIVE", "description", "Family actively enrolled with students"),
            Map.of("status", "INACTIVE", "description", "Family temporarily inactive"),
            Map.of("status", "ARCHIVED", "description", "Family no longer has enrolled students")
        ));

        return ResponseEntity.ok(statuses);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 30");
        metadata.put("category", "Family & Household Management");
        metadata.put("description", "Comprehensive family household and sibling management with discount support");

        metadata.put("capabilities", List.of(
            "Family household CRUD operations",
            "Sibling linking and relationship management",
            "Automatic discount calculations",
            "Multiple discount types (sibling, early bird)",
            "Family status lifecycle management",
            "Family statistics and reporting",
            "Search by family name or parent name"
        ));

        metadata.put("endpoints", Map.of(
            "families", List.of("POST /families", "GET /families/{id}", "PUT /families/{id}", "DELETE /families/{id}"),
            "students", List.of("POST /families/{id}/students/{studentId}", "DELETE /families/{id}/students/{studentId}"),
            "siblings", List.of("POST /siblings/link", "GET /students/{id}/siblings"),
            "discounts", List.of("POST /families/{id}/sibling-discount", "POST /families/{id}/early-bird-discount", "POST /discounts/apply-to-eligible"),
            "status", List.of("POST /families/{id}/activate", "POST /families/{id}/deactivate", "POST /families/{id}/archive")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Family Household Management API");

        help.put("commonWorkflows", Map.of(
            "createFamily", List.of(
                "1. POST /api/family-management/families?createdByStaffId={staffId}",
                "2. POST /api/family-management/siblings/link (to add students)",
                "3. POST /api/family-management/families/{id}/calculate-discounts"
            ),
            "linkSiblings", List.of(
                "1. POST /api/family-management/siblings/link with studentIds and familyName",
                "2. System automatically creates family and links students",
                "3. Discounts calculated automatically based on enrollment"
            ),
            "applyDiscounts", List.of(
                "1. GET /api/family-management/discounts/eligible",
                "2. POST /api/family-management/discounts/apply-to-eligible?updatedByStaffId={staffId}",
                "3. Review updated families"
            ),
            "viewSiblings", List.of(
                "1. GET /api/family-management/students/{studentId}/family",
                "2. GET /api/family-management/students/{studentId}/siblings",
                "3. Use sibling information for contact sync, etc."
            )
        ));

        help.put("endpoints", Map.of(
            "createFamily", "POST /api/family-management/families?createdByStaffId={id}",
            "getFamily", "GET /api/family-management/families/{id}",
            "addStudent", "POST /api/family-management/families/{familyId}/students/{studentId}?updatedByStaffId={id}",
            "linkSiblings", "POST /api/family-management/siblings/link?createdByStaffId={id}",
            "getSiblings", "GET /api/family-management/students/{id}/siblings",
            "applyDiscounts", "POST /api/family-management/discounts/apply-to-eligible?updatedByStaffId={id}",
            "statistics", "GET /api/family-management/statistics",
            "dashboard", "GET /api/family-management/dashboard"
        ));

        help.put("examples", Map.of(
            "createFamily", "curl -X POST 'http://localhost:8080/api/family-management/families?createdByStaffId=1' -H 'Content-Type: application/json' -d '{\"familyName\":\"Smith Family\"}'",
            "linkSiblings", "curl -X POST 'http://localhost:8080/api/family-management/siblings/link?createdByStaffId=1' -H 'Content-Type: application/json' -d '{\"studentIds\":[101,102,103],\"familyName\":\"Smith Family\"}'",
            "applyDiscounts", "curl -X POST http://localhost:8080/api/family-management/discounts/apply-to-eligible?updatedByStaffId=1",
            "getStatistics", "curl http://localhost:8080/api/family-management/statistics"
        ));

        help.put("discountRules", Map.of(
            "2ndChild", "Applied when family has 2+ enrolled children",
            "3rdPlusChildren", "Applied when family has 3+ enrolled children",
            "techFeeWaiver", "Automatically applied for families with 3+ children",
            "earlyBird", "Manually applied for early registration",
            "automatic", "Discounts recalculated automatically when students added/removed"
        ));

        help.put("notes", Map.of(
            "automaticCalculation", "Discounts are automatically recalculated when students are added/removed from family",
            "familyId", "System generates unique family IDs automatically (FAM-{timestamp})",
            "deletion", "Families with linked children cannot be deleted (remove children first)",
            "siblingDetection", "Use link-siblings endpoint to establish family relationships",
            "staffTracking", "All operations require createdByStaffId or updatedByStaffId for audit trail"
        ));

        return ResponseEntity.ok(help);
    }
}
