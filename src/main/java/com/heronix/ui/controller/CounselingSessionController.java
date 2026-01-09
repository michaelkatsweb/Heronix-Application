package com.heronix.ui.controller;

import com.heronix.model.domain.CounselingReferral;
import com.heronix.model.domain.CounselingSession;
import com.heronix.model.domain.Student;
import com.heronix.model.domain.Teacher;
import com.heronix.service.CounselingSessionService;
import com.heronix.service.CounselingManagementService;
import com.heronix.service.StudentService;
import com.heronix.service.TeacherService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Counseling Session Documentation Form
 * Handles individual counseling session notes, progress tracking, and risk assessment
 */
@Component
public class CounselingSessionController {

    private static final Logger logger = LoggerFactory.getLogger(CounselingSessionController.class);

    @Autowired
    private CounselingSessionService counselingSessionService;

    @Autowired
    private CounselingManagementService counselingManagementService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // Session Information
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextField gradeField;
    @FXML private DatePicker sessionDatePicker;
    @FXML private TextField durationField;
    @FXML private ComboBox<Teacher> counselorComboBox;
    @FXML private ComboBox<String> sessionTypeComboBox;
    @FXML private TextField sessionNumberField;
    @FXML private ComboBox<CounselingReferral> referralComboBox;

    // Session Focus
    @FXML private ComboBox<String> primaryFocusComboBox;
    @FXML private TextField secondaryFocusField;

    // Session Notes
    @FXML private TextArea presentingConcernTextArea;
    @FXML private TextArea sessionNotesTextArea;
    @FXML private TextArea interventionsTextArea;

    // Progress & Observations
    @FXML private ComboBox<String> moodComboBox;
    @FXML private ComboBox<String> engagementComboBox;
    @FXML private TextArea progressTextArea;
    @FXML private TextArea observationsTextArea;

    // Risk Assessment
    @FXML private CheckBox suicidalIdeationCheckBox;
    @FXML private CheckBox selfHarmRiskCheckBox;
    @FXML private CheckBox harmToOthersCheckBox;
    @FXML private CheckBox substanceUseCheckBox;
    @FXML private CheckBox abuseDisclosedCheckBox;
    @FXML private CheckBox safetyPlanReviewedCheckBox;
    @FXML private VBox riskDetailsBox;
    @FXML private TextArea riskDetailsTextArea;

    // Goals & Next Steps
    @FXML private TextArea goalsAddressedTextArea;
    @FXML private TextArea homeworkTextArea;
    @FXML private DatePicker followUpDatePicker;
    @FXML private TextField followUpNotesField;

    // Referrals & Collaboration
    @FXML private CheckBox externalReferralMadeCheckBox;
    @FXML private CheckBox parentContactedCheckBox;
    @FXML private CheckBox teacherConsultationCheckBox;
    @FXML private CheckBox adminNotifiedCheckBox;
    @FXML private CheckBox crisisTeamInvolvedCheckBox;
    @FXML private CheckBox emergencyContactCheckBox;
    @FXML private TextArea collaborationTextArea;

    // Counselor Reflections
    @FXML private TextArea clinicalImpressionsTextArea;
    @FXML private TextArea nextSessionPlanTextArea;

    // Buttons
    @FXML private Button saveButton;
    @FXML private Button saveDraftButton;
    @FXML private Button cancelButton;
    @FXML private Label riskIndicator;

    private CounselingSession currentSession;
    private Student preSelectedStudent;
    private CounselingReferral preSelectedReferral;

    @FXML
    public void initialize() {
        logger.info("Initializing CounselingSessionController");

        setupComboBoxConverters();
        setupRiskMonitoring();
        loadFormData();
        setDefaultValues();
    }

    private void setupComboBoxConverters() {
        // Student ComboBox converter
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" :
                        student.getFirstName() + " " + student.getLastName() +
                                " (Grade " + student.getGradeLevel() + ")";
            }

