package com.heronix.controller.api;

import com.heronix.model.domain.Immunization;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Immunization.VaccineType;
import com.heronix.model.domain.Immunization.VerificationMethod;
import com.heronix.repository.StudentRepository;
import com.heronix.service.ImmunizationService;
import com.heronix.service.ImmunizationService.ImmunizationComplianceReport;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Immunization Records Management
 *
 * Provides endpoints for managing student immunization records and compliance:
 * - Immunization record CRUD operations
 * - State requirement compliance checking
 * - Exemption management (medical and religious)
 * - Vaccination series completion tracking
 * - Due date reminders and overdue alerts
 * - Verification and documentation tracking
 *
 * Supported Vaccines:
 * - DTaP/Tdap (Diphtheria, Tetanus, Pertussis)
 * - MMR (Measles, Mumps, Rubella)
 * - Polio (IPV)
 * - Hepatitis B
 * - Varicella (Chickenpox)
 * - HPV (Human Papillomavirus)
 * - Meningococcal
 * - Influenza
 * - COVID-19
 *
 * Compliance Features:
 * - Multi-dose series tracking
 * - Age-based requirements
 * - State mandate compliance
 * - Exemption documentation
 * - Overdue and due-soon alerts
 * - Non-compliant student reports
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 33 - December 29, 2025
 */
@RestController
@RequestMapping("/api/immunization")
@RequiredArgsConstructor
public class ImmunizationApiController {

    private final ImmunizationService immunizationService;
    private final StudentRepository studentRepository;

    // ==================== Immunization CRUD ====================

