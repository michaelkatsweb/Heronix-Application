package com.heronix.controller;

import com.heronix.model.domain.EmergencyContact;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
import com.heronix.service.EmergencyContactService;
import com.heronix.service.StudentService;
import com.heronix.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Emergency Contacts Form
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@Slf4j
public class EmergencyContactsFormController {

    @Autowired
    private EmergencyContactService contactService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    private User currentUser;
    private EmergencyContact selectedContact;
    private boolean editMode = false;

    // Search & Filter
    @FXML private ComboBox<Student> cmbStudent;
    @FXML private TextField txtSearchRelationship;
    @FXML private ToggleButton toggleUnverified;
    @FXML private ToggleButton toggleMissingPhone;
    @FXML private ToggleButton toggleIncomplete;

    // Table
    @FXML private TableView<EmergencyContact> tblContacts;
    @FXML private TableColumn<EmergencyContact, String> colStudent;
    @FXML private TableColumn<EmergencyContact, String> colName;
    @FXML private TableColumn<EmergencyContact, String> colRelationship;
    @FXML private TableColumn<EmergencyContact, String> colPriority;
    @FXML private TableColumn<EmergencyContact, String> colPhone;
    @FXML private TableColumn<EmergencyContact, String> colAuthorized;
    @FXML private TableColumn<EmergencyContact, String> colVerified;

    @FXML private Label lblRecordCount;

    // Action Buttons
    @FXML private Button btnRefresh;
    @FXML private Button btnNewContact;
    @FXML private Button btnEdit;
    @FXML private Button btnMovePriorityUp;
    @FXML private Button btnMovePriorityDown;
    @FXML private Button btnVerify;
    @FXML private Button btnDelete;

    // Details Panel
    @FXML private VBox detailsPanel;
    @FXML private ComboBox<Student> cmbDetailStudent;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtRelationship;
    @FXML private Spinner<Integer> spnPriority;

    @FXML private TextField txtPrimaryPhone;
    @FXML private TextField txtSecondaryPhone;
    @FXML private TextField txtWorkPhone;
    @FXML private TextField txtEmail;

    @FXML private TextField txtAddress;
    @FXML private TextField txtCity;
    @FXML private TextField txtState;
    @FXML private TextField txtZipCode;
    @FXML private CheckBox chkLivesWithStudent;

    @FXML private CheckBox chkAuthorizedPickup;
    @FXML private CheckBox chkAuthorizedMedical;
    @FXML private CheckBox chkAuthorizedFinancial;
    @FXML private TextArea txtAuthorizationNotes;

    @FXML private CheckBox chkVerified;
    @FXML private DatePicker dpVerificationDate;
    @FXML private CheckBox chkActive;

    @FXML private TextArea txtNotes;
    @FXML private TextArea txtEmergencyInstructions;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnCopyToSiblings;

    // Status Bar
    @FXML private Label lblStatusMessage;
    @FXML private Label lblUser;

    private ObservableList<EmergencyContact> contactsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Initializing EmergencyContactsFormController");

        this.currentUser = getCurrentUser();
        lblUser.setText("User: " + currentUser.getUsername());

        setupTableColumns();
        setupComboBoxes();
        setupSelectionListener();

        loadContacts();
        setDetailsReadOnly(true);

