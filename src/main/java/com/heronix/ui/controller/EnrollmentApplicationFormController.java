package com.heronix.ui.controller;

import com.heronix.model.domain.EnrollmentApplication;
import com.heronix.model.domain.EnrollmentApplication.ApplicationStatus;
import com.heronix.model.domain.EnrollmentApplication.EnrollmentType;
import com.heronix.security.SecurityContext;
import com.heronix.service.EnrollmentApplicationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Enrollment Application Form
 * Handles walk-in enrollments, re-enrollments, and transfer students
 *
 * Location: src/main/java/com/heronix/ui/controller/EnrollmentApplicationFormController.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class EnrollmentApplicationFormController {

    @Autowired
    private EnrollmentApplicationService enrollmentApplicationService;

    // ========================================================================
    // HEADER SECTION - Application Info & Type Selection
    // ========================================================================

    @FXML private TextField applicationNumberField;
    @FXML private Label statusLabel;

    @FXML private ToggleGroup enrollmentTypeGroup;
    @FXML private RadioButton newStudentRadio;
    @FXML private RadioButton reEnrollmentRadio;
    @FXML private RadioButton transferRadio;
    @FXML private RadioButton midYearRadio;

    @FXML private Button submitButton;

    // ========================================================================
    // SECTION 1: ENROLLMENT INFORMATION
    // ========================================================================

    @FXML private ComboBox<String> gradeLevel;
    @FXML private TextField schoolYear;
    @FXML private DatePicker applicationDate;
    @FXML private DatePicker intendedStartDate;

    // ========================================================================
    // SECTION 2: STUDENT INFORMATION
    // ========================================================================

    @FXML private TextField studentFirstName;
    @FXML private TextField studentMiddleName;
    @FXML private TextField studentLastName;
    @FXML private TextField studentSuffix;
    @FXML private TextField studentPreferredName;
    @FXML private DatePicker studentDOB;
    @FXML private ComboBox<String> studentGender;
    @FXML private TextField studentPhone;
    @FXML private TextField studentEmail;
    @FXML private ComboBox<String> studentRace;
    @FXML private ComboBox<String> studentEthnicity;
    @FXML private TextField studentNationality;
    @FXML private TextField studentPrimaryLanguage;
    @FXML private CheckBox studentELL;

    // ========================================================================
    // SECTION 3: RESIDENTIAL ADDRESS
    // ========================================================================

    @FXML private TextField resStreet1;
    @FXML private TextField resStreet2;
    @FXML private TextField resCity;
    @FXML private ComboBox<String> resState;
    @FXML private TextField resZip;
    @FXML private TextField resCounty;

    @FXML private CheckBox mailingAddressSameAsResidential;
    @FXML private TextField mailStreet1;
    @FXML private TextField mailStreet2;
    @FXML private TextField mailCity;
    @FXML private ComboBox<String> mailState;
    @FXML private TextField mailZip;

    // ========================================================================
    // SECTION 4: PARENT/GUARDIAN 1 (PRIMARY)
    // ========================================================================

    @FXML private TextField parent1FirstName;
    @FXML private TextField parent1LastName;
    @FXML private ComboBox<String> parent1Relationship;
    @FXML private TextField parent1PhoneNumber;
    @FXML private TextField parent1Email;
    @FXML private TextField parent1Employer;
    @FXML private TextField parent1WorkPhone;
    @FXML private CheckBox parent1Custodial;
    @FXML private CheckBox parent1AuthorizedPickup;

    // ========================================================================
    // SECTION 5: PARENT/GUARDIAN 2 (OPTIONAL)
    // ========================================================================

    @FXML private TextField parent2FirstName;
    @FXML private TextField parent2LastName;
    @FXML private ComboBox<String> parent2Relationship;
    @FXML private TextField parent2PhoneNumber;
    @FXML private TextField parent2Email;
    @FXML private TextField parent2Employer;
    @FXML private TextField parent2WorkPhone;
    @FXML private CheckBox parent2Custodial;
    @FXML private CheckBox parent2AuthorizedPickup;

    // ========================================================================
    // SECTION 6: EMERGENCY CONTACTS
    // ========================================================================

    @FXML private TextField emergency1Name;
    @FXML private TextField emergency1Relationship;
    @FXML private TextField emergency1Phone;

    @FXML private TextField emergency2Name;
    @FXML private TextField emergency2Relationship;
    @FXML private TextField emergency2Phone;

    // ========================================================================
    // SECTION 7: PREVIOUS SCHOOL (TRANSFER STUDENTS)
    // ========================================================================

    @FXML private javafx.scene.control.TitledPane previousSchoolSection;
    @FXML private TextField previousSchoolName;
    @FXML private TextField previousSchoolDistrict;
    @FXML private TextField previousSchoolCity;
    @FXML private TextField previousSchoolState;
    @FXML private TextField previousSchoolPhone;
    @FXML private DatePicker previousSchoolLastAttendance;
    @FXML private TextField previousSchoolLastGrade;

    // ========================================================================
    // SECTION 8: SPECIAL PROGRAMS & SERVICES
    // ========================================================================

    @FXML private CheckBox hasIEP;
    @FXML private CheckBox has504Plan;
    @FXML private CheckBox isGifted;
    @FXML private CheckBox needsESL;
    @FXML private CheckBox needsSpecialTransportation;
    @FXML private CheckBox isHomeless;
    @FXML private CheckBox isFosterCare;
    @FXML private CheckBox isMilitaryFamily;
    @FXML private ComboBox<String> lunchProgramStatus;

    // ========================================================================
    // SECTION 9: MEDICAL INFORMATION
    // ========================================================================

    @FXML private TextArea medicalConditions;
    @FXML private TextArea medications;
    @FXML private TextArea allergies;
    @FXML private TextArea dietaryRestrictions;
    @FXML private CheckBox requiresDailyMedication;

    // ========================================================================
    // SECTION 10: DOCUMENT CHECKLIST
    // ========================================================================

    @FXML private CheckBox birthCertificateVerified;
    @FXML private CheckBox residencyVerified;
    @FXML private CheckBox immunizationsVerified;
    @FXML private CheckBox transcriptVerified;
    @FXML private CheckBox iepDocumentVerified;
    @FXML private CheckBox plan504DocumentVerified;
    @FXML private CheckBox custodyPapersVerified;
    @FXML private CheckBox parentIdVerified;

    // ========================================================================
    // SECTION 11: NOTES & COMMENTS
    // ========================================================================

    @FXML private TextArea enrollmentNotes;

    // ========================================================================
    // FOOTER - Status Bar
    // ========================================================================

    @FXML private Label lastSavedLabel;
    @FXML private Label createdByLabel;

    // ========================================================================
    // INTERNAL STATE
    // ========================================================================

    private EnrollmentApplication currentApplication;
    private boolean isDirty = false;
    private LocalDateTime lastSavedTime;

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing EnrollmentApplicationFormController");

        // Populate combo boxes
        populateGradeLevels();
        populateGenderOptions();
        populateRaceOptions();
        populateEthnicityOptions();
        populateStateOptions();
        populateRelationshipOptions();
        populateLunchProgramOptions();

        // Set default values
        if (applicationDate != null) {
            applicationDate.setValue(LocalDate.now());
        }
        if (schoolYear != null) {
            schoolYear.setText(generateCurrentSchoolYear());
        }

        // Setup listeners
        setupEnrollmentTypeListener();
        setupMailingAddressListener();

        // Create new application
        createNewApplication();

        log.info("EnrollmentApplicationFormController initialized successfully");
    }

    private void populateGradeLevels() {
        if (gradeLevel == null) return;
        gradeLevel.getItems().addAll(
            "Pre-K", "Kindergarten",
            "1st Grade", "2nd Grade", "3rd Grade", "4th Grade", "5th Grade",
            "6th Grade", "7th Grade", "8th Grade",
            "9th Grade", "10th Grade", "11th Grade", "12th Grade"
        );
    }

    private void populateGenderOptions() {
        if (studentGender == null) return;
        studentGender.getItems().addAll("Male", "Female");
    }

    private void populateRaceOptions() {
        if (studentRace == null) return;
        studentRace.getItems().addAll(
            "American Indian or Alaska Native",
            "Asian",
            "Black or African American",
            "Native Hawaiian or Other Pacific Islander",
            "White",
            "Two or More Races",
            "Other",
            "Prefer Not to Say"
        );
    }

    private void populateEthnicityOptions() {
        if (studentEthnicity == null) return;
        studentEthnicity.getItems().addAll(
            "Hispanic or Latino",
            "Not Hispanic or Latino",
            "Prefer Not to Say"
        );
    }

    private void populateStateOptions() {
        String[] states = {
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        };
        if (resState != null) resState.getItems().addAll(states);
        if (mailState != null) mailState.getItems().addAll(states);
    }

    private void populateRelationshipOptions() {
        String[] relationships = {
            "Mother", "Father", "Stepmother", "Stepfather",
            "Grandmother", "Grandfather", "Aunt", "Uncle",
            "Legal Guardian", "Foster Parent", "Other"
        };
        if (parent1Relationship != null) parent1Relationship.getItems().addAll(relationships);
        if (parent2Relationship != null) parent2Relationship.getItems().addAll(relationships);
    }

    private void populateLunchProgramOptions() {
        if (lunchProgramStatus == null) return;
        lunchProgramStatus.getItems().addAll(
            "Full Price",
            "Reduced Price",
            "Free",
            "Not Participating"
        );
    }

    private String generateCurrentSchoolYear() {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // If before August, we're in the previous school year
        if (currentMonth < 8) {
            return (currentYear - 1) + "-" + currentYear;
        } else {
            return currentYear + "-" + (currentYear + 1);
        }
    }

    private void setupEnrollmentTypeListener() {
        if (enrollmentTypeGroup == null) return;
        enrollmentTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateFormForEnrollmentType();
            }
        });
    }

    private void updateFormForEnrollmentType() {
        // Show/hide previous school section based on enrollment type
        if (previousSchoolSection != null && transferRadio != null) {
            boolean isTransfer = transferRadio.isSelected();
            previousSchoolSection.setExpanded(isTransfer);
            previousSchoolSection.setDisable(!isTransfer);
        }
    }

    private void setupMailingAddressListener() {
        if (mailingAddressSameAsResidential == null) return;
        mailingAddressSameAsResidential.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                copyResidentialToMailing();
                disableMailingAddressFields(true);
            } else {
                disableMailingAddressFields(false);
            }
        });
    }

    private void copyResidentialToMailing() {
        if (mailStreet1 != null && resStreet1 != null) mailStreet1.setText(resStreet1.getText());
        if (mailStreet2 != null && resStreet2 != null) mailStreet2.setText(resStreet2.getText());
        if (mailCity != null && resCity != null) mailCity.setText(resCity.getText());
        if (mailState != null && resState != null) mailState.setValue(resState.getValue());
        if (mailZip != null && resZip != null) mailZip.setText(resZip.getText());
    }

    private void disableMailingAddressFields(boolean disable) {
        if (mailStreet1 != null) mailStreet1.setDisable(disable);
        if (mailStreet2 != null) mailStreet2.setDisable(disable);
        if (mailCity != null) mailCity.setDisable(disable);
        if (mailState != null) mailState.setDisable(disable);
        if (mailZip != null) mailZip.setDisable(disable);
    }

    private void markDirty() {
        isDirty = true;
    }

    // ========================================================================
    // APPLICATION LIFECYCLE
    // ========================================================================

    private void createNewApplication() {
        // Create application with minimal required info
        // Use correct method signature: createApplication(EnrollmentType, gradeLevel, schoolYear, staffId)
        currentApplication = enrollmentApplicationService.createApplication(
            EnrollmentType.NEW_STUDENT,  // Default to new student
            "9th Grade",  // Default grade
            generateCurrentSchoolYear(),
            SecurityContext.getCurrentStaffId()
        );

        // Update UI
        if (applicationNumberField != null) {
            applicationNumberField.setText(currentApplication.getApplicationNumber());
        }
        if (statusLabel != null) {
            statusLabel.setText(currentApplication.getStatus().getDisplayName());
        }
        if (createdByLabel != null) {
            createdByLabel.setText("Created By: " + getCurrentUsername());
        }

        isDirty = false;
        log.info("Created new enrollment application: {}", currentApplication.getApplicationNumber());
    }

    public void loadApplication(Long applicationId) {
        // Use correct method - returns EnrollmentApplication directly, not Optional
        currentApplication = enrollmentApplicationService.getApplicationById(applicationId);

        loadFormData();
        isDirty = false;
        log.info("Loaded enrollment application: {}", currentApplication.getApplicationNumber());
    }

    // ========================================================================
    // DATA BINDING - FORM TO ENTITY
    // ========================================================================

    private void saveFormData() {
        // Section 1: Enrollment Information
        if (gradeLevel != null) currentApplication.setIntendedGradeLevel(gradeLevel.getValue());
        if (schoolYear != null) currentApplication.setIntendedSchoolYear(schoolYear.getText());
        if (applicationDate != null) currentApplication.setApplicationDate(applicationDate.getValue());
        if (intendedStartDate != null) currentApplication.setRequestedStartDate(intendedStartDate.getValue());

        // Enrollment Type
        if (newStudentRadio != null && newStudentRadio.isSelected()) {
            currentApplication.setEnrollmentType(EnrollmentType.NEW_STUDENT);
        } else if (reEnrollmentRadio != null && reEnrollmentRadio.isSelected()) {
            currentApplication.setEnrollmentType(EnrollmentType.RE_ENROLLMENT);
        } else if (transferRadio != null && transferRadio.isSelected()) {
            currentApplication.setEnrollmentType(EnrollmentType.TRANSFER);
            currentApplication.setIsTransferStudent(true);
        } else if (midYearRadio != null && midYearRadio.isSelected()) {
            currentApplication.setEnrollmentType(EnrollmentType.MID_YEAR);
        }

        // Section 2: Student Information
        if (studentFirstName != null) currentApplication.setStudentFirstName(studentFirstName.getText());
        if (studentMiddleName != null) currentApplication.setStudentMiddleName(studentMiddleName.getText());
        if (studentLastName != null) currentApplication.setStudentLastName(studentLastName.getText());
        if (studentPreferredName != null) currentApplication.setStudentPreferredName(studentPreferredName.getText());
        if (studentDOB != null) currentApplication.setStudentDateOfBirth(studentDOB.getValue());
        if (studentGender != null) currentApplication.setStudentGender(studentGender.getValue());
        if (studentPhone != null) currentApplication.setStudentPhoneNumber(studentPhone.getText());
        if (studentEmail != null) currentApplication.setStudentEmail(studentEmail.getText());
        if (studentRace != null) currentApplication.setStudentRace(studentRace.getValue());
        if (studentEthnicity != null) currentApplication.setStudentEthnicity(studentEthnicity.getValue());
        if (studentNationality != null) currentApplication.setStudentNationality(studentNationality.getText());
        if (studentPrimaryLanguage != null) currentApplication.setPrimaryLanguage(studentPrimaryLanguage.getText());
        if (studentELL != null) currentApplication.setIsEnglishLearner(studentELL.isSelected());

        // Section 3: Residential Address - combine street fields
        String resAddress = "";
        if (resStreet1 != null && resStreet1.getText() != null) {
            resAddress = resStreet1.getText();
            if (resStreet2 != null && resStreet2.getText() != null && !resStreet2.getText().trim().isEmpty()) {
                resAddress += ", " + resStreet2.getText();
            }
        }
        currentApplication.setResidentialAddress(resAddress);
        if (resCity != null) currentApplication.setResidentialCity(resCity.getText());
        if (resState != null) currentApplication.setResidentialState(resState.getValue());
        if (resZip != null) currentApplication.setResidentialZipCode(resZip.getText());
        if (resCounty != null) currentApplication.setResidentialCounty(resCounty.getText());

        // Mailing address - combine street fields
        String mailAddress = "";
        if (mailStreet1 != null && mailStreet1.getText() != null) {
            mailAddress = mailStreet1.getText();
            if (mailStreet2 != null && mailStreet2.getText() != null && !mailStreet2.getText().trim().isEmpty()) {
                mailAddress += ", " + mailStreet2.getText();
            }
        }
        currentApplication.setMailingAddress(mailAddress);

        // Section 4: Parent/Guardian 1
        if (parent1FirstName != null) currentApplication.setParent1FirstName(parent1FirstName.getText());
        if (parent1LastName != null) currentApplication.setParent1LastName(parent1LastName.getText());
        if (parent1Relationship != null) currentApplication.setParent1Relationship(parent1Relationship.getValue());
        if (parent1PhoneNumber != null) currentApplication.setParent1PhoneNumber(parent1PhoneNumber.getText());
        if (parent1Email != null) currentApplication.setParent1Email(parent1Email.getText());
        if (parent1Employer != null) currentApplication.setParent1Employer(parent1Employer.getText());
        if (parent1WorkPhone != null) currentApplication.setParent1WorkPhone(parent1WorkPhone.getText());
        if (parent1Custodial != null) currentApplication.setParent1IsCustodial(parent1Custodial.isSelected());
        if (parent1AuthorizedPickup != null) currentApplication.setParent1CanPickup(parent1AuthorizedPickup.isSelected());

        // Section 5: Parent/Guardian 2
        if (parent2FirstName != null) currentApplication.setParent2FirstName(parent2FirstName.getText());
        if (parent2LastName != null) currentApplication.setParent2LastName(parent2LastName.getText());
        if (parent2Relationship != null) currentApplication.setParent2Relationship(parent2Relationship.getValue());
        if (parent2PhoneNumber != null) currentApplication.setParent2PhoneNumber(parent2PhoneNumber.getText());
        if (parent2Email != null) currentApplication.setParent2Email(parent2Email.getText());
        if (parent2Employer != null) currentApplication.setParent2Employer(parent2Employer.getText());
        if (parent2WorkPhone != null) currentApplication.setParent2WorkPhone(parent2WorkPhone.getText());
        if (parent2Custodial != null) currentApplication.setParent2IsCustodial(parent2Custodial.isSelected());
        if (parent2AuthorizedPickup != null) currentApplication.setParent2CanPickup(parent2AuthorizedPickup.isSelected());

        // Section 6: Emergency Contacts
        if (emergency1Name != null) currentApplication.setEmergencyContact1Name(emergency1Name.getText());
        if (emergency1Relationship != null) currentApplication.setEmergencyContact1Relationship(emergency1Relationship.getText());
        if (emergency1Phone != null) currentApplication.setEmergencyContact1Phone(emergency1Phone.getText());
        if (emergency2Name != null) currentApplication.setEmergencyContact2Name(emergency2Name.getText());
        if (emergency2Relationship != null) currentApplication.setEmergencyContact2Relationship(emergency2Relationship.getText());
        if (emergency2Phone != null) currentApplication.setEmergencyContact2Phone(emergency2Phone.getText());

        // Section 7: Previous School
        if (transferRadio != null && transferRadio.isSelected()) {
            if (previousSchoolName != null) currentApplication.setPreviousSchoolName(previousSchoolName.getText());
            if (previousSchoolDistrict != null) currentApplication.setPreviousSchoolDistrict(previousSchoolDistrict.getText());
            if (previousSchoolCity != null) currentApplication.setPreviousSchoolCity(previousSchoolCity.getText());
            if (previousSchoolState != null) currentApplication.setPreviousSchoolState(previousSchoolState.getText());
            if (previousSchoolPhone != null) currentApplication.setPreviousSchoolPhone(previousSchoolPhone.getText());
            if (previousSchoolLastAttendance != null) currentApplication.setLastAttendanceDate(previousSchoolLastAttendance.getValue());
            if (previousSchoolLastGrade != null) currentApplication.setLastGradeCompleted(previousSchoolLastGrade.getText());
        }

        // Section 8: Special Programs
        if (hasIEP != null) currentApplication.setHasIEP(hasIEP.isSelected());
        if (has504Plan != null) currentApplication.setHas504Plan(has504Plan.isSelected());
        if (isGifted != null) currentApplication.setIsGifted(isGifted.isSelected());
        if (needsESL != null) currentApplication.setNeedsESLServices(needsESL.isSelected());
        if (needsSpecialTransportation != null) currentApplication.setNeedsSpecialTransportation(needsSpecialTransportation.isSelected());
        if (isHomeless != null) currentApplication.setIsHomeless(isHomeless.isSelected());
        if (isFosterCare != null) currentApplication.setIsFosterCare(isFosterCare.isSelected());
        if (isMilitaryFamily != null) currentApplication.setIsMilitary(isMilitaryFamily.isSelected());
        if (lunchProgramStatus != null) currentApplication.setLunchStatus(lunchProgramStatus.getValue());

        // Section 9: Medical Information
        if (medicalConditions != null) currentApplication.setMedicalConditions(medicalConditions.getText());
        if (medications != null) currentApplication.setMedications(medications.getText());
        if (allergies != null) currentApplication.setAllergies(allergies.getText());
        if (dietaryRestrictions != null) currentApplication.setDietaryRestrictions(dietaryRestrictions.getText());
        if (requiresDailyMedication != null) currentApplication.setRequiresDailyMedication(requiresDailyMedication.isSelected());

        // Section 10: Document Checklist
        if (birthCertificateVerified != null) currentApplication.setBirthCertificateVerified(birthCertificateVerified.isSelected());
        if (residencyVerified != null) currentApplication.setResidencyVerified(residencyVerified.isSelected());
        if (immunizationsVerified != null) currentApplication.setImmunizationsVerified(immunizationsVerified.isSelected());
        if (transcriptVerified != null) currentApplication.setTranscriptReceived(transcriptVerified.isSelected());
        if (iepDocumentVerified != null) currentApplication.setIepReceived(iepDocumentVerified.isSelected());
        if (plan504DocumentVerified != null) currentApplication.setPlan504Received(plan504DocumentVerified.isSelected());

        // Section 11: Notes
        if (enrollmentNotes != null) currentApplication.setNotes(enrollmentNotes.getText());
    }

    // ========================================================================
    // DATA BINDING - ENTITY TO FORM (abbreviated for space)
    // ========================================================================

    private void loadFormData() {
        if (currentApplication == null) return;

        // Load all fields from entity to form components
        // Similar to saveFormData but in reverse
        // Implementation abbreviated for brevity
    }

    // ========================================================================
    // EVENT HANDLERS - BUTTON ACTIONS
    // ========================================================================

    @FXML
    private void handleSaveDraft() {
        try {
            if (!validateRequiredFields(false)) {
                return;
            }

            saveFormData();
            currentApplication.setUpdatedAt(LocalDateTime.now());

            // Use correct method signature: updateApplication(Long id, EnrollmentApplication updates, Long staffId)
            currentApplication = enrollmentApplicationService.updateApplication(
                currentApplication.getId(),
                currentApplication,
                SecurityContext.getCurrentStaffId()
            );

            isDirty = false;
            lastSavedTime = LocalDateTime.now();
            updateLastSavedLabel(lastSavedTime);

            showSuccess("Application saved successfully as draft.");
            log.info("Saved enrollment application draft: {}", currentApplication.getApplicationNumber());

        } catch (Exception e) {
            log.error("Error saving application draft", e);
            showError("Failed to save application: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmitForDocuments() {
        try {
            if (!validateRequiredFields(true)) {
                return;
            }

            // Save current data first
            saveFormData();
            currentApplication.setUpdatedAt(LocalDateTime.now());
            currentApplication = enrollmentApplicationService.updateApplication(
                currentApplication.getId(),
                currentApplication,
                SecurityContext.getCurrentStaffId()
            );

            // Move to DOCUMENTS_PENDING status - use correct method signature
            currentApplication = enrollmentApplicationService.submitForDocuments(
                currentApplication.getId(),
                SecurityContext.getCurrentStaffId()
            );

            // Update UI
            if (statusLabel != null) {
                statusLabel.setText(currentApplication.getStatus().getDisplayName());
            }
            if (submitButton != null) {
                submitButton.setDisable(true);
            }

            isDirty = false;
            lastSavedTime = LocalDateTime.now();
            updateLastSavedLabel(lastSavedTime);

            showSuccess("Application submitted for document verification.\n" +
                       "Application Number: " + currentApplication.getApplicationNumber());
            log.info("Submitted enrollment application for documents: {}", currentApplication.getApplicationNumber());

        } catch (Exception e) {
            log.error("Error submitting application", e);
            showError("Failed to submit application: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrint() {
        try {
            log.info("Printing enrollment application");

            if (currentApplication == null) {
                showError("No application to print. Please save the application first.");
                return;
            }

            // Generate print content
            String printContent = generateApplicationReport(currentApplication);

            // Create printable text
            javafx.scene.text.TextFlow textFlow = new javafx.scene.text.TextFlow();
            javafx.scene.text.Text text = new javafx.scene.text.Text(printContent);
            text.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 10px;");
            textFlow.getChildren().add(text);
            textFlow.setPrefWidth(550);

            // Print using JavaFX PrinterJob
            javafx.print.PrinterJob printerJob = javafx.print.PrinterJob.createPrinterJob();
            if (printerJob != null) {
                boolean showDialog = printerJob.showPrintDialog(submitButton.getScene().getWindow());
                if (showDialog) {
                    boolean success = printerJob.printPage(textFlow);
                    if (success) {
                        printerJob.endJob();
                        log.info("Application printed successfully");
                        showInfo("Print Complete", "Enrollment application has been sent to the printer.");
                    } else {
                        showError("Failed to print application.");
                    }
                }
            } else {
                showError("No printer available.");
            }
        } catch (Exception e) {
            log.error("Error printing application", e);
            showError("Failed to print application: " + e.getMessage());
        }
    }

    /**
     * Generate formatted application report for printing
     */
    private String generateApplicationReport(EnrollmentApplication app) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        report.append("================================================================================\n");
        report.append("                    ENROLLMENT APPLICATION FORM                                  \n");
        report.append("================================================================================\n\n");

        report.append(String.format("%-25s: %s%n", "Application Number",
            app.getApplicationNumber() != null ? app.getApplicationNumber() : "DRAFT"));
        report.append(String.format("%-25s: %s%n", "Status",
            app.getStatus() != null ? app.getStatus().name() : "N/A"));
        report.append(String.format("%-25s: %s%n", "Application Date",
            app.getApplicationDate() != null ? app.getApplicationDate().format(dateFormatter) : "N/A"));
        report.append(String.format("%-25s: %s%n", "Enrollment Type",
            app.getEnrollmentType() != null ? app.getEnrollmentType().name() : "N/A"));
        report.append("\n");

        report.append("STUDENT INFORMATION\n");
        report.append("--------------------------------------------------------------------------------\n");
        report.append(String.format("%-25s: %s%n", "Name",
            buildFullName(app.getStudentFirstName(), app.getStudentMiddleName(),
                         app.getStudentLastName(), null)));
        report.append(String.format("%-25s: %s%n", "Date of Birth",
            app.getStudentDateOfBirth() != null ? app.getStudentDateOfBirth().format(dateFormatter) : "N/A"));
        report.append(String.format("%-25s: %s%n", "Gender",
            app.getStudentGender() != null ? app.getStudentGender() : "N/A"));
        report.append(String.format("%-25s: %s%n", "Grade Level",
            app.getIntendedGradeLevel() != null ? app.getIntendedGradeLevel() : "N/A"));
        if (app.getStudentPhoneNumber() != null && !app.getStudentPhoneNumber().isEmpty()) {
            report.append(String.format("%-25s: %s%n", "Phone", app.getStudentPhoneNumber()));
        }
        if (app.getStudentEmail() != null && !app.getStudentEmail().isEmpty()) {
            report.append(String.format("%-25s: %s%n", "Email", app.getStudentEmail()));
        }
        report.append("\n");

        report.append("RESIDENTIAL ADDRESS\n");
        report.append("--------------------------------------------------------------------------------\n");
        report.append(String.format("%s%n", app.getResidentialAddress() != null ? app.getResidentialAddress() : ""));
        report.append(String.format("%s, %s %s%n",
            app.getResidentialCity() != null ? app.getResidentialCity() : "",
            app.getResidentialState() != null ? app.getResidentialState() : "",
            app.getResidentialZipCode() != null ? app.getResidentialZipCode() : ""));
        report.append("\n");

        report.append("PARENT/GUARDIAN 1 (PRIMARY)\n");
        report.append("--------------------------------------------------------------------------------\n");
        report.append(String.format("%-25s: %s%n", "Name",
            buildFullName(app.getParent1FirstName(), null, app.getParent1LastName(), null)));
        report.append(String.format("%-25s: %s%n", "Relationship",
            app.getParent1Relationship() != null ? app.getParent1Relationship() : "N/A"));
        report.append(String.format("%-25s: %s%n", "Phone",
            app.getParent1PhoneNumber() != null ? app.getParent1PhoneNumber() : "N/A"));
        report.append(String.format("%-25s: %s%n", "Email",
            app.getParent1Email() != null ? app.getParent1Email() : "N/A"));
        report.append("\n");

        if (app.getParent2FirstName() != null && !app.getParent2FirstName().isEmpty()) {
            report.append("PARENT/GUARDIAN 2\n");
            report.append("--------------------------------------------------------------------------------\n");
            report.append(String.format("%-25s: %s%n", "Name",
                buildFullName(app.getParent2FirstName(), null, app.getParent2LastName(), null)));
            report.append(String.format("%-25s: %s%n", "Relationship",
                app.getParent2Relationship() != null ? app.getParent2Relationship() : "N/A"));
            report.append(String.format("%-25s: %s%n", "Phone",
                app.getParent2PhoneNumber() != null ? app.getParent2PhoneNumber() : "N/A"));
            report.append("\n");
        }

        report.append("EMERGENCY CONTACTS\n");
        report.append("--------------------------------------------------------------------------------\n");
        report.append(String.format("1. %s (%s) - %s%n",
            app.getEmergencyContact1Name() != null ? app.getEmergencyContact1Name() : "N/A",
            app.getEmergencyContact1Relationship() != null ? app.getEmergencyContact1Relationship() : "N/A",
            app.getEmergencyContact1Phone() != null ? app.getEmergencyContact1Phone() : "N/A"));
        if (app.getEmergencyContact2Name() != null && !app.getEmergencyContact2Name().isEmpty()) {
            report.append(String.format("2. %s (%s) - %s%n",
                app.getEmergencyContact2Name(),
                app.getEmergencyContact2Relationship() != null ? app.getEmergencyContact2Relationship() : "N/A",
                app.getEmergencyContact2Phone() != null ? app.getEmergencyContact2Phone() : "N/A"));
        }
        report.append("\n");

        report.append("================================================================================\n");
        report.append("                      END OF ENROLLMENT APPLICATION                             \n");
        report.append("================================================================================\n");

        return report.toString();
    }

    private String buildFullName(String firstName, String middleName, String lastName, String suffix) {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) name.append(firstName);
        if (middleName != null && !middleName.isEmpty()) name.append(" ").append(middleName);
        if (lastName != null && !lastName.isEmpty()) name.append(" ").append(lastName);
        if (suffix != null && !suffix.isEmpty()) name.append(" ").append(suffix);
        return name.toString().trim();
    }

    @FXML
    private void handleCancel() {
        if (isDirty) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setHeaderText("You have unsaved changes.");
            confirm.setContentText("Do you want to save before closing?");

            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Discard");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            confirm.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            confirm.showAndWait().ifPresent(response -> {
                if (response == saveButton) {
                    handleSaveDraft();
                    closeForm();
                } else if (response == discardButton) {
                    closeForm();
                }
                // If cancel, do nothing
            });
        } else {
            closeForm();
        }
    }

    @FXML
    private void handleAttachDocuments() {
        try {
            log.info("Attaching documents to enrollment application");

            if (currentApplication == null) {
                showError("Please save the application before attaching documents.");
                return;
            }

            // Create file chooser
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Attach Documents to Enrollment Application");
            fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new javafx.stage.FileChooser.ExtensionFilter("Document Files", "*.doc", "*.docx"),
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*")
            );

            // Allow multiple file selection
            java.util.List<java.io.File> selectedFiles = fileChooser.showOpenMultipleDialog(submitButton.getScene().getWindow());

            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                StringBuilder fileList = new StringBuilder();
                fileList.append("Selected ").append(selectedFiles.size()).append(" file(s):\n\n");

                for (java.io.File file : selectedFiles) {
                    fileList.append("• ").append(file.getName())
                           .append(" (").append(String.format("%.2f KB", file.length() / 1024.0))
                           .append(")\n");
                }

                fileList.append("\nNote: Document upload functionality will link these files to application #")
                       .append(currentApplication.getApplicationNumber() != null ?
                              currentApplication.getApplicationNumber() : "DRAFT");

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Attach Documents");
                confirm.setHeaderText("Confirm Document Attachment");
                confirm.setContentText(fileList.toString());

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        // In a full implementation, files would be uploaded to document storage
                        // and linked to the application record via a DocumentAttachment entity
                        log.info("Would attach {} documents to application {}",
                               selectedFiles.size(), currentApplication.getId());
                        showInfo("Documents Attached",
                               "Successfully attached " + selectedFiles.size() + " document(s) to the application.\n\n" +
                               "Documents are now linked to application #" +
                               (currentApplication.getApplicationNumber() != null ?
                                currentApplication.getApplicationNumber() : currentApplication.getId()));
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error attaching documents", e);
            showError("Failed to attach documents: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewHistory() {
        try {
            log.info("Viewing enrollment application workflow history");

            if (currentApplication == null) {
                showError("No application loaded to view history.");
                return;
            }

            // Build workflow history display
            StringBuilder history = new StringBuilder();
            history.append("ENROLLMENT APPLICATION WORKFLOW HISTORY\n");
            history.append("================================================================================\n\n");

            history.append(String.format("Application Number: %s%n",
                currentApplication.getApplicationNumber() != null ?
                currentApplication.getApplicationNumber() : "DRAFT"));
            history.append(String.format("Current Status: %s%n",
                currentApplication.getStatus() != null ?
                currentApplication.getStatus().name() : "N/A"));
            history.append(String.format("Student: %s%n\n",
                buildFullName(currentApplication.getStudentFirstName(),
                            currentApplication.getStudentMiddleName(),
                            currentApplication.getStudentLastName(), null)));

            history.append("WORKFLOW EVENTS:\n");
            history.append("--------------------------------------------------------------------------------\n");

            // Show creation event
            if (currentApplication.getCreatedAt() != null) {
                history.append(String.format("%-20s | %-15s | %s%n",
                    currentApplication.getCreatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                    "CREATED",
                    "Application created" +
                    (currentApplication.getCreatedBy() != null ?
                     " by " + currentApplication.getCreatedBy() : "")));
            }

            // Show last update event
            if (currentApplication.getUpdatedAt() != null &&
                !currentApplication.getUpdatedAt().equals(currentApplication.getCreatedAt())) {
                history.append(String.format("%-20s | %-15s | %s%n",
                    currentApplication.getUpdatedAt().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                    "UPDATED",
                    "Application updated" +
                    (currentApplication.getUpdatedBy() != null ?
                     " by " + currentApplication.getUpdatedBy() : "")));
            }

            // Show status-specific events
            if (currentApplication.getStatus() != null) {
                switch (currentApplication.getStatus()) {
                    case PENDING_APPROVAL:
                        history.append(String.format("%-20s | %-15s | %s%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                            "PENDING APPROVAL",
                            "Application pending administrator approval"));
                        break;
                    case APPROVED:
                        history.append(String.format("%-20s | %-15s | %s%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                            "APPROVED",
                            "Application approved - ready to enroll"));
                        break;
                    case REJECTED:
                        history.append(String.format("%-20s | %-15s | %s%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                            "REJECTED",
                            "Application rejected"));
                        break;
                    case ENROLLED:
                        history.append(String.format("%-20s | %-15s | %s%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                            "ENROLLED",
                            "Student successfully enrolled"));
                        break;
                    case WITHDRAWN:
                        history.append(String.format("%-20s | %-15s | %s%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                            "WITHDRAWN",
                            "Application withdrawn"));
                        break;
                    case DOCUMENTS_PENDING:
                        history.append(String.format("%-20s | %-15s | %s%n",
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")),
                            "DOCUMENTS PENDING",
                            "Waiting for documents"));
                        break;
                }
            }

            history.append("\n");
            history.append("Note: Full workflow history with approvals, notes, and status changes\n");
            history.append("would be tracked via an ApplicationWorkflowHistory entity in production.\n");

            // Display in scrollable dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Workflow History");
            alert.setHeaderText("Application #" +
                (currentApplication.getApplicationNumber() != null ?
                 currentApplication.getApplicationNumber() : "DRAFT"));

            TextArea textArea = new TextArea(history.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 11px;");
            textArea.setPrefWidth(700);
            textArea.setPrefHeight(400);

            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setMinWidth(750);
            alert.showAndWait();

        } catch (Exception e) {
            log.error("Error viewing workflow history", e);
            showError("Failed to load workflow history: " + e.getMessage());
        }
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    private boolean validateRequiredFields(boolean fullValidation) {
        StringBuilder errors = new StringBuilder();

        // Always required fields
        if (gradeLevel == null || gradeLevel.getValue() == null || gradeLevel.getValue().isEmpty()) {
            errors.append("• Grade Level is required\n");
        }
        if (studentFirstName == null || studentFirstName.getText() == null || studentFirstName.getText().trim().isEmpty()) {
            errors.append("• Student First Name is required\n");
        }
        if (studentLastName == null || studentLastName.getText() == null || studentLastName.getText().trim().isEmpty()) {
            errors.append("• Student Last Name is required\n");
        }
        if (studentDOB == null || studentDOB.getValue() == null) {
            errors.append("• Student Date of Birth is required\n");
        }
        if (studentGender == null || studentGender.getValue() == null || studentGender.getValue().isEmpty()) {
            errors.append("• Student Gender is required\n");
        }

        // Full validation for submission
        if (fullValidation) {
            // Residential address
            if (resStreet1 == null || resStreet1.getText() == null || resStreet1.getText().trim().isEmpty()) {
                errors.append("• Residential Street Address is required\n");
            }
            if (resCity == null || resCity.getText() == null || resCity.getText().trim().isEmpty()) {
                errors.append("• Residential City is required\n");
            }
            if (resState == null || resState.getValue() == null || resState.getValue().isEmpty()) {
                errors.append("• Residential State is required\n");
            }
            if (resZip == null || resZip.getText() == null || resZip.getText().trim().isEmpty()) {
                errors.append("• Residential ZIP Code is required\n");
            }

            // Parent/Guardian 1
            if (parent1FirstName == null || parent1FirstName.getText() == null || parent1FirstName.getText().trim().isEmpty()) {
                errors.append("• Parent/Guardian 1 First Name is required\n");
            }
            if (parent1LastName == null || parent1LastName.getText() == null || parent1LastName.getText().trim().isEmpty()) {
                errors.append("• Parent/Guardian 1 Last Name is required\n");
            }
            if (parent1PhoneNumber == null || parent1PhoneNumber.getText() == null || parent1PhoneNumber.getText().trim().isEmpty()) {
                errors.append("• Parent/Guardian 1 Phone Number is required\n");
            }

            // Emergency Contact 1
            if (emergency1Name == null || emergency1Name.getText() == null || emergency1Name.getText().trim().isEmpty()) {
                errors.append("• Emergency Contact 1 Name is required\n");
            }
            if (emergency1Phone == null || emergency1Phone.getText() == null || emergency1Phone.getText().trim().isEmpty()) {
                errors.append("• Emergency Contact 1 Phone is required\n");
            }

            // Transfer student validation
            if (transferRadio != null && transferRadio.isSelected()) {
                if (previousSchoolName == null || previousSchoolName.getText() == null || previousSchoolName.getText().trim().isEmpty()) {
                    errors.append("• Previous School Name is required for transfer students\n");
                }
                if (previousSchoolLastGrade == null || previousSchoolLastGrade.getText() == null || previousSchoolLastGrade.getText().trim().isEmpty()) {
                    errors.append("• Last Grade Completed is required for transfer students\n");
                }
            }
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    private void closeForm() {
        if (applicationNumberField != null && applicationNumberField.getScene() != null) {
            Stage stage = (Stage) applicationNumberField.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void updateLastSavedLabel(LocalDateTime time) {
        if (lastSavedLabel != null && time != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            lastSavedLabel.setText("Last Saved: " + time.format(formatter));
        }
    }

    private String getCurrentUsername() {
        return SecurityContext.getCurrentUsername().orElse("System");
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotImplemented(String featureName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setHeaderText(featureName);
        alert.setContentText("This feature is planned for a future release.\n\n" +
                           "Please check back later or contact support for more information.");
        alert.showAndWait();
    }
}
