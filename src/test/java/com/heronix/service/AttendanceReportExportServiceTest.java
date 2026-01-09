package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AttendanceReportExportService
 *
 * Tests the Excel report generation functionality including:
 * - Daily attendance reports
 * - Student attendance summaries
 * - Chronic absenteeism reports
 * - Excel formatting and styling
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 53 - Testing Infrastructure
 */
@ExtendWith(MockitoExtension.class)
class AttendanceReportExportServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private AttendanceReportExportService exportService;

    private Student sampleStudent1;
    private Student sampleStudent2;
    private AttendanceRecord presentRecord;
    private AttendanceRecord absentRecord;

    @BeforeEach
    void setUp() {
        sampleStudent1 = new Student();
        sampleStudent1.setId(1L);
        sampleStudent1.setFirstName("John");
        sampleStudent1.setLastName("Doe");
        sampleStudent1.setGradeLevel("10");

        sampleStudent2 = new Student();
        sampleStudent2.setId(2L);
        sampleStudent2.setFirstName("Jane");
        sampleStudent2.setLastName("Smith");
        sampleStudent2.setGradeLevel("11");

        presentRecord = new AttendanceRecord();
        presentRecord.setId(1L);
        presentRecord.setStudent(sampleStudent1);
        presentRecord.setAttendanceDate(LocalDate.of(2025, 12, 30));
        presentRecord.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
        presentRecord.setCreatedAt(LocalDateTime.of(2025, 12, 30, 8, 30));
        presentRecord.setNotes("On time");

        absentRecord = new AttendanceRecord();
        absentRecord.setId(2L);
        absentRecord.setStudent(sampleStudent2);
        absentRecord.setAttendanceDate(LocalDate.of(2025, 12, 30));
        absentRecord.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
        absentRecord.setCreatedAt(LocalDateTime.of(2025, 12, 30, 8, 35));
        absentRecord.setNotes("Unexcused absence");
    }

    @Test
    void testExportDailyAttendanceExcel_Success() throws IOException {
        // Given
        LocalDate reportDate = LocalDate.of(2025, 12, 30);
        List<AttendanceRecord> records = Arrays.asList(presentRecord, absentRecord);
        when(attendanceRepository.findByAttendanceDate(reportDate)).thenReturn(records);

        // When
        byte[] excelData = exportService.exportDailyAttendanceExcel(reportDate);

        // Then
        assertThat(excelData).isNotNull();
        assertThat(excelData.length).isGreaterThan(0);
        verify(attendanceRepository, times(1)).findByAttendanceDate(reportDate);

        // Verify Excel content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).contains("Daily Attendance");

            // Verify header row
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow).isNotNull();
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("Student ID");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Student Name");

            // Verify data rows
            Row dataRow1 = sheet.getRow(1);
            assertThat(dataRow1).isNotNull();
            assertThat(dataRow1.getCell(0).getNumericCellValue()).isEqualTo(1.0);
            assertThat(dataRow1.getCell(1).getStringCellValue()).contains("John");
        }
    }

    @Test
    void testExportDailyAttendanceExcel_EmptyRecords() throws IOException {
        // Given
        LocalDate reportDate = LocalDate.of(2025, 12, 30);
        when(attendanceRepository.findByAttendanceDate(reportDate)).thenReturn(Arrays.asList());

        // When
        byte[] excelData = exportService.exportDailyAttendanceExcel(reportDate);

        // Then
        assertThat(excelData).isNotNull();
        verify(attendanceRepository, times(1)).findByAttendanceDate(reportDate);

        // Verify Excel has header but no data rows
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(0); // Only header row
        }
    }

    @Test
    void testExportStudentAttendanceSummaryExcel_Success() throws IOException {
        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 30);

        List<Student> students = Arrays.asList(sampleStudent1, sampleStudent2);
        when(studentRepository.findAll()).thenReturn(students);

        List<AttendanceRecord> student1Records = Arrays.asList(presentRecord);
        when(attendanceRepository.findByStudentAndDateBetween(sampleStudent1, startDate, endDate))
            .thenReturn(student1Records);

        List<AttendanceRecord> student2Records = Arrays.asList(absentRecord);
        when(attendanceRepository.findByStudentAndDateBetween(sampleStudent2, startDate, endDate))
            .thenReturn(student2Records);

        // When
        byte[] excelData = exportService.exportStudentAttendanceSummary(startDate, endDate);

        // Then
        assertThat(excelData).isNotNull();
        assertThat(excelData.length).isGreaterThan(0);
        verify(studentRepository, times(1)).findAll();
        verify(attendanceRepository, times(2)).findByStudentAndDateBetween(any(), any(), any());

        // Verify Excel content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).contains("Student Attendance Summary");

            // Verify at least 2 student rows plus header
            assertThat(sheet.getLastRowNum()).isGreaterThanOrEqualTo(2);
        }
    }

    @Test
    void testExportChronicAbsenteeismReport_Success() throws IOException {
        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 30);
        double threshold = 10.0;

        List<Student> students = Arrays.asList(sampleStudent1, sampleStudent2);
        when(studentRepository.findAll()).thenReturn(students);

        // Student 1: 1 present out of 1 record (0% absent)
        List<AttendanceRecord> student1Records = Arrays.asList(presentRecord);
        when(attendanceRepository.findByStudentAndDateBetween(sampleStudent1, startDate, endDate))
            .thenReturn(student1Records);

        // Student 2: 1 absent out of 1 record (100% absent - above threshold)
        List<AttendanceRecord> student2Records = Arrays.asList(absentRecord);
        when(attendanceRepository.findByStudentAndDateBetween(sampleStudent2, startDate, endDate))
            .thenReturn(student2Records);

        // When
        byte[] excelData = exportService.exportChronicAbsenteeismReport(startDate, endDate, threshold);

        // Then
        assertThat(excelData).isNotNull();
        assertThat(excelData.length).isGreaterThan(0);
        verify(studentRepository, times(1)).findAll();

        // Verify Excel content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).contains("Chronic Absenteeism");

            // Should have header + at least 1 at-risk student (Jane Smith with 100% absence)
            assertThat(sheet.getLastRowNum()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void testExportChronicAbsenteeismReport_NoAtRiskStudents() throws IOException {
        // Given
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 30);
        double threshold = 100.0; // Very high threshold - no students should qualify

        List<Student> students = Arrays.asList(sampleStudent1);
        when(studentRepository.findAll()).thenReturn(students);

        List<AttendanceRecord> student1Records = Arrays.asList(presentRecord);
        when(attendanceRepository.findByStudentAndDateBetween(sampleStudent1, startDate, endDate))
            .thenReturn(student1Records);

        // When
        byte[] excelData = exportService.exportChronicAbsenteeismReport(startDate, endDate, threshold);

        // Then
        assertThat(excelData).isNotNull();
        verify(studentRepository, times(1)).findAll();

        // Verify Excel has header but minimal data rows
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheetAt(0);
            // Should only have header row
            assertThat(sheet.getLastRowNum()).isEqualTo(0);
        }
    }

    @Test
    void testExportDailyAttendanceExcel_NullDate() {
        // When/Then
        assertThatThrownBy(() -> exportService.exportDailyAttendanceExcel(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testExportStudentAttendanceSummaryExcel_InvalidDateRange() {
        // Given - end date before start date
        LocalDate startDate = LocalDate.of(2025, 12, 30);
        LocalDate endDate = LocalDate.of(2025, 12, 1);

        // When/Then - should handle gracefully or throw exception
        assertThatThrownBy(() -> exportService.exportStudentAttendanceSummary(startDate, endDate))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