            @Override
            public Student fromString(String string) {
                return null;
            }
        });

        // Auto-populate grade when student selected
        studentComboBox.setOnAction(e -> {
            Student student = studentComboBox.getValue();
            if (student != null) {
                gradeField.setText(String.valueOf(student.getGradeLevel()));
                updateSessionNumber(student);
                loadStudentReferrals(student);
            }
        });

        // Counselor ComboBox converter
        counselorComboBox.setConverter(new javafx.util.StringConverter<Teacher>() {
            @Override
            public String toString(Teacher teacher) {
                return teacher == null ? "" :
                        teacher.getFirstName() + " " + teacher.getLastName();
            }

            @Override
            public Teacher fromString(String string) {
                return null;
            }
        });

        // Referral ComboBox converter
        referralComboBox.setConverter(new javafx.util.StringConverter<CounselingReferral>() {
            @Override
            public String toString(CounselingReferral referral) {
                if (referral == null) return "";
                return referral.getReferralDate().toString() + " - " +
                        (referral.getPrimaryConcern() != null ?
                                referral.getPrimaryConcern().toString().replace("_", " ") : "");
            }

            @Override
            public CounselingReferral fromString(String string) {
                return null;
            }
        });
    }

    private void setupRiskMonitoring() {
        // Show risk details box when any high-risk indicator is checked
        suicidalIdeationCheckBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateRiskDetailsVisibility());
        selfHarmRiskCheckBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateRiskDetailsVisibility());
        harmToOthersCheckBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateRiskDetailsVisibility());
        abuseDisclosedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateRiskDetailsVisibility());
        emergencyContactCheckBox.selectedProperty().addListener((obs, oldVal, newVal) ->
                updateRiskIndicator());
    }

    private void updateRiskDetailsVisibility() {
        boolean hasRisk = suicidalIdeationCheckBox.isSelected() ||
                selfHarmRiskCheckBox.isSelected() ||
                harmToOthersCheckBox.isSelected() ||
                abuseDisclosedCheckBox.isSelected();

        riskDetailsBox.setVisible(hasRisk);
        riskDetailsBox.setManaged(hasRisk);
        updateRiskIndicator();
    }

    private void updateRiskIndicator() {
        if (suicidalIdeationCheckBox.isSelected() || abuseDisclosedCheckBox.isSelected() ||
                emergencyContactCheckBox.isSelected()) {
            riskIndicator.setText("⚠ HIGH RISK SESSION - ENSURE PROPER DOCUMENTATION");
            riskIndicator.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else if (selfHarmRiskCheckBox.isSelected() || harmToOthersCheckBox.isSelected()) {
            riskIndicator.setText("⚠ Risk factors present - Document thoroughly");
            riskIndicator.setStyle("-fx-text-fill: #f57c00; -fx-font-weight: bold;");
        } else {
            riskIndicator.setText("");
        }
    }

    private void loadFormData() {
        try {
            // Load students
            List<Student> students = studentService.getAllStudents();
            studentComboBox.setItems(FXCollections.observableArrayList(students));

            // Load counselors (teachers with counselor role)
            List<Teacher> counselors = teacherService.getAllTeachers().stream()
                    .filter(t -> t.getRole() != null &&
                            (t.getRole().toString().contains("COUNSELOR") ||
                                    t.getRole().toString().contains("SOCIAL_WORKER")))
                    .toList();
            counselorComboBox.setItems(FXCollections.observableArrayList(counselors));

        } catch (Exception e) {
            logger.error("Error loading form data", e);
            showError("Failed to load form data: " + e.getMessage());
        }
    }

    private void setDefaultValues() {
        sessionDatePicker.setValue(LocalDate.now());
        durationField.setText("30");

        // If pre-selected student or referral, set them
        if (preSelectedStudent != null) {
            studentComboBox.setValue(preSelectedStudent);
        }
        if (preSelectedReferral != null) {
            referralComboBox.setValue(preSelectedReferral);
            if (preSelectedStudent == null) {
                studentComboBox.setValue(preSelectedReferral.getStudent());
            }
        }
    }

    private void updateSessionNumber(Student student) {
        try {
            // Get all sessions for this student
            List<CounselingSession> sessions = counselingSessionService.getSessionsByStudent(student);
            int nextSessionNumber = sessions.size() + 1;
            sessionNumberField.setText(String.valueOf(nextSessionNumber));
        } catch (Exception e) {
            logger.warn("Could not calculate session number", e);
            sessionNumberField.setText("1");
        }
    }

    private void loadStudentReferrals(Student student) {
        try {
            List<CounselingReferral> referrals = counselingManagementService.getReferralsByStudent(student);
            referralComboBox.setItems(FXCollections.observableArrayList(referrals));
        } catch (Exception e) {
            logger.error("Error loading student referrals", e);
        }
    }

    @FXML
    private void handleSave() {
        if (validateForm()) {
            if (hasHighRisk() && !confirmHighRiskSession()) {
                return;
            }

            try {
                CounselingSession session = buildSessionFromForm();
                session.setStatus(CounselingSession.SessionStatus.COMPLETED);

                CounselingSession saved = counselingSessionService.saveSession(session);

                if (hasHighRisk()) {
                    showWarning("High-Risk Session Saved",
                            "This session has been flagged for supervisor review.\n" +
                                    "Ensure all required protocols have been followed.");
                } else {
                    showSuccess("Counseling session saved successfully");
                }

                closeWindow();

            } catch (Exception e) {
                logger.error("Error saving counseling session", e);
                showError("Failed to save session: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveDraft() {
        try {
            CounselingSession session = buildSessionFromForm();
            session.setStatus(CounselingSession.SessionStatus.DRAFT);

            counselingSessionService.saveSession(session);
            showSuccess("Session saved as draft");
            closeWindow();

        } catch (Exception e) {
            logger.error("Error saving draft", e);
            showError("Failed to save draft: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        List<String> errors = new ArrayList<>();

        if (studentComboBox.getValue() == null) {
            errors.add("Student is required");
        }
        if (sessionDatePicker.getValue() == null) {
            errors.add("Session date is required");
        }
        if (counselorComboBox.getValue() == null) {
            errors.add("Counselor is required");
        }
        if (sessionTypeComboBox.getValue() == null || sessionTypeComboBox.getValue().isEmpty()) {
            errors.add("Session type is required");
        }
        if (primaryFocusComboBox.getValue() == null || primaryFocusComboBox.getValue().isEmpty()) {
            errors.add("Primary focus is required");
        }
        if (sessionNotesTextArea.getText() == null || sessionNotesTextArea.getText().trim().isEmpty()) {
            errors.add("Session notes are required");
        }

        // Validate risk details if high-risk boxes checked
        if (hasHighRisk()) {
            if (riskDetailsTextArea.getText() == null || riskDetailsTextArea.getText().trim().isEmpty()) {
                errors.add("Risk details are required when high-risk indicators are checked");
            }
        }

        // Validate duration
        if (durationField.getText() != null && !durationField.getText().trim().isEmpty()) {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                if (duration <= 0 || duration > 240) {
                    errors.add("Duration must be between 1 and 240 minutes");
                }
            } catch (NumberFormatException e) {
                errors.add("Duration must be a valid number");
            }
        }

        if (!errors.isEmpty()) {
            showValidationErrors(errors);
            return false;
        }

        return true;
    }

    private CounselingSession buildSessionFromForm() {
        CounselingSession session = currentSession != null ? currentSession : new CounselingSession();

        // Session information
        session.setStudent(studentComboBox.getValue());
        session.setSessionDate(sessionDatePicker.getValue());
        if (durationField.getText() != null && !durationField.getText().trim().isEmpty()) {
            session.setDuration(Integer.parseInt(durationField.getText().trim()));
        }
        session.setCounselor(counselorComboBox.getValue());
        if (sessionTypeComboBox.getValue() != null) {
            session.setSessionType(CounselingSession.SessionType.valueOf(sessionTypeComboBox.getValue()));
        }
        if (sessionNumberField.getText() != null && !sessionNumberField.getText().isEmpty()) {
            try {
                // Validate it's a number, then store as String
                Integer.parseInt(sessionNumberField.getText());
                session.setSessionNumber(sessionNumberField.getText());
            } catch (NumberFormatException e) {
                session.setSessionNumber("1");
            }
        }
        session.setReferral(referralComboBox.getValue());

        // Session focus
        if (primaryFocusComboBox.getValue() != null) {
            session.setPrimaryFocus(CounselingSession.CounselingFocus.valueOf(primaryFocusComboBox.getValue()));
        }
        session.setSecondaryFocus(secondaryFocusField.getText());

        // Session notes
        session.setPresentingConcern(presentingConcernTextArea.getText());
        session.setSessionNotes(sessionNotesTextArea.getText());
        session.setInterventionsUsed(interventionsTextArea.getText());

        // Progress & observations
        if (moodComboBox.getValue() != null) {
            session.setMoodObserved(CounselingSession.Mood.valueOf(moodComboBox.getValue()));
        }
        if (engagementComboBox.getValue() != null) {
            session.setEngagementLevel(CounselingSession.EngagementLevel.valueOf(engagementComboBox.getValue()));
        }
        session.setProgressNotes(progressTextArea.getText());
        session.setBehavioralObservations(observationsTextArea.getText());

        // Risk assessment
        session.setSuicidalIdeation(suicidalIdeationCheckBox.isSelected());
        session.setSelfHarmRisk(selfHarmRiskCheckBox.isSelected());
        session.setHarmToOthersRisk(harmToOthersCheckBox.isSelected());
        session.setSubstanceUseDisclosed(substanceUseCheckBox.isSelected());
        session.setAbuseDisclosed(abuseDisclosedCheckBox.isSelected());
        session.setSafetyPlanReviewed(safetyPlanReviewedCheckBox.isSelected());
        session.setRiskDetails(riskDetailsTextArea.getText());

        // Set risk level based on checkboxes
        if (suicidalIdeationCheckBox.isSelected() || abuseDisclosedCheckBox.isSelected()) {
            session.setRiskLevel(CounselingSession.RiskLevel.IMMINENT);
        } else if (selfHarmRiskCheckBox.isSelected() || harmToOthersCheckBox.isSelected()) {
            session.setRiskLevel(CounselingSession.RiskLevel.HIGH);
        } else if (substanceUseCheckBox.isSelected()) {
            session.setRiskLevel(CounselingSession.RiskLevel.MODERATE);
        } else {
            session.setRiskLevel(CounselingSession.RiskLevel.LOW);
        }

        // Goals & next steps
        session.setGoalsAddressed(goalsAddressedTextArea.getText());
        session.setHomeworkAssigned(homeworkTextArea.getText());
        session.setFollowUpDate(followUpDatePicker.getValue());
        session.setFollowUpNotes(followUpNotesField.getText());

        // Referrals & collaboration
        session.setExternalReferralMade(externalReferralMadeCheckBox.isSelected());
        session.setParentContacted(parentContactedCheckBox.isSelected());
        session.setTeacherConsultation(teacherConsultationCheckBox.isSelected());
        session.setAdminNotified(adminNotifiedCheckBox.isSelected());
        session.setCrisisTeamInvolved(crisisTeamInvolvedCheckBox.isSelected());
        session.setEmergencyContactMade(emergencyContactCheckBox.isSelected());
        session.setCollaborationDetails(collaborationTextArea.getText());

        // Counselor reflections
        session.setClinicalImpressions(clinicalImpressionsTextArea.getText());
        session.setNextSessionPlan(nextSessionPlanTextArea.getText());

        return session;
    }

    private boolean hasHighRisk() {
        return suicidalIdeationCheckBox.isSelected() ||
                selfHarmRiskCheckBox.isSelected() ||
                harmToOthersCheckBox.isSelected() ||
                abuseDisclosedCheckBox.isSelected();
    }

    private boolean confirmHighRiskSession() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("High-Risk Session Confirmation");
        alert.setHeaderText("This session contains HIGH-RISK indicators");
        alert.setContentText(
                "Have you:\n" +
                        "• Completed a thorough risk assessment?\n" +
                        "• Made necessary notifications (parent, admin, crisis team)?\n" +
                        "• Documented all safety interventions?\n" +
                        "• Reviewed/created a safety plan if needed?\n\n" +
                        "Proceed with saving this session?");

        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    /**
     * Pre-select a student for the session
     */
    public void setStudent(Student student) {
        this.preSelectedStudent = student;
        if (studentComboBox != null) {
            studentComboBox.setValue(student);
        }
    }

    /**
     * Pre-select a referral and associated student
     */
    public void setReferral(CounselingReferral referral) {
        this.preSelectedReferral = referral;
        if (referral != null) {
            this.preSelectedStudent = referral.getStudent();
            if (studentComboBox != null) {
                studentComboBox.setValue(referral.getStudent());
            }
            if (referralComboBox != null) {
                referralComboBox.setValue(referral);
            }
        }
    }

    /**
     * Load existing session for editing
     */
    public void loadSession(CounselingSession session) {
        this.currentSession = session;
        if (session != null) {
            populateForm(session);
        }
    }

    private void populateForm(CounselingSession session) {
        studentComboBox.setValue(session.getStudent());
        sessionDatePicker.setValue(session.getSessionDate());
        durationField.setText(String.valueOf(session.getDuration()));
        counselorComboBox.setValue(session.getCounselor());
        if (session.getSessionType() != null) {
            sessionTypeComboBox.setValue(session.getSessionType().name());
        }
        sessionNumberField.setText(String.valueOf(session.getSessionNumber()));
        referralComboBox.setValue(session.getReferral());

        if (session.getPrimaryFocus() != null) {
            primaryFocusComboBox.setValue(session.getPrimaryFocus().name());
        }
        secondaryFocusField.setText(session.getSecondaryFocus());

        presentingConcernTextArea.setText(session.getPresentingConcern());
        sessionNotesTextArea.setText(session.getSessionNotes());
        interventionsTextArea.setText(session.getInterventionsUsed());

        if (session.getMoodObserved() != null) {
            moodComboBox.setValue(session.getMoodObserved().name());
        }
        if (session.getEngagementLevel() != null) {
            engagementComboBox.setValue(session.getEngagementLevel().name());
        }
        progressTextArea.setText(session.getProgressNotes());
        observationsTextArea.setText(session.getBehavioralObservations());

        suicidalIdeationCheckBox.setSelected(session.isSuicidalIdeation());
        selfHarmRiskCheckBox.setSelected(session.isSelfHarmRisk());
        harmToOthersCheckBox.setSelected(session.isHarmToOthersRisk());
        substanceUseCheckBox.setSelected(session.isSubstanceUseDisclosed());
        abuseDisclosedCheckBox.setSelected(session.isAbuseDisclosed());
        safetyPlanReviewedCheckBox.setSelected(session.isSafetyPlanReviewed());
        riskDetailsTextArea.setText(session.getRiskDetails());

        goalsAddressedTextArea.setText(session.getGoalsAddressed());
        homeworkTextArea.setText(session.getHomeworkAssigned());
        followUpDatePicker.setValue(session.getFollowUpDate());
        followUpNotesField.setText(session.getFollowUpNotes());

        externalReferralMadeCheckBox.setSelected(session.isExternalReferralMade());
        parentContactedCheckBox.setSelected(session.isParentContacted());
        teacherConsultationCheckBox.setSelected(session.isTeacherConsultation());
        adminNotifiedCheckBox.setSelected(session.isAdminNotified());
        crisisTeamInvolvedCheckBox.setSelected(session.isCrisisTeamInvolved());
        emergencyContactCheckBox.setSelected(session.isEmergencyContactMade());
        collaborationTextArea.setText(session.getCollaborationDetails());

        clinicalImpressionsTextArea.setText(session.getClinicalImpressions());
        nextSessionPlanTextArea.setText(session.getNextSessionPlan());
    }

    /**
     * Set the form to view-only mode (disable editing)
     */
    public void setViewMode(boolean viewMode) {
        if (viewMode) {
            studentComboBox.setDisable(true);
            sessionDatePicker.setDisable(true);
            durationField.setDisable(true);
            counselorComboBox.setDisable(true);
            sessionTypeComboBox.setDisable(true);
            referralComboBox.setDisable(true);

            primaryFocusComboBox.setDisable(true);
            secondaryFocusField.setDisable(true);

            presentingConcernTextArea.setDisable(true);
            sessionNotesTextArea.setDisable(true);
            interventionsTextArea.setDisable(true);

            moodComboBox.setDisable(true);
            engagementComboBox.setDisable(true);
            progressTextArea.setDisable(true);
            observationsTextArea.setDisable(true);

            suicidalIdeationCheckBox.setDisable(true);
            selfHarmRiskCheckBox.setDisable(true);
            harmToOthersCheckBox.setDisable(true);
            substanceUseCheckBox.setDisable(true);
            abuseDisclosedCheckBox.setDisable(true);
            safetyPlanReviewedCheckBox.setDisable(true);
            riskDetailsTextArea.setDisable(true);

            goalsAddressedTextArea.setDisable(true);
            homeworkTextArea.setDisable(true);
            followUpDatePicker.setDisable(true);
            followUpNotesField.setDisable(true);

            externalReferralMadeCheckBox.setDisable(true);
            parentContactedCheckBox.setDisable(true);
            teacherConsultationCheckBox.setDisable(true);
            adminNotifiedCheckBox.setDisable(true);
            crisisTeamInvolvedCheckBox.setDisable(true);
            emergencyContactCheckBox.setDisable(true);
            collaborationTextArea.setDisable(true);

            clinicalImpressionsTextArea.setDisable(true);
            nextSessionPlanTextArea.setDisable(true);

            saveButton.setVisible(false);
            saveDraftButton.setVisible(false);
            cancelButton.setText("Close");
        }
    }

    private void showValidationErrors(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText("Please correct the following errors:");
        alert.setContentText(String.join("\n", errors));
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
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

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
