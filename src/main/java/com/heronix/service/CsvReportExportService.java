package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CSV Report Export Service
 *
 * Provides CSV export functionality for attendance reports.
 *
 * Features:
 * - Daily attendance reports in CSV format
 * - Student attendance summary in CSV format
 * - Chronic absenteeism reports in CSV format
 * - RFC 4180 compliant CSV formatting
 * - UTF-8 encoding with BOM for Excel compatibility
 * - Proper escaping of special characters
 *
 * CSV Format:
 * - Comma-separated values
 * - Double-quoted fields containing commas, quotes, or newlines
 * - Escaped quotes (double quotes)
 * - Header row with column names
 * - UTF-8 encoding with BOM (Byte Order Mark)
 *
 * Benefits:
 * - Universal compatibility (Excel, Google Sheets, databases)
 * - Lightweight and fast
 * - Easy to parse programmatically
 * - Human-readable in text editors
 * - Smaller file sizes than Excel/PDF
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 58 - Report Export Formats & CSV Support
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CsvReportExportService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\r\n";
    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    /**
     * Export daily attendance report as CSV
     *
     * @param date Report date
     * @return CSV file as byte array
     * @throws IOException If export fails
     */
    public byte[] exportDailyAttendanceCsv(LocalDate date) throws IOException {
        log.info("Generating daily attendance CSV report for {}", date);

        List<AttendanceRecord> records = attendanceRepository.findByAttendanceDate(date);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Write UTF-8 BOM for Excel compatibility
        baos.write(UTF8_BOM);

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // Write header
            writeCsvLine(writer,
                "Student ID",
                "Student Name",
                "Grade",
                "Date",
                "Status",
                "Time In",
                "Time Out",
                "Notes"
            );

            // Write data rows
            for (AttendanceRecord record : records) {
                String studentName = record.getStudent() != null ?
                    record.getStudent().getFullName() : "Unknown";

                String grade = record.getStudent() != null && record.getStudent().getGradeLevel() != null ?
                    record.getStudent().getGradeLevel().toString() : "N/A";

                String studentId = record.getStudent() != null && record.getStudent().getId() != null ?
                    record.getStudent().getId().toString() : "";

                writeCsvLine(writer,
                    studentId,
                    studentName,
                    grade,
                    record.getAttendanceDate().format(DATE_FORMATTER),
                    record.getStatus() != null ? record.getStatus().toString() : "UNKNOWN",
                    record.getCreatedAt() != null ? record.getCreatedAt().format(DATETIME_FORMATTER) : "",
                    "",  // Time Out not available in current model
                    record.getNotes() != null ? record.getNotes() : ""
                );
            }

            writer.flush();
        }

        log.info("Daily attendance CSV generated: {} records", records.size());
        return baos.toByteArray();
    }

    /**
     * Export student attendance summary as CSV
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @return CSV file as byte array
     * @throws IOException If export fails
     */
    public byte[] exportStudentAttendanceSummaryCsv(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating student attendance summary CSV for {} to {}", startDate, endDate);

        // Get all students and calculate their attendance
        List<com.heronix.model.domain.Student> students = studentRepository.findAll();
        Map<Long, StudentAttendanceSummary> summaryMap = students.stream()
            .collect(Collectors.toMap(
                com.heronix.model.domain.Student::getId,
                student -> {
                    List<AttendanceRecord> studentRecords = attendanceRepository
                        .findByStudentAndDateBetween(student, startDate, endDate);
                    return calculateSummary(student, studentRecords);
                }
            ));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(UTF8_BOM);

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // Write header
            writeCsvLine(writer,
                "Student ID",
                "Student Name",
                "Grade",
                "Total Days",
                "Present Days",
                "Absent Days",
                "Tardy Days",
                "Excused Absences",
                "Unexcused Absences",
                "Attendance Rate (%)",
                "Period Start",
                "Period End"
            );

            // Write data rows
            for (StudentAttendanceSummary summary : summaryMap.values()) {
                writeCsvLine(writer,
                    summary.studentId,
                    summary.studentName,
                    summary.grade,
                    String.valueOf(summary.totalDays),
                    String.valueOf(summary.presentDays),
                    String.valueOf(summary.absentDays),
                    String.valueOf(summary.tardyDays),
                    String.valueOf(summary.excusedAbsences),
                    String.valueOf(summary.unexcusedAbsences),
                    String.format("%.2f", summary.attendanceRate),
                    startDate.format(DATE_FORMATTER),
                    endDate.format(DATE_FORMATTER)
                );
            }

            writer.flush();
        }

        log.info("Student summary CSV generated: {} students", summaryMap.size());
        return baos.toByteArray();
    }

    /**
     * Export chronic absenteeism report as CSV
     *
     * @param startDate Period start date
     * @param endDate Period end date
     * @param threshold Absenteeism threshold percentage
     * @return CSV file as byte array
     * @throws IOException If export fails
     */
    public byte[] exportChronicAbsenteeismCsv(LocalDate startDate, LocalDate endDate, double threshold) throws IOException {
        log.info("Generating chronic absenteeism CSV for {} to {} (threshold: {}%)", startDate, endDate, threshold);

        // Get all students and calculate their attendance
        List<com.heronix.model.domain.Student> students = studentRepository.findAll();
        Map<Long, StudentAttendanceSummary> summaryMap = students.stream()
            .collect(Collectors.toMap(
                com.heronix.model.domain.Student::getId,
                student -> {
                    List<AttendanceRecord> studentRecords = attendanceRepository
                        .findByStudentAndDateBetween(student, startDate, endDate);
                    return calculateSummary(student, studentRecords);
                }
            ));

        // Filter students below threshold
        List<StudentAttendanceSummary> chronicStudents = summaryMap.values().stream()
            .filter(s -> s.attendanceRate < threshold)
            .sorted((a, b) -> Double.compare(a.attendanceRate, b.attendanceRate))
            .toList();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(UTF8_BOM);

        try (Writer writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            // Write header
            writeCsvLine(writer,
                "Student ID",
                "Student Name",
                "Grade",
                "Attendance Rate (%)",
                "Total Days",
                "Absent Days",
                "Unexcused Absences",
                "Days Below Threshold",
                "Risk Level",
                "Period Start",
                "Period End"
            );

            // Write data rows
            for (StudentAttendanceSummary summary : chronicStudents) {
                double gapFromThreshold = threshold - summary.attendanceRate;
                String riskLevel = getRiskLevel(summary.attendanceRate, threshold);

                writeCsvLine(writer,
                    summary.studentId,
                    summary.studentName,
                    summary.grade,
                    String.format("%.2f", summary.attendanceRate),
                    String.valueOf(summary.totalDays),
                    String.valueOf(summary.absentDays),
                    String.valueOf(summary.unexcusedAbsences),
                    String.format("%.1f", gapFromThreshold),
                    riskLevel,
                    startDate.format(DATE_FORMATTER),
                    endDate.format(DATE_FORMATTER)
                );
            }

            writer.flush();
        }

        log.info("Chronic absenteeism CSV generated: {} students below threshold", chronicStudents.size());
        return baos.toByteArray();
    }

    /**
     * Write a CSV line with proper escaping
     *
     * @param writer Writer to write to
     * @param fields Fields to write
     * @throws IOException If write fails
     */
    private void writeCsvLine(Writer writer, String... fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(CSV_SEPARATOR);
            }
            writer.write(escapeCsvField(fields[i]));
        }
        writer.write(LINE_SEPARATOR);
    }

    /**
     * Escape CSV field according to RFC 4180
     *
     * @param field Field to escape
     * @return Escaped field
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }

        // Check if field needs quoting
        boolean needsQuoting = field.contains(CSV_SEPARATOR) ||
                               field.contains("\"") ||
                               field.contains("\n") ||
                               field.contains("\r");

        if (!needsQuoting) {
            return field;
        }

        // Escape quotes by doubling them and wrap in quotes
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    /**
     * Calculate attendance summary for a student
     */
    private StudentAttendanceSummary calculateSummary(com.heronix.model.domain.Student student, List<AttendanceRecord> records) {
        StudentAttendanceSummary summary = new StudentAttendanceSummary();

        summary.studentId = student.getId() != null ? student.getId().toString() : "";
        summary.studentName = student.getFullName();
        summary.grade = student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A";

        summary.totalDays = records.size();
        summary.presentDays = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT)
            .count();
        summary.absentDays = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
            .count();
        summary.tardyDays = (int) records.stream()
            .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.TARDY)
            .count();

        // All absences are counted as unexcused in current model
        summary.excusedAbsences = 0;
        summary.unexcusedAbsences = summary.absentDays;

        summary.attendanceRate = summary.totalDays > 0 ?
            (summary.presentDays * 100.0 / summary.totalDays) : 0.0;

        return summary;
    }

    /**
     * Determine risk level based on attendance rate
     */
    private String getRiskLevel(double attendanceRate, double threshold) {
        double gap = threshold - attendanceRate;

        if (gap >= 20) {
            return "CRITICAL";
        } else if (gap >= 10) {
            return "HIGH";
        } else if (gap >= 5) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }

    /**
     * Student attendance summary data class
     */
    private static class StudentAttendanceSummary {
        String studentId = "";
        String studentName = "";
        String grade = "";
        int totalDays = 0;
        int presentDays = 0;
        int absentDays = 0;
        int tardyDays = 0;
        int excusedAbsences = 0;
        int unexcusedAbsences = 0;
        double attendanceRate = 0.0;
    }
}
