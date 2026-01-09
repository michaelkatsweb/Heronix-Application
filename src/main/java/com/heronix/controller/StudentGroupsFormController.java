package com.heronix.controller;

import com.heronix.model.domain.Student;
import com.heronix.model.domain.StudentGroup;
import com.heronix.model.domain.StudentGroup.*;
import com.heronix.model.domain.User;
import com.heronix.service.StudentGroupService;
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
import java.util.stream.Collectors;

/**
 * Controller for Student Groups Form
 *
 * Manages all student groupings:
 * - Homerooms, Advisory Groups, Cohorts
 * - House Systems, Teams
 * - Learning Communities, Academic Tracks
 * - Extracurricular Clubs, Intervention Groups
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Controller
@Slf4j
public class StudentGroupsFormController {

    @Autowired
    private StudentGroupService groupService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserService userService;

    private User currentUser;
    private StudentGroup selectedGroup;
    private boolean editMode = false;

    // ========================================================================
    // FXML COMPONENTS - Search & Filter
    // ========================================================================

    @FXML private TextField txtSearchName;
    @FXML private ComboBox<GroupType> cmbGroupType;
    @FXML private ComboBox<GroupStatus> cmbGroupStatus;
    @FXML private ComboBox<String> cmbAcademicYear;

    @FXML private ToggleButton toggleHomerooms;
    @FXML private ToggleButton toggleAdvisories;
    @FXML private ToggleButton toggleCohorts;
    @FXML private ToggleButton toggleHouses;
    @FXML private ToggleButton toggleTeams;
    @FXML private ToggleButton toggleExtracurricular;
    @FXML private ToggleButton toggleWithCapacity;
    @FXML private ToggleButton toggleFull;

    // ========================================================================
    // FXML COMPONENTS - Table
    // ========================================================================

    @FXML private TableView<StudentGroup> tblGroups;
    @FXML private TableColumn<StudentGroup, String> colGroupName;
    @FXML private TableColumn<StudentGroup, String> colType;
    @FXML private TableColumn<StudentGroup, String> colStatus;
    @FXML private TableColumn<StudentGroup, String> colEnrollment;
    @FXML private TableColumn<StudentGroup, String> colCapacity;
    @FXML private TableColumn<StudentGroup, String> colAdvisor;

    @FXML private Label lblRecordCount;

    // ========================================================================
    // FXML COMPONENTS - Action Buttons
    // ========================================================================

    @FXML private Button btnRefresh;
    @FXML private Button btnNewGroup;
    @FXML private Button btnView;
    @FXML private Button btnEdit;
    @FXML private Button btnManageStudents;
    @FXML private Button btnClone;
    @FXML private Button btnDelete;

    // ========================================================================
    // FXML COMPONENTS - Details Panel
    // ========================================================================

    @FXML private VBox detailsPanel;
    @FXML private TextField txtGroupName;
    @FXML private TextField txtGroupCode;
    @FXML private ComboBox<GroupType> cmbType;
    @FXML private ComboBox<GroupStatus> cmbStatus;
    @FXML private TextField txtAcademicYear;
    @FXML private TextField txtGradeLevel;
    @FXML private TextArea txtDescription;

    @FXML private TextField txtMaxCapacity;
    @FXML private Label lblCurrentEnrollment;
    @FXML private Label lblAvailableSpots;
    @FXML private CheckBox chkAcceptingMembers;

    @FXML private ComboBox<User> cmbPrimaryAdvisor;
    @FXML private ComboBox<User> cmbSecondaryAdvisor;
    @FXML private ComboBox<User> cmbHomeroomTeacher;

    @FXML private TextField txtMeetingLocation;
    @FXML private TextField txtMeetingSchedule;
    @FXML private TextArea txtMeetingNotes;

    @FXML private VBox sectionHouse;
    @FXML private TextField txtHouseName;
    @FXML private TextField txtHouseColor;
    @FXML private TextField txtHouseMascot;
    @FXML private Label lblHousePoints;

    @FXML private VBox sectionTeam;
    @FXML private ComboBox<TeamType> cmbTeamType;
    @FXML private TextField txtSportActivity;
    @FXML private TextField txtCompetitionLevel;

    @FXML private TextArea txtAdministrativeNotes;
    @FXML private CheckBox chkVisibleToParents;
    @FXML private CheckBox chkVisibleToStudents;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    // ========================================================================
    // FXML COMPONENTS - Student Membership
    // ========================================================================

    @FXML private TextField txtSearchStudent;
    @FXML private ListView<Student> lstMembers;
    @FXML private Label lblMemberCount;

    // ========================================================================
    // FXML COMPONENTS - Status Bar
    // ========================================================================

    @FXML private Label lblStatusMessage;
    @FXML private Label lblUser;

    // ========================================================================
    // Data
    // ========================================================================

    private ObservableList<StudentGroup> groupsList = FXCollections.observableArrayList();
    private ObservableList<Student> membersList = FXCollections.observableArrayList();

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing StudentGroupsFormController");

        this.currentUser = getCurrentUser();
        lblUser.setText("User: " + currentUser.getUsername());

        setupTableColumns();
        setupComboBoxes();
        setupSelectionListener();
        setupTypeChangeListener();

        loadGroups();
        setDetailsReadOnly(true);

        updateStatusMessage("Ready");
    }

    private void setupTableColumns() {
        colGroupName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGroupName()));

        colType.setCellValueFactory(data -> {
            GroupType type = data.getValue().getGroupType();
            return new SimpleStringProperty(type != null ? type.getDisplayName() : "");
        });

        colStatus.setCellValueFactory(data -> {
            GroupStatus status = data.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.getDisplayName() : "");
        });

        colEnrollment.setCellValueFactory(data -> {
            Integer enrollment = data.getValue().getCurrentEnrollment();
            return new SimpleStringProperty(enrollment != null ? enrollment.toString() : "0");
        });

        colCapacity.setCellValueFactory(data -> {
            Integer capacity = data.getValue().getMaxCapacity();
            return new SimpleStringProperty(capacity != null ? capacity.toString() : "∞");
        });

        colAdvisor.setCellValueFactory(data -> {
            User advisor = data.getValue().getPrimaryAdvisor();
            return new SimpleStringProperty(advisor != null ? advisor.getFullName() : "");
        });

        tblGroups.setItems(groupsList);
    }

    private void setupComboBoxes() {
        cmbGroupType.setItems(FXCollections.observableArrayList(GroupType.values()));
        cmbType.setItems(FXCollections.observableArrayList(GroupType.values()));
        cmbGroupStatus.setItems(FXCollections.observableArrayList(GroupStatus.values()));
        cmbStatus.setItems(FXCollections.observableArrayList(GroupStatus.values()));
        cmbTeamType.setItems(FXCollections.observableArrayList(TeamType.values()));

        // Academic years
        ObservableList<String> years = FXCollections.observableArrayList(
                "2024-2025", "2025-2026", "2026-2027"
        );
        cmbAcademicYear.setItems(years);

        loadStaff();
    }

    private void setupSelectionListener() {
        tblGroups.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedGroup = newVal;
                enableActionButtons(true);
                loadGroupDetails(newVal);
                loadGroupMembers(newVal);
            } else {
                selectedGroup = null;
                enableActionButtons(false);
                clearDetailsPanel();
                membersList.clear();
            }
        });
    }

    private void setupTypeChangeListener() {
        cmbType.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateVisibilityForGroupType(newVal);
            }
        });
    }

    private void loadStaff() {
        try {
            List<User> staff = userService.getAllStaff();
            ObservableList<User> staffList = FXCollections.observableArrayList(staff);
            cmbPrimaryAdvisor.setItems(staffList);
            cmbSecondaryAdvisor.setItems(staffList);
            cmbHomeroomTeacher.setItems(staffList);
        } catch (Exception e) {
            log.error("Error loading staff", e);
            showError("Failed to load staff: " + e.getMessage());
        }
    }

    private void updateVisibilityForGroupType(GroupType type) {
        sectionHouse.setVisible(type == GroupType.HOUSE);
        sectionHouse.setManaged(type == GroupType.HOUSE);

        sectionTeam.setVisible(type == GroupType.TEAM);
        sectionTeam.setManaged(type == GroupType.TEAM);
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadGroups() {
        try {
            List<StudentGroup> groups = groupService.getAllGroups();
            groupsList.setAll(groups);
            lblRecordCount.setText(groups.size() + " groups");
            updateStatusMessage("Loaded " + groups.size() + " groups");
        } catch (Exception e) {
            log.error("Error loading groups", e);
            showError("Failed to load groups: " + e.getMessage());
        }
    }

    private void loadGroupDetails(StudentGroup group) {
        if (group == null) return;

        try {
            txtGroupName.setText(group.getGroupName());
            txtGroupCode.setText(group.getGroupCode());
            cmbType.setValue(group.getGroupType());
            cmbStatus.setValue(group.getStatus());
            txtAcademicYear.setText(group.getAcademicYear());
            txtGradeLevel.setText(group.getGradeLevel());
            txtDescription.setText(group.getDescription());

            txtMaxCapacity.setText(group.getMaxCapacity() != null ? group.getMaxCapacity().toString() : "");
            lblCurrentEnrollment.setText(group.getCurrentEnrollment() != null ? group.getCurrentEnrollment().toString() : "0");
            int available = group.getAvailableSpots();
            lblAvailableSpots.setText(available == Integer.MAX_VALUE ? "∞" : String.valueOf(available));
            chkAcceptingMembers.setSelected(group.getAcceptingNewMembers() != null && group.getAcceptingNewMembers());

            cmbPrimaryAdvisor.setValue(group.getPrimaryAdvisor());
            cmbSecondaryAdvisor.setValue(group.getSecondaryAdvisor());
            cmbHomeroomTeacher.setValue(group.getHomeroomTeacher());

            txtMeetingLocation.setText(group.getMeetingLocation());
            txtMeetingSchedule.setText(group.getMeetingSchedule());
            txtMeetingNotes.setText(group.getMeetingNotes());

            // House details
            if (group.getGroupType() == GroupType.HOUSE) {
                txtHouseName.setText(group.getHouseName());
                txtHouseColor.setText(group.getHouseColor());
                txtHouseMascot.setText(group.getHouseMascot());
                lblHousePoints.setText(group.getHousePoints() != null ? group.getHousePoints().toString() : "0");
            }

            // Team details
            if (group.getGroupType() == GroupType.TEAM) {
                cmbTeamType.setValue(group.getTeamType());
                txtSportActivity.setText(group.getSportOrActivity());
                txtCompetitionLevel.setText(group.getCompetitionLevel());
            }

            txtAdministrativeNotes.setText(group.getAdministrativeNotes());
            chkVisibleToParents.setSelected(group.getVisibleToParents() != null && group.getVisibleToParents());
            chkVisibleToStudents.setSelected(group.getVisibleToStudents() != null && group.getVisibleToStudents());

            updateVisibilityForGroupType(group.getGroupType());
            setDetailsReadOnly(true);

        } catch (Exception e) {
            log.error("Error loading group details", e);
            showError("Failed to load group details: " + e.getMessage());
        }
    }

    private void loadGroupMembers(StudentGroup group) {
        if (group == null) return;

        try {
            List<Long> studentIds = groupService.getStudentIdsInGroup(group.getId());
            List<Student> students = studentIds.stream()
                    .map(id -> studentService.getStudentById(id).orElse(null))
                    .filter(s -> s != null)
                    .collect(Collectors.toList());

            membersList.setAll(students);
            lstMembers.setItems(membersList);
            lblMemberCount.setText(students.size() + " students");

        } catch (Exception e) {
            log.error("Error loading group members", e);
            showError("Failed to load members: " + e.getMessage());
        }
    }

    private void clearDetailsPanel() {
        txtGroupName.clear();
        txtGroupCode.clear();
        cmbType.setValue(null);
        cmbStatus.setValue(null);
        txtAcademicYear.clear();
        txtGradeLevel.clear();
        txtDescription.clear();

        txtMaxCapacity.clear();
        lblCurrentEnrollment.setText("0");
        lblAvailableSpots.setText("0");
        chkAcceptingMembers.setSelected(false);

        cmbPrimaryAdvisor.setValue(null);
        cmbSecondaryAdvisor.setValue(null);
        cmbHomeroomTeacher.setValue(null);

        txtMeetingLocation.clear();
        txtMeetingSchedule.clear();
        txtMeetingNotes.clear();

        txtHouseName.clear();
        txtHouseColor.clear();
        txtHouseMascot.clear();
        lblHousePoints.setText("0");

        cmbTeamType.setValue(null);
        txtSportActivity.clear();
        txtCompetitionLevel.clear();

        txtAdministrativeNotes.clear();
        chkVisibleToParents.setSelected(false);
        chkVisibleToStudents.setSelected(false);
    }

    // ========================================================================
    // EVENT HANDLERS - Search & Filter
    // ========================================================================

    @FXML
    private void handleRefresh() {
        loadGroups();
    }

    @FXML
    private void handleSearch() {
        try {
            List<StudentGroup> results = groupService.getAllGroups();

            String searchName = txtSearchName.getText();
            if (searchName != null && !searchName.trim().isEmpty()) {
                results = results.stream()
                        .filter(g -> g.getGroupName().toLowerCase().contains(searchName.toLowerCase()) ||
                                (g.getGroupCode() != null && g.getGroupCode().toLowerCase().contains(searchName.toLowerCase())))
                        .collect(Collectors.toList());
            }

            GroupType type = cmbGroupType.getValue();
            if (type != null) {
                results = results.stream().filter(g -> g.getGroupType() == type).collect(Collectors.toList());
            }

            GroupStatus status = cmbGroupStatus.getValue();
            if (status != null) {
                results = results.stream().filter(g -> g.getStatus() == status).collect(Collectors.toList());
            }

            String academicYear = cmbAcademicYear.getValue();
            if (academicYear != null) {
                results = results.stream()
                        .filter(g -> academicYear.equals(g.getAcademicYear()))
                        .collect(Collectors.toList());
            }

            groupsList.setAll(results);
            lblRecordCount.setText(results.size() + " groups");
            updateStatusMessage("Found " + results.size() + " matching groups");

        } catch (Exception e) {
            log.error("Error searching groups", e);
            showError("Search failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearSearch() {
        txtSearchName.clear();
        cmbGroupType.setValue(null);
        cmbGroupStatus.setValue(null);
        cmbAcademicYear.setValue(null);

        toggleHomerooms.setSelected(false);
        toggleAdvisories.setSelected(false);
        toggleCohorts.setSelected(false);
        toggleHouses.setSelected(false);
        toggleTeams.setSelected(false);
        toggleExtracurricular.setSelected(false);
        toggleWithCapacity.setSelected(false);
        toggleFull.setSelected(false);

        loadGroups();
    }

    @FXML
    private void handleQuickFilter() {
        try {
            List<StudentGroup> results = null;

            if (toggleHomerooms.isSelected()) {
                results = groupService.getAllHomerooms();
            } else if (toggleAdvisories.isSelected()) {
                results = groupService.getAllAdvisories();
            } else if (toggleCohorts.isSelected()) {
                results = groupService.getAllCohorts();
            } else if (toggleHouses.isSelected()) {
                results = groupService.getAllHouses();
            } else if (toggleTeams.isSelected()) {
                results = groupService.getAllTeams();
            } else if (toggleExtracurricular.isSelected()) {
                results = groupService.getAllExtracurricularClubs();
            } else if (toggleWithCapacity.isSelected()) {
                results = groupService.getGroupsWithCapacity();
            } else if (toggleFull.isSelected()) {
                results = groupService.getFullGroups();
            } else {
                loadGroups();
                return;
            }

            groupsList.setAll(results);
            lblRecordCount.setText(results.size() + " groups");

        } catch (Exception e) {
            log.error("Error applying quick filter", e);
            showError("Filter failed: " + e.getMessage());
        }
    }

    // ========================================================================
    // EVENT HANDLERS - Actions
    // ========================================================================

    @FXML
    private void handleNewGroup() {
        selectedGroup = null;
        editMode = true;
        clearDetailsPanel();
        setDetailsReadOnly(false);
        cmbStatus.setValue(GroupStatus.ACTIVE);
        txtAcademicYear.setText(LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1));
        chkAcceptingMembers.setSelected(true);
        updateStatusMessage("Enter new group details");
    }

    @FXML
    private void handleViewDetails() {
        if (selectedGroup != null) {
            setDetailsReadOnly(true);
            loadGroupDetails(selectedGroup);
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedGroup != null) {
            editMode = true;
            setDetailsReadOnly(false);
            updateStatusMessage("Editing group - make changes and click Save");
        }
    }

    @FXML
    private void handleManageStudents() {
        if (selectedGroup != null) {
            showInfo("Use the Student Membership panel on the right to add/remove students");
        }
    }

    @FXML
    private void handleClone() {
        if (selectedGroup != null) {
            TextInputDialog dialog = new TextInputDialog(LocalDate.now().getYear() + "-" + (LocalDate.now().getYear() + 1));
            dialog.setTitle("Clone Group");
            dialog.setHeaderText("Clone group for new academic year");
            dialog.setContentText("Academic Year:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(year -> {
                try {
                    StudentGroup cloned = groupService.cloneGroupForNewYear(selectedGroup.getId(), year, currentUser.getId());
                    showInfo("Group cloned successfully for " + year);
                    loadGroups();
                } catch (Exception e) {
                    log.error("Error cloning group", e);
                    showError("Failed to clone: " + e.getMessage());
                }
            });
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedGroup != null && confirmAction("Delete this group? This cannot be undone.")) {
            try {
                groupService.deleteGroup(selectedGroup.getId());
                showInfo("Group deleted successfully");
                loadGroups();
                clearDetailsPanel();
            } catch (Exception e) {
                log.error("Error deleting group", e);
                showError("Failed to delete: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (txtGroupName.getText() == null || txtGroupName.getText().trim().isEmpty()) {
                showWarning("Please enter a group name");
                return;
            }
            if (cmbType.getValue() == null) {
                showWarning("Please select a group type");
                return;
            }

            StudentGroup group;

            if (selectedGroup == null) {
                group = StudentGroup.builder()
                        .createdBy(currentUser)
                        .build();
            } else {
                group = selectedGroup;
            }

            updateGroupFromForm(group);

            StudentGroup saved;
            if (selectedGroup == null) {
                saved = groupService.createGroup(group, currentUser.getId());
            } else {
                saved = groupService.updateGroup(group, currentUser.getId());
            }

            showInfo("Group saved successfully");
            editMode = false;
            setDetailsReadOnly(true);
            loadGroups();
            tblGroups.getSelectionModel().select(saved);

        } catch (Exception e) {
            log.error("Error saving group", e);
            showError("Failed to save: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (editMode) {
            editMode = false;
            if (selectedGroup != null) {
                loadGroupDetails(selectedGroup);
            } else {
                clearDetailsPanel();
            }
            setDetailsReadOnly(true);
            updateStatusMessage("Edit cancelled");
        }
    }

    // ========================================================================
    // EVENT HANDLERS - House Points
    // ========================================================================

    @FXML
    private void handleAddPoints() {
        if (selectedGroup != null && selectedGroup.getGroupType() == GroupType.HOUSE) {
            try {
                StudentGroup updated = groupService.addHousePoints(selectedGroup.getId(), 10, currentUser.getId());
                lblHousePoints.setText(updated.getHousePoints().toString());
                showInfo("Added 10 points");
                loadGroups();
            } catch (Exception e) {
                log.error("Error adding points", e);
                showError("Failed to add points: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSubtractPoints() {
        if (selectedGroup != null && selectedGroup.getGroupType() == GroupType.HOUSE) {
            try {
                StudentGroup updated = groupService.subtractHousePoints(selectedGroup.getId(), 10, currentUser.getId());
                lblHousePoints.setText(updated.getHousePoints().toString());
                showInfo("Subtracted 10 points");
                loadGroups();
            } catch (Exception e) {
                log.error("Error subtracting points", e);
                showError("Failed to subtract points: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetPoints() {
        if (selectedGroup != null && selectedGroup.getGroupType() == GroupType.HOUSE && confirmAction("Reset house points to 0?")) {
            try {
                StudentGroup updated = groupService.resetHousePoints(selectedGroup.getId(), currentUser.getId());
                lblHousePoints.setText("0");
                showInfo("Points reset");
                loadGroups();
            } catch (Exception e) {
                log.error("Error resetting points", e);
                showError("Failed to reset points: " + e.getMessage());
            }
        }
    }

    // ========================================================================
    // EVENT HANDLERS - Student Membership
    // ========================================================================

    @FXML
    private void handleAddStudent() {
        if (selectedGroup != null) {
            String searchText = txtSearchStudent.getText();
            if (searchText == null || searchText.trim().isEmpty()) {
                showWarning("Please enter a student name or ID");
                return;
            }

            try {
                List<Student> allStudents = studentService.getAllStudents();
                List<Student> matches = allStudents.stream()
                        .filter(s -> {
                            String fullName = s.getFirstName() + " " + s.getLastName();
                            return fullName.toLowerCase().contains(searchText.toLowerCase()) ||
                                    s.getStudentId().toLowerCase().contains(searchText.toLowerCase());
                        })
                        .collect(Collectors.toList());

                if (matches.isEmpty()) {
                    showWarning("No students found matching: " + searchText);
                } else if (matches.size() == 1) {
                    Student student = matches.get(0);
                    groupService.enrollStudent(selectedGroup.getId(), student.getId(), currentUser.getId());
                    showInfo("Student added successfully");
                    loadGroupMembers(selectedGroup);
                    loadGroups();
                    txtSearchStudent.clear();
                } else {
                    showInfo("Multiple matches found. Please be more specific.");
                }

            } catch (Exception e) {
                log.error("Error adding student", e);
                showError("Failed to add student: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRemoveStudent() {
        Student selected = lstMembers.getSelectionModel().getSelectedItem();
        if (selectedGroup != null && selected != null && confirmAction("Remove " + selected.getFirstName() + " " + selected.getLastName() + " from this group?")) {
            try {
                groupService.removeStudent(selectedGroup.getId(), selected.getId(), currentUser.getId());
                showInfo("Student removed successfully");
                loadGroupMembers(selectedGroup);
                loadGroups();
            } catch (Exception e) {
                log.error("Error removing student", e);
                showError("Failed to remove student: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBulkAdd() {
        showInfo("Bulk add feature - coming soon!");
    }

    @FXML
    private void handleExportList() {
        showInfo("Export feature - coming soon!");
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void updateGroupFromForm(StudentGroup group) {
        group.setGroupName(txtGroupName.getText());
        group.setGroupCode(txtGroupCode.getText());
        group.setGroupType(cmbType.getValue());
        group.setStatus(cmbStatus.getValue());
        group.setAcademicYear(txtAcademicYear.getText());
        group.setGradeLevel(txtGradeLevel.getText());
        group.setDescription(txtDescription.getText());

        try {
            String capacityText = txtMaxCapacity.getText();
            group.setMaxCapacity(capacityText != null && !capacityText.isEmpty() ? Integer.parseInt(capacityText) : null);
        } catch (NumberFormatException e) {
            log.warn("Invalid capacity value");
        }

        group.setAcceptingNewMembers(chkAcceptingMembers.isSelected());

        group.setPrimaryAdvisor(cmbPrimaryAdvisor.getValue());
        group.setSecondaryAdvisor(cmbSecondaryAdvisor.getValue());
        group.setHomeroomTeacher(cmbHomeroomTeacher.getValue());

        group.setMeetingLocation(txtMeetingLocation.getText());
        group.setMeetingSchedule(txtMeetingSchedule.getText());
        group.setMeetingNotes(txtMeetingNotes.getText());

        if (group.getGroupType() == GroupType.HOUSE) {
            group.setIsHouse(true);
            group.setHouseName(txtHouseName.getText());
            group.setHouseColor(txtHouseColor.getText());
            group.setHouseMascot(txtHouseMascot.getText());
        }

        if (group.getGroupType() == GroupType.TEAM) {
            group.setIsTeam(true);
            group.setTeamType(cmbTeamType.getValue());
            group.setSportOrActivity(txtSportActivity.getText());
            group.setCompetitionLevel(txtCompetitionLevel.getText());
        }

        group.setAdministrativeNotes(txtAdministrativeNotes.getText());
        group.setVisibleToParents(chkVisibleToParents.isSelected());
        group.setVisibleToStudents(chkVisibleToStudents.isSelected());
        group.setUpdatedBy(currentUser);
    }

    private void setDetailsReadOnly(boolean readOnly) {
        detailsPanel.setDisable(readOnly);
        btnSave.setVisible(!readOnly);
        btnCancel.setVisible(!readOnly);
    }

    private void enableActionButtons(boolean enable) {
        btnView.setDisable(!enable);
        btnEdit.setDisable(!enable);
        btnManageStudents.setDisable(!enable);
        btnClone.setDisable(!enable);
        btnDelete.setDisable(!enable);
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