    @PostMapping("/records")
    public ResponseEntity<Map<String, Object>> createImmunization(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String vaccineTypeStr = (String) requestBody.get("vaccineType");
            Integer doseNumber = Integer.valueOf(requestBody.get("doseNumber").toString());
            LocalDate administrationDate = LocalDate.parse((String) requestBody.get("administrationDate"));
            String administeredBy = (String) requestBody.get("administeredBy");
            Long enteredByStaffId = Long.valueOf(requestBody.get("enteredByStaffId").toString());

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            VaccineType vaccineType = VaccineType.valueOf(vaccineTypeStr);

            Immunization created = immunizationService.createImmunization(
                student, vaccineType, doseNumber, administrationDate, administeredBy, enteredByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunization", created);
            response.put("message", "Immunization record created successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Validation error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to create immunization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<Map<String, Object>> getImmunizationById(@PathVariable Long id) {
        try {
            Immunization immunization = immunizationService.getImmunizationById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunization", immunization);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get immunization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<Map<String, Object>> updateImmunization(
            @PathVariable Long id,
            @RequestBody Immunization immunization) {

        try {
            immunization.setId(id);
            Immunization updated = immunizationService.updateImmunization(immunization);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunization", updated);
            response.put("message", "Immunization record updated successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to update immunization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Student Immunization Queries ====================

    @GetMapping("/records/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getImmunizationsForStudent(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<Immunization> immunizations = immunizationService.getImmunizationsForStudent(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunizations", immunizations);
            response.put("count", immunizations.size());
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
            errorResponse.put("error", "Failed to get immunizations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/records/student/{studentId}/type/{vaccineType}")
    public ResponseEntity<Map<String, Object>> getImmunizationsByType(
            @PathVariable Long studentId,
            @PathVariable VaccineType vaccineType) {

        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            List<Immunization> immunizations = immunizationService.getImmunizationsByType(student, vaccineType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunizations", immunizations);
            response.put("count", immunizations.size());
            response.put("studentId", studentId);
            response.put("vaccineType", vaccineType);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get immunizations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Verification & Exemptions ====================

    @PostMapping("/records/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyImmunization(
            @PathVariable Long id,
            @RequestParam VerificationMethod verificationMethod,
            @RequestParam Long verifiedByStaffId) {

        try {
            Immunization verified = immunizationService.verifyImmunization(
                id, verificationMethod, verifiedByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunization", verified);
            response.put("message", "Immunization verified successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to verify immunization: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/exemptions/medical")
    public ResponseEntity<Map<String, Object>> recordMedicalExemption(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String vaccineTypeStr = (String) requestBody.get("vaccineType");
            LocalDate expirationDate = requestBody.get("exemptionExpirationDate") != null ?
                LocalDate.parse((String) requestBody.get("exemptionExpirationDate")) : null;
            Boolean documentationOnFile = Boolean.valueOf(requestBody.get("documentationOnFile").toString());
            String notes = (String) requestBody.get("notes");
            Long enteredByStaffId = Long.valueOf(requestBody.get("enteredByStaffId").toString());

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            VaccineType vaccineType = VaccineType.valueOf(vaccineTypeStr);

            Immunization updated = immunizationService.recordMedicalExemption(
                student, vaccineType, expirationDate, documentationOnFile, notes, enteredByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunization", updated);
            response.put("message", "Medical exemption recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record exemption: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/exemptions/religious")
    public ResponseEntity<Map<String, Object>> recordReligiousExemption(
            @RequestBody Map<String, Object> requestBody) {

        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            String vaccineTypeStr = (String) requestBody.get("vaccineType");
            Boolean documentationOnFile = Boolean.valueOf(requestBody.get("documentationOnFile").toString());
            String notes = (String) requestBody.get("notes");
            Long enteredByStaffId = Long.valueOf(requestBody.get("enteredByStaffId").toString());

            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            VaccineType vaccineType = VaccineType.valueOf(vaccineTypeStr);

            Immunization updated = immunizationService.recordReligiousExemption(
                student, vaccineType, documentationOnFile, notes, enteredByStaffId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunization", updated);
            response.put("message", "Religious exemption recorded successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to record exemption: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Compliance Checking ====================

    @GetMapping("/compliance/student/{studentId}")
    public ResponseEntity<Map<String, Object>> checkCompliance(@PathVariable Long studentId) {
        try {
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

            ImmunizationComplianceReport report = immunizationService.checkCompliance(student);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("complianceReport", report);
            response.put("compliant", report.isCompliant());
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
            errorResponse.put("error", "Failed to check compliance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/compliance/student/{studentId}/status")
    public ResponseEntity<Map<String, Object>> isStudentCompliant(@PathVariable Long studentId) {
        try {
            boolean compliant = immunizationService.isStudentCompliant(studentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("studentId", studentId);
            response.put("compliant", compliant);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to check compliance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/compliance/non-compliant")
    public ResponseEntity<Map<String, Object>> getNonCompliantStudents() {
        try {
            List<Student> students = immunizationService.getNonCompliantStudents();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("students", students);
            response.put("count", students.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get non-compliant students: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Alerts & Tracking ====================

    @GetMapping("/alerts/overdue")
    public ResponseEntity<Map<String, Object>> getOverdueImmunizations() {
        try {
            List<Immunization> immunizations = immunizationService.getOverdueImmunizations();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunizations", immunizations);
            response.put("count", immunizations.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get overdue immunizations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/alerts/due-soon")
    public ResponseEntity<Map<String, Object>> getImmunizationsDueSoon() {
        try {
            List<Immunization> immunizations = immunizationService.getImmunizationsDueSoon();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunizations", immunizations);
            response.put("count", immunizations.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get due-soon immunizations: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/alerts/incomplete-series")
    public ResponseEntity<Map<String, Object>> getIncompleteImmunizationSeries() {
        try {
            List<Immunization> immunizations = immunizationService.getIncompleteImmunizationSeries();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("immunizations", immunizations);
            response.put("count", immunizations.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get incomplete series: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/students-with-exemptions")
    public ResponseEntity<Map<String, Object>> getStudentsWithExemptions() {
        try {
            List<Student> students = immunizationService.getStudentsWithExemptions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("students", students);
            response.put("count", students.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to get students with exemptions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("operations", List.of(
            Map.of(
                "name", "Create Immunization Record",
                "endpoint", "POST /api/immunization/records",
                "description", "Add immunization record for a student"
            ),
            Map.of(
                "name", "Check Compliance",
                "endpoint", "GET /api/immunization/compliance/student/{studentId}",
                "description", "View detailed compliance report for student"
            ),
            Map.of(
                "name", "Non-Compliant Students",
                "endpoint", "GET /api/immunization/compliance/non-compliant",
                "description", "List all non-compliant students"
            ),
            Map.of(
                "name", "Overdue Alerts",
                "endpoint", "GET /api/immunization/alerts/overdue",
                "description", "View overdue immunizations"
            ),
            Map.of(
                "name", "Record Exemption",
                "endpoint", "POST /api/immunization/records/{id}/exemption/medical",
                "description", "Document medical or religious exemption"
            )
        ));

        dashboard.put("vaccineTypes", List.of(
            Map.of("type", "DTAP", "name", "Diphtheria, Tetanus, Pertussis", "doses", "5"),
            Map.of("type", "MMR", "name", "Measles, Mumps, Rubella", "doses", "2"),
            Map.of("type", "POLIO", "name", "Polio (IPV)", "doses", "4"),
            Map.of("type", "HEPATITIS_B", "name", "Hepatitis B", "doses", "3"),
            Map.of("type", "VARICELLA", "name", "Chickenpox", "doses", "2"),
            Map.of("type", "HPV", "name", "Human Papillomavirus", "doses", "2-3"),
            Map.of("type", "MENINGOCOCCAL", "name", "Meningococcal", "doses", "2"),
            Map.of("type", "INFLUENZA", "name", "Influenza (Flu)", "doses", "Annual"),
            Map.of("type", "COVID19", "name", "COVID-19", "doses", "2+")
        ));

        dashboard.put("features", List.of(
            "Multi-dose vaccination series tracking",
            "State mandate compliance checking",
            "Medical and religious exemption documentation",
            "Overdue and due-soon alerts",
            "Verification and documentation tracking",
            "Non-compliant student reports",
            "Incomplete series identification",
            "Age-based requirement tracking"
        ));

        try {
            dashboard.put("statistics", Map.of(
                "totalRecords", immunizationService.getImmunizationCount(),
                "nonCompliantStudents", immunizationService.getNonCompliantStudents().size(),
                "overdueImmunizations", immunizationService.getOverdueImmunizations().size(),
                "dueSoon", immunizationService.getImmunizationsDueSoon().size(),
                "incompleteSeries", immunizationService.getIncompleteImmunizationSeries().size(),
                "studentsWithExemptions", immunizationService.getStudentsWithExemptions().size()
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
        metadata.put("phase", "Phase 33");
        metadata.put("category", "Health Office Management");
        metadata.put("description", "Student immunization records and compliance management");

        metadata.put("capabilities", List.of(
            "Immunization record CRUD operations",
            "State requirement compliance checking",
            "Multi-dose series tracking",
            "Medical and religious exemptions",
            "Verification and documentation",
            "Overdue and due-soon alerts",
            "Non-compliant student reports"
        ));

        metadata.put("endpoints", Map.of(
            "records", List.of("POST /records", "GET /records/{id}", "PUT /records/{id}", "GET /records/student/{studentId}"),
            "verification", List.of("POST /records/{id}/verify", "POST /records/{id}/exemption/medical", "POST /records/{id}/exemption/religious"),
            "compliance", List.of("GET /compliance/student/{studentId}", "GET /compliance/non-compliant"),
            "alerts", List.of("GET /alerts/overdue", "GET /alerts/due-soon", "GET /alerts/incomplete-series")
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();
        help.put("description", "Immunization Records API - Manage student immunizations and state compliance");

        help.put("commonWorkflows", Map.of(
            "addImmunization", List.of(
                "1. POST /api/immunization/records (create immunization record)",
                "2. POST /api/immunization/records/{id}/verify (verify with documentation)",
                "3. System automatically tracks series completion"
            ),
            "checkCompliance", List.of(
                "1. GET /api/immunization/compliance/student/{studentId} (detailed report)",
                "2. Review missing vaccines and required doses",
                "3. Follow up with parents for missing immunizations"
            ),
            "manageExemptions", List.of(
                "1. POST /api/immunization/records/{id}/exemption/medical (with physician documentation)",
                "2. OR POST /api/immunization/records/{id}/exemption/religious (with signed form)",
                "3. Exemptions counted toward compliance"
            ),
            "monitorAlerts", List.of(
                "1. GET /api/immunization/alerts/overdue (students needing immediate attention)",
                "2. GET /api/immunization/alerts/due-soon (upcoming due dates)",
                "3. GET /api/immunization/compliance/non-compliant (all non-compliant students)"
            )
        ));

        help.put("examples", Map.of(
            "createRecord", "curl -X POST http://localhost:9590/api/immunization/records -H 'Content-Type: application/json' -d '{\"studentId\":123,\"vaccineType\":\"MMR\",\"doseNumber\":1,\"administrationDate\":\"2025-09-15\",\"administeredBy\":\"Dr. Smith\",\"enteredByStaffId\":1}'",
            "checkCompliance", "curl http://localhost:9590/api/immunization/compliance/student/123",
            "getNonCompliant", "curl http://localhost:9590/api/immunization/compliance/non-compliant",
            "recordExemption", "curl -X POST 'http://localhost:9590/api/immunization/records/1/exemption/medical?exemptionReason=Allergic%20reaction&physicianName=Dr.%20Jones&enteredByStaffId=1'"
        ));

        help.put("notes", Map.of(
            "series", "System automatically tracks multi-dose series and calculates next dose due dates",
            "verification", "Immunization records should be verified against official documentation",
            "exemptions", "Medical exemptions require physician documentation; religious exemptions require signed form",
            "compliance", "Compliance rules vary by state and age - system uses configurable requirements"
        ));

        return ResponseEntity.ok(help);
    }
}
