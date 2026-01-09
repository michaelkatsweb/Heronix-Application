package com.heronix.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentAccommodation;
import com.heronix.model.domain.StudentAccommodation.*;
import com.heronix.model.domain.User;
import com.heronix.service.StudentAccommodationService;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Student Accommodations Form
 *
 * Manages all student accommodations including:
 * - 504 Plans, IEPs, ELL/ESL
 * - Gifted, At-Risk, Title I
 * - Homeless, Foster Care, Military Families
 * - Lunch Programs, Transportation
 * - Accessibility, Assistive Technology
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@Slf4j
public class StudentAccommodationsFormController {

    @Autowired
    private StudentAccommodationService accommodationService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    private User currentUser;
    private StudentAccommodation selectedAccommodation;
    private boolean editMode = false;

    // ========================================================================
    // FXML COMPONENTS - Search & Filter
    // ========================================================================

    @FXML private TextField txtSearchStudent;
    @FXML private ComboBox<AccommodationType> cmbAccommodationType;
    @FXML private ComboBox<AccommodationStatus> cmbStatus;
    @FXML private ComboBox<User> cmbCoordinator;

    @FXML private ToggleButton toggle504Plans;
    @FXML private ToggleButton toggleIEPs;
    @FXML private ToggleButton toggleELL;
    @FXML private ToggleButton toggleGifted;
    @FXML private ToggleButton toggleAtRisk;
    @FXML private ToggleButton toggleOverdueReviews;
    @FXML private ToggleButton toggleExpiringSoon;

    // ========================================================================
    // FXML COMPONENTS - Table
    // ========================================================================

    @FXML private TableView<StudentAccommodation> tblAccommodations;
    @FXML private TableColumn<StudentAccommodation, String> colStudentName;
    @FXML private TableColumn<StudentAccommodation, String> colType;
    @FXML private TableColumn<StudentAccommodation, String> colStatus;
    @FXML private TableColumn<StudentAccommodation, String> colStartDate;
    @FXML private TableColumn<StudentAccommodation, String> colEndDate;
    @FXML private TableColumn<StudentAccommodation, String> colNextReview;
    @FXML private TableColumn<StudentAccommodation, String> colCoordinator;

    @FXML private Label lblRecordCount;

    // ========================================================================
    // FXML COMPONENTS - Action Buttons
    // ========================================================================

    @FXML private Button btnRefresh;
    @FXML private Button btnNewAccommodation;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnActivate;
    @FXML private Button btnDeactivate;
    @FXML private Button btnDelete;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - Basic
    // ========================================================================

    @FXML private VBox detailsPanel;
    @FXML private ComboBox<Student> cmbStudent;
    @FXML private ComboBox<AccommodationType> cmbType;
    @FXML private ComboBox<AccommodationStatus> cmbDetailStatus;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<User> cmbDetailCoordinator;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - 504 Plan
    // ========================================================================

    @FXML private VBox section504Plan;
    @FXML private CheckBox chkHas504Plan;
    @FXML private TextArea txt504Accommodations;
    @FXML private DatePicker dp504PlanDate;
    @FXML private DatePicker dp504ReviewDate;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - IEP
    // ========================================================================

    @FXML private VBox sectionIEP;
    @FXML private CheckBox chkHasIEP;
    @FXML private ComboBox<IEPPlacement> cmbIEPPlacement;
    @FXML private TextField txtPrimaryDisability;
    @FXML private DatePicker dpIEPStartDate;
    @FXML private DatePicker dpIEPReviewDate;
    @FXML private TextField txtCaseManager;
    @FXML private TextArea txtIEPAccommodations;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - ELL
    // ========================================================================

    @FXML private VBox sectionELL;
    @FXML private CheckBox chkIsELL;
    @FXML private ComboBox<ELLProficiencyLevel> cmbELLProficiency;
    @FXML private TextField txtHomeLanguage;
    @FXML private DatePicker dpELLEntryDate;
    @FXML private DatePicker dpELLExitDate;
    @FXML private TextArea txtELLServices;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - Other Designations
    // ========================================================================

