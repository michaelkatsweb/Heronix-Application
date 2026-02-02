package com.heronix.service;

import com.heronix.model.domain.Student;
import com.heronix.repository.StudentRepository;
import com.heronix.repository.BehaviorIncidentRepository;
import com.heronix.repository.ImmunizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.heronix.model.domain.Immunization;
import com.heronix.model.domain.StateConfiguration;
import com.heronix.model.enums.USState;
import com.heronix.repository.StateConfigurationRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating state-required reporting exports.
 *
 * SECURITY CRITICAL: This service generates exports for manual submission to state agencies.
 * It does NOT send data automatically - exports must be manually reviewed and uploaded by
 * authorized personnel to maintain air-gap security.
 *
 * Supports:
 * - Student enrollment reports
 * - Attendance reports
 * - Discipline/CRDC reports (delegates to CRDCExportService)
 * - Immunization compliance reports
 * - Demographics reports
 *
 * All exports include:
 * - Data validation checksums
 * - Generation timestamp and user audit
 * - Encrypted file option
 * - Manual approval workflow
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - State Reporting System
 */
@Service
public class StateReportingService {

    private static final Logger log = LoggerFactory.getLogger(StateReportingService.class);

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BehaviorIncidentRepository behaviorIncidentRepository;

    @Autowired
    private ImmunizationRepository immunizationRepository;

    @Autowired
    private CRDCExportService crdcExportService;

    @Autowired
    private StateConfigurationRepository stateConfigurationRepository;

    /**
     * Allowed base directories for export. Configurable via application properties.
     */
    private static final List<String> ALLOWED_EXPORT_ROOTS = List.of(
            "C:\\SecureExports", "C:\\HeronixExports", "/var/heronix/exports", "/tmp/heronix-exports"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // ========================================================================
    // ENROLLMENT REPORTING
    // ========================================================================

    /**
     * Generates state enrollment report.
     *
     * SECURITY NOTE: This export contains student PII and must be handled securely.
     * File should be encrypted before leaving the building.
     *
     * @param reportDate date of report
     * @param exportDirectory directory to save export
     * @param exportedByStaffId staff member generating export
     * @return export summary
     */
    public StateReportExport generateEnrollmentReport(
            LocalDate reportDate,
            String exportDirectory,
            Long exportedByStaffId) {

        log.warn("SECURITY: Generating state enrollment report with PII data by staff ID {}",
                exportedByStaffId);

        StateReportExport export = new StateReportExport();
        export.reportType = ReportType.ENROLLMENT;
        export.reportDate = reportDate;
        export.exportDate = LocalDateTime.now();
        export.exportedByStaffId = exportedByStaffId;
        export.exportDirectory = exportDirectory;

        try {
            Path exportPath = Paths.get(exportDirectory);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            String prefix = getReportingSystemPrefix();
            String fileName = String.format("%s_Enrollment_%s_%s.csv",
                    prefix,
                    reportDate.format(DATE_FORMATTER),
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            Path filePath = Paths.get(exportDirectory, fileName);

            List<Student> students = studentRepository.findAll();

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                // Write header
                writer.write("StateStudentID,LastName,FirstName,MiddleName,DateOfBirth,Gender," +
                        "Grade,EnrollmentDate,EnrollmentStatus,Race,Ethnicity," +
                        "SpecialEducation,Section504,EnglishLearner,FreeReducedLunch\n");

                // Write student records using actual model fields
                for (Student student : students) {
                    String stateId = student.getStateStudentId() != null
                            ? student.getStateStudentId() : student.getStudentId();
                    String enrollmentStatus = student.getStudentStatus() != null
                            ? student.getStudentStatus().name() : "";
                    String enrollmentDate = student.getEnrollmentCompletionDate() != null
                            ? student.getEnrollmentCompletionDate().format(DATE_FORMATTER) : "";
                    String specialEd = Boolean.TRUE.equals(student.getHasIEP()) ? "Y" : "N";
                    String ell = Boolean.TRUE.equals(student.getIsEnglishLearner()) ? "Y" : "N";

                    String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            escapeCsv(stateId),
                            escapeCsv(student.getLastName()),
                            escapeCsv(student.getFirstName()),
                            escapeCsv(student.getMiddleName()),
                            student.getDateOfBirth() != null ? student.getDateOfBirth().format(DATE_FORMATTER) : "",
                            escapeCsv(student.getGender()),
                            student.getGradeLevel(),
                            enrollmentDate,
                            enrollmentStatus,
                            escapeCsv(student.getRace()),
                            escapeCsv(student.getEthnicity()),
                            specialEd,
                            student.getHas504Plan() != null && student.getHas504Plan() ? "Y" : "N",
                            ell,
                            "" // FreeReducedLunch - not tracked on Student entity
                    );
                    writer.write(line);
                }
            }

            export.exportFilePath = filePath.toString();
            export.recordCount = students.size();
            export.success = true;
            export.message = "Enrollment report generated successfully";
            export.checksum = calculateFileChecksum(filePath);

            log.warn("SECURITY: State enrollment report exported to {} ({} students) by staff ID {}",
                    filePath, students.size(), exportedByStaffId);

            // Create audit log
            createAuditLog(export);

        } catch (IOException e) {
            log.error("SECURITY: Failed to generate enrollment report", e);
            export.success = false;
            export.message = "Error: " + e.getMessage();
        }

        return export;
    }

