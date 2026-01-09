# Enrollment Services REST API Documentation

## Overview

This document provides comprehensive documentation for the Heronix SIS Enrollment Services REST API. These services handle all enrollment-related operations including transfer students, re-enrollments, enrollment verifications, and transfer-out documentation.

**Base URL**: `http://localhost:8080/api`

**Version**: 1.0.0

**Last Updated**: December 23, 2025

---

## Table of Contents

1. [Transfer Student Service](#1-transfer-student-service)
2. [Re-Enrollment Service](#2-re-enrollment-service)
3. [Enrollment Verification Service](#3-enrollment-verification-service)
4. [Transfer Out Documentation Service](#4-transfer-out-documentation-service)
5. [Common Response Codes](#common-response-codes)
6. [Error Handling](#error-handling)

---

## 1. Transfer Student Service

Manages incoming transfer students from other schools.

### Base Endpoint
`/api/transfer-students`

### Entities

#### TransferStudent
```json
{
  "id": 1,
  "transferNumber": "TRN-2025-001234",
  "status": "DRAFT",
  "transferDate": "2025-03-15",
  "studentFirstName": "John",
  "studentLastName": "Doe",
  "dateOfBirth": "2010-05-15",
  "previousSchoolName": "Lincoln Elementary",
  "previousSchoolDistrict": "Springfield USD",
  "totalRecordsExpected": 7,
  "recordsReceived": 0,
  "allRecordsReceived": false,
  "transcriptReceived": false,
  "immunizationRecordsReceived": false,
  "iepReceived": false,
  "createdAt": "2025-12-23T10:30:00",
  "createdBy": { "id": 1, "username": "staff" }
}
```

#### Status Workflow
```
DRAFT → RECORDS_REQUESTED → PARTIAL_RECORDS → ALL_RECORDS_RECEIVED →
COUNSELOR_ASSIGNED → PLACEMENT_TESTING → READY_FOR_PLACEMENT → ENROLLED → COMPLETED
```

### API Endpoints

#### Create Transfer Student
```http
POST /api/transfer-students
Content-Type: application/json

{
  "studentFirstName": "John",
  "studentLastName": "Doe",
  "dateOfBirth": "2010-05-15",
  "transferDate": "2025-03-15",
  "previousSchoolName": "Lincoln Elementary",
  "createdByStaffId": 1
}
```

**Response**: `201 Created`
```json
{
  "id": 1,
  "transferNumber": "TRN-2025-001234",
  "status": "DRAFT",
  ...
}
```

#### Get Transfer Student by ID
```http
GET /api/transfer-students/{id}
```

**Response**: `200 OK`

#### Get by Transfer Number
```http
GET /api/transfer-students/number/{transferNumber}
```

**Response**: `200 OK`

#### Get by Status
```http
GET /api/transfer-students/status/{status}
```

**Valid Statuses**: `DRAFT`, `RECORDS_REQUESTED`, `PARTIAL_RECORDS`, `ALL_RECORDS_RECEIVED`, `COUNSELOR_ASSIGNED`, `PLACEMENT_TESTING`, `READY_FOR_PLACEMENT`, `ENROLLED`, `COMPLETED`, `CANCELLED`

**Response**: `200 OK`
```json
[
  { "id": 1, "transferNumber": "TRN-2025-001234", ... },
  { "id": 2, "transferNumber": "TRN-2025-001235", ... }
]
```

#### Get Pending Records
```http
GET /api/transfer-students/pending-records
```

Returns all transfers awaiting records from previous school.

**Response**: `200 OK`

#### Get Ready for Placement
```http
GET /api/transfer-students/ready-for-placement
```

Returns all transfers ready for grade-level placement.

**Response**: `200 OK`

#### Get by Counselor
```http
GET /api/transfer-students/counselor/{counselorId}
```

**Response**: `200 OK`

#### Search by Student Name
```http
GET /api/transfer-students/search?name={searchTerm}
```

**Response**: `200 OK`

#### Update Transfer Student
```http
PUT /api/transfer-students/{id}?updatedByStaffId=1
Content-Type: application/json

{
  "id": 1,
  "previousSchoolName": "Updated School Name",
  ...
}
```

**Response**: `200 OK`

#### Mark Record as Received
```http
POST /api/transfer-students/{id}/records/{recordType}?updatedByStaffId=1
```

**Valid Record Types**: `transcript`, `immunization`, `iep`, `504`, `discipline`, `attendance`, `testscores`

**Response**: `200 OK`

#### Assign Counselor
```http
POST /api/transfer-students/{id}/assign-counselor?counselorId=5&updatedByStaffId=1
```

**Response**: `200 OK`

#### Propose Grade Level
```http
POST /api/transfer-students/{id}/propose-grade?proposedGradeLevel=10th%20Grade&updatedByStaffId=1
```

**Response**: `200 OK`

#### Complete Enrollment
```http
POST /api/transfer-students/{id}/complete
  ?enrolledStudentId=100
  &assignedGrade=10th%20Grade
  &assignedHomeroom=Room%20205
  &completedByStaffId=1
```

**Response**: `200 OK`

#### Cancel Transfer
```http
POST /api/transfer-students/{id}/cancel?reason=Family%20moved%20out%20of%20district&cancelledByStaffId=1
```

**Response**: `200 OK`

#### Delete Transfer (Draft Only)
```http
DELETE /api/transfer-students/{id}
```

**Response**: `204 No Content`

#### Get Statistics
```http
GET /api/transfer-students/statistics
```

**Response**: `200 OK`
```json
{
  "total": 150,
  "draft": 25,
  "recordsRequested": 40,
  "allRecordsReceived": 35,
  "enrolled": 45,
  "completed": 5,
  "cancelled": 0,
  "averageRecordsCompletion": 0.75,
  "averageProcessingDays": 14.5
}
```

---

## 2. Re-Enrollment Service

Manages students re-enrolling after previously withdrawing.

### Base Endpoint
`/api/re-enrollments`

### Entities

#### ReEnrollment
```json
{
  "id": 1,
  "reEnrollmentNumber": "REE-2025-001234",
  "status": "DRAFT",
  "applicationDate": "2025-12-23",
  "student": { "id": 50, "firstName": "Jane", "lastName": "Smith" },
  "requestedGradeLevel": "11th Grade",
  "intendedEnrollmentDate": "2026-01-15",
  "previousWithdrawalDate": "2024-05-20",
  "monthsAway": 7,
  "reason": "PERSONAL_CIRCUMSTANCES",
  "counselorDecision": null,
  "principalDecision": null,
  "hasOutstandingFees": true,
  "outstandingFeesAmount": 150.00,
  "feesPaid": false
}
```

#### Status Workflow
```
DRAFT → PENDING_REVIEW → PENDING_APPROVAL → APPROVED → ENROLLED
       ↘ REJECTED
```

#### Approval Decisions
- `APPROVED` - Approved for re-enrollment
- `CONDITIONAL` - Approved with conditions
- `DENIED` - Not approved

### API Endpoints

#### Create Re-Enrollment
```http
POST /api/re-enrollments
Content-Type: application/json

{
  "studentId": 50,
  "requestedGradeLevel": "11th Grade",
  "intendedEnrollmentDate": "2026-01-15",
  "createdByStaffId": 1
}
```

**Response**: `201 Created`

#### Get Re-Enrollment by ID
```http
GET /api/re-enrollments/{id}
```

**Response**: `200 OK`

#### Get by Re-Enrollment Number
```http
GET /api/re-enrollments/number/{reEnrollmentNumber}
```

**Response**: `200 OK`

#### Get by Status
```http
GET /api/re-enrollments/status/{status}
```

**Valid Statuses**: `DRAFT`, `PENDING_REVIEW`, `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `ENROLLED`, `CANCELLED`

**Response**: `200 OK`

#### Get Pending Review
```http
GET /api/re-enrollments/pending-review
```

**Response**: `200 OK`

#### Get by Counselor
```http
GET /api/re-enrollments/counselor/{counselorId}
```

**Response**: `200 OK`

#### Update Re-Enrollment
```http
PUT /api/re-enrollments/{id}?updatedByStaffId=1
Content-Type: application/json

{
  "id": 1,
  "requestedGradeLevel": "12th Grade",
  ...
}
```

**Response**: `200 OK`

#### Assign Counselor
```http
POST /api/re-enrollments/{id}/assign-counselor?counselorId=5&updatedByStaffId=1
```

**Response**: `200 OK`

#### Counselor Decision
```http
POST /api/re-enrollments/{id}/counselor-decision
  ?decision=APPROVED
  &notes=Student%20ready%20to%20return
  &counselorId=5
```

**Valid Decisions**: `APPROVED`, `CONDITIONAL`, `DENIED`

**Response**: `200 OK`

#### Principal Decision
```http
POST /api/re-enrollments/{id}/principal-decision
  ?decision=APPROVED
  &notes=Final%20approval%20granted
  &principalId=3
```

**Response**: `200 OK`

#### Record Fee Payment
```http
POST /api/re-enrollments/{id}/record-fee-payment?updatedByStaffId=1
```

**Response**: `200 OK`

#### Complete Re-Enrollment
```http
POST /api/re-enrollments/{id}/complete
  ?enrolledStudentId=50
  &completedByStaffId=1
```

**Response**: `200 OK`

#### Cancel Re-Enrollment
```http
POST /api/re-enrollments/{id}/cancel
  ?reason=Student%20enrolled%20elsewhere
  &cancelledByStaffId=1
```

**Response**: `200 OK`

#### Delete Re-Enrollment (Draft Only)
```http
DELETE /api/re-enrollments/{id}
```

**Response**: `204 No Content`

#### Get Statistics
```http
GET /api/re-enrollments/statistics
```

**Response**: `200 OK`
```json
{
  "total": 85,
  "draft": 15,
  "pendingReview": 20,
  "pendingApproval": 18,
  "approved": 25,
  "enrolled": 5,
  "rejected": 2,
  "cancelled": 0,
  "conditionalApprovals": 3,
  "averageMonthsAway": 8.5
}
```

#### Get Count by Reason
```http
GET /api/re-enrollments/count-by-reason
```

**Response**: `200 OK`
```json
[
  ["PERSONAL_CIRCUMSTANCES", 35],
  ["HEALTH_ISSUES", 20],
  ["FAMILY_RELOCATION", 15],
  ...
]
```

---

## 3. Enrollment Verification Service

Manages enrollment verification requests (proof of enrollment letters/certificates).

### Base Endpoint
`/api/enrollment-verifications`

### Entities

#### EnrollmentVerification
```json
{
  "id": 1,
  "verificationNumber": "ENV-2025-001234",
  "status": "DRAFT",
  "requestDate": "2025-12-23",
  "student": { "id": 75, "firstName": "Alice", "lastName": "Johnson" },
  "purpose": "STUDENT_LOAN",
  "documentType": "ENROLLMENT_LETTER",
  "urgentRequest": false,
  "fullTimeEnrollment": true,
  "academicGoodStanding": true,
  "includeGPA": true,
  "currentGPA": 3.75,
  "officialSeal": true,
  "verificationFee": 5.00,
  "totalFee": 5.00,
  "feePaid": false,
  "validityDays": 90
}
```

#### Status Workflow
```
DRAFT → PENDING_VERIFICATION → VERIFIED → GENERATED → DELIVERED
       ↘ CANCELLED
```

#### Verification Purposes
- `COLLEGE_APPLICATION`
- `STUDENT_LOAN`
- `VISA_IMMIGRATION`
- `INSURANCE`
- `EMPLOYMENT`
- `GOVERNMENT_BENEFIT`
- `ATHLETIC_ELIGIBILITY`
- `OTHER`

### API Endpoints

#### Create Verification
```http
POST /api/enrollment-verifications
Content-Type: application/json

{
  "studentId": 75,
  "purpose": "STUDENT_LOAN",
  "createdByStaffId": 1
}
```

**Response**: `201 Created`

#### Get Verification by ID
```http
GET /api/enrollment-verifications/{id}
```

**Response**: `200 OK`

#### Get by Verification Number
```http
GET /api/enrollment-verifications/number/{verificationNumber}
```

**Response**: `200 OK`

#### Get by Status
```http
GET /api/enrollment-verifications/status/{status}
```

**Valid Statuses**: `DRAFT`, `PENDING_VERIFICATION`, `VERIFIED`, `GENERATED`, `DELIVERED`, `CANCELLED`

**Response**: `200 OK`

#### Get by Purpose
```http
GET /api/enrollment-verifications/purpose/{purpose}
```

**Response**: `200 OK`

#### Get Pending Verification
```http
GET /api/enrollment-verifications/pending-verification
```

**Response**: `200 OK`

#### Get Urgent Verifications
```http
GET /api/enrollment-verifications/urgent
```

**Response**: `200 OK`

#### Get Unpaid Fees
```http
GET /api/enrollment-verifications/unpaid-fees
```

**Response**: `200 OK`

#### Get by Student
```http
GET /api/enrollment-verifications/student/{studentId}
```

**Response**: `200 OK`

#### Update Verification
```http
PUT /api/enrollment-verifications/{id}?updatedByStaffId=1
Content-Type: application/json

{
  "id": 1,
  "urgentRequest": true,
  ...
}
```

**Response**: `200 OK`

#### Verify Enrollment
```http
POST /api/enrollment-verifications/{id}/verify
  ?fullTimeEnrollment=true
  &academicGoodStanding=true
  &currentGPA=3.75
  &verifiedByStaffId=1
```

**Response**: `200 OK`

#### Generate Document
```http
POST /api/enrollment-verifications/{id}/generate
  ?documentPath=/documents/ENV-2025-001234.pdf
  &generatedByStaffId=1
```

**Response**: `200 OK`

#### Record Fee Payment
```http
POST /api/enrollment-verifications/{id}/record-payment?updatedByStaffId=1
```

**Response**: `200 OK`

#### Send Verification
```http
POST /api/enrollment-verifications/{id}/send
  ?deliveryMethod=Email
  &trackingNumber=TRACK123
  &sentByStaffId=1
```

**Response**: `200 OK`

#### Mark as Delivered
```http
POST /api/enrollment-verifications/{id}/mark-delivered
  ?trackingNumber=TRACK123
  &updatedByStaffId=1
```

**Response**: `200 OK`

#### Cancel Verification
```http
POST /api/enrollment-verifications/{id}/cancel
  ?reason=Request%20withdrawn
  &cancelledByStaffId=1
```

**Response**: `200 OK`

#### Delete Verification (Draft Only)
```http
DELETE /api/enrollment-verifications/{id}
```

**Response**: `204 No Content`

#### Get Statistics
```http
GET /api/enrollment-verifications/statistics
```

**Response**: `200 OK`
```json
{
  "total": 420,
  "draft": 45,
  "pendingVerification": 75,
  "verified": 120,
  "generated": 90,
  "delivered": 85,
  "cancelled": 5,
  "urgent": 12,
  "unpaidFees": 30
}
```

#### Get Count by Purpose
```http
GET /api/enrollment-verifications/count-by-purpose
```

**Response**: `200 OK`
```json
[
  ["STUDENT_LOAN", 180],
  ["COLLEGE_APPLICATION", 125],
  ["INSURANCE", 65],
  ...
]
```

---

## 4. Transfer Out Documentation Service

Manages outgoing transfer student records to other schools.

### Base Endpoint
`/api/transfer-out`

### Entities

#### TransferOutDocumentation
```json
{
  "id": 1,
  "transferOutNumber": "TRO-2025-001234",
  "status": "DRAFT",
  "requestDate": "2025-12-23",
  "student": { "id": 60, "firstName": "Bob", "lastName": "Williams" },
  "destinationSchoolName": "Jefferson High School",
  "destinationSchoolCity": "Springfield",
  "destinationSchoolState": "CA",
  "expectedTransferDate": "2026-01-15",
  "totalDocumentsIncluded": 12,
  "documentsPackaged": 0,
  "allDocumentsPackaged": false,
  "requiresParentConsent": true,
  "parentConsentObtained": false,
  "hasOutstandingFees": false,
  "destinationAcknowledged": false
}
```

#### Status Workflow
```
DRAFT → PENDING_WITHDRAWAL → RECORDS_PREPARATION → PENDING_CONSENT →
PENDING_FEES → READY_TO_SEND → SENT → ACKNOWLEDGED → COMPLETED
```

#### Transmission Methods
- `US_MAIL`
- `CERTIFIED_MAIL`
- `COURIER`
- `FAX`
- `EMAIL`
- `ELECTRONIC_TRANSCRIPT`
- `PARCHMENT`
- `NAVIANCE`
- `HAND_DELIVERY`
- `PICKUP`

### API Endpoints

#### Create Transfer Out
```http
POST /api/transfer-out
Content-Type: application/json

{
  "studentId": 60,
  "destinationSchoolName": "Jefferson High School",
  "expectedTransferDate": "2026-01-15",
  "createdByStaffId": 1
}
```

**Response**: `201 Created`

#### Get Transfer Out by ID
```http
GET /api/transfer-out/{id}
```

**Response**: `200 OK`

#### Get by Transfer Out Number
```http
GET /api/transfer-out/number/{transferOutNumber}
```

**Response**: `200 OK`

#### Get by Status
```http
GET /api/transfer-out/status/{status}
```

**Valid Statuses**: `DRAFT`, `PENDING_WITHDRAWAL`, `RECORDS_PREPARATION`, `PENDING_CONSENT`, `PENDING_FEES`, `READY_TO_SEND`, `SENT`, `ACKNOWLEDGED`, `COMPLETED`, `CANCELLED`

**Response**: `200 OK`

#### Get Ready to Send
```http
GET /api/transfer-out/ready-to-send
```

**Response**: `200 OK`

#### Get Pending Acknowledgment
```http
GET /api/transfer-out/pending-acknowledgment
```

**Response**: `200 OK`

#### Get by Assigned Staff
```http
GET /api/transfer-out/staff/{staffId}
```

**Response**: `200 OK`

#### Get Unassigned
```http
GET /api/transfer-out/unassigned
```

**Response**: `200 OK`

#### Search by Destination School
```http
GET /api/transfer-out/search?school={schoolName}
```

**Response**: `200 OK`

#### Get Needing Attention
```http
GET /api/transfer-out/needs-attention
```

Returns transfers with delayed processing or pending follow-up.

**Response**: `200 OK`

#### Update Transfer Out
```http
PUT /api/transfer-out/{id}?updatedByStaffId=1
Content-Type: application/json

{
  "id": 1,
  "destinationSchoolCity": "Updated City",
  ...
}
```

**Response**: `200 OK`

#### Start Records Preparation
```http
POST /api/transfer-out/{id}/start-records-prep?updatedByStaffId=1
```

**Response**: `200 OK`

#### Mark Document as Included
```http
POST /api/transfer-out/{id}/include-document/{documentType}?updatedByStaffId=1
```

**Valid Document Types**: `transcript`, `immunization`, `iep`, `504`, `discipline`, `attendance`, `testscores`, `health`, `specialed`, `counseling`, `athletic`, `cumulative`

**Response**: `200 OK`

#### Include All Documents
```http
POST /api/transfer-out/{id}/include-all-documents?updatedByStaffId=1
```

**Response**: `200 OK`

#### Assign Staff
```http
POST /api/transfer-out/{id}/assign-staff?staffId=5&updatedByStaffId=1
```

**Response**: `200 OK`

#### Record Parent Consent
```http
POST /api/transfer-out/{id}/record-consent?updatedByStaffId=1
```

**Response**: `200 OK`

#### Record Fee Payment
```http
POST /api/transfer-out/{id}/record-payment?updatedByStaffId=1
```

**Response**: `200 OK`

#### Send Records
```http
POST /api/transfer-out/{id}/send
  ?method=CERTIFIED_MAIL
  &trackingNumber=USPS9876543210
  &sentByStaffId=1
```

**Response**: `200 OK`

#### Record Acknowledgment
```http
POST /api/transfer-out/{id}/acknowledge
  ?acknowledgedBy=Jane%20Smith%20-%20Registrar
  &acknowledgmentMethod=Email
  &updatedByStaffId=1
```

**Response**: `200 OK`

#### Complete Transfer Out
```http
POST /api/transfer-out/{id}/complete?completedByStaffId=1
```

**Response**: `200 OK`

#### Cancel Transfer Out
```http
POST /api/transfer-out/{id}/cancel
  ?reason=Student%20not%20transferring
  &cancelledByStaffId=1
```

**Response**: `200 OK`

#### Add Follow-Up Note
```http
POST /api/transfer-out/{id}/follow-up
  ?note=Called%20destination%20school%20-%20they%20received%20records
  &updatedByStaffId=1
```

**Response**: `200 OK`

#### Delete Transfer Out (Draft Only)
```http
DELETE /api/transfer-out/{id}
```

**Response**: `204 No Content`

#### Get Statistics
```http
GET /api/transfer-out/statistics
```

**Response**: `200 OK`
```json
{
  "total": 95,
  "draft": 12,
  "recordsPreparation": 18,
  "readyToSend": 8,
  "sent": 25,
  "acknowledged": 20,
  "completed": 10,
  "cancelled": 2,
  "midYearTransfers": 35,
  "internationalTransfers": 3,
  "averageDocumentsCompletion": 0.82,
  "averageProcessingDays": 9.5
}
```

#### Get Count by Destination School
```http
GET /api/transfer-out/count-by-school
```

**Response**: `200 OK`
```json
[
  ["Jefferson High School", 15],
  ["Lincoln Middle School", 12],
  ["Roosevelt Elementary", 8],
  ...
]
```

---

## Common Response Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Deletion successful |
| 400 | Bad Request - Invalid request data |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Business rule violation (e.g., can't delete non-draft record) |
| 500 | Internal Server Error |

---

## Error Handling

All endpoints return standardized error responses:

```json
{
  "timestamp": "2025-12-23T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Transfer record not found: 99999",
  "path": "/api/transfer-students/99999"
}
```

### Common Error Scenarios

1. **Resource Not Found**
   ```json
   {
     "message": "Transfer record not found: 99999"
   }
   ```

2. **Business Rule Violation**
   ```json
   {
     "message": "Student already has a pending transfer"
   }
   ```

3. **Invalid State Transition**
   ```json
   {
     "message": "Cannot cancel completed transfer out"
   }
   ```

4. **Missing Required Data**
   ```json
   {
     "message": "Counselor decision required before principal review"
   }
   ```

---

## Best Practices

### 1. Always Check Status Before State Transitions
Before calling workflow transition endpoints (e.g., complete, cancel), verify the current status allows the transition.

### 2. Handle 409 Conflicts Gracefully
Conflict responses indicate business rule violations. Display helpful messages to users.

### 3. Use Proper HTTP Methods
- `GET` for retrieving data
- `POST` for creating and state transitions
- `PUT` for updates
- `DELETE` for deletions

### 4. Include Staff ID in All Mutations
All create, update, and state transition operations require a staff ID for audit trail.

### 5. Pagination for List Endpoints
Consider implementing pagination for endpoints returning lists of records in production.

---

## Support

For API support or questions:
- **Email**: support@heronix.edu
- **Documentation**: https://docs.heronix.edu/api
- **Issue Tracker**: https://github.com/heronix/sis/issues

---

**Document Version**: 1.0.0
**Generated**: December 23, 2025
**API Version**: 1.0.0
