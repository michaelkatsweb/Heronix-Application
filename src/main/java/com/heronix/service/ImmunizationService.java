package com.heronix.service;

import com.heronix.model.domain.Immunization;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Immunization.VaccineType;
import com.heronix.model.domain.Immunization.VerificationMethod;
import com.heronix.repository.ImmunizationRepository;
import com.heronix.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing student immunizations and compliance.
 *
 * Handles:
 * - Immunization record CRUD operations
 * - State requirement compliance checking
 * - Exemption management
 * - Series completion tracking
 * - Due date reminders
 * - State reporting exports
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Health Office Management System
 */
@Service
public class ImmunizationService {

    private static final Logger log = LoggerFactory.getLogger(ImmunizationService.class);

    @Autowired
    private ImmunizationRepository immunizationRepository;

    @Autowired
    private StudentRepository studentRepository;

    // ========================================================================
    // IMMUNIZATION CRUD OPERATIONS
    // ========================================================================

    /**
     * Creates a new immunization record.
     *
     * @param student the student
     * @param vaccineType type of vaccine
     * @param doseNumber dose number in series
     * @param administrationDate date administered
     * @param administeredBy provider name
     * @param enteredByStaffId staff entering the record
     * @return created immunization record
     */
    @Transactional
    public Immunization createImmunization(
            Student student,
            VaccineType vaccineType,
            Integer doseNumber,
            LocalDate administrationDate,
            String administeredBy,
            Long enteredByStaffId) {

        log.info("Creating immunization record for student ID {}: {} dose {}",
                student.getId(), vaccineType, doseNumber);

        Immunization immunization = Immunization.builder()
                .student(student)
                .vaccineType(vaccineType)
                .doseNumber(doseNumber)
                .totalDosesRequired(vaccineType.getTypicalDosesRequired())
                .administrationDate(administrationDate)
                .administeredBy(administeredBy)
                .enteredByStaffId(enteredByStaffId)
                .entryDate(LocalDateTime.now())
                .verified(false)
                .documentationOnFile(false)
                .build();

        // Calculate next dose due date if series not complete
        if (doseNumber < vaccineType.getTypicalDosesRequired()) {
            immunization.setNextDoseDueDate(calculateNextDoseDate(vaccineType, doseNumber, administrationDate));
        }

        immunization = immunizationRepository.save(immunization);
        log.info("Created immunization record ID {} for student: {} {}",
                immunization.getId(), student.getFirstName(), student.getLastName());

        return immunization;
    }

    /**
     * Creates a new immunization record from an Immunization object.
     * Overloaded method to support UI controllers that build the object directly.
     *
     * @param immunization the immunization record to create
     * @return created immunization record
     */
    @Transactional
    public Immunization createImmunization(Immunization immunization) {
        log.info("Creating immunization record for student ID {}: {} dose {}",
                immunization.getStudent().getId(),
                immunization.getVaccineType(),
                immunization.getDoseNumber());

        if (immunization.getEntryDate() == null) {
            immunization.setEntryDate(LocalDateTime.now());
        }

        // Calculate next dose due date if series not complete
        if (immunization.getDoseNumber() < immunization.getTotalDosesRequired()) {
            immunization.setNextDoseDueDate(calculateNextDoseDate(
                    immunization.getVaccineType(),
                    immunization.getDoseNumber(),
                    immunization.getAdministrationDate()));
        }

        immunization = immunizationRepository.save(immunization);
        log.info("Created immunization record ID {} for student: {} {}",
                immunization.getId(),
                immunization.getStudent().getFirstName(),
                immunization.getStudent().getLastName());

        return immunization;
    }

    /**
     * Gets an immunization record by ID.
     *
     * @param id the immunization ID
     * @return immunization record
     */
    public Immunization getImmunizationById(Long id) {
        return immunizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Immunization record not found: " + id));
    }

    /**
     * Gets all immunizations for a student.
     *
     * @param student the student
     * @return list of immunization records
     */
    public List<Immunization> getImmunizationsForStudent(Student student) {
        log.debug("Fetching immunizations for student ID {}", student.getId());
        return immunizationRepository.findByStudent(student);
    }

