# Enrollment Forms - Dropdown Fields Reference

## EnrollmentApplicationForm.fxml

### Gender (studentGender)
- Male
- Female
- Non-Binary
- Prefer Not to Say

### Grade Level (gradeLevel)
- Pre-K
- Kindergarten
- 1st Grade through 12th Grade

### Race (studentRace)
- American Indian or Alaska Native
- Asian
- Black or African American
- Native Hawaiian or Other Pacific Islander
- White
- Two or More Races

### Ethnicity (studentEthnicity)
- Hispanic or Latino
- Not Hispanic or Latino

### US States (resState, mailState)
All 50 US state abbreviations (AL, AK, AZ, ... WY)

### Parent Relationship (parent1Relationship, parent2Relationship)
- Mother
- Father
- Grandmother
- Grandfather
- Stepmother
- Stepfather
- Legal Guardian
- Foster Parent
- Aunt
- Uncle
- Other Relative
- Other

### Lunch Program (lunchProgramStatus)
- Free
- Reduced
- Paid
- Not Participating

## ReEnrollmentForm.fxml

### Grade Levels (requestedGradeLevel, assignedGradeLevel, previousGradeLevel)
- Kindergarten
- 1st Grade through 12th Grade

### Withdrawal Reasons (previousWithdrawalReason)
- TRANSFERRED
- MOVED
- HOMESCHOOL
- PRIVATE_SCHOOL
- EXPELLED
- DROPPED_OUT
- GRADUATED
- OTHER

## TransferOutDocumentationForm.fxml

### US States (destinationState)
All 50 US state abbreviations

### Transfer Reasons (transferReason)
- FAMILY_RELOCATION
- PARENT_CHOICE
- ACADEMIC_REASONS
- DISCIPLINARY_REASONS
- SPECIAL_SERVICES_NEEDED
- OTHER

### Transmission Methods (transmissionMethod)
- EMAIL
- FAX
- CERTIFIED_MAIL
- REGULAR_MAIL
- HAND_DELIVERY
- ELECTRONIC_TRANSCRIPT_SYSTEM

### Acknowledgment Methods (acknowledgmentMethod)
- EMAIL
- PHONE
- FAX
- MAIL
- IN_PERSON

## EnrollmentVerificationForm.fxml

### Requester Types (requesterType)
- STUDENT
- PARENT_GUARDIAN
- SCHOOL
- GOVERNMENT_AGENCY
- INSURANCE_COMPANY
- EMPLOYER
- OTHER

### Verification Purpose (purpose)
- COLLEGE_APPLICATION
- SCHOLARSHIP
- STUDENT_LOAN
- INSURANCE
- VISA_IMMIGRATION
- EMPLOYMENT
- OTHER

### Delivery Methods (deliveryMethod)
- EMAIL
- POSTAL_MAIL
- PICKUP
- FAX

## Database Field Lengths

All forms should respect these maximum field lengths from the database schema:

- **Gender**: 20 characters
- **Grade Level**: 10-20 characters
- **Race**: 50 characters
- **Ethnicity**: 50 characters
- **State**: 2-50 characters (varies by field)
- **Names**: 100 characters
- **Email**: 100 characters
- **Phone**: 20 characters
- **Addresses**: 200 characters
- **Notes/Text Areas**: 1000-2000 characters

## Implementation Status

✅ All dropdown fields are populated in controller initialize() methods
✅ All values are compatible with database schema
✅ All enums match between UI and domain models
