# Heronix SIS - Attendance Reporting System Guide

## Overview

The Heronix SIS Attendance Reporting System is a comprehensive solution for generating, scheduling, and managing attendance reports. This system was built across Phases 48-51 and provides end-to-end functionality from backend report generation to frontend user interface.

**Version:** 1.0
**Last Updated:** December 30, 2025
**Phases Covered:** 48, 49, 50, 51

---

## Table of Contents

1. [System Architecture](#system-architecture)
2. [Features](#features)
3. [API Reference](#api-reference)
4. [User Guide](#user-guide)
5. [Developer Guide](#developer-guide)
6. [Configuration](#configuration)
7. [Troubleshooting](#troubleshooting)

---

## System Architecture

### Components Overview

The Reporting System consists of four main layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  - JavaFX Report Generation Dialog                          │
│  - CSS Styling & UI Components                              │
│  - Report Dialog Launcher                                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    API Layer (REST)                          │
│  - AttendanceReportApiController                            │
│  - ReportManagementApiController                            │
│  - HTTP Endpoints                                            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  - AttendanceReportExportService (Excel)                    │
│  - AttendanceReportPdfService (PDF)                         │
│  - ScheduledReportService (Automation)                      │
│  - ReportHistoryService (Tracking)                          │
│  - EmailNotificationService (Alerts)                        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                                │
│  - AttendanceRepository                                      │
│  - StudentRepository                                         │
│  - ReportHistoryRepository                                  │
│  - JPA Entities & Database                                  │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

- **Backend Framework:** Spring Boot 3
- **Frontend:** JavaFX 21
- **Excel Generation:** Apache POI 5.x
- **PDF Generation:** Apache PDFBox 3.x
- **Scheduling:** Spring @Scheduled
- **Database:** JPA/Hibernate
- **Real-time Updates:** WebSocket (STOMP)
- **HTTP Client:** Java 11+ HttpClient

---

## Features

### Phase 48: Dashboard & Real-Time Updates
- ✅ Professional JavaFX FXML dashboard layouts
- ✅ CSS-styled metric cards with color coding
- ✅ WebSocket integration for live attendance updates
- ✅ Auto-reconnect functionality
- ✅ Real-time dashboard refresh

### Phase 49: Report Export & Scheduling
- ✅ Excel report generation (Apache POI)
  - Daily attendance reports
  - Student attendance summaries
  - Chronic absenteeism reports
- ✅ PDF report generation (Apache PDFBox)
  - Print-ready formatting
  - Professional headers/footers
  - Page numbering
- ✅ Automated scheduled reports
  - Daily reports (8 AM)
  - Weekly summaries (Monday 7 AM)
  - Monthly reports (1st at 6 AM)
- ✅ Email notification system (stub implementation)
- ✅ Report archival and cleanup

### Phase 50: REST API & History Tracking
- ✅ RESTful API endpoints
  - `/api/reports/attendance/daily/{format}`
  - `/api/reports/attendance/summary/{format}`
  - `/api/reports/attendance/chronic-absenteeism`
- ✅ Report history tracking
  - JPA entity for audit trail
  - Download count tracking
  - Access timestamps
- ✅ Report management endpoints
  - View history
  - Statistics
  - Cleanup old reports
- ✅ Complete metadata tracking

### Phase 51: Frontend Integration
- ✅ Modern report generation dialog
- ✅ Date range presets (Today, Yesterday, Last 7 Days, etc.)
- ✅ Real-time form validation
- ✅ Async report generation with progress feedback
- ✅ File save dialog integration
- ✅ Success/error alerting
- ✅ Desktop file opening

---

## API Reference

### Report Generation Endpoints

#### Generate Daily Attendance Report (Excel)
```http
GET /api/reports/attendance/daily/excel?date=2025-12-30
```

**Parameters:**
- `date` (optional): Report date in ISO format (yyyy-MM-dd). Defaults to yesterday.

**Response:**
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Body: Binary Excel file

**Example:**
```bash
curl -O "http://localhost:9590/api/reports/attendance/daily/excel?date=2025-12-30"
```

#### Generate Daily Attendance Report (PDF)
```http
GET /api/reports/attendance/daily/pdf?date=2025-12-30
```

**Parameters:**
- `date` (optional): Report date in ISO format. Defaults to yesterday.

**Response:**
- Content-Type: `application/pdf`
- Body: Binary PDF file

#### Generate Student Attendance Summary (Excel)
```http
GET /api/reports/attendance/summary/excel?startDate=2025-12-01&endDate=2025-12-30
```

**Parameters:**
- `startDate` (optional): Period start date. Defaults to first day of current month.
- `endDate` (optional): Period end date. Defaults to today.

**Response:**
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Body: Binary Excel file with attendance statistics

#### Generate Student Attendance Summary (PDF)
```http
GET /api/reports/attendance/summary/pdf?startDate=2025-12-01&endDate=2025-12-30
```

**Parameters:**
- `startDate` (optional): Period start date
- `endDate` (optional): Period end date

**Response:**
- Content-Type: `application/pdf`
- Body: Binary PDF file

#### Generate Chronic Absenteeism Report
```http
GET /api/reports/attendance/chronic-absenteeism?startDate=2025-12-01&endDate=2025-12-30&threshold=10.0
```

**Parameters:**
- `startDate` (optional): Period start date. Defaults to first day of current month.
- `endDate` (optional): Period end date. Defaults to today.
- `threshold` (optional): Absenteeism threshold percentage. Defaults to 10.0.

**Response:**
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Body: Binary Excel file with at-risk students

### Report Management Endpoints

#### Get All Report History
```http
GET /api/reports/management/history
```

**Response:**
```json
[
  {
    "id": 1,
    "reportType": "DAILY_ATTENDANCE",
    "reportFormat": "EXCEL",
    "reportName": "daily-attendance-2025-12-30.xlsx",
    "filePath": "/reports/daily-attendance-2025-12-30.xlsx",
    "fileSize": 12345,
    "startDate": "2025-12-30",
    "endDate": "2025-12-30",
    "generatedBy": "admin",
    "generatedAt": "2025-12-30T08:00:00",
    "status": "COMPLETED",
    "scheduled": true,
    "emailed": false,
    "downloadCount": 3
  }
]
```

#### Get Specific Report History
```http
GET /api/reports/management/history/{id}
```

**Path Parameters:**
- `id`: Report history ID

#### Get Recent Reports
```http
GET /api/reports/management/recent?days=30
```

**Parameters:**
- `days` (optional): Number of days to look back. Defaults to 30.

#### Get Reports by Type
```http
GET /api/reports/management/type/{type}
```

**Path Parameters:**
- `type`: Report type (`DAILY_ATTENDANCE`, `STUDENT_SUMMARY`, `CHRONIC_ABSENTEEISM`)

#### Get Reports by Status
```http
GET /api/reports/management/status/{status}
```

**Path Parameters:**
- `status`: Report status (`COMPLETED`, `FAILED`, `PENDING`, `GENERATING`)

#### Get Report Statistics
```http
GET /api/reports/management/statistics
```

**Response:**
```json
{
  "totalReports": 100,
  "completedReports": 85,
  "failedReports": 10,
  "scheduledReports": 15
}
```

#### Clean Up Old Reports
```http
POST /api/reports/management/cleanup?retentionDays=90
```

**Parameters:**
- `retentionDays` (optional): Number of days to retain reports. Defaults to 90.

**Response:**
```json
{
  "cleanedCount": 25,
  "retentionDays": 90
}
```

---

## User Guide

### Generating Reports via UI

1. **Open Report Dialog**
   - Navigate to the main menu
   - Select "Reports" > "Generate Attendance Report"
   - The Report Generation Dialog will appear

2. **Select Report Type**
   - Choose from:
     - **Daily Attendance**: Detailed record for a specific date
     - **Student Summary**: Aggregated statistics over a date range
     - **Chronic Absenteeism**: At-risk students above threshold

3. **Select Format**
   - **Excel (.xlsx)**: Spreadsheet with formulas and formatting
   - **PDF**: Print-ready document

4. **Choose Date Range**
   - Use calendar pickers for start/end dates
   - Or click preset buttons:
     - Today
     - Yesterday
     - Last 7 Days
     - Last 30 Days
     - This Month

5. **Configure Parameters** (if applicable)
   - For Chronic Absenteeism: Set threshold percentage (default: 10%)

6. **Generate Report**
   - Click "Generate Report"
   - Choose save location
   - Wait for progress indicator
   - Report will download and optionally open

### Scheduled Reports

Reports are automatically generated on schedule:

- **Daily Attendance**: Every day at 8:00 AM
- **Weekly Summary**: Every Monday at 7:00 AM
- **Monthly Chronic Absenteeism**: 1st of month at 6:00 AM

Scheduled reports are saved to: `reports/attendance/`

### Email Notifications

Configure email notifications in `application.properties`:

```properties
email.enabled=true
email.smtp.host=smtp.example.com
email.smtp.port=587
email.smtp.username=your-email@example.com
email.smtp.password=your-password
email.from=noreply@example.com
email.admin.recipients=admin@example.com
```

---

## Developer Guide

### Adding a New Report Type

1. **Create Service Method**

```java
// In AttendanceReportExportService.java
public byte[] exportCustomReport(LocalDate date, String parameter) throws IOException {
    // Query data
    List<AttendanceRecord> records = attendanceRepository.customQuery(date, parameter);

    // Generate Excel
    try (Workbook workbook = new XSSFWorkbook()) {
        Sheet sheet = workbook.createSheet("Custom Report");
        // ... populate sheet

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }
}
```

2. **Add API Endpoint**

```java
// In AttendanceReportApiController.java
@GetMapping("/custom")
public ResponseEntity<byte[]> generateCustomReport(
        @RequestParam LocalDate date,
        @RequestParam String parameter) {

    try {
        byte[] excelData = exportService.exportCustomReport(date, parameter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "custom-report.xlsx");

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

3. **Add to UI**
   - Update `ReportGenerationDialog.fxml` ComboBox
   - Add handler in `ReportGenerationDialogController.java`
   - Update `buildEndpoint()` method

### Customizing Report Templates

#### Excel Styling

```java
private CellStyle createCustomStyle(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    font.setColor(IndexedColors.BLUE.getIndex());
    style.setFont(font);
    style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return style;
}
```

#### PDF Customization

```java
private void drawCustomHeader(PDPageContentStream contentStream, float yPosition) throws IOException {
    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
    contentStream.beginText();
    contentStream.newLineAtOffset(MARGIN, yPosition);
    contentStream.showText("Custom Report Header");
    contentStream.endText();
}
```

### Database Schema

#### ReportHistory Table

```sql
CREATE TABLE report_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_type VARCHAR(50) NOT NULL,
    report_format VARCHAR(20) NOT NULL,
    report_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    start_date DATE,
    end_date DATE,
    parameters TEXT,
    generated_by VARCHAR(100),
    generated_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    scheduled BOOLEAN DEFAULT FALSE,
    emailed BOOLEAN DEFAULT FALSE,
    download_count INT DEFAULT 0,
    last_accessed TIMESTAMP,
    INDEX idx_report_type_generated (report_type, generated_at),
    INDEX idx_generated_by_date (generated_by, generated_at)
);
```

---

## Configuration

### Application Properties

```properties
# API Base URL
api.base-url=http://localhost:9590

# Report Storage
reports.storage.path=reports/attendance
reports.retention.days=90

# Scheduled Reports
reports.scheduled.daily.enabled=true
reports.scheduled.daily.cron=0 0 8 * * ?
reports.scheduled.weekly.enabled=true
reports.scheduled.weekly.cron=0 0 7 ? * MON
reports.scheduled.monthly.enabled=true
reports.scheduled.monthly.cron=0 0 6 1 * ?

# Email Configuration
email.enabled=false
email.smtp.host=smtp.example.com
email.smtp.port=587
email.smtp.username=
email.smtp.password=
email.from=noreply@example.com
email.admin.recipients=admin@example.com

# WebSocket
websocket.enabled=true
websocket.endpoint=/ws
websocket.topic.prefix=/topic
```

---

## Troubleshooting

### Common Issues

#### Reports Not Generating

**Symptom:** API returns 500 error

**Solutions:**
1. Check database connectivity
2. Verify AttendanceRepository has data
3. Check application logs for stack traces
4. Ensure Apache POI/PDFBox dependencies are present

#### Scheduled Reports Not Running

**Symptom:** No reports generated at scheduled times

**Solutions:**
1. Verify `@EnableScheduling` is present in main application class
2. Check cron expression syntax
3. Ensure application is running during scheduled time
4. Check logs for scheduling errors

#### UI Dialog Not Opening

**Symptom:** Error when clicking "Generate Report"

**Solutions:**
1. Verify FXML file path is correct
2. Check Spring context initialization
3. Ensure ReportDialogLauncher is autowired
4. Check JavaFX thread safety

#### Large Reports Timing Out

**Symptom:** HTTP timeout for large date ranges

**Solutions:**
1. Increase HTTP client timeout
2. Add pagination to queries
3. Generate reports asynchronously
4. Optimize database queries

### Logging

Enable debug logging:

```properties
logging.level.com.heronix.service=DEBUG
logging.level.com.heronix.controller=DEBUG
```

---

## Performance Considerations

### Excel Generation
- **Small reports (<1000 rows):** ~1-2 seconds
- **Medium reports (1000-10000 rows):** ~5-10 seconds
- **Large reports (>10000 rows):** Consider pagination

### PDF Generation
- **Small reports (<500 records):** ~2-3 seconds
- **Large reports:** May require multiple pages, ~5-15 seconds

### Optimization Tips
1. Use database indexes on `attendance_date` and `student_id`
2. Batch query results instead of individual lookups
3. Cache frequently accessed data
4. Use async report generation for large reports
5. Implement report result caching

---

## Security Considerations

1. **Authentication:** Ensure API endpoints require authentication
2. **Authorization:** Implement role-based access control
3. **Input Validation:** Validate all date ranges and parameters
4. **File Access:** Restrict file system access to reports directory
5. **SQL Injection:** Use parameterized queries (already implemented via JPA)
6. **Rate Limiting:** Implement rate limiting for API endpoints

---

## Support

For issues or questions:
- **Email:** support@heronix.com
- **Documentation:** https://docs.heronix.com
- **Issue Tracker:** https://github.com/heronix/sis/issues

---

**End of Guide**