    @FXML private CheckBox chkGifted;
    @FXML private CheckBox chkAtRisk;
    @FXML private CheckBox chkTitleI;
    @FXML private CheckBox chkHomeless;
    @FXML private CheckBox chkFosterCare;
    @FXML private CheckBox chkMilitaryFamily;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - Lunch & Transportation
    // ========================================================================

    @FXML private ComboBox<LunchStatus> cmbLunchStatus;
    @FXML private CheckBox chkSpecialTransportation;
    @FXML private TextField txtBusNumber;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - Accessibility
    // ========================================================================

    @FXML private CheckBox chkAssistiveTechnology;
    @FXML private TextArea txtAssistiveTechnology;
    @FXML private CheckBox chkAccessibilityAccommodations;
    @FXML private TextArea txtAccessibilityAccommodations;

    // ========================================================================
    // FXML COMPONENTS - Details Panel - Review & Notes
    // ========================================================================

    @FXML private DatePicker dpLastReviewDate;
    @FXML private DatePicker dpNextReviewDate;
    @FXML private TextArea txtCoordinatorNotes;
    @FXML private TextArea txtAdministrativeNotes;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnScheduleReview;

    // ========================================================================
    // FXML COMPONENTS - Status Bar
    // ========================================================================

    @FXML private Label lblStatusMessage;
    @FXML private Label lblUser;

    // ========================================================================
    // Data
    // ========================================================================

    private ObservableList<StudentAccommodation> accommodationsList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing StudentAccommodationsFormController");

        // Set current user (in production, get from session)
        this.currentUser = getCurrentUser();
        lblUser.setText("User: " + currentUser.getUsername());

        // Initialize table columns
        setupTableColumns();

        // Initialize combo boxes
        setupComboBoxes();

        // Setup table selection listener
        setupSelectionListener();

        // Load initial data
        loadAccommodations();

        // Set details panel to read-only initially
        setDetailsReadOnly(true);