    /**
     * Gets all immunizations for a student by student ID.
     *
     * @param studentId the student ID
     * @return list of immunization records
     */
    public List<Immunization> getImmunizationsByStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return getImmunizationsForStudent(student);
    }

    /**
     * Checks if a student is compliant with all required immunizations.
     *
     * @param studentId the student ID
     * @return true if student is compliant, false otherwise
     */
    public boolean isStudentCompliant(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        ImmunizationComplianceReport report = checkCompliance(student);
        return report.isCompliant();
    }

    /**
     * Gets immunizations by vaccine type for a student.
     *
     * @param student the student
     * @param vaccineType the vaccine type
     * @return list of immunization records
     */
    public List<Immunization> getImmunizationsByType(Student student, VaccineType vaccineType) {
        log.debug("Fetching {} immunizations for student ID {}", vaccineType, student.getId());
        return immunizationRepository.findByStudentAndVaccineType(student, vaccineType);
    }

    /**
     * Updates an immunization record.
     *
     * @param immunization the immunization to update
     * @return updated immunization record
     */
    @Transactional
    public Immunization updateImmunization(Immunization immunization) {
        log.info("Updating immunization record ID {}", immunization.getId());

        immunization.setLastUpdated(LocalDateTime.now());
        immunization = immunizationRepository.save(immunization);

        log.info("Updated immunization record ID {}", immunization.getId());
        return immunization;
    }

    /**
     * Verifies an immunization record.
     *
     * @param immunizationId the immunization ID
     * @param verificationMethod how it was verified
     * @param verifiedByStaffId staff member verifying
     * @return updated immunization record
     */
    @Transactional
    public Immunization verifyImmunization(
            Long immunizationId,
            VerificationMethod verificationMethod,
            Long verifiedByStaffId) {

        log.info("Verifying immunization record ID {}", immunizationId);

        Immunization immunization = getImmunizationById(immunizationId);
        immunization.setVerified(true);
        immunization.setVerificationMethod(verificationMethod);
        immunization.setVerifiedByStaffId(verifiedByStaffId);
        immunization.setVerificationDate(LocalDate.now());

        immunization = immunizationRepository.save(immunization);
        log.info("Immunization record ID {} verified", immunizationId);

        return immunization;
    }

    // ========================================================================
    // EXEMPTION MANAGEMENT
    // ========================================================================

    /**
     * Records a medical exemption for a vaccine.
     *
     * @param student the student
     * @param vaccineType vaccine type being exempted
     * @param exemptionExpirationDate expiration date of exemption
     * @param documentationOnFile whether documentation is on file
     * @param notes exemption notes
     * @param enteredByStaffId staff entering the exemption
     * @return created immunization record with exemption
     */
    @Transactional
    public Immunization recordMedicalExemption(
            Student student,
            VaccineType vaccineType,
            LocalDate exemptionExpirationDate,
            Boolean documentationOnFile,
            String notes,
            Long enteredByStaffId) {

        log.info("Recording medical exemption for student ID {}: {}", student.getId(), vaccineType);

        Immunization immunization = Immunization.builder()
                .student(student)
                .vaccineType(vaccineType)
                .doseNumber(0)
                .administrationDate(LocalDate.now())
                .isMedicalExemption(true)
                .exemptionDocumentationOnFile(documentationOnFile)
                .exemptionExpirationDate(exemptionExpirationDate)
                .notes(notes)
                .enteredByStaffId(enteredByStaffId)
                .entryDate(LocalDateTime.now())
                .verified(true)
                .meetsStateRequirement(false) // Exemption doesn't meet requirement, but is compliant
                .build();

        immunization = immunizationRepository.save(immunization);
        log.info("Medical exemption recorded - Immunization ID {}", immunization.getId());

        return immunization;
    }

    /**
     * Records a religious exemption for a vaccine.
     *
     * @param student the student
     * @param vaccineType vaccine type being exempted
     * @param documentationOnFile whether documentation is on file
     * @param notes exemption notes
     * @param enteredByStaffId staff entering the exemption
     * @return created immunization record with exemption
     */
    @Transactional
    public Immunization recordReligiousExemption(
            Student student,
            VaccineType vaccineType,
            Boolean documentationOnFile,
            String notes,
            Long enteredByStaffId) {

        log.info("Recording religious exemption for student ID {}: {}", student.getId(), vaccineType);

        Immunization immunization = Immunization.builder()
                .student(student)
                .vaccineType(vaccineType)
                .doseNumber(0)
                .administrationDate(LocalDate.now())
                .isReligiousExemption(true)
                .exemptionDocumentationOnFile(documentationOnFile)
                .notes(notes)
                .enteredByStaffId(enteredByStaffId)
                .entryDate(LocalDateTime.now())
                .verified(true)
                .meetsStateRequirement(false)
                .build();

        immunization = immunizationRepository.save(immunization);
        log.info("Religious exemption recorded - Immunization ID {}", immunization.getId());

        return immunization;
    }

    // ========================================================================
    // COMPLIANCE CHECKING
    // ========================================================================

    /**
     * Checks immunization compliance for a student.
     *
     * @param student the student
     * @return compliance report
     */
    public ImmunizationComplianceReport checkCompliance(Student student) {
        log.info("Checking immunization compliance for student ID {}", student.getId());

        List<Immunization> immunizations = getImmunizationsForStudent(student);
        ImmunizationComplianceReport report = new ImmunizationComplianceReport();
        report.student = student;
        report.checkDate = LocalDate.now();

        // Check each required vaccine
        for (VaccineType vaccineType : VaccineType.values()) {
            if (!vaccineType.isRequiredForSchool()) {
                continue;
            }

            VaccineCompliance compliance = checkVaccineCompliance(student, vaccineType, immunizations);
            report.vaccineComplianceMap.put(vaccineType, compliance);

            if (!compliance.isCompliant) {
                report.compliant = false;
                report.missingVaccines.add(vaccineType);
            }
        }

        log.info("Compliance check complete for student ID {}: {}",
                student.getId(), report.compliant ? "COMPLIANT" : "NON-COMPLIANT");

        return report;
    }

    /**
     * Checks compliance for a specific vaccine.
     *
     * @param student the student
     * @param vaccineType the vaccine type
     * @param immunizations student's immunization records
     * @return vaccine compliance status
     */
    private VaccineCompliance checkVaccineCompliance(
            Student student,
            VaccineType vaccineType,
            List<Immunization> immunizations) {

        VaccineCompliance compliance = new VaccineCompliance();
        compliance.vaccineType = vaccineType;

        List<Immunization> vaccineRecords = immunizations.stream()
                .filter(i -> i.getVaccineType() == vaccineType)
                .sorted(Comparator.comparing(Immunization::getDoseNumber))
                .collect(Collectors.toList());

        if (vaccineRecords.isEmpty()) {
            compliance.isCompliant = false;
            compliance.status = "Missing - No doses recorded";
            return compliance;
        }

        // Check for exemptions
        boolean hasExemption = vaccineRecords.stream().anyMatch(Immunization::hasExemption);
        if (hasExemption) {
            compliance.isCompliant = true;
            compliance.status = "Exempt";
            compliance.hasExemption = true;
            return compliance;
        }

        // Check series completion
        int highestDose = vaccineRecords.stream()
                .mapToInt(Immunization::getDoseNumber)
                .max()
                .orElse(0);

        int requiredDoses = vaccineType.getTypicalDosesRequired();

        if (highestDose >= requiredDoses) {
            compliance.isCompliant = true;
            compliance.status = "Complete - " + highestDose + " of " + requiredDoses + " doses";
            compliance.dosesReceived = highestDose;
            compliance.dosesRequired = requiredDoses;
        } else {
            compliance.isCompliant = false;
            compliance.status = "Incomplete - " + highestDose + " of " + requiredDoses + " doses";
            compliance.dosesReceived = highestDose;
            compliance.dosesRequired = requiredDoses;

            // Find next dose due date
            compliance.nextDoseDue = vaccineRecords.stream()
                    .filter(i -> i.getNextDoseDueDate() != null)
                    .map(Immunization::getNextDoseDueDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);
        }

        return compliance;
    }

    /**
     * Gets students non-compliant with immunization requirements.
     *
     * @return list of non-compliant students
     */
    public List<Student> getNonCompliantStudents() {
        log.debug("Fetching non-compliant students");

        List<Student> nonCompliantStudents = new ArrayList<>();

        for (VaccineType vaccineType : VaccineType.values()) {
            if (vaccineType.isRequiredForSchool()) {
                List<Student> students = immunizationRepository
                        .findStudentsNonCompliantForVaccine(vaccineType);
                nonCompliantStudents.addAll(students);
            }
        }

        // Remove duplicates
        return nonCompliantStudents.stream().distinct().collect(Collectors.toList());
    }

    // ========================================================================
    // DUE DATE MANAGEMENT
    // ========================================================================

    /**
     * Gets overdue immunizations.
     *
     * @return list of overdue immunization records
     */
    public List<Immunization> getOverdueImmunizations() {
        log.debug("Fetching overdue immunizations");
        return immunizationRepository.findOverdueImmunizations();
    }

    /**
     * Gets immunizations due soon (within 30 days).
     *
     * @return list of immunizations due soon
     */
    public List<Immunization> getImmunizationsDueSoon() {
        log.debug("Fetching immunizations due soon");
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return immunizationRepository.findImmunizationsDueSoon(thirtyDaysFromNow);
    }

    /**
     * Gets incomplete immunization series.
     *
     * @return list of immunizations needing additional doses
     */
    public List<Immunization> getIncompleteImmunizationSeries() {
        log.debug("Fetching incomplete immunization series");
        return immunizationRepository.findIncompleteImmunizationSeries();
    }

    /**
     * Calculates next dose due date based on vaccine type and current dose.
     *
     * @param vaccineType the vaccine type
     * @param currentDose current dose number
     * @param lastDoseDate date of last dose
     * @return calculated next dose date
     */
    private LocalDate calculateNextDoseDate(
            VaccineType vaccineType,
            Integer currentDose,
            LocalDate lastDoseDate) {

        // Standard intervals (simplified - real implementation should follow CDC schedule)
        return switch (vaccineType) {
            case DTaP, POLIO, HEPATITIS_B, HIB, PNEUMOCOCCAL -> {
                if (currentDose == 1) yield lastDoseDate.plusMonths(2);
                if (currentDose == 2) yield lastDoseDate.plusMonths(2);
                if (currentDose == 3) yield lastDoseDate.plusMonths(6);
                yield lastDoseDate.plusYears(1);
            }
            case MMR, VARICELLA, HEPATITIS_A -> lastDoseDate.plusMonths(6);
            case MENINGOCOCCAL -> lastDoseDate.plusYears(3);
            case HPV -> {
                if (currentDose == 1) yield lastDoseDate.plusMonths(2);
                yield lastDoseDate.plusMonths(4);
            }
            default -> lastDoseDate.plusMonths(3);
        };
    }

    // ========================================================================
    // REPORTING
    // ========================================================================

    /**
     * Gets students with exemptions.
     *
     * @return list of students with any exemption
     */
    public List<Student> getStudentsWithExemptions() {
        log.debug("Fetching students with immunization exemptions");
        return immunizationRepository.findStudentsWithExemptions();
    }

    /**
     * Gets total count of immunization records.
     *
     * @return count
     */
    public long getImmunizationCount() {
        return immunizationRepository.count();
    }

    // ========================================================================
    // DATA TRANSFER OBJECTS (DTOs)
    // ========================================================================

    /**
     * Immunization compliance report for a student.
     */
    public static class ImmunizationComplianceReport {
        public Student student;
        public LocalDate checkDate;
        public boolean compliant = true;
        public Map<VaccineType, VaccineCompliance> vaccineComplianceMap = new HashMap<>();
        public List<VaccineType> missingVaccines = new ArrayList<>();

        public Student getStudent() { return student; }
        public LocalDate getCheckDate() { return checkDate; }
        public boolean isCompliant() { return compliant; }
        public Map<VaccineType, VaccineCompliance> getVaccineComplianceMap() { return vaccineComplianceMap; }
        public List<VaccineType> getMissingVaccines() { return missingVaccines; }
    }

    /**
     * Compliance status for a specific vaccine.
     */
    public static class VaccineCompliance {
        public VaccineType vaccineType;
        public boolean isCompliant;
        public String status;
        public boolean hasExemption;
        public int dosesReceived;
        public int dosesRequired;
        public LocalDate nextDoseDue;

        public VaccineType getVaccineType() { return vaccineType; }
        public boolean isCompliant() { return isCompliant; }
        public String getStatus() { return status; }
        public boolean hasExemption() { return hasExemption; }
        public int getDosesReceived() { return dosesReceived; }
        public int getDosesRequired() { return dosesRequired; }
        public LocalDate getNextDoseDue() { return nextDoseDue; }
    }
}
