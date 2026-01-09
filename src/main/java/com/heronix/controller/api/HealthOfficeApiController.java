package com.heronix.controller.api;

import com.heronix.model.domain.HealthRecord;
import com.heronix.model.domain.NurseVisit;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.NurseVisit.VisitReason;
import com.heronix.model.domain.NurseVisit.Disposition;
import com.heronix.model.domain.NurseVisit.ContactMethod;
import com.heronix.model.domain.HealthRecord.ScreeningResult;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.TeacherRepository;
import com.heronix.service.HealthOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Health Office Management
 *
 * Provides endpoints for managing health office operations:
 * - Health record CRUD operations
 * - Nurse visit tracking (check-in/check-out)
 * - Health screenings (vision, hearing, scoliosis)
 * - Parent notification tracking
 * - Emergency contact management
 * - Health alerts and high-risk student identification
 *
 * Nurse Visit Workflow:
 * 1. Check-in student (record arrival time, reason, symptoms)
 * 2. Record treatments/observations (temperature, medications, assessments)
 * 3. Notify parent if required
 * 4. Check-out student (disposition: returned to class, sent home, to hospital)
 *
 * Health Screening Types:
 * - Vision screening (Snellen chart, pass/fail/refer)
 * - Hearing screening (Audiometer, pass/fail/refer)
 * - Scoliosis screening (Physical exam, pass/fail/refer)
 * - Height/Weight tracking (BMI calculation)
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 34 - December 29, 2025
 */
@RestController
@RequestMapping("/api/health-office")
@RequiredArgsConstructor
public class HealthOfficeApiController {

    private final HealthOfficeService healthOfficeService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    // ==================== Health Record Management ====================

