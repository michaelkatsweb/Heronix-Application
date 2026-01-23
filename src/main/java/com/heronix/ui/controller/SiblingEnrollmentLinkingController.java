package com.heronix.ui.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.FamilyHousehold;
import com.heronix.security.SecurityContext;
import com.heronix.service.StudentService;
import com.heronix.service.FamilyManagementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Sibling Enrollment Linking
 * Links siblings during enrollment for family tracking and discounts
 *
 * Features:
 * - Family household creation/linking
 * - Sibling relationship tracking
 * - Family discount calculation
 * - Shared parent/guardian information
 *
 * Location: src/main/java/com/heronix/ui/controller/SiblingEnrollmentLinkingController.java
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class SiblingEnrollmentLinkingController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private FamilyManagementService familyManagementService;

    private FamilyHousehold currentFamily;

    // Header
    @FXML private TextField familyIdField;
    @FXML private Label familyStatusLabel;
    @FXML private Label totalChildrenLabel;
    @FXML private Label enrolledCountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label familyDiscountLabel;
    @FXML private Button addSiblingButton;
    @FXML private Button saveButton;
    @FXML private Button applyDiscountsButton;

    // Section 1: Family Information
    @FXML private TextField familySearchField;
    @FXML private TextField familyNameField;
    @FXML private ComboBox<String> householdTypeComboBox;
    @FXML private TextArea primaryAddressArea;
    @FXML private TextField primaryParentField;
    @FXML private TextField primaryPhoneField;
    @FXML private TextField primaryEmailField;
    @FXML private TextArea familyNotesArea;

    // Section 2: Siblings
    @FXML private TableView siblingsTable;

    // Section 3: Parents
    @FXML private TextField parent1NameField;
    @FXML private ComboBox<String> parent1RelationshipComboBox;
    @FXML private TextField parent1PhoneField;
    @FXML private TextField parent1EmailField;
    @FXML private TextField parent1WorkPhoneField;
    @FXML private TextField parent1EmployerField;
    @FXML private CheckBox parent1LivesInHouseholdCheckbox;
    @FXML private CheckBox parent1CustodialCheckbox;

    @FXML private TextField parent2NameField;
    @FXML private ComboBox<String> parent2RelationshipComboBox;
    @FXML private TextField parent2PhoneField;
    @FXML private TextField parent2EmailField;
    @FXML private TextField parent2WorkPhoneField;
    @FXML private TextField parent2EmployerField;
    @FXML private CheckBox parent2LivesInHouseholdCheckbox;
    @FXML private CheckBox parent2CustodialCheckbox;

    @FXML private TextField emergency1NameField;
    @FXML private TextField emergency1PhoneField;
    @FXML private TextField emergency1RelationField;
    @FXML private TextField emergency2NameField;
    @FXML private TextField emergency2PhoneField;
    @FXML private TextField emergency2RelationField;

    // Section 4: Discounts
    @FXML private ComboBox<String> discountTypeComboBox;
    @FXML private Label enrolledChildrenCountLabel;
    @FXML private Label baseDiscountPercentLabel;
    @FXML private Label siblingDiscountLabel;
    @FXML private Label totalFamilyDiscountLabel;
    @FXML private TableView feeBreakdownTable;
    @FXML private CheckBox discount2ndChildCheckbox;
    @FXML private CheckBox discount3rdPlusCheckbox;
    @FXML private CheckBox earlyBirdDiscountCheckbox;
    @FXML private CheckBox waiveTechFees3rdPlusCheckbox;

    // Section 5: Custody
    @FXML private ComboBox<String> custodyArrangementComboBox;
    @FXML private CheckBox custodyPapersCheckbox;
    @FXML private TextArea pickupRestrictionsArea;
    @FXML private TextArea specialFamilyNotesArea;

    // Footer
    @FXML private Label lastSavedLabel;
    @FXML private Label createdByLabel;

    private boolean isDirty = false;

    @FXML
    public void initialize() {
        log.info("Initializing SiblingEnrollmentLinkingController");

        populateComboBoxes();
        generateFamilyId();

        log.info("SiblingEnrollmentLinkingController initialized");
    }

    private void populateComboBoxes() {
        if (householdTypeComboBox != null) {
            householdTypeComboBox.getItems().addAll(
                "Two-Parent Household", "Single-Parent Household", "Blended Family",
                "Grandparent Guardian", "Foster Family", "Other"
            );
        }

        if (parent1RelationshipComboBox != null && parent2RelationshipComboBox != null) {
            String[] relationships = {"Mother", "Father", "Stepmother", "Stepfather",
                "Grandmother", "Grandfather", "Legal Guardian", "Foster Parent", "Other"};
            parent1RelationshipComboBox.getItems().addAll(relationships);
            parent2RelationshipComboBox.getItems().addAll(relationships);
        }

        if (discountTypeComboBox != null) {
            discountTypeComboBox.getItems().addAll(
                "Standard Sibling Discount", "Military Family Discount",
                "Staff Family Discount", "Need-Based Discount", "Custom Discount"
            );
            discountTypeComboBox.getSelectionModel().selectFirst();
        }

        if (custodyArrangementComboBox != null) {
            custodyArrangementComboBox.getItems().addAll(
                "Joint Custody", "Sole Custody - Parent 1", "Sole Custody - Parent 2",
                "Shared Custody (50/50)", "Primary Custody with Visitation", "Other"
            );
        }
    }

    private void generateFamilyId() {
        String familyId = "FAM-" + System.currentTimeMillis();
        if (familyIdField != null) {
            familyIdField.setText(familyId);
        }
    }

    @FXML
    private void handleSearchFamily() {
        try {
            log.info("Searching for family household");

            String searchTerm = familySearchField != null ? familySearchField.getText().trim() : "";
            if (searchTerm.isEmpty()) {
                showError("Please enter a family name or parent name to search.");
                return;
            }

            // Search by family name or parent name
            java.util.List<FamilyHousehold> results = new java.util.ArrayList<>();
            results.addAll(familyManagementService.searchFamiliesByName(searchTerm));
            results.addAll(familyManagementService.searchFamiliesByParentName(searchTerm));

            // Remove duplicates
            results = results.stream().distinct().collect(java.util.stream.Collectors.toList());

            if (results.isEmpty()) {
                showInfo("No families found matching: " + searchTerm);
                return;
            }

            // If multiple results, show selection dialog
            if (results.size() > 1) {
                javafx.scene.control.ChoiceDialog<FamilyHousehold> dialog =
                    new javafx.scene.control.ChoiceDialog<>(results.get(0), results);
                dialog.setTitle("Select Family");
                dialog.setHeaderText("Multiple families found");
                dialog.setContentText("Select family:");

                // Custom cell factory to display family name
                dialog.getItems().forEach(family -> {
                    // Override toString for display
                });

                dialog.showAndWait().ifPresent(selected -> {
                    loadFamily(selected);
                });
            } else {
                loadFamily(results.get(0));
            }

            log.info("Family search completed: {} results", results.size());
        } catch (Exception e) {
            log.error("Error searching for family", e);
            showError("Failed to search: " + e.getMessage());
        }
    }

    private void loadFamily(FamilyHousehold family) {
        currentFamily = family;

        // Update header
        if (familyIdField != null) {
            familyIdField.setText(family.getFamilyId());
        }
        if (familyStatusLabel != null) {
            familyStatusLabel.setText(family.getStatus() != null ? family.getStatus().getDisplayName() : "");
        }

        // Update family information
        if (familyNameField != null) {
            familyNameField.setText(family.getFamilyName());
        }
        if (householdTypeComboBox != null && family.getHouseholdType() != null) {
            householdTypeComboBox.setValue(family.getHouseholdType().getDisplayName());
        }
        if (primaryAddressArea != null) {
            primaryAddressArea.setText(family.getPrimaryAddress());
        }
        if (primaryParentField != null) {
            primaryParentField.setText(family.getPrimaryParentName());
        }
        if (primaryPhoneField != null) {
            primaryPhoneField.setText(family.getPrimaryPhone());
        }
        if (primaryEmailField != null) {
            primaryEmailField.setText(family.getPrimaryEmail());
        }
        if (familyNotesArea != null) {
            familyNotesArea.setText(family.getFamilyNotes());
        }

        // Update parent information
        if (parent1NameField != null) {
            parent1NameField.setText(family.getParent1Name());
        }
        if (parent1RelationshipComboBox != null && family.getParent1Relationship() != null) {
            parent1RelationshipComboBox.setValue(family.getParent1Relationship().getDisplayName());
        }
        if (parent1PhoneField != null) {
            parent1PhoneField.setText(family.getParent1Phone());
        }
        if (parent1EmailField != null) {
            parent1EmailField.setText(family.getParent1Email());
        }
        if (parent1WorkPhoneField != null) {
            parent1WorkPhoneField.setText(family.getParent1WorkPhone());
        }
        if (parent1EmployerField != null) {
            parent1EmployerField.setText(family.getParent1Employer());
        }
        if (parent1LivesInHouseholdCheckbox != null) {
            parent1LivesInHouseholdCheckbox.setSelected(family.getParent1LivesInHousehold() != null && family.getParent1LivesInHousehold());
        }
        if (parent1CustodialCheckbox != null) {
            parent1CustodialCheckbox.setSelected(family.getParent1IsCustodial() != null && family.getParent1IsCustodial());
        }

        if (parent2NameField != null) {
            parent2NameField.setText(family.getParent2Name());
        }
        if (parent2RelationshipComboBox != null && family.getParent2Relationship() != null) {
            parent2RelationshipComboBox.setValue(family.getParent2Relationship().getDisplayName());
        }
        if (parent2PhoneField != null) {
            parent2PhoneField.setText(family.getParent2Phone());
        }
        if (parent2EmailField != null) {
            parent2EmailField.setText(family.getParent2Email());
        }
        if (parent2WorkPhoneField != null) {
            parent2WorkPhoneField.setText(family.getParent2WorkPhone());
        }
        if (parent2EmployerField != null) {
            parent2EmployerField.setText(family.getParent2Employer());
        }
        if (parent2LivesInHouseholdCheckbox != null) {
            parent2LivesInHouseholdCheckbox.setSelected(family.getParent2LivesInHousehold() != null && family.getParent2LivesInHousehold());
        }
        if (parent2CustodialCheckbox != null) {
            parent2CustodialCheckbox.setSelected(family.getParent2IsCustodial() != null && family.getParent2IsCustodial());
        }

        // Update emergency contacts
        if (emergency1NameField != null) {
            emergency1NameField.setText(family.getEmergency1Name());
        }
        if (emergency1PhoneField != null) {
            emergency1PhoneField.setText(family.getEmergency1Phone());
        }
        if (emergency1RelationField != null) {
            emergency1RelationField.setText(family.getEmergency1Relation());
        }
        if (emergency2NameField != null) {
            emergency2NameField.setText(family.getEmergency2Name());
        }
        if (emergency2PhoneField != null) {
            emergency2PhoneField.setText(family.getEmergency2Phone());
        }
        if (emergency2RelationField != null) {
            emergency2RelationField.setText(family.getEmergency2Relation());
        }

        // Update discounts
        if (discount2ndChildCheckbox != null) {
            discount2ndChildCheckbox.setSelected(family.getDiscount2ndChild() != null && family.getDiscount2ndChild());
        }
        if (discount3rdPlusCheckbox != null) {
            discount3rdPlusCheckbox.setSelected(family.getDiscount3rdPlusChildren() != null && family.getDiscount3rdPlusChildren());
        }
        if (earlyBirdDiscountCheckbox != null) {
            earlyBirdDiscountCheckbox.setSelected(family.getEarlyBirdDiscountApplied() != null && family.getEarlyBirdDiscountApplied());
        }
        if (waiveTechFees3rdPlusCheckbox != null) {
            waiveTechFees3rdPlusCheckbox.setSelected(family.getWaiveTechFees3rdPlus() != null && family.getWaiveTechFees3rdPlus());
        }

        // Update custody
        if (custodyArrangementComboBox != null && family.getCustodyArrangement() != null) {
            custodyArrangementComboBox.setValue(family.getCustodyArrangement().getDisplayName());
        }
        if (custodyPapersCheckbox != null) {
            custodyPapersCheckbox.setSelected(family.getCustodyPapersOnFile() != null && family.getCustodyPapersOnFile());
        }
        if (pickupRestrictionsArea != null) {
            pickupRestrictionsArea.setText(family.getPickupRestrictions());
        }
        if (specialFamilyNotesArea != null) {
            specialFamilyNotesArea.setText(family.getSpecialFamilyNotes());
        }

        // Update discount labels
        updateDiscountLabels();

        // Update last saved
        if (lastSavedLabel != null && family.getUpdatedAt() != null) {
            lastSavedLabel.setText(family.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (createdByLabel != null && family.getCreatedBy() != null) {
            createdByLabel.setText(family.getCreatedBy().getFullName());
        }

        showSuccess("Family loaded: " + family.getFamilyId());
        log.info("Loaded family: {}", family.getFamilyId());
    }

    @FXML
    private void handleCreateFamily() {
        if (familyNameField == null || familyNameField.getText().trim().isEmpty()) {
            showError("Please enter a family name first.");
            return;
        }

        try {
            currentFamily = familyManagementService.createFamilyHousehold(
                familyNameField.getText().trim(),
                SecurityContext.getCurrentStaffId()
            );

            familyIdField.setText(currentFamily.getFamilyId());
            familyStatusLabel.setText(currentFamily.getStatus().getDisplayName());
            showInfo("Family household created: " + currentFamily.getFamilyId());
            log.info("Created family household: {}", currentFamily.getFamilyId());
        } catch (Exception e) {
            log.error("Error creating family", e);
            showError("Failed to create family: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddSibling() {
        try {
            log.info("Adding sibling to family");

            if (currentFamily == null) {
                showError("Please create or search for a family first.");
                return;
            }

            // Show choice dialog: Add existing student or create new student
            Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
            choice.setTitle("Add Sibling");
            choice.setHeaderText("How would you like to add a sibling?");
            choice.setContentText("Choose an option:");

            ButtonType existingButton = new ButtonType("Add Existing Student");
            ButtonType newButton = new ButtonType("Create New Student");
            ButtonType cancelButton = ButtonType.CANCEL;

            choice.getButtonTypes().setAll(existingButton, newButton, cancelButton);

            choice.showAndWait().ifPresent(response -> {
                if (response == existingButton) {
                    handleAddExistingStudent();
                } else if (response == newButton) {
                    handleAddNewChild();
                }
            });

        } catch (Exception e) {
            log.error("Error adding sibling", e);
            showError("Failed to add sibling: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddExistingStudent() {
        try {
            log.info("Adding existing student to family");

            if (currentFamily == null) {
                showError("Please create or search for a family first.");
                return;
            }

            // Prompt for student ID or name
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
            dialog.setTitle("Add Existing Student");
            dialog.setHeaderText("Search for student by ID or name");
            dialog.setContentText("Student ID or Name:");

            dialog.showAndWait().ifPresent(searchTerm -> {
                try {
                    // Try to search by ID first
                    Student student = null;
                    try {
                        Long studentId = Long.parseLong(searchTerm.trim());
                        student = studentService.findById(studentId);
                    } catch (NumberFormatException e) {
                        // Search by name
                        java.util.List<Student> students = studentService.searchByName(searchTerm.trim());
                        if (students.isEmpty()) {
                            showError("No student found matching: " + searchTerm);
                            return;
                        } else if (students.size() > 1) {
                            // Show selection dialog
                            javafx.scene.control.ChoiceDialog<Student> choiceDialog =
                                new javafx.scene.control.ChoiceDialog<>(students.get(0), students);
                            choiceDialog.setTitle("Select Student");
                            choiceDialog.setHeaderText("Multiple students found");
                            choiceDialog.setContentText("Select student:");
                            choiceDialog.showAndWait().ifPresent(selected -> {
                                addStudentToCurrentFamily(selected);
                            });
                            return;
                        } else {
                            student = students.get(0);
                        }
                    }

                    if (student == null) {
                        showError("Student not found: " + searchTerm);
                        return;
                    }

                    addStudentToCurrentFamily(student);

                } catch (Exception e) {
                    log.error("Error searching for student", e);
                    showError("Failed to search for student: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Error adding existing student", e);
            showError("Failed to add student: " + e.getMessage());
        }
    }

    private void addStudentToCurrentFamily(Student student) {
        try {
            // Confirm if student already has a family
            if (student.getFamilyHousehold() != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Student Already in Family");
                confirm.setHeaderText("This student is already part of family: " +
                    student.getFamilyHousehold().getFamilyId());
                confirm.setContentText("Move student to this family?");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return;
                }
            }

            // Add student to family
            currentFamily = familyManagementService.addStudentToFamily(
                currentFamily.getId(),
                student.getId(),
                SecurityContext.getCurrentStaffId()
            );

            // Refresh UI
            updateDiscountLabels();
            showSuccess("Student added to family: " + student.getFullName());
            log.info("Added student {} to family {}", student.getId(), currentFamily.getFamilyId());

        } catch (Exception e) {
            log.error("Error adding student to family", e);
            showError("Failed to add student: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddNewChild() {
        try {
            log.info("Creating new child for family");

            if (currentFamily == null) {
                showError("Please create or search for a family first.");
                return;
            }

            // Create student with basic information from family
            Student newStudent = new Student();
            newStudent.setFamilyHousehold(currentFamily);

            // Pre-populate from family information
            if (currentFamily.getPrimaryAddress() != null) {
                newStudent.setHomeStreetAddress(currentFamily.getPrimaryAddress());
            }
            if (currentFamily.getPrimaryPhone() != null) {
                newStudent.setCellPhone(currentFamily.getPrimaryPhone());
            }
            if (currentFamily.getPrimaryEmail() != null) {
                newStudent.setEmail(currentFamily.getPrimaryEmail());
            }

            // Set enrollment status
            newStudent.setStudentStatus(Student.StudentStatus.ACTIVE);
            newStudent.setActive(true);

            // Save the student
            Student savedStudent = studentService.save(newStudent);

            // Add to family
            currentFamily = familyManagementService.addStudentToFamily(
                currentFamily.getId(),
                savedStudent.getId(),
                SecurityContext.getCurrentStaffId()
            );

            // Refresh UI
            updateDiscountLabels();

            showInfo("New student created with ID: " + savedStudent.getId() +
                "\n\nPlease complete student enrollment form to add full details.");
            log.info("Created new student {} for family {}", savedStudent.getId(), currentFamily.getFamilyId());

        } catch (Exception e) {
            log.error("Error creating new child", e);
            showError("Failed to create new child: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveSibling() {
        try {
            log.info("Removing sibling from family");

            if (currentFamily == null) {
                showError("Please create or search for a family first.");
                return;
            }

            if (siblingsTable == null || siblingsTable.getSelectionModel().getSelectedItem() == null) {
                showError("Please select a student from the siblings table first.");
                return;
            }

            // Get selected student from table
            Object selectedItem = siblingsTable.getSelectionModel().getSelectedItem();
            Student student = null;

            // Handle different table item types
            if (selectedItem instanceof Student) {
                student = (Student) selectedItem;
            } else {
                showError("Unable to identify selected student.");
                return;
            }

            final Student studentToRemove = student;

            // Confirm removal
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Remove Sibling");
            confirm.setHeaderText("Remove student from this family?");
            confirm.setContentText("Student: " + studentToRemove.getFullName() +
                "\n\nThis will recalculate family discounts.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        currentFamily = familyManagementService.removeStudentFromFamily(
                            currentFamily.getId(),
                            studentToRemove.getId(),
                            SecurityContext.getCurrentStaffId()
                        );

                        // Refresh UI
                        updateDiscountLabels();
                        showSuccess("Student removed from family: " + studentToRemove.getFullName());
                        log.info("Removed student {} from family {}", studentToRemove.getId(), currentFamily.getFamilyId());

                    } catch (Exception e) {
                        log.error("Error removing student from family", e);
                        showError("Failed to remove student: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error removing sibling", e);
            showError("Failed to remove sibling: " + e.getMessage());
        }
    }

    @FXML
    private void handleSetPrimaryStudent() {
        try {
            log.info("Setting primary student for family");

            if (currentFamily == null) {
                showError("Please create or search for a family first.");
                return;
            }

            if (siblingsTable == null || siblingsTable.getSelectionModel().getSelectedItem() == null) {
                showError("Please select a student from the siblings table first.");
                return;
            }

            // Get selected student from table
            Object selectedItem = siblingsTable.getSelectionModel().getSelectedItem();
            Student student = null;

            // Handle different table item types
            if (selectedItem instanceof Student) {
                student = (Student) selectedItem;
            } else {
                showError("Unable to identify selected student.");
                return;
            }

            final Student primaryStudent = student;

            // Confirm setting as primary
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Set Primary Student");
            confirm.setHeaderText("Set this student as the primary student for the family?");
            confirm.setContentText("Student: " + primaryStudent.getFullName() +
                "\n\nThe primary student is used for family correspondence and discount calculations.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        currentFamily = familyManagementService.setPrimaryStudent(
                            currentFamily.getId(),
                            primaryStudent.getId(),
                            SecurityContext.getCurrentStaffId()
                        );

                        showSuccess("Primary student set: " + primaryStudent.getFullName());
                        log.info("Set primary student {} for family {}", primaryStudent.getId(), currentFamily.getFamilyId());

                    } catch (Exception e) {
                        log.error("Error setting primary student", e);
                        showError("Failed to set primary student: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            log.error("Error setting primary student", e);
            showError("Failed to set primary student: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewStudentDetails() {
        try {
            log.info("Viewing student details");

            if (siblingsTable == null || siblingsTable.getSelectionModel().getSelectedItem() == null) {
                showError("Please select a student from the siblings table first.");
                return;
            }

            // Get selected student from table
            Object selectedItem = siblingsTable.getSelectionModel().getSelectedItem();
            Student student = null;

            // Handle different table item types
            if (selectedItem instanceof Student) {
                student = (Student) selectedItem;
            } else {
                showError("Unable to identify selected student.");
                return;
            }

            // Display student details in an information dialog
            StringBuilder details = new StringBuilder();
            details.append("Student ID: ").append(student.getId() != null ? student.getId() : "N/A").append("\n");
            details.append("Student Number: ").append(student.getStudentId() != null ? student.getStudentId() : "N/A").append("\n");
            details.append("Name: ").append(student.getFullName() != null ? student.getFullName() : "N/A").append("\n");
            details.append("Grade: ").append(student.getGradeLevel() != null ? student.getGradeLevel() : "N/A").append("\n");
            details.append("DOB: ").append(student.getDateOfBirth() != null ?
                student.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "N/A").append("\n");
            details.append("Gender: ").append(student.getGender() != null ? student.getGender() : "N/A").append("\n");
            details.append("\n");
            details.append("Student Status: ").append(student.getStudentStatus() != null ?
                student.getStudentStatus().getDisplayName() : "N/A").append("\n");
            details.append("Active: ").append(student.isActive() ? "Yes" : "No").append("\n");
            details.append("\n");
            details.append("Home Address: ").append(student.getHomeStreetAddress() != null ? student.getHomeStreetAddress() : "N/A").append("\n");
            details.append("Cell Phone: ").append(student.getCellPhone() != null ? student.getCellPhone() : "N/A").append("\n");
            details.append("Email: ").append(student.getEmail() != null ? student.getEmail() : "N/A").append("\n");
            details.append("\n");
            details.append("Current GPA: ").append(student.getCurrentGPA() != null ?
                String.format("%.2f", student.getCurrentGPA()) : "N/A").append("\n");
            details.append("Credits Earned: ").append(student.getCreditsEarned() != null ?
                String.format("%.1f", student.getCreditsEarned()) : "N/A").append("\n");

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Student Details");
            info.setHeaderText(student.getFullName());
            info.setContentText(details.toString());
            info.setResizable(true);
            info.getDialogPane().setPrefWidth(500);
            info.showAndWait();

            log.info("Displayed details for student {}", student.getId());

        } catch (Exception e) {
            log.error("Error viewing student details", e);
            showError("Failed to view student details: " + e.getMessage());
        }
    }

    @FXML
    private void handleRecalculateDiscounts() {
        calculateDiscounts();
    }

    @FXML
    private void handleApplyDiscounts() {
        if (validateAllFields()) {
            calculateDiscounts();
            showSuccess("Discounts applied to all enrolled children.");
        }
    }

    private void calculateDiscounts() {
        if (currentFamily == null) {
            return;
        }

        try {
            // Save form data to entity
            saveFormDataToEntity();

            // Recalculate discounts
            currentFamily = familyManagementService.calculateFamilyDiscounts(
                currentFamily.getId(),
                SecurityContext.getCurrentStaffId()
            );

            // Update UI
            updateDiscountLabels();
            log.info("Recalculated discounts for family: {}", currentFamily.getFamilyId());
        } catch (Exception e) {
            log.error("Error calculating discounts", e);
            showError("Failed to calculate discounts: " + e.getMessage());
        }
    }

    private void updateDiscountLabels() {
        if (currentFamily == null) return;

        if (totalFamilyDiscountLabel != null) {
            totalFamilyDiscountLabel.setText(String.format("$%.2f",
                currentFamily.getTotalFamilyDiscount() != null ? currentFamily.getTotalFamilyDiscount() : 0.0));
        }
        if (familyDiscountLabel != null) {
            familyDiscountLabel.setText(String.format("$%.2f",
                currentFamily.getTotalFamilyDiscount() != null ? currentFamily.getTotalFamilyDiscount() : 0.0));
        }
        if (enrolledCountLabel != null) {
            enrolledCountLabel.setText(String.valueOf(
                currentFamily.getEnrolledChildren() != null ? currentFamily.getEnrolledChildren() : 0));
        }
        if (totalChildrenLabel != null) {
            totalChildrenLabel.setText(String.valueOf(
                currentFamily.getTotalChildren() != null ? currentFamily.getTotalChildren() : 0));
        }
    }

    @FXML
    private void handleSave() {
        if (!validateAllFields()) {
            return;
        }

        if (currentFamily == null) {
            showError("Please create or search for a family first.");
            return;
        }

        try {
            // Save form data to entity
            saveFormDataToEntity();

            // Save to database
            currentFamily = familyManagementService.updateFamilyHousehold(
                currentFamily,
                SecurityContext.getCurrentStaffId()
            );

            isDirty = false;
            if (lastSavedLabel != null) {
                lastSavedLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            showSuccess("Family information saved successfully.");
            log.info("Saved family household: {}", currentFamily.getFamilyId());
        } catch (Exception e) {
            log.error("Error saving family", e);
            showError("Failed to save: " + e.getMessage());
        }
    }

    /**
     * Save form data to FamilyHousehold entity
     */
    private void saveFormDataToEntity() {
        if (currentFamily == null) return;

        // Family Information
        if (familyNameField != null) {
            currentFamily.setFamilyName(familyNameField.getText());
        }
        if (householdTypeComboBox != null && householdTypeComboBox.getValue() != null) {
            currentFamily.setHouseholdType(FamilyHousehold.HouseholdType.valueOf(
                householdTypeComboBox.getValue().toUpperCase().replace(" ", "_").replace("-", "_")
            ));
        }
        if (primaryAddressArea != null) {
            currentFamily.setPrimaryAddress(primaryAddressArea.getText());
        }
        if (primaryParentField != null) {
            currentFamily.setPrimaryParentName(primaryParentField.getText());
        }
        if (primaryPhoneField != null) {
            currentFamily.setPrimaryPhone(primaryPhoneField.getText());
        }
        if (primaryEmailField != null) {
            currentFamily.setPrimaryEmail(primaryEmailField.getText());
        }
        if (familyNotesArea != null) {
            currentFamily.setFamilyNotes(familyNotesArea.getText());
        }

        // Parent 1
        if (parent1NameField != null) {
            currentFamily.setParent1Name(parent1NameField.getText());
        }
        if (parent1PhoneField != null) {
            currentFamily.setParent1Phone(parent1PhoneField.getText());
        }
        if (parent1EmailField != null) {
            currentFamily.setParent1Email(parent1EmailField.getText());
        }
        if (parent1WorkPhoneField != null) {
            currentFamily.setParent1WorkPhone(parent1WorkPhoneField.getText());
        }
        if (parent1EmployerField != null) {
            currentFamily.setParent1Employer(parent1EmployerField.getText());
        }
        if (parent1LivesInHouseholdCheckbox != null) {
            currentFamily.setParent1LivesInHousehold(parent1LivesInHouseholdCheckbox.isSelected());
        }
        if (parent1CustodialCheckbox != null) {
            currentFamily.setParent1IsCustodial(parent1CustodialCheckbox.isSelected());
        }

        // Parent 2
        if (parent2NameField != null) {
            currentFamily.setParent2Name(parent2NameField.getText());
        }
        if (parent2PhoneField != null) {
            currentFamily.setParent2Phone(parent2PhoneField.getText());
        }
        if (parent2EmailField != null) {
            currentFamily.setParent2Email(parent2EmailField.getText());
        }
        if (parent2WorkPhoneField != null) {
            currentFamily.setParent2WorkPhone(parent2WorkPhoneField.getText());
        }
        if (parent2EmployerField != null) {
            currentFamily.setParent2Employer(parent2EmployerField.getText());
        }
        if (parent2LivesInHouseholdCheckbox != null) {
            currentFamily.setParent2LivesInHousehold(parent2LivesInHouseholdCheckbox.isSelected());
        }
        if (parent2CustodialCheckbox != null) {
            currentFamily.setParent2IsCustodial(parent2CustodialCheckbox.isSelected());
        }

        // Emergency Contacts
        if (emergency1NameField != null) {
            currentFamily.setEmergency1Name(emergency1NameField.getText());
        }
        if (emergency1PhoneField != null) {
            currentFamily.setEmergency1Phone(emergency1PhoneField.getText());
        }
        if (emergency1RelationField != null) {
            currentFamily.setEmergency1Relation(emergency1RelationField.getText());
        }
        if (emergency2NameField != null) {
            currentFamily.setEmergency2Name(emergency2NameField.getText());
        }
        if (emergency2PhoneField != null) {
            currentFamily.setEmergency2Phone(emergency2PhoneField.getText());
        }
        if (emergency2RelationField != null) {
            currentFamily.setEmergency2Relation(emergency2RelationField.getText());
        }

        // Discounts
        if (discount2ndChildCheckbox != null) {
            currentFamily.setDiscount2ndChild(discount2ndChildCheckbox.isSelected());
        }
        if (discount3rdPlusCheckbox != null) {
            currentFamily.setDiscount3rdPlusChildren(discount3rdPlusCheckbox.isSelected());
        }
        if (earlyBirdDiscountCheckbox != null) {
            currentFamily.setEarlyBirdDiscountApplied(earlyBirdDiscountCheckbox.isSelected());
        }
        if (waiveTechFees3rdPlusCheckbox != null) {
            currentFamily.setWaiveTechFees3rdPlus(waiveTechFees3rdPlusCheckbox.isSelected());
        }

        // Custody
        if (custodyArrangementComboBox != null && custodyArrangementComboBox.getValue() != null) {
            currentFamily.setCustodyArrangement(FamilyHousehold.CustodyArrangement.valueOf(
                custodyArrangementComboBox.getValue().toUpperCase().replace(" ", "_").replace("-", "_")
            ));
        }
        if (custodyPapersCheckbox != null) {
            currentFamily.setCustodyPapersOnFile(custodyPapersCheckbox.isSelected());
        }
        if (pickupRestrictionsArea != null) {
            currentFamily.setPickupRestrictions(pickupRestrictionsArea.getText());
        }
        if (specialFamilyNotesArea != null) {
            currentFamily.setSpecialFamilyNotes(specialFamilyNotesArea.getText());
        }
    }

    @FXML
    private void handleClose() {
        if (isDirty) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setContentText("Save changes before closing?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    handleSave();
                }
            });
        }
        closeForm();
    }

    private boolean validateAllFields() {
        StringBuilder errors = new StringBuilder();

        if (familyNameField == null || familyNameField.getText().trim().isEmpty()) {
            errors.append("• Family name is required\n");
        }
        if (primaryParentField == null || primaryParentField.getText().trim().isEmpty()) {
            errors.append("• Primary parent/guardian is required\n");
        }
        if (primaryPhoneField == null || primaryPhoneField.getText().trim().isEmpty()) {
            errors.append("• Primary phone is required\n");
        }

        if (errors.length() > 0) {
            showError("Validation Error:\n" + errors.toString());
            return false;
        }
        return true;
    }

    private void closeForm() {
        if (familyIdField != null && familyIdField.getScene() != null) {
            Stage stage = (Stage) familyIdField.getScene().getWindow();
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

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
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
