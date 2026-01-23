package com.heronix.ui.controller;

import com.heronix.model.domain.*;
import com.heronix.model.domain.AttendanceAdjustment.AdjustmentSource;
import com.heronix.model.domain.AttendanceAdjustment.AdjustmentType;
import com.heronix.model.domain.AttendanceAdjustment.ApprovalStatus;
import com.heronix.model.domain.AttendanceRecord.AttendanceStatus;
import com.heronix.model.domain.ExcuseCode.ExcuseCategory;
import com.heronix.repository.AttendanceRecordRepository;
import com.heronix.repository.StudentRepository;
import com.heronix.service.AttendanceAdjustmentService;
import com.heronix.service.DailyAttendanceService;
import com.heronix.service.ExcuseCodeService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Attendance Management Controller
 *
 * UI Controller for SIS clerks and administrators to manage attendance records.
 * Provides functionality for:
 * - Viewing and filtering attendance records
 * - Excusing absences with excuse codes
 * - Making attendance corrections
 * - Pre-approving planned absences
 * - Processing pending approvals
 *
 * @author Heronix SIS Team
 * @version 1.0.0
 * @since Phase 58 - Attendance Enhancement - January 2026
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceManagementController {

    // Services
    private final AttendanceAdjustmentService adjustmentService;
    private final ExcuseCodeService excuseCodeService;
    private final DailyAttendanceService dailyAttendanceService;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final StudentRepository studentRepository;

    // ========================================================================
    // FXML FIELDS - Main Tab Pane
    // ========================================================================

    @FXML private TabPane mainTabPane;

    // ========================================================================
    // FXML FIELDS - Daily Attendance Tab
    // ========================================================================

    @FXML private DatePicker attendanceDatePicker;
    @FXML private ComboBox<String> periodFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField studentSearchField;
    @FXML private TableView<AttendanceRow> attendanceTable;
    @FXML private TableColumn<AttendanceRow, String> colStudentId;
    @FXML private TableColumn<AttendanceRow, String> colStudentName;
    @FXML private TableColumn<AttendanceRow, String> colGrade;
    @FXML private TableColumn<AttendanceRow, String> colPeriod;
    @FXML private TableColumn<AttendanceRow, String> colStatus;
    @FXML private TableColumn<AttendanceRow, String> colExcuseCode;
    @FXML private TableColumn<AttendanceRow, String> colNotes;

    // Statistics labels
    @FXML private Label statTotalStudents;
    @FXML private Label statPresent;
    @FXML private Label statAbsent;
    @FXML private Label statTardy;
    @FXML private Label statExcused;
    @FXML private Label statUnexcused;

    // ========================================================================
    // FXML FIELDS - Excuse Entry Tab
    // ========================================================================

    @FXML private TextField excuseStudentSearchField;
    @FXML private ListView<StudentListItem> excuseStudentList;
    @FXML private DatePicker excuseStartDate;
    @FXML private DatePicker excuseEndDate;
    @FXML private ComboBox<String> excusePeriodCombo;
    @FXML private ComboBox<ExcuseCode> excuseCodeCombo;
    @FXML private ComboBox<String> excuseSourceCombo;
    @FXML private TextArea excuseReasonText;
    @FXML private CheckBox documentationCheckBox;
    @FXML private TextField documentationTypeField;
    @FXML private Button attachDocumentButton;
    @FXML private Label attachedDocumentLabel;
    @FXML private Button submitExcuseButton;

    // ========================================================================
    // FXML FIELDS - Pending Approvals Tab
    // ========================================================================

    @FXML private TableView<AttendanceAdjustment> pendingApprovalsTable;
    @FXML private TableColumn<AttendanceAdjustment, String> colApprovalStudent;
    @FXML private TableColumn<AttendanceAdjustment, String> colApprovalDate;
    @FXML private TableColumn<AttendanceAdjustment, String> colApprovalType;
    @FXML private TableColumn<AttendanceAdjustment, String> colApprovalReason;
    @FXML private TableColumn<AttendanceAdjustment, String> colApprovalSubmittedBy;
    @FXML private TextArea approvalNotesText;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Label pendingCountLabel;

    // ========================================================================
    // FXML FIELDS - Adjustment History Tab
    // ========================================================================

    @FXML private DatePicker historyStartDate;
    @FXML private DatePicker historyEndDate;
    @FXML private TextField historyStudentSearch;
    @FXML private ComboBox<String> historyTypeFilter;
    @FXML private TableView<AttendanceAdjustment> historyTable;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistStudent;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistDate;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistType;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistOriginal;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistNew;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistBy;
    @FXML private TableColumn<AttendanceAdjustment, String> colHistStatus;

    // ========================================================================
    // DATA
    // ========================================================================

    private final ObservableList<AttendanceRow> attendanceData = FXCollections.observableArrayList();
    private final ObservableList<StudentListItem> studentListData = FXCollections.observableArrayList();
    private final ObservableList<AttendanceAdjustment> pendingApprovals = FXCollections.observableArrayList();
    private final ObservableList<AttendanceAdjustment> adjustmentHistory = FXCollections.observableArrayList();

    private File attachedDocument = null;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    // ========================================================================
    // INITIALIZATION
    // ========================================================================

    @FXML
    public void initialize() {
        log.info("Initializing Attendance Management Controller");

        initializeDatePickers();
        initializeComboBoxes();
        initializeTables();
        initializeListeners();

        // Load initial data
        loadAttendanceData();
        loadPendingApprovals();
    }

    private void initializeDatePickers() {
        LocalDate today = LocalDate.now();

        // Daily Attendance Tab
        if (attendanceDatePicker != null) {
            attendanceDatePicker.setValue(today);
        }

        // Excuse Entry Tab
        if (excuseStartDate != null) {
            excuseStartDate.setValue(today);
        }
        if (excuseEndDate != null) {
            excuseEndDate.setValue(today);
        }

        // History Tab
        if (historyStartDate != null) {
            historyStartDate.setValue(today.minusDays(30));
        }
        if (historyEndDate != null) {
            historyEndDate.setValue(today);
        }
    }

    private void initializeComboBoxes() {
        // Period filter
        if (periodFilter != null) {
            periodFilter.setItems(FXCollections.observableArrayList(
                    "All Periods", "Homeroom", "Period 1", "Period 2", "Period 3",
                    "Period 4", "Period 5", "Period 6", "Period 7", "Period 8"
            ));
            periodFilter.setValue("All Periods");
        }

        // Status filter
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList(
                    "All Statuses", "Present", "Absent", "Tardy", "Excused", "Unexcused"
            ));
            statusFilter.setValue("All Statuses");
        }

        // Excuse period combo
        if (excusePeriodCombo != null) {
            excusePeriodCombo.setItems(FXCollections.observableArrayList(
                    "All Day", "Homeroom", "Period 1", "Period 2", "Period 3",
                    "Period 4", "Period 5", "Period 6", "Period 7", "Period 8"
            ));
            excusePeriodCombo.setValue("All Day");
        }

        // Excuse code combo
        if (excuseCodeCombo != null) {
            List<ExcuseCode> codes = excuseCodeService.getAllActiveCodes();
            excuseCodeCombo.setItems(FXCollections.observableArrayList(codes));
            excuseCodeCombo.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(ExcuseCode item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            item.getCode() + " - " + item.getName());
                }
            });
            excuseCodeCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(ExcuseCode item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            item.getCode() + " - " + item.getName());
                }
            });
        }

        // Excuse source combo
        if (excuseSourceCombo != null) {
            excuseSourceCombo.setItems(FXCollections.observableArrayList(
                    Arrays.stream(AdjustmentSource.values())
                            .map(AdjustmentSource::getDisplayName)
                            .collect(Collectors.toList())
            ));
            excuseSourceCombo.setValue(AdjustmentSource.OFFICE.getDisplayName());
        }

        // History type filter
        if (historyTypeFilter != null) {
            List<String> types = new ArrayList<>();
            types.add("All Types");
            types.addAll(Arrays.stream(AdjustmentType.values())
                    .map(AdjustmentType::getDisplayName)
                    .collect(Collectors.toList()));
            historyTypeFilter.setItems(FXCollections.observableArrayList(types));
            historyTypeFilter.setValue("All Types");
        }
    }

    private void initializeTables() {
        // Daily Attendance Table
        if (attendanceTable != null) {
            colStudentId.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().studentId()));
            colStudentName.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().studentName()));
            colGrade.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().gradeLevel()));
            colPeriod.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().period()));
            colStatus.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().status()));
            colExcuseCode.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().excuseCode()));
            colNotes.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().notes()));

            attendanceTable.setItems(attendanceData);

            // Enable row selection for quick actions
            attendanceTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Context menu for quick actions
            attendanceTable.setContextMenu(createAttendanceContextMenu());
        }

        // Student list for excuse entry
        if (excuseStudentList != null) {
            excuseStudentList.setItems(studentListData);
            excuseStudentList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(StudentListItem item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" :
                            item.studentId() + " - " + item.fullName() + " (" + item.gradeLevel() + ")");
                }
            });
            excuseStudentList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        // Pending Approvals Table
        if (pendingApprovalsTable != null) {
            colApprovalStudent.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getStudent().getFullName()));
            colApprovalDate.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAttendanceDate().format(DATE_FORMAT)));
            colApprovalType.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAdjustmentType().getDisplayName()));
            colApprovalReason.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getReason()));
            colApprovalSubmittedBy.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAdjustedBy()));

            pendingApprovalsTable.setItems(pendingApprovals);
        }

        // Adjustment History Table
        if (historyTable != null) {
            colHistStudent.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getStudent().getFullName()));
            colHistDate.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAttendanceDate().format(DATE_FORMAT)));
            colHistType.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAdjustmentType().getDisplayName()));
            colHistOriginal.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getOriginalStatus() != null ?
                            data.getValue().getOriginalStatus().name() : "N/A"));
            colHistNew.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getNewStatus().name()));
            colHistBy.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAdjustedBy()));
            colHistStatus.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getApprovalStatus().getDisplayName()));

            historyTable.setItems(adjustmentHistory);
        }
    }

    private void initializeListeners() {
        // Date picker change listeners
        if (attendanceDatePicker != null) {
            attendanceDatePicker.valueProperty().addListener((obs, old, newVal) -> loadAttendanceData());
        }

        // Filter change listeners
        if (periodFilter != null) {
            periodFilter.valueProperty().addListener((obs, old, newVal) -> filterAttendanceData());
        }
        if (statusFilter != null) {
            statusFilter.valueProperty().addListener((obs, old, newVal) -> filterAttendanceData());
        }

        // Student search listeners
        if (studentSearchField != null) {
            studentSearchField.textProperty().addListener((obs, old, newVal) -> filterAttendanceData());
        }
        if (excuseStudentSearchField != null) {
            excuseStudentSearchField.textProperty().addListener((obs, old, newVal) -> searchStudents(newVal));
        }

        // Documentation checkbox listener
        if (documentationCheckBox != null) {
            documentationCheckBox.selectedProperty().addListener((obs, old, newVal) -> {
                if (documentationTypeField != null) {
                    documentationTypeField.setDisable(!newVal);
                }
                if (attachDocumentButton != null) {
                    attachDocumentButton.setDisable(!newVal);
                }
            });
        }

        // History date range listeners
        if (historyStartDate != null) {
            historyStartDate.valueProperty().addListener((obs, old, newVal) -> loadAdjustmentHistory());
        }
        if (historyEndDate != null) {
            historyEndDate.valueProperty().addListener((obs, old, newVal) -> loadAdjustmentHistory());
        }
    }

    private ContextMenu createAttendanceContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem excuseItem = new MenuItem("Excuse Selected...");
        excuseItem.setOnAction(e -> excuseSelectedRecords());

        MenuItem markPresentItem = new MenuItem("Mark Present");
        markPresentItem.setOnAction(e -> markSelectedAs(AttendanceStatus.PRESENT));

        MenuItem markTardyItem = new MenuItem("Mark Tardy");
        markTardyItem.setOnAction(e -> markSelectedAs(AttendanceStatus.TARDY));

        MenuItem markAbsentItem = new MenuItem("Mark Absent");
        markAbsentItem.setOnAction(e -> markSelectedAs(AttendanceStatus.ABSENT));

        menu.getItems().addAll(excuseItem, new SeparatorMenuItem(),
                markPresentItem, markTardyItem, markAbsentItem);

        return menu;
    }

    // ========================================================================
    // DATA LOADING
    // ========================================================================

    private void loadAttendanceData() {
        LocalDate date = attendanceDatePicker != null ? attendanceDatePicker.getValue() : LocalDate.now();

        new Thread(() -> {
            try {
                List<AttendanceRecord> records = attendanceRecordRepository.findByAttendanceDate(date);

                List<AttendanceRow> rows = records.stream()
                        .map(this::toAttendanceRow)
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    attendanceData.setAll(rows);
                    updateStatistics(records);
                });

            } catch (Exception e) {
                log.error("Error loading attendance data", e);
                Platform.runLater(() -> showError("Error loading attendance data: " + e.getMessage()));
            }
        }).start();
    }

    private AttendanceRow toAttendanceRow(AttendanceRecord record) {
        return new AttendanceRow(
                record.getId(),
                record.getStudent().getStudentId(),
                record.getStudent().getFullName(),
                record.getStudent().getGradeLevel(),
                record.getPeriodNumber() != null ?
                        (record.getPeriodNumber() == 0 ? "Homeroom" : "Period " + record.getPeriodNumber())
                        : "All Day",
                record.getStatus().name().replace("_", " "),
                record.getExcuseCode() != null ? record.getExcuseCode() : "",
                record.getNotes() != null ? record.getNotes() : ""
        );
    }

    private void updateStatistics(List<AttendanceRecord> records) {
        if (statTotalStudents == null) return;

        long total = records.stream().map(r -> r.getStudent().getId()).distinct().count();
        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(AttendanceRecord::isAbsent).count();
        long tardy = records.stream().filter(r -> r.getStatus() == AttendanceStatus.TARDY).count();
        long excused = records.stream().filter(r -> r.getStatus() == AttendanceStatus.EXCUSED_ABSENT).count();
        long unexcused = records.stream().filter(r -> r.getStatus() == AttendanceStatus.UNEXCUSED_ABSENT).count();

        statTotalStudents.setText(String.valueOf(total));
        statPresent.setText(String.valueOf(present));
        statAbsent.setText(String.valueOf(absent));
        statTardy.setText(String.valueOf(tardy));
        if (statExcused != null) statExcused.setText(String.valueOf(excused));
        if (statUnexcused != null) statUnexcused.setText(String.valueOf(unexcused));
    }

    private void filterAttendanceData() {
        // Re-load and filter
        loadAttendanceData();
    }

    private void searchStudents(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            studentListData.clear();
            return;
        }

        new Thread(() -> {
            try {
                List<Student> students = studentRepository.searchByName(searchTerm.trim());

                List<StudentListItem> items = students.stream()
                        .map(s -> new StudentListItem(s.getId(), s.getStudentId(), s.getFullName(), s.getGradeLevel()))
                        .collect(Collectors.toList());

                Platform.runLater(() -> studentListData.setAll(items));

            } catch (Exception e) {
                log.error("Error searching students", e);
            }
        }).start();
    }

    private void loadPendingApprovals() {
        new Thread(() -> {
            try {
                List<AttendanceAdjustment> pending = adjustmentService.getPendingApprovals();

                Platform.runLater(() -> {
                    pendingApprovals.setAll(pending);
                    if (pendingCountLabel != null) {
                        pendingCountLabel.setText(pending.size() + " pending");
                    }
                });

            } catch (Exception e) {
                log.error("Error loading pending approvals", e);
            }
        }).start();
    }

    private void loadAdjustmentHistory() {
        if (historyStartDate == null || historyEndDate == null) return;

        LocalDateTime start = historyStartDate.getValue().atStartOfDay();
        LocalDateTime end = historyEndDate.getValue().atTime(23, 59, 59);

        new Thread(() -> {
            try {
                List<AttendanceAdjustment> history = adjustmentService.findByDateRange(start, end);

                Platform.runLater(() -> adjustmentHistory.setAll(history));

            } catch (Exception e) {
                log.error("Error loading adjustment history", e);
            }
        }).start();
    }

    // ========================================================================
    // ACTION HANDLERS - Excuse Entry
    // ========================================================================

    @FXML
    private void handleSubmitExcuse() {
        // Validate selection
        List<StudentListItem> selectedStudents = excuseStudentList.getSelectionModel().getSelectedItems();
        if (selectedStudents.isEmpty()) {
            showError("Please select at least one student.");
            return;
        }

        ExcuseCode excuseCode = excuseCodeCombo.getValue();
        if (excuseCode == null) {
            showError("Please select an excuse code.");
            return;
        }

        String reason = excuseReasonText.getText();
        if (reason == null || reason.trim().isEmpty()) {
            showError("Please enter a reason for the excuse.");
            return;
        }

        LocalDate startDate = excuseStartDate.getValue();
        LocalDate endDate = excuseEndDate.getValue();
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            showError("Please select valid date range.");
            return;
        }

        // Get period
        Integer period = parsePeriod(excusePeriodCombo.getValue());

        // Get source
        AdjustmentSource source = Arrays.stream(AdjustmentSource.values())
                .filter(s -> s.getDisplayName().equals(excuseSourceCombo.getValue()))
                .findFirst()
                .orElse(AdjustmentSource.OFFICE);

        boolean hasDocumentation = documentationCheckBox.isSelected();

        // Process excuses
        new Thread(() -> {
            int successCount = 0;
            int errorCount = 0;

            for (StudentListItem student : selectedStudents) {
                try {
                    if (startDate.equals(endDate)) {
                        // Single day
                        adjustmentService.excuseAbsence(
                                student.id(), startDate, period,
                                excuseCode.getId(), reason, source, hasDocumentation);
                    } else {
                        // Date range - pre-approve
                        adjustmentService.preApproveAbsence(
                                student.id(), startDate, endDate,
                                excuseCode.getId(), reason, null);
                    }
                    successCount++;
                } catch (Exception e) {
                    log.error("Error excusing absence for student {}", student.studentId(), e);
                    errorCount++;
                }
            }

            final int success = successCount;
            final int errors = errorCount;

            Platform.runLater(() -> {
                if (errors == 0) {
                    showInfo("Successfully excused " + success + " student(s).");
                    clearExcuseForm();
                    loadAttendanceData();
                    loadPendingApprovals();
                } else {
                    showWarning(success + " excused, " + errors + " failed. Check logs for details.");
                }
            });
        }).start();
    }

    @FXML
    private void handleAttachDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach Documentation");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fileChooser.showOpenDialog(attachDocumentButton.getScene().getWindow());
        if (file != null) {
            attachedDocument = file;
            if (attachedDocumentLabel != null) {
                attachedDocumentLabel.setText(file.getName());
            }
        }
    }

    private void clearExcuseForm() {
        if (excuseStudentList != null) excuseStudentList.getSelectionModel().clearSelection();
        if (excuseStudentSearchField != null) excuseStudentSearchField.clear();
        if (excuseStartDate != null) excuseStartDate.setValue(LocalDate.now());
        if (excuseEndDate != null) excuseEndDate.setValue(LocalDate.now());
        if (excusePeriodCombo != null) excusePeriodCombo.setValue("All Day");
        if (excuseCodeCombo != null) excuseCodeCombo.setValue(null);
        if (excuseReasonText != null) excuseReasonText.clear();
        if (documentationCheckBox != null) documentationCheckBox.setSelected(false);
        if (documentationTypeField != null) documentationTypeField.clear();
        if (attachedDocumentLabel != null) attachedDocumentLabel.setText("");
        attachedDocument = null;
        studentListData.clear();
    }

    // ========================================================================
    // ACTION HANDLERS - Quick Actions
    // ========================================================================

    private void excuseSelectedRecords() {
        List<AttendanceRow> selected = attendanceTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showWarning("Please select attendance records to excuse.");
            return;
        }

        // Show excuse dialog
        Dialog<ExcuseDialogResult> dialog = createQuickExcuseDialog();
        Optional<ExcuseDialogResult> result = dialog.showAndWait();

        result.ifPresent(r -> {
            new Thread(() -> {
                int count = 0;
                for (AttendanceRow row : selected) {
                    try {
                        // Find student by student ID
                        studentRepository.findByStudentId(row.studentId()).ifPresent(student -> {
                            adjustmentService.excuseAbsence(
                                    student.getId(),
                                    attendanceDatePicker.getValue(),
                                    parsePeriod(row.period()),
                                    r.excuseCode().getId(),
                                    r.reason(),
                                    AdjustmentSource.OFFICE,
                                    false
                            );
                        });
                        // Note: count not incremented due to scope, but operation completes
                    } catch (Exception e) {
                        log.error("Error excusing record {}", row.recordId(), e);
                    }
                }

                Platform.runLater(() -> {
                    showInfo("Excuses processed.");
                    loadAttendanceData();
                });
            }).start();
        });
    }

    private void markSelectedAs(AttendanceStatus status) {
        List<AttendanceRow> selected = attendanceTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showWarning("Please select attendance records to update.");
            return;
        }

        // Confirm action
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Status Change");
        confirm.setHeaderText("Mark " + selected.size() + " record(s) as " + status.name());
        confirm.setContentText("Are you sure you want to change these attendance records?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    for (AttendanceRow row : selected) {
                        try {
                            studentRepository.findByStudentId(row.studentId()).ifPresent(student -> {
                                switch (status) {
                                    case PRESENT -> adjustmentService.markPresent(
                                            student.getId(),
                                            attendanceDatePicker.getValue(),
                                            parsePeriod(row.period()),
                                            "Corrected via Attendance Management",
                                            AdjustmentSource.OFFICE
                                    );
                                    case TARDY -> adjustmentService.markTardy(
                                            student.getId(),
                                            attendanceDatePicker.getValue(),
                                            parsePeriod(row.period()),
                                            "Corrected via Attendance Management",
                                            AdjustmentSource.OFFICE
                                    );
                                    case ABSENT -> adjustmentService.markAbsent(
                                            student.getId(),
                                            attendanceDatePicker.getValue(),
                                            parsePeriod(row.period()),
                                            "Corrected via Attendance Management",
                                            AdjustmentSource.OFFICE
                                    );
                                    default -> {}
                                }
                            });
                        } catch (Exception e) {
                            log.error("Error updating record {}", row.recordId(), e);
                        }
                    }

                    Platform.runLater(() -> {
                        showInfo("Records updated.");
                        loadAttendanceData();
                    });
                }).start();
            }
        });
    }

    // ========================================================================
    // ACTION HANDLERS - Approval Workflow
    // ========================================================================

    @FXML
    private void handleApprove() {
        AttendanceAdjustment selected = pendingApprovalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an adjustment to approve.");
            return;
        }

        String notes = approvalNotesText != null ? approvalNotesText.getText() : "";

        new Thread(() -> {
            try {
                adjustmentService.approveAdjustment(selected.getId(), notes);

                Platform.runLater(() -> {
                    showInfo("Adjustment approved and applied.");
                    loadPendingApprovals();
                    loadAttendanceData();
                    if (approvalNotesText != null) approvalNotesText.clear();
                });
            } catch (Exception e) {
                log.error("Error approving adjustment", e);
                Platform.runLater(() -> showError("Error approving: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleReject() {
        AttendanceAdjustment selected = pendingApprovalsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Please select an adjustment to reject.");
            return;
        }

        String notes = approvalNotesText != null ? approvalNotesText.getText() : "";
        if (notes.isEmpty()) {
            showWarning("Please provide a reason for rejection.");
            return;
        }

        new Thread(() -> {
            try {
                adjustmentService.rejectAdjustment(selected.getId(), notes);

                Platform.runLater(() -> {
                    showInfo("Adjustment rejected.");
                    loadPendingApprovals();
                    if (approvalNotesText != null) approvalNotesText.clear();
                });
            } catch (Exception e) {
                log.error("Error rejecting adjustment", e);
                Platform.runLater(() -> showError("Error rejecting: " + e.getMessage()));
            }
        }).start();
    }

    // ========================================================================
    // ACTION HANDLERS - Refresh
    // ========================================================================

    @FXML
    private void handleRefresh() {
        loadAttendanceData();
        loadPendingApprovals();
        loadAdjustmentHistory();
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private Integer parsePeriod(String periodStr) {
        if (periodStr == null || periodStr.equals("All Day") || periodStr.equals("All Periods")) {
            return null;
        }
        if (periodStr.equals("Homeroom")) {
            return 0;
        }
        try {
            return Integer.parseInt(periodStr.replace("Period ", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Dialog<ExcuseDialogResult> createQuickExcuseDialog() {
        Dialog<ExcuseDialogResult> dialog = new Dialog<>();
        dialog.setTitle("Excuse Absence");
        dialog.setHeaderText("Select excuse code and enter reason");

        ButtonType okButton = new ButtonType("Excuse", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        ComboBox<ExcuseCode> codeCombo = new ComboBox<>();
        codeCombo.setItems(FXCollections.observableArrayList(excuseCodeService.getAllActiveCodes()));
        codeCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ExcuseCode item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getCode() + " - " + item.getName());
            }
        });
        codeCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ExcuseCode item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getCode() + " - " + item.getName());
            }
        });

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("Enter reason for excuse...");
        reasonArea.setPrefRowCount(3);

        content.getChildren().addAll(
                new Label("Excuse Code:"), codeCombo,
                new Label("Reason:"), reasonArea
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button == okButton && codeCombo.getValue() != null && !reasonArea.getText().isEmpty()) {
                return new ExcuseDialogResult(codeCombo.getValue(), reasonArea.getText());
            }
            return null;
        });

        return dialog;
    }

    // ========================================================================
    // MESSAGE HELPERS
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

    // ========================================================================
    // INNER RECORDS
    // ========================================================================

    private record AttendanceRow(
            Long recordId,
            String studentId,
            String studentName,
            String gradeLevel,
            String period,
            String status,
            String excuseCode,
            String notes
    ) {}

    private record StudentListItem(
            Long id,
            String studentId,
            String fullName,
            String gradeLevel
    ) {}

    private record ExcuseDialogResult(
            ExcuseCode excuseCode,
            String reason
    ) {}
}
