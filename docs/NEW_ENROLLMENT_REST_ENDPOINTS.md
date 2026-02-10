# New Enrollment REST API Endpoints

## Overview
Three new REST controllers have been added to complete the enrollment services REST API. All enrollment services now have full REST API support.

**Created:** 2025-12-24
**Author:** Heronix SIS Team

---

## 1. Enrollment Application API

**Base URL:** `/api/enrollment-applications`
**Controller:** [EnrollmentApplicationController.java](../src/main/java/com/heronix/controller/EnrollmentApplicationController.java)

### Create Operations

#### Create New Application
```http
POST /api/enrollment-applications
Content-Type: application/json

{
  "enrollmentType": "NEW_STUDENT",
  "intendedGradeLevel": "9",
  "intendedSchoolYear": "2025-2026",
  "createdByStaffId": 1
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "applicationNumber": "2025-000001",
  "status": "DRAFT",
  "enrollmentType": "NEW_STUDENT",
  "intendedGradeLevel": "9",
  "intendedSchoolYear": "2025-2026",
  ...
}
```

### Read Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/enrollment-applications/{id}` | GET | Get application by ID |
| `/api/enrollment-applications/number/{applicationNumber}` | GET | Get application by number |
| `/api/enrollment-applications/status/{status}` | GET | Get applications by status |
| `/api/enrollment-applications/ready-for-approval` | GET | Get applications ready for approval |
| `/api/enrollment-applications/awaiting-documents` | GET | Get applications awaiting documents |
| `/api/enrollment-applications/search?name={name}` | GET | Search by student/parent name |
| `/api/enrollment-applications/statistics?schoolYear={year}` | GET | Get enrollment statistics |

### Workflow Operations

#### Submit for Documents
```http
POST /api/enrollment-applications/{id}/submit-for-documents?staffId=1
```

#### Move to Verification
```http
POST /api/enrollment-applications/{id}/move-to-verification?staffId=1
```

#### Request Previous School Records
```http
POST /api/enrollment-applications/{id}/request-records?staffId=1
```

#### Submit for Approval
```http
POST /api/enrollment-applications/{id}/submit-for-approval?staffId=1
```

#### Approve Application
```http
POST /api/enrollment-applications/{id}/approve?adminStaffId=1&approvalNotes=Approved
```

#### Reject Application
```http
POST /api/enrollment-applications/{id}/reject?adminStaffId=1&rejectionReason=Missing documents
```

#### Withdraw Application
```http
POST /api/enrollment-applications/{id}/withdraw?staffId=1&reason=Family moved
```

#### Enroll Student (Create Student Account)
```http
POST /api/enrollment-applications/{id}/enroll?staffId=1
```

**Response:** `201 Created` with Student object

### Update Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/enrollment-applications/{id}?staffId={staffId}` | PUT | Update application details |

---

## 2. Pre-Registration API

**Base URL:** `/api/pre-registrations`
**Controller:** [PreRegistrationController.java](../src/main/java/com/heronix/controller/PreRegistrationController.java)

### Create Operations

#### Create New Pre-Registration
```http
POST /api/pre-registrations
Content-Type: application/json

{
  "studentId": 123,
  "targetSchoolYear": "2025-2026",
  "createdByStaffId": 1
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "registrationNumber": "PRE-2025-000001",
  "status": "DRAFT",
  "targetSchoolYear": "2025-2026",
  "student": {...},
  "currentGrade": "9",
  "nextGrade": "10",
  ...
}
```

### Read Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/pre-registrations/{id}` | GET | Get pre-registration by ID |
| `/api/pre-registrations/number/{registrationNumber}` | GET | Get by registration number |
| `/api/pre-registrations/status/{status}` | GET | Get by status |
| `/api/pre-registrations/student/{studentId}` | GET | Get all for student |
| `/api/pre-registrations/school-year/{year}` | GET | Get all for school year |
| `/api/pre-registrations/awaiting-review` | GET | Get awaiting review |
| `/api/pre-registrations/search?name={name}` | GET | Search by student name |
| `/api/pre-registrations/statistics?schoolYear={year}` | GET | Get statistics |
| `/api/pre-registrations/counts-by-grade?schoolYear={year}` | GET | Get counts by grade |

