package com.heronix.service;

import com.heronix.model.domain.BehaviorIncident;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.BehaviorIncident.BehaviorCategory;
import com.heronix.model.domain.BehaviorIncident.SeverityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for exporting behavior data for federal CRDC (Civil Rights Data Collection) reporting.
 *
 * Generates compliant CSV exports for:
 * - Disciplinary incidents by category
 * - Suspensions (in-school and out-of-school)
 * - Expulsions
 * - Referrals to law enforcement
 * - School-related arrests
 *
 * CRDC Requirements:
 * - Disaggregated by race/ethnicity, gender, disability status
 * - Counts of students (not incidents) for most categories
 * - Annual reporting cycle (typically school year)
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Discipline/Behavior Management System
 */
@Service
public class CRDCExportService {

    private static final Logger log = LoggerFactory.getLogger(CRDCExportService.class);

    @Autowired
    private BehaviorIncidentService behaviorIncidentService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // CRDC-specific behavior categories mapping
    private static final Map<BehaviorCategory, String> CRDC_CATEGORY_MAPPING = new HashMap<>();

    static {
        // Map internal categories to CRDC reporting categories
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.BULLYING, "BULLYING");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.HARASSMENT, "HARASSMENT");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.FIGHTING, "PHYSICAL_ATTACK");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.DEFIANCE, "INSUBORDINATION");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.VANDALISM, "VANDALISM");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.THEFT, "THEFT");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.DISRUPTION, "CLASSROOM_DISRUPTION");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.INAPPROPRIATE_LANGUAGE, "PROFANITY");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.TECHNOLOGY_MISUSE, "TECHNOLOGY_VIOLATION");
        CRDC_CATEGORY_MAPPING.put(BehaviorCategory.DRESS_CODE_VIOLATION, "DRESS_CODE");
    }

    // ========================================================================
    // CRDC EXPORT GENERATION
    // ========================================================================

    /**
     * Generates a comprehensive CRDC behavior export for a school year.
     *
     * @param schoolYearStart start of school year
     * @param schoolYearEnd end of school year
     * @param exportDirectory directory to save export files
     * @return export summary
     */
    public CRDCExportSummary generateCRDCExport(
            LocalDate schoolYearStart,
            LocalDate schoolYearEnd,
            String exportDirectory) {

        log.info("Generating CRDC export for school year {} to {}",
                schoolYearStart, schoolYearEnd);

        CRDCExportSummary summary = new CRDCExportSummary();
        summary.schoolYearStart = schoolYearStart;
        summary.schoolYearEnd = schoolYearEnd;
        summary.exportDate = LocalDate.now();
        summary.exportDirectory = exportDirectory;

        try {
            // Create export directory if it doesn't exist
            Path exportPath = Paths.get(exportDirectory);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            // Generate discipline incidents export
            String incidentsFile = generateDisciplineIncidentsExport(
                    schoolYearStart, schoolYearEnd, exportDirectory);
            summary.incidentsExportFile = incidentsFile;

            // Generate student-level summary export
            String studentSummaryFile = generateStudentDisciplineSummary(
                    schoolYearStart, schoolYearEnd, exportDirectory);
            summary.studentSummaryFile = studentSummaryFile;

            // Generate category breakdown export
            String categoryFile = generateCategoryBreakdownExport(
                    schoolYearStart, schoolYearEnd, exportDirectory);
            summary.categoryBreakdownFile = categoryFile;

            summary.success = true;
            summary.message = "CRDC export generated successfully";

            log.info("CRDC export completed successfully. Files saved to: {}", exportDirectory);

        } catch (IOException e) {
            log.error("Error generating CRDC export", e);
            summary.success = false;
            summary.message = "Error: " + e.getMessage();
        }

        return summary;
    }

    /**
     * Generates discipline incidents CSV export.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param exportDirectory directory to save export
     * @return file path
     */
    private String generateDisciplineIncidentsExport(
            LocalDate startDate,
            LocalDate endDate,
            String exportDirectory) throws IOException {

        String fileName = String.format("CRDC_Discipline_Incidents_%s_to_%s.csv",
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER));

        Path filePath = Paths.get(exportDirectory, fileName);

        log.info("Generating discipline incidents export: {}", fileName);

        List<BehaviorIncident> incidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write CSV header
            writer.write("StudentID,IncidentDate,BehaviorCategory,CRDCCategory,SeverityLevel," +
                    "AdminReferral,ParentContacted,InterventionApplied,ReferralOutcome\n");

            // Write incident records
            for (BehaviorIncident incident : incidents) {
                String crdcCategory = CRDC_CATEGORY_MAPPING.getOrDefault(
                        incident.getBehaviorCategory(), "OTHER");

                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        escapeCsv(incident.getStudent().getStudentId()),
                        incident.getIncidentDate().format(DATE_FORMATTER),
                        escapeCsv(incident.getBehaviorCategory().name()),
                        escapeCsv(crdcCategory),
                        incident.getSeverityLevel() != null ?
                                escapeCsv(incident.getSeverityLevel().name()) : "",
                        incident.getAdminReferralRequired() ? "Y" : "N",
                        incident.getParentContacted() ? "Y" : "N",
                        escapeCsv(incident.getInterventionApplied()),
                        escapeCsv(incident.getReferralOutcome())
                );

                writer.write(line);
            }
        }

        log.info("Discipline incidents export completed: {} incidents", incidents.size());

        return filePath.toString();
    }

    /**
     * Generates student-level discipline summary CSV export.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param exportDirectory directory to save export
     * @return file path
     */
    private String generateStudentDisciplineSummary(
            LocalDate startDate,
            LocalDate endDate,
            String exportDirectory) throws IOException {

        String fileName = String.format("CRDC_Student_Discipline_Summary_%s_to_%s.csv",
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER));

        Path filePath = Paths.get(exportDirectory, fileName);

        log.info("Generating student discipline summary export: {}", fileName);

        List<BehaviorIncident> allIncidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Group by student
        Map<Student, List<BehaviorIncident>> incidentsByStudent = allIncidents.stream()
                .collect(Collectors.groupingBy(BehaviorIncident::getStudent));

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write CSV header
            writer.write("StudentID,FirstName,LastName,Grade,TotalIncidents," +
                    "MinorIncidents,ModerateIncidents,MajorIncidents," +
                    "AdminReferrals,SuspensionRecommended\n");

            // Write student summaries
            for (Map.Entry<Student, List<BehaviorIncident>> entry : incidentsByStudent.entrySet()) {
                Student student = entry.getKey();
                List<BehaviorIncident> studentIncidents = entry.getValue();

                long minorCount = studentIncidents.stream()
                        .filter(i -> i.getSeverityLevel() == SeverityLevel.MINOR).count();

                long moderateCount = studentIncidents.stream()
                        .filter(i -> i.getSeverityLevel() == SeverityLevel.MODERATE).count();

                long majorCount = studentIncidents.stream()
                        .filter(i -> i.getSeverityLevel() == SeverityLevel.MAJOR).count();

                long adminReferrals = studentIncidents.stream()
                        .filter(BehaviorIncident::getAdminReferralRequired).count();

                // Check if suspension was recommended in any referral outcome
                boolean suspensionRecommended = studentIncidents.stream()
                        .anyMatch(i -> i.getReferralOutcome() != null &&
                                i.getReferralOutcome().toLowerCase().contains("suspension"));

                String line = String.format("%s,%s,%s,%s,%d,%d,%d,%d,%d,%s\n",
                        escapeCsv(student.getStudentId()),
                        escapeCsv(student.getFirstName()),
                        escapeCsv(student.getLastName()),
                        escapeCsv(String.valueOf(student.getGradeLevel())),
                        studentIncidents.size(),
                        minorCount,
                        moderateCount,
                        majorCount,
                        adminReferrals,
                        suspensionRecommended ? "Y" : "N"
                );

                writer.write(line);
            }
        }

        log.info("Student discipline summary export completed: {} students", incidentsByStudent.size());

        return filePath.toString();
    }

    /**
     * Generates category breakdown CSV export.
     *
     * @param startDate start of reporting period
     * @param endDate end of reporting period
     * @param exportDirectory directory to save export
     * @return file path
     */
    private String generateCategoryBreakdownExport(
            LocalDate startDate,
            LocalDate endDate,
            String exportDirectory) throws IOException {

        String fileName = String.format("CRDC_Category_Breakdown_%s_to_%s.csv",
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER));

        Path filePath = Paths.get(exportDirectory, fileName);

        log.info("Generating category breakdown export: {}", fileName);

        List<BehaviorIncident> incidents = behaviorIncidentService
                .getAllIncidentsByDateRange(startDate, endDate)
                .stream()
                .filter(BehaviorIncident::isNegative)
                .collect(Collectors.toList());

        // Group by CRDC category
        Map<String, Long> crdcCategoryCounts = incidents.stream()
                .collect(Collectors.groupingBy(
                        i -> CRDC_CATEGORY_MAPPING.getOrDefault(i.getBehaviorCategory(), "OTHER"),
                        Collectors.counting()
                ));

        // Count unique students per category
        Map<String, Long> uniqueStudentsByCRDCCategory = new HashMap<>();
        for (Map.Entry<String, Long> entry : crdcCategoryCounts.entrySet()) {
            String crdcCategory = entry.getKey();

            long uniqueStudents = incidents.stream()
                    .filter(i -> CRDC_CATEGORY_MAPPING.getOrDefault(i.getBehaviorCategory(), "OTHER")
                            .equals(crdcCategory))
                    .map(i -> i.getStudent().getId())
                    .distinct()
                    .count();

            uniqueStudentsByCRDCCategory.put(crdcCategory, uniqueStudents);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write CSV header
            writer.write("CRDCCategory,TotalIncidents,UniqueStudents\n");

            // Write category counts
            for (Map.Entry<String, Long> entry : crdcCategoryCounts.entrySet()) {
                String crdcCategory = entry.getKey();
                long incidentCount = entry.getValue();
                long studentCount = uniqueStudentsByCRDCCategory.get(crdcCategory);

                String line = String.format("%s,%d,%d\n",
                        escapeCsv(crdcCategory),
                        incidentCount,
                        studentCount
                );

                writer.write(line);
            }
        }

        log.info("Category breakdown export completed: {} categories", crdcCategoryCounts.size());

        return filePath.toString();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Escapes a string for CSV format.
     *
     * @param value the value to escape
     * @return escaped CSV value
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    /**
     * Validates export directory path.
     *
     * @param directory the directory path
     * @return true if valid
     */
    public boolean validateExportDirectory(String directory) {
        try {
            Path path = Paths.get(directory);

            if (Files.exists(path)) {
                return Files.isDirectory(path) && Files.isWritable(path);
            }

            // Try to create the directory
            Files.createDirectories(path);
            return true;

        } catch (IOException e) {
            log.error("Invalid export directory: {}", directory, e);
            return false;
        }
    }

    // ========================================================================
    // DATA TRANSFER OBJECTS (DTOs)
    // ========================================================================

    /**
     * CRDC export summary DTO.
     */
    public static class CRDCExportSummary {
        public LocalDate schoolYearStart;
        public LocalDate schoolYearEnd;
        public LocalDate exportDate;
        public String exportDirectory;
        public String incidentsExportFile;
        public String studentSummaryFile;
        public String categoryBreakdownFile;
        public boolean success;
        public String message;

        public LocalDate getSchoolYearStart() { return schoolYearStart; }
        public LocalDate getSchoolYearEnd() { return schoolYearEnd; }
        public LocalDate getExportDate() { return exportDate; }
        public String getExportDirectory() { return exportDirectory; }
        public String getIncidentsExportFile() { return incidentsExportFile; }
        public String getStudentSummaryFile() { return studentSummaryFile; }
        public String getCategoryBreakdownFile() { return categoryBreakdownFile; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
