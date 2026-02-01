package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentParentRelationship;
import com.heronix.model.domain.WithdrawalRecord;
import com.heronix.model.domain.WithdrawalRecord.WithdrawalStatus;
import com.heronix.model.domain.WithdrawalRecord.FinalStatus;
import com.heronix.repository.StudentParentRelationshipRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.security.SecurityContext;
import com.heronix.service.WithdrawalService;
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
 * Controller for Student Withdrawal Processing Form
 * Handles withdrawal, exit interviews, clearance, and transfer-out documentation
 *
 * Location: src/main/java/com/heronix/ui/controller/StudentWithdrawalFormController.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class StudentWithdrawalFormController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private StudentParentRelationshipRepository parentRelationshipRepository;

    private WithdrawalRecord currentWithdrawal;

    // Header
    @FXML private TextField withdrawalNumberField;
    @FXML private Label statusLabel;
    @FXML private Button completeButton;

    // Section 1: Student Info
    @FXML private TextField studentSearchField;
    @FXML private TextField studentNameField;
    @FXML private TextField studentIdField;
    @FXML private TextField currentGradeField;
    @FXML private TextField currentStatusField;
    @FXML private TextField enrollmentDateField;
    @FXML private DatePicker lastAttendanceDatePicker;

    // Section 2: Withdrawal Details
    @FXML private DatePicker withdrawalDatePicker;
    @FXML private ComboBox<String> withdrawalTypeComboBox;
    @FXML private ComboBox<String> withdrawalReasonComboBox;
    @FXML private TextArea withdrawalDetailsArea;
    @FXML private CheckBox isTransferringCheckbox;

    // Section 3: Receiving School
    @FXML private TitledPane receivingSchoolSection;
    @FXML private TextField receivingSchoolNameField;
    @FXML private TextField receivingSchoolDistrictField;
    @FXML private TextField receivingSchoolLocationField;
    @FXML private TextField receivingSchoolPhoneField;
    @FXML private TextField receivingSchoolEmailField;
    @FXML private DatePicker expectedStartDatePicker;
    @FXML private TextField recordsRequestedByField;

    // Section 4: Exit Interview
    @FXML private DatePicker interviewDatePicker;
    @FXML private TextField interviewedByField;
    @FXML private CheckBox parentPresentCheckbox;
    @FXML private CheckBox studentPresentCheckbox;
    @FXML private ComboBox<String> satisfactionRatingComboBox;
    @FXML private TextArea primaryFactorsArea;
    @FXML private TextArea academicFeedbackArea;
    @FXML private TextArea socialFeedbackArea;
    @FXML private TextArea improvementSuggestionsArea;
    @FXML private ToggleGroup recommendGroup;
    @FXML private RadioButton recommendYesRadio;
    @FXML private RadioButton recommendNoRadio;
    @FXML private RadioButton recommendMaybeRadio;
    @FXML private TextArea exitInterviewNotesArea;

    // Section 5: Clearance Checklist
    @FXML private CheckBox finalGradesCheckbox;
    @FXML private CheckBox transcriptPrintedCheckbox;
    @FXML private CheckBox iep504FinalizedCheckbox;
    @FXML private CheckBox progressReportsSentCheckbox;
    @FXML private CheckBox libraryBooksCheckbox;
    @FXML private CheckBox textbooksCheckbox;
    @FXML private CheckBox libraryFinesCheckbox;
    @FXML private CheckBox devicesReturnedCheckbox;
    @FXML private CheckBox athleticEquipmentCheckbox;
    @FXML private CheckBox instrumentsCheckbox;
    @FXML private CheckBox lockerClearedCheckbox;
    @FXML private CheckBox lockerLockCheckbox;
    @FXML private CheckBox parkingPermitCheckbox;
    @FXML private CheckBox idCardCheckbox;
    @FXML private CheckBox tuitionPaidCheckbox;
    @FXML private CheckBox cafeteriaBalanceCheckbox;
    @FXML private CheckBox activityFeesCheckbox;
    @FXML private CheckBox damageFeesCheckbox;
    @FXML private CheckBox recordsReleaseSignedCheckbox;
    @FXML private CheckBox immunizationsCopiedCheckbox;
    @FXML private CheckBox paperworkCompletedCheckbox;
    @FXML private CheckBox parentNotificationCheckbox;
    @FXML private Label clearanceStatusLabel;

    // Section 6: Parent Confirmation
    @FXML private TextField parentNameField;
    @FXML private TextField parentPhoneField;
    @FXML private TextField parentEmailField;
    @FXML private CheckBox acknowledgeWithdrawalCheckbox;
    @FXML private CheckBox acknowledgeClearanceCheckbox;
    @FXML private CheckBox acknowledgeRecordsCheckbox;
    @FXML private CheckBox acknowledgeNoRefundCheckbox;
    @FXML private TextField parentSignatureField;
    @FXML private TextField signatureDateField;

    // Section 7: Administrative
    @FXML private TextArea administrativeNotesArea;
    @FXML private TextField processedByField;
    @FXML private TextField processingDateField;
    @FXML private ComboBox<String> finalStatusComboBox;
    @FXML private DatePicker effectiveDatePicker;

    // Footer
    @FXML private Label lastSavedLabel;
    @FXML private Label clearanceSummaryLabel;

    private Student currentStudent;
    private boolean isDirty = false;

    @FXML
    public void initialize() {
        log.info("Initializing StudentWithdrawalFormController");

        populateComboBoxes();
        generateWithdrawalNumber();
        setupListeners();

        if (signatureDateField != null) {
            signatureDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        if (processingDateField != null) {
            processingDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        log.info("StudentWithdrawalFormController initialized");
    }

    private void populateComboBoxes() {
        if (withdrawalTypeComboBox != null) {
            withdrawalTypeComboBox.getItems().addAll(
                "Transfer to Another School", "Moved Out of District", "Home School",
                "Graduation", "Dropout", "Expelled", "Medical Reasons", "Other"
            );
        }

        if (withdrawalReasonComboBox != null) {
            withdrawalReasonComboBox.getItems().addAll(
                "Family Relocation", "Transfer to Public School", "Transfer to Private School",
                "Academic Reasons", "Behavioral Issues", "Bullying Concerns",
                "Financial Hardship", "Health/Medical Reasons", "Homeschooling",
                "Dissatisfaction with School", "Completed Grade 12", "Other"
            );
        }

        if (satisfactionRatingComboBox != null) {
            satisfactionRatingComboBox.getItems().addAll(
                "Very Satisfied", "Satisfied", "Neutral", "Dissatisfied", "Very Dissatisfied"
            );
        }

        if (finalStatusComboBox != null) {
            finalStatusComboBox.getItems().addAll(
                "WITHDRAWN", "TRANSFERRED", "GRADUATED", "DROPPED_OUT", "EXPELLED"
            );
        }
    }

    private void generateWithdrawalNumber() {
        String withdrawalNum = "WD-" + LocalDate.now().getYear() + "-" + String.format("%06d", (int) (Math.random() * 999999));
        if (withdrawalNumberField != null) {
            withdrawalNumberField.setText(withdrawalNum);
        }
    }

    private void setupListeners() {
        if (isTransferringCheckbox != null && receivingSchoolSection != null) {
            isTransferringCheckbox.selectedProperty().addListener((obs, old, val) -> {
                receivingSchoolSection.setDisable(!val);
                receivingSchoolSection.setExpanded(val);
            });
        }

        // Add listeners to all clearance checkboxes
        CheckBox[] clearanceBoxes = {
            finalGradesCheckbox, transcriptPrintedCheckbox, iep504FinalizedCheckbox, progressReportsSentCheckbox,
            libraryBooksCheckbox, textbooksCheckbox, libraryFinesCheckbox, devicesReturnedCheckbox,
            athleticEquipmentCheckbox, instrumentsCheckbox, lockerClearedCheckbox, lockerLockCheckbox,
            parkingPermitCheckbox, idCardCheckbox, tuitionPaidCheckbox, cafeteriaBalanceCheckbox,
            activityFeesCheckbox, damageFeesCheckbox, recordsReleaseSignedCheckbox, immunizationsCopiedCheckbox,
            paperworkCompletedCheckbox, parentNotificationCheckbox
        };

        for (CheckBox box : clearanceBoxes) {
            if (box != null) {
                box.selectedProperty().addListener((obs, old, val) -> updateClearanceStatus());
            }
        }
    }

    @FXML
    private void handleSearchStudent() {
        if (studentSearchField == null || studentSearchField.getText().trim().isEmpty()) {
            showError("Please enter a Student ID or Name.");
            return;
        }

        try {
            String searchTerm = studentSearchField.getText().trim();
            Student student = null;

            if (searchTerm.matches("\\d+")) {
                student = studentRepository.findById(Long.parseLong(searchTerm)).orElse(null);
            }

            if (student != null) {
                loadStudentData(student);
            } else {
                showError("Student not found.");
            }
        } catch (Exception e) {
            log.error("Error searching student", e);
            showError("Error: " + e.getMessage());
        }
    }

    private void loadStudentData(Student student) {
        currentStudent = student;

        // Create withdrawal record
        createWithdrawalRecord();

        if (studentNameField != null) {
            studentNameField.setText(student.getFirstName() + " " + student.getLastName());
        }
        if (studentIdField != null) {
            studentIdField.setText(student.getStudentId());
        }
        if (currentGradeField != null) {
            currentGradeField.setText(student.getGradeLevel());
        }
        if (currentStatusField != null) {
            currentStatusField.setText(student.getStudentStatus() != null ? student.getStudentStatus().getDisplayName() : "Active");
        }

        // Load parent info from StudentParentRelationship
        try {
            parentRelationshipRepository.findByStudentAndIsPrimaryContactTrue(student).ifPresent(rel -> {
                if (parentNameField != null && rel.getParent() != null) {
                    parentNameField.setText(rel.getParent().getFullName());
                }
                if (parentPhoneField != null && rel.getParent() != null) {
                    String phone = rel.getParent().getCellPhone() != null ? rel.getParent().getCellPhone() : rel.getParent().getHomePhone();
                    parentPhoneField.setText(phone != null ? phone : "");
                }
                if (parentEmailField != null && rel.getParent() != null) {
                    parentEmailField.setText(rel.getParent().getEmail() != null ? rel.getParent().getEmail() : "");
                }
            });
        } catch (Exception e) {
            log.debug("Could not load parent info for student {}", student.getStudentId(), e);
        }

        log.info("Loaded student: {}", student.getStudentId());
    }

    /**
     * Create new withdrawal record in database
     */
    private void createWithdrawalRecord() {
        if (currentStudent == null) {
            showError("Please select a student first.");
            return;
        }

        if (withdrawalDatePicker == null || withdrawalDatePicker.getValue() == null) {
            showError("Please select a withdrawal date.");
            return;
        }

        try {
            currentWithdrawal = withdrawalService.createWithdrawal(
                currentStudent.getId(),
                withdrawalDatePicker.getValue(),
                SecurityContext.getCurrentStaffId()
            );

            withdrawalNumberField.setText(currentWithdrawal.getWithdrawalNumber());
            statusLabel.setText(currentWithdrawal.getStatus().getDisplayName());

            log.info("Created withdrawal record: {}", currentWithdrawal.getWithdrawalNumber());
        } catch (Exception e) {
            log.error("Error creating withdrawal record", e);
            showError("Error creating withdrawal record: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckAllClearance() {
        CheckBox[] boxes = {
            finalGradesCheckbox, transcriptPrintedCheckbox, iep504FinalizedCheckbox, progressReportsSentCheckbox,
            libraryBooksCheckbox, textbooksCheckbox, libraryFinesCheckbox, devicesReturnedCheckbox,
            athleticEquipmentCheckbox, instrumentsCheckbox, lockerClearedCheckbox, lockerLockCheckbox,
            parkingPermitCheckbox, idCardCheckbox, tuitionPaidCheckbox, cafeteriaBalanceCheckbox,
            activityFeesCheckbox, damageFeesCheckbox, recordsReleaseSignedCheckbox, immunizationsCopiedCheckbox,
            paperworkCompletedCheckbox, parentNotificationCheckbox
        };

        for (CheckBox box : boxes) {
            if (box != null) box.setSelected(true);
        }

        updateClearanceStatus();
    }

    private void updateClearanceStatus() {
        CheckBox[] boxes = {
            finalGradesCheckbox, transcriptPrintedCheckbox, iep504FinalizedCheckbox, progressReportsSentCheckbox,
            libraryBooksCheckbox, textbooksCheckbox, libraryFinesCheckbox, devicesReturnedCheckbox,
            athleticEquipmentCheckbox, instrumentsCheckbox, lockerClearedCheckbox, lockerLockCheckbox,
            parkingPermitCheckbox, idCardCheckbox, tuitionPaidCheckbox, cafeteriaBalanceCheckbox,
            activityFeesCheckbox, damageFeesCheckbox, recordsReleaseSignedCheckbox, immunizationsCopiedCheckbox,
            paperworkCompletedCheckbox, parentNotificationCheckbox
        };

        int clearedCount = 0;
        for (CheckBox box : boxes) {
            if (box != null && box.isSelected()) clearedCount++;
        }

        boolean allCleared = (clearedCount == boxes.length);

        if (clearanceSummaryLabel != null) {
            clearanceSummaryLabel.setText(clearedCount + "/" + boxes.length + " items cleared");
        }
        if (clearanceStatusLabel != null) {
            clearanceStatusLabel.setText(allCleared ? "CLEARED" : "NOT CLEARED");
        }
        if (completeButton != null) {
            completeButton.setDisable(!allCleared);
        }
    }

    @FXML
    private void handleSaveDraft() {
        if (currentWithdrawal == null) {
            showError("Please select a student first.");
            return;
        }

        try {
            // Save form data to entity
            saveFormDataToEntity();

            // Save to database
            currentWithdrawal = withdrawalService.updateWithdrawal(
                currentWithdrawal,
                SecurityContext.getCurrentStaffId()
            );

            isDirty = false;
            if (lastSavedLabel != null) {
                lastSavedLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            // Update clearance summary
            updateClearanceStatus();

            showSuccess("Withdrawal draft saved.");
            log.info("Saved withdrawal draft: {}", currentWithdrawal.getWithdrawalNumber());
        } catch (Exception e) {
            log.error("Error saving", e);
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCompleteWithdrawal() {
        if (!validateAllFields()) {
            return;
        }

        if (currentWithdrawal == null) {
            showError("Please select a student first.");
            return;
        }

        try {
            // Save form data to entity
            saveFormDataToEntity();

            // Determine final status based on withdrawal type
            FinalStatus finalStatus = FinalStatus.WITHDRAWN;
            if (currentWithdrawal.getIsTransferring() != null && currentWithdrawal.getIsTransferring()) {
                finalStatus = FinalStatus.TRANSFERRED;
            } else if (finalStatusComboBox != null && finalStatusComboBox.getValue() != null) {
                finalStatus = FinalStatus.valueOf(finalStatusComboBox.getValue());
            }

            // Complete withdrawal
            currentWithdrawal = withdrawalService.completeWithdrawal(
                currentWithdrawal.getId(),
                finalStatus,
                SecurityContext.getCurrentStaffId()
            );

            if (statusLabel != null) {
                statusLabel.setText(currentWithdrawal.getStatus().getDisplayName());
            }
            if (completeButton != null) {
                completeButton.setDisable(true);
            }

            showSuccess("Student withdrawal completed successfully.\n\n" +
                       "Withdrawal Number: " + currentWithdrawal.getWithdrawalNumber() + "\n" +
                       "Final Status: " + finalStatus.getDisplayName());
            log.info("Completed withdrawal: {}", currentWithdrawal.getWithdrawalNumber());
        } catch (Exception e) {
            log.error("Error completing withdrawal", e);
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateTransferDocs() {
        try {
            log.info("Generating transfer documents");

            if (currentStudent == null) {
                showError("Please select a student first.");
                return;
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Transfer Documents");
            fileChooser.setInitialFileName("transfer_docs_" + currentStudent.getStudentId() + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(
                studentNameField != null ? studentNameField.getScene().getWindow() : null);

            if (file != null) {
                // Generate PDF transfer packet
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "OFFICIAL TRANSFER RECORDS PACKET", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Header info
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11);
                com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD);

                document.add(new com.itextpdf.text.Paragraph("Prepared: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")), normalFont));
                document.add(new com.itextpdf.text.Paragraph("Withdrawal Number: " +
                    (currentWithdrawal != null && currentWithdrawal.getWithdrawalNumber() != null ?
                        currentWithdrawal.getWithdrawalNumber() : "N/A"), normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Student Information
                document.add(new com.itextpdf.text.Paragraph("STUDENT INFORMATION", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable studentTable = new com.itextpdf.text.pdf.PdfPTable(2);
                studentTable.setWidthPercentage(100);
                studentTable.setWidths(new float[]{35, 65});

                addTransferField(studentTable, "Student Name:", currentStudent.getFullName());
                addTransferField(studentTable, "Student ID:", currentStudent.getStudentId());
                addTransferField(studentTable, "Date of Birth:", currentStudent.getDateOfBirth() != null ?
                    currentStudent.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A");
                addTransferField(studentTable, "Current Grade:", currentStudent.getGradeLevel() != null ?
                    currentStudent.getGradeLevel() : "N/A");
                addTransferField(studentTable, "Last Attendance Date:", lastAttendanceDatePicker != null &&
                    lastAttendanceDatePicker.getValue() != null ?
                    lastAttendanceDatePicker.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A");
                addTransferField(studentTable, "Withdrawal Date:", withdrawalDatePicker != null &&
                    withdrawalDatePicker.getValue() != null ?
                    withdrawalDatePicker.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A");

                document.add(studentTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Receiving School Information
                if (currentWithdrawal != null && currentWithdrawal.getIsTransferring() != null &&
                    currentWithdrawal.getIsTransferring()) {
                    document.add(new com.itextpdf.text.Paragraph("RECEIVING SCHOOL INFORMATION", boldFont));
                    document.add(new com.itextpdf.text.Paragraph(" "));

                    com.itextpdf.text.pdf.PdfPTable schoolTable = new com.itextpdf.text.pdf.PdfPTable(2);
                    schoolTable.setWidthPercentage(100);
                    schoolTable.setWidths(new float[]{35, 65});

                    addTransferField(schoolTable, "School Name:",
                        receivingSchoolNameField != null ? receivingSchoolNameField.getText() : "N/A");
                    addTransferField(schoolTable, "School District:",
                        receivingSchoolDistrictField != null ? receivingSchoolDistrictField.getText() : "N/A");
                    addTransferField(schoolTable, "Location:",
                        receivingSchoolLocationField != null ? receivingSchoolLocationField.getText() : "N/A");
                    addTransferField(schoolTable, "Phone:",
                        receivingSchoolPhoneField != null ? receivingSchoolPhoneField.getText() : "N/A");
                    addTransferField(schoolTable, "Email:",
                        receivingSchoolEmailField != null ? receivingSchoolEmailField.getText() : "N/A");

                    document.add(schoolTable);
                    document.add(new com.itextpdf.text.Paragraph(" "));
                }

                // Academic Summary
                document.add(new com.itextpdf.text.Paragraph("ACADEMIC SUMMARY", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.pdf.PdfPTable academicTable = new com.itextpdf.text.pdf.PdfPTable(2);
                academicTable.setWidthPercentage(100);
                academicTable.setWidths(new float[]{35, 65});

                addTransferField(academicTable, "Current GPA:", currentStudent.getCurrentGPA() != null ?
                    String.format("%.2f", currentStudent.getCurrentGPA()) : "N/A");
                addTransferField(academicTable, "Credits Earned:", currentStudent.getCreditsEarned() != null ?
                    String.format("%.1f", currentStudent.getCreditsEarned()) : "0.0");

                document.add(academicTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Records Included
                document.add(new com.itextpdf.text.Paragraph("RECORDS INCLUDED IN THIS PACKET:", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                com.itextpdf.text.List recordsList = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
                recordsList.add(new com.itextpdf.text.ListItem("Official Transcript", normalFont));
                recordsList.add(new com.itextpdf.text.ListItem("Cumulative Student Record", normalFont));
                recordsList.add(new com.itextpdf.text.ListItem("Attendance Records", normalFont));
                recordsList.add(new com.itextpdf.text.ListItem("Immunization Records", normalFont));
                recordsList.add(new com.itextpdf.text.ListItem("Standardized Test Scores", normalFont));
                if (iep504FinalizedCheckbox != null && iep504FinalizedCheckbox.isSelected()) {
                    recordsList.add(new com.itextpdf.text.ListItem("IEP/504 Documentation", normalFont));
                }
                document.add(recordsList);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Certification
                document.add(new com.itextpdf.text.Paragraph("CERTIFICATION", boldFont));
                document.add(new com.itextpdf.text.Paragraph(" "));
                document.add(new com.itextpdf.text.Paragraph(
                    "This is to certify that the enclosed documents are true and accurate copies of the official " +
                    "records maintained by Heronix Student Information System for the above-named student.", normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Signature lines
                com.itextpdf.text.pdf.PdfPTable signatureTable = new com.itextpdf.text.pdf.PdfPTable(2);
                signatureTable.setWidthPercentage(100);
                signatureTable.setWidths(new float[]{50, 50});

                addTransferField(signatureTable, "Registrar Signature:", "____________________");
                addTransferField(signatureTable, "Date:", "____________________");

                document.add(signatureTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Footer
                com.itextpdf.text.Font smallFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
                com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph(
                    "CONFIDENTIAL - This packet contains educational records protected under FERPA. " +
                    "Unauthorized disclosure is prohibited.", smallFont);
                footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(footer);

                document.close();

                // Save to file
                java.nio.file.Files.write(file.toPath(), baos.toByteArray());

                showSuccess("Transfer documents generated successfully!\n\nFile: " + file.getAbsolutePath());
                log.info("Transfer documents saved to: {}", file.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("Error generating transfer documents", e);
            showError("Failed to generate transfer documents: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrintChecklist() {
        try {
            log.info("Printing clearance checklist");

            if (currentStudent == null) {
                showError("Please select a student first.");
                return;
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save Clearance Checklist");
            fileChooser.setInitialFileName("clearance_checklist_" + currentStudent.getStudentId() + "_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            java.io.File file = fileChooser.showSaveDialog(
                studentNameField != null ? studentNameField.getScene().getWindow() : null);

            if (file != null) {
                // Generate PDF clearance checklist
                com.itextpdf.text.Document document = new com.itextpdf.text.Document();
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);

                document.open();

                // Title
                com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
                com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(
                    "STUDENT WITHDRAWAL CLEARANCE CHECKLIST", titleFont);
                title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                document.add(title);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Student info
                com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11);
                com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD);

                document.add(new com.itextpdf.text.Paragraph("Student: " + currentStudent.getFullName(), boldFont));
                document.add(new com.itextpdf.text.Paragraph("Student ID: " + currentStudent.getStudentId(), normalFont));
                document.add(new com.itextpdf.text.Paragraph("Grade: " +
                    (currentStudent.getGradeLevel() != null ? currentStudent.getGradeLevel() : "N/A"), normalFont));
                document.add(new com.itextpdf.text.Paragraph("Withdrawal Date: " +
                    (withdrawalDatePicker != null && withdrawalDatePicker.getValue() != null ?
                        withdrawalDatePicker.getValue().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A"), normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Checklist table
                com.itextpdf.text.pdf.PdfPTable checklistTable = new com.itextpdf.text.pdf.PdfPTable(3);
                checklistTable.setWidthPercentage(100);
                checklistTable.setWidths(new float[]{10, 60, 30});

                // Header row
                com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.BOLD);
                addChecklistHeader(checklistTable, "Done", headerFont);
                addChecklistHeader(checklistTable, "Item", headerFont);
                addChecklistHeader(checklistTable, "Verified By / Date", headerFont);

                // Academic Records
                addChecklistRow(checklistTable, finalGradesCheckbox, "Final grades posted and verified");
                addChecklistRow(checklistTable, transcriptPrintedCheckbox, "Official transcript printed");
                addChecklistRow(checklistTable, iep504FinalizedCheckbox, "IEP/504 plan finalized (if applicable)");
                addChecklistRow(checklistTable, progressReportsSentCheckbox, "Progress reports sent to parent/receiving school");

                // Library & Resources
                addChecklistRow(checklistTable, libraryBooksCheckbox, "Library books returned");
                addChecklistRow(checklistTable, textbooksCheckbox, "Textbooks returned");
                addChecklistRow(checklistTable, libraryFinesCheckbox, "Library fines paid/waived");

                // Technology & Equipment
                addChecklistRow(checklistTable, devicesReturnedCheckbox, "Technology devices returned (laptop, tablet, etc.)");
                addChecklistRow(checklistTable, athleticEquipmentCheckbox, "Athletic equipment returned");
                addChecklistRow(checklistTable, instrumentsCheckbox, "Musical instruments returned");
                addChecklistRow(checklistTable, lockerClearedCheckbox, "Locker cleaned out and lock returned");

                document.add(checklistTable);
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Completion status
                int totalItems = 11;
                int completedItems = countCompletedCheckboxes();
                double completionPercentage = (completedItems / (double) totalItems) * 100;

                document.add(new com.itextpdf.text.Paragraph("COMPLETION STATUS", boldFont));
                document.add(new com.itextpdf.text.Paragraph(
                    String.format("Completed: %d of %d items (%.0f%%)", completedItems, totalItems, completionPercentage),
                    normalFont));
                document.add(new com.itextpdf.text.Paragraph(" "));

                if (completionPercentage >= 100) {
                    document.add(new com.itextpdf.text.Paragraph("✓ CLEARANCE COMPLETE - Student may withdraw", boldFont));
                } else {
                    document.add(new com.itextpdf.text.Paragraph("⚠ CLEARANCE INCOMPLETE - Outstanding items must be resolved", boldFont));
                }
                document.add(new com.itextpdf.text.Paragraph(" "));
                document.add(new com.itextpdf.text.Paragraph(" "));

                // Signature
                com.itextpdf.text.pdf.PdfPTable signatureTable = new com.itextpdf.text.pdf.PdfPTable(2);
                signatureTable.setWidthPercentage(100);
                signatureTable.setWidths(new float[]{50, 50});

                addTransferField(signatureTable, "Counselor Signature:", "____________________");
                addTransferField(signatureTable, "Date:", "____________________");

                document.add(signatureTable);

                document.close();

                // Save to file
                java.nio.file.Files.write(file.toPath(), baos.toByteArray());

                showSuccess("Clearance checklist generated successfully!\n\nFile: " + file.getAbsolutePath());
                log.info("Clearance checklist saved to: {}", file.getAbsolutePath());
            }

        } catch (Exception e) {
            log.error("Error printing clearance checklist", e);
            showError("Failed to print clearance checklist: " + e.getMessage());
        }
    }

    // Helper methods for PDF generation
    private void addTransferField(com.itextpdf.text.pdf.PdfPTable table, String label, String value) {
        com.itextpdf.text.Font labelFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font valueFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        com.itextpdf.text.pdf.PdfPCell labelCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(label, labelFont));
        labelCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        com.itextpdf.text.pdf.PdfPCell valueCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(value != null ? value : "N/A", valueFont));
        valueCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addChecklistHeader(com.itextpdf.text.pdf.PdfPTable table, String text, com.itextpdf.text.Font font) {
        com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(text, font));
        cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addChecklistRow(com.itextpdf.text.pdf.PdfPTable table, CheckBox checkbox, String itemText) {
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 10);

        // Checkbox column
        com.itextpdf.text.pdf.PdfPCell checkCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(checkbox != null && checkbox.isSelected() ? "☑" : "☐", normalFont));
        checkCell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        checkCell.setPadding(5);
        table.addCell(checkCell);

        // Item text column
        com.itextpdf.text.pdf.PdfPCell itemCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase(itemText, normalFont));
        itemCell.setPadding(5);
        table.addCell(itemCell);

        // Verified by column
        com.itextpdf.text.pdf.PdfPCell verifyCell = new com.itextpdf.text.pdf.PdfPCell(
            new com.itextpdf.text.Phrase("", normalFont));
        verifyCell.setPadding(5);
        table.addCell(verifyCell);
    }

    private int countCompletedCheckboxes() {
        int count = 0;
        if (finalGradesCheckbox != null && finalGradesCheckbox.isSelected()) count++;
        if (transcriptPrintedCheckbox != null && transcriptPrintedCheckbox.isSelected()) count++;
        if (iep504FinalizedCheckbox != null && iep504FinalizedCheckbox.isSelected()) count++;
        if (progressReportsSentCheckbox != null && progressReportsSentCheckbox.isSelected()) count++;
        if (libraryBooksCheckbox != null && libraryBooksCheckbox.isSelected()) count++;
        if (textbooksCheckbox != null && textbooksCheckbox.isSelected()) count++;
        if (libraryFinesCheckbox != null && libraryFinesCheckbox.isSelected()) count++;
        if (devicesReturnedCheckbox != null && devicesReturnedCheckbox.isSelected()) count++;
        if (athleticEquipmentCheckbox != null && athleticEquipmentCheckbox.isSelected()) count++;
        if (instrumentsCheckbox != null && instrumentsCheckbox.isSelected()) count++;
        if (lockerClearedCheckbox != null && lockerClearedCheckbox.isSelected()) count++;
        return count;
    }

    @FXML
    private void handleCancel() {
        if (isDirty) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setContentText("Save before closing?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) handleSaveDraft();
            });
        }
        closeForm();
    }

    /**
     * Save form data to WithdrawalRecord entity
     */
    private void saveFormDataToEntity() {
        if (currentWithdrawal == null) return;

        // Withdrawal Details
        if (withdrawalDatePicker != null && withdrawalDatePicker.getValue() != null) {
            currentWithdrawal.setWithdrawalDate(withdrawalDatePicker.getValue());
        }
        if (withdrawalTypeComboBox != null && withdrawalTypeComboBox.getValue() != null) {
            currentWithdrawal.setWithdrawalType(WithdrawalRecord.WithdrawalType.valueOf(
                withdrawalTypeComboBox.getValue().toUpperCase().replace(" ", "_").replace("-", "_")
            ));
        }
        if (withdrawalReasonComboBox != null && withdrawalReasonComboBox.getValue() != null) {
            currentWithdrawal.setWithdrawalReason(WithdrawalRecord.WithdrawalReason.valueOf(
                withdrawalReasonComboBox.getValue().toUpperCase().replace(" ", "_").replace("/", "_")
            ));
        }
        if (withdrawalDetailsArea != null) {
            currentWithdrawal.setWithdrawalDetails(withdrawalDetailsArea.getText());
        }
        if (isTransferringCheckbox != null) {
            currentWithdrawal.setIsTransferring(isTransferringCheckbox.isSelected());
        }
        if (lastAttendanceDatePicker != null && lastAttendanceDatePicker.getValue() != null) {
            currentWithdrawal.setLastAttendanceDate(lastAttendanceDatePicker.getValue());
        }

        // Receiving School
        if (receivingSchoolNameField != null) {
            currentWithdrawal.setReceivingSchoolName(receivingSchoolNameField.getText());
        }
        if (receivingSchoolDistrictField != null) {
            currentWithdrawal.setReceivingSchoolDistrict(receivingSchoolDistrictField.getText());
        }
        if (receivingSchoolLocationField != null) {
            currentWithdrawal.setReceivingSchoolLocation(receivingSchoolLocationField.getText());
        }
        if (receivingSchoolPhoneField != null) {
            currentWithdrawal.setReceivingSchoolPhone(receivingSchoolPhoneField.getText());
        }
        if (receivingSchoolEmailField != null) {
            currentWithdrawal.setReceivingSchoolEmail(receivingSchoolEmailField.getText());
        }
        if (expectedStartDatePicker != null && expectedStartDatePicker.getValue() != null) {
            currentWithdrawal.setExpectedStartDate(expectedStartDatePicker.getValue());
        }
        if (recordsRequestedByField != null) {
            currentWithdrawal.setRecordsRequestedBy(recordsRequestedByField.getText());
        }

        // Exit Interview
        if (interviewDatePicker != null && interviewDatePicker.getValue() != null) {
            currentWithdrawal.setInterviewDate(interviewDatePicker.getValue());
        }
        if (interviewedByField != null) {
            currentWithdrawal.setInterviewedBy(interviewedByField.getText());
        }
        if (parentPresentCheckbox != null) {
            currentWithdrawal.setParentPresent(parentPresentCheckbox.isSelected());
        }
        if (studentPresentCheckbox != null) {
            currentWithdrawal.setStudentPresent(studentPresentCheckbox.isSelected());
        }
        if (satisfactionRatingComboBox != null && satisfactionRatingComboBox.getValue() != null) {
            currentWithdrawal.setSatisfactionRating(WithdrawalRecord.SatisfactionRating.valueOf(
                satisfactionRatingComboBox.getValue().toUpperCase().replace(" ", "_")
            ));
        }
        if (primaryFactorsArea != null) {
            currentWithdrawal.setPrimaryFactors(primaryFactorsArea.getText());
        }
        if (academicFeedbackArea != null) {
            currentWithdrawal.setAcademicFeedback(academicFeedbackArea.getText());
        }
        if (socialFeedbackArea != null) {
            currentWithdrawal.setSocialFeedback(socialFeedbackArea.getText());
        }
        if (improvementSuggestionsArea != null) {
            currentWithdrawal.setImprovementSuggestions(improvementSuggestionsArea.getText());
        }
        if (exitInterviewNotesArea != null) {
            currentWithdrawal.setExitInterviewNotes(exitInterviewNotesArea.getText());
        }

        // Clearance Checklist - Academic
        if (finalGradesCheckbox != null) {
            currentWithdrawal.setFinalGradesRecorded(finalGradesCheckbox.isSelected());
        }
        if (transcriptPrintedCheckbox != null) {
            currentWithdrawal.setTranscriptPrinted(transcriptPrintedCheckbox.isSelected());
        }
        if (iep504FinalizedCheckbox != null) {
            currentWithdrawal.setIep504Finalized(iep504FinalizedCheckbox.isSelected());
        }
        if (progressReportsSentCheckbox != null) {
            currentWithdrawal.setProgressReportsSent(progressReportsSentCheckbox.isSelected());
        }

        // Clearance Checklist - Library & Materials
        if (libraryBooksCheckbox != null) {
            currentWithdrawal.setLibraryBooksReturned(libraryBooksCheckbox.isSelected());
        }
        if (textbooksCheckbox != null) {
            currentWithdrawal.setTextbooksReturned(textbooksCheckbox.isSelected());
        }
        if (libraryFinesCheckbox != null) {
            currentWithdrawal.setLibraryFinesPaid(libraryFinesCheckbox.isSelected());
        }
        if (devicesReturnedCheckbox != null) {
            currentWithdrawal.setDevicesReturned(devicesReturnedCheckbox.isSelected());
        }
        if (athleticEquipmentCheckbox != null) {
            currentWithdrawal.setAthleticEquipmentReturned(athleticEquipmentCheckbox.isSelected());
        }
        if (instrumentsCheckbox != null) {
            currentWithdrawal.setInstrumentsReturned(instrumentsCheckbox.isSelected());
        }

        // Clearance Checklist - Facilities
        if (lockerClearedCheckbox != null) {
            currentWithdrawal.setLockerCleared(lockerClearedCheckbox.isSelected());
        }
        if (lockerLockCheckbox != null) {
            currentWithdrawal.setLockerLockReturned(lockerLockCheckbox.isSelected());
        }
        if (parkingPermitCheckbox != null) {
            currentWithdrawal.setParkingPermitReturned(parkingPermitCheckbox.isSelected());
        }
        if (idCardCheckbox != null) {
            currentWithdrawal.setIdCardReturned(idCardCheckbox.isSelected());
        }

        // Clearance Checklist - Financial
        if (tuitionPaidCheckbox != null) {
            currentWithdrawal.setTuitionPaid(tuitionPaidCheckbox.isSelected());
        }
        if (cafeteriaBalanceCheckbox != null) {
            currentWithdrawal.setCafeteriaBalanceSettled(cafeteriaBalanceCheckbox.isSelected());
        }
        if (activityFeesCheckbox != null) {
            currentWithdrawal.setActivityFeesPaid(activityFeesCheckbox.isSelected());
        }
        if (damageFeesCheckbox != null) {
            currentWithdrawal.setDamageFeesPaid(damageFeesCheckbox.isSelected());
        }

        // Clearance Checklist - Administrative
        if (recordsReleaseSignedCheckbox != null) {
            currentWithdrawal.setRecordsReleaseSigned(recordsReleaseSignedCheckbox.isSelected());
        }
        if (immunizationsCopiedCheckbox != null) {
            currentWithdrawal.setImmunizationsCopied(immunizationsCopiedCheckbox.isSelected());
        }
        if (paperworkCompletedCheckbox != null) {
            currentWithdrawal.setPaperworkCompleted(paperworkCompletedCheckbox.isSelected());
        }
        if (parentNotificationCheckbox != null) {
            currentWithdrawal.setParentNotificationSent(parentNotificationCheckbox.isSelected());
        }

        // Parent Confirmation
        if (parentNameField != null) {
            currentWithdrawal.setParentName(parentNameField.getText());
        }
        if (parentPhoneField != null) {
            currentWithdrawal.setParentPhone(parentPhoneField.getText());
        }
        if (parentEmailField != null) {
            currentWithdrawal.setParentEmail(parentEmailField.getText());
        }
        if (acknowledgeWithdrawalCheckbox != null) {
            currentWithdrawal.setAcknowledgedWithdrawal(acknowledgeWithdrawalCheckbox.isSelected());
        }
        if (acknowledgeClearanceCheckbox != null) {
            currentWithdrawal.setAcknowledgedClearance(acknowledgeClearanceCheckbox.isSelected());
        }
        if (acknowledgeRecordsCheckbox != null) {
            currentWithdrawal.setAcknowledgedRecords(acknowledgeRecordsCheckbox.isSelected());
        }
        if (acknowledgeNoRefundCheckbox != null) {
            currentWithdrawal.setAcknowledgedNoRefund(acknowledgeNoRefundCheckbox.isSelected());
        }
        if (parentSignatureField != null) {
            currentWithdrawal.setParentSignature(parentSignatureField.getText());
        }
        if (signatureDateField != null && signatureDateField.getText() != null) {
            try {
                currentWithdrawal.setSignatureDate(LocalDate.parse(signatureDateField.getText()));
            } catch (Exception e) {
                log.warn("Could not parse signature date");
            }
        }

        // Administrative
        if (administrativeNotesArea != null) {
            currentWithdrawal.setAdministrativeNotes(administrativeNotesArea.getText());
        }
        if (effectiveDatePicker != null && effectiveDatePicker.getValue() != null) {
            currentWithdrawal.setEffectiveDate(effectiveDatePicker.getValue());
        }
    }

    private boolean validateAllFields() {
        StringBuilder errors = new StringBuilder();

        if (currentStudent == null) errors.append("• Student must be selected\n");
        if (withdrawalDatePicker == null || withdrawalDatePicker.getValue() == null)
            errors.append("• Withdrawal date is required\n");
        if (withdrawalTypeComboBox == null || withdrawalTypeComboBox.getValue() == null)
            errors.append("• Withdrawal type is required\n");
        if (parentSignatureField == null || parentSignatureField.getText().trim().isEmpty())
            errors.append("• Parent signature is required\n");

        if (errors.length() > 0) {
            showError("Validation Error:\n" + errors.toString());
            return false;
        }
        return true;
    }

    private void closeForm() {
        if (withdrawalNumberField != null && withdrawalNumberField.getScene() != null) {
            Stage stage = (Stage) withdrawalNumberField.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showNotImplemented(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Feature Not Implemented");
        alert.setContentText(feature + " - Coming in future release");
        alert.showAndWait();
    }
}