### Workflow Operations

#### Submit for Review
```http
POST /api/pre-registrations/{id}/submit?staffId=1
```

#### Begin Review
```http
POST /api/pre-registrations/{id}/begin-review?staffId=1
```

#### Approve Pre-Registration
```http
POST /api/pre-registrations/{id}/approve?staffId=1&approvalNotes=Approved for 2025-2026
```

#### Confirm Enrollment
```http
POST /api/pre-registrations/{id}/confirm?staffId=1
```

#### Cancel Pre-Registration
```http
POST /api/pre-registrations/{id}/cancel?staffId=1&reason=Family relocating
```

#### Move to Waitlist
```http
POST /api/pre-registrations/{id}/waitlist?staffId=1
```

### Update Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/pre-registrations/{id}?staffId={staffId}` | PUT | Update pre-registration |

### Delete Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/pre-registrations/{id}` | DELETE | Delete draft pre-registration |

---

## 3. Withdrawal API

**Base URL:** `/api/withdrawals`
**Controller:** [WithdrawalController.java](../src/main/java/com/heronix/controller/WithdrawalController.java)

### Create Operations

#### Create New Withdrawal
```http
POST /api/withdrawals
Content-Type: application/json

{
  "studentId": 123,
  "withdrawalDate": "2025-03-15",
  "createdByStaffId": 1
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "withdrawalNumber": "WD-2025-000001",
  "status": "DRAFT",
  "student": {...},
  "withdrawalDate": "2025-03-15",
  "totalClearanceItems": 24,
  "clearedItems": 0,
  "allCleared": false,
  ...
}
```

### Read Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/withdrawals/{id}` | GET | Get withdrawal by ID |
| `/api/withdrawals/number/{withdrawalNumber}` | GET | Get by withdrawal number |
| `/api/withdrawals/status/{status}` | GET | Get by status |
| `/api/withdrawals/student/{studentId}` | GET | Get all for student |
| `/api/withdrawals/student/{studentId}/recent` | GET | Get most recent for student |
| `/api/withdrawals/pending` | GET | Get pending withdrawals |
| `/api/withdrawals/in-clearance` | GET | Get in clearance process |
| `/api/withdrawals/needing-attention` | GET | Get needing attention (7+ days old) |
| `/api/withdrawals/search?name={name}` | GET | Search by student name |
| `/api/withdrawals/statistics` | GET | Get withdrawal statistics |
| `/api/withdrawals/counts-by-type` | GET | Get counts by withdrawal type |
| `/api/withdrawals/counts-by-month?startDate={date}&endDate={date}` | GET | Get counts by month |

### Workflow Operations

#### Start Clearance Process
```http
POST /api/withdrawals/{id}/start-clearance?staffId=1
```

#### Begin Clearance Process
```http
POST /api/withdrawals/{id}/begin-clearance?staffId=1
```

#### Mark as Cleared
```http
POST /api/withdrawals/{id}/mark-cleared?staffId=1
```

#### Complete Withdrawal
```http
POST /api/withdrawals/{id}/complete?finalStatus=GRADUATED&staffId=1
```

**Final Status Options:**
- `TRANSFERRED` - Student transferred to another school
- `GRADUATED` - Student graduated
- `DROPPED_OUT` - Student dropped out
- `EXPELLED` - Student was expelled
- `MOVED` - Family moved away
- `HOMESCHOOL` - Switched to homeschooling
- `OTHER` - Other reason

#### Cancel Withdrawal
```http
POST /api/withdrawals/{id}/cancel?reason=Student returning&staffId=1
```

### Clearance Operations

#### Check All Clearance Items (Bulk)
```http
POST /api/withdrawals/{id}/check-all-clearance?staffId=1
```

#### Update Specific Clearance Item
```http
POST /api/withdrawals/{id}/clearance-item?itemName=libraryBooksReturned&checked=true&staffId=1
```

