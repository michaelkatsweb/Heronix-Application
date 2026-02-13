package com.heronix.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.User;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Student Demographics Form
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StudentDemographicsFormController {

    private final StudentService studentService;
    private final UserService userService;

    // Table and columns
    @FXML private TableView<Student> tblStudents;
    @FXML private TableColumn<Student, String> colStudentId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGrade;
    @FXML private TableColumn<Student, String> colStatus;

    // Filter controls
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbGradeFilter;
    @FXML private ComboBox<String> cmbStatusFilter;

    // Action buttons
    @FXML private Button btnRefresh;
    @FXML private Button btnSearch;
    @FXML private Button btnClearFilters;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnUploadPhoto;
    @FXML private Button btnPrint;

    // Basic Information
    @FXML private TextField txtStudentId;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtMiddleName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtPreferredFirstName;
    @FXML private TextField txtNickname;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cmbGender;
    @FXML private TextField txtGenderIdentity;
    @FXML private TextField txtPronouns;

    // Contact Information
    @FXML private TextField txtCellPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPersonalEmail;

    // Residential Address
    @FXML private TextField txtStreetAddress;
    @FXML private TextField txtApartment;
    @FXML private TextField txtCity;
    @FXML private ComboBox<String> cmbState;
    @FXML private TextField txtZipCode;
    @FXML private ComboBox<String> cmbCountry;

    // Mailing Address
    @FXML private CheckBox chkSameAsResidential;
    @FXML private TextField txtMailingStreet;
    @FXML private TextField txtMailingCity;
    @FXML private ComboBox<String> cmbMailingState;
    @FXML private TextField txtMailingZip;

    // Ethnicity & Demographics
    @FXML private ComboBox<String> cmbEthnicity;
    @FXML private ComboBox<String> cmbHispanicLatino;
    @FXML private ComboBox<String> cmbNativeLanguage;
    @FXML private ComboBox<String> cmbHomeLanguage;
    @FXML private TextField txtOtherLanguages;

    // Citizenship & Immigration
    @FXML private ComboBox<String> cmbCitizenshipStatus;
    @FXML private ComboBox<String> cmbCountryOfBirth;
    @FXML private ComboBox<String> cmbCountryOfCitizenship;
    @FXML private ComboBox<String> cmbVisaType;
    @FXML private DatePicker dpVisaExpiration;

    // Photo & Identification
    @FXML private Label lblPhotoPath;
    @FXML private TextField txtStateId;
    @FXML private TextField txtSsnLast4;

    // Family Information
    @FXML private Spinner<Integer> spnNumSiblings;
    @FXML private Spinner<Integer> spnBirthOrder;
    @FXML private Spinner<Integer> spnHouseholdSize;
    @FXML private ComboBox<String> cmbLivesWith;

    // Additional Notes
    @FXML private TextArea txtNotes;

    // Status bar
    @FXML private Label lblCurrentUser;
    @FXML private Label lblTotalStudents;
    @FXML private Label lblSelectedStudent;

    private ObservableList<Student> studentsList = FXCollections.observableArrayList();
    private ObservableList<Student> filteredList = FXCollections.observableArrayList();
    private Student selectedStudent;
    private boolean editMode = false;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupComboBoxes();
        setupSpinners();
        setupListeners();
        loadStudents();
        updateStatusBar();
        setFormEditable(false);

        // Set current user
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            lblCurrentUser.setText(currentUser.getUsername());
        }
    }

    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        colStudentId.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStudentId()));

        colName.setCellValueFactory(cellData -> {
            Student student = cellData.getValue();
            return new SimpleStringProperty(student.getLastName() + ", " + student.getFirstName());
        });

        colGrade.setCellValueFactory(cellData -> {
            String grade = cellData.getValue().getGradeLevel();
            return new SimpleStringProperty(grade != null ? grade : "");
        });

        colStatus.setCellValueFactory(cellData -> {
            Student.StudentStatus status = cellData.getValue().getStudentStatus();
            return new SimpleStringProperty(status != null ? status.toString() : "");
        });
    }

    /**
     * Setup combo boxes
     */
    private void setupComboBoxes() {
        // Grade levels
        cmbGradeFilter.setItems(FXCollections.observableArrayList(
            "All Grades", "K", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
        ));

        // Student status
        cmbStatusFilter.setItems(FXCollections.observableArrayList(
            "All Statuses", "ACTIVE", "INACTIVE", "GRADUATED", "WITHDRAWN", "TRANSFERRED"
        ));

        // Gender
        cmbGender.setItems(FXCollections.observableArrayList(
            "Male", "Female"
        ));

        // US States
        ObservableList<String> states = FXCollections.observableArrayList(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        );
        cmbState.setItems(states);
        cmbMailingState.setItems(states);

        // Countries
        ObservableList<String> countries = FXCollections.observableArrayList(
            "United States", "Canada", "Mexico", "United Kingdom", "China", "India",
            "Japan", "South Korea", "Philippines", "Vietnam", "Other"
        );
        cmbCountry.setItems(countries);
        cmbCountryOfBirth.setItems(countries);
        cmbCountryOfCitizenship.setItems(countries);

        // Ethnicity
        cmbEthnicity.setItems(FXCollections.observableArrayList(
            "American Indian or Alaska Native",
            "Asian",
            "Black or African American",
            "Native Hawaiian or Other Pacific Islander",
            "White",
            "Two or More Races",
            "Prefer not to say"
        ));

        // Hispanic/Latino
        cmbHispanicLatino.setItems(FXCollections.observableArrayList(
            "Yes", "No", "Prefer not to say"
        ));

        // Languages
        ObservableList<String> languages = FXCollections.observableArrayList(
            "English", "Spanish", "Chinese (Mandarin)", "Chinese (Cantonese)",
            "French", "German", "Greek", "Italian", "Polish", "Japanese", "Thai",
            "Vietnamese", "Korean", "Tagalog", "Russian", "Portuguese", "Other"
        );
        cmbNativeLanguage.setItems(languages);
        cmbHomeLanguage.setItems(languages);

        // Citizenship Status
        cmbCitizenshipStatus.setItems(FXCollections.observableArrayList(
            "U.S. Citizen",
            "U.S. Permanent Resident",
            "Visa Holder",
            "Refugee",
            "Asylum Seeker",
            "Other"
        ));

        // Visa Types
        cmbVisaType.setItems(FXCollections.observableArrayList(
            "N/A",
            "F-1 (Student)",
            "F-2 (Dependent)",
            "J-1 (Exchange Visitor)",
            "J-2 (Dependent)",
            "H-1B (Dependent)",
            "Other"
        ));

        // Lives With
        cmbLivesWith.setItems(FXCollections.observableArrayList(
            "Both Parents",
            "Mother Only",
            "Father Only",
            "Mother and Step-Father",
            "Father and Step-Mother",
            "Grandparents",
            "Other Relatives",
            "Foster Family",
            "Group Home",
            "Other"
        ));
    }

    /**
     * Setup spinners
     */
    private void setupSpinners() {
        spnNumSiblings.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 0));
        spnBirthOrder.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));
        spnHouseholdSize.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1));
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
                    populateForm(newValue);
                    btnView.setDisable(false);
                    btnEdit.setDisable(false);
                    lblSelectedStudent.setText(newValue.getLastName() + ", " + newValue.getFirstName());
                } else {
                    btnView.setDisable(true);
                    btnEdit.setDisable(true);
                    lblSelectedStudent.setText("None");
                }
            }
        );

        // Same as residential checkbox
        chkSameAsResidential.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                copyResidentialToMailing();
            }
        });
    }

    /**
     * Load students
     */
    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentsList.setAll(students);
            filteredList.setAll(students);
            tblStudents.setItems(filteredList);
            updateStatusBar();
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Failed to load students: " + e.getMessage());
        }
    }

    /**
     * Populate form with student data
     */
    private void populateForm(Student student) {
        if (student == null) {
            clearForm();
            return;
        }

        // Basic Information
        txtStudentId.setText(student.getStudentId());
        txtFirstName.setText(student.getFirstName());
        txtMiddleName.setText(student.getMiddleName());
        txtLastName.setText(student.getLastName());
        txtPreferredFirstName.setText(student.getPreferredFirstName());
        txtNickname.setText(student.getNickname());
        dpDateOfBirth.setValue(student.getDateOfBirth());
        cmbGender.setValue(student.getGender() != null ? student.getGender().toString() : null);
        txtGenderIdentity.setText(student.getGenderIdentity());
        txtPronouns.setText(student.getPronouns());

        // Contact Information
        txtCellPhone.setText(student.getCellPhone());
        txtEmail.setText(student.getEmail());
        txtPersonalEmail.setText(student.getPersonalEmail());

        // Residential Address
        txtStreetAddress.setText(student.getStreetAddress());
        txtApartment.setText(student.getApartmentUnit());
        txtCity.setText(student.getCity());
        cmbState.setValue(student.getState());
        txtZipCode.setText(student.getZipCode());
        cmbCountry.setValue(student.getCountry() != null ? student.getCountry() : "United States");

        // Mailing Address
        txtMailingStreet.setText(student.getMailingStreet());
        txtMailingCity.setText(student.getMailingCity());
        cmbMailingState.setValue(student.getMailingState());
        txtMailingZip.setText(student.getMailingZipCode());

        // Ethnicity & Demographics
        cmbEthnicity.setValue(student.getEthnicity() != null ? student.getEthnicity().toString() : null);
        cmbHispanicLatino.setValue(student.getIsHispanicLatino() != null ?
            (student.getIsHispanicLatino() ? "Yes" : "No") : null);
        cmbNativeLanguage.setValue(student.getPrimaryLanguage());
        cmbHomeLanguage.setValue(student.getHomeLanguage());

        // Citizenship & Immigration
        cmbCitizenshipStatus.setValue(student.getCitizenshipStatus() != null ?
            student.getCitizenshipStatus().toString() : null);
        cmbCountryOfBirth.setValue(student.getCountryOfBirth());
        cmbCountryOfCitizenship.setValue(student.getCountryOfCitizenship());

        // Photo & Identification
        if (student.getPhotoPath() != null) {
            lblPhotoPath.setText(student.getPhotoPath());
        } else {
            lblPhotoPath.setText("No photo uploaded");
        }
        txtStateId.setText(student.getStateStudentId());
    }

    /**
     * Clear the form
     */
    private void clearForm() {
        txtStudentId.clear();
        txtFirstName.clear();
        txtMiddleName.clear();
        txtLastName.clear();
        txtPreferredFirstName.clear();
        txtNickname.clear();
        dpDateOfBirth.setValue(null);
        cmbGender.setValue(null);
        txtGenderIdentity.clear();
        txtPronouns.clear();

        txtCellPhone.clear();
        txtEmail.clear();
        txtPersonalEmail.clear();

        txtStreetAddress.clear();
        txtApartment.clear();
        txtCity.clear();
        cmbState.setValue(null);
        txtZipCode.clear();
        cmbCountry.setValue("United States");

        chkSameAsResidential.setSelected(false);
        txtMailingStreet.clear();
        txtMailingCity.clear();
        cmbMailingState.setValue(null);
        txtMailingZip.clear();

        cmbEthnicity.setValue(null);
        cmbHispanicLatino.setValue(null);
        cmbNativeLanguage.setValue(null);
        cmbHomeLanguage.setValue(null);
        txtOtherLanguages.clear();

        cmbCitizenshipStatus.setValue(null);
        cmbCountryOfBirth.setValue(null);
        cmbCountryOfCitizenship.setValue(null);
        cmbVisaType.setValue(null);
        dpVisaExpiration.setValue(null);

        lblPhotoPath.setText("No photo uploaded");
        txtStateId.clear();
        txtSsnLast4.clear();

        spnNumSiblings.getValueFactory().setValue(0);
        spnBirthOrder.getValueFactory().setValue(1);
        spnHouseholdSize.getValueFactory().setValue(1);
        cmbLivesWith.setValue(null);

        txtNotes.clear();
    }

    /**
     * Copy residential address to mailing address
     */
    private void copyResidentialToMailing() {
        txtMailingStreet.setText(txtStreetAddress.getText());
        txtMailingCity.setText(txtCity.getText());
        cmbMailingState.setValue(cmbState.getValue());
        txtMailingZip.setText(txtZipCode.getText());
    }

    /**
     * Set form editable state
     */
    private void setFormEditable(boolean editable) {
        txtFirstName.setEditable(editable);
        txtMiddleName.setEditable(editable);
        txtLastName.setEditable(editable);
        txtPreferredFirstName.setEditable(editable);
        txtNickname.setEditable(editable);
        dpDateOfBirth.setDisable(!editable);
        cmbGender.setDisable(!editable);
        txtGenderIdentity.setEditable(editable);
        txtPronouns.setEditable(editable);

        txtCellPhone.setEditable(editable);
        txtEmail.setEditable(editable);
        txtPersonalEmail.setEditable(editable);

        txtStreetAddress.setEditable(editable);
        txtApartment.setEditable(editable);
        txtCity.setEditable(editable);
        cmbState.setDisable(!editable);
        txtZipCode.setEditable(editable);
        cmbCountry.setDisable(!editable);

        chkSameAsResidential.setDisable(!editable);
        txtMailingStreet.setEditable(editable);
        txtMailingCity.setEditable(editable);
        cmbMailingState.setDisable(!editable);
        txtMailingZip.setEditable(editable);

        cmbEthnicity.setDisable(!editable);
        cmbHispanicLatino.setDisable(!editable);
        cmbNativeLanguage.setDisable(!editable);
        cmbHomeLanguage.setDisable(!editable);
        txtOtherLanguages.setEditable(editable);

        cmbCitizenshipStatus.setDisable(!editable);
        cmbCountryOfBirth.setDisable(!editable);
        cmbCountryOfCitizenship.setDisable(!editable);
        cmbVisaType.setDisable(!editable);
        dpVisaExpiration.setDisable(!editable);

        txtStateId.setEditable(editable);
        txtSsnLast4.setEditable(editable);

        spnNumSiblings.setDisable(!editable);
        spnBirthOrder.setDisable(!editable);
        spnHouseholdSize.setDisable(!editable);
        cmbLivesWith.setDisable(!editable);

        txtNotes.setEditable(editable);

        btnSave.setDisable(!editable);
        btnCancel.setDisable(!editable);
    }

    /**
     * Update status bar
     */
    private void updateStatusBar() {
        lblTotalStudents.setText(String.valueOf(studentsList.size()));
    }

    // Event Handlers

    @FXML
    private void handleRefresh() {
        loadStudents();
        showInfo("Student list refreshed");
    }

    @FXML
    private void handleSearch() {
        String searchTerm = txtSearch.getText().toLowerCase();

        List<Student> filtered = studentsList.stream()
            .filter(student -> {
                // Search filter
                if (!searchTerm.isEmpty()) {
                    String fullName = (student.getLastName() + " " + student.getFirstName()).toLowerCase();
                    String studentId = student.getStudentId() != null ? student.getStudentId().toLowerCase() : "";
                    if (!fullName.contains(searchTerm) && !studentId.contains(searchTerm)) {
                        return false;
                    }
                }

                // Grade filter
                if (cmbGradeFilter.getValue() != null && !"All Grades".equals(cmbGradeFilter.getValue())) {
                    String grade = String.valueOf(student.getGradeLevel());
                    if (!cmbGradeFilter.getValue().equals(grade)) {
                        return false;
                    }
                }

                // Status filter
                if (cmbStatusFilter.getValue() != null && !"All Statuses".equals(cmbStatusFilter.getValue())) {
                    if (student.getStudentStatus() == null ||
                        !student.getStudentStatus().toString().equals(cmbStatusFilter.getValue())) {
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
        cmbGradeFilter.setValue(null);
        cmbStatusFilter.setValue(null);
        filteredList.setAll(studentsList);
    }

    @FXML
    private void handleView() {
        if (selectedStudent != null) {
            populateForm(selectedStudent);
            setFormEditable(false);
            editMode = false;
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedStudent != null) {
            populateForm(selectedStudent);
            setFormEditable(true);
            editMode = true;
        }
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            if (selectedStudent != null) {
                // Update demographic fields
                selectedStudent.setFirstName(txtFirstName.getText());
                selectedStudent.setMiddleName(txtMiddleName.getText());
                selectedStudent.setLastName(txtLastName.getText());
                selectedStudent.setPreferredFirstName(txtPreferredFirstName.getText());
                selectedStudent.setNickname(txtNickname.getText());
                selectedStudent.setDateOfBirth(dpDateOfBirth.getValue());
                selectedStudent.setGenderIdentity(txtGenderIdentity.getText());
                selectedStudent.setPronouns(txtPronouns.getText());

                selectedStudent.setCellPhone(txtCellPhone.getText());
                selectedStudent.setEmail(txtEmail.getText());
                selectedStudent.setPersonalEmail(txtPersonalEmail.getText());

                selectedStudent.setStreetAddress(txtStreetAddress.getText());
                selectedStudent.setApartmentUnit(txtApartment.getText());
                selectedStudent.setCity(txtCity.getText());
                selectedStudent.setState(cmbState.getValue());
                selectedStudent.setZipCode(txtZipCode.getText());
                selectedStudent.setCountry(cmbCountry.getValue());

                selectedStudent.setMailingStreet(txtMailingStreet.getText());
                selectedStudent.setMailingCity(txtMailingCity.getText());
                selectedStudent.setMailingState(cmbMailingState.getValue());
                selectedStudent.setMailingZipCode(txtMailingZip.getText());

                selectedStudent.setPrimaryLanguage(cmbNativeLanguage.getValue());
                selectedStudent.setHomeLanguage(cmbHomeLanguage.getValue());

                selectedStudent.setCountryOfBirth(cmbCountryOfBirth.getValue());
                selectedStudent.setCountryOfCitizenship(cmbCountryOfCitizenship.getValue());

                selectedStudent.setStateStudentId(txtStateId.getText());

                Student saved = studentService.updateStudent(selectedStudent);

                showInfo("Student demographics updated successfully");
                loadStudents();
                setFormEditable(false);
                editMode = false;

                tblStudents.getSelectionModel().select(saved);
            }
        } catch (Exception e) {
            log.error("Error saving student demographics", e);
            showError("Failed to save student demographics: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (selectedStudent != null) {
            populateForm(selectedStudent);
        } else {
            clearForm();
        }
        setFormEditable(false);
        editMode = false;
    }

    @FXML
    private void handleSameAsResidential() {
        if (chkSameAsResidential.isSelected()) {
            copyResidentialToMailing();
        }
    }

    @FXML
    private void handleUploadPhoto() {
        showInfo("Photo upload functionality will be implemented in a future update");
    }

    @FXML
    private void handlePrint() {
        if (selectedStudent == null) {
            showWarning("Please select a student");
            return;
        }
        showInfo("Print functionality will be implemented in a future update");
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

        if (dpDateOfBirth.getValue() == null) {
            showWarning("Date of birth is required");
            return false;
        }

        if (cmbGender.getValue() == null) {
            showWarning("Gender is required");
            return false;
        }

        if (txtStreetAddress.getText() == null || txtStreetAddress.getText().trim().isEmpty()) {
            showWarning("Street address is required");
            return false;
        }

        if (txtCity.getText() == null || txtCity.getText().trim().isEmpty()) {
            showWarning("City is required");
            return false;
        }

        if (cmbState.getValue() == null) {
            showWarning("State is required");
            return false;
        }

        if (txtZipCode.getText() == null || txtZipCode.getText().trim().isEmpty()) {
            showWarning("ZIP code is required");
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