        updateStatusMessage("Ready");
    }

    private void setupTableColumns() {
        colStudentName.setCellValueFactory(data -> {
            Student student = data.getValue().getStudent();
            String name = student != null ? student.getFirstName() + " " + student.getLastName() : "";
            return new SimpleStringProperty(name);
        });

        colType.setCellValueFactory(data -> {
            AccommodationType type = data.getValue().getType();
            return new SimpleStringProperty(type != null ? type.getDisplayName() : "");
        });

        colStatus.setCellValueFactory(data -> {
            AccommodationStatus status = data.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.getDisplayName() : "");
        });

        colStartDate.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getStartDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        colEndDate.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getEndDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        colNextReview.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getNextReviewDate();
            return new SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        colCoordinator.setCellValueFactory(data -> {
            User coordinator = data.getValue().getCoordinator();
            return new SimpleStringProperty(coordinator != null ? coordinator.getFullName() : "");
        });

        tblAccommodations.setItems(accommodationsList);
    }

    private void setupComboBoxes() {
        // Accommodation Type combo boxes
        cmbAccommodationType.setItems(FXCollections.observableArrayList(AccommodationType.values()));
        cmbType.setItems(FXCollections.observableArrayList(AccommodationType.values()));

        // Status combo boxes
        cmbStatus.setItems(FXCollections.observableArrayList(AccommodationStatus.values()));
        cmbDetailStatus.setItems(FXCollections.observableArrayList(AccommodationStatus.values()));

        // IEP Placement
        cmbIEPPlacement.setItems(FXCollections.observableArrayList(IEPPlacement.values()));

        // ELL Proficiency
        cmbELLProficiency.setItems(FXCollections.observableArrayList(ELLProficiencyLevel.values()));

        // Lunch Status
        cmbLunchStatus.setItems(FXCollections.observableArrayList(LunchStatus.values()));

        // Load coordinators (staff members)
        loadCoordinators();

        // Load students
        loadStudents();
    }

    private void setupSelectionListener() {
        tblAccommodations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedAccommodation = newVal;
                enableActionButtons(true);
                loadAccommodationDetails(newVal);
            } else {
                selectedAccommodation = null;
                enableActionButtons(false);
                clearDetailsPanel();
            }
        });
    }

    private void loadCoordinators() {
        try {
            List<User> staff = userService.getAllStaff();
            ObservableList<User> staffList = FXCollections.observableArrayList(staff);
            cmbCoordinator.setItems(staffList);
            cmbDetailCoordinator.setItems(staffList);
        } catch (Exception e) {
            log.error("Error loading coordinators", e);
            showError("Failed to load coordinators: " + e.getMessage());
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            ObservableList<Student> studentList = FXCollections.observableArrayList(students);
            cmbStudent.setItems(studentList);
        } catch (Exception e) {
            log.error("Error loading students", e);
            showError("Failed to load students: " + e.getMessage());
        }
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadAccommodations() {
        try {
            List<StudentAccommodation> accommodations = accommodationService.getAllAccommodations();
            accommodationsList.setAll(accommodations);
            lblRecordCount.setText(accommodations.size() + " records");
            updateStatusMessage("Loaded " + accommodations.size() + " accommodations");
        } catch (Exception e) {
            log.error("Error loading accommodations", e);
            showError("Failed to load accommodations: " + e.getMessage());
        }
    }

    private void loadAccommodationDetails(StudentAccommodation accommodation) {
        if (accommodation == null) return;

        try {
            // Basic information
            cmbStudent.setValue(accommodation.getStudent());
            cmbType.setValue(accommodation.getType());
            cmbDetailStatus.setValue(accommodation.getStatus());
            dpStartDate.setValue(accommodation.getStartDate());
            dpEndDate.setValue(accommodation.getEndDate());
            cmbDetailCoordinator.setValue(accommodation.getCoordinator());

            // 504 Plan
            chkHas504Plan.setSelected(accommodation.getHas504Plan() != null && accommodation.getHas504Plan());
            txt504Accommodations.setText(accommodation.getPlan504Accommodations());
            dp504PlanDate.setValue(accommodation.getPlan504Date());
            dp504ReviewDate.setValue(accommodation.getPlan504ReviewDate());

            // IEP
            chkHasIEP.setSelected(accommodation.getHasIEP() != null && accommodation.getHasIEP());
            cmbIEPPlacement.setValue(accommodation.getIepPlacement());
            txtPrimaryDisability.setText(accommodation.getPrimaryDisability());
            dpIEPStartDate.setValue(accommodation.getIepStartDate());
            dpIEPReviewDate.setValue(accommodation.getIepReviewDate());
            txtCaseManager.setText(accommodation.getCaseManagerName());
            txtIEPAccommodations.setText(accommodation.getIepAccommodations());

            // ELL
            chkIsELL.setSelected(accommodation.getIsELL() != null && accommodation.getIsELL());
            cmbELLProficiency.setValue(accommodation.getEllProficiencyLevel());
            txtHomeLanguage.setText(accommodation.getHomeLanguage());
            dpELLEntryDate.setValue(accommodation.getEllEntryDate());
            dpELLExitDate.setValue(accommodation.getEllExitDate());
            txtELLServices.setText(accommodation.getEllServices());

            // Other Designations
            chkGifted.setSelected(accommodation.getIsGifted() != null && accommodation.getIsGifted());
            chkAtRisk.setSelected(accommodation.getIsAtRisk() != null && accommodation.getIsAtRisk());
            chkTitleI.setSelected(accommodation.getTitleIParticipating() != null && accommodation.getTitleIParticipating());
            chkHomeless.setSelected(accommodation.getHomelessStatus() != null && accommodation.getHomelessStatus());
            chkFosterCare.setSelected(accommodation.getFosterCareStatus() != null && accommodation.getFosterCareStatus());
            chkMilitaryFamily.setSelected(accommodation.getMilitaryFamily() != null && accommodation.getMilitaryFamily());

            // Lunch & Transportation
            cmbLunchStatus.setValue(accommodation.getLunchStatus());
            chkSpecialTransportation.setSelected(accommodation.getRequiresSpecialTransportation() != null &&
                    accommodation.getRequiresSpecialTransportation());
            txtBusNumber.setText(accommodation.getBusNumber());

            // Accessibility
            chkAssistiveTechnology.setSelected(accommodation.getRequiresAssistiveTechnology() != null &&
                    accommodation.getRequiresAssistiveTechnology());
            txtAssistiveTechnology.setText(accommodation.getAssistiveTechnologyList());
            chkAccessibilityAccommodations.setSelected(accommodation.getRequiresAccessibilityAccommodations() != null &&
                    accommodation.getRequiresAccessibilityAccommodations());
            txtAccessibilityAccommodations.setText(accommodation.getAccessibilityAccommodationsList());

            // Review & Notes
            dpLastReviewDate.setValue(accommodation.getLastReviewDate());
            dpNextReviewDate.setValue(accommodation.getNextReviewDate());
            txtCoordinatorNotes.setText(accommodation.getCoordinatorNotes());
            txtAdministrativeNotes.setText(accommodation.getAdministrativeNotes());

            setDetailsReadOnly(true);
        } catch (Exception e) {
            log.error("Error loading accommodation details", e);
            showError("Failed to load accommodation details: " + e.getMessage());
        }
    }

    private void clearDetailsPanel() {
        // Basic
        cmbStudent.setValue(null);
        cmbType.setValue(null);
        cmbDetailStatus.setValue(null);
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        cmbDetailCoordinator.setValue(null);

        // 504 Plan
        chkHas504Plan.setSelected(false);
        txt504Accommodations.clear();
        dp504PlanDate.setValue(null);
        dp504ReviewDate.setValue(null);

        // IEP
        chkHasIEP.setSelected(false);
        cmbIEPPlacement.setValue(null);
        txtPrimaryDisability.clear();
        dpIEPStartDate.setValue(null);
        dpIEPReviewDate.setValue(null);
        txtCaseManager.clear();
        txtIEPAccommodations.clear();

        // ELL
        chkIsELL.setSelected(false);
        cmbELLProficiency.setValue(null);
        txtHomeLanguage.clear();
        dpELLEntryDate.setValue(null);
        dpELLExitDate.setValue(null);
        txtELLServices.clear();

        // Other
        chkGifted.setSelected(false);
        chkAtRisk.setSelected(false);
        chkTitleI.setSelected(false);
        chkHomeless.setSelected(false);
        chkFosterCare.setSelected(false);
        chkMilitaryFamily.setSelected(false);

        // Lunch & Transportation
        cmbLunchStatus.setValue(null);
        chkSpecialTransportation.setSelected(false);
        txtBusNumber.clear();

        // Accessibility
        chkAssistiveTechnology.setSelected(false);
        txtAssistiveTechnology.clear();
        chkAccessibilityAccommodations.setSelected(false);
        txtAccessibilityAccommodations.clear();

        // Review & Notes
        dpLastReviewDate.setValue(null);
        dpNextReviewDate.setValue(null);
        txtCoordinatorNotes.clear();
        txtAdministrativeNotes.clear();
    }

    // ========================================================================
    // EVENT HANDLERS - Search & Filter
    // ========================================================================

    @FXML
    private void handleRefresh() {
        log.info("Refreshing accommodation list");
        loadAccommodations();
    }

    @FXML
    private void handleSearch() {
        log.info("Searching accommodations");
        try {
            List<StudentAccommodation> results;

            // Apply filters
            String studentName = txtSearchStudent.getText();
            AccommodationType type = cmbAccommodationType.getValue();
            AccommodationStatus status = cmbStatus.getValue();
            User coordinator = cmbCoordinator.getValue();

            // Start with all accommodations
            results = accommodationService.getAllAccommodations();

            // Apply student name filter
            if (studentName != null && !studentName.trim().isEmpty()) {
                results = results.stream()
                        .filter(a -> {
                            Student s = a.getStudent();
                            String fullName = s.getFirstName() + " " + s.getLastName();
                            return fullName.toLowerCase().contains(studentName.toLowerCase());
                        })
                        .toList();
            }

            // Apply type filter
            if (type != null) {
                results = results.stream()
                        .filter(a -> a.getType() == type)
                        .toList();
            }

            // Apply status filter
            if (status != null) {
                results = results.stream()
                        .filter(a -> a.getStatus() == status)
                        .toList();
            }

            // Apply coordinator filter
            if (coordinator != null) {
                results = results.stream()
                        .filter(a -> a.getCoordinator() != null && a.getCoordinator().getId().equals(coordinator.getId()))
                        .toList();
            }

            accommodationsList.setAll(results);
            lblRecordCount.setText(results.size() + " records");
            updateStatusMessage("Found " + results.size() + " matching accommodations");

        } catch (Exception e) {
            log.error("Error searching accommodations", e);
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSearch() {
        txtSearchStudent.clear();
        cmbAccommodationType.setValue(null);
        cmbStatus.setValue(null);
        cmbCoordinator.setValue(null);

        // Clear all toggles
        toggle504Plans.setSelected(false);
        toggleIEPs.setSelected(false);
        toggleELL.setSelected(false);
        toggleGifted.setSelected(false);
        toggleAtRisk.setSelected(false);
        toggleOverdueReviews.setSelected(false);
        toggleExpiringSoon.setSelected(false);

        loadAccommodations();
    }

    @FXML
    private void handleQuickFilter() {
        try {
            List<StudentAccommodation> results = null;

            if (toggle504Plans.isSelected()) {
                results = accommodationService.getStudentsWith504Plans();
            } else if (toggleIEPs.isSelected()) {
                results = accommodationService.getStudentsWithIEPs();
            } else if (toggleELL.isSelected()) {
                results = accommodationService.getELLStudents();
            } else if (toggleGifted.isSelected()) {
                results = accommodationService.getGiftedStudents();
            } else if (toggleAtRisk.isSelected()) {
                results = accommodationService.getAtRiskStudents();
            } else if (toggleOverdueReviews.isSelected()) {
                results = accommodationService.getOverdueReviews();
            } else if (toggleExpiringSoon.isSelected()) {
                results = accommodationService.getExpiringSoon(30);
            } else {
                loadAccommodations();
                return;
            }

            accommodationsList.setAll(results);
            lblRecordCount.setText(results.size() + " records");
            updateStatusMessage("Applied quick filter: " + results.size() + " results");

        } catch (Exception e) {
            log.error("Error applying quick filter", e);
            showError("Filter failed: " + e.getMessage());
        }
    }

    // ========================================================================
    // EVENT HANDLERS - Actions
    // ========================================================================

    @FXML
    private void handleNewAccommodation() {
        log.info("Creating new accommodation");
        selectedAccommodation = null;
        editMode = true;
        clearDetailsPanel();
        setDetailsReadOnly(false);
        cmbDetailStatus.setValue(AccommodationStatus.DRAFT);
        dpStartDate.setValue(LocalDate.now());
        updateStatusMessage("Enter new accommodation details");
    }

    @FXML
    private void handleViewDetails() {
        if (selectedAccommodation != null) {
            log.info("Viewing accommodation details: {}", selectedAccommodation.getId());
            setDetailsReadOnly(true);
            loadAccommodationDetails(selectedAccommodation);
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedAccommodation != null) {
            log.info("Editing accommodation: {}", selectedAccommodation.getId());
            editMode = true;
            setDetailsReadOnly(false);
            updateStatusMessage("Editing accommodation - make changes and click Save");
        }
    }

    @FXML
    private void handleActivate() {
        if (selectedAccommodation != null) {
            try {
                StudentAccommodation activated = accommodationService.activateAccommodation(
                        selectedAccommodation.getId(), currentUser.getId());
                showInfo("Accommodation activated successfully");
                loadAccommodations();
                loadAccommodationDetails(activated);
            } catch (Exception e) {
                log.error("Error activating accommodation", e);
                showError("Failed to activate: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeactivate() {
        if (selectedAccommodation != null && confirmAction("Deactivate this accommodation?")) {
            try {
                StudentAccommodation deactivated = accommodationService.deactivateAccommodation(
                        selectedAccommodation.getId(), currentUser.getId());
                showInfo("Accommodation deactivated successfully");
                loadAccommodations();
                loadAccommodationDetails(deactivated);
            } catch (Exception e) {
                log.error("Error deactivating accommodation", e);
                showError("Failed to deactivate: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedAccommodation != null && confirmAction("Delete this accommodation? This cannot be undone.")) {
            try {
                accommodationService.deleteAccommodation(selectedAccommodation.getId());
                showInfo("Accommodation deleted successfully");
                loadAccommodations();
                clearDetailsPanel();
            } catch (Exception e) {
                log.error("Error deleting accommodation", e);
                showError("Failed to delete: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Validate required fields
            if (cmbStudent.getValue() == null) {
                showWarning("Please select a student");
                return;
            }
            if (cmbType.getValue() == null) {
                showWarning("Please select an accommodation type");
                return;
            }

            StudentAccommodation accommodation;

            if (selectedAccommodation == null) {
                // Create new
                accommodation = accommodationService.createAccommodation(
                        cmbStudent.getValue().getId(),
                        cmbType.getValue(),
                        currentUser.getId()
                );
            } else {
                // Update existing
                accommodation = selectedAccommodation;
            }

            // Update fields
            updateAccommodationFromForm(accommodation);

            // Save
            StudentAccommodation saved = accommodationService.updateAccommodation(accommodation, currentUser.getId());

            showInfo("Accommodation saved successfully");
            editMode = false;
            setDetailsReadOnly(true);
            loadAccommodations();

            // Reselect the saved accommodation
            tblAccommodations.getSelectionModel().select(saved);

        } catch (Exception e) {
            log.error("Error saving accommodation", e);
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (editMode) {
            editMode = false;
            if (selectedAccommodation != null) {
                loadAccommodationDetails(selectedAccommodation);
            } else {
                clearDetailsPanel();
            }
            setDetailsReadOnly(true);
            updateStatusMessage("Edit cancelled");
        }
    }

    @FXML
    private void handleScheduleReview() {
        if (selectedAccommodation != null) {
            TextInputDialog dialog = new TextInputDialog("30");
            dialog.setTitle("Schedule Review");
            dialog.setHeaderText("Schedule next review for this accommodation");
            dialog.setContentText("Days until review:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(days -> {
                try {
                    int daysInt = Integer.parseInt(days);
                    LocalDate reviewDate = LocalDate.now().plusDays(daysInt);
                    StudentAccommodation updated = accommodationService.scheduleReview(
                            selectedAccommodation.getId(), reviewDate, currentUser.getId());
                    showInfo("Review scheduled for " + reviewDate.format(dateFormatter));
                    loadAccommodations();
                    loadAccommodationDetails(updated);
                } catch (NumberFormatException e) {
                    showError("Please enter a valid number of days");
                } catch (Exception e) {
                    log.error("Error scheduling review", e);
                    showError("Failed to schedule review: " + e.getMessage());
                }
            });
        }
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void updateAccommodationFromForm(StudentAccommodation accommodation) {
        // Basic
        accommodation.setType(cmbType.getValue());
        accommodation.setStatus(cmbDetailStatus.getValue());
        accommodation.setStartDate(dpStartDate.getValue());
        accommodation.setEndDate(dpEndDate.getValue());
        accommodation.setCoordinator(cmbDetailCoordinator.getValue());

        // 504 Plan
        accommodation.setHas504Plan(chkHas504Plan.isSelected());
        accommodation.setPlan504Accommodations(txt504Accommodations.getText());
        accommodation.setPlan504Date(dp504PlanDate.getValue());
        accommodation.setPlan504ReviewDate(dp504ReviewDate.getValue());

        // IEP
        accommodation.setHasIEP(chkHasIEP.isSelected());
        accommodation.setIepPlacement(cmbIEPPlacement.getValue());
        accommodation.setPrimaryDisability(txtPrimaryDisability.getText());
        accommodation.setIepStartDate(dpIEPStartDate.getValue());
        accommodation.setIepReviewDate(dpIEPReviewDate.getValue());
        accommodation.setCaseManagerName(txtCaseManager.getText());
        accommodation.setIepAccommodations(txtIEPAccommodations.getText());

        // ELL
        accommodation.setIsELL(chkIsELL.isSelected());
        accommodation.setEllProficiencyLevel(cmbELLProficiency.getValue());
        accommodation.setHomeLanguage(txtHomeLanguage.getText());
        accommodation.setEllEntryDate(dpELLEntryDate.getValue());
        accommodation.setEllExitDate(dpELLExitDate.getValue());
        accommodation.setEllServices(txtELLServices.getText());

        // Other Designations
        accommodation.setIsGifted(chkGifted.isSelected());
        accommodation.setIsAtRisk(chkAtRisk.isSelected());
        accommodation.setTitleIParticipating(chkTitleI.isSelected());
        accommodation.setHomelessStatus(chkHomeless.isSelected());
        accommodation.setFosterCareStatus(chkFosterCare.isSelected());
        accommodation.setMilitaryFamily(chkMilitaryFamily.isSelected());

        // Lunch & Transportation
        accommodation.setLunchStatus(cmbLunchStatus.getValue());
        accommodation.setRequiresSpecialTransportation(chkSpecialTransportation.isSelected());
        accommodation.setBusNumber(txtBusNumber.getText());

        // Accessibility
        accommodation.setRequiresAssistiveTechnology(chkAssistiveTechnology.isSelected());
        accommodation.setAssistiveTechnologyList(txtAssistiveTechnology.getText());
        accommodation.setRequiresAccessibilityAccommodations(chkAccessibilityAccommodations.isSelected());
        accommodation.setAccessibilityAccommodationsList(txtAccessibilityAccommodations.getText());

        // Review & Notes
        accommodation.setLastReviewDate(dpLastReviewDate.getValue());
        accommodation.setNextReviewDate(dpNextReviewDate.getValue());
        accommodation.setCoordinatorNotes(txtCoordinatorNotes.getText());
        accommodation.setAdministrativeNotes(txtAdministrativeNotes.getText());
    }

    private void setDetailsReadOnly(boolean readOnly) {
        detailsPanel.setDisable(readOnly);
        btnSave.setVisible(!readOnly);
        btnCancel.setVisible(!readOnly);
    }

    private void enableActionButtons(boolean enable) {
        btnView.setDisable(!enable);
        btnEdit.setDisable(!enable);
        btnActivate.setDisable(!enable);
        btnDeactivate.setDisable(!enable);
        btnDelete.setDisable(!enable);
        btnScheduleReview.setDisable(!enable);
    }

    private void updateStatusMessage(String message) {
        lblStatusMessage.setText(message);
    }

    private User getCurrentUser() {
        // In production, get from session/security context
        // For now, return a default user
        try {
            return userService.getAllStaff().stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.warn("Could not load current user", e);
            return null;
        }
    }

    // ========================================================================
    // DIALOG HELPERS
    // ========================================================================

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