**Clearance Item Names:**
- Academic: `finalGradesRecorded`, `transcriptPrinted`, `iep504Finalized`, `progressReportsSent`
- Library: `libraryBooksReturned`, `textbooksReturned`, `libraryFinesPaid`
- Devices: `devicesReturned`, `athleticEquipmentReturned`, `instrumentsReturned`
- Facilities: `lockerCleared`, `lockerLockReturned`, `parkingPermitReturned`, `idCardReturned`
- Financial: `tuitionPaid`, `cafeteriaBalanceSettled`, `activityFeesPaid`, `damageFeesPaid`
- Administrative: `recordsReleaseSigned`, `immunizationsCopied`, `paperworkCompleted`, `parentNotificationSent`, `withdrawalFormSigned`, `finalTranscriptRequested`

### Update Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/withdrawals/{id}?staffId={staffId}` | PUT | Update withdrawal record |

### Delete Operations

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/withdrawals/{id}` | DELETE | Delete draft withdrawal |

---

## Complete Enrollment Services REST API Summary

All 7 enrollment services now have full REST API support:

| Service | Base URL | Status |
|---------|----------|--------|
| Transfer Student | `/api/transfer-students` | ✅ Available |
| Re-Enrollment | `/api/re-enrollments` | ✅ Available |
| Enrollment Verification | `/api/enrollment-verifications` | ✅ Available |
| Transfer Out Documentation | `/api/transfer-out` | ✅ Available |
| **Enrollment Application** | `/api/enrollment-applications` | ✅ **NEW** |
| **Pre-Registration** | `/api/pre-registrations` | ✅ **NEW** |
| **Withdrawal** | `/api/withdrawals` | ✅ **NEW** |

---

## Error Handling

All endpoints follow standard HTTP status codes:

- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST creation
- `204 No Content` - Successful DELETE
- `400 Bad Request` - Invalid request data or business rule violation
- `404 Not Found` - Resource not found

Error responses include descriptive messages in the logs.

---

## Next Steps

1. **OpenAPI/Swagger Documentation** - All endpoints are automatically documented via SpringDoc OpenAPI
2. **Access Documentation** - Visit `http://localhost:9590/swagger-ui.html` when application is running
3. **API Testing** - Use Postman, curl, or the Swagger UI to test endpoints
4. **Integration** - Frontend applications can now integrate with all enrollment services

---

## Example Integration Flow

### Complete Enrollment Application Workflow

```bash
# 1. Create draft application
curl -X POST http://localhost:9590/api/enrollment-applications \
  -H "Content-Type: application/json" \
  -d '{
    "enrollmentType": "NEW_STUDENT",
    "intendedGradeLevel": "9",
    "intendedSchoolYear": "2025-2026",
    "createdByStaffId": 1
  }'

# 2. Update with student information
curl -X PUT http://localhost:9590/api/enrollment-applications/1?staffId=1 \
  -H "Content-Type: application/json" \
  -d '{
    "studentFirstName": "John",
    "studentLastName": "Doe",
    "studentDateOfBirth": "2010-05-15",
    ...
  }'

# 3. Submit for documents
curl -X POST http://localhost:9590/api/enrollment-applications/1/submit-for-documents?staffId=1

# 4. Move to verification
curl -X POST http://localhost:9590/api/enrollment-applications/1/move-to-verification?staffId=1

# 5. Submit for approval
curl -X POST http://localhost:9590/api/enrollment-applications/1/submit-for-approval?staffId=1

# 6. Approve application
curl -X POST "http://localhost:9590/api/enrollment-applications/1/approve?adminStaffId=1&approvalNotes=Approved"

# 7. Enroll student (create Student account)
curl -X POST http://localhost:9590/api/enrollment-applications/1/enroll?staffId=1
```

---

**For detailed service implementation details, see:**
- [ENROLLMENT_SERVICES_API.md](ENROLLMENT_SERVICES_API.md)
- [ENROLLMENT_SERVICES_IMPLEMENTATION_GUIDE.md](ENROLLMENT_SERVICES_IMPLEMENTATION_GUIDE.md)
