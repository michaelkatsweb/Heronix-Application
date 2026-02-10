package com.heronix.controller.api;

import com.heronix.service.AttendanceReportingService;
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
 * REST API Controller for Attendance Reporting
 *
 * Provides state-mandated attendance reporting including:
 * - ADA (Average Daily Attendance) for state funding calculations
 * - ADM (Average Daily Membership) for enrollment reporting
 * - Truancy reporting for compliance
 * - Attendance funding reports combining ADA/ADM
 *
 * State Compliance:
 * - ADA is critical for state funding allocations
 * - ADM tracks total enrollment for capacity planning
 * - Truancy reports identify students requiring intervention
 *
 * Formulas:
 * - ADA = Total Days Present / Total Days in Period
 * - ADM = Total Days Enrolled / Total Days in Period
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since December 29, 2025
 */
@RestController
@RequestMapping("/api/attendance-reporting")
@RequiredArgsConstructor
public class AttendanceReportingApiController {

    private final AttendanceReportingService attendanceReportingService;

    // ==================== ADA Calculations ====================

    @GetMapping("/ada")
    public ResponseEntity<Map<String, Object>> calculateADA(
            @RequestParam(required = false) Long schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            AttendanceReportingService.ADACalculation ada =
                attendanceReportingService.calculateADA(schoolId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("calculation", "ADA");
            response.put("schoolId", ada.getSchoolId());
            response.put("startDate", ada.getStartDate());
            response.put("endDate", ada.getEndDate());
            response.put("totalDaysInPeriod", ada.getTotalDaysInPeriod());
            response.put("totalDaysPresent", ada.getTotalDaysPresent());
            response.put("totalDaysAbsent", ada.getTotalDaysAbsent());
            response.put("ada", ada.getAda());
            response.put("attendanceRate", String.format("%.2f%%", ada.getAttendanceRate()));
            response.put("calculatedDate", ada.getCalculatedDate());

            response.put("purpose", "State funding calculation");
            response.put("formula", "ADA = Total Days Present / Total Days in Period");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== ADM Calculations ====================

    @GetMapping("/adm")
    public ResponseEntity<Map<String, Object>> calculateADM(
            @RequestParam(required = false) Long schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            AttendanceReportingService.ADMCalculation adm =
                attendanceReportingService.calculateADM(schoolId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("calculation", "ADM");
            response.put("schoolId", adm.getSchoolId());
            response.put("startDate", adm.getStartDate());
            response.put("endDate", adm.getEndDate());
            response.put("totalDaysInPeriod", adm.getTotalDaysInPeriod());
            response.put("totalEnrollmentDays", adm.getTotalEnrollmentDays());
            response.put("totalStudents", adm.getTotalStudents());
            response.put("adm", adm.getAdm());
            response.put("averageEnrollment", adm.getAverageEnrollment());
            response.put("calculatedDate", adm.getCalculatedDate());

            response.put("purpose", "Enrollment reporting and capacity planning");
            response.put("formula", "ADM = Total Days Enrolled / Total Days in Period");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Combined Funding Report ====================

    @GetMapping("/funding-report")
    public ResponseEntity<Map<String, Object>> generateFundingReport(
            @RequestParam(required = false) Long schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            AttendanceReportingService.AttendanceFundingReport report =
                attendanceReportingService.generateFundingReport(schoolId, startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("reportType", "ATTENDANCE_FUNDING");
            response.put("schoolId", report.getSchoolId());
            response.put("startDate", report.getStartDate());
            response.put("endDate", report.getEndDate());
            response.put("reportGeneratedDate", report.getReportGeneratedDate());

            // ADA details
            Map<String, Object> adaDetails = new HashMap<>();
            adaDetails.put("value", report.getAda().getAda());
            adaDetails.put("totalDaysPresent", report.getAda().getTotalDaysPresent());
            adaDetails.put("attendanceRate", String.format("%.2f%%", report.getAda().getAttendanceRate()));
            adaDetails.put("purpose", "State funding allocation");
            response.put("ada", adaDetails);

            // ADM details
            Map<String, Object> admDetails = new HashMap<>();
            admDetails.put("value", report.getAdm().getAdm());
            admDetails.put("totalStudents", report.getAdm().getTotalStudents());
            admDetails.put("averageEnrollment", report.getAdm().getAverageEnrollment());
            admDetails.put("purpose", "Enrollment reporting");
            response.put("adm", admDetails);

            // Ratio
            response.put("attendanceToEnrollmentRatio", report.getAttendanceToEnrollmentRatio());
            response.put("ratioDescription", "ADA/ADM ratio indicates attendance efficiency");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Truancy Reporting ====================

    @GetMapping("/truancy-report")
    public ResponseEntity<Map<String, Object>> generateTruancyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "3") int truancyThreshold) {

        try {
            AttendanceReportingService.TruancyReport report =
                attendanceReportingService.generateTruancyReport(startDate, endDate, truancyThreshold);

            Map<String, Object> response = new HashMap<>();
            response.put("reportType", "TRUANCY");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("truancyThreshold", truancyThreshold);
            response.put("reportGeneratedDate", LocalDate.now());

            // Note: The actual TruancyReport structure would need to be read from the service
            // For now, return the report object directly
            response.put("report", report);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Dashboard ====================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(required = false) Long schoolId) {

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30); // Last 30 days

        Map<String, Object> dashboard = new HashMap<>();

        try {
            // Calculate current period ADA/ADM
            AttendanceReportingService.ADACalculation ada =
                attendanceReportingService.calculateADA(schoolId, startDate, endDate);
            AttendanceReportingService.ADMCalculation adm =
                attendanceReportingService.calculateADM(schoolId, startDate, endDate);

            dashboard.put("period", Map.of(
                "startDate", startDate,
                "endDate", endDate,
                "days", 30
            ));

            dashboard.put("overview", Map.of(
                "ada", ada.getAda(),
                "adm", adm.getAdm(),
                "attendanceRate", String.format("%.2f%%", ada.getAttendanceRate()),
                "totalStudents", adm.getTotalStudents()
            ));

            dashboard.put("compliance", Map.of(
                "adaForFunding", ada.getAda(),
                "admForEnrollment", adm.getAdm(),
                "fundingImpact", "Based on ADA calculation"
            ));

            dashboard.put("quickActions", Map.of(
                "generateFundingReport", "GET /api/attendance-reporting/funding-report?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
                "calculateADA", "GET /api/attendance-reporting/ada?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
                "truancyReport", "GET /api/attendance-reporting/truancy-report?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD"
            ));

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam(required = false) Long schoolId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        try {
            AttendanceReportingService.AttendanceFundingReport report =
                attendanceReportingService.generateFundingReport(schoolId, startDate, endDate);

            Map<String, Object> summary = new HashMap<>();
            summary.put("ada", report.getAda().getAda());
            summary.put("adm", report.getAdm().getAdm());
            summary.put("attendanceRate", String.format("%.2f%%", report.getAda().getAttendanceRate()));
            summary.put("ratio", report.getAttendanceToEnrollmentRatio());

            // Status indicator
            double attendanceRate = report.getAda().getAttendanceRate();
            String status;
            if (attendanceRate >= 95.0) {
                status = "EXCELLENT";
            } else if (attendanceRate >= 90.0) {
                status = "GOOD";
            } else if (attendanceRate >= 85.0) {
                status = "FAIR";
            } else {
                status = "NEEDS_IMPROVEMENT";
            }
            summary.put("status", status);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== Configuration ====================

    @GetMapping("/configuration/formulas")
    public ResponseEntity<Map<String, Object>> getFormulas() {
        Map<String, Object> formulas = new HashMap<>();

        formulas.put("ADA", Map.of(
            "name", "Average Daily Attendance",
            "formula", "Total Days Present / Total Days in Period",
            "purpose", "State funding calculations",
            "critical", true,
            "description", "Used to determine state funding allocation based on actual attendance"
        ));

        formulas.put("ADM", Map.of(
            "name", "Average Daily Membership",
            "formula", "Total Days Enrolled / Total Days in Period",
            "purpose", "Enrollment reporting and capacity planning",
            "critical", true,
            "description", "Tracks total enrollment regardless of attendance"
        ));

        formulas.put("AttendanceRate", Map.of(
            "name", "Attendance Rate",
            "formula", "(Days Present / Days Enrolled) * 100",
            "purpose", "Performance monitoring",
            "description", "Percentage of enrolled days that students were actually present"
        ));

        formulas.put("Ratio", Map.of(
            "name", "ADA/ADM Ratio",
            "formula", "ADA / ADM",
            "purpose", "Attendance efficiency indicator",
            "description", "Ratio close to 1.0 indicates high attendance; lower ratios indicate attendance issues"
        ));

        return ResponseEntity.ok(formulas);
    }

    @GetMapping("/configuration/periods")
    public ResponseEntity<Map<String, Object>> getReportingPeriods() {
        Map<String, Object> periods = new HashMap<>();

        LocalDate today = LocalDate.now();

        periods.put("currentMonth", Map.of(
            "startDate", today.withDayOfMonth(1),
            "endDate", today,
            "description", "Month-to-date reporting"
        ));

        periods.put("currentQuarter", Map.of(
            "description", "Quarter-to-date reporting",
            "note", "Calculate based on school calendar quarters"
        ));

        periods.put("yearToDate", Map.of(
            "description", "Year-to-date reporting",
            "note", "Calculate from first day of school year"
        ));

        periods.put("custom", Map.of(
            "description", "Custom date range",
            "usage", "Specify startDate and endDate parameters"
        ));

        return ResponseEntity.ok(periods);
    }

    // ==================== Utility Endpoints ====================

    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("version", "1.0.0");
        metadata.put("features", Map.of(
            "adaCalculation", "Average Daily Attendance for state funding",
            "admCalculation", "Average Daily Membership for enrollment",
            "fundingReports", "Combined ADA/ADM funding reports",
            "truancyReports", "Truancy identification and reporting"
        ));

        metadata.put("stateCompliance", Map.of(
            "adaCritical", "Required for state funding allocations",
            "admRequired", "Required for enrollment reporting",
            "truancyRequired", "Required for intervention compliance"
        ));

        metadata.put("calculations", Map.of(
            "adaFormula", "Total Days Present / Total Days in Period",
            "admFormula", "Total Days Enrolled / Total Days in Period",
            "attendanceRate", "(Days Present / Days Enrolled) * 100"
        ));

        return ResponseEntity.ok(metadata);
    }

    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getHelp() {
        Map<String, Object> help = new HashMap<>();

        help.put("title", "Attendance Reporting Help");

        help.put("commonWorkflows", Map.of(
            "monthlyFundingReport", List.of(
                "1. Determine start/end dates for reporting period",
                "2. GET /api/attendance-reporting/funding-report?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
                "3. Submit ADA value to state funding system",
                "4. File report for records"
            ),
            "truancyIdentification", List.of(
                "1. GET /api/attendance-reporting/truancy-report?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
                "2. Review students with excessive absences",
                "3. Generate intervention alerts",
                "4. Document intervention actions"
            ),
            "quarterlyReview", List.of(
                "1. Calculate ADA and ADM for quarter",
                "2. Review attendance rate trends",
                "3. Compare to previous quarters",
                "4. Identify improvement areas"
            )
        ));

        help.put("endpoints", Map.of(
            "ada", "GET /api/attendance-reporting/ada?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
            "adm", "GET /api/attendance-reporting/adm?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
            "fundingReport", "GET /api/attendance-reporting/funding-report?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD",
            "truancyReport", "GET /api/attendance-reporting/truancy-report?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD"
        ));

        help.put("parameters", Map.of(
            "schoolId", "Optional - filter by specific school/campus",
            "startDate", "Required - start of reporting period (ISO format: YYYY-MM-DD)",
            "endDate", "Required - end of reporting period (ISO format: YYYY-MM-DD)"
        ));

        help.put("examples", Map.of(
            "monthlyADA", "curl 'http://localhost:9590/api/attendance-reporting/ada?startDate=2025-01-01&endDate=2025-01-31'",
            "schoolSpecific", "curl 'http://localhost:9590/api/attendance-reporting/funding-report?schoolId=1&startDate=2025-01-01&endDate=2025-01-31'",
            "dashboard", "curl 'http://localhost:9590/api/attendance-reporting/dashboard'"
        ));

        return ResponseEntity.ok(help);
    }
}
