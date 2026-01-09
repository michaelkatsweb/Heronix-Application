package com.heronix.controller.api;

import com.heronix.model.domain.EmergencyContact;
import com.heronix.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API Controller for Emergency Contact Management
 *
 * Provides comprehensive endpoints for managing student emergency contacts including:
 * - CRUD operations for emergency contacts
 * - Priority management and reordering
 * - Authorization management (pickup, medical, financial)
 * - Contact validation and verification
 * - Bulk operations for multiple contacts
 * - Search and filtering capabilities
 * - Sibling contact synchronization
 *
 * All endpoints return JSON responses with standard structure:
 * {
 *   "success": true/false,
 *   "data": {...},
 *   "message": "Description of result"
 * }
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactApiController {

    private final EmergencyContactService contactService;

    // ========================================================================
    // CRUD OPERATIONS
    // ========================================================================

    /**
     * Create a new emergency contact for a student
     *
     * POST /api/emergency-contacts
     *
     * Request Body:
     * {
     *   "studentId": 123,
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "relationship": "Father",
     *   "primaryPhone": "555-1234",
     *   "priorityOrder": 1,
     *   "email": "john.doe@email.com",
     *   "authorizedToPickUp": true
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createContact(
            @RequestBody Map<String, Object> requestBody) {
        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String firstName = (String) requestBody.get("firstName");
            String lastName = (String) requestBody.get("lastName");
            String relationship = (String) requestBody.get("relationship");
            String primaryPhone = (String) requestBody.get("primaryPhone");
            Integer priorityOrder = Integer.valueOf(requestBody.get("priorityOrder").toString());

            EmergencyContact contact = contactService.createContact(
                    studentId, firstName, lastName, relationship, primaryPhone, priorityOrder);

            // Set optional fields if provided
            if (requestBody.containsKey("secondaryPhone")) {
                contact.setSecondaryPhone((String) requestBody.get("secondaryPhone"));
            }
            if (requestBody.containsKey("email")) {
                contact.setEmail((String) requestBody.get("email"));
            }
            if (requestBody.containsKey("workPhone")) {
                contact.setWorkPhone((String) requestBody.get("workPhone"));
            }
            if (requestBody.containsKey("streetAddress")) {
                contact.setStreetAddress((String) requestBody.get("streetAddress"));
            }
            if (requestBody.containsKey("city")) {
                contact.setCity((String) requestBody.get("city"));
            }
            if (requestBody.containsKey("state")) {
                contact.setState((String) requestBody.get("state"));
            }
            if (requestBody.containsKey("zipCode")) {
                contact.setZipCode((String) requestBody.get("zipCode"));
            }
            if (requestBody.containsKey("authorizedToPickUp")) {
                contact.setAuthorizedToPickUp(Boolean.valueOf(requestBody.get("authorizedToPickUp").toString()));
            }
            if (requestBody.containsKey("livesWithStudent")) {
                contact.setLivesWithStudent(Boolean.valueOf(requestBody.get("livesWithStudent").toString()));
            }
            if (requestBody.containsKey("notes")) {
                contact.setNotes((String) requestBody.get("notes"));
            }

            EmergencyContact updated = contactService.updateContact(contact);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Emergency contact created successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating emergency contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get emergency contact by ID
     *
     * GET /api/emergency-contacts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getContactById(@PathVariable Long id) {
        try {
            Optional<EmergencyContact> contact = contactService.getContactById(id);

            if (contact.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Emergency contact not found with ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", contact.get());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving emergency contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get all emergency contacts for a specific student
     *
     * GET /api/emergency-contacts/student/{studentId}
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getContactsByStudent(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getContactsByStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving emergency contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get active emergency contacts for a specific student
     *
     * GET /api/emergency-contacts/student/{studentId}/active
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<Map<String, Object>> getActiveContactsByStudent(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getActiveContactsByStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving active emergency contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update an existing emergency contact
     *
     * PUT /api/emergency-contacts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateContact(
            @PathVariable Long id,
            @RequestBody EmergencyContact updatedContact) {
        try {
            EmergencyContact updated = contactService.updateContact(id, updatedContact);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Emergency contact updated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating emergency contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete an emergency contact
     *
     * DELETE /api/emergency-contacts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long id) {
        try {
            contactService.deleteContact(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Emergency contact deleted successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deleting emergency contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Deactivate an emergency contact (soft delete)
     *
     * POST /api/emergency-contacts/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateContact(@PathVariable Long id) {
        try {
            EmergencyContact deactivated = contactService.deactivateContact(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", deactivated);
            response.put("message", "Emergency contact deactivated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error deactivating emergency contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Reactivate an emergency contact
     *
     * POST /api/emergency-contacts/{id}/reactivate
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Map<String, Object>> reactivateContact(@PathVariable Long id) {
        try {
            EmergencyContact reactivated = contactService.reactivateContact(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", reactivated);
            response.put("message", "Emergency contact reactivated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error reactivating emergency contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // PRIORITY MANAGEMENT
    // ========================================================================

    /**
     * Set priority for an emergency contact
     *
     * POST /api/emergency-contacts/{id}/priority
     */
    @PostMapping("/{id}/priority")
    public ResponseEntity<Map<String, Object>> setPriority(
            @PathVariable Long id,
            @RequestParam Integer newPriority) {
        try {
            EmergencyContact updated = contactService.setPriority(id, newPriority);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Priority updated successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating priority: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Move contact priority up
     *
     * POST /api/emergency-contacts/{id}/priority/up
     */
    @PostMapping("/{id}/priority/up")
    public ResponseEntity<Map<String, Object>> movePriorityUp(@PathVariable Long id) {
        try {
            EmergencyContact updated = contactService.movePriorityUp(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Contact moved up in priority");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error moving priority up: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Move contact priority down
     *
     * POST /api/emergency-contacts/{id}/priority/down
     */
    @PostMapping("/{id}/priority/down")
    public ResponseEntity<Map<String, Object>> movePriorityDown(@PathVariable Long id) {
        try {
            EmergencyContact updated = contactService.movePriorityDown(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Contact moved down in priority");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error moving priority down: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Make contact primary (set to priority 1)
     *
     * POST /api/emergency-contacts/{id}/make-primary
     */
    @PostMapping("/{id}/make-primary")
    public ResponseEntity<Map<String, Object>> makePrimary(@PathVariable Long id) {
        try {
            EmergencyContact updated = contactService.makePrimary(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Contact set as primary");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error setting primary contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Normalize priorities for a student's contacts
     *
     * POST /api/emergency-contacts/student/{studentId}/normalize-priorities
     */
    @PostMapping("/student/{studentId}/normalize-priorities")
    public ResponseEntity<Map<String, Object>> normalizePriorities(@PathVariable Long studentId) {
        try {
            contactService.normalizePriorities(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Priorities normalized successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error normalizing priorities: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // AUTHORIZATION MANAGEMENT
    // ========================================================================

    /**
     * Set pickup authorization for a contact
     *
     * POST /api/emergency-contacts/{id}/authorization/pickup
     */
    @PostMapping("/{id}/authorization/pickup")
    public ResponseEntity<Map<String, Object>> setPickupAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized) {
        try {
            EmergencyContact updated = contactService.setPickupAuthorization(id, authorized);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Pickup authorization updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating pickup authorization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Set medical authorization for a contact
     *
     * POST /api/emergency-contacts/{id}/authorization/medical
     */
    @PostMapping("/{id}/authorization/medical")
    public ResponseEntity<Map<String, Object>> setMedicalAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized) {
        try {
            EmergencyContact updated = contactService.setMedicalAuthorization(id, authorized);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Medical authorization updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating medical authorization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Set financial authorization for a contact
     *
     * POST /api/emergency-contacts/{id}/authorization/financial
     */
    @PostMapping("/{id}/authorization/financial")
    public ResponseEntity<Map<String, Object>> setFinancialAuthorization(
            @PathVariable Long id,
            @RequestParam Boolean authorized) {
        try {
            EmergencyContact updated = contactService.setFinancialAuthorization(id, authorized);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", updated);
            response.put("message", "Financial authorization updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error updating financial authorization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contacts authorized for pickup
     *
     * GET /api/emergency-contacts/student/{studentId}/authorized-pickup
     */
    @GetMapping("/student/{studentId}/authorized-pickup")
    public ResponseEntity<Map<String, Object>> getAuthorizedPickupContacts(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getAuthorizedPickupContacts(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving authorized pickup contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contacts authorized for medical decisions
     *
     * GET /api/emergency-contacts/student/{studentId}/authorized-medical
     */
    @GetMapping("/student/{studentId}/authorized-medical")
    public ResponseEntity<Map<String, Object>> getMedicalAuthorizationContacts(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getMedicalAuthorizationContacts(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving medical authorization contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // VALIDATION AND VERIFICATION
    // ========================================================================

    /**
     * Verify an emergency contact
     *
     * POST /api/emergency-contacts/{id}/verify
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyContact(@PathVariable Long id) {
        try {
            EmergencyContact verified = contactService.verifyContact(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", verified);
            response.put("message", "Contact verified successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error verifying contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get unverified contacts
     *
     * GET /api/emergency-contacts/unverified
     */
    @GetMapping("/unverified")
    public ResponseEntity<Map<String, Object>> getUnverifiedContacts() {
        try {
            List<EmergencyContact> contacts = contactService.getUnverifiedContacts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving unverified contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contacts needing reverification
     *
     * GET /api/emergency-contacts/needs-reverification
     */
    @GetMapping("/needs-reverification")
    public ResponseEntity<Map<String, Object>> getContactsNeedingReverification(
            @RequestParam(defaultValue = "365") int days) {
        try {
            List<EmergencyContact> contacts = contactService.getContactsNeedingReverification(days);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            response.put("daysSinceVerification", days);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving contacts needing reverification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get incomplete contacts
     *
     * GET /api/emergency-contacts/incomplete
     */
    @GetMapping("/incomplete")
    public ResponseEntity<Map<String, Object>> getIncompleteContacts() {
        try {
            List<EmergencyContact> contacts = contactService.getIncompleteContacts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving incomplete contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contacts missing phone numbers
     *
     * GET /api/emergency-contacts/missing-phone
     */
    @GetMapping("/missing-phone")
    public ResponseEntity<Map<String, Object>> getContactsMissingPhone() {
        try {
            List<EmergencyContact> contacts = contactService.getContactsMissingPhone();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving contacts missing phone: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get invalid contacts for a student
     *
     * GET /api/emergency-contacts/student/{studentId}/invalid
     */
    @GetMapping("/student/{studentId}/invalid")
    public ResponseEntity<Map<String, Object>> getInvalidContacts(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getInvalidContacts(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving invalid contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // QUERY OPERATIONS
    // ========================================================================

    /**
     * Get primary contact for a student
     *
     * GET /api/emergency-contacts/student/{studentId}/primary
     */
    @GetMapping("/student/{studentId}/primary")
    public ResponseEntity<Map<String, Object>> getPrimaryContact(@PathVariable Long studentId) {
        try {
            Optional<EmergencyContact> contact = contactService.getPrimaryContact(studentId);

            if (contact.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "No primary contact found for student ID: " + studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contact", contact.get());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving primary contact: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get high priority contacts (priority 1 or 2)
     *
     * GET /api/emergency-contacts/student/{studentId}/high-priority
     */
    @GetMapping("/student/{studentId}/high-priority")
    public ResponseEntity<Map<String, Object>> getHighPriorityContacts(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getHighPriorityContacts(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving high priority contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contacts by relationship
     *
     * GET /api/emergency-contacts/student/{studentId}/relationship/{relationship}
     */
    @GetMapping("/student/{studentId}/relationship/{relationship}")
    public ResponseEntity<Map<String, Object>> getContactsByRelationship(
            @PathVariable Long studentId,
            @PathVariable String relationship) {
        try {
            List<EmergencyContact> contacts = contactService.getContactsByRelationship(studentId, relationship);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            response.put("relationship", relationship);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving contacts by relationship: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contacts living with student
     *
     * GET /api/emergency-contacts/student/{studentId}/living-with
     */
    @GetMapping("/student/{studentId}/living-with")
    public ResponseEntity<Map<String, Object>> getContactsLivingWithStudent(@PathVariable Long studentId) {
        try {
            List<EmergencyContact> contacts = contactService.getContactsLivingWithStudent(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving contacts living with student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // SEARCH OPERATIONS
    // ========================================================================

    /**
     * Search contacts by name
     *
     * GET /api/emergency-contacts/search/name
     */
    @GetMapping("/search/name")
    public ResponseEntity<Map<String, Object>> searchContactsByName(@RequestParam String searchTerm) {
        try {
            List<EmergencyContact> contacts = contactService.searchContactsByName(searchTerm);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            response.put("searchTerm", searchTerm);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching contacts by name: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Search contacts by phone
     *
     * GET /api/emergency-contacts/search/phone
     */
    @GetMapping("/search/phone")
    public ResponseEntity<Map<String, Object>> searchContactsByPhone(@RequestParam String phoneNumber) {
        try {
            List<EmergencyContact> contacts = contactService.searchContactsByPhone(phoneNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            response.put("phoneNumber", phoneNumber);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching contacts by phone: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Search contacts by email
     *
     * GET /api/emergency-contacts/search/email
     */
    @GetMapping("/search/email")
    public ResponseEntity<Map<String, Object>> searchContactsByEmail(@RequestParam String email) {
        try {
            List<EmergencyContact> contacts = contactService.searchContactsByEmail(email);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", contacts);
            response.put("count", contacts.size());
            response.put("email", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching contacts by email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // BULK OPERATIONS
    // ========================================================================

    /**
     * Bulk verify contacts
     *
     * POST /api/emergency-contacts/bulk/verify
     */
    @PostMapping("/bulk/verify")
    public ResponseEntity<Map<String, Object>> bulkVerifyContacts(@RequestBody List<Long> contactIds) {
        try {
            List<EmergencyContact> verified = contactService.bulkVerifyContacts(contactIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", verified);
            response.put("count", verified.size());
            response.put("message", "Contacts verified successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error bulk verifying contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Bulk deactivate contacts
     *
     * POST /api/emergency-contacts/bulk/deactivate
     */
    @PostMapping("/bulk/deactivate")
    public ResponseEntity<Map<String, Object>> bulkDeactivateContacts(@RequestBody List<Long> contactIds) {
        try {
            List<EmergencyContact> deactivated = contactService.bulkDeactivateContacts(contactIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", deactivated);
            response.put("count", deactivated.size());
            response.put("message", "Contacts deactivated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error bulk deactivating contacts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Copy contacts from one student to another (for siblings)
     *
     * POST /api/emergency-contacts/copy-to-sibling
     */
    @PostMapping("/copy-to-sibling")
    public ResponseEntity<Map<String, Object>> copyContactsToSibling(
            @RequestParam Long sourceStudentId,
            @RequestParam Long targetStudentId) {
        try {
            List<EmergencyContact> copiedContacts = contactService.copyContactsToSibling(sourceStudentId, targetStudentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", copiedContacts);
            response.put("count", copiedContacts.size());
            response.put("message", "Contacts copied successfully to sibling");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error copying contacts to sibling: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Copy a contact to all siblings
     *
     * POST /api/emergency-contacts/{id}/copy-to-siblings
     */
    @PostMapping("/{id}/copy-to-siblings")
    public ResponseEntity<Map<String, Object>> copyContactToSiblings(@PathVariable Long id) {
        try {
            List<EmergencyContact> copiedContacts = contactService.copyContactToSiblings(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("contacts", copiedContacts);
            response.put("count", copiedContacts.size());
            response.put("message", "Contact copied to siblings successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error copying contact to siblings: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // STATISTICS AND DASHBOARD
    // ========================================================================

    /**
     * Get emergency contact dashboard statistics
     *
     * GET /api/emergency-contacts/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        try {
            List<EmergencyContact> allContacts = contactService.getAllContacts();
            List<EmergencyContact> unverified = contactService.getUnverifiedContacts();
            List<EmergencyContact> incomplete = contactService.getIncompleteContacts();
            List<EmergencyContact> missingPhone = contactService.getContactsMissingPhone();
            List<EmergencyContact> needsReverification = contactService.getContactsNeedingReverification(365);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalContacts", allContacts.size());
            stats.put("unverifiedContacts", unverified.size());
            stats.put("incompleteContacts", incomplete.size());
            stats.put("contactsMissingPhone", missingPhone.size());
            stats.put("contactsNeedingReverification", needsReverification.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving dashboard: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get contact requirements status for a student
     *
     * GET /api/emergency-contacts/student/{studentId}/requirements
     */
    @GetMapping("/student/{studentId}/requirements")
    public ResponseEntity<Map<String, Object>> getContactRequirements(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "2") int minimumRequired) {
        try {
            long activeCount = contactService.countActiveContactsForStudent(studentId);
            boolean hasMinimum = contactService.hasMinimumContacts(studentId, minimumRequired);
            String missingRequirements = contactService.getMissingContactRequirements(studentId, minimumRequired);

            Map<String, Object> requirements = new HashMap<>();
            requirements.put("activeContactCount", activeCount);
            requirements.put("minimumRequired", minimumRequired);
            requirements.put("hasMinimumContacts", hasMinimum);
            requirements.put("missingRequirements", missingRequirements);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requirements", requirements);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error retrieving contact requirements: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========================================================================
    // METADATA AND HELP
    // ========================================================================

    /**
     * Get API metadata
     *
     * GET /api/emergency-contacts/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0.0");
        metadata.put("description", "Emergency Contact Management API");
        metadata.put("features", Arrays.asList(
                "CRUD operations for emergency contacts",
                "Priority management and reordering",
                "Authorization management (pickup, medical, financial)",
                "Contact validation and verification",
                "Bulk operations for multiple contacts",
                "Search and filtering capabilities",
                "Sibling contact synchronization"
        ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("metadata", metadata);
        return ResponseEntity.ok(response);
    }

    /**
     * Get API help and usage information
     *
     * GET /api/emergency-contacts/help
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Emergency Contact Management API - Comprehensive endpoints for managing student emergency contacts");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("POST /api/emergency-contacts", "Create new emergency contact");
        endpoints.put("GET /api/emergency-contacts/{id}", "Get contact by ID");
        endpoints.put("GET /api/emergency-contacts/student/{studentId}", "Get all contacts for a student");
        endpoints.put("PUT /api/emergency-contacts/{id}", "Update emergency contact");
        endpoints.put("DELETE /api/emergency-contacts/{id}", "Delete emergency contact");
        endpoints.put("POST /api/emergency-contacts/{id}/deactivate", "Deactivate contact");
        endpoints.put("POST /api/emergency-contacts/{id}/priority", "Set contact priority");
        endpoints.put("POST /api/emergency-contacts/{id}/authorization/pickup", "Set pickup authorization");
        endpoints.put("POST /api/emergency-contacts/{id}/verify", "Verify contact");
        endpoints.put("GET /api/emergency-contacts/search/name", "Search contacts by name");
        endpoints.put("POST /api/emergency-contacts/bulk/verify", "Bulk verify contacts");
        endpoints.put("GET /api/emergency-contacts/dashboard", "Get dashboard statistics");

        help.put("endpoints", endpoints);

        Map<String, String> examples = new LinkedHashMap<>();
        examples.put("Create Contact", "POST /api/emergency-contacts with body: {\"studentId\": 123, \"firstName\": \"John\", \"lastName\": \"Doe\", \"relationship\": \"Father\", \"primaryPhone\": \"555-1234\", \"priorityOrder\": 1}");
        examples.put("Get Student Contacts", "GET /api/emergency-contacts/student/123");
        examples.put("Set Pickup Authorization", "POST /api/emergency-contacts/456/authorization/pickup?authorized=true");
        examples.put("Search by Name", "GET /api/emergency-contacts/search/name?searchTerm=John");
        examples.put("Get Dashboard", "GET /api/emergency-contacts/dashboard");

        help.put("examples", examples);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("help", help);
        return ResponseEntity.ok(response);
    }
}
