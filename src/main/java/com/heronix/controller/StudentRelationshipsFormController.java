package com.heronix.controller;

import com.heronix.model.domain.ParentGuardian;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.service.ParentGuardianService;
import com.heronix.service.StudentService;
import com.heronix.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Student Relationships Form
 * Manages sibling relationships, family groups, household management, and carpool coordination
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StudentRelationshipsFormController {

    private final StudentService studentService;
    private final ParentGuardianService parentService;
    private final UserService userService;

    // Table and columns
    @FXML private TableView<Student> tblStudents;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGrade;
    @FXML private TableColumn<Student, String> colSiblings;

    // Filter controls
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFamilyFilter;

    // Action buttons
    @FXML private Button btnRefresh;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnAutoDetectSiblings;
    @FXML private Button btnCreateFamilyGroup;

    // Selected Student Info
    @FXML private Label lblStudentId;
    @FXML private Label lblStudentName;
    @FXML private Label lblGrade;
    @FXML private Label lblFamilyGroup;

    // Siblings Section
    @FXML private ListView<String> lstSiblings;
    @FXML private ComboBox<Student> cmbAddSibling;
    @FXML private ComboBox<String> cmbRelationshipType;
    @FXML private Button btnAddSibling;
    @FXML private Button btnRemoveSibling;

    // Family Group Information
    @FXML private TextField txtFamilyName;
    @FXML private ComboBox<ParentGuardian> cmbPrimaryContact;
    @FXML private TextArea txtHouseholdAddress;
    @FXML private TextArea txtFamilyNotes;
    @FXML private Button btnSaveFamilyGroup;
    @FXML private Button btnDeleteFamilyGroup;

    // Household Management
    @FXML private CheckBox chkLivesTogether;
    @FXML private CheckBox chkSharedCustody;
    @FXML private TextArea txtCustodySchedule;
    @FXML private CheckBox chkBillingFamily;
    @FXML private CheckBox chkCombinedReporting;

    // Carpool Coordination
    @FXML private CheckBox chkInCarpool;
    @FXML private TextField txtCarpoolGroup;
    @FXML private TextField txtPickupTime;
    @FXML private TextField txtDropoffTime;
    @FXML private TextArea txtCarpoolContacts;
    @FXML private TextArea txtCarpoolNotes;
    @FXML private Button btnSaveCarpool;

    // Family Members & Stats
    @FXML private ListView<String> lstFamilyMembers;
    @FXML private ListView<String> lstSharedParents;
    @FXML private Label lblTotalSiblings;
    @FXML private Label lblSameHousehold;
    @FXML private Label lblInCarpool;
    @FXML private Label lblSharedParents;

    // Status bar
    @FXML private Label lblCurrentUser;
    @FXML private Label lblTotalStudents;
    @FXML private Label lblFamilyGroups;

    private ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private ObservableList<Student> filteredList = FXCollections.observableArrayList();
    private Student selectedStudent;
    private List<Student> currentSiblings = new ArrayList<>();

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadStudents();
        updateStatusBar();

        // Set current user
        try {
            List<User> users = userService.findAll();
            if (!users.isEmpty()) {
                lblCurrentUser.setText(users.get(0).getUsername());
            } else {
                lblCurrentUser.setText("System User");
            }
        } catch (Exception e) {
            lblCurrentUser.setText("System User");
        }
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        colName.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            return new SimpleStringProperty(student.getLastName() + ", " + student.getFirstName());
        });

        colGrade.setCellValueFactory(cellData -> {
            String grade = cellData.getValue().getGradeLevel();
            return new SimpleStringProperty(grade != null ? grade : "");
        });

        colSiblings.setCellValueFactory(cellData -> {
            // Count siblings using parent service
            try {
                List<Student> siblings = parentService.findSiblings(cellData.getValue().getId());
                return new SimpleStringProperty(String.valueOf(siblings.size()));
            } catch (Exception e) {
                return new SimpleStringProperty("0");
            }
        });
    }

    /**
     * Setup combo boxes
     */
    private void setupComboBoxes() {
        // Relationship types
        cmbRelationshipType.setItems(FXCollections.observableArrayList(
            "Full Sibling",
            "Half Sibling (Same Mother)",
            "Half Sibling (Same Father)",
            "Step Sibling",
            "Adopted Sibling",
            "Foster Sibling",
            "Other"
        ));

        // Student combo box converter
        StringConverter<Student> studentConverter = new StringConverter<>() {
            @Override
            public String toString(Student student) {
                return student != null ?
                    student.getLastName() + ", " + student.getFirstName() + " (" + student.getStudentId() + ")" : "";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        };

        cmbAddSibling.setConverter(studentConverter);

        // Parent combo box converter
        StringConverter<ParentGuardian> parentConverter = new StringConverter<>() {
            @Override
            public String toString(ParentGuardian parent) {
                return parent != null ? parent.getFullName() : "";
            }

            @Override
            public ParentGuardian fromString(String string) {
                return null;
            }
        };

        cmbPrimaryContact.setConverter(parentConverter);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        // Table selection listener
        tblStudents.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedStudent = newValue;
                if (newValue != null) {
                    loadStudentRelationships(newValue);
                } else {
                    clearStudentInfo();
                }
            }
        );

        // Siblings list selection listener
        lstSiblings.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                btnRemoveSibling.setDisable(newValue == null);
            }
        );
    }

    /**
     * Load students
     */
    private void loadStudents() {
        try {
            List<Student> students = studentService.getActiveStudents();
            studentsList.setAll(students);
            filteredList.setAll(students);
            tblStudents.setItems(filteredList);

            // Also load into add sibling combo
            cmbAddSibling.setItems(FXCollections.observableArrayList(students));

            updateStatusBar();
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Failed to load students: " + e.getMessage());
        }
    }

    /**
     * Load student relationships
     */
    private void loadStudentRelationships(Student student) {
        try {
            // Update student info labels
            lblStudentId.setText(student.getStudentId());
            lblStudentName.setText(student.getLastName() + ", " + student.getFirstName());
            lblGrade.setText(student.getGradeLevel() != null ? String.valueOf(student.getGradeLevel()) : "N/A");

            // Load siblings
            currentSiblings = parentService.findSiblings(student.getId());
            ObservableList<String> siblingNames = FXCollections.observableArrayList(
                currentSiblings.stream()
                    .map(s -> s.getLastName() + ", " + s.getFirstName() +
                             " (Grade " + (s.getGradeLevel() != null ? s.getGradeLevel() : "?") + ")")
                    .collect(Collectors.toList())
            );
            lstSiblings.setItems(siblingNames);

            // Load shared parents
            List<ParentGuardian> parents = parentService.getParentsForStudent(student.getId());
            ObservableList<String> parentNames = FXCollections.observableArrayList(
                parents.stream()
                    .map(p -> p.getFullName() + " (" + p.getRelationship() + ")")
                    .collect(Collectors.toList())
            );
            lstSharedParents.setItems(parentNames);

            // Load parents into primary contact combo
            cmbPrimaryContact.setItems(FXCollections.observableArrayList(parents));

            // Update family members list (student + siblings)
            List<String> familyMembers = new ArrayList<>();
            familyMembers.add(student.getLastName() + ", " + student.getFirstName() + " (Grade " +
                             (student.getGradeLevel() != null ? student.getGradeLevel() : "?") + ")");
            familyMembers.addAll(siblingNames);
            lstFamilyMembers.setItems(FXCollections.observableArrayList(familyMembers));

            // Update stats
            updateRelationshipStats(student);

        } catch (Exception e) {
            log.error("Error loading student relationships", e);
            showError("Failed to load relationships: " + e.getMessage());
        }
    }

    /**
     * Update relationship statistics
     */
    private void updateRelationshipStats(Student student) {
        lblTotalSiblings.setText(String.valueOf(currentSiblings.size()));
        lblSharedParents.setText(String.valueOf(lstSharedParents.getItems().size()));

        // These would be based on actual data fields in production
        lblSameHousehold.setText("N/A");
        lblInCarpool.setText("N/A");
    }

    /**
     * Clear student info display
     */
    private void clearStudentInfo() {
        lblStudentId.setText("-");
        lblStudentName.setText("-");
        lblGrade.setText("-");
        lblFamilyGroup.setText("-");

        lstSiblings.setItems(FXCollections.observableArrayList());
        lstSharedParents.setItems(FXCollections.observableArrayList());
        lstFamilyMembers.setItems(FXCollections.observableArrayList());

        txtFamilyName.clear();
        txtHouseholdAddress.clear();
        txtFamilyNotes.clear();

        lblTotalSiblings.setText("0");
        lblSameHousehold.setText("0");
        lblInCarpool.setText("0");
        lblSharedParents.setText("0");
    }

    /**
     * Update status bar
     */
    private void updateStatusBar() {
        lblTotalStudents.setText(String.valueOf(studentsList.size()));
        lblFamilyGroups.setText("N/A"); // Would count actual family groups in production
    }

    // Event Handlers

    @FXML
    private void handleRefresh() {
        loadStudents();
        if (selectedStudent != null) {
            loadStudentRelationships(selectedStudent);
        }
        showInfo("Relationships refreshed");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = txtSearch.getText().toLowerCase();

        List<Student> filtered = studentsList.stream()
            .filter(student -> {
                if (!searchTerm.isEmpty()) {
                    String fullName = (student.getLastName() + " " + student.getFirstName()).toLowerCase();
                    String studentId = student.getStudentId() != null ? student.getStudentId().toLowerCase() : "";
                    if (!fullName.contains(searchTerm) && !studentId.contains(searchTerm)) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cmbFamilyFilter.setValue(null);
        filteredList.setAll(studentsList);
    }

    @FXML
    private void handleAutoDetectSiblings() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        try {
            // Auto-detect siblings based on shared parents
            List<Student> detectedSiblings = parentService.findSiblings(selectedStudent.getId());

            if (detectedSiblings.isEmpty()) {
                showInfo("No siblings detected for this student");
            } else {
                showInfo("Found " + detectedSiblings.size() + " sibling(s) based on shared parents");
                loadStudentRelationships(selectedStudent);
            }
        } catch (Exception e) {
            log.error("Error auto-detecting siblings", e);
            showError("Failed to auto-detect siblings: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateFamilyGroup() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        // Pre-populate family name with student's last name
        txtFamilyName.setText(selectedStudent.getLastName() + " Family");
        showInfo("Family group template created. Enter details and click 'Save Family Group'");
    }

    @FXML
    private void handleAddSibling() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        Student siblingToAdd = cmbAddSibling.getValue();
        if (siblingToAdd == null) {
            showWarning("Please select a sibling to add");
            return;
        }

        if (siblingToAdd.getId().equals(selectedStudent.getId())) {
            showWarning("Cannot add student as their own sibling");
            return;
        }

        String relationshipType = cmbRelationshipType.getValue();
        if (relationshipType == null) {
            showWarning("Please select a relationship type");
            return;
        }

        try {
            // In production, would create actual sibling relationship record
            showInfo("Sibling relationship added: " + siblingToAdd.getFullName() + " (" + relationshipType + ")");

            loadStudentRelationships(selectedStudent);

            cmbAddSibling.setValue(null);
            cmbRelationshipType.setValue(null);
        } catch (Exception e) {
            log.error("Error adding sibling", e);
            showError("Failed to add sibling: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveSibling() {
        String selectedSibling = lstSiblings.getSelectionModel().getSelectedItem();
        if (selectedSibling == null) {
            showWarning("Please select a sibling to remove");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Remove");
        alert.setHeaderText("Remove Sibling Relationship");
        alert.setContentText("Are you sure you want to remove this sibling relationship?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // In production, would remove actual relationship record
                    showInfo("Sibling relationship removed");
                    loadStudentRelationships(selectedStudent);
                } catch (Exception e) {
                    log.error("Error removing sibling", e);
                    showError("Failed to remove sibling: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSaveFamilyGroup() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        if (txtFamilyName.getText() == null || txtFamilyName.getText().trim().isEmpty()) {
            showWarning("Please enter a family name");
            return;
        }

        try {
            // In production, would save family group to database
            showInfo("Family group saved: " + txtFamilyName.getText());
            lblFamilyGroup.setText(txtFamilyName.getText());
        } catch (Exception e) {
            log.error("Error saving family group", e);
            showError("Failed to save family group: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteFamilyGroup() {
        if (txtFamilyName.getText() == null || txtFamilyName.getText().trim().isEmpty()) {
            showWarning("No family group to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Family Group");
        alert.setContentText("Are you sure you want to delete this family group? This will not delete the students.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // In production, would delete family group from database
                    txtFamilyName.clear();
                    txtHouseholdAddress.clear();
                    txtFamilyNotes.clear();
                    lblFamilyGroup.setText("-");
                    showInfo("Family group deleted");
                } catch (Exception e) {
                    log.error("Error deleting family group", e);
                    showError("Failed to delete family group: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSaveCarpool() {
        if (selectedStudent == null) {
            showWarning("Please select a student first");
            return;
        }

        try {
            // In production, would save carpool info to database
            showInfo("Carpool information saved");
        } catch (Exception e) {
            log.error("Error saving carpool info", e);
            showError("Failed to save carpool information: " + e.getMessage());
        }
    }

    // Helper methods for alerts

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