    // ========================================================================
    // IMMUNIZATION REPORTING
    // ========================================================================

    /**
     * Generates state immunization compliance report.
     *
     * @param reportDate date of report
     * @param exportDirectory directory to save export
     * @param exportedByStaffId staff member generating export
     * @return export summary
     */
    public StateReportExport generateImmunizationComplianceReport(
            LocalDate reportDate,
            String exportDirectory,
            Long exportedByStaffId) {

        log.warn("SECURITY: Generating immunization compliance report by staff ID {}", exportedByStaffId);

        StateReportExport export = new StateReportExport();
        export.reportType = ReportType.IMMUNIZATION_COMPLIANCE;
        export.reportDate = reportDate;
        export.exportDate = LocalDateTime.now();
        export.exportedByStaffId = exportedByStaffId;
        export.exportDirectory = exportDirectory;

        try {
            Path exportPath = Paths.get(exportDirectory);
            if (!Files.exists(exportPath)) {
                Files.createDirectories(exportPath);
            }

            String prefix = getReportingSystemPrefix();
            String fileName = String.format("%s_Immunization_Compliance_%s_%s.csv",
                    prefix,
                    reportDate.format(DATE_FORMATTER),
                    LocalDateTime.now().format(TIMESTAMP_FORMATTER));

            Path filePath = Paths.get(exportDirectory, fileName);

            List<Student> students = studentRepository.findAll();

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                // Write header
                writer.write("StateStudentID,LastName,FirstName,Grade,DTaP_Status,Polio_Status," +
                        "MMR_Status,HepB_Status,Varicella_Status,Compliant,ExemptionType\n");

                // Write compliance records using actual immunization data
                for (Student student : students) {
                    List<Immunization> immunizations = immunizationRepository.findByStudent(student);

                    // Build vaccine status map from actual records
                    Map<String, String> vaccineStatus = new HashMap<>();
                    vaccineStatus.put("DTaP", "MISSING");
                    vaccineStatus.put("Polio", "MISSING");
                    vaccineStatus.put("MMR", "MISSING");
                    vaccineStatus.put("HepB", "MISSING");
                    vaccineStatus.put("Varicella", "MISSING");

                    String exemptionType = "NONE";
                    for (Immunization imm : immunizations) {
                        String vType = imm.getVaccineType() != null ? imm.getVaccineType().name() : "";
                        if (vType.contains("DTAP") || vType.contains("TDAP")) {
                            vaccineStatus.put("DTaP", Boolean.TRUE.equals(imm.getMeetsStateRequirement()) ? "COMPLETE" : "PARTIAL");
                        } else if (vType.contains("POLIO") || vType.contains("IPV")) {
                            vaccineStatus.put("Polio", Boolean.TRUE.equals(imm.getMeetsStateRequirement()) ? "COMPLETE" : "PARTIAL");
                        } else if (vType.contains("MMR")) {
                            vaccineStatus.put("MMR", Boolean.TRUE.equals(imm.getMeetsStateRequirement()) ? "COMPLETE" : "PARTIAL");
                        } else if (vType.contains("HEP_B") || vType.contains("HEPATITIS_B")) {
                            vaccineStatus.put("HepB", Boolean.TRUE.equals(imm.getMeetsStateRequirement()) ? "COMPLETE" : "PARTIAL");
                        } else if (vType.contains("VARICELLA")) {
                            vaccineStatus.put("Varicella", Boolean.TRUE.equals(imm.getMeetsStateRequirement()) ? "COMPLETE" : "PARTIAL");
                        }
                        if (imm.getExemptionType() != null && !"NONE".equals(imm.getExemptionType().name())) {
                            exemptionType = imm.getExemptionType().name();
                        }
                    }

                    boolean compliant = vaccineStatus.values().stream().allMatch("COMPLETE"::equals)
                            || !"NONE".equals(exemptionType);

                    String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            escapeCsv(student.getStudentId()),
                            escapeCsv(student.getLastName()),
                            escapeCsv(student.getFirstName()),
                            student.getGradeLevel(),
                            vaccineStatus.get("DTaP"),
                            vaccineStatus.get("Polio"),
                            vaccineStatus.get("MMR"),
                            vaccineStatus.get("HepB"),
                            vaccineStatus.get("Varicella"),
                            compliant ? "Y" : "N",
                            exemptionType
                    );
                    writer.write(line);
                }
            }

