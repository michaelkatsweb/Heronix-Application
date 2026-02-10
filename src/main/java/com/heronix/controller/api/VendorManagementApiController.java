package com.heronix.controller.api;

import com.heronix.model.domain.Vendor;
import com.heronix.model.domain.Vendor.VendorStatus;
import com.heronix.model.domain.VendorCategory;
import com.heronix.service.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for Vendor Management
 *
 * Provides endpoints for managing vendors and suppliers:
 * - Vendor CRUD operations
 * - Vendor approval workflow (PENDING -> APPROVED/REJECTED/SUSPENDED)
 * - Vendor category management
 * - Purchase amount validation
 * - District sync operations
 * - Search and lookup operations
 *
 * Supports:
 * - Vendor creation and updates
 * - Multi-status workflow (PENDING, APPROVED, REJECTED, SUSPENDED, INACTIVE)
 * - Purchase limits and payment terms tracking
 * - Quote requirement validation
 * - Category-based organization
 * - District integration sync
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 32 - December 29, 2025
 */
@RestController
@RequestMapping("/api/vendor-management")
@RequiredArgsConstructor
public class VendorManagementApiController {

    private final VendorService vendorService;

    // ==================== Vendor CRUD ====================

    @PostMapping("/vendors")
    public ResponseEntity<Map<String, Object>> createVendor(
            @RequestBody Vendor vendor,
            @RequestParam String createdByUser) {

        try {
            Vendor created = vendorService.createVendor(vendor, createdByUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", created);
            response.put("message", "Vendor created successfully with PENDING status");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/{id}")
    public ResponseEntity<Map<String, Object>> getVendorById(@PathVariable Long id) {
        try {
            Optional<Vendor> vendor = vendorService.getVendorById(id);

            if (vendor.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vendor not found: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", vendor.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/vendors/{id}")
    public ResponseEntity<Map<String, Object>> updateVendor(
            @PathVariable Long id,
            @RequestBody Vendor updatedVendor,
            @RequestParam String updatedByUser) {

        try {
            Vendor updated = vendorService.updateVendor(id, updatedVendor, updatedByUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", updated);
            response.put("message", "Vendor updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found or validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/vendors/{id}")
    public ResponseEntity<Map<String, Object>> deleteVendor(
            @PathVariable Long id,
            @RequestParam String deletedByUser) {

        try {
            vendorService.deleteVendor(id, deletedByUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vendor soft-deleted successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Query Operations ====================

    @GetMapping("/vendors")
    public ResponseEntity<Map<String, Object>> getAllActiveVendors() {
        try {
            List<Vendor> vendors = vendorService.getAllActiveVendors();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendors", vendors);
            response.put("count", vendors.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get vendors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/approved")
    public ResponseEntity<Map<String, Object>> getApprovedVendors() {
        try {
            List<Vendor> vendors = vendorService.getApprovedVendors();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendors", vendors);
            response.put("count", vendors.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get approved vendors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/code/{vendorCode}")
    public ResponseEntity<Map<String, Object>> getVendorByCode(@PathVariable String vendorCode) {
        try {
            Optional<Vendor> vendor = vendorService.getVendorByCode(vendorCode);

            if (vendor.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Vendor not found with code: " + vendorCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", vendor.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/search")
    public ResponseEntity<Map<String, Object>> searchVendors(@RequestParam String name) {
        try {
            List<Vendor> results = vendorService.searchVendorsByName(name);

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

    @GetMapping("/vendors/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getVendorsByCategory(@PathVariable Long categoryId) {
        try {
            List<Vendor> vendors = vendorService.getApprovedVendorsByCategory(categoryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendors", vendors);
            response.put("count", vendors.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get vendors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/supporting-amount")
    public ResponseEntity<Map<String, Object>> getVendorsSupportingAmount(
            @RequestParam BigDecimal amount) {

        try {
            List<Vendor> vendors = vendorService.getVendorsSupportingAmount(amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendors", vendors);
            response.put("count", vendors.size());
            response.put("requestedAmount", amount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get vendors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Approval Workflow ====================

    @PostMapping("/vendors/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveVendor(
            @PathVariable Long id,
            @RequestParam String approvedByUser) {

        try {
            Vendor approved = vendorService.approveVendor(id, approvedByUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", approved);
            response.put("message", "Vendor approved successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to approve vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/vendors/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectVendor(
            @PathVariable Long id,
            @RequestParam String reason) {

        try {
            Vendor rejected = vendorService.rejectVendor(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", rejected);
            response.put("message", "Vendor rejected");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to reject vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/vendors/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendVendor(
            @PathVariable Long id,
            @RequestParam String reason) {

        try {
            Vendor suspended = vendorService.suspendVendor(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", suspended);
            response.put("message", "Vendor suspended");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to suspend vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/vendors/{id}/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateVendor(@PathVariable Long id) {
        try {
            Vendor reactivated = vendorService.reactivateVendor(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", reactivated);
            response.put("message", "Vendor reactivated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to reactivate vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Validation Operations ====================

    @GetMapping("/vendors/{id}/available-for-purchase")
    public ResponseEntity<Map<String, Object>> isVendorAvailableForPurchase(@PathVariable Long id) {
        try {
            boolean available = vendorService.isVendorAvailableForPurchase(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("available", available);
            response.put("vendorId", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check availability: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/{id}/requires-quote")
    public ResponseEntity<Map<String, Object>> doesPurchaseRequireQuote(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        try {
            boolean requiresQuote = vendorService.doesPurchaseRequireQuote(id, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requiresQuote", requiresQuote);
            response.put("vendorId", id);
            response.put("amount", amount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check quote requirement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/vendors/{id}/within-limits")
    public ResponseEntity<Map<String, Object>> isPurchaseWithinLimits(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        try {
            boolean withinLimits = vendorService.isPurchaseWithinLimits(id, amount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("withinLimits", withinLimits);
            response.put("vendorId", id);
            response.put("amount", amount);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check purchase limits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Category Management ====================

    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestBody VendorCategory category,
            @RequestParam String createdByUser) {

        try {
            VendorCategory created = vendorService.createCategory(category, createdByUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("category", created);
            response.put("message", "Vendor category created successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @RequestBody VendorCategory updatedCategory) {

        try {
            VendorCategory updated = vendorService.updateCategory(id, updatedCategory);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("category", updated);
            response.put("message", "Category updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllActiveCategories() {
        try {
            List<VendorCategory> categories = vendorService.getAllActiveCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            response.put("count", categories.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        try {
            Optional<VendorCategory> category = vendorService.getCategoryById(id);

            if (category.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Category not found: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("category", category.get());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== District Sync ====================

    @GetMapping("/vendors/sync/pending")
    public ResponseEntity<Map<String, Object>> getVendorsNeedingSync() {
        try {
            List<Vendor> vendors = vendorService.getVendorsNeedingSync();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendors", vendors);
            response.put("count", vendors.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get pending sync vendors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/vendors/{id}/sync/mark-synced")
    public ResponseEntity<Map<String, Object>> markVendorAsSynced(@PathVariable Long id) {
        try {
            vendorService.markVendorAsSynced(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vendor marked as synced");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to mark vendor as synced: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/vendors/sync/from-district")
    public ResponseEntity<Map<String, Object>> syncVendorFromDistrict(
            @RequestBody Vendor districtVendor) {

        try {
            Vendor synced = vendorService.syncVendorFromDistrict(districtVendor);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("vendor", synced);
            response.put("message", "Vendor synced from district successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to sync vendor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Create Vendor",
                "endpoint", "POST /api/vendor-management/vendors",
                "description", "Create new vendor (starts as PENDING)"
            ),
            Map.of(
                "name", "Approve Vendor",
                "endpoint", "POST /api/vendor-management/vendors/{id}/approve",
                "description", "Approve vendor for purchases"
            ),
            Map.of(
                "name", "Search Vendors",
                "endpoint", "GET /api/vendor-management/vendors/search",
                "description", "Search vendors by name"
            ),
            Map.of(
                "name", "Validate Purchase Amount",
                "endpoint", "GET /api/vendor-management/vendors/{id}/within-limits",
                "description", "Check if purchase amount is within vendor limits"
            ),
            Map.of(
                "name", "Sync from District",
                "endpoint", "POST /api/vendor-management/vendors/sync/from-district",
                "description", "Sync vendor information from district system"
            )
        ));

        dashboard.put("features", List.of(
            "Vendor approval workflow (PENDING -> APPROVED/REJECTED/SUSPENDED)",
            "Purchase amount validation and quote requirements",
            "Category-based vendor organization",
            "District integration sync support",
            "Payment terms and method tracking",
            "Soft delete with active/inactive status"
        ));

        try {
            dashboard.put("statistics", Map.of(
                "pending", vendorService.getVendorCountByStatus(VendorStatus.PENDING),
                "approved", vendorService.getVendorCountByStatus(VendorStatus.APPROVED),
                "rejected", vendorService.getVendorCountByStatus(VendorStatus.REJECTED),
                "suspended", vendorService.getVendorCountByStatus(VendorStatus.SUSPENDED),
                "activeCategories", vendorService.getActiveCategoryCount()
            ));
        } catch (Exception e) {
            dashboard.put("statisticsError", "Failed to load statistics: " + e.getMessage());
        }

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Reference Data ====================

    @GetMapping("/reference/payment-methods")
    public ResponseEntity<Map<String, Object>> getPaymentMethods() {
        Map<String, Object> methods = new HashMap<>();
        methods.put("paymentMethods", List.of(
            "Purchase Order", "Credit Card", "Check", "ACH Transfer", "Wire Transfer"
        ));
        return ResponseEntity.ok(methods);
    }

    @GetMapping("/reference/vendor-statuses")
    public ResponseEntity<Map<String, Object>> getVendorStatuses() {
        Map<String, Object> statuses = new HashMap<>();
        statuses.put("vendorStatuses", List.of(
            Map.of("value", "PENDING", "description", "Awaiting approval"),
            Map.of("value", "APPROVED", "description", "Approved for purchases"),
            Map.of("value", "REJECTED", "description", "Rejected - not approved"),
            Map.of("value", "SUSPENDED", "description", "Temporarily suspended"),
            Map.of("value", "INACTIVE", "description", "Inactive - cannot be used")
        ));
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/reference/payment-terms")
    public ResponseEntity<Map<String, Object>> getPaymentTerms() {
        Map<String, Object> terms = new HashMap<>();
        terms.put("paymentTerms", List.of(
            "Net 30", "Net 60", "Net 90", "Due on Receipt", "2/10 Net 30", "Prepaid"
        ));
        return ResponseEntity.ok(terms);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 32");
        metadata.put("category", "Vendor & Supplier Management");
        metadata.put("description", "Comprehensive vendor management with approval workflow and district sync");

        metadata.put("capabilities", List.of(
            "Vendor creation and management",
            "Approval workflow (PENDING -> APPROVED/REJECTED/SUSPENDED)",
            "Purchase amount validation and quote requirements",
            "Category-based organization",
            "District integration sync",
            "Payment terms and method tracking",
            "Search by name, code, or category"
        ));

        metadata.put("endpoints", Map.of(
            "crud", List.of("POST /vendors", "GET /vendors/{id}", "PUT /vendors/{id}", "DELETE /vendors/{id}"),
            "workflow", List.of("POST /vendors/{id}/approve", "POST /vendors/{id}/reject", "POST /vendors/{id}/suspend", "POST /vendors/{id}/reactivate"),
            "validation", List.of("GET /vendors/{id}/available-for-purchase", "GET /vendors/{id}/requires-quote", "GET /vendors/{id}/within-limits"),
            "categories", List.of("POST /categories", "GET /categories", "PUT /categories/{id}"),
            "sync", List.of("GET /vendors/sync/pending", "POST /vendors/sync/from-district")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Vendor Management API - Manage vendors, suppliers, and purchase workflows");

        help.put("commonWorkflows", Map.of(
            "createAndApproveVendor", List.of(
                "1. POST /api/vendor-management/vendors (creates with PENDING status)",
                "2. POST /api/vendor-management/vendors/{id}/approve",
                "3. Vendor is now available for purchases"
            ),
            "validatePurchase", List.of(
                "1. GET /api/vendor-management/vendors/{id}/available-for-purchase",
                "2. GET /api/vendor-management/vendors/{id}/within-limits?amount=5000",
                "3. GET /api/vendor-management/vendors/{id}/requires-quote?amount=5000",
                "4. Proceed with purchase if all validations pass"
            ),
            "searchVendors", List.of(
                "1. GET /api/vendor-management/vendors/search?name={searchTerm}",
                "2. OR GET /api/vendor-management/vendors/category/{categoryId}",
                "3. OR GET /api/vendor-management/vendors/supporting-amount?amount=5000"
            ),
            "districtSync", List.of(
                "1. GET /api/vendor-management/vendors/sync/pending (get vendors needing sync)",
                "2. POST /api/vendor-management/vendors/sync/from-district (sync vendor data)",
                "3. POST /api/vendor-management/vendors/{id}/sync/mark-synced (mark as synced)"
            )
        ));

        help.put("endpoints", Map.of(
            "vendors", "GET/POST/PUT/DELETE /api/vendor-management/vendors",
            "approval", "POST /api/vendor-management/vendors/{id}/approve",
            "search", "GET /api/vendor-management/vendors/search",
            "validation", "GET /api/vendor-management/vendors/{id}/within-limits",
            "categories", "GET/POST/PUT /api/vendor-management/categories",
            "dashboard", "GET /api/vendor-management/dashboard"
        ));

        help.put("examples", Map.of(
            "createVendor", "curl -X POST 'http://localhost:9590/api/vendor-management/vendors?createdByUser=admin' -H 'Content-Type: application/json' -d '{\"name\":\"Office Supplies Inc\",\"vendorCode\":\"OSI001\",\"email\":\"sales@officesupplies.com\"}'",
            "approveVendor", "curl -X POST 'http://localhost:9590/api/vendor-management/vendors/1/approve?approvedByUser=admin'",
            "searchVendors", "curl 'http://localhost:9590/api/vendor-management/vendors/search?name=Office'",
            "checkLimits", "curl 'http://localhost:9590/api/vendor-management/vendors/1/within-limits?amount=5000.00'"
        ));

        help.put("notes", Map.of(
            "workflow", "Vendors start as PENDING and must be approved before use",
            "softDelete", "DELETE operation sets active=false (soft delete)",
            "purchaseValidation", "Always validate vendor availability and purchase limits before creating purchase orders",
            "districtSync", "Vendors can be synced with district systems using district vendor IDs"
        ));

        return ResponseEntity.ok(help);
    }
}
