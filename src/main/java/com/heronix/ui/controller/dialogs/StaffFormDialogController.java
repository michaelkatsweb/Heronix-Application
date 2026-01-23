package com.heronix.ui.controller.dialogs;

import com.heronix.model.domain.Campus;
import com.heronix.model.domain.Staff;
import com.heronix.model.enums.StaffOccupation;
import com.heronix.repository.CampusRepository;
import com.heronix.service.StaffService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Controller for Staff Form Dialog
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 59 - Staff/Teacher Separation
 */
@Slf4j
@Controller
public class StaffFormDialogController implements Initializable {

    @Autowired
    private StaffService staffService;

    @Autowired
    private CampusRepository campusRepository;

    // Dialog labels
    @FXML private Label dialogTitle;
    @FXML private Label dialogSubtitle;

    // Position Information
    @FXML private ComboBox<StaffOccupation> occupationCombo;
    @FXML private TextField jobTitleField;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private ComboBox<Campus> campusCombo;

    // Personal Information
    @FXML private ComboBox<String> titleCombo;
    @FXML private TextField firstNameField;
    @FXML private TextField middleNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField preferredNameField;
    @FXML private TextField employeeIdField;

    // Contact Information
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField mobileField;
    @FXML private TextField officeRoomField;

    // Employment Information
    @FXML private DatePicker hireDatePicker;
    @FXML private ComboBox<String> employmentTypeCombo;
    @FXML private TextField workHoursField;
    @FXML private Spinner<Integer> experienceSpinner;
    @FXML private CheckBox activeCheckbox;

    // Occupation-specific sections
    @FXML private VBox paraSection;
    @FXML private ComboBox<String> paraAssignmentCombo;
    @FXML private Spinner<Integer> maxStudentsSpinner;
    @FXML private CheckBox medicalTrainingCheck;
    @FXML private CheckBox behavioralTrainingCheck;

    @FXML private VBox driverSection;
    @FXML private TextField cdlNumberField;
    @FXML private DatePicker cdlExpirationPicker;

    @FXML private VBox nurseSection;
    @FXML private TextField nursingLicenseField;
    @FXML private DatePicker nursingExpirationPicker;

    @FXML private VBox counselorSection;
    @FXML private ComboBox<String> counselorLicenseTypeCombo;
    @FXML private TextField counselorLicenseField;
    @FXML private DatePicker counselorExpirationPicker;

    @FXML private TextArea notesArea;

    private Staff staff;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        log.info("Initializing Staff Form Dialog Controller");