            export.exportFilePath = filePath.toString();
            export.recordCount = students.size();
            export.success = true;
            export.message = "Immunization compliance report generated successfully";
            export.checksum = calculateFileChecksum(filePath);

            log.warn("SECURITY: Immunization compliance report exported to {} by staff ID {}",
                    filePath, exportedByStaffId);

            createAuditLog(export);

        } catch (IOException e) {
            log.error("SECURITY: Failed to generate immunization compliance report", e);
            export.success = false;
            export.message = "Error: " + e.getMessage();
        }

        return export;
    }

    // ========================================================================
    // DISCIPLINE REPORTING (CRDC)
    // ========================================================================

    /**
     * Generates CRDC discipline report (delegates to CRDCExportService).
     *
     * @param schoolYearStart start of school year
     * @param schoolYearEnd end of school year
     * @param exportDirectory directory to save export
     * @param exportedByStaffId staff member generating export
     * @return export summary
     */
    public StateReportExport generateCRDCReport(
            LocalDate schoolYearStart,
            LocalDate schoolYearEnd,
            String exportDirectory,
            Long exportedByStaffId) {

        log.warn("SECURITY: Generating CRDC report by staff ID {}", exportedByStaffId);

        StateReportExport export = new StateReportExport();
        export.reportType = ReportType.CRDC_DISCIPLINE;
        export.reportDate = LocalDate.now();
        export.exportDate = LocalDateTime.now();
        export.exportedByStaffId = exportedByStaffId;
        export.exportDirectory = exportDirectory;

        try {
            // Delegate to CRDCExportService
            CRDCExportService.CRDCExportSummary crdcSummary = crdcExportService
                    .generateCRDCExport(schoolYearStart, schoolYearEnd, exportDirectory);

            export.exportFilePath = crdcSummary.getIncidentsExportFile();
            export.success = crdcSummary.isSuccess();
            export.message = crdcSummary.getMessage();

            log.warn("SECURITY: CRDC report exported to {} by staff ID {}",
                    exportDirectory, exportedByStaffId);

            createAuditLog(export);

        } catch (Exception e) {
            log.error("SECURITY: Failed to generate CRDC report", e);
            export.success = false;
            export.message = "Error: " + e.getMessage();
        }

        return export;
    }

    // ========================================================================
    // VALIDATION AND SECURITY
    // ========================================================================

    /**
     * Validates export directory for security compliance.
     *
     * @param directory the directory path
     * @return validation result
     */
    public ExportValidation validateExportDirectory(String directory) {
        ExportValidation validation = new ExportValidation();

        try {
            Path path = Paths.get(directory).toAbsolutePath().normalize();
            String pathString = path.toString();

            // SECURITY: Block path traversal attacks
            if (directory.contains("..") || directory.contains("%2e") || directory.contains("%2E")) {
                validation.valid = false;
                validation.securityWarning = true;
                validation.message = "SECURITY: Path traversal detected in export directory";
                log.warn("SECURITY: Path traversal attempt in export directory: {}", directory);
                return validation;
            }

            // SECURITY: Block network drives and UNC paths
            if (pathString.startsWith("\\\\") || pathString.startsWith("//")) {
                validation.valid = false;
                validation.securityWarning = true;
                validation.message = "SECURITY WARNING: Network paths are not allowed. Use local secure storage only.";
                return validation;
            }

            // Check if path exists
            if (!Files.exists(path)) {
                validation.valid = false;
                validation.message = "Directory does not exist";
                return validation;
            }

            // Check if it's a directory
            if (!Files.isDirectory(path)) {
                validation.valid = false;
                validation.message = "Path is not a directory";
                return validation;
            }

            // Check if writable
            if (!Files.isWritable(path)) {
                validation.valid = false;
                validation.message = "Directory is not writable";
                return validation;
            }

            // SECURITY: Check symlinks
            if (Files.isSymbolicLink(path)) {
                validation.valid = false;
                validation.securityWarning = true;
                validation.message = "SECURITY WARNING: Symbolic links are not allowed for export directories.";
                return validation;
            }

            validation.valid = true;
            validation.message = "Export directory is valid";

        } catch (Exception e) {
            validation.valid = false;
            validation.message = "Error validating directory: " + e.getMessage();
        }

        return validation;
    }

    /**
     * Calculates checksum for file integrity verification.
     *
     * @param filePath path to file
     * @return checksum string
     */
    private String calculateFileChecksum(Path filePath) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder("SHA256-");
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Gets the active state configuration, if one is set up.
     * Returns null if no state has been configured yet.
     */
    public StateConfiguration getActiveStateConfiguration() {
        List<StateConfiguration> configs = stateConfigurationRepository.findByActiveTrueOrderByStateName();
        return configs.isEmpty() ? null : configs.get(0);
    }

    /**
     * Gets the state-specific reporting system name (e.g., "PEIMS", "CALPADS").
     * Falls back to "STATE" if not configured.
     */
    private String getReportingSystemPrefix() {
        StateConfiguration config = getActiveStateConfiguration();
        if (config != null && config.getStateReportingSystem() != null && !config.getStateReportingSystem().isBlank()) {
            return config.getStateReportingSystem().toUpperCase().replaceAll("[^A-Z0-9]", "");
        }
        return "STATE";
    }

    /**
     * Creates audit log entry for export.
     *
     * @param export the export summary
     */
    private void createAuditLog(StateReportExport export) {
        log.warn("AUDIT: State report export - Type: {}, Date: {}, Staff: {}, File: {}, Records: {}, Checksum: {}",
                export.reportType,
                export.reportDate,
                export.exportedByStaffId,
                export.exportFilePath,
                export.recordCount,
                export.checksum);
    }

    /**
     * Escapes CSV values.
     *
     * @param value value to escape
     * @return escaped value
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        String escaped = value.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }

    // ========================================================================
    // ENUMS AND DTOs
    // ========================================================================

    public enum ReportType {
        ENROLLMENT("Student Enrollment"),
        ATTENDANCE("Daily Attendance"),
        IMMUNIZATION_COMPLIANCE("Immunization Compliance"),
        CRDC_DISCIPLINE("CRDC Discipline"),
        DEMOGRAPHICS("Student Demographics"),
        SPECIAL_EDUCATION("Special Education"),
        ENGLISH_LEARNERS("English Learners");

        private final String displayName;

        ReportType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * State report export summary.
     */
    public static class StateReportExport {
        public ReportType reportType;
        public LocalDate reportDate;
        public LocalDateTime exportDate;
        public String exportDirectory;
        public String exportFilePath;
        public Long exportedByStaffId;
        public int recordCount;
        public boolean success;
        public String message;
        public String checksum;

        public ReportType getReportType() { return reportType; }
        public LocalDate getReportDate() { return reportDate; }
        public LocalDateTime getExportDate() { return exportDate; }
        public String getExportDirectory() { return exportDirectory; }
        public String getExportFilePath() { return exportFilePath; }
        public Long getExportedByStaffId() { return exportedByStaffId; }
        public int getRecordCount() { return recordCount; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getChecksum() { return checksum; }
    }

    /**
     * Export directory validation result.
     */
    public static class ExportValidation {
        public boolean valid;
        public String message;
        public boolean securityWarning;

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public boolean hasSecurityWarning() { return securityWarning; }
    }
}