        updateStatusMessage("Ready");
    }

    private void setupTableColumns() {
        colStudent.setCellValueFactory(data -> {
            Student student = data.getValue().getStudent();
            String name = student != null ? student.getFirstName() + " " + student.getLastName() : "";
            return new SimpleStringProperty(name);
        });

        colName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFirstName() + " " + data.getValue().getLastName()));

        colRelationship.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRelationship()));

        colPriority.setCellValueFactory(data -> {
            Integer priority = data.getValue().getPriorityOrder();
            return new SimpleStringProperty(priority != null ? priority.toString() : "");
        });

        colPhone.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPrimaryPhone()));

        colAuthorized.setCellValueFactory(data -> {
            Boolean authorized = data.getValue().getAuthorizedToPickUp();
            return new SimpleStringProperty(Boolean.TRUE.equals(authorized) ? "Yes" : "No");
        });

        colVerified.setCellValueFactory(data -> {
            Boolean verified = data.getValue().getVerified();
            return new SimpleStringProperty(Boolean.TRUE.equals(verified) ? "✓" : "");
        });

        tblContacts.setItems(contactsList);
    }

    private void setupComboBoxes() {
        loadStudents();
    }

    private void setupSelectionListener() {
        tblContacts.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedContact = newVal;
                enableActionButtons(true);
                loadContactDetails(newVal);
            } else {
                selectedContact = null;
                enableActionButtons(false);
                clearDetailsPanel();
            }
        });
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            ObservableList<Student> studentList = FXCollections.observableArrayList(students);
            cmbStudent.setItems(studentList);
            cmbDetailStudent.setItems(studentList);
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Failed to load students: " + e.getMessage());
        }
    }

    private void loadContacts() {
        try {
            List<EmergencyContact> contacts = contactService.getAllContacts();
            contactsList.setAll(contacts);
            lblRecordCount.setText(contacts.size() + " contacts");
            updateStatusMessage("Loaded " + contacts.size() + " contacts");
        } catch (Exception e) {
            log.error("Error loading contacts", e);
            showError("Failed to load contacts: " + e.getMessage());
        }
    }

    private void loadContactDetails(EmergencyContact contact) {
        if (contact == null) return;

        try {
            cmbDetailStudent.setValue(contact.getStudent());
            txtFirstName.setText(contact.getFirstName());
            txtLastName.setText(contact.getLastName());
            txtRelationship.setText(contact.getRelationship());
            spnPriority.getValueFactory().setValue(contact.getPriorityOrder() != null ? contact.getPriorityOrder() : 1);

            txtPrimaryPhone.setText(contact.getPrimaryPhone());
            txtSecondaryPhone.setText(contact.getSecondaryPhone());
            txtWorkPhone.setText(contact.getWorkPhone());
            txtEmail.setText(contact.getEmail());

            txtAddress.setText(contact.getAddress());
            txtCity.setText(contact.getCity());
            txtState.setText(contact.getState());
            txtZipCode.setText(contact.getZipCode());
            chkLivesWithStudent.setSelected(contact.getLivesWithStudent() != null && contact.getLivesWithStudent());

            chkAuthorizedPickup.setSelected(contact.getAuthorizedToPickUp() != null && contact.getAuthorizedToPickUp());
            chkAuthorizedMedical.setSelected(contact.getAuthorizedForMedical() != null && contact.getAuthorizedForMedical());
            chkAuthorizedFinancial.setSelected(contact.getAuthorizedForFinancial() != null && contact.getAuthorizedForFinancial());
            txtAuthorizationNotes.setText(contact.getAuthorizationNotes());

            chkVerified.setSelected(contact.getVerified() != null && contact.getVerified());
            dpVerificationDate.setValue(contact.getVerificationDate());
            chkActive.setSelected(contact.getIsActive() != null && contact.getIsActive());

            txtNotes.setText(contact.getNotes());
            txtEmergencyInstructions.setText(contact.getEmergencyInstructions());

            setDetailsReadOnly(true);
        } catch (Exception e) {
            log.error("Error loading contact details", e);
            showError("Failed to load contact details: " + e.getMessage());
        }
    }

    private void clearDetailsPanel() {
        cmbDetailStudent.setValue(null);
        txtFirstName.clear();
        txtLastName.clear();
        txtRelationship.clear();
        spnPriority.getValueFactory().setValue(1);

        txtPrimaryPhone.clear();
        txtSecondaryPhone.clear();
        txtWorkPhone.clear();
        txtEmail.clear();

        txtAddress.clear();
        txtCity.clear();
        txtState.clear();
        txtZipCode.clear();
        chkLivesWithStudent.setSelected(false);

        chkAuthorizedPickup.setSelected(false);
        chkAuthorizedMedical.setSelected(false);
        chkAuthorizedFinancial.setSelected(false);
        txtAuthorizationNotes.clear();

        chkVerified.setSelected(false);
        dpVerificationDate.setValue(null);
        chkActive.setSelected(true);

        txtNotes.clear();
        txtEmergencyInstructions.clear();
    }

    @FXML
    private void handleRefresh() {
        loadContacts();
    }

    @FXML
    private void handleSearch() {
        try {
            Student student = cmbStudent.getValue();
            if (student != null) {
                List<EmergencyContact> results = contactService.getActiveContactsByStudent(student.getId());
                contactsList.setAll(results);
                lblRecordCount.setText(results.size() + " contacts");
            }
        } catch (Exception e) {
            log.error("Error searching contacts", e);
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSearch() {
        cmbStudent.setValue(null);
        txtSearchRelationship.clear();
        toggleUnverified.setSelected(false);
        toggleMissingPhone.setSelected(false);
        toggleIncomplete.setSelected(false);
        loadContacts();
    }

    @FXML
    private void handleQuickFilter() {
        try {
            List<EmergencyContact> results = null;

            if (toggleUnverified.isSelected()) {
                results = contactService.getUnverifiedContacts();
            } else if (toggleMissingPhone.isSelected()) {
                results = contactService.getContactsMissingPhone();
            } else if (toggleIncomplete.isSelected()) {
                results = contactService.getIncompleteContacts();
            } else {
                loadContacts();
                return;
            }

            contactsList.setAll(results);
            lblRecordCount.setText(results.size() + " contacts");
        } catch (Exception e) {
            log.error("Error applying filter", e);
            showError("Filter failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewContact() {
        selectedContact = null;
        editMode = true;
        clearDetailsPanel();
        setDetailsReadOnly(false);
        chkActive.setSelected(true);
        updateStatusMessage("Enter new contact details");
    }

    @FXML
    private void handleEdit() {
        if (selectedContact != null) {
            editMode = true;
            setDetailsReadOnly(false);
            updateStatusMessage("Editing contact - make changes and click Save");
        }
    }

    @FXML
    private void handleMovePriorityUp() {
        if (selectedContact != null) {
            try {
                EmergencyContact updated = contactService.movePriorityUp(selectedContact.getId());
                showInfo("Priority moved up");
                loadContacts();
                tblContacts.getSelectionModel().select(updated);
            } catch (Exception e) {
                log.error("Error moving priority", e);
                showError("Failed to move priority: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleMovePriorityDown() {
        if (selectedContact != null) {
            try {
                EmergencyContact updated = contactService.movePriorityDown(selectedContact.getId());
                showInfo("Priority moved down");
                loadContacts();
                tblContacts.getSelectionModel().select(updated);
            } catch (Exception e) {
                log.error("Error moving priority", e);
                showError("Failed to move priority: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleVerify() {
        if (selectedContact != null) {
            try {
                EmergencyContact verified = contactService.verifyContact(selectedContact.getId());
                showInfo("Contact verified");
                loadContacts();
                loadContactDetails(verified);
            } catch (Exception e) {
                log.error("Error verifying contact", e);
                showError("Failed to verify: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedContact != null && confirmAction("Delete this contact? This cannot be undone.")) {
            try {
                contactService.deleteContact(selectedContact.getId());
                showInfo("Contact deleted");
                loadContacts();
                clearDetailsPanel();
            } catch (Exception e) {
                log.error("Error deleting contact", e);
                showError("Failed to delete: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (cmbDetailStudent.getValue() == null) {
                showWarning("Please select a student");
                return;
            }
            if (txtFirstName.getText() == null || txtFirstName.getText().trim().isEmpty()) {
                showWarning("Please enter first name");
                return;
            }
            if (txtLastName.getText() == null || txtLastName.getText().trim().isEmpty()) {
                showWarning("Please enter last name");
                return;
            }

            EmergencyContact contact;
            if (selectedContact == null) {
                contact = new EmergencyContact();
                contact.setStudent(cmbDetailStudent.getValue());
                contact.setIsActive(true);
            } else {
                contact = selectedContact;
            }

            updateContactFromForm(contact);

            EmergencyContact saved = contactService.updateContact(contact);
            showInfo("Contact saved successfully");
            editMode = false;
            setDetailsReadOnly(true);
            loadContacts();
            tblContacts.getSelectionModel().select(saved);

        } catch (Exception e) {
            log.error("Error saving contact", e);
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (editMode) {
            editMode = false;
            if (selectedContact != null) {
                loadContactDetails(selectedContact);
            } else {
                clearDetailsPanel();
            }
            setDetailsReadOnly(true);
            updateStatusMessage("Edit cancelled");
        }
    }

    @FXML
    private void handleCopyToSiblings() {
        if (selectedContact != null) {
            try {
                List<EmergencyContact> copied = contactService.copyContactToSiblings(selectedContact.getId());
                showInfo("Contact copied to " + copied.size() + " sibling(s)");
                loadContacts();
            } catch (Exception e) {
                log.error("Error copying to siblings", e);
                showError("Failed to copy: " + e.getMessage());
            }
        }
    }

    private void updateContactFromForm(EmergencyContact contact) {
        contact.setFirstName(txtFirstName.getText());
        contact.setLastName(txtLastName.getText());
        contact.setRelationship(txtRelationship.getText());
        contact.setPriorityOrder(spnPriority.getValue());

        contact.setPrimaryPhone(txtPrimaryPhone.getText());
        contact.setSecondaryPhone(txtSecondaryPhone.getText());
        contact.setWorkPhone(txtWorkPhone.getText());
        contact.setEmail(txtEmail.getText());

        contact.setAddress(txtAddress.getText());
        contact.setCity(txtCity.getText());
        contact.setState(txtState.getText());
        contact.setZipCode(txtZipCode.getText());
        contact.setLivesWithStudent(chkLivesWithStudent.isSelected());

        contact.setAuthorizedToPickUp(chkAuthorizedPickup.isSelected());
        contact.setAuthorizedForMedical(chkAuthorizedMedical.isSelected());
        contact.setAuthorizedForFinancial(chkAuthorizedFinancial.isSelected());
        contact.setAuthorizationNotes(txtAuthorizationNotes.getText());

        contact.setVerified(chkVerified.isSelected());
        contact.setVerificationDate(dpVerificationDate.getValue());
        contact.setIsActive(chkActive.isSelected());

        contact.setNotes(txtNotes.getText());
        contact.setEmergencyInstructions(txtEmergencyInstructions.getText());
    }

    private void setDetailsReadOnly(boolean readOnly) {
        detailsPanel.setDisable(readOnly);
        btnSave.setVisible(!readOnly);
        btnCancel.setVisible(!readOnly);
    }

    private void enableActionButtons(boolean enable) {
        btnEdit.setDisable(!enable);
        btnMovePriorityUp.setDisable(!enable);
        btnMovePriorityDown.setDisable(!enable);
        btnVerify.setDisable(!enable);
        btnDelete.setDisable(!enable);
        btnCopyToSiblings.setDisable(!enable);
    }

    private void updateStatusMessage(String message) {
        lblStatusMessage.setText(message);
    }

    private User getCurrentUser() {
        try {
            return userService.getAllStaff().stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.warn("Could not load current user", e);
            return null;
        }
    }

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

    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Action");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
