package com.heronix.controller.api;

import com.heronix.model.domain.Immunization;
import com.heronix.model.domain.Immunization.VaccineType;
import com.heronix.model.domain.Immunization.VerificationMethod;
import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.service.ImmunizationService;
import com.heronix.service.ImmunizationService.ImmunizationComplianceReport;
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
 * REST API Controller for Immunization Management
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/immunizations")
@RequiredArgsConstructor
public class ImmunizationManagementApiController {

    private final ImmunizationService immunizationService;
    private final StudentRepository studentRepository;

    // ==================== Immunization CRUD Operations ====================

    @GetMapping("/{id}")
    public ResponseEntity<Immunization> getImmunizationById(@PathVariable Long id) {
        Immunization immunization = immunizationService.getImmunizationById(id);
        return ResponseEntity.ok(immunization);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Immunization>> getImmunizationsForStudent(@PathVariable Long studentId) {
        List<Immunization> immunizations = immunizationService.getImmunizationsByStudent(studentId);
        return ResponseEntity.ok(immunizations);
    }

    @GetMapping("/student/{studentId}/by-type/{type}")
    public ResponseEntity<List<Immunization>> getImmunizationsByStudentAndType(
            @PathVariable Long studentId,
            @PathVariable VaccineType type) {
        List<Immunization> allForStudent = immunizationService.getImmunizationsByStudent(studentId);
        List<Immunization> filtered = allForStudent.stream()
                .filter(imm -> imm.getVaccineType() == type)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @PostMapping
    public ResponseEntity<Immunization> createImmunization(@RequestBody Immunization immunization) {
        Immunization created = immunizationService.createImmunization(immunization);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Immunization> updateImmunization(
            @PathVariable Long id,
            @RequestBody Immunization immunization) {
        immunization.setId(id);
        Immunization updated = immunizationService.updateImmunization(immunization);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<Immunization> verifyImmunization(
            @PathVariable Long id,
            @RequestParam VerificationMethod verificationMethod,
            @RequestParam Long verifiedByStaffId) {
        Immunization verified = immunizationService.verifyImmunization(id, verificationMethod, verifiedByStaffId);
        return ResponseEntity.ok(verified);
    }

    // ==================== Compliance Operations ====================

    @GetMapping("/compliance/student/{studentId}")
    public ResponseEntity<Map<String, Boolean>> isStudentCompliant(@PathVariable Long studentId) {
        boolean compliant = immunizationService.isStudentCompliant(studentId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("compliant", compliant);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/compliance/non-compliant")
    public ResponseEntity<List<Student>> getNonCompliantStudents() {
        List<Student> students = immunizationService.getNonCompliantStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/compliance/overdue")
    public ResponseEntity<List<Immunization>> getOverdueImmunizations() {
        List<Immunization> immunizations = immunizationService.getOverdueImmunizations();
        return ResponseEntity.ok(immunizations);
    }

    @GetMapping("/compliance/due-soon")
    public ResponseEntity<List<Immunization>> getImmunizationsDueSoon() {
        List<Immunization> immunizations = immunizationService.getImmunizationsDueSoon();
        return ResponseEntity.ok(immunizations);
    }

    @GetMapping("/compliance/incomplete-series")
    public ResponseEntity<List<Immunization>> getIncompleteImmunizationSeries() {
        List<Immunization> immunizations = immunizationService.getIncompleteImmunizationSeries();
        return ResponseEntity.ok(immunizations);
    }

    // ==================== Exemption Operations ====================

    @GetMapping("/exemptions/students")
    public ResponseEntity<List<Student>> getStudentsWithExemptions() {
        List<Student> students = immunizationService.getStudentsWithExemptions();
        return ResponseEntity.ok(students);
    }

    @PostMapping("/exemptions/medical")
    public ResponseEntity<Immunization> recordMedicalExemption(
            @RequestParam Long studentId,
            @RequestParam VaccineType vaccineType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate exemptionExpirationDate,
            @RequestParam(defaultValue = "false") Boolean documentationOnFile,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long enteredByStaffId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        Immunization exemption = immunizationService.recordMedicalExemption(
                student, vaccineType, exemptionExpirationDate, documentationOnFile, notes, enteredByStaffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(exemption);
    }

    @PostMapping("/exemptions/religious")
    public ResponseEntity<Immunization> recordReligiousExemption(
            @RequestParam Long studentId,
            @RequestParam VaccineType vaccineType,
            @RequestParam(defaultValue = "false") Boolean documentationOnFile,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Long enteredByStaffId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));
        Immunization exemption = immunizationService.recordReligiousExemption(
                student, vaccineType, documentationOnFile, notes, enteredByStaffId);
        return ResponseEntity.status(HttpStatus.CREATED).body(exemption);
    }

    // ==================== Statistics and Reports ====================

    @GetMapping("/statistics/total-count")
    public ResponseEntity<Long> getTotalImmunizationsCount() {
        long count = immunizationService.getImmunizationCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary() {
        Map<String, Object> summary = new HashMap<>();

        long totalImmunizations = immunizationService.getImmunizationCount();
        List<Immunization> overdue = immunizationService.getOverdueImmunizations();
        List<Immunization> dueSoon = immunizationService.getImmunizationsDueSoon();
        List<Student> nonCompliant = immunizationService.getNonCompliantStudents();
        List<Student> studentsWithExemptions = immunizationService.getStudentsWithExemptions();
        List<Immunization> incomplete = immunizationService.getIncompleteImmunizationSeries();

        summary.put("totalImmunizations", totalImmunizations);
        summary.put("overdueCount", overdue.size());
        summary.put("dueSoonCount", dueSoon.size());
        summary.put("nonCompliantCount", nonCompliant.size());
        summary.put("studentsWithExemptions", studentsWithExemptions.size());
        summary.put("incompleteSeriesCount", incomplete.size());

        return ResponseEntity.ok(summary);
    }

    // ==================== Dashboard Endpoints ====================

    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> dashboard = new HashMap<>();

        long totalImmunizations = immunizationService.getImmunizationCount();
        List<Immunization> overdue = immunizationService.getOverdueImmunizations();
        List<Immunization> dueSoon = immunizationService.getImmunizationsDueSoon();
        List<Student> nonCompliant = immunizationService.getNonCompliantStudents();
        List<Student> studentsWithExemptions = immunizationService.getStudentsWithExemptions();
        List<Immunization> incomplete = immunizationService.getIncompleteImmunizationSeries();

        dashboard.put("totalImmunizations", totalImmunizations);
        dashboard.put("overdueCount", overdue.size());
        dashboard.put("overdueImmunizations", overdue);
        dashboard.put("dueSoonCount", dueSoon.size());
        dashboard.put("dueSoonImmunizations", dueSoon);
        dashboard.put("nonCompliantCount", nonCompliant.size());
        dashboard.put("nonCompliantStudents", nonCompliant);
        dashboard.put("studentsWithExemptions", studentsWithExemptions);
        dashboard.put("incompleteSeriesCount", incomplete.size());
        dashboard.put("incompleteSeriesImmunizations", incomplete);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@PathVariable Long studentId) {
        Map<String, Object> dashboard = new HashMap<>();

        List<Immunization> allImmunizations = immunizationService.getImmunizationsByStudent(studentId);
        boolean compliant = immunizationService.isStudentCompliant(studentId);

        // Filter overdue and due soon for this student
        List<Immunization> overdue = allImmunizations.stream()
                .filter(Immunization::isOverdue)
                .toList();
        List<Immunization> dueSoon = allImmunizations.stream()
                .filter(Immunization::isDueSoon)
                .toList();
        List<Immunization> exemptions = allImmunizations.stream()
                .filter(Immunization::hasExemption)
                .toList();
        List<Immunization> incomplete = allImmunizations.stream()
                .filter(imm -> !imm.isSeriesComplete())
                .toList();

        dashboard.put("studentId", studentId);
        dashboard.put("compliant", compliant);
        dashboard.put("totalImmunizations", allImmunizations.size());
        dashboard.put("immunizations", allImmunizations);
        dashboard.put("overdueCount", overdue.size());
        dashboard.put("overdueImmunizations", overdue);
        dashboard.put("dueSoonCount", dueSoon.size());
        dashboard.put("dueSoonImmunizations", dueSoon);
        dashboard.put("exemptions", exemptions);
        dashboard.put("incompleteSeriesCount", incomplete.size());
        dashboard.put("incompleteSeriesImmunizations", incomplete);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/dashboard/compliance")
    public ResponseEntity<Map<String, Object>> getComplianceDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        List<Student> nonCompliant = immunizationService.getNonCompliantStudents();
        List<Immunization> overdue = immunizationService.getOverdueImmunizations();
        List<Immunization> dueSoon = immunizationService.getImmunizationsDueSoon();
        List<Student> studentsWithExemptions = immunizationService.getStudentsWithExemptions();
        List<Immunization> incomplete = immunizationService.getIncompleteImmunizationSeries();

        dashboard.put("nonCompliantStudents", nonCompliant);
        dashboard.put("nonCompliantCount", nonCompliant.size());
        dashboard.put("overdueImmunizations", overdue);
        dashboard.put("overdueCount", overdue.size());
        dashboard.put("dueSoonImmunizations", dueSoon);
        dashboard.put("dueSoonCount", dueSoon.size());
        dashboard.put("studentsWithExemptions", studentsWithExemptions);
        dashboard.put("exemptionCount", studentsWithExemptions.size());
        dashboard.put("incompleteSeriesImmunizations", incomplete);
        dashboard.put("incompleteCount", incomplete.size());

        return ResponseEntity.ok(dashboard);
    }
}
