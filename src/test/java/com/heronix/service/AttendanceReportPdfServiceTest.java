package com.heronix.service;

import com.heronix.model.domain.AttendanceRecord;
import com.heronix.model.domain.Student;
import com.heronix.repository.AttendanceRepository;
import com.heronix.repository.StudentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
 * Unit Tests for AttendanceReportPdfService
 *
 * Tests the PDF report generation functionality including:
 * - Daily attendance PDF reports
 * - Student attendance summary PDFs
 * - PDF formatting and content
 * - Page handling and overflow
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 53 - Testing Infrastructure
 */
@ExtendWith(MockitoExtension.class)
class AttendanceReportPdfServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private AttendanceReportPdfService pdfService;

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
    void testExportDailyAttendancePdf_Success() throws IOException {
        // Given
        LocalDate reportDate = LocalDate.of(2025, 12, 30);
        List<AttendanceRecord> records = Arrays.asList(presentRecord, absentRecord);
        when(attendanceRepository.findByAttendanceDate(reportDate)).thenReturn(records);

        // When
        byte[] pdfData = pdfService.exportDailyAttendancePdf(reportDate);

        // Then
        assertThat(pdfData).isNotNull();
        assertThat(pdfData.length).isGreaterThan(0);
        verify(attendanceRepository, times(1)).findByAttendanceDate(reportDate);

        // Verify PDF content
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            assertThat(text).contains("Daily Attendance Report");
            assertThat(text).contains("John");
            assertThat(text).contains("Doe");
            assertThat(text).contains("Jane");
            assertThat(text).contains("Smith");
            assertThat(text).contains("PRESENT");
            assertThat(text).contains("ABSENT");
        }
    }

    @Test
    void testExportDailyAttendancePdf_EmptyRecords() throws IOException {
        // Given
        LocalDate reportDate = LocalDate.of(2025, 12, 30);
        when(attendanceRepository.findByAttendanceDate(reportDate)).thenReturn(Arrays.asList());

        // When
        byte[] pdfData = pdfService.exportDailyAttendancePdf(reportDate);

        // Then
        assertThat(pdfData).isNotNull();
        verify(attendanceRepository, times(1)).findByAttendanceDate(reportDate);

        // Verify PDF has header but minimal content
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            assertThat(text).contains("Daily Attendance Report");
        }
    }

    @Test
    void testExportStudentAttendanceSummaryPdf_Success() throws IOException {
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
        byte[] pdfData = pdfService.exportStudentAttendanceSummaryPdf(startDate, endDate);

        // Then
        assertThat(pdfData).isNotNull();
        assertThat(pdfData.length).isGreaterThan(0);
        verify(studentRepository, times(1)).findAll();
        verify(attendanceRepository, times(2)).findByStudentAndDateBetween(any(), any(), any());

        // Verify PDF content
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            assertThat(text).contains("Student Attendance Summary");
            assertThat(text).contains("John");
            assertThat(text).contains("Jane");
        }
    }

    @Test
    void testExportDailyAttendancePdf_MultiplePages() throws IOException {
        // Given - Create many records to force multiple pages
        LocalDate reportDate = LocalDate.of(2025, 12, 30);

        List<AttendanceRecord> manyRecords = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Student student = new Student();
            student.setId((long) i);
            student.setFirstName("Student" + i);
            student.setLastName("Test" + i);
            student.setGradeLevel(String.valueOf(9 + (i % 4)));

            AttendanceRecord record = new AttendanceRecord();
            record.setId((long) i);
            record.setStudent(student);
            record.setAttendanceDate(reportDate);
            record.setStatus(i % 2 == 0 ? AttendanceRecord.AttendanceStatus.PRESENT : AttendanceRecord.AttendanceStatus.ABSENT);
            record.setCreatedAt(LocalDateTime.of(2025, 12, 30, 8, 30));

            manyRecords.add(record);
        }

        when(attendanceRepository.findByAttendanceDate(reportDate)).thenReturn(manyRecords);

        // When
        byte[] pdfData = pdfService.exportDailyAttendancePdf(reportDate);

        // Then
        assertThat(pdfData).isNotNull();

        try (PDDocument document = Loader.loadPDF(pdfData)) {
            // Should have multiple pages due to overflow
            assertThat(document.getNumberOfPages()).isGreaterThan(1);
        }
    }

    @Test
    void testExportDailyAttendancePdf_NullDate() {
        // When/Then
        assertThatThrownBy(() -> pdfService.exportDailyAttendancePdf(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testExportStudentAttendanceSummaryPdf_InvalidDateRange() {
        // Given - end date before start date
        LocalDate startDate = LocalDate.of(2025, 12, 30);
        LocalDate endDate = LocalDate.of(2025, 12, 1);

        // When/Then - should handle gracefully or throw exception
        assertThatThrownBy(() -> pdfService.exportStudentAttendanceSummaryPdf(startDate, endDate))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testPdfContainsMetadata() throws IOException {
        // Given
        LocalDate reportDate = LocalDate.of(2025, 12, 30);
        when(attendanceRepository.findByAttendanceDate(reportDate)).thenReturn(Arrays.asList(presentRecord));

        // When
        byte[] pdfData = pdfService.exportDailyAttendancePdf(reportDate);

        // Then
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            // Verify PDF has valid metadata
            assertThat(document.getDocumentInformation()).isNotNull();
            assertThat(document.getDocumentInformation().getTitle()).isNotNull();
        }
    }
}