    @PostMapping("/health-records")
    public ResponseEntity<Map<String, Object>> createHealthRecord(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String emergencyContactName = (String) requestBody.get("emergencyContactName");
            String emergencyContactRelationship = (String) requestBody.get("emergencyContactRelationship");
            String emergencyContactPhone = (String) requestBody.get("emergencyContactPhone");
            Long createdByStaffId = Long.valueOf(requestBody.get("createdByStaffId").toString());

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            HealthRecord created = healthOfficeService.createHealthRecord(
                student, emergencyContactName, emergencyContactRelationship,
                emergencyContactPhone, createdByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", created);
            response.put("message", "Health record created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "State error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create health record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health-records/{id}")
    public ResponseEntity<Map<String, Object>> getHealthRecordById(@PathVariable Long id) {
        try {
            HealthRecord healthRecord = healthOfficeService.getHealthRecordById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", healthRecord);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get health record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health-records/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getHealthRecordByStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            HealthRecord healthRecord = healthOfficeService.getHealthRecord(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", healthRecord);
            response.put("studentId", studentId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get health record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/health-records/{id}")
    public ResponseEntity<Map<String, Object>> updateHealthRecord(
            @PathVariable Long id,
            @RequestBody HealthRecord healthRecord,
            @RequestParam Long updatedByStaffId) {

        try {
            healthRecord.setId(id);
            HealthRecord updated = healthOfficeService.updateHealthRecord(healthRecord, updatedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", updated);
            response.put("message", "Health record updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update health record: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/health-records/{id}/mark-complete")
    public ResponseEntity<Map<String, Object>> markHealthRecordComplete(
            @PathVariable Long id,
            @RequestParam Long staffId) {

        try {
            HealthRecord updated = healthOfficeService.markHealthRecordComplete(id, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", updated);
            response.put("message", "Health record marked as complete");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to mark complete: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Health Screenings ====================

    @PostMapping("/health-records/{id}/vision-screening")
    public ResponseEntity<Map<String, Object>> recordVisionScreening(
            @PathVariable Long id,
            @RequestParam LocalDate screeningDate,
            @RequestParam ScreeningResult result,
            @RequestParam Long staffId) {

        try {
            HealthRecord updated = healthOfficeService.recordVisionScreening(
                id, screeningDate, result, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", updated);
            response.put("message", "Vision screening recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record screening: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/health-records/{id}/hearing-screening")
    public ResponseEntity<Map<String, Object>> recordHearingScreening(
            @PathVariable Long id,
            @RequestParam LocalDate screeningDate,
            @RequestParam ScreeningResult result,
            @RequestParam Long staffId) {

        try {
            HealthRecord updated = healthOfficeService.recordHearingScreening(
                id, screeningDate, result, staffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecord", updated);
            response.put("message", "Hearing screening recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record screening: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health-records/needing-vision-screening")
    public ResponseEntity<Map<String, Object>> getStudentsNeedingVisionScreening() {
        try {
            List<HealthRecord> records = healthOfficeService.getStudentsNeedingVisionScreening();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecords", records);
            response.put("count", records.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get records: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health-records/needing-hearing-screening")
    public ResponseEntity<Map<String, Object>> getStudentsNeedingHearingScreening() {
        try {
            List<HealthRecord> records = healthOfficeService.getStudentsNeedingHearingScreening();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecords", records);
            response.put("count", records.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get records: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health-records/high-risk")
    public ResponseEntity<Map<String, Object>> getHighRiskStudents() {
        try {
            List<HealthRecord> records = healthOfficeService.getHighRiskStudents();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecords", records);
            response.put("count", records.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get high-risk students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health-records/incomplete")
    public ResponseEntity<Map<String, Object>> getIncompleteHealthRecords() {
        try {
            List<HealthRecord> records = healthOfficeService.getIncompleteHealthRecords();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("healthRecords", records);
            response.put("count", records.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incomplete records: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Nurse Visit Management ====================

    @PostMapping("/nurse-visits/check-in")
    public ResponseEntity<Map<String, Object>> checkInStudent(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String visitReasonStr = (String) requestBody.get("visitReason");
            String chiefComplaint = (String) requestBody.get("chiefComplaint");
            Long nurseStaffId = Long.valueOf(requestBody.get("nurseStaffId").toString());

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            VisitReason visitReason = VisitReason.valueOf(visitReasonStr);

            NurseVisit visit = healthOfficeService.checkInStudent(
                student, visitReason, chiefComplaint, nurseStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visit", visit);
            response.put("message", "Student checked in successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check in student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/nurse-visits/{id}/check-out")
    public ResponseEntity<Map<String, Object>> checkOutStudent(
            @PathVariable Long id,
            @RequestParam Disposition disposition) {

        try {
            NurseVisit updated = healthOfficeService.checkOutStudent(id, disposition);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visit", updated);
            response.put("message", "Student checked out successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check out student: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/nurse-visits/{id}/record-temperature")
    public ResponseEntity<Map<String, Object>> recordTemperature(
            @PathVariable Long id,
            @RequestParam Double temperature) {

        try {
            NurseVisit updated = healthOfficeService.recordTemperature(id, temperature);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visit", updated);
            response.put("temperature", temperature);
            response.put("message", "Temperature recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record temperature: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/nurse-visits/{id}/notify-parent")
    public ResponseEntity<Map<String, Object>> recordParentNotification(
            @PathVariable Long id,
            @RequestParam ContactMethod contactMethod,
            @RequestParam String contactDetails) {

        try {
            NurseVisit updated = healthOfficeService.recordParentNotification(
                id, contactMethod, contactDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visit", updated);
            response.put("message", "Parent notification recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record notification: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/nurse-visits/{id}/send-home")
    public ResponseEntity<Map<String, Object>> sendStudentHome(
            @PathVariable Long id,
            @RequestParam String reason) {

        try {
            NurseVisit updated = healthOfficeService.sendStudentHome(id, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visit", updated);
            response.put("message", "Student sent home");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to send student home: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Nurse Visit Queries ====================

    @GetMapping("/nurse-visits/{id}")
    public ResponseEntity<Map<String, Object>> getNurseVisitById(@PathVariable Long id) {
        try {
            NurseVisit visit = healthOfficeService.getNurseVisitById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visit", visit);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get visit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/nurse-visits/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getNurseVisitsForStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<NurseVisit> visits = healthOfficeService.getNurseVisitsForStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visits", visits);
            response.put("count", visits.size());
            response.put("studentId", studentId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get visits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/nurse-visits/date-range")
    public ResponseEntity<Map<String, Object>> getNurseVisitsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            List<NurseVisit> visits = healthOfficeService.getNurseVisitsByDateRange(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visits", visits);
            response.put("count", visits.size());
            response.put("startDate", startDate);
            response.put("endDate", endDate);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get visits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/nurse-visits/active")
    public ResponseEntity<Map<String, Object>> getActiveVisits() {
        try {
            List<NurseVisit> visits = healthOfficeService.getActiveVisits();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visits", visits);
            response.put("count", visits.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get active visits: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/nurse-visits/pending-parent-notification")
    public ResponseEntity<Map<String, Object>> getVisitsRequiringParentNotification() {
        try {
            List<NurseVisit> visits = healthOfficeService.getVisitsRequiringParentNotification();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visits", visits);
            response.put("count", visits.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get pending notifications: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/nurse-visits/sent-home-today")
    public ResponseEntity<Map<String, Object>> getStudentsSentHomeToday() {
        try {
            List<NurseVisit> visits = healthOfficeService.getStudentsSentHomeToday();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("visits", visits);
            response.put("count", visits.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get students sent home: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/nurse-visits/frequent-visitors")
    public ResponseEntity<Map<String, Object>> getFrequentVisitors(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "3") int minimumVisits) {

        try {
            List<Object[]> frequentVisitors = healthOfficeService.getFrequentVisitors(
                startDate, endDate, Long.valueOf(minimumVisits));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("frequentVisitors", frequentVisitors);
            response.put("count", frequentVisitors.size());
            response.put("criteria", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "minimumVisits", Integer.valueOf(minimumVisits)
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get frequent visitors: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Check In Student",
                "endpoint", "POST /api/health-office/nurse-visits/check-in",
                "description", "Check in student to health office"
            ),
            Map.of(
                "name", "Active Visits",
                "endpoint", "GET /api/health-office/nurse-visits/active",
                "description", "View currently checked-in students"
            ),
            Map.of(
                "name", "Health Screenings",
                "endpoint", "GET /api/health-office/health-records/needing-vision-screening",
                "description", "View students needing screenings"
            ),
            Map.of(
                "name", "High-Risk Students",
                "endpoint", "GET /api/health-office/health-records/high-risk",
                "description", "View high-risk student health records"
            )
        ));

        dashboard.put("features", List.of(
            "Health record management with emergency contacts",
            "Nurse visit check-in/check-out workflow",
            "Vision and hearing screening tracking",
            "Temperature and vital signs recording",
            "Parent notification tracking",
            "High-risk student identification",
            "Frequent visitor analysis",
            "Students sent home tracking"
        ));

        try {
            dashboard.put("statistics", Map.of(
                "totalHealthRecords", healthOfficeService.getHealthRecordCount(),
                "totalNurseVisits", healthOfficeService.getNurseVisitCount(),
                "activeVisits", healthOfficeService.getActiveVisits().size(),
                "incompleteRecords", healthOfficeService.getIncompleteHealthRecords().size(),
                "needingVisionScreening", healthOfficeService.getStudentsNeedingVisionScreening().size(),
                "needingHearingScreening", healthOfficeService.getStudentsNeedingHearingScreening().size(),
                "highRiskStudents", healthOfficeService.getHighRiskStudents().size(),
                "pendingParentNotifications", healthOfficeService.getVisitsRequiringParentNotification().size()
            ));
        } catch (Exception e) {
            dashboard.put("statisticsError", "Failed to load statistics: " + e.getMessage());
        }

        return ResponseEntity.ok(dashboard);
    }

    // ==================== Metadata ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("apiVersion", "1.0.0");
        metadata.put("phase", "Phase 34");
        metadata.put("category", "Health Office Management");
        metadata.put("description", "Comprehensive health office operations and nurse visit tracking");

        metadata.put("capabilities", List.of(
            "Health record CRUD operations",
            "Nurse visit check-in/check-out workflow",
            "Vision and hearing screening management",
            "Temperature and vital signs recording",
            "Parent notification tracking",
            "High-risk student identification",
            "Frequent visitor analysis",
            "Students sent home tracking"
        ));

        metadata.put("endpoints", Map.of(
            "healthRecords", List.of("POST /health-records", "GET /health-records/{id}", "PUT /health-records/{id}", "POST /health-records/{id}/mark-complete"),
            "screenings", List.of("POST /health-records/{id}/vision-screening", "POST /health-records/{id}/hearing-screening"),
            "nurseVisits", List.of("POST /nurse-visits/check-in", "POST /nurse-visits/{id}/check-out", "POST /nurse-visits/{id}/record-temperature", "POST /nurse-visits/{id}/notify-parent"),
            "queries", List.of("GET /nurse-visits/active", "GET /nurse-visits/pending-parent-notification", "GET /health-records/high-risk")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Health Office API - Manage health records, nurse visits, and health screenings");

        help.put("commonWorkflows", Map.of(
            "nurseVisit", List.of(
                "1. POST /api/health-office/nurse-visits/check-in (student arrives)",
                "2. POST /api/health-office/nurse-visits/{id}/record-temperature (if needed)",
                "3. POST /api/health-office/nurse-visits/{id}/notify-parent (if required)",
                "4. POST /api/health-office/nurse-visits/{id}/check-out (student leaves)"
            ),
            "healthScreening", List.of(
                "1. GET /api/health-office/health-records/needing-vision-screening",
                "2. POST /api/health-office/health-records/{id}/vision-screening",
                "3. Review REFER results and notify parents"
            ),
            "sendHome", List.of(
                "1. POST /api/health-office/nurse-visits/check-in",
                "2. POST /api/health-office/nurse-visits/{id}/notify-parent",
                "3. POST /api/health-office/nurse-visits/{id}/send-home",
                "4. POST /api/health-office/nurse-visits/{id}/check-out (disposition: SENT_HOME)"
            )
        ));

        help.put("visitReasons", List.of(
            "ILLNESS - General illness symptoms",
            "INJURY - Physical injury or accident",
            "MEDICATION - Scheduled medication",
            "CHRONIC_CONDITION - Chronic condition management",
            "ASSESSMENT - Health assessment or check",
            "OTHER - Other reasons"
        ));

        help.put("dispositions", List.of(
            "RETURNED_TO_CLASS - Student returned to classroom",
            "SENT_HOME - Student sent home sick",
            "SENT_TO_HOSPITAL - Emergency hospital transport",
            "PARENT_PICKUP - Parent picked up student",
            "STILL_IN_OFFICE - Still being treated"
        ));

        help.put("examples", Map.of(
            "checkIn", "curl -X POST http://localhost:8080/api/health-office/nurse-visits/check-in -H 'Content-Type: application/json' -d '{\"studentId\":123,\"sendingTeacherId\":5,\"visitReason\":\"ILLNESS\",\"symptoms\":\"Headache, nausea\",\"arrivalTime\":\"10:30:00\",\"nurseStaffId\":1}'",
            "recordTemperature", "curl -X POST 'http://localhost:8080/api/health-office/nurse-visits/1/record-temperature?temperature=99.5'",
            "checkOut", "curl -X POST 'http://localhost:8080/api/health-office/nurse-visits/1/check-out?disposition=RETURNED_TO_CLASS'",
            "visionScreening", "curl -X POST 'http://localhost:8080/api/health-office/health-records/1/vision-screening?result=PASS&notes=20/20%20vision&staffId=1'"
        ));

        help.put("notes", Map.of(
            "workflow", "Always check in before checkout - workflow enforced by system",
            "parentNotification", "System tracks which visits require parent notification",
            "highRisk", "High-risk students flagged based on chronic conditions, allergies, special needs",
            "screenings", "Vision and hearing screenings typically done annually or as required by state"
        ));

        return ResponseEntity.ok(help);
    }
}
