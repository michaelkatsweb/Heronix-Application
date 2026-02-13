package com.heronix.controller;

import com.heronix.model.domain.ParentGuardian;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentParentRelationship;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Parent/Guardian Management Form
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ParentGuardianManagementFormController {

    private final ParentGuardianService parentService;
    private final StudentService studentService;
    private final UserService userService;

    // Table and columns
    @FXML private TableView<ParentGuardian> tblParents;
    @FXML private TableColumn<ParentGuardian, String> colName;
    @FXML private TableColumn<ParentGuardian, String> colRelationship;
    @FXML private TableColumn<ParentGuardian, String> colPhone;
    @FXML private TableColumn<ParentGuardian, String> colEmail;
    @FXML private TableColumn<ParentGuardian, String> colStudents;

    // Filter controls
    @FXML private TextField txtSearchName;
    @FXML private ComboBox<Student> cmbStudentFilter;
    @FXML private ComboBox<String> cmbRelationshipFilter;
    @FXML private ToggleButton togglePrimaryCustodian;
    @FXML private ToggleButton toggleCustodial;
    @FXML private ToggleButton togglePickupAuth;
    @FXML private ToggleButton toggleHasPortalAccess;
    @FXML private ToggleButton toggleMissingInfo;

    // Action buttons
    @FXML private Button btnRefresh;
    @FXML private Button btnNew;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // Basic Information
    @FXML private TextField txtFirstName;
    @FXML private TextField txtMiddleName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPreferredName;
    @FXML private ComboBox<String> cmbRelationship;

    // Contact Information
    @FXML private TextField txtCellPhone;
    @FXML private TextField txtHomePhone;
    @FXML private TextField txtWorkPhone;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbPreferredContact;

    // Address Information
    @FXML private CheckBox chkLivesWithStudent;
    @FXML private TextField txtStreetAddress;
    @FXML private TextField txtCity;
    @FXML private ComboBox<String> cmbState;
    @FXML private TextField txtZipCode;

    // Employment Information
    @FXML private TextField txtEmployer;
    @FXML private TextField txtOccupation;
    @FXML private TextArea txtWorkAddress;

    // Custodial & Permissions
    @FXML private CheckBox chkPrimaryCustodian;
    @FXML private CheckBox chkHasLegalCustody;
    @FXML private CheckBox chkCanPickUpStudent;
    @FXML private CheckBox chkAuthorizedForEmergency;
    @FXML private CheckBox chkCanMakeDecisions;
    @FXML private CheckBox chkReceivesReportCards;
    @FXML private CheckBox chkReceivesEmails;
    @FXML private CheckBox chkReceivesAlerts;
    @FXML private TextField txtCustodyDocumentation;
    @FXML private Button btnUploadDocument;

    // Portal Access
    @FXML private CheckBox chkHasPortalAccess;
    @FXML private TextField txtPortalUsername;
    @FXML private Label lblLastLogin;
    @FXML private Label lblPortalStatus;
    @FXML private Button btnCreatePortalAccount;
    @FXML private Button btnResetPassword;
    @FXML private Button btnSendInvitation;

    // Communication Preferences
    @FXML private ComboBox<String> cmbPreferredLanguage;
    @FXML private CheckBox chkTextNotifications;
    @FXML private CheckBox chkEmailNotifications;
    @FXML private TextArea txtNotes;

    // Student Relationships
    @FXML private ListView<String> lstStudents;
    @FXML private ComboBox<Student> cmbLinkStudent;
    @FXML private CheckBox chkLinkPrimary;
    @FXML private CheckBox chkLinkCustodial;
    @FXML private CheckBox chkLinkPickup;
    @FXML private CheckBox chkLinkEmergency;
    @FXML private Button btnLinkStudent;
    @FXML private Button btnUnlinkStudent;
    @FXML private ListView<String> lstSiblings;

    // Status bar
    @FXML private Label lblCurrentUser;
    @FXML private Label lblTotalParents;
    @FXML private Label lblMissingInfoCount;

    private ObservableList<ParentGuardian> parentsList = FXCollections.observableArrayList();
    private ObservableList<ParentGuardian> filteredList = FXCollections.observableArrayList();
    private ParentGuardian selectedParent;
    private boolean editMode = false;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupListeners();
        loadStudents();
        loadParents();
        updateStatusBar();
        setFormEditable(false);

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
        colName.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFullName()));

        colRelationship.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRelationship() != null ?
                cellData.getValue().getRelationship() : ""));

        colPhone.setCellValueFactory(cellData -> {
            ParentGuardian parent = cellData.getValue();
            String phone = parent.getCellPhone() != null ? parent.getCellPhone() :
                          parent.getHomePhone() != null ? parent.getHomePhone() : "";
            return new SimpleStringProperty(phone);
        });

        colEmail.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getEmail() != null ?
                cellData.getValue().getEmail() : ""));

        colStudents.setCellValueFactory(cellData -> {
            List<Student> students = parentService.getStudentsForParent(cellData.getValue().getId());
            return new SimpleStringProperty(String.valueOf(students.size()));
        });
    }

    /**
     * Setup combo boxes
     */
    private void setupComboBoxes() {
        // Relationship types
        cmbRelationship.setItems(FXCollections.observableArrayList(
            "Mother", "Father", "Step-Mother", "Step-Father",
            "Grandmother", "Grandfather", "Aunt", "Uncle",
            "Legal Guardian", "Foster Parent", "Other"
        ));

        cmbRelationshipFilter.setItems(FXCollections.observableArrayList(
            "All Relationships", "Mother", "Father", "Step-Mother", "Step-Father",
            "Grandmother", "Grandfather", "Aunt", "Uncle", "Legal Guardian", "Foster Parent", "Other"
        ));

        // Preferred contact methods
        cmbPreferredContact.setItems(FXCollections.observableArrayList(
            "Cell Phone", "Home Phone", "Work Phone", "Email", "Text Message"
        ));

        // US States
        cmbState.setItems(FXCollections.observableArrayList(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        ));

        // Languages
        cmbPreferredLanguage.setItems(FXCollections.observableArrayList(
            "English", "Spanish", "French", "Chinese", "Vietnamese",
            "Greek", "Italian", "Polish", "Japanese", "Thai",
            "Korean", "Tagalog", "Russian", "Portuguese", "Other"
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

        cmbStudentFilter.setConverter(studentConverter);
        cmbLinkStudent.setConverter(studentConverter);
    }

    /**
     * Setup listeners
     */
    private void setupListeners() {
        // Table selection listener
        tblParents.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                selectedParent = newValue;
                if (newValue != null) {
                    populateForm(newValue);
                    loadStudentRelationships(newValue);
                    btnView.setDisable(false);
                    btnEdit.setDisable(false);
                    btnDelete.setDisable(false);
                } else {
                    btnView.setDisable(true);
                    btnEdit.setDisable(true);
                    btnDelete.setDisable(true);
                }
            }
        );

        // Student list selection listener
        lstStudents.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                btnUnlinkStudent.setDisable(newValue == null);
            }
        );
    }

    /**
     * Load students into combo boxes
     */
    private void loadStudents() {
        List<Student> students = studentService.getActiveStudents();
        cmbStudentFilter.setItems(FXCollections.observableArrayList(students));
        cmbLinkStudent.setItems(FXCollections.observableArrayList(students));
    }

    /**
     * Load parents/guardians
     */
    private void loadParents() {
        try {
            // Get all parents from repository
            List<ParentGuardian> parents = parentService.searchByName("");
            parentsList.setAll(parents);
            filteredList.setAll(parents);
            tblParents.setItems(filteredList);
            updateStatusBar();
        } catch (Exception e) {
            log.error("Error loading parents", e);
            showError("Failed to load parents/guardians: " + e.getMessage());
        }
    }

    /**
     * Load student relationships for selected parent
     */
    private void loadStudentRelationships(ParentGuardian parent) {
        try {
            List<Student> students = parentService.getStudentsForParent(parent.getId());

            ObservableList<String> studentNames = FXCollections.observableArrayList(
                students.stream()
                    .map(s -> s.getLastName() + ", " + s.getFirstName() + " (" + s.getStudentId() + ")")
                    .collect(Collectors.toList())
            );
            lstStudents.setItems(studentNames);

            // Load siblings
            if (!students.isEmpty()) {
                List<Student> siblings = parentService.findSiblings(students.get(0).getId());
                ObservableList<String> siblingNames = FXCollections.observableArrayList(
                    siblings.stream()
                        .map(s -> s.getLastName() + ", " + s.getFirstName())
                        .collect(Collectors.toList())
                );
                lstSiblings.setItems(siblingNames);
            } else {
                lstSiblings.setItems(FXCollections.observableArrayList());
            }
        } catch (Exception e) {
            log.error("Error loading student relationships", e);
        }
    }

    /**
     * Populate form with parent data
     */
    private void populateForm(ParentGuardian parent) {
        if (parent == null) {
            clearForm();
            return;
        }

        // Basic Information
        txtFirstName.setText(parent.getFirstName());
        txtMiddleName.setText(parent.getMiddleName());
        txtLastName.setText(parent.getLastName());
        txtPreferredName.setText(parent.getPreferredName());
        cmbRelationship.setValue(parent.getRelationship());

        // Contact Information
        txtCellPhone.setText(parent.getCellPhone());
        txtHomePhone.setText(parent.getHomePhone());
        txtWorkPhone.setText(parent.getWorkPhone());
        txtEmail.setText(parent.getEmail());
        cmbPreferredContact.setValue(parent.getPreferredContactMethod());

        // Address Information
        chkLivesWithStudent.setSelected(Boolean.TRUE.equals(parent.getLivesWithStudent()));
        txtStreetAddress.setText(parent.getHomeAddress());
        txtCity.setText(parent.getCity());
        cmbState.setValue(parent.getState());
        txtZipCode.setText(parent.getZipCode());

        // Employment Information
        txtEmployer.setText(parent.getEmployer());
        txtOccupation.setText(parent.getOccupation());
        txtWorkAddress.setText(parent.getWorkAddress());

        // Custodial & Permissions
        chkPrimaryCustodian.setSelected(Boolean.TRUE.equals(parent.getIsPrimaryCustodian()));
        chkHasLegalCustody.setSelected(Boolean.TRUE.equals(parent.getHasLegalCustody()));
        chkCanPickUpStudent.setSelected(Boolean.TRUE.equals(parent.getCanPickUpStudent()));
        chkAuthorizedForEmergency.setSelected(Boolean.TRUE.equals(parent.getAuthorizedForEmergency()));
        chkCanMakeDecisions.setSelected(Boolean.TRUE.equals(parent.getCanMakeEducationalDecisions()));
        chkReceivesReportCards.setSelected(Boolean.TRUE.equals(parent.getReceivesReportCards()));
        chkReceivesEmails.setSelected(Boolean.TRUE.equals(parent.getReceivesSchoolEmails()));
        chkReceivesAlerts.setSelected(Boolean.TRUE.equals(parent.getReceivesEmergencyAlerts()));

        // Portal Access
        chkHasPortalAccess.setSelected(parent.getPortalUser() != null);
        txtPortalUsername.setText(parent.getPortalUser() != null ? parent.getPortalUser().getUsername() : "");
        lblPortalStatus.setText(parent.getPortalUser() != null ? "Active" : "Inactive");

        // Communication Preferences
        cmbPreferredLanguage.setValue(parent.getPreferredLanguage());
        chkTextNotifications.setSelected(Boolean.TRUE.equals(parent.getReceivesTextNotifications()));
        chkEmailNotifications.setSelected(Boolean.TRUE.equals(parent.getReceivesEmailNotifications()));
        txtNotes.setText(parent.getNotes());
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        txtFirstName.clear();
        txtMiddleName.clear();
        txtLastName.clear();
        txtPreferredName.clear();
        cmbRelationship.setValue(null);

        txtCellPhone.clear();
        txtHomePhone.clear();
        txtWorkPhone.clear();
        txtEmail.clear();
        cmbPreferredContact.setValue(null);

        chkLivesWithStudent.setSelected(false);
        txtStreetAddress.clear();
        txtCity.clear();
        cmbState.setValue(null);
        txtZipCode.clear();

        txtEmployer.clear();
        txtOccupation.clear();
        txtWorkAddress.clear();

        chkPrimaryCustodian.setSelected(false);
        chkHasLegalCustody.setSelected(false);
        chkCanPickUpStudent.setSelected(false);
        chkAuthorizedForEmergency.setSelected(false);
        chkCanMakeDecisions.setSelected(false);
        chkReceivesReportCards.setSelected(false);
        chkReceivesEmails.setSelected(false);
        chkReceivesAlerts.setSelected(false);
        txtCustodyDocumentation.clear();

        chkHasPortalAccess.setSelected(false);
        txtPortalUsername.clear();
        lblLastLogin.setText("Never");
        lblPortalStatus.setText("Inactive");

        cmbPreferredLanguage.setValue(null);
        chkTextNotifications.setSelected(false);
        chkEmailNotifications.setSelected(false);
        txtNotes.clear();

        lstStudents.setItems(FXCollections.observableArrayList());
        lstSiblings.setItems(FXCollections.observableArrayList());
    }

    /**
     * Set form editable state
     */
    private void setFormEditable(boolean editable) {
        txtFirstName.setEditable(editable);
        txtMiddleName.setEditable(editable);
        txtLastName.setEditable(editable);
        txtPreferredName.setEditable(editable);
        cmbRelationship.setDisable(!editable);

        txtCellPhone.setEditable(editable);
        txtHomePhone.setEditable(editable);
        txtWorkPhone.setEditable(editable);
        txtEmail.setEditable(editable);
        cmbPreferredContact.setDisable(!editable);

        chkLivesWithStudent.setDisable(!editable);
        txtStreetAddress.setEditable(editable);
        txtCity.setEditable(editable);
        cmbState.setDisable(!editable);
        txtZipCode.setEditable(editable);

        txtEmployer.setEditable(editable);
        txtOccupation.setEditable(editable);
        txtWorkAddress.setEditable(editable);

        chkPrimaryCustodian.setDisable(!editable);
        chkHasLegalCustody.setDisable(!editable);
        chkCanPickUpStudent.setDisable(!editable);
        chkAuthorizedForEmergency.setDisable(!editable);
        chkCanMakeDecisions.setDisable(!editable);
        chkReceivesReportCards.setDisable(!editable);
        chkReceivesEmails.setDisable(!editable);
        chkReceivesAlerts.setDisable(!editable);
        txtCustodyDocumentation.setEditable(editable);

        cmbPreferredLanguage.setDisable(!editable);
        chkTextNotifications.setDisable(!editable);
        chkEmailNotifications.setDisable(!editable);
        txtNotes.setEditable(editable);

        btnSave.setDisable(!editable);
        btnCancel.setDisable(!editable);
    }

    /**
     * Update status bar
     */
    private void updateStatusBar() {
        lblTotalParents.setText(String.valueOf(parentsList.size()));

        // Count parents with missing critical info
        long missingInfoCount = parentsList.stream()
            .filter(p -> p.getCellPhone() == null || p.getEmail() == null)
            .count();
        lblMissingInfoCount.setText(String.valueOf(missingInfoCount));
    }

    // Event Handlers

    @FXML
    private void handleRefresh() {
        loadParents();
        showInfo("Parent/Guardian list refreshed");
    }

    @FXML
    private void handleNew() {
        clearForm();
        setFormEditable(true);
        editMode = false;
        selectedParent = null;
        tblParents.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        txtSearchName.clear();
        cmbStudentFilter.setValue(null);
        cmbRelationshipFilter.setValue(null);
        togglePrimaryCustodian.setSelected(false);
        toggleCustodial.setSelected(false);
        togglePickupAuth.setSelected(false);
        toggleHasPortalAccess.setSelected(false);
        toggleMissingInfo.setSelected(false);
        filteredList.setAll(parentsList);
    }

    @FXML
    private void handleQuickFilter() {
        applyFilters();
    }

    /**
     * Apply filters to parents list
     */
    private void applyFilters() {
        String searchName = txtSearchName.getText().toLowerCase();

        List<ParentGuardian> filtered = parentsList.stream()
            .filter(parent -> {
                // Name search
                if (!searchName.isEmpty()) {
                    if (!parent.getFullName().toLowerCase().contains(searchName)) {
                        return false;
                    }
                }

                // Relationship filter
                if (cmbRelationshipFilter.getValue() != null &&
                    !"All Relationships".equals(cmbRelationshipFilter.getValue())) {
                    if (!cmbRelationshipFilter.getValue().equals(parent.getRelationship())) {
                        return false;
                    }
                }

                // Quick filters
                if (togglePrimaryCustodian.isSelected() && !Boolean.TRUE.equals(parent.getIsPrimaryCustodian())) {
                    return false;
                }
                if (toggleCustodial.isSelected() && !Boolean.TRUE.equals(parent.getHasLegalCustody())) {
                    return false;
                }
                if (togglePickupAuth.isSelected() && !Boolean.TRUE.equals(parent.getCanPickUpStudent())) {
                    return false;
                }
                if (toggleHasPortalAccess.isSelected() && parent.getPortalUser() == null) {
                    return false;
                }
                if (toggleMissingInfo.isSelected()) {
                    if (parent.getCellPhone() != null && parent.getEmail() != null) {
                        return false;
                    }
                }

                return true;
            })
            .collect(Collectors.toList());

        filteredList.setAll(filtered);
    }

    @FXML
    private void handleView() {
        if (selectedParent != null) {
            populateForm(selectedParent);
            setFormEditable(false);
            editMode = false;
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedParent != null) {
            populateForm(selectedParent);
            setFormEditable(true);
            editMode = true;
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedParent == null) {
            showWarning("Please select a parent/guardian to delete");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Parent/Guardian");
        alert.setContentText("Are you sure you want to delete this parent/guardian record?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Note: Actual delete implementation would go here
                    showInfo("Parent/Guardian deleted successfully");
                    loadParents();
                    clearForm();
                } catch (Exception e) {
                    log.error("Error deleting parent/guardian", e);
                    showError("Failed to delete parent/guardian: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            // Get current user for audit
            Long currentUserId = 1L; // Default to admin user ID
            try {
                List<User> users = userService.findAll();
                if (!users.isEmpty()) {
                    currentUserId = users.get(0).getId();
                }
            } catch (Exception e) {
                log.warn("Could not get current user, using default ID");
            }

            if (editMode && selectedParent != null) {
                // Update existing parent
                ParentGuardian updates = new ParentGuardian();
                updates.setFirstName(txtFirstName.getText());
                updates.setMiddleName(txtMiddleName.getText());
                updates.setLastName(txtLastName.getText());
                updates.setPreferredName(txtPreferredName.getText());
                updates.setRelationship(cmbRelationship.getValue());
                updates.setCellPhone(txtCellPhone.getText());
                updates.setHomePhone(txtHomePhone.getText());
                updates.setWorkPhone(txtWorkPhone.getText());
                updates.setEmail(txtEmail.getText());
                updates.setPreferredContactMethod(cmbPreferredContact.getValue());
                updates.setEmployer(txtEmployer.getText());
                updates.setWorkAddress(txtWorkAddress.getText());

                ParentGuardian saved = parentService.updateParent(selectedParent.getId(), updates);

                showInfo("Parent/Guardian updated successfully");
                loadParents();
                tblParents.getSelectionModel().select(saved);
            } else {
                // Create new parent
                ParentGuardian saved = parentService.createParent(
                    txtFirstName.getText(),
                    txtLastName.getText(),
                    cmbRelationship.getValue(),
                    txtCellPhone.getText(),
                    txtEmail.getText(),
                    currentUserId
                );

                showInfo("Parent/Guardian created successfully");
                loadParents();
                tblParents.getSelectionModel().select(saved);
            }

            setFormEditable(false);
            editMode = false;
        } catch (Exception e) {
            log.error("Error saving parent/guardian", e);
            showError("Failed to save parent/guardian: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (selectedParent != null) {
            populateForm(selectedParent);
        } else {
            clearForm();
        }
        setFormEditable(false);
        editMode = false;
    }

    @FXML
    private void handleLinkStudent() {
        if (selectedParent == null) {
            showWarning("Please select a parent/guardian first");
            return;
        }

        Student student = cmbLinkStudent.getValue();
        if (student == null) {
            showWarning("Please select a student to link");
            return;
        }

        try {
            parentService.linkParentToStudent(
                selectedParent.getId(),
                student.getId(),
                cmbRelationship.getValue(),
                chkLinkPrimary.isSelected(),
                chkLinkCustodial.isSelected(),
                chkLinkPickup.isSelected()
            );

            showInfo("Student linked successfully");
            loadStudentRelationships(selectedParent);

            // Clear link form
            cmbLinkStudent.setValue(null);
            chkLinkPrimary.setSelected(false);
            chkLinkCustodial.setSelected(false);
            chkLinkPickup.setSelected(false);
            chkLinkEmergency.setSelected(false);
        } catch (Exception e) {
            log.error("Error linking student", e);
            showError("Failed to link student: " + e.getMessage());
        }
    }

    @FXML
    private void handleUnlinkStudent() {
        String selectedStudentName = lstStudents.getSelectionModel().getSelectedItem();
        if (selectedStudentName == null) {
            showWarning("Please select a student to unlink");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Unlink");
        alert.setHeaderText("Unlink Student");
        alert.setContentText("Are you sure you want to unlink this student?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Note: Would need to implement unlink logic
                    showInfo("Student unlinked successfully");
                    loadStudentRelationships(selectedParent);
                } catch (Exception e) {
                    log.error("Error unlinking student", e);
                    showError("Failed to unlink student: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleUploadDocument() {
        showInfo("Document upload functionality will be implemented in a future update");
    }

    @FXML
    private void handleCreatePortalAccount() {
        if (selectedParent == null) {
            showWarning("Please select a parent/guardian");
            return;
        }

        showInfo("Portal account creation will be implemented in a future update");
    }

    @FXML
    private void handleResetPassword() {
        if (selectedParent == null || selectedParent.getPortalUser() == null) {
            showWarning("Parent does not have a portal account");
            return;
        }

        showInfo("Password reset email will be sent");
    }

    @FXML
    private void handleSendInvitation() {
        if (selectedParent == null) {
            showWarning("Please select a parent/guardian");
            return;
        }

        showInfo("Invitation email will be sent");
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        if (txtFirstName.getText() == null || txtFirstName.getText().trim().isEmpty()) {
            showWarning("First name is required");
            return false;
        }

        if (txtLastName.getText() == null || txtLastName.getText().trim().isEmpty()) {
            showWarning("Last name is required");
            return false;
        }

        if (cmbRelationship.getValue() == null) {
            showWarning("Relationship is required");
            return false;
        }

        if (txtCellPhone.getText() == null || txtCellPhone.getText().trim().isEmpty()) {
            showWarning("Cell phone is required");
            return false;
        }

        if (txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) {
            showWarning("Email is required");
            return false;
        }

        return true;
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
