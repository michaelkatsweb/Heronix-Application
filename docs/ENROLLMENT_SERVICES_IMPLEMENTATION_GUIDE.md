# Enrollment Services Implementation Guide

## Overview

This guide provides detailed information about the Heronix SIS Enrollment Services implementation, including architecture, design patterns, database schema, and integration instructions.

**Version**: 1.0.0
**Last Updated**: December 23, 2025
**Author**: Heronix SIS Development Team

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Service Implementations](#service-implementations)
4. [Database Schema](#database-schema)
5. [Workflow Management](#workflow-management)
6. [Integration Guide](#integration-guide)
7. [Testing Strategy](#testing-strategy)
8. [Deployment](#deployment)

---

## Architecture Overview

### Three-Tier Architecture

```
┌─────────────────────────────────────────────┐
│          Presentation Layer                 │
│  (REST Controllers / JavaFX UI)             │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Business Logic Layer               │
│  (Services with @Transactional)             │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Data Access Layer                  │
│  (Spring Data JPA Repositories)             │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│          Database (H2/PostgreSQL)           │
└─────────────────────────────────────────────┘
```

### Design Patterns Used

1. **Repository Pattern**: Data access abstraction
2. **Service Layer Pattern**: Business logic encapsulation
3. **Builder Pattern**: Entity construction (via Lombok @Builder)
4. **Strategy Pattern**: Status-based workflow management
5. **DTO Pattern**: Data transfer between layers

---

## Technology Stack

### Backend Framework
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: Repository and ORM
- **Hibernate**: JPA implementation with auto-DDL
- **H2 Database**: Embedded database (development)
- **Lombok**: Code generation (@Data, @Builder, @Slf4j)

### API Layer
- **Spring Web**: REST controllers
- **Jackson**: JSON serialization
- **CORS**: Cross-origin resource sharing enabled

### Testing
- **JUnit 5**: Testing framework
- **AssertJ**: Fluent assertions
- **Spring Boot Test**: Integration testing support

---

## Service Implementations

### 1. Transfer Student Service

**Purpose**: Manage incoming transfer students from other schools.

**Location**: `com.heronix.service.TransferStudentService`

**Key Features**:
- Automatic transfer number generation (`TRN-YYYY-NNNNNN`)
- 7-document records tracking system
- Counselor assignment workflow
- Automatic status progression based on records completion
- Statistical reporting

**Core Methods**:
```java
// Create new transfer
TransferStudent createTransferRecord(
    String firstName, String lastName, LocalDate dob,
    LocalDate transferDate, String previousSchool, Long staffId)

// Track received documents (7 types)
TransferStudent markRecordReceived(
    Long transferId, String recordType, Long staffId)

// Workflow management
TransferStudent assignCounselor(Long transferId, Long counselorId, Long staffId)
TransferStudent completeEnrollment(
    Long transferId, Long studentId, String grade,
    String homeroom, Long staffId)

// Statistics
TransferStatistics getStatistics()
```

**Business Rules**:
1. Cannot have duplicate pending transfers for same student
2. All 7 records must be received before placement
3. Only DRAFT status records can be deleted
4. Cancellation stores reason in administrative notes

---

### 2. Re-Enrollment Service

**Purpose**: Handle students re-enrolling after withdrawal.

**Location**: `com.heronix.service.ReEnrollmentService`

**Key Features**:
- Automatic re-enrollment number generation (`REE-YYYY-NNNNNN`)
- Two-tier approval workflow (Counselor → Principal)
- Time-away calculation (months/years since withdrawal)
- Conditional approval support
- Outstanding fees tracking

**Core Methods**:
```java
// Create re-enrollment application
ReEnrollment createReEnrollment(
    Long studentId, String requestedGrade,
    LocalDate intendedDate, Long staffId)

// Approval workflow
ReEnrollment assignCounselor(Long reEnrollmentId, Long counselorId, Long staffId)
ReEnrollment counselorDecision(
    Long id, ApprovalDecision decision, String notes, Long counselorId)
ReEnrollment principalDecision(
    Long id, ApprovalDecision decision, String notes, Long principalId)

// Completion
ReEnrollment recordFeePayment(Long id, Long staffId)
ReEnrollment completeReEnrollment(Long id, Long studentId, Long staffId)
```

**Business Rules**:
1. Counselor must approve before principal review
2. Cannot cancel enrolled re-enrollments
3. Fees must be paid before enrollment (if outstanding)
4. Only DRAFT status can be deleted

**Approval Decisions**:
- `APPROVED`: Full approval
- `CONDITIONAL`: Approved with conditions
- `DENIED`: Rejected

---

### 3. Enrollment Verification Service

**Purpose**: Generate enrollment verification letters/certificates.

**Location**: `com.heronix.service.EnrollmentVerificationService`

**Key Features**:
- Automatic verification number generation (`ENV-YYYY-NNNNNN`)
- Multiple verification purposes (loans, visas, insurance, etc.)
- Fee calculation and tracking
- Document generation workflow
- 90-day validity period (configurable)

**Core Methods**:
```java
// Create verification request
EnrollmentVerification createVerification(
    Long studentId, VerificationPurpose purpose, Long staffId)

// Verification workflow
EnrollmentVerification verifyEnrollment(
    Long id, Boolean fullTime, Boolean goodStanding,
    Double gpa, Long staffId)
EnrollmentVerification generateDocument(Long id, String path, Long staffId)
EnrollmentVerification sendVerification(
    Long id, String method, String tracking, Long staffId)

// Fee management
EnrollmentVerification recordFeePayment(Long id, Long staffId)
```

**Verification Purposes**:
- `COLLEGE_APPLICATION`
- `STUDENT_LOAN`
- `VISA_IMMIGRATION`
- `INSURANCE`
- `EMPLOYMENT`
- `GOVERNMENT_BENEFIT`
- `ATHLETIC_ELIGIBILITY`
- `OTHER`

**Business Rules**:
1. Must verify enrollment before document generation
2. Fees must be paid before sending (if applicable)
3. Documents have configurable validity period
4. Only DRAFT can be deleted

---

### 4. Transfer Out Documentation Service

**Purpose**: Manage outgoing student records to other schools.

**Location**: `com.heronix.service.TransferOutDocumentationService`

**Key Features**:
- Automatic transfer-out number generation (`TRO-YYYY-NNNNNN`)
- 12-document packaging system
- Multiple transmission methods
- FERPA compliance tracking
- Acknowledgment and follow-up system

**Core Methods**:
```java
// Create transfer out
TransferOutDocumentation createTransferOut(
    Long studentId, String destinationSchool,
    LocalDate expectedDate, Long staffId)

// Records packaging (12 document types)
TransferOutDocumentation markDocumentIncluded(
    Long id, String documentType, Long staffId)
TransferOutDocumentation includeAllDocuments(Long id, Long staffId)

// Compliance
TransferOutDocumentation recordParentConsent(Long id, Long staffId)
TransferOutDocumentation recordFeePayment(Long id, Long staffId)

// Transmission
TransferOutDocumentation sendRecords(
    Long id, TransmissionMethod method, String tracking, Long staffId)
TransferOutDocumentation recordAcknowledgment(
    Long id, String acknowledgedBy, String method, Long staffId)

// Follow-up
TransferOutDocumentation addFollowUpNote(Long id, String note, Long staffId)
```

**Transmission Methods**:
- `US_MAIL`, `CERTIFIED_MAIL`, `COURIER`
- `FAX`, `EMAIL`
- `ELECTRONIC_TRANSCRIPT`, `PARCHMENT`, `NAVIANCE`
- `HAND_DELIVERY`, `PICKUP`

**Business Rules**:
1. All required documents must be packaged
2. Parent consent required (FERPA)
3. Outstanding fees must be paid if holding records
4. Cannot complete until destination acknowledges receipt
5. Automatic follow-up scheduling based on transmission method

---

## Database Schema

### Entity Relationships

```
Student ──────┐
              ├──< TransferStudent
              ├──< ReEnrollment
              ├──< EnrollmentVerification
              └──< TransferOutDocumentation

User (Staff) ─┐
              ├──< created_by
              ├──< updated_by
              ├──< assigned_counselor
              └──< processed_by
```

### Key Fields by Entity

#### TransferStudent Table
```sql
CREATE TABLE transfer_students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transfer_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    transfer_date DATE,
    student_first_name VARCHAR(100),
    student_last_name VARCHAR(100),
    date_of_birth DATE,
    previous_school_name VARCHAR(200),
    -- Records tracking (7 documents)
    transcript_received BOOLEAN,
    immunization_records_received BOOLEAN,
    iep_received BOOLEAN,
    plan_504_received BOOLEAN,
    discipline_records_received BOOLEAN,
    attendance_records_received BOOLEAN,
    test_scores_received BOOLEAN,
    total_records_expected INTEGER,
    records_received INTEGER,
    all_records_received BOOLEAN,
    -- Audit fields
    created_by_staff_id BIGINT,
    created_at TIMESTAMP,
    updated_by_staff_id BIGINT,
    updated_at TIMESTAMP,
    INDEX idx_transfer_number (transfer_number),
    INDEX idx_status (status)
);
```

#### ReEnrollment Table
```sql
CREATE TABLE re_enrollments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    re_enrollment_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    application_date DATE NOT NULL,
    student_id BIGINT NOT NULL,
    requested_grade_level VARCHAR(20),
    intended_enrollment_date DATE,
    -- Previous enrollment
    previous_withdrawal_date DATE,
    months_away INTEGER,
    years_away INTEGER,
    -- Approval workflow
    counselor_decision VARCHAR(20),
    counselor_review_date DATE,
    counselor_notes TEXT,
    principal_decision VARCHAR(20),
    principal_review_date DATE,
    principal_notes TEXT,
    -- Fees
    has_outstanding_fees BOOLEAN,
    outstanding_fees_amount DECIMAL(10,2),
    fees_paid BOOLEAN,
    -- Audit fields
    created_by_staff_id BIGINT,
    created_at TIMESTAMP,
    updated_by_staff_id BIGINT,
    updated_at TIMESTAMP,
    INDEX idx_re_enrollment_number (re_enrollment_number),
    INDEX idx_status (status),
    INDEX idx_student (student_id)
);
```

#### EnrollmentVerification Table
```sql
CREATE TABLE enrollment_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    verification_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_date DATE NOT NULL,
    student_id BIGINT NOT NULL,
    purpose VARCHAR(50),
    document_type VARCHAR(30),
    -- Verification details
    full_time_enrollment BOOLEAN,
    academic_good_standing BOOLEAN,
    include_gpa BOOLEAN,
    current_gpa DECIMAL(3,2),
    -- Document properties
    official_seal BOOLEAN,
    has_expiration BOOLEAN,
    validity_days INTEGER,
    valid_from DATE,
    valid_until DATE,
    -- Fees
    verification_fee DECIMAL(10,2),
    total_fee DECIMAL(10,2),
    fee_paid BOOLEAN,
    urgent_request BOOLEAN,
    -- Audit fields
    created_by_staff_id BIGINT,
    created_at TIMESTAMP,
    INDEX idx_verification_number (verification_number),
    INDEX idx_status (status),
    INDEX idx_student (student_id),
    INDEX idx_purpose (purpose)
);
```

#### TransferOutDocumentation Table
```sql
CREATE TABLE transfer_out_documentation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transfer_out_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_date DATE NOT NULL,
    student_id BIGINT NOT NULL,
    expected_transfer_date DATE,
    -- Destination school
    destination_school_name VARCHAR(200),
    destination_school_city VARCHAR(100),
    destination_school_state VARCHAR(2),
    destination_school_zip VARCHAR(10),
    destination_school_phone VARCHAR(20),
    -- Documents (12 types)
    transcript_included BOOLEAN,
    immunization_records_included BOOLEAN,
    iep_included BOOLEAN,
    plan_504_included BOOLEAN,
    discipline_records_included BOOLEAN,
    attendance_records_included BOOLEAN,
    test_scores_included BOOLEAN,
    health_records_included BOOLEAN,
    special_education_records_included BOOLEAN,
    counseling_records_included BOOLEAN,
    athletic_eligibility_included BOOLEAN,
    cumulative_folder_included BOOLEAN,
    total_documents_included INTEGER,
    documents_packaged INTEGER,
    all_documents_packaged BOOLEAN,
    -- Compliance
    requires_parent_consent BOOLEAN,
    parent_consent_obtained BOOLEAN,
    ferpa_release_obtained BOOLEAN,
    -- Transmission
    transmission_method VARCHAR(30),
    sent_date DATE,
    tracking_number VARCHAR(100),
    destination_acknowledged BOOLEAN,
    acknowledgment_date DATE,
    -- Fees
    has_outstanding_fees BOOLEAN,
    fees_paid_before_release BOOLEAN,
    records_held_for_non_payment BOOLEAN,
    -- Audit fields
    created_by_staff_id BIGINT,
    created_at TIMESTAMP,
    INDEX idx_transfer_out_number (transfer_out_number),
    INDEX idx_status (status),
    INDEX idx_student (student_id)
);
```

---

## Workflow Management

### Status Enums and Transitions

Each service uses enum-based status management for type safety and workflow control.

#### Example: Transfer Student Workflow
```java
public enum TransferStatus {
    DRAFT,                    // Initial creation
    RECORDS_REQUESTED,        // Requested from previous school
    PARTIAL_RECORDS,          // Some records received
    ALL_RECORDS_RECEIVED,     // All 7 records received
    COUNSELOR_ASSIGNED,       // Assigned to counselor
    PLACEMENT_TESTING,        // Undergoing placement tests
    READY_FOR_PLACEMENT,      // Ready for grade assignment
    ENROLLED,                 // Enrolled in school
    COMPLETED,                // Process completed
    CANCELLED                 // Cancelled
}
```

### Automatic Status Transitions

Services automatically update status based on data completeness:

```java
// Example from TransferStudentService
public TransferStudent markRecordReceived(Long id, String type, Long staffId) {
    TransferStudent transfer = getTransferById(id);

    // Mark specific record as received
    switch (type.toLowerCase()) {
        case "transcript" -> transfer.setTranscriptReceived(true);
        // ... other types
    }

    // Recalculate completion
    transfer.calculateRecordsCompletion();

    // Auto-transition if all records received
    if (Boolean.TRUE.equals(transfer.getAllRecordsReceived())) {
        transfer.setStatus(TransferStatus.ALL_RECORDS_RECEIVED);
    }

    return updateTransfer(transfer, staffId);
}
```

---

## Integration Guide

### Frontend Integration (JavaFX)

#### Example: Creating a Transfer Student

```java
// In your JavaFX Controller
@FXML
private void handleCreateTransfer() {
    String apiUrl = "http://localhost:9590/api/transfer-students";

    Map<String, Object> requestData = new HashMap<>();
    requestData.put("studentFirstName", firstNameField.getText());
    requestData.put("studentLastName", lastNameField.getText());
    requestData.put("dateOfBirth", dobPicker.getValue().toString());
    requestData.put("transferDate", transferDatePicker.getValue().toString());
    requestData.put("previousSchoolName", schoolField.getText());
    requestData.put("createdByStaffId", currentUserId);

    // Use HttpClient or RestTemplate
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(apiUrl))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(
            new ObjectMapper().writeValueAsString(requestData)))
        .build();

    HttpResponse<String> response = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 201) {
        showSuccess("Transfer student created successfully!");
    }
}
```

### Backend Service Integration

#### Example: Using Services in Custom Logic

```java
@Service
public class EnrollmentProcessService {

    @Autowired
    private TransferStudentService transferService;

    @Autowired
    private EmailService emailService;

    public void processNewTransfer(Long transferId) {
        // Get transfer
        TransferStudent transfer = transferService.getTransferById(transferId);

        // Send welcome email
        emailService.sendWelcomeEmail(transfer);

        // Request records from previous school
        emailService.sendRecordsRequest(
            transfer.getPreviousSchoolEmail(),
            transfer.getTransferNumber()
        );

        // Update status
        transfer.setStatus(TransferStatus.RECORDS_REQUESTED);
        transferService.updateTransfer(transfer, getCurrentStaffId());
    }
}
```

---

## Testing Strategy

### Unit Tests
Test individual service methods in isolation.

```java
@Test
void testCreateTransferStudent() {
    TransferStudent transfer = transferStudentService.createTransferRecord(
        "John", "Doe", LocalDate.of(2010, 5, 15),
        LocalDate.now().plusDays(30), "Lincoln Elementary", 1L);

    assertThat(transfer).isNotNull();
    assertThat(transfer.getTransferNumber()).startsWith("TRN-");
    assertThat(transfer.getStatus()).isEqualTo(TransferStatus.DRAFT);
}
```

### Integration Tests
Test full workflows with database.

```java
@SpringBootTest
@Transactional
class TransferStudentServiceIntegrationTest {

    @Autowired
    private TransferStudentService service;

    @Test
    void testCompleteWorkflow() {
        // Create
        TransferStudent transfer = service.createTransferRecord(...);

        // Mark all records received
        for (String type : ALL_RECORD_TYPES) {
            service.markRecordReceived(transfer.getId(), type, 1L);
        }

        // Verify auto-transition
        TransferStudent updated = service.getTransferById(transfer.getId());
        assertThat(updated.getStatus())
            .isEqualTo(TransferStatus.ALL_RECORDS_RECEIVED);
    }
}
```

### API Tests
Test REST endpoints.

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TransferStudentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testCreateTransferStudent() {
        Map<String, Object> request = Map.of(
            "studentFirstName", "John",
            "studentLastName", "Doe",
            ...
        );

        ResponseEntity<TransferStudent> response =
            restTemplate.postForEntity(
                "/api/transfer-students", request, TransferStudent.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

---

## Deployment

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 14+ (production) or H2 (development)
- 2GB RAM minimum
- 1GB disk space

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
java -jar target/heronix-sis-1.0.0.jar
```

### Configuration

#### application.properties (Production)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/heronix_sis
spring.datasource.username=heronix
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# API
server.port=8080
spring.mvc.cors.allowed-origins=https://heronix.edu

# Logging
logging.level.com.heronix=INFO
logging.file.name=/var/log/heronix/sis.log
```

### Docker Deployment
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/heronix-sis-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

```bash
docker build -t heronix-sis:1.0.0 .
docker run -p 8080:8080 heronix-sis:1.0.0
```

---

## Performance Considerations

### Database Indexing
All services use indexed fields for optimal query performance:
- Unique indexes on number fields (e.g., `transfer_number`)
- Indexes on status fields for filtering
- Indexes on foreign keys (e.g., `student_id`)

### Caching Strategy
Consider implementing caching for frequently accessed data:

```java
@Service
public class TransferStudentService {

    @Cacheable(value = "transfers", key = "#id")
    public TransferStudent getTransferById(Long id) {
        return transferStudentRepository.findById(id)
            .orElseThrow(...);
    }

    @CacheEvict(value = "transfers", key = "#result.id")
    public TransferStudent updateTransfer(...) {
        // ...
    }
}
```

### Pagination
For list endpoints returning many records:

```java
@GetMapping
public Page<TransferStudent> getAll(Pageable pageable) {
    return transferStudentRepository.findAll(pageable);
}
```

---

## Support and Maintenance

### Logging
All services use SLF4J logging:

```java
@Slf4j
@Service
public class TransferStudentService {

    public TransferStudent createTransferRecord(...) {
        log.info("Creating transfer record for: {} {}", firstName, lastName);
        // ...
        log.info("Created transfer: {}", transfer.getTransferNumber());
        return transfer;
    }
}
```

### Monitoring
Key metrics to monitor:
- Transfer processing time (creation to completion)
- Re-enrollment approval rates
- Verification request volume by purpose
- Transfer-out acknowledgment delays

### Backup Strategy
- Daily database backups
- Retention: 30 days
- Document storage backups (generated PDFs)

---

## Changelog

### Version 1.0.0 (2025-12-23)
- Initial release
- Transfer Student Service
- Re-Enrollment Service
- Enrollment Verification Service
- Transfer Out Documentation Service
- REST API implementation
- Comprehensive documentation

---

**Document Version**: 1.0.0
**For Support**: support@heronix.edu
**Documentation**: https://docs.heronix.edu