        setupOccupationCombo();
        setupTitleCombo();
        setupDepartmentCombo();
        setupCampusCombo();
        setupEmploymentTypeCombo();
        setupSpinners();
        setupParaFields();
        setupCounselorFields();
    }

    private void setupOccupationCombo() {
        // Group occupations by category
        occupationCombo.setItems(FXCollections.observableArrayList(StaffOccupation.values()));

        // Custom cell factory to show category grouping
        occupationCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StaffOccupation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        occupationCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(StaffOccupation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Show/hide occupation-specific sections when occupation changes
        occupationCombo.setOnAction(e -> updateOccupationSections());
    }

    private void setupTitleCombo() {
        titleCombo.setItems(FXCollections.observableArrayList(
                "Mr.", "Ms.", "Mrs.", "Dr.", "Prof."
        ));
    }

    private void setupDepartmentCombo() {
        List<String> departments = staffService.getDistinctDepartments();
        departments.add(0, ""); // Empty option for no department
        departmentCombo.setItems(FXCollections.observableArrayList(departments));
    }

    private void setupCampusCombo() {
        List<Campus> campuses = campusRepository.findAll();
        campusCombo.setItems(FXCollections.observableArrayList(campuses));

        campusCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Campus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        campusCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Campus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
    }

    private void setupEmploymentTypeCombo() {
        employmentTypeCombo.setItems(FXCollections.observableArrayList(
                "Full-Time", "Part-Time", "Contractor", "Temporary", "Seasonal"
        ));
    }

    private void setupSpinners() {
        // Experience spinner (0-50 years)
        SpinnerValueFactory<Integer> expFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0);
        experienceSpinner.setValueFactory(expFactory);

        // Max students spinner (1-10)
        SpinnerValueFactory<Integer> studentsFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1);
        maxStudentsSpinner.setValueFactory(studentsFactory);
    }

    private void setupParaFields() {
        paraAssignmentCombo.setItems(FXCollections.observableArrayList(
                "One-on-One", "Small Group", "Classroom Support", "Floater"
        ));
    }

    private void setupCounselorFields() {
        counselorLicenseTypeCombo.setItems(FXCollections.observableArrayList(
                "LPC", "LCSW", "LMFT", "LP", "NCC", "LPCC", "Other"
        ));
    }

    private void updateOccupationSections() {
        StaffOccupation selected = occupationCombo.getValue();
        if (selected == null) {
            hideAllSections();
            return;
        }

        // Hide all first
        hideAllSections();

        // Show relevant section based on occupation
        switch (selected) {
            case PARAPROFESSIONAL, TEACHER_AIDE -> {
                paraSection.setVisible(true);
                paraSection.setManaged(true);
            }
            case BUS_DRIVER, BUS_AIDE -> {
                driverSection.setVisible(true);
                driverSection.setManaged(true);
            }
            case SCHOOL_NURSE, NURSE_AIDE -> {
                nurseSection.setVisible(true);
                nurseSection.setManaged(true);
            }
            case COUNSELOR, GUIDANCE_COUNSELOR, SCHOOL_PSYCHOLOGIST, SOCIAL_WORKER -> {
                counselorSection.setVisible(true);
                counselorSection.setManaged(true);
            }
            default -> {
                // No special section needed
            }
        }
    }

    private void hideAllSections() {
        paraSection.setVisible(false);
        paraSection.setManaged(false);
        driverSection.setVisible(false);
        driverSection.setManaged(false);
        nurseSection.setVisible(false);
        nurseSection.setManaged(false);
        counselorSection.setVisible(false);
        counselorSection.setManaged(false);
    }

    /**
     * Set staff for editing
     */
    public void setStaff(Staff staff) {
        this.staff = staff;
        this.isEditMode = true;

        dialogTitle.setText("Edit Staff Member");
        dialogSubtitle.setText("Update staff member information");

        // Populate fields
        occupationCombo.setValue(staff.getOccupation());
        jobTitleField.setText(staff.getJobTitle());
        departmentCombo.setValue(staff.getDepartment());
        campusCombo.setValue(staff.getPrimaryCampus());

        titleCombo.setValue(staff.getTitle());
        firstNameField.setText(staff.getFirstName());
        middleNameField.setText(staff.getMiddleName());
        lastNameField.setText(staff.getLastName());
        preferredNameField.setText(staff.getPreferredName());
        employeeIdField.setText(staff.getEmployeeId());

        emailField.setText(staff.getEmail());
        phoneField.setText(staff.getPhoneNumber());
        mobileField.setText(staff.getMobilePhone());
        officeRoomField.setText(staff.getOfficeRoom());

        hireDatePicker.setValue(staff.getHireDate());
        employmentTypeCombo.setValue(staff.getEmploymentType());
        workHoursField.setText(staff.getWorkHours());
        if (staff.getYearsExperience() != null) {
            experienceSpinner.getValueFactory().setValue(staff.getYearsExperience());
        }
        activeCheckbox.setSelected(Boolean.TRUE.equals(staff.getActive()));

        // Paraprofessional fields
        paraAssignmentCombo.setValue(staff.getParaAssignmentType());
        if (staff.getMaxStudents() != null) {
            maxStudentsSpinner.getValueFactory().setValue(staff.getMaxStudents());
        }
        medicalTrainingCheck.setSelected(Boolean.TRUE.equals(staff.getMedicalTraining()));
        behavioralTrainingCheck.setSelected(Boolean.TRUE.equals(staff.getBehavioralTraining()));

        // Driver fields
        cdlNumberField.setText(staff.getCdlNumber());
        cdlExpirationPicker.setValue(staff.getCdlExpiration());

        // Nurse fields
        nursingLicenseField.setText(staff.getNursingLicense());
        nursingExpirationPicker.setValue(staff.getNursingLicenseExpiration());

        // Counselor fields
        counselorLicenseTypeCombo.setValue(staff.getCounselorLicenseType());
        counselorLicenseField.setText(staff.getCounselorLicenseNumber());
        counselorExpirationPicker.setValue(staff.getCounselorLicenseExpiration());

        notesArea.setText(staff.getNotes());

        // Update sections visibility
        updateOccupationSections();
    }

    /**
     * Get the staff object with form data
     */
    public Staff getStaff() {
        if (staff == null) {
            staff = new Staff();
        }

        // Position
        staff.setOccupation(occupationCombo.getValue());
        staff.setJobTitle(jobTitleField.getText());
        staff.setDepartment(departmentCombo.getValue());
        staff.setPrimaryCampus(campusCombo.getValue());

        // Personal
        staff.setTitle(titleCombo.getValue());
        staff.setFirstName(firstNameField.getText());
        staff.setMiddleName(middleNameField.getText());
        staff.setLastName(lastNameField.getText());
        staff.setPreferredName(preferredNameField.getText());
        staff.setEmployeeId(employeeIdField.getText());

        // Contact
        staff.setEmail(emailField.getText());
        staff.setPhoneNumber(phoneField.getText());
        staff.setMobilePhone(mobileField.getText());
        staff.setOfficeRoom(officeRoomField.getText());

        // Employment
        staff.setHireDate(hireDatePicker.getValue());
        staff.setEmploymentType(employmentTypeCombo.getValue());
        staff.setWorkHours(workHoursField.getText());
        staff.setYearsExperience(experienceSpinner.getValue());
        staff.setActive(activeCheckbox.isSelected());

        // Paraprofessional
        staff.setParaAssignmentType(paraAssignmentCombo.getValue());
        staff.setMaxStudents(maxStudentsSpinner.getValue());
        staff.setMedicalTraining(medicalTrainingCheck.isSelected());
        staff.setBehavioralTraining(behavioralTrainingCheck.isSelected());

        // Driver
        staff.setCdlNumber(cdlNumberField.getText());
        staff.setCdlExpiration(cdlExpirationPicker.getValue());

        // Nurse
        staff.setNursingLicense(nursingLicenseField.getText());
        staff.setNursingLicenseExpiration(nursingExpirationPicker.getValue());

        // Counselor
        staff.setCounselorLicenseType(counselorLicenseTypeCombo.getValue());
        staff.setCounselorLicenseNumber(counselorLicenseField.getText());
        staff.setCounselorLicenseExpiration(counselorExpirationPicker.getValue());

        staff.setNotes(notesArea.getText());

        return staff;
    }

    /**
     * Validate the form
     */
    public boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (occupationCombo.getValue() == null) {
            errors.append("- Occupation type is required\n");
        }
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errors.append("- First name is required\n");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errors.append("- Last name is required\n");
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

    /**
     * Save the staff member
     */
    public boolean save() {
        if (!validate()) {
            return false;
        }

        try {
            Staff staffToSave = getStaff();

            if (isEditMode) {
                staffService.updateStaff(staffToSave);
                log.info("Updated staff member: {}", staffToSave.getFullName());
            } else {
                staffService.createStaff(staffToSave);
                log.info("Created new staff member: {}", staffToSave.getFullName());
            }

            return true;
        } catch (Exception e) {
            log.error("Error saving staff member", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not save staff member");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return false;
        }
    }
}
