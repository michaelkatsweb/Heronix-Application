package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attendance Document Service
 *
 * Generates attendance-related documents including:
 * - Truancy warning letters
 * - Attendance improvement plan documents
 * - Perfect attendance certificates
 * - Attendance contracts
 * - State attendance reports
 * - Attendance summaries for transcripts
 *
 * @author Heronix Scheduling System Team
 * @version 1.0.0
 * @since Phase 2 - Attendance Enhancement
 */
@Slf4j
@Service
public class AttendanceDocumentService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AttendanceReportingService reportingService;

    // ========================================================================
    // TRUANCY LETTERS
    // ========================================================================

    /**
     * Generate truancy warning letter (1st notice)
     */
    public TruancyLetter generateTruancyWarningLetter(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating truancy warning letter for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        long unexcusedAbsences = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.UNEXCUSED_ABSENT)
                .count();

        long totalAbsences = records.stream()
                .filter(AttendanceRecord::isAbsent)
                .count();

        String letterContent = buildTruancyWarningLetterContent(student, unexcusedAbsences, totalAbsences, startDate, endDate);

        return TruancyLetter.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .letterType("TRUANCY_WARNING_1ST")
                .unexcusedAbsences((int) unexcusedAbsences)
                .totalAbsences((int) totalAbsences)
                .startDate(startDate)
                .endDate(endDate)
                .letterContent(letterContent)
                .generatedDate(LocalDate.now())
                .requiresSignature(true)
                .severity("WARNING")
                .build();
    }

    /**
     * Generate truancy final notice (before court referral)
     */
    public TruancyLetter generateTruancyFinalNotice(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating truancy final notice for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        long unexcusedAbsences = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.UNEXCUSED_ABSENT)
                .count();

        String letterContent = buildTruancyFinalNoticeContent(student, unexcusedAbsences, startDate, endDate);

        return TruancyLetter.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .letterType("TRUANCY_FINAL_NOTICE")
                .unexcusedAbsences((int) unexcusedAbsences)
                .startDate(startDate)
                .endDate(endDate)
                .letterContent(letterContent)
                .generatedDate(LocalDate.now())
                .requiresSignature(true)
                .severity("CRITICAL")
                .legalConsequences(true)
                .build();
    }

    // ========================================================================
    // ATTENDANCE IMPROVEMENT PLANS
    // ========================================================================

    /**
     * Generate attendance improvement plan document
     */
    public AttendanceImprovementPlan generateImprovementPlan(Long studentId, int targetAttendanceRate, int durationDays) {
        log.info("Generating attendance improvement plan for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(durationDays);

        String planContent = buildImprovementPlanContent(student, targetAttendanceRate, startDate, endDate);

        return AttendanceImprovementPlan.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .gradeLevel(student.getGradeLevel())
                .startDate(startDate)
                .endDate(endDate)
                .targetAttendanceRate(targetAttendanceRate)
                .currentAttendanceRate(calculateCurrentAttendanceRate(studentId))
                .planContent(planContent)
                .interventions(buildDefaultInterventions())
                .goals(buildAttendanceGoals(targetAttendanceRate))
                .createdDate(LocalDate.now())
                .status("ACTIVE")
                .requiresWeeklyReview(true)
                .build();
    }

    // ========================================================================
    // ATTENDANCE CONTRACTS
    // ========================================================================

    /**
     * Generate attendance contract (student/parent agreement)
     */
    public AttendanceContract generateAttendanceContract(Long studentId, int durationDays) {
        log.info("Generating attendance contract for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(durationDays);

        String contractContent = buildAttendanceContractContent(student, startDate, endDate);

        return AttendanceContract.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .gradeLevel(student.getGradeLevel())
                .startDate(startDate)
                .endDate(endDate)
                .contractContent(contractContent)
                .studentCommitments(buildStudentCommitments())
                .parentCommitments(buildParentCommitments())
                .schoolCommitments(buildSchoolCommitments())
                .consequences(buildContractConsequences())
                .createdDate(LocalDate.now())
                .requiresStudentSignature(true)
                .requiresParentSignature(true)
                .requiresAdminSignature(true)
                .status("PENDING_SIGNATURE")
                .build();
    }

    // ========================================================================
    // PERFECT ATTENDANCE CERTIFICATES
    // ========================================================================

    /**
     * Generate perfect attendance certificate
     */
    public AttendanceCertificate generatePerfectAttendanceCertificate(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating perfect attendance certificate for student {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        boolean isPerfect = records.stream()
                .allMatch(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT);

        if (!isPerfect) {
            throw new IllegalArgumentException("Student does not have perfect attendance for this period");
        }

        String certificateContent = buildPerfectAttendanceCertificateContent(student, startDate, endDate, records.size());

        return AttendanceCertificate.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .gradeLevel(student.getGradeLevel())
                .certificateType("PERFECT_ATTENDANCE")
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(records.size())
                .certificateContent(certificateContent)
                .issuedDate(LocalDate.now())
                .signedBy("Principal")
                .build();
    }

    /**
     * Identify students eligible for perfect attendance awards
     */
    public List<Student> identifyPerfectAttendanceStudents(LocalDate startDate, LocalDate endDate) {
        log.info("Identifying perfect attendance students from {} to {}", startDate, endDate);

        List<Student> allStudents = studentRepository.findAllActive();
        List<Student> perfectAttendance = new ArrayList<>();

        for (Student student : allStudents) {
            List<AttendanceRecord> records = attendanceRepository
                    .findByStudentIdAndAttendanceDateBetween(student.getId(), startDate, endDate);

            boolean isPerfect = !records.isEmpty() && records.stream()
                    .allMatch(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT);

            if (isPerfect) {
                perfectAttendance.add(student);
            }
        }

        log.info("Found {} students with perfect attendance", perfectAttendance.size());
        return perfectAttendance;
    }

    // ========================================================================
    // STATE REPORTING DOCUMENTS
    // ========================================================================

    /**
     * Generate state attendance report (formatted for state submission)
     */
    public StateAttendanceReport generateStateReport(Long schoolId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating state attendance report for school {} from {} to {}", schoolId, startDate, endDate);

        AttendanceReportingService.ADACalculation ada = reportingService.calculateADA(schoolId, startDate, endDate);
        AttendanceReportingService.ADMCalculation adm = reportingService.calculateADM(schoolId, startDate, endDate);

        // Calculate additional state metrics
        List<AttendanceRecord> allRecords = attendanceRepository.findAll().stream()
                .filter(r -> !r.getAttendanceDate().isBefore(startDate) && !r.getAttendanceDate().isAfter(endDate))
                .filter(r -> schoolId == null || (r.getCampus() != null && r.getCampus().getId().equals(schoolId)))
                .collect(Collectors.toList());

        long chronicAbsentStudents = countChronicAbsentStudents(schoolId, startDate, endDate);
        double chronicAbsentRate = adm.getTotalStudents() > 0 ?
                (double) chronicAbsentStudents / adm.getTotalStudents() * 100 : 0.0;

        return StateAttendanceReport.builder()
                .schoolId(schoolId)
                .reportingPeriodStart(startDate)
                .reportingPeriodEnd(endDate)
                .ada(ada.getAda())
                .adm(adm.getAdm())
                .totalEnrollment(adm.getTotalStudents())
                .totalDaysInPeriod(ada.getTotalDaysInPeriod())
                .totalDaysPresent(ada.getTotalDaysPresent())
                .totalDaysAbsent(ada.getTotalDaysAbsent())
                .attendanceRate(ada.getAttendanceRate())
                .chronicAbsentCount((int) chronicAbsentStudents)
                .chronicAbsentRate(chronicAbsentRate)
                .generatedDate(LocalDate.now())
                .certifiedBy("District Attendance Officer")
                .reportFormat("STATE_STANDARD_FORMAT")
                .build();
    }

    // ========================================================================
    // ATTENDANCE SUMMARIES
    // ========================================================================

    /**
     * Generate attendance summary for transcript
     */
    public AttendanceSummary generateTranscriptSummary(Long studentId, String academicYear) {
        log.info("Generating attendance summary for student {} for year {}", studentId, academicYear);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        // Parse academic year (e.g., "2024-2025")
        String[] years = academicYear.split("-");
        LocalDate startDate = LocalDate.of(Integer.parseInt(years[0]), 8, 1);
        LocalDate endDate = LocalDate.of(Integer.parseInt(years[1]), 6, 30);

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        long presentDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                .count();
        long absentDays = records.stream().filter(AttendanceRecord::isAbsent).count();
        long tardyDays = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.TARDY)
                .count();

        int totalSchoolDays = calculateSchoolDays(startDate, endDate);
        double attendanceRate = totalSchoolDays > 0 ? (double) presentDays / totalSchoolDays * 100 : 0.0;

        return AttendanceSummary.builder()
                .studentId(studentId)
                .studentName(student.getFullName())
                .academicYear(academicYear)
                .totalSchoolDays(totalSchoolDays)
                .daysPresent((int) presentDays)
                .daysAbsent((int) absentDays)
                .daysTardy((int) tardyDays)
                .attendanceRate(attendanceRate)
                .isPerfectAttendance(absentDays == 0 && tardyDays == 0)
                .build();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String buildTruancyWarningLetterContent(Student student, long unexcusedAbsences, long totalAbsences, LocalDate startDate, LocalDate endDate) {
        return String.format(
                "TRUANCY WARNING LETTER\n\n" +
                "Date: %s\n\n" +
                "RE: %s - Student ID: %s - Grade %s\n\n" +
                "Dear Parent/Guardian,\n\n" +
                "This letter is to inform you that %s has accumulated %d unexcused absences " +
                "(%d total absences) between %s and %s.\n\n" +
                "According to state law, students are required to attend school regularly. " +
                "Excessive unexcused absences may result in:\n" +
                "• Loss of academic credit\n" +
                "• Retention in current grade level\n" +
                "• Referral to truancy court\n" +
                "• Potential legal action against parents/guardians\n\n" +
                "REQUIRED ACTION:\n" +
                "You must contact the school within 5 school days to:\n" +
                "1. Explain the reasons for the absences\n" +
                "2. Provide documentation for excused absences\n" +
                "3. Develop an attendance improvement plan\n\n" +
                "Please contact the Attendance Office at [Phone] or [Email].\n\n" +
                "Sincerely,\n\n" +
                "[Principal Name]\n" +
                "Principal\n\n" +
                "CC: Attendance Office, Student File",
                LocalDate.now(),
                student.getFullName(),
                student.getStudentId(),
                student.getGradeLevel(),
                student.getFirstName(),
                unexcusedAbsences,
                totalAbsences,
                startDate,
                endDate
        );
    }

    private String buildTruancyFinalNoticeContent(Student student, long unexcusedAbsences, LocalDate startDate, LocalDate endDate) {
        return String.format(
                "FINAL TRUANCY NOTICE - LEGAL ACTION PENDING\n\n" +
                "Date: %s\n\n" +
                "RE: %s - Student ID: %s - Grade %s\n\n" +
                "Dear Parent/Guardian,\n\n" +
                "NOTICE OF POTENTIAL COURT REFERRAL\n\n" +
                "Despite previous warnings, %s has accumulated %d unexcused absences " +
                "between %s and %s, in violation of state attendance laws.\n\n" +
                "IMMEDIATE LEGAL CONSEQUENCES:\n" +
                "This is your final notice before the following actions are taken:\n" +
                "1. Referral to Juvenile Court for truancy proceedings\n" +
                "2. Potential fines up to $500 per violation\n" +
                "3. Mandatory court-ordered attendance monitoring\n" +
                "4. Possible criminal charges for contributing to truancy\n" +
                "5. Involvement of Child Protective Services\n\n" +
                "MANDATORY MEETING:\n" +
                "You are REQUIRED to attend a meeting with school administration within 3 school days.\n" +
                "Failure to attend will result in immediate court referral.\n\n" +
                "Contact the Principal's Office IMMEDIATELY at [Phone].\n\n" +
                "This is a serious legal matter. Do not ignore this notice.\n\n" +
                "Sincerely,\n\n" +
                "[Principal Name]\n" +
                "Principal\n\n" +
                "CC: Truancy Officer, District Attorney, Student File",
                LocalDate.now(),
                student.getFullName(),
                student.getStudentId(),
                student.getGradeLevel(),
                student.getFirstName(),
                unexcusedAbsences,
                startDate,
                endDate
        );
    }

    private String buildImprovementPlanContent(Student student, int targetRate, LocalDate startDate, LocalDate endDate) {
        return String.format(
                "ATTENDANCE IMPROVEMENT PLAN\n\n" +
                "Student: %s\n" +
                "Grade: %s\n" +
                "Start Date: %s\n" +
                "End Date: %s\n\n" +
                "CURRENT STATUS:\n" +
                "Current Attendance Rate: %.1f%%\n" +
                "Target Attendance Rate: %d%%\n\n" +
                "GOALS:\n" +
                "1. Achieve %d%% attendance rate by %s\n" +
                "2. Reduce unexcused absences to zero\n" +
                "3. Improve on-time arrival to 95%%\n\n" +
                "INTERVENTIONS:\n" +
                "1. Daily attendance monitoring\n" +
                "2. Weekly parent contact\n" +
                "3. Incentive program for perfect weeks\n" +
                "4. Counselor support for barriers\n" +
                "5. Peer mentor assignment\n\n" +
                "PROGRESS REVIEWS:\n" +
                "Weekly reviews every Friday\n" +
                "Plan revision as needed\n\n" +
                "Signatures:\n" +
                "Student: __________________ Date: ______\n" +
                "Parent: ___________________ Date: ______\n" +
                "Counselor: ________________ Date: ______\n" +
                "Principal: ________________ Date: ______",
                student.getFullName(),
                student.getGradeLevel(),
                startDate,
                endDate,
                calculateCurrentAttendanceRate(student.getId()),
                targetRate,
                targetRate,
                endDate
        );
    }

    private String buildAttendanceContractContent(Student student, LocalDate startDate, LocalDate endDate) {
        return String.format(
                "ATTENDANCE CONTRACT\n\n" +
                "This contract is entered into on %s between:\n" +
                "Student: %s (Grade %s)\n" +
                "Parent/Guardian: ___________________\n" +
                "School: [School Name]\n\n" +
                "CONTRACT PERIOD: %s to %s\n\n" +
                "STUDENT AGREES TO:\n" +
                "• Attend school every day unless ill or emergency\n" +
                "• Arrive on time before the tardy bell\n" +
                "• Notify school of absences in advance when possible\n" +
                "• Make up all missed work within required timeframes\n" +
                "• Meet with counselor weekly to review attendance\n\n" +
                "PARENT/GUARDIAN AGREES TO:\n" +
                "• Ensure child arrives to school on time daily\n" +
                "• Contact school by 9:00 AM for any absence\n" +
                "• Provide documentation for excused absences\n" +
                "• Attend monthly attendance review meetings\n" +
                "• Support school interventions and consequences\n\n" +
                "SCHOOL AGREES TO:\n" +
                "• Provide daily attendance monitoring\n" +
                "• Contact parent immediately for absences\n" +
                "• Offer counseling and support services\n" +
                "• Recognize and reward improved attendance\n" +
                "• Provide make-up work opportunities\n\n" +
                "CONSEQUENCES FOR NON-COMPLIANCE:\n" +
                "• Loss of school privileges\n" +
                "• Mandatory after-school attendance recovery\n" +
                "• Referral to truancy court\n" +
                "• Academic credit reduction\n\n" +
                "Signatures:\n" +
                "Student: __________________ Date: ______\n" +
                "Parent: ___________________ Date: ______\n" +
                "Principal: ________________ Date: ______",
                LocalDate.now(),
                student.getFullName(),
                student.getGradeLevel(),
                startDate,
                endDate
        );
    }

    private String buildPerfectAttendanceCertificateContent(Student student, LocalDate startDate, LocalDate endDate, int totalDays) {
        return String.format(
                "═══════════════════════════════════════\n" +
                "     PERFECT ATTENDANCE CERTIFICATE\n" +
                "═══════════════════════════════════════\n\n" +
                "This certificate is proudly presented to\n\n" +
                "         %s\n\n" +
                "In recognition of PERFECT ATTENDANCE\n" +
                "for the period from %s to %s\n\n" +
                "%d Consecutive Days Present\n" +
                "No Absences • No Tardies\n\n" +
                "Your dedication to regular school attendance\n" +
                "demonstrates commitment to academic excellence.\n\n" +
                "Congratulations on this outstanding achievement!\n\n\n" +
                "Date Issued: %s\n\n" +
                "_______________________\n" +
                "Principal Signature\n\n" +
                "[School Seal]",
                student.getFullName().toUpperCase(),
                startDate,
                endDate,
                totalDays,
                LocalDate.now()
        );
    }

    private double calculateCurrentAttendanceRate(Long studentId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<AttendanceRecord> records = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        if (records.isEmpty()) return 0.0;

        long presentCount = records.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
                .count();

        return (double) presentCount / records.size() * 100;
    }

    private long countChronicAbsentStudents(Long schoolId, LocalDate startDate, LocalDate endDate) {
        List<Student> students = studentRepository.findAllActive();
        int schoolDays = calculateSchoolDays(startDate, endDate);
        double chronicThreshold = schoolDays * 0.10; // 10% absence rate

        return students.stream()
                .filter(s -> {
                    List<AttendanceRecord> records = attendanceRepository
                            .findByStudentIdAndAttendanceDateBetween(s.getId(), startDate, endDate);
                    long absences = records.stream().filter(AttendanceRecord::isAbsent).count();
                    return absences >= chronicThreshold;
                })
                .count();
    }

    private List<String> buildDefaultInterventions() {
        return Arrays.asList(
                "Daily attendance check-in with counselor",
                "Weekly parent communication",
                "Peer mentor support",
                "Incentive program for perfect weeks",
                "Transportation assistance if needed",
                "Academic tutoring to catch up on missed work"
        );
    }

    private List<String> buildAttendanceGoals(int targetRate) {
        return Arrays.asList(
                String.format("Achieve %d%% attendance rate", targetRate),
                "Zero unexcused absences",
                "95% on-time arrival rate",
                "Complete all missed assignments within 48 hours"
        );
    }

    private List<String> buildStudentCommitments() {
        return Arrays.asList(
                "Attend school every day unless ill",
                "Arrive on time before tardy bell",
                "Notify school in advance of planned absences",
                "Complete all makeup work promptly"
        );
    }

    private List<String> buildParentCommitments() {
        return Arrays.asList(
                "Ensure child arrives to school on time",
                "Contact school by 9 AM for absences",
                "Provide documentation for excused absences",
                "Attend monthly review meetings"
        );
    }

    private List<String> buildSchoolCommitments() {
        return Arrays.asList(
                "Daily attendance monitoring",
                "Immediate parent contact for absences",
                "Counseling and support services",
                "Recognition for improved attendance"
        );
    }

    private int calculateSchoolDays(LocalDate startDate, LocalDate endDate) {
        // Simplified: exclude weekends
        // In production, would also exclude holidays from school calendar
        int days = 0;
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek().getValue() < 6) { // Monday-Friday
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    private List<String> buildContractConsequences() {
        return Arrays.asList(
                "Loss of extracurricular privileges",
                "Mandatory attendance recovery sessions",
                "Referral to truancy court",
                "Academic credit reduction"
        );
    }

    // ========================================================================
    // DTO CLASSES
    // ========================================================================

    @Data
    @Builder
    public static class TruancyLetter {
        private Long studentId;
        private String studentName;
        private String letterType;
        private Integer unexcusedAbsences;
        private Integer totalAbsences;
        private LocalDate startDate;
        private LocalDate endDate;
        private String letterContent;
        private LocalDate generatedDate;
        private Boolean requiresSignature;
        private String severity;
        private Boolean legalConsequences;
    }

    @Data
    @Builder
    public static class AttendanceImprovementPlan {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer targetAttendanceRate;
        private Double currentAttendanceRate;
        private String planContent;
        private List<String> interventions;
        private List<String> goals;
        private LocalDate createdDate;
        private String status;
        private Boolean requiresWeeklyReview;
    }

    @Data
    @Builder
    public static class AttendanceContract {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private LocalDate startDate;
        private LocalDate endDate;
        private String contractContent;
        private List<String> studentCommitments;
        private List<String> parentCommitments;
        private List<String> schoolCommitments;
        private List<String> consequences;
        private LocalDate createdDate;
        private Boolean requiresStudentSignature;
        private Boolean requiresParentSignature;
        private Boolean requiresAdminSignature;
        private String status;
    }

    @Data
    @Builder
    public static class AttendanceCertificate {
        private Long studentId;
        private String studentName;
        private String gradeLevel;
        private String certificateType;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer totalDays;
        private String certificateContent;
        private LocalDate issuedDate;
        private String signedBy;
    }

    @Data
    @Builder
    public static class StateAttendanceReport {
        private Long schoolId;
        private LocalDate reportingPeriodStart;
        private LocalDate reportingPeriodEnd;
        private Double ada;
        private Double adm;
        private Integer totalEnrollment;
        private Integer totalDaysInPeriod;
        private Double totalDaysPresent;
        private Double totalDaysAbsent;
        private Double attendanceRate;
        private Integer chronicAbsentCount;
        private Double chronicAbsentRate;
        private LocalDate generatedDate;
        private String certifiedBy;
        private String reportFormat;
    }

    @Data
    @Builder
    public static class AttendanceSummary {
        private Long studentId;
        private String studentName;
        private String academicYear;
        private Integer totalSchoolDays;
        private Integer daysPresent;
        private Integer daysAbsent;
        private Integer daysTardy;
        private Double attendanceRate;
        private Boolean isPerfectAttendance;
    }
}
