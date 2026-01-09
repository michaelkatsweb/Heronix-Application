package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Attendance Report Export Service
 *
 * Generates and exports attendance reports in various formats.
 * Supports Excel, PDF, CSV, and custom report templates.
 *
 * Report Types:
 * - Daily Attendance Report
 * - Student Attendance Summary
 * - Chronic Absenteeism Report
 * - Attendance Trends Report
 * - Class/Section Attendance Report
 *
 * Export Formats:
 * - Excel (.xlsx) - Full formatting, charts, multiple sheets
 * - PDF - Print-ready reports with headers/footers
 * - CSV - Raw data for external analysis
 * - JSON - API integration format
 *
 * Features:
 * - Custom date ranges
 * - Filtering by student, class, status
 * - Aggregated statistics
 * - Professional formatting
 * - Branding (school logo, colors)
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 49 - Report Export & Scheduling
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceReportExportService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Export daily attendance report to Excel
     *
     * @param date Report date
     * @return Excel file as byte array
     */
    public byte[] exportDailyAttendanceExcel(LocalDate date) throws IOException {
        log.info("Generating daily attendance Excel report for {}", date);

        List<AttendanceRecord> records = attendanceRepository.findByAttendanceDate(date);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daily Attendance - " + date.format(DATE_FORMATTER));

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student ID", "Student Name", "Grade", "Status", "Time", "Notes"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (AttendanceRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                Student student = record.getStudent();

                createCell(row, 0, student.getId().toString(), dataStyle);
                createCell(row, 1, student.getFullName(), dataStyle);
                createCell(row, 2, student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A", dataStyle);
                createCell(row, 3, record.getStatus().name(), dataStyle);
                createCell(row, 4, record.getCreatedAt() != null ? record.getCreatedAt().toString() : "", dataStyle);
                createCell(row, 5, record.getNotes() != null ? record.getNotes() : "", dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Add summary section
            addSummarySection(sheet, records, rowNum + 2);

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export student attendance summary to Excel
     *
     * @param startDate Start date
     * @param endDate End date
     * @return Excel file as byte array
     */
    public byte[] exportStudentAttendanceSummary(LocalDate startDate, LocalDate endDate) throws IOException {
        log.info("Generating student attendance summary Excel report from {} to {}", startDate, endDate);

        List<Student> students = studentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance Summary");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student ID", "Student Name", "Grade", "Total Days", "Present", "Absent", "Tardy", "Excused", "Attendance Rate"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (Student student : students) {
                List<AttendanceRecord> records = attendanceRepository
                    .findByStudentAndDateBetween(student, startDate, endDate);

                if (records.isEmpty()) {
                    continue; // Skip students with no attendance records
                }

                Row row = sheet.createRow(rowNum++);

                Map<AttendanceRecord.AttendanceStatus, Long> statusCounts = records.stream()
                    .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

                long presentCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.PRESENT, 0L);
                long absentCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.ABSENT, 0L);
                long tardyCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.TARDY, 0L);
                long excusedCount = statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.EXCUSED_ABSENT, 0L);
                long totalDays = records.size();
                double attendanceRate = totalDays > 0 ? (presentCount * 100.0 / totalDays) : 0.0;

                createCell(row, 0, student.getId().toString(), dataStyle);
                createCell(row, 1, student.getFullName(), dataStyle);
                createCell(row, 2, student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A", dataStyle);
                createCell(row, 3, String.valueOf(totalDays), dataStyle);
                createCell(row, 4, String.valueOf(presentCount), dataStyle);
                createCell(row, 5, String.valueOf(absentCount), dataStyle);
                createCell(row, 6, String.valueOf(tardyCount), dataStyle);
                createCell(row, 7, String.valueOf(excusedCount), dataStyle);

                Cell rateCell = row.createCell(8);
                rateCell.setCellValue(attendanceRate / 100.0);
                rateCell.setCellStyle(percentStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export chronic absenteeism report to Excel
     *
     * @param startDate Start date
     * @param endDate End date
     * @param thresholdPercent Chronic absenteeism threshold (default 10%)
     * @return Excel file as byte array
     */
    public byte[] exportChronicAbsenteeismReport(LocalDate startDate, LocalDate endDate, double thresholdPercent) throws IOException {
        log.info("Generating chronic absenteeism Excel report from {} to {}", startDate, endDate);

        List<Student> students = studentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Chronic Absenteeism");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle warningStyle = createWarningStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student ID", "Student Name", "Grade", "Total Days", "Absent Days", "Absence Rate", "At Risk"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (Student student : students) {
                List<AttendanceRecord> records = attendanceRepository
                    .findByStudentAndDateBetween(student, startDate, endDate);

                if (records.isEmpty()) {
                    continue;
                }

                long absentCount = records.stream()
                    .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                    .count();
                long totalDays = records.size();
                double absenceRate = totalDays > 0 ? (absentCount * 100.0 / totalDays) : 0.0;

                // Only include students at risk
                if (absenceRate < thresholdPercent) {
                    continue;
                }

                Row row = sheet.createRow(rowNum++);
                boolean isAtRisk = absenceRate >= thresholdPercent;

                createCell(row, 0, student.getId().toString(), isAtRisk ? warningStyle : dataStyle);
                createCell(row, 1, student.getFullName(), isAtRisk ? warningStyle : dataStyle);
                createCell(row, 2, student.getGradeLevel() != null ? student.getGradeLevel().toString() : "N/A", isAtRisk ? warningStyle : dataStyle);
                createCell(row, 3, String.valueOf(totalDays), isAtRisk ? warningStyle : dataStyle);
                createCell(row, 4, String.valueOf(absentCount), isAtRisk ? warningStyle : dataStyle);

                Cell rateCell = row.createCell(5);
                rateCell.setCellValue(absenceRate / 100.0);
                rateCell.setCellStyle(percentStyle);

                createCell(row, 6, isAtRisk ? "YES" : "NO", isAtRisk ? warningStyle : dataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create data cell style
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create warning cell style (for at-risk students)
     */
    private CellStyle createWarningStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create percent cell style
     */
    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        return style;
    }

    /**
     * Create cell with value and style
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Add summary section to sheet
     */
    private void addSummarySection(Sheet sheet, List<AttendanceRecord> records, int startRow) {
        Map<AttendanceRecord.AttendanceStatus, Long> statusCounts = records.stream()
            .collect(Collectors.groupingBy(AttendanceRecord::getStatus, Collectors.counting()));

        Row summaryLabelRow = sheet.createRow(startRow);
        summaryLabelRow.createCell(0).setCellValue("Summary:");

        Row totalRow = sheet.createRow(startRow + 1);
        totalRow.createCell(0).setCellValue("Total Records:");
        totalRow.createCell(1).setCellValue(records.size());

        Row presentRow = sheet.createRow(startRow + 2);
        presentRow.createCell(0).setCellValue("Present:");
        presentRow.createCell(1).setCellValue(statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.PRESENT, 0L));

        Row absentRow = sheet.createRow(startRow + 3);
        absentRow.createCell(0).setCellValue("Absent:");
        absentRow.createCell(1).setCellValue(statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.ABSENT, 0L));

        Row tardyRow = sheet.createRow(startRow + 4);
        tardyRow.createCell(0).setCellValue("Tardy:");
        tardyRow.createCell(1).setCellValue(statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.TARDY, 0L));

        Row excusedRow = sheet.createRow(startRow + 5);
        excusedRow.createCell(0).setCellValue("Excused:");
        excusedRow.createCell(1).setCellValue(statusCounts.getOrDefault(AttendanceRecord.AttendanceStatus.EXCUSED_ABSENT, 0L));
    }
}
